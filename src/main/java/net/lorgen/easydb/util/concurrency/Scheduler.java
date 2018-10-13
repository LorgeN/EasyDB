package net.lorgen.easydb.util.concurrency;

import com.google.common.util.concurrent.ListenableFutureTask;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * TODO: Make this entire thing an interface to allow for custom implementations
 */
public class Scheduler {

    private ScheduledExecutorService executor;

    public Scheduler() {
        this(5);
    }

    public Scheduler(int corePool) {
        this.executor = Executors.newScheduledThreadPool(corePool);
    }

    public Future<?> submit(Runnable runnable) {
        return this.executor.submit(this.safe(runnable));
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initDelay, long amount, TimeUnit unit) {
        return this.executor.scheduleAtFixedRate(this.safe(runnable), initDelay, amount, unit);
    }

    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit) {
        return this.executor.schedule(this.safe(runnable), delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executor.schedule(this.safe(callable), delay, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(this.safe(runnable), initialDelay, delay, unit);
    }

    public <T> ListenableFutureTask<T> submit(Callable<T> callable) {
        ListenableFutureTask<T> task = ListenableFutureTask.create(this.safe(callable));
        this.executor.submit(task);
        return task;
    }

    public <T> Future<T> submit(Runnable task, T result) {
        return executor.submit(this.safe(task), result);
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks.stream().map(this::safe).collect(Collectors.toList()));
    }

    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executor.invokeAll(tasks.stream().map(this::safe).collect(Collectors.toList()), timeout, unit);
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasks.stream().map(this::safe).collect(Collectors.toList()));
    }

    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks.stream().map(this::safe).collect(Collectors.toList()), timeout, unit);
    }

    protected <T> Callable<T> safe(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        };
    }

    protected Runnable safe(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
    }
}
