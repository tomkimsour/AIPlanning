package aiplanning;

import java.awt.Point;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.PKIXCertPathBuilderResult;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;
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

	public enum PathPlanningAction implements Action {
		UP, DOWN, LEFT, RIGHT, HALT, PUSH_UP, PUSH_DOWN, PUSH_RIGHT, PUSH_LEFT
	}

	public static void main(String[] args) {
		/**
		 * First step of the processing pipeline: sensing
		 * This step provides the decision system with the right information about the environment.
		 * In this case, this information is: where do we start, where do we end, where are the obstacles.
		 */

		File inputFile = Paths.get(args[0]).toFile();
		Set<Point> goalPoints = generateGoalPoints(inputFile);
		Set<Point> boxPoints = generateBoxPoints(inputFile);
		Point start = getStart(inputFile);
		ObstacleMap om = generateObstacleMap(inputFile);
		for (Point obstaclePos : om.getObstacles()) {
			System.out.println("Obstacle at position: " + obstaclePos.x + ", " + obstaclePos.y);
		}

		//A bit of free visualisation, for you to better see the map!
		MapDisplayer md = MapDisplayer.newInstance(om);
		md.setVisible(true);


		State startState = toState(start, boxPoints, goalPoints);
		// Right now saying go back to start, but the player position should be arbitrary
		State goalState = toState(start, goalPoints, goalPoints); // copy might be unnecessary

		/**
		 * Second step of the processing pipeline: deciding
		 * This step projects the pre-processed sensory input into a decision
		 * structure
		 */

		WorldModel<State, Action> wm = generateWorldModel(om, goalPoints);

		System.out.println("Goal Points are: " + goalPoints);
		System.out.println("Box Points are: " + boxPoints.toString());
		System.out.println("Player Position is: " + start.toString());
		System.out.println("Start State is: " + startState.toString());
		System.out.println("Goal State is: " + goalState.toString());
		System.out.println("World Model is: " + wm.toString());

		// NullPointer here
		// Because of call to getProbabilityOf() in DiscreteProbabilityDistributionImpl
		// Probably because we are trying to move into some state which is not valid
		PlanningOutcome po = Planning.resolve(wm, startState, goalState, 50);


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
		PathPlanningState initialPathState = (PathPlanningState) initialState;
		Point initialPoint = new Point(initialPathState.x, initialPathState.y);
		for (PairImpl<State, Action> pair : plan.getStateActionPairs()) {
			PathPlanningAction pathAction = (PathPlanningAction) pair.getRight();
			directions.add(switch (pathAction) {
				case DOWN, PUSH_DOWN -> Path.Direction.SOUTH;
				case UP, PUSH_UP -> Path.Direction.NORTH;
				case RIGHT, PUSH_RIGHT -> Path.Direction.EAST;
				case LEFT, PUSH_LEFT -> Path.Direction.WEST;
				case HALT -> null;
			});
		}
		return new Path(initialPoint, directions);
	}

	private static State toState(Point start, Set<Point> boxes, Set<Point> goals) {
		return new SokobanState(start, boxes, goals);
	}

	private static Set<Point> generateGoalPoints(File inputFile) {
		return new HashSet<>(generatePointsForChar(inputFile, '.'));
	}

	private static Set<Point> generateBoxPoints(File inputFile) {
		return new HashSet<>(generatePointsForChar(inputFile, '$'));
	}

	private static Set<Point> generatePointsForChar(File inputFile, char lookFor) {
		Set<Point> charPoints = new HashSet<>();
		try {
			List<String> lines = Files.readAllLines(inputFile.toPath());
			for (int i = 0; i < lines.size(); i++) {
				String currentLine = lines.get(i);
				for (int ii = 0; ii < currentLine.length(); ii++) {
					if (currentLine.charAt(ii) == lookFor) {
						charPoints.add(new Point(ii, i));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return charPoints;
	}

	// The input file should probably be read line by line
	// Gives row number and then column number within each row
	// Need int width, int height and Set<Point> obstacles
	private static ObstacleMap generateObstacleMap(File inputFile) {
		Set<Point> wallPoints = generatePointsForChar(inputFile, '#'); // Should the boxes be added to the om?
		HashSet<Point> points = new HashSet<>(wallPoints);
		int height = 0;
		int width = 0;
		try {
			List<String> lines = Files.readAllLines(inputFile.toPath());
			height = lines.size();
			for (String line : lines) {
				if (line.length() > width) {
					width = line.length();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ObstacleMap(width, height, points);
	}


	// And here
	private static Point getStart(File inputFile) {
		return new ArrayList<>(generatePointsForChar(inputFile, '@')).get(0);
	}

	private static boolean canMoveTo(Point newPosition, ObstacleMap om, SokobanState state) {
		Set<Point> obstaclePositions = om.getObstacles();
		for (Point obstaclePos : obstaclePositions) {
			if (obstaclePos.x == newPosition.x && obstaclePos.y == newPosition.y) {
				return false;
			}
		} // Not colliding with an obstacle
		for (Point boxPoint : state.boxPositions) {
			if ((boxPoint.x == newPosition.x) && (boxPoint.y == newPosition.y)) {
				return false;
			}
		} // Not colliding with a box

		return true;
	}


	private static WorldModel<State, Action> generateWorldModel(ObstacleMap om, Set<Point> goalPositions) {
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
					SokobanState state_ = (SokobanState) state;
					Set<Action> actions = new HashSet<>();
					actions.add(PathPlanningAction.HALT);
					Point playerPosition = state_.playerPosition;

					if (canMoveTo(new Point(playerPosition.x + 1, playerPosition.y), om, state_)) {
						actions.add(PathPlanningAction.RIGHT);
					}
					if (canMoveTo(new Point(playerPosition.x - 1, playerPosition.y), om, state_)) {
						actions.add(PathPlanningAction.LEFT);
					}
					if (canMoveTo(new Point(playerPosition.x, playerPosition.y + 1), om, state_)) {
						actions.add(PathPlanningAction.DOWN);
					}
					if (canMoveTo(new Point(playerPosition.x, playerPosition.y - 1), om, state_)) {
						actions.add(PathPlanningAction.UP);
					}

					// This should be fine
					actions.addAll(state_.getPossibleActions(om));
					System.out.println("Actions are: " + actions + "in state: " + state_);
					return actions;
				};

		// Move player and boxes by altering the existing state
		BiFunction<State, Action, State> transition = (s, a) -> {
			// A bit ugly with casting
			SokobanState sokobanState = (SokobanState) s;
			PathPlanningAction pathPlanningAction = (PathPlanningAction) a;
			sokobanState.carryOutAction(pathPlanningAction);
			return sokobanState;
		};


		// Makes (pretty valid) assumption that number of boxes matches number of goal positions
		Set<State> states = generateAllStates(om, goalPositions, goalPositions.size());
		System.out.println("All possible states are: ");
		for (State state : states) {
			System.out.println(state);
		}


		BiFunction<State, Action, Double> reward = (s, a) -> {
			SokobanState state = (SokobanState) s;
			PathPlanningAction action = (PathPlanningAction) a;
			// TODO: This call to carryOutAction might be the issue
			if (state.carryOutAction(action).boxDistances() < state.boxDistances()) {
				return 1.0;
			} else if (state.isGoal()) {
				return 10.0; // Want to be in goal state
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


	private static Set<State> generateAllStates(ObstacleMap om, Set<Point> goalPositions, int boxes) {
		List<Point> playerPositions = generateAllPlayerPositions(om);
		Set<Set<Point>> possibleBoxPositions = generateAllBoxPositions(om, boxes);
		Set<State> sokobanStates = new HashSet<>();
		for (Point playerPosition : playerPositions) {
			for (Set<Point> boxPositions : possibleBoxPositions) {
				if (!boxPositions.contains(playerPosition)) { // Only add valid states where player is not in box
					sokobanStates.add(new SokobanState(playerPosition, boxPositions, goalPositions));
				}
			}
		}

		return sokobanStates;
	}


	private static Set<Set<Point>> generateAllBoxPositions(ObstacleMap om, int boxes) {
		List<Point> openPositions = generateAllPlayerPositions(om);
		List<int[]> combinations = Combinations.generateIndexes(openPositions.size(), boxes);
		Set<Set<Point>> pointCombinations = new HashSet<>();
		for (int[] combination : combinations) {
			Set<Point> points = new HashSet<>();
			for (int index : combination) {
				points.add(openPositions.get(index));
			}
			pointCombinations.add(points);
		}
		return pointCombinations;
	}

	private static List<Point> generateAllPlayerPositions(ObstacleMap om) {
		List<Point> playerPositions = new ArrayList<>();
		for (int row = 0; row < om.getHeight(); row++) {
			for (int col = 0; col < om.getWidth(); col++) {
				if (!om.getObstacles().contains(new Point(col, row))) {
					playerPositions.add(new Point(col, row));
				}
			}
		}

		return playerPositions;
	}

}
