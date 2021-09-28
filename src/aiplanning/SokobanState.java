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
    }

    public Set<Point> moveBox(Point from, Point to) {
        Set<Point> boxes = new HashSet<>(this.boxPositions);
        boxes.remove(from);
        boxes.add(to);
        return boxes;
    }

    public Point movePlayer(int xDiff, int yDiff) {
        return new Point(this.playerPosition.x + xDiff, this.playerPosition.y + yDiff);
    }


    // Maybe move this to a function in AGS, just 'cause we have a similar method there already
    public Set<Action> getPossibleActions(ObstacleMap om) {
        int x = playerPosition.x;
        int y = playerPosition.y;
        Set<Point> obstacles = om.getObstacles();
        Set<Action> possibleActions = new HashSet<>();
        // TODO: THIS IS REALLY UGLY, BUT CHECK IF IT WORKS FIRST
        if (boxPositions.contains(new Point(x + 1, y)) && (!boxPositions.contains(new Point(x + 2, y))) &&
                (!obstacles.contains(new Point(x + 2, y)))) {
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


    // For debugging purposes only, check if box positions contain player position
    private boolean isInvalid() {
        return boxPositions.contains(playerPosition);
    }

    public SokobanState carryOutAction(AssignmentGlobalStructure.PathPlanningAction action) {
        int xDiff = 0;
        int yDiff = 0;
        boolean moveBox = false;
        switch (action.toString()) {
            // TODO: Lots of redundancy below, probably
            case "RIGHT" -> {
                    xDiff = 1;
            }
            case "LEFT" -> {
                    xDiff = -1;
            }
            case "DOWN" -> {
                    yDiff = 1;
            }
            case "UP" -> {
                    yDiff = -1;
            }
            case "PUSH_RIGHT" -> {
                xDiff = 1;
                moveBox = true;
            }
            case "PUSH_LEFT" -> {
                xDiff = -1;
                moveBox = true;
            }
            case "PUSH_UP" -> {
                yDiff = -1;
                moveBox = true;
            }
            case "PUSH_DOWN" -> {
                yDiff = 1;
                moveBox = true;
            }

        }

        int sum = xDiff + yDiff; // for debugging only
        Point oldPoint = new Point(playerPosition.x + xDiff, playerPosition.y + yDiff);
        Point newPoint = new Point(playerPosition.x + (2 * xDiff), playerPosition.y + (yDiff * 2));
        // Here we always try to move a box no matter what?
        Point newPlayerPoint = movePlayer(xDiff, yDiff);
        if (moveBox) {
            Set<Point> newBoxPoints = moveBox(oldPoint, newPoint);
            return new SokobanState(newPlayerPoint, newBoxPoints, new HashSet<>(goalPositions));
        }
        // TODO: All this copying might be completely unnecessary
        return new SokobanState(newPlayerPoint, new HashSet<>(boxPositions), new HashSet<>(goalPositions));
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
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return this.hashCode() == that.hashCode(); // Worth a shot?
    }

    @Override
    public int hashCode() {
        // Implementation heavily based on Baeldung article
        int hash = 13;
        for (Point box : boxPositions) {
            hash += 31 * hash + box.x;
            hash += 31 * hash + box.y;
        }
        hash += 31 * playerPosition.x;
        hash += 31 * playerPosition.y;
        return hash;
    }

    @Override
    public String toString() {
        return "SokobanState{" +
                "playerPosition=" + playerPosition +
                ", boxPositions=" + boxPositions +
                ", goalPositions=" + goalPositions +
                ", hash=" + hashCode() +
                '}';
    }
}
