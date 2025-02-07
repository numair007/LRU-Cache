package prod_cons;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ProducerConsumer {
    private static final int BUFFER_SIZE = 5; // Maximum size of the shared buffer
    private final LinkedList<Integer> buffer = new LinkedList<>();

    // Semaphores
    private final Semaphore empty = new Semaphore(BUFFER_SIZE); // Tracks empty slots
    private final Semaphore full = new Semaphore(0);            // Tracks filled slots
    private final Semaphore mutex = new Semaphore(1);           // Ensures mutual exclusion

    // Producer class
    class Producer extends Thread {
        @Override
        public void run() {
            try {
                int value = 0; // Item to produce
                while (true) {
                    empty.acquire(); // Wait if the buffer is full
                    mutex.acquire(); // Enter critical section

                    buffer.add(value);
                    System.out.println("Produced: " + value);
                    value++;

                    mutex.release(); // Exit critical section
                    full.release();  // Signal that an item is available

                    Thread.sleep(500); // Simulate production time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Consumer class
    class Consumer extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    full.acquire(); // Wait if the buffer is empty
                    mutex.acquire(); // Enter critical section

                    int value = buffer.removeFirst();
                    System.out.println("Consumed: " + value);

                    mutex.release(); // Exit critical section
                    empty.release(); // Signal that a slot is available

                    Thread.sleep(1000); // Simulate consumption time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Main method
    public static void main(String[] args) {
        ProducerConsumer pc = new ProducerConsumer();

        // Create and start producer and consumer threads
        Producer producer = pc.new Producer();
        Consumer consumer = pc.new Consumer();

        producer.start();
        consumer.start();
    }
}

