package net.lorgen.easydb.util.concurrency;

import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UtilConcurrency {

    public static final Scheduler SCHEDULER = new Scheduler();

    public static Future<?> submit(Runnable runnable) {
        return SCHEDULER.submit(runnable);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initDelay, long amount, TimeUnit unit) {
        return SCHEDULER.scheduleAtFixedRate(runnable, initDelay, amount, unit);
    }

    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return SCHEDULER.schedule(runnable, delay, unit);
    }

    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return SCHEDULER.schedule(callable, delay, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
        return SCHEDULER.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
    }

    public static <T> ListenableFutureTask<T> submit(Callable<T> callable) {
        return SCHEDULER.submit(callable);
    }

    public static <T> Future<T> submit(Runnable task, T result) {
        return SCHEDULER.submit(task, result);
    }

    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return SCHEDULER.invokeAll(tasks);
    }

    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return SCHEDULER.invokeAll(tasks, timeout, unit);
    }

    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return SCHEDULER.invokeAny(tasks);
    }

    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return SCHEDULER.invokeAny(tasks, timeout, unit);
    }

    public static <T> Callable<T> safe(Callable<T> callable) {
        return SCHEDULER.safe(callable);
    }

    public static Runnable safe(Runnable runnable) {
        return SCHEDULER.safe(runnable);
    }
}
