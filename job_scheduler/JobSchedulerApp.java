package job_scheduler;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

// Job Interface
interface Job {
    void execute();
}

// Concrete Job Implementation
class PrintJob implements Job {
    private final String message;

    public PrintJob(String message) {
        this.message = message;
    }

    @Override
    public void execute() {
        System.out.println("Executing PrintJob: " + message);
    }
}

// Factory for creating jobs
class JobFactory {
    public static Job createJob(String type, String... params) {
        if ("PRINT".equalsIgnoreCase(type)) {
            return new PrintJob(params[0]);
        }
        // Add more job types as required
        return null;
    }
}

// Job Scheduler Singleton
class JobScheduler {
    private static final JobScheduler INSTANCE = new JobScheduler();
    private final ExecutorService executorService;

    private JobScheduler() {
        this.executorService = Executors.newFixedThreadPool(10); // Pool of 10 threads
    }

    public static JobScheduler getInstance() {
        return INSTANCE;
    }

    public void submitJob(Job job) {
        executorService.submit(() -> {
            try {
                job.execute();
            } catch (Exception e) {
                System.err.println("Job execution failed: " + e.getMessage());
            }
        });
    }
    // This is same as above, we pass runnable class to submit method, submit returns a future object
    /*
     * public void submitJob(Job job) {
    executorService.submit(new Runnable() {
        @Override
        public void run() {
            try {
                job.execute(); // Execute the job's logic
            } catch (Exception e) {
                System.err.println("Job execution failed: " + e.getMessage());
            }
        }
    });
}
     */

    public void shutdown() {
        executorService.shutdown();
    }
}

// Observer for monitoring job status
class JobStatusObserver {
    public void onJobCompleted(Job job) {
        System.out.println("Job Completed: " + job);
    }

    public void onJobFailed(Job job, Exception e) {
        System.err.println("Job Failed: " + job + ", Error: " + e.getMessage());
    }
}

// Main Application
public class JobSchedulerApp {
    public static void main(String[] args) {
        JobScheduler scheduler = JobScheduler.getInstance();
        JobStatusObserver observer = new JobStatusObserver();

        // Creating and submitting jobs
        Job printJob1 = JobFactory.createJob("PRINT", "Hello World!");
        Job printJob2 = JobFactory.createJob("PRINT", "Design Patterns in Java");

        scheduler.submitJob(printJob1);
        scheduler.submitJob(printJob2);

        // Simulate completion status
        observer.onJobCompleted(printJob1);
        observer.onJobCompleted(printJob2);

        scheduler.shutdown();
    }
}
