package aiplanning;

import finitestatemachine.Action;
import finitestatemachine.State;

public class StateNode implements Comparable<StateNode> {
    private final State current;
    private State previous;
    private Action action; // TODO: Check if this is good
    private double gScore;
    private double hScore;

    StateNode(State current) {
        this(current, null, null, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    StateNode(State current, State previous, Action action, double gScore, double hScore) {
        this.current = current;
        this.previous = previous;
        this.action = action;
        this.gScore = gScore;
        this.hScore = hScore;
    }

    public State getCurrent() {
        return current;
    }

    public State getPrevious() {
        return previous;
    }

    public double gethScore() {
        return hScore;
    }

    public void setPrevious(State previous) {
        this.previous = previous;
    }

    public void sethScore(double hScore) {
        this.hScore = hScore;
    }

    public void setgScore(double gScore) {
        this.gScore = gScore;
    }

    @Override
    public int compareTo(StateNode o) {
        return Double.compare(this.gScore, o.gScore);
    }
}
