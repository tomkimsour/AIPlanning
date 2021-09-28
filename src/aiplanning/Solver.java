package aiplanning;

import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;

import java.util.*;


public class Solver {

    // Maybe start with DFS or BFS, so that the complexity is not from the actual algorithm?
    // maxHorizon might not be necessary depending on what search we use
    // We want to save the actions somehow, perhaps in a queue?
    // Then return that and match
    public static<S extends State, A extends Action> Queue<Action> resolve(WorldModel<State, Action> wm, S startingState, S goalState) {
        List<State> visited = new ArrayList<>();
        Set<Action> startingActions = wm.getActionsFrom(startingState);
        Queue<Action> savedActions = new ArrayDeque<>(); // TODO: Make sure this is a reasonable choice
        // How to determine what action to take?
        // Maybe always take first action?
        helper(startingState, startingActions, wm, visited, goalState, savedActions);

        return savedActions;
    }

    // Will probably need this for recursion
    private static int helper(State state, Set<Action> possibleActions, WorldModel<State, Action> wm, List<State> visited, State goalState, Queue<Action> savedActions) {
        if (state.hashCode() == goalState.hashCode()) { // Have found the goal, don't add more actions
            return - 1; // This probably does not stop the iteration from continuing in the calling method
        }
        visited.add(state); // Mark state as visited
        for (Action action : possibleActions) {
            if (!visited.contains(wm.getConsequenceOfPlaying(state, action))) { // Find first non-visited state
                State newState = wm.getConsequenceOfPlaying(state, action);
                Set<Action> newActions = wm.getActionsFrom(newState);
                savedActions.add(action);
                int result = helper(newState, newActions, wm, visited, goalState, savedActions); // Visit it
                if (result == -1) {
                    break;
                }
            }
        }
        return 0;
    }
}
