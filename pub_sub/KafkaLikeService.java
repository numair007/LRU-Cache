package pub_sub;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

// Message class representing a message
class Message {
    private final String content;

    public Message(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

// Topic class to manage messages and subscribers
class Topic {
    private final String name;
    private final BlockingQueue<Message> messageQueue;
    private final List<Subscriber> subscribers;
    private final ReentrantLock lock;

    public Topic(String name) {
        this.name = name;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.subscribers = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    public String getName() {
        return name;
    }

    public void publishMessage(Message message) {
        try {
            lock.lock();
            messageQueue.offer(message);
            notifySubscribers();
        } finally {
            lock.unlock();
        }
    }

    public Message consumeMessage() {
        try {
            lock.lock();
            return messageQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    public void addSubscriber(Subscriber subscriber) {
        try {
            lock.lock();
            subscribers.add(subscriber);
        } finally {
            lock.unlock();
        }
    }

    private void notifySubscribers() {
        for (Subscriber subscriber : subscribers) {
            subscriber.onMessageAvailable(this);
        }
    }
}

// Singleton Broker class
class Broker {
    private static Broker instance;
    private final Map<String, Topic> topics;
    private final ReentrantLock lock;

    private Broker() {
        this.topics = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public static Broker getInstance() {
        if (instance == null) {
            synchronized (Broker.class) {
                if (instance == null) {
                    instance = new Broker();
                }
            }
        }
        return instance;
    }

    public Topic getOrCreateTopic(String topicName) {
        topics.putIfAbsent(topicName, new Topic(topicName));
        return topics.get(topicName);
    }
}

// Publisher class
class Publisher {
    private final String id;

    public Publisher(String id) {
        this.id = id;
    }

    public void publish(String topicName, String messageContent) {
        Broker broker = Broker.getInstance();
        Topic topic = broker.getOrCreateTopic(topicName);
        topic.publishMessage(new Message(messageContent));
        System.out.println("Publisher [" + id + "] published message: " + messageContent);
    }
}

// Subscriber class
class Subscriber {
    private final String id;

    public Subscriber(String id) {
        this.id = id;
    }

    public void subscribe(String topicName) {
        Broker broker = Broker.getInstance();
        Topic topic = broker.getOrCreateTopic(topicName);
        topic.addSubscriber(this);
        System.out.println("Subscriber [" + id + "] subscribed to topic: " + topicName);
    }

    public void onMessageAvailable(Topic topic) {
        Message message = topic.consumeMessage();
        if (message != null) {
            System.out.println("Subscriber [" + id + "] consumed message from topic [" 
                               + topic.getName() + "]: " + message.getContent());
        }
    }
}

// Test class
public class KafkaLikeService {
    public static void main(String[] args) {
        Broker broker = Broker.getInstance();

        // Create publishers
        Publisher publisher1 = new Publisher("Publisher1");
        Publisher publisher2 = new Publisher("Publisher2");

        // Create subscribers
        Subscriber subscriber1 = new Subscriber("Subscriber1");
        Subscriber subscriber2 = new Subscriber("Subscriber2");

        // Subscribing to topics
        subscriber1.subscribe("Topic1");
        subscriber2.subscribe("Topic1");
        subscriber2.subscribe("Topic2");

        // Publishing messages
        publisher1.publish("Topic1", "Hello from Publisher1!");
        publisher2.publish("Topic1", "Another message for Topic1!");
        publisher2.publish("Topic2", "Message for Topic2!");

        // Adding a delay to allow all messages to be consumed
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

