package RateLimiter;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;


public class RateLimiterDriver {
    public static void main(String[] args) {
        RateLimiter rateLimiter = RateLimiter.getRateLimiterInstance();
        RateLimiterPolicy rateLimiterPolicy = new RateLimiterTimePolicy(10, 4);
        rateLimiter.setRateLimitPolicy(rateLimiterPolicy);
        int t = 0;
        boolean res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 5;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 5;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 5;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 5;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 20;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 100;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 100;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 100;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 100;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);
        t = 111;
        res = rateLimiter.isAllow(0, t);
        System.out.println("Allowed? for "+t+" "+res);



    }
}

class RateLimiter{

    static RateLimiter rateLimiterInstance;
    RateLimiterPolicy rateLimiterPolicy;

    private RateLimiter(){

    }
    public static RateLimiter getRateLimiterInstance(){
        if(rateLimiterInstance == null) rateLimiterInstance = new RateLimiter();
        return rateLimiterInstance;
    }
    public void setRateLimitPolicy(RateLimiterPolicy rateLimiterPolicy){
        this.rateLimiterPolicy = rateLimiterPolicy;
    }
    public boolean isAllow(int id, int timestamp){
        return rateLimiterPolicy.isAllow(id, timestamp);
    }
}

interface RateLimiterPolicy{
    public boolean isAllow(int id, int timestamp);
}

class RateLimiterTimePolicy implements RateLimiterPolicy{
    int timeLimit;
    int allowedCalls;
    Map<Integer, Deque<Integer>> apiCallMap;
    ReentrantReadWriteLock readWriteLock;


    RateLimiterTimePolicy(int time, int allowedCalls){
        this.timeLimit = time;
        this.allowedCalls = allowedCalls;
        apiCallMap = new ConcurrentHashMap<>(); //  use concurrent hashmap for fine grained locking ability
        readWriteLock = new ReentrantReadWriteLock();
    }
    // My earlier implementstion, using renetrant lock, reduced perfomance due to overlocking
    // public boolean isAllow(int id, int timestamp){
    //     boolean res = false;
    //     readWriteLock.writeLock().lock();
    //     try{
    //         if(!apiCallMap.containsKey(id)){
    //             apiCallMap.put(id, new LinkedList<>());
    //         }
    //         Deque<Integer> dq = apiCallMap.get(id);
    //         //System.out.println(dq.size()+" Size");
    //         while(dq.size() > 0 && (timestamp - dq.peekFirst())>timeLimit){
    //             dq.removeFirst();
    //         }
    //         if(dq.size() >= allowedCalls){
    //             res= false;
    //         }
    //         else{
    //             dq.addLast(timestamp);
    //             res= true;
    //         }
    //     }
    //     catch(Exception exception){
    //         System.out.println(exception.getMessage());
    //     }
    //     finally{
    //         readWriteLock.writeLock().unlock();
    //     }
    //     return res;

    // }
    public boolean isAllow(int id, int timestamp) {
        // Ensure the deque exists for the given client ID
        apiCallMap.computeIfAbsent(id, k -> new LinkedList<>()); // atomic operation, which put new LinkedList if one does not already exisit
    
        // Synchronize on the specific client's deque
        synchronized (apiCallMap.get(id)) { // lock on one clients linkedlist only, any other call to this function with same id would get blocked here, but allowed for other ids, as they will have different id and different linkedlist
            Deque<Integer> dq = apiCallMap.get(id);
    
            // Remove outdated timestamps
            while (!dq.isEmpty() && (timestamp - dq.peekFirst()) > timeLimit) {
                dq.removeFirst();
            }
    
            // Check if the request is allowed
            if (dq.size() >= allowedCalls) {
                return false; // Deny the request
            } else {
                dq.addLast(timestamp); // Allow the request
                return true;
            }
        }
    }
}

// In this problem if thread saftey is needed
// Only need to block the call for thread opearitng on same id
// Because no point in blocking the entire call for other ids, when some other id being worked on
// So use lock for each different k
// Refer this code


class ThreadSafeOnK {
    private final ConcurrentHashMap<Integer, Object> lockMap = new ConcurrentHashMap<>();

    public void threadSafeFunction(int k) {
        Object lock = lockMap.computeIfAbsent(k, key -> new Object());
        
        synchronized (lock) {
            try {
                // Critical section: Code that needs to be thread-safe for the given k
                System.out.println("Processing for k = " + k + " by " + Thread.currentThread().getName());
                Thread.sleep(1000); // Simulating some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interruption
            } finally {
                // Clean up lock if no other thread is using it
                lockMap.compute(k, (key, value) -> 
                    Thread.holdsLock(value) ? value : null);
            }
        }
    }

    public static void main(String[] args) {
        ThreadSafeOnK safeOnK = new ThreadSafeOnK();

        // Test with multiple threads
        Runnable task = () -> {
            for (int i = 0; i < 3; i++) {
                safeOnK.threadSafeFunction(i % 2); // Alternating between k = 0 and k = 1
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();
    }
}

// More explanation of this overlocking issue


/*
 * Explanation of Overlocking in the Current Code:
In your implementation, you are using a global write lock (readWriteLock.writeLock().lock()), which locks the entire apiCallMap during the execution of the isAllow method. This approach ensures safety but significantly reduces performance because:

All threads trying to access any client's request data will have to wait for the global lock.
In a highly concurrent environment with many clients, this becomes a bottleneck since threads block even when they could have worked on independent client data without conflict.
Solution: Fine-Grained Locking per Client
To improve concurrency, you can use fine-grained locking. The idea is to lock only the data related to the specific id (client) instead of locking the entire apiCallMap. This ensures that:

Multiple threads can simultaneously process requests for different clients without contention.
Only threads working on the same client will contend for the lock.
Implementation: Using synchronized for Per-Client Locking
java
Copy code
public boolean isAllow(int id, int timestamp) {
    // Ensure the deque exists for the given client ID
    apiCallMap.computeIfAbsent(id, k -> new LinkedList<>());

    // Synchronize on the specific client's deque
    synchronized (apiCallMap.get(id)) {
        Deque<Integer> dq = apiCallMap.get(id);

        // Remove outdated timestamps
        while (!dq.isEmpty() && (timestamp - dq.peekFirst()) > timeLimit) {
            dq.removeFirst();
        }

        // Check if the request is allowed
        if (dq.size() >= allowedCalls) {
            return false; // Deny the request
        } else {
            dq.addLast(timestamp); // Allow the request
            return true;
        }
    }
}
Explanation of Overlocking in the Current Code:
In your implementation, you are using a global write lock (readWriteLock.writeLock().lock()), which locks the entire apiCallMap during the execution of the isAllow method. This approach ensures safety but significantly reduces performance because:

All threads trying to access any client's request data will have to wait for the global lock.
In a highly concurrent environment with many clients, this becomes a bottleneck since threads block even when they could have worked on independent client data without conflict.
Solution: Fine-Grained Locking per Client
To improve concurrency, you can use fine-grained locking. The idea is to lock only the data related to the specific id (client) instead of locking the entire apiCallMap. This ensures that:

Multiple threads can simultaneously process requests for different clients without contention.
Only threads working on the same client will contend for the lock.
Implementation: Using synchronized for Per-Client Locking
java
Copy code
public boolean isAllow(int id, int timestamp) {
    // Ensure the deque exists for the given client ID
    apiCallMap.computeIfAbsent(id, k -> new LinkedList<>());

    // Synchronize on the specific client's deque
    synchronized (apiCallMap.get(id)) {
        Deque<Integer> dq = apiCallMap.get(id);

        // Remove outdated timestamps
        while (!dq.isEmpty() && (timestamp - dq.peekFirst()) > timeLimit) {
            dq.removeFirst();
        }

        // Check if the request is allowed
        if (dq.size() >= allowedCalls) {
            return false; // Deny the request
        } else {
            dq.addLast(timestamp); // Allow the request
            return true;
        }
    }
}
Key Points in the Implementation:
computeIfAbsent:

Ensures a Deque is initialized for every new client ID without requiring explicit null checks.
Thread-safe initialization prevents race conditions during the creation of the client's Deque.
synchronized(apiCallMap.get(id)):

Locks only the specific Deque for the current client ID.
Other threads can process other clients simultaneously.
Reduced Lock Contention:

Since locks are localized to individual clients, there is no global contention for apiCallMap.
Advantages of Per-Client Locking:
Better Scalability:
Multiple threads can process requests for different clients in parallel.
Improved Performance:
Lock contention is minimized because threads rarely access the same client's data simultaneously.
Additional Considerations:
Memory Management:

The apiCallMap can grow indefinitely as new clients make requests. To address this, consider implementing an eviction policy (e.g., using a LinkedHashMap with accessOrder=true).
Concurrency-Friendly Collections:

If you expect very high concurrency, use a ConcurrentHashMap instead of a HashMap for apiCallMap. It provides thread-safe access without requiring global synchronization.
Atomic Operations:

You might consider atomic operations (e.g., AtomicInteger for counters) for simpler rate-limiting policies, but for timestamp-based logic, locking is still required.
Thread-Safe Data Structures:

If Deque operations become a bottleneck, you can use ConcurrentLinkedDeque, but it lacks direct support for removing elements based on timestamps.
How It Solves Overlocking:
By locking per client ID (synchronized(apiCallMap.get(id))), only threads working on the same client will wait for each other.
Threads processing different clients can execute concurrently without interference, maximizing throughput.
This approach is especially beneficial in scenarios with a large number of clients making requests at varying rates.







 */

