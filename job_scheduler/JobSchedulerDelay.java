package job_scheduler;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Job interface representing a task to be executed
interface Job {
    void execute();
    long getDelay();
    TimeUnit getTimeUnit();
}

// Concrete Job implementation example
class PrintJob implements Job {
    private final String message;

    public PrintJob(String message) {
        this.message = message;
    }

    @Override
    public void execute() {
        System.out.println(message);
    }

    @Override
    public long getDelay() {
        return 1; // Delay in seconds
    }

    @Override
    public TimeUnit getTimeUnit() {
        return TimeUnit.SECONDS;
    }
}

// Job Scheduler class following Singleton pattern
class JobScheduler {
    private static JobScheduler instance;
    private final ScheduledExecutorService scheduler;
    private final Map<Job, ScheduledFuture<?>> jobMap;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    // Private constructor for Singleton
    private JobScheduler() {
        scheduler = Executors.newScheduledThreadPool(10); // Fixed thread pool of 10
        jobMap = new ConcurrentHashMap<>();
    }

    // Singleton instance getter
    public static JobScheduler getInstance() {
        if (instance == null) {
            synchronized (JobScheduler.class) {
                if (instance == null) {
                    instance = new JobScheduler();
                }
            }
        }
        return instance;
    }

    // Method to schedule a job
    public void schedule(Job job) {
        if (isShutdown.get()) {
            throw new IllegalStateException("Scheduler is shutdown");
        }
        long delay = job.getDelay();
        TimeUnit unit = job.getTimeUnit();
        
        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(job::execute, delay, delay, unit);
        jobMap.put(job, future);
    }

    // Method to cancel a scheduled job
    public void cancel(Job job) {
        ScheduledFuture<?> future = jobMap.remove(job);
        if (future != null) {
            future.cancel(false);
        }
    }

    // Method to shutdown the scheduler
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            scheduler.shutdown();
        }
    }
}

// Main class demonstrating the job scheduler usage
public class JobSchedulerDelay {
    public static void main(String[] args) {
        JobScheduler jobScheduler = JobScheduler.getInstance();

        Job printJob1 = new PrintJob("Hello from Job 1");
        Job printJob2 = new PrintJob("Hello from Job 2");

        jobScheduler.schedule(printJob1); // Schedule first job
        jobScheduler.schedule(printJob2); // Schedule second job

        // Let it run for a few seconds and then shutdown
        try {
            Thread.sleep(5000); // Run for 5 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        jobScheduler.shutdown(); // Shutdown the scheduler
    }
}

/*
 * Here is implementation of ScheduledService scheduleWithFixedDelay implementation
 * Idea - > delay initially, startTime, execute task, check dealy = curr-startTime, 
 * if(delay < requiredTaskDela) sleep = requiredTaskDela- sleep
 * then thread.sleep(sleep)
 * 
 */



 class FixedDelayScheduler {
    private final ExecutorService executorService;

    public FixedDelayScheduler() {
        executorService = Executors.newSingleThreadExecutor(); // Single-threaded for sequential execution
    }

    public void scheduleWithFixedDelay(Runnable task, long initialDelay, long delay) {
        executorService.submit(() -> {
            try {
                // Initial delay
                Thread.sleep(initialDelay);

                while (!Thread.currentThread().isInterrupted()) {
                    long startTime = System.currentTimeMillis();
                    
                    try {
                        task.run(); // Execute the task
                    } catch (Exception e) {
                        System.err.println("Task execution failed: " + e.getMessage());
                    }

                    // Calculate time taken and adjust sleep for fixed delay
                    long executionTime = System.currentTimeMillis() - startTime;
                    long sleepTime = delay - executionTime;

                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Scheduler interrupted");
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        });
    }

    public void shutdown() {
        executorService.shutdownNow(); // Stop the scheduler
    }

    public static void main(String[] args) {
        FixedDelayScheduler scheduler = new FixedDelayScheduler();

        // Task to execute
        Runnable task = () -> System.out.println("Task executed at: " + System.currentTimeMillis());

        // Schedule task with a 1-second initial delay and 2-second fixed delay
        scheduler.scheduleWithFixedDelay(task, 1000, 2000);

        // Example: Stop scheduler after 10 seconds (for demonstration)
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                scheduler.shutdown();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}


