package edu.cnan.beehive.core.monitor;

/**
 * Computes per-interval deltas for cumulative (monotonically
 * increasing) metrics such as completed task count and reject count.
 *
 * @author cnan
 */
public class DeltaWrapper {
    private Long lastValue;
    private Long currentValue;

    /**
     * Records a new cumulative value, shifting the current value
     * to {@code lastValue}.
     */
    public synchronized void update(long newValue) {
        this.lastValue = (this.currentValue == null) ? newValue : this.currentValue;
        this.currentValue = newValue;
    }

    /**
     * Returns the delta between the current and previous cycle.
     * @return the delta, or {@code 0} on the first cycle
     */
    public synchronized long getDelta() {
        if (currentValue == null || lastValue == null) {
            return 0;
        }
        return currentValue - lastValue;
    }
}
