package aiplanning;

import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;

import java.util.*;


public class Solver {

    // Recursively calls helper in order to add actions that lead to the goal in a Stack
    // The goals are added in the reverse order they should be executed in,
    // i.e. the action that reaches the goal state is added first.
    public static<S extends State, A extends Action> Stack<Action> resolve(WorldModel<State, Action> wm, S startingState, S goalState) {
        List<State> visited = new ArrayList<>();
        Set<Action> startingActions = wm.getActionsFrom(startingState);
        Stack<Action> savedActions = new Stack<>();
        helper(startingState, startingActions, wm, visited, goalState, savedActions);

        return savedActions;
    }


    private static int helper(State state, Set<Action> possibleActions, WorldModel<State, Action> wm, List<State> visited, State goalState, Stack<Action> savedActions) {
        if (state.hashCode() == goalState.hashCode()) { // Have found the goal, don't add more actions
            return - 1;
        }
        visited.add(state);
        for (Action action : possibleActions) {
            if (!visited.contains(wm.getConsequenceOfPlaying(state, action))) { // Find first non-visited state
                State newState = wm.getConsequenceOfPlaying(state, action);
                Set<Action> newActions = wm.getActionsFrom(newState);
                int result = helper(newState, newActions, wm, visited, goalState, savedActions); // Visit it
                if (result == -1) { // If we reached the goal
                    savedActions.push(action);
                    return  -1;
                }
            }
        }
        return 0;
    }
}
