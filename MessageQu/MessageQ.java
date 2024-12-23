package MessageQu;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Message Class
 class Message {
    private final String id;
    private final String payload;
    private final long timestamp;

    public Message(String id, String payload) {
        this.id = id;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public String getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

// MessageQueue Class
class MessageQueue {
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();

    public void publish(Message message) {
        queue.add(message); // Thread-safe
    }

    public Message consume() {
        try {
            return queue.take(); // Blocks if queue is empty
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consumer interrupted", e);
        }
    }

    public int getQueueSize() {
        return queue.size();
    }
}

// Publisher Class
class Publisher {
    private final MessageQueue messageQueue;

    public Publisher(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    public void publish(String payload) {
        String id = java.util.UUID.randomUUID().toString();
        Message message = new Message(id, payload);
        messageQueue.publish(message);
        System.out.println("Published: " + message.getId());
    }
}

// Subscriber Class
class Subscriber implements Runnable {
    private final MessageQueue messageQueue;

    public Subscriber(MessageQueue messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        while (true) {
            Message message = messageQueue.consume();
            processMessage(message);
        }
    }

    private void processMessage(Message message) {
        System.out.println("Processed: " + message.getId() + ", Payload: " + message.getPayload());
    }
}

// Main Class
public class MessageQ {
    public static void main(String[] args) {
        MessageQueue queue = new MessageQueue();

        Publisher publisher1 = new Publisher(queue);
        Publisher publisher2 = new Publisher(queue);

        Subscriber subscriber1 = new Subscriber(queue);
        Subscriber subscriber2 = new Subscriber(queue);

        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Start publishers
        executor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                publisher1.publish("Message from Publisher 1 - " + i);
            }
        });

        executor.execute(() -> {
            for (int i = 0; i < 10; i++) {
                publisher2.publish("Message from Publisher 2 - " + i);
            }
        });

        // Start subscribers
        executor.execute(subscriber1);
        executor.execute(subscriber2);

        executor.shutdown();
    }
}
