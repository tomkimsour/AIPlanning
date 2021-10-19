package aiplanning;

import deterministicplanning.models.WorldModel;
import deterministicplanning.solvers.planningoutcomes.PlanningOutcome;
import finitestatemachine.Action;
import finitestatemachine.State;
import markov.impl.PairImpl;

import java.util.*;


public class Solver {


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

    public static <S extends State, A extends Action> Stack<Action> BFS(WorldModel<State, Action> wm, S startingState, S goalState) {
        Map<State, State> states = BFSHelper(wm, startingState, goalState);
        State pointer = states.get(goalState);
        Stack<Action> actions = new Stack<>();
        actions.push(getActionForTransition(pointer, goalState));
        while (!pointer.equals(startingState)) {
            State parent = states.get(pointer);
            Action fromParentToState = getActionForTransition(parent, pointer);
            actions.push(fromParentToState);
            pointer = parent;
        }

        return actions;
    }

    public static <S extends State, A extends Action> Map<State, State> BFSHelper(WorldModel<State, Action> wm, S startingState, S goalState) {
        Queue<State> toExplore = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        Map<State, State> parents = new HashMap<>();
        toExplore.add(startingState);

        while (!toExplore.isEmpty()) {
            State current = toExplore.poll();
            visited.add(current);
            Set<Action> possibleActions = wm.getActionsFrom(current);
            for (Action action : possibleActions) {
                State child = wm.getConsequenceOfPlaying(current, action);
                if (!visited.contains(child)) {
                    toExplore.add(child);
                    parents.put(child, current);

                    if (child.equals(goalState)) {
                        return parents;
                    }
                }
            }
        }
        throw new IllegalStateException("No path could be found");
    }

    // Slow as poop
    // ------- Iterative Deepening Depth-First Search --------------
    public static <S extends State, A extends Action> Stack<State> IDDFS(WorldModel<State, Action> wm, S startingState, S goalState) {
        int maxDepth = 50;
        for (int depth = 0; depth < maxDepth; depth++) {
            System.out.println("Depth is: " + depth);
            long startTime = System.nanoTime();
            PairImpl<Stack<State>, Boolean> foundRemaining = DLS(wm, startingState, goalState, depth);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("DFS Duration with depth " + depth + " is: " + duration + "ms");
            if (foundRemaining.getLeft() != null) {
                return foundRemaining.getLeft();
            } else if (!foundRemaining.getRight()) {
                return null;
            }

        }
        return null;
    }

    // Can't return a set of states, order is important
    private static <S extends State, A extends Action> PairImpl<Stack<State>, Boolean> DLS(WorldModel<State, Action> wm, S currentState, S goalState, int depth) {

        if (depth == 0) {
            if (currentState.equals(goalState)) {
                Stack<State> states = new Stack<>();
                states.add(currentState);
                return PairImpl.newInstance(states, true); // will only ever return the goal state
            } else {
                // Return something signifying that there may be remaining nodes to explore

                return PairImpl.newInstance(null, true);
            }
        } else if (depth > 0) {
            boolean remaining = false;
            Set<Action> possibleActions = wm.getActionsFrom(currentState);
            for (Action action : possibleActions) {
                State child = wm.getConsequenceOfPlaying(currentState, action);
                PairImpl<Stack<State>, Boolean> foundRemaining = DLS(wm, child, goalState, depth - 1);
                if (foundRemaining.getLeft() != null) {
                    foundRemaining.getLeft().push(currentState);
                    return PairImpl.newInstance(foundRemaining.getLeft(), true);
                }
                if (foundRemaining.getRight()) {
                    remaining = true;
                }
            }

            return PairImpl.newInstance(null, remaining);
        }
        return null;
    }


}
