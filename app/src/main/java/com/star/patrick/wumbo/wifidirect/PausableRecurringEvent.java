package com.star.patrick.wumbo.wifidirect;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PausableRecurringEvent {
    private ScheduledExecutorService scheduler;
    private boolean isPaused;

    public PausableRecurringEvent(final Runnable task, long initialDelay, long period, TimeUnit unit, boolean initiallyPaused) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        isPaused = initiallyPaused;

        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    task.run();
                }
            }
        }, initialDelay, period, unit);
    }



    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
