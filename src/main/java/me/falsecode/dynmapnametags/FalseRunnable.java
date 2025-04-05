package me.falsecode.dynmapnametags;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class FalseRunnable implements Runnable{
    public static final ArrayList<FalseRunnable> nextTick = new ArrayList<>();

    private ScheduledExecutorService service;
    public void runTaskLater(long delay) {
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.service.schedule(this, delay, TimeUnit.MILLISECONDS);
        this.service.shutdown();
    }

    public Thread runTaskAsync() {
       Thread thread = new Thread(this);
       thread.start();
       return thread;
    }

    public ScheduledExecutorService runTaskTimer(long delay, long period) {
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.service.scheduleAtFixedRate(this, delay, period, TimeUnit.MILLISECONDS);
        return this.service;
    }

    public void runTaskNextTick() {
        nextTick.add(this);
    }

    public void cancel() {
        try {
            this.service.shutdownNow();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public static FalseRunnable of(Runnable runnable) {
        return new FalseRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }
}
