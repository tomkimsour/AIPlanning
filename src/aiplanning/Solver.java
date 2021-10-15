package aiplanning;

import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;
import markov.impl.PairImpl;

import java.util.*;


public class Solver {

    public static<S extends State, A extends Action> List<Action> bfs(WorldModel<State, Action> wm, State start, State goal) {
        List<State> states = newBFS(wm, start, goal);
        List<Action> actions = new ArrayList<>();
        for (int i = 0; i < states.size() - 1; i++) {
            actions.add(getActionForTransition(states.get(i), states.get(i+1)));
        }

        return actions;
    }

    public static<S extends State, A extends Action> List<State> newBFS(WorldModel<State, Action> wm, State start, State goal) {
        Queue<List<State>> pathQueue = new ArrayDeque<>();
        List<State> initialPath = new ArrayList<>();
        initialPath.add(start);
        pathQueue.add(initialPath);
        while (!pathQueue.isEmpty()) {
            List<State> path = pathQueue.poll();
            State node = path.get(path.size()-1); // This should probably not be poll(), should not remove element!
            if (node.equals(goal)) {
                return path;
            }

            Set<Action> possibleActions = wm.getActionsFrom(node);
            for (Action action : possibleActions) {
                State adjacent = wm.getConsequenceOfPlaying(node, action);
                List<State> newPath = new ArrayList<>(path);
                newPath.add(adjacent);
                pathQueue.add(newPath);
            }
        }

        throw new IllegalStateException("No path found!");
    }


    public static <S extends State, A extends Action> Stack<Action> recursiveBFS(WorldModel<State, Action> wm, S startState, S goalState) {
        Stack<Action> actions = new Stack<>();
        Queue<State> toExplore = new ArrayDeque<>();
        toExplore.add(startState);
        bfsHelper(wm, toExplore, new HashSet<>(), goalState, actions);
        return actions;
    }

    // Will probably need to add stack of actions to arguments in order to pass it along and return
    public static <S extends State, A extends Action>PairImpl<Integer, State> bfsHelper(WorldModel<State, Action> wm, Queue<State> toExplore, Set<State> discovered, State goal, Stack<Action> actions) {


        if (toExplore.isEmpty()) {
            return PairImpl.newInstance(0, null);
        }

        State current = toExplore.poll();
        if (current.equals(goal)) {
            return PairImpl.newInstance(1, current);
        }

        for (Action action : wm.getActionsFrom(current)) {
            State newState = wm.getConsequenceOfPlaying(current, action);
            if (!discovered.contains(newState)) {
                discovered.add(newState);
                toExplore.add(newState);
            }
        }

        PairImpl<Integer, State> result = bfsHelper(wm, toExplore, discovered, goal, actions);
        if (result.getLeft().equals(1)) { // Check that Integer(1) == 1 is true
           Action goodAction = getActionForTransition(current, result.getRight());
           actions.add(goodAction);
           return PairImpl.newInstance(1, current);
        }

        return PairImpl.newInstance(0, null);
    }

    public static Action getActionForTransition(State from, State to) {
        PathPlanningState from_ = (PathPlanningState) from;
        PathPlanningState to_ = (PathPlanningState) to;

        switch (to_.x - from_.x) {
            case 1 -> {
                return AssignmentGlobalStructure.PathPlanningAction.RIGHT;
            }
            case -1 -> {
                return AssignmentGlobalStructure.PathPlanningAction.LEFT;
            }
        }
        switch (to_.y - from_.y) {
            case 1 -> {
                return AssignmentGlobalStructure.PathPlanningAction.DOWN;
            }
            case -1 -> {
                return AssignmentGlobalStructure.PathPlanningAction.UP;
            }
        }

        throw new IllegalArgumentException("No connecting action found for given states: " + from + " -> " + to);
    }


    // Probably want to do this recursively as well, since we add all actions otherwise, even those that aren't
    // on the path to the goal
    public static <S extends State, A extends Action> Queue<Action> BFS(WorldModel<State, Action> wm, S startingState, S goalState) {
        Queue<State> states = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        Queue<Action> actions = new ArrayDeque<>(); // The actions we should take to reach the goal, in order
        visited.add(startingState);
        states.add(startingState);
        while (!states.isEmpty()) {
            State state = states.poll();
            if (state.equals(goalState)) {
                return actions; // Change this so it returns the correct actions
            }
            Set<Action> possibleActions = wm.getActionsFrom(state);
            for (Action action : possibleActions) {
                State newState = wm.getConsequenceOfPlaying(state, action);
                if (!visited.contains(newState)) { // Node we haven't visited
                    visited.add(newState);
                    states.add(newState);
                    actions.add(action);
                }
            }
        }

        // Have not found a path to the goal, unreachable!
        throw new IllegalStateException("No path found!");
    }

    // Recursively calls helper in order to add actions that lead to the goal in a Stack
    // The goals are added in the reverse order they should be executed in,
    // i.e. the action that reaches the goal state is added first.
    public static <S extends State, A extends Action> Stack<Action> resolve(WorldModel<State, Action> wm, S startingState, S goalState) {
        List<State> visited = new ArrayList<>();
        Set<Action> startingActions = wm.getActionsFrom(startingState);
        Stack<Action> savedActions = new Stack<>();
        helper(startingState, startingActions, wm, visited, goalState, savedActions);

        return savedActions;
    }


    private static int helper(State state, Set<Action> possibleActions, WorldModel<State, Action> wm, List<State> visited, State goalState, Stack<Action> savedActions) {
        if (state.hashCode() == goalState.hashCode()) { // Have found the goal, don't add more actions
            return -1;
        }
        visited.add(state);
        for (Action action : possibleActions) {
            if (!visited.contains(wm.getConsequenceOfPlaying(state, action))) { // Find first non-visited state
                State newState = wm.getConsequenceOfPlaying(state, action);
                Set<Action> newActions = wm.getActionsFrom(newState);
                int result = helper(newState, newActions, wm, visited, goalState, savedActions); // Visit it
                if (result == -1) { // If we reached the goal
                    savedActions.push(action);
                    return -1;
                }
            }
        }
        return 0;
    }
}
