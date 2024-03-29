package aiplanning;

import finitestatemachine.State;

import java.awt.*;
import java.util.Objects;

// For the path-planning, the state should probably only be the position of the player
public class PathPlanningState implements State {
    int x;
    int y;

    public PathPlanningState(Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public PathPlanningState(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathPlanningState that = (PathPlanningState) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "PathPlanningState{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
