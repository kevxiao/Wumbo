package com.star.patrick.wumbo.wifidirect;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestartableEvent {
    private ScheduledExecutorService scheduler;
    private Runnable task;
    private boolean finished;

    public RestartableEvent(final Runnable task) {
        this.task = task;
        scheduler = null;
        finished = true;
    }

    public void restart(long initialDelay) {
        restart(initialDelay, TimeUnit.SECONDS);
    }

    public void restart(long initialDelay, TimeUnit unit) {
        if (!finished) {
            return;
        }

        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        finished = false;

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                task.run();
                finished = true;
            }
        }, initialDelay, unit);
    }
}
