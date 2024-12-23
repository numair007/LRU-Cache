package JobSchedulerDependency;


import java.util.*;
import java.util.concurrent.*;

// Task Class
class Task {
    private final String name;
    private final Runnable action;

    public Task(String name, Runnable action) {
        this.name = name;
        this.action = action;
    }

    public void execute() {
        System.out.println("Executing Task: " + name);
        action.run();
    }

    public String getName() {
        return name;
    }
}

// JobScheduler Class
class JobScheduler {
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public void schedule(Map<String, Task> tasks, Map<String, List<String>> dependencies) {
        // Initialize CountDownLatches for each task
        Map<String, CountDownLatch> latches = new HashMap<>();
        tasks.forEach((taskName, task) -> 
            latches.put(taskName, new CountDownLatch(dependencies.get(taskName).size()))
        );

        // Submit tasks for execution
        tasks.forEach((taskName, task) -> executorService.submit(() -> {
            try {
                // Wait for prerequisites
                latches.get(taskName).await();

                // Execute the task
                task.execute();

                // Notify dependent tasks
                dependencies.forEach((dependentTask, prereqs) -> {
                    if (prereqs.contains(taskName)) {
                        latches.get(dependentTask).countDown();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

// Main Class
public class JobSchedulerDependencyDriver {
    public static void main(String[] args) throws InterruptedException {
        Map<String, Task> tasks = new HashMap<>();
        Map<String, List<String>> dependencies = new HashMap<>();

        // Define tasks
        tasks.put("Task1", new Task("Task1", () -> sleep(1000)));
        tasks.put("Task2", new Task("Task2", () -> sleep(1000)));
        tasks.put("Task3", new Task("Task3", () -> sleep(1000)));
        tasks.put("Task4", new Task("Task4", () -> sleep(1000)));

        // Define dependencies
        dependencies.put("Task1", Collections.emptyList());
        dependencies.put("Task2", Collections.emptyList());
        dependencies.put("Task3", List.of("Task1"));
        dependencies.put("Task4", List.of("Task2"));

        // Schedule and execute tasks
        JobScheduler scheduler = new JobScheduler();
        scheduler.schedule(tasks, dependencies);
        scheduler.shutdown();
    }

    // Helper method for simulating task execution
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
/*
 * CountDownLatch
 * What is CountDownLatch?
CountDownLatch is a synchronization aid provided by Java's java.util.concurrent package. It allows one or more threads to wait until a set of operations being performed by other threads is complete.

It works by maintaining a count, initialized when the latch is created. Threads can:

Decrement the count using the countDown() method, indicating that one operation is complete.
Wait until the count reaches zero using the await() method, at which point all waiting threads are released to proceed.
Key Methods
CountDownLatch(int count): Constructs a latch initialized with a given count.
countDown(): Decrements the count by 1. When it reaches zero, all waiting threads are released.
await(): Blocks the calling thread until the count reaches zero.
Why is CountDownLatch Useful Here?
In the context of the multithreaded job scheduler, tasks often have prerequisites that must be completed before they can execute. For example:

Task3 might depend on Task1 being completed.
Task4 might depend on Task2 being completed.
We use CountDownLatch to synchronize task execution, ensuring that tasks are executed only after their prerequisites are complete.

How CountDownLatch is Used in the Code?
Initialize CountDownLatch for Prerequisites: Each task has a CountDownLatch initialized with the number of prerequisites it depends on:

java
Copy code
Map<String, CountDownLatch> taskLatches = new HashMap<>();
for (String taskName : tasks.keySet()) {
    taskLatches.put(taskName, new CountDownLatch(prerequisites.get(taskName).size()));
}
Tasks Wait for Prerequisites: Before a task runs, it calls await() on its latch to wait until its prerequisites are completed:

java
Copy code
taskLatches.get(taskName).await(); // Wait for prerequisites
task.run(); // Execute task
Notify Prerequisite Completion: When a task finishes, it notifies dependent tasks by calling countDown() on their latches:

java
Copy code
task.addObserver(completedTask -> {
    for (Map.Entry<String, List<String>> prereqEntry : prerequisites.entrySet()) {
        if (prereqEntry.getValue().contains(completedTask)) {
            taskLatches.get(prereqEntry.getKey()).countDown();
        }
    }
});
Benefits of Using CountDownLatch in this Scheduler
Simplifies Dependency Management: The latch ensures that tasks only proceed when all prerequisites are met without manually tracking dependency states.

Thread-Safe Synchronization: Multiple threads can decrement the latch safely, ensuring consistent behavior in concurrent environments.

Maximizes Parallelism: Tasks without prerequisites start immediately, while others wait just long enough for dependencies to complete.

Clear Flow Control: The use of await() and countDown() provides a clear and simple way to enforce dependencies, making the code easier to understand and maintain.

Example Flow
For a job with the following dependencies:

Task3 depends on Task1
Task4 depends on Task2
Task3's latch is initialized with a count of 1 (due to Task1).
Task4's latch is initialized with a count of 1 (due to Task2).
When Task1 completes, it calls countDown() on Task3's latch.
Task3 is released from waiting and begins execution.
Similarly, Task2 releases Task4.
This approach ensures tasks are executed in the correct order while achieving maximum parallel execution where possible.
 */

