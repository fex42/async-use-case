package com.ing.diba.metrics;

import com.ing.diba.metrics.codahale.DefaultTimer;

/**
 * Created by dhaa on 12.06.16.
 */
public abstract class InstrumentedComponent {

    private DefaultTimer defaultTimer;

    public InstrumentedComponent() {
    }

    public DefaultTimer getDefaultTimer() {
        return defaultTimer;
    }

    public void setDefaultTimer(DefaultTimer defaultTimer) {
        this.defaultTimer = defaultTimer;
        if (this.defaultTimer != null) {
            initTimer();
        }
    }

    protected boolean initTimer(final String name, final String[] names) {
        if (this.defaultTimer != null) {
            return this.defaultTimer.init(name, names);
        }
        return false;
    }


    abstract protected boolean initTimer();

    protected Object start() {
        if (this.defaultTimer != null) {
            return this.defaultTimer.start();
        }
        return null;
    }

    protected void stop(final Object context) {
        if (this.defaultTimer != null) {
            this.defaultTimer.stop(context);
        }
    }

}