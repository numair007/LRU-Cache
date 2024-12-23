package prod_cons.self;



import java.util.concurrent.*;

class Message {
    String content;

    Message(String content) {
        this.content = content;
    }
}

class MessageQueue {
    private final ArrayBlockingQueue<Message> queue;

    MessageQueue() {
        queue = new ArrayBlockingQueue<>(10);
    }

    public void publishMessage(String content) {
        try {
            queue.put(new Message(content)); // Blocks if the queue is full
            System.out.println("Message " + content + " Produced by Producer");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Handle interruption
        }
    }

    public Message consume() {
        try {
            return queue.take(); // Blocks until a message is available
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}

class Publisher implements Runnable {
    private final MessageQueue messageQueue;
    private final String id;

    Publisher(MessageQueue messageQueue, String id) {
        this.messageQueue = messageQueue;
        this.id = id;
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            messageQueue.publishMessage("Message-" + i + " by Producer-" + id);
            sleep(100); // Simulate delay
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Subscriber implements Runnable {
    private final MessageQueue messageQueue;

    Subscriber(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            Message message = messageQueue.consume();
            if (message != null) {
                System.out.println("Message " + message.content + " Consumed by Consumer");
            }
        }
    }
}

public class ProducerConsumerSinglePullChatGPT {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue();

        ExecutorService executorService = Executors.newFixedThreadPool(6);

        for (int i = 0; i < 3; i++) {
            executorService.submit(new Publisher(messageQueue, String.valueOf(i)));
        }
        for (int i = 0; i < 3; i++) {
            executorService.submit(new Subscriber(messageQueue));
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Shutdown completed.");
    }
}
