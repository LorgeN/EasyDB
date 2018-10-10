package net.lorgen.easydb.util.concurrency;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScheduledRunnable implements Runnable {

    private Scheduler scheduler;
    private Future<?> future;

    public ScheduledRunnable(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Future<?> submit() {
        return this.future = scheduler.submit(this);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(long initDelay, long amount, TimeUnit unit) {
        return (ScheduledFuture<?>) (this.future = scheduler.scheduleAtFixedRate(this, initDelay, amount, unit));
    }

    public ScheduledFuture<?> schedule(long delay, TimeUnit unit) {
        return (ScheduledFuture<?>) (this.future = scheduler.schedule(this, delay, unit));
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<?>) (this.future = scheduler.scheduleWithFixedDelay(this, initialDelay, delay, unit));
    }

    public <T> Future<T> submit(T result) {
        return (Future<T>) (this.future = scheduler.submit(this, result));
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Future<?> getFuture() {
        return future;
    }

    public void cancel() {
        this.getFuture().cancel(false);
    }
}
