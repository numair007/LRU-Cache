package prod_cons.self;


import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Message{
    String content;

    Message(String content){
        this.content = content;
    }
}

class MessageQueue{
    ArrayBlockingQueue<Message> queue;

    MessageQueue(){
        queue = new ArrayBlockingQueue<>(10);
    }

    public void publishMessage(String content){
        queue.offer(new Message(content));
    }

    public Message consume(){
        Message message = null;
        try{
            message = queue.take();
        }
        catch(InterruptedException exception){

        }
        return message;
         
    }
}

class Publisher{
    MessageQueue messageQueue;
    Publisher(MessageQueue messageQueue){
        this.messageQueue = messageQueue;
    }

    public void publishMessage(String content){
        sleep(100000);
        messageQueue.publishMessage(content);
        System.out.println("Message "+content+" Produced by Producer");
    }
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
class Subscriber implements Runnable{
    MessageQueue messageQueue;
    Subscriber(MessageQueue messageQueue){
        this.messageQueue = messageQueue;
    }
    public void run(){
        consume();
    }
    public void consume(){
        Message message = messageQueue.consume();
        if(message != null){
            System.out.println("Message "+message.content+" Consumed by Consumer");
        }
        sleep(10);
    }
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
}

public class ProducerConsumerSinglePull {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue();
        // Publisher publisher1 = new Publisher(messageQueue);
        // Publisher publisher2 = new Publisher(messageQueue);
        // Publisher publisher3 = new Publisher(messageQueue);
        // Subscriber subscriber1 = new Subscriber(messageQueue);
        // Subscriber subscriber2 = new Subscriber(messageQueue);
        // Subscriber subscriber3 = new Subscriber(messageQueue);

        ExecutorService executorService = Executors.newFixedThreadPool(10);


        for(int i=0;i<3;i++){
            Publisher publisher = new Publisher(messageQueue);
            String id = i+"";
            executorService.submit(() -> {
                publisher.publishMessage("Message by Producer "+id);
            });
        }
        for(int i=0;i<3;i++){
            Subscriber consumer = new Subscriber(messageQueue);
            executorService.submit(() -> {
                consumer.consume();
            });
        }
        

        executorService.shutdown();
    }
}

