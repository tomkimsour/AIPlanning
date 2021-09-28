package aiplanning;

import deterministicplanning.models.WorldModel;
import finitestatemachine.Action;
import finitestatemachine.State;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;


public class Solver {

    //TODO: Implement this
    public static void findPath(WorldModel<State, Action> wm, State startState, State goalState) {
        StateNode initial = new StateNode(startState);

    }

    // For path planning, heuristic can probably just be euclidean distance between player and goal
    private static double heuristic(State startingState, State goalState) {
        PathPlanningState state = (PathPlanningState) startingState;
        PathPlanningState goalState_ = (PathPlanningState) goalState;
        return Point2D.distance(state.x, state.y, goalState_.x, goalState_.y);
    }
}
