package JobSchedulerDependency.self;

import java.util.*;
import java.util.concurrent.*;

class Task{
    String id;
    Runnable action;

    Task(String id, Runnable run){
        this.id = id;
        this.action = run;
    }

    public void execute(){
        System.out.println("Task "+id+" Execution");
        action.run();
    }

}

class JobScheduler{
    ExecutorService executorService = Executors.newFixedThreadPool(5);
    //Map<String, CountDownLatch> latches;

    public void schedule(Map<String, Task> tasks, Map<String, List<String>> pre){

        Map<String, CountDownLatch> latches = new HashMap<>();

        tasks.forEach((name, task) -> {
            latches.put(name, new CountDownLatch(pre.get(name).size()));
        });

        tasks.forEach((name, task) -> {
            executorService.submit(() -> {
                CountDownLatch currCountDownLatch = latches.get(name);
                try{
                    currCountDownLatch.await();
                    task.execute();
                    pre.forEach((taskName, list) -> {
                        if(list.contains(name)){
                            latches.get(taskName).countDown();
                        }
                    });

                }
                catch(InterruptedException exception){

                }
                
            });
        });
        

    }
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

public class JobSchedulerDependency{
    public static void main(String[] args) throws InterruptedException {
        Map<String, Task> tasks = new HashMap<>();
        Map<String, List<String>> pre = new HashMap<>();

        tasks.put("Task1", new Task("Task1", () -> sleep(1000)));
        tasks.put("Task2", new Task("Task2", () -> sleep(1000)));
        tasks.put("Task3", new Task("Task3", () -> sleep(1000)));
        tasks.put("Task4", new Task("Task4", () -> sleep(1000)));

        // Define dependencies
        pre.put("Task1", Collections.emptyList());
        pre.put("Task2", List.of("Task2"));
        pre.put("Task3", List.of("Task1"));
        pre.put("Task4", List.of("Task2"));

        // Schedule and execute tasks
        JobScheduler scheduler = new JobScheduler();
        scheduler.schedule(tasks, pre);
        scheduler.shutdown();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}