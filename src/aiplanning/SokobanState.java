package aiplanning;

import finitestatemachine.Action;
import finitestatemachine.State;
import obstaclemaps.ObstacleMap;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SokobanState implements State {

    Point playerPosition;
    Set<Point> boxPositions;
    Set<Point> goalPositions;


    public SokobanState(Point playerPosition, Set<Point> boxPositions, Set<Point> goalPositions) {
        this.playerPosition = playerPosition;
        this.boxPositions = boxPositions;
        this.goalPositions = goalPositions;
        System.out.println("Invalid state: " + this.isInvalid());
    }

    public void moveBox(Point from, Point to) {
        System.out.println("boxPositions.contains() box we want to move: " + boxPositions.contains(from));
        // TODO: Make sure we cannot move boxes into each other
        System.out.println("boxPositions.contains() position we want to move to: " + boxPositions.contains(to));
        this.boxPositions.remove(from);
        this.boxPositions.add(to);
    }

    public void movePlayer(int xDiff, int yDiff) {
        this.playerPosition.x += xDiff;
        this.playerPosition.y += yDiff;
    }


    // Maybe move this to a function in AGS, just 'cause we have a similar method there already
    public Set<Action> getPossibleActions(ObstacleMap om) {
        int x = playerPosition.x;
        int y = playerPosition.y;
        Set<Point> obstacles = om.getObstacles();
        Set<Action> possibleActions = new HashSet<>();
        // TODO: THIS IS REALLY UGLY, BUT CHECK IF IT WORKS FIRST
        if (boxPositions.contains(new Point(x + 1, y)) && (!boxPositions.contains(new Point(x + 2, y))) &&
                !obstacles.contains(new Point(x + 2, y))) {
            possibleActions.add(AssignmentGlobalStructure.PathPlanningAction.PUSH_RIGHT);
        }
        if (boxPositions.contains(new Point(x - 1, y)) && (!boxPositions.contains(new Point(x - 2, y))) &&
                (!obstacles.contains(new Point(x - 2, y)))) {
            possibleActions.add(AssignmentGlobalStructure.PathPlanningAction.PUSH_LEFT);
        }
        if (boxPositions.contains(new Point(x, y + 1)) && (!boxPositions.contains(new Point(x, y + 2))) &&
                (!obstacles.contains(new Point(x, y + 2)))) {
            possibleActions.add(AssignmentGlobalStructure.PathPlanningAction.PUSH_DOWN);
        }
        if (boxPositions.contains(new Point(x, y - 1)) && (!boxPositions.contains(new Point(x, y - 2))) &&
                (!obstacles.contains(new Point(x, y - 2)))) {
            possibleActions.add(AssignmentGlobalStructure.PathPlanningAction.PUSH_UP);
        }

        return possibleActions;
    }

    public boolean isGoal() {
        return boxPositions.equals(goalPositions);
    }

    // For debugging purposes only, check if box positions contain player position
    private boolean isInvalid() {
        return boxPositions.contains(playerPosition);
    }

    public SokobanState carryOutAction(SokobanState initial, AssignmentGlobalStructure.PathPlanningAction action) {
        // TODO: Use this in AssignmentGlobalStructure
        int xDiff = 0;
        int yDiff = 0;
        switch (action.toString()) {
            case "RIGHT" -> xDiff = 1;
            case "LEFT" -> xDiff = -1;
            case "DOWN" -> yDiff = 1;
            case "UP" -> yDiff = -1;
            case "PUSH_RIGHT" -> {
                xDiff = 1;
                Point oldPoint = new Point(initial.playerPosition.x + xDiff, initial.playerPosition.y);
                Point newPoint = new Point(initial.playerPosition.x + (xDiff * 2), initial.playerPosition.y);
                initial.moveBox(oldPoint, newPoint); // Should this really be playerPosition???? NO!
            }
            case "PUSH_LEFT" -> {
                xDiff = -1;
                Point oldPoint = new Point(initial.playerPosition.x + xDiff, initial.playerPosition.y);
                Point newPoint = new Point(initial.playerPosition.x + (xDiff * 2), initial.playerPosition.y);
                initial.moveBox(oldPoint, newPoint);
            }
            case "PUSH_UP" -> {
                yDiff = -1;
                Point oldPoint = new Point(initial.playerPosition.x, initial.playerPosition.y + yDiff);
                Point newPoint = new Point(initial.playerPosition.x, initial.playerPosition.y + (yDiff * 2));
                initial.moveBox(oldPoint, newPoint);
            }
            case "PUSH_DOWN" -> {
                yDiff = 1;
                Point oldPoint = new Point(initial.playerPosition.x, initial.playerPosition.y + yDiff);
                Point newPoint = new Point(initial.playerPosition.x, initial.playerPosition.y + (yDiff * 2));
                initial.moveBox(oldPoint, newPoint);
            }
        }

        initial.movePlayer(xDiff, yDiff);
        return initial;
    }

    // Should the distance be from each box to each goal point?
    // Seems like it would get us into trouble
    // Maybe distance from each box to the closest goal without box?
    public double boxDistances() {
        double distance = 0;
        // Find free goals
        Set<Point> freeGoals = new HashSet<>();
        for (Point goal : goalPositions) {
            if (!hasBox(goal)) {
                freeGoals.add(goal);
            }
        }
        // Calculate distance from each box to the closest goal
        for (Point box : boxPositions) {
            distance += distanceToClosestGoal(box, freeGoals);
        }

        return distance;
    }


    private boolean hasBox(Point goal) {
        for (Point box : goalPositions) {
            if (Point2D.distance(box.x, box.y, goal.x, goal.y) <= 0.00001) {
                return true;
            }
        }
        return false;
    }

    private double distanceToClosestGoal(Point box, Set<Point> goalPositions) {
        double closestDistance = Double.POSITIVE_INFINITY;
        for (Point goal : goalPositions) {
            double distance = Point2D.distance(box.x, box.y, goal.x, goal.y);
            if (distance < closestDistance) {
                closestDistance = distance;
            }
        }

        return closestDistance;
    }

    // This should probably only take the box- and goal-positions into account, i.e. NOT the player position
    // Makes defining the goal state easier, otherwise we have to care about where the player is at
    // TODO: Try adding back playerPositions
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return Objects.equals(boxPositions, that.boxPositions) && Objects.equals(goalPositions, that.goalPositions);
    }

    // TODO: Maybe add playerPosition here as well, am trying without it
    @Override
    public int hashCode() {
        return Objects.hash(boxPositions, goalPositions);
    }

    @Override
    public String toString() {
        return "SokobanState{" +
                "playerPosition=" + playerPosition +
                ", boxPositions=" + boxPositions +
                ", goalPositions=" + goalPositions +
                '}';
    }
}
