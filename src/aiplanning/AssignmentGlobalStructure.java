package aiplanning;

import java.awt.Point;
import java.io.*;
import java.nio.file.Paths;
import java.security.cert.PKIXCertPathBuilderResult;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import deterministicplanning.models.FunctionBasedDeterministicWorldModel;
import deterministicplanning.models.Plan;
import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.Planning;
import deterministicplanning.solvers.planningoutcomes.FailedPlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import deterministicplanning.solvers.planningoutcomes.SuccessfulPlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;
import finitestatemachine.impl.StringStateImpl;
import markov.impl.PairImpl;
import obstaclemaps.MapDisplayer;
import obstaclemaps.ObstacleMap;
import obstaclemaps.Path;

public class AssignmentGlobalStructure {

	//TODO: Fix bug that requires all lines in the read file to be the same length
	private enum PathPlanningAction implements Action {
		UP, DOWN, LEFT, RIGHT, HALT
	}

	public static void main(String[] args) {
		/**
		 * First step of the processing pipeline: sensing
		 * This step provides the decision system with the right information about the environment.
		 * In this case, this information is: where do we start, where do we end, where are the obstacles.
		 */


		File inputFile = Paths.get(args[0]).toFile();
		FileFormatter.formatFile(inputFile);
		Point start = getStart(inputFile);
		Point goal = getEnd(inputFile);
		ObstacleMap om = generateObstacleMap(inputFile);


		//A bit of free visualisation, for you to better see the map!
		MapDisplayer md = MapDisplayer.newInstance(om);
		md.setVisible(true);


		State startState = toState(start);
		State goalState = toState(goal);

		/**
		 * Second step of the processing pipeline: deciding
		 * This step projects the pre-processed sensory input into a decision
		 * structure
		 */

		System.out.println("Start String State is: " + startState);
		System.out.println("Goal String State is: " + goalState);

		WorldModel<State, Action> wm = generateWorldModel(om, goal);


		PlanningOutcome po = Planning.resolve(wm, startState, goalState, 200);



		/**
		 * Third step of the processing pipeline: action
		 * This step turns the outcome of the decision into a concrete action:
		 * either printing that no plan is found or which plan is found.
		 */
		if (po instanceof FailedPlanningOutcome) {
			System.out.println("No plan could be found.");
		} else {
			Plan<State, Action> plan = ((SuccessfulPlanningOutcome) po).getPlan();
			Path p = planToPath(plan);
			md.setPath(p);
			System.out.println("Path found:" + p);
		}
	}

	private static Path planToPath(Plan<State, Action> plan) {
		// A Path needs a starting point and a list of Directions
		// which are NORTH, SOUTH, EAST, WEST
		List<Path.Direction> directions = new ArrayList<>();
		State initialState = plan.getStateActionPairs().get(0).getLeft();
		System.out.println("Initial state in planning is: " + initialState);
		PathPlanningState initialPathState = (PathPlanningState) initialState;
		Point initialPoint = new Point(initialPathState.x, initialPathState.y);
		for (PairImpl<State, Action> pair : plan.getStateActionPairs()) {
			PathPlanningAction pathAction = (PathPlanningAction) pair.getRight();
			directions.add(switch (pathAction) {
				case DOWN -> Path.Direction.SOUTH;
				case UP -> Path.Direction.NORTH;
				case RIGHT -> Path.Direction.EAST;
				case LEFT -> Path.Direction.WEST;
				case HALT -> null;
			});
		}
		return new Path(initialPoint, directions);
	}

	private static State toState(Point start) {
		return new PathPlanningState(start);
	}

	// The input file should probably be read line by line
	// Gives row number and then column number within each row
	// Need int width, int height and Set<Point> obstacles
	private static ObstacleMap generateObstacleMap(File inputFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			Set<Point> obstacles = new HashSet<>();
			int currentRow = 0;
			String currentLine;
			int maxCol = 0;
			String obstacleCharacters = "#$";
			while ((currentLine = reader.readLine()) != null) {
				CharacterIterator iterator = new StringCharacterIterator(currentLine);
				int currentCol = 0;
				while (iterator.current() != CharacterIterator.DONE) {


					if (obstacleCharacters.indexOf(iterator.current()) >= 0) {
						obstacles.add(new Point(currentCol, currentRow));
					}

					currentCol++;
					if (currentCol > maxCol) {
						maxCol = currentCol;
					}
					iterator.next();
				}
				currentRow++;
			}
			return ObstacleMap.newInstance(maxCol, currentRow, obstacles);
		} catch (IOException e) {
			e.printStackTrace();
		}

		throw new Error("To be implemented");
	}

	// Same thing here
	private static Point getEnd(File inputFile) {
		return getCharacterPosition(inputFile, '.');
	}

	// And here
	private static Point getStart(File inputFile) {
		return getCharacterPosition(inputFile, '@');
	}

	private static Point getCharacterPosition(File inputFile, char character) {
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			String currentLine;
			int currentRow = 0;
			while ((currentLine = reader.readLine()) != null) {
				int currentCol = 0;
				CharacterIterator iterator = new StringCharacterIterator(currentLine);

				while (iterator.current() != CharacterIterator.DONE) {
					if (iterator.current() == character) {
						return new Point(currentCol, currentRow); // Should be (x, y)
					}

					currentCol++;
					iterator.next();
				}
				currentRow++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		throw new Error("To be implemented");
	}


	private static WorldModel<State, Action> generateWorldModel(ObstacleMap om, Point goal) {
		/**
		 * This is where you describe your own word model. Checkout deterministicplanning.mains.MainForAiDeveloppers or
		 * deterministicplanning.mains.MainMinimalItKnowledge for some examples of how to implement such function.
		 *
		 * Note:
		 * In the minimal example presented above, all states, actions, transitions, etc were "hardcoded".
		 * However, in the context of our exercise, programmatic constructs will become necessary,
		 * such as "for" loops and "if then else" blocks.
		 */

		Function<State, Set<Action>> actionsPerState =
				state -> {
					PathPlanningState state_ = (PathPlanningState) state;
					Set<Action> actions = new HashSet<>();
					actions.add(PathPlanningAction.HALT);

					Set<Point> obstaclePositions = om.getObstacles();

					if (!obstaclePositions.contains(new Point(state_.x + 1, state_.y))) {
						actions.add(PathPlanningAction.RIGHT);
					}
					if (!obstaclePositions.contains(new Point(state_.x - 1, state_.y))) {
						actions.add(PathPlanningAction.LEFT);
					}
					if (!obstaclePositions.contains(new Point(state_.x, state_.y + 1))) {
						actions.add(PathPlanningAction.DOWN);
					}
					if (!obstaclePositions.contains(new Point(state_.x, state_.y - 1))) {
						actions.add(PathPlanningAction.UP);
					}


					return actions;
				};

		BiFunction<State, Action, State> transition = (s, a) -> {
			PathPlanningState state_ = (PathPlanningState) s;


			return switch (a.toString()) {
				case "RIGHT" -> new PathPlanningState(state_.x + 1, state_.y);
				case "LEFT" -> new PathPlanningState(state_.x - 1, state_.y);
				case "DOWN" -> new PathPlanningState(state_.x, state_.y + 1);
				case "UP" -> new PathPlanningState(state_.x, state_.y - 1);
				default -> s;
			};
		};


		Set<State> states = new HashSet<>();
		for (int row = 0; row < om.getHeight() - 1; row++) {
			for (int col = 0; col < om.getWidth(); col++) {
				if (!om.getObstacles().contains(new Point(col, row))) {
					states.add(new PathPlanningState(col, row));
				}
			}
		}

		// Need a suitable reward function
		// Should all moves just have reward -1 so that as few moves as possible is recommended?
		BiFunction<State, Action, Double> reward = (s, a) -> {
			if (new PathPlanningState(goal.x, goal.y).equals(s)) {
				return 1.0;
			}
			return -1.0;
		};

		return FunctionBasedDeterministicWorldModel.newInstance(
				states,
				transition,
				reward,
				actionsPerState
		);

	}

}
