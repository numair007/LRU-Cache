package MeetingSchedulerDriver;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MeetingSchedulerDriver {
    public static void main(String[] args) {
        BookingSystem bookingSystemInstance = BookingSystem.getBookingSystemInstance();
        bookingSystemInstance.setBookingStrategy(new MinimumWastageStrategy(10));
        int result = bookingSystemInstance.reserveCar(0, 10);
        System.out.println(result);
        result = bookingSystemInstance.reserveCar(5, 10);
        System.out.println(result);
        result =bookingSystemInstance.reserveCar(12, 20);
        System.out.println(result);
        result =bookingSystemInstance.getAllAvailableCars(0, 10);
        System.out.println(result);

    }
}

class sortBooking implements Comparator<Booking>{
    public int compare(Booking a, Booking b){
        return a.end - b.end; // come back on logic of least wastage
    }
}
class Booking  {
    int start;
    int end;
    int car;

    Booking(int start, int end, int car){
        this.start = start;
        this.end = end;
        this.car = car;
    }

   

    
}
class BookingSystem{
    BookingStrategy bookingStrategy;
    int capacity;
    static BookingSystem bookingSystemInstance;

    private  BookingSystem(){

    }

    public static BookingSystem getBookingSystemInstance(){
        if(bookingSystemInstance == null) bookingSystemInstance = new BookingSystem();
        return bookingSystemInstance;
    }

    public void setBookingStrategy(BookingStrategy bookingStrategy){
        this.bookingStrategy = bookingStrategy;
    }
    public int reserveCar(int start, int end){
        return bookingStrategy.reserveCar(start, end);
    }
    public int getAllAvailableCars(int start, int end){
        return bookingStrategy.getAllAvailableCars(start, end);
    }
}

interface BookingStrategy{
    public int reserveCar(int start, int end);
    public int getAllAvailableCars(int start, int end);
}

class MinimumWastageStrategy implements BookingStrategy{

    int totalCars;

    PriorityQueue<Booking> availableCars;
    PriorityQueue<Booking> bookedCars;
    TreeMap<Integer, List<Integer>> bookings;
    ReentrantReadWriteLock readWriteLock;

    MinimumWastageStrategy(int totalCars){
        this.totalCars = totalCars;
        availableCars  = new PriorityQueue<>(new sortBooking());
        bookedCars = new PriorityQueue<>(new sortBooking());
        bookings = new TreeMap<>();
        for(int i=0;i<totalCars;i++){
            availableCars.add(new Booking(0, 0, i));
        }
        readWriteLock  = new ReentrantReadWriteLock();
    }
    public int reserveCar(int start, int end){
        readWriteLock.writeLock().lock();
        int ans = -1;
        try{
            addBooking(start,end);
            while(bookedCars.size() > 0 && bookedCars.peek().end<= start){
                Booking b = bookedCars.remove();
                availableCars.add(new Booking(0, 0, b.car));
            }

             ans = -1;
            if(availableCars.size() > 0){
                Booking b = availableCars.remove();
                ans = b.car;
                bookedCars.add(new Booking(start, end, b.car));

            }
            else{
                Booking b = bookedCars.remove();
                int dur = end-start;
                start = b.end;
                end = start+dur;
                bookedCars.add(new Booking(start, end, b.car));
                ans = b.car;
            }

            return ans;
        }
        catch(Exception e){
            
        }
        finally{
            readWriteLock.writeLock().unlock();
            
        }
        return ans;
        
    }
    private void addBooking(int start,int end){
        if(!bookings.containsKey(start)) bookings.put(start, new ArrayList<>());
        bookings.get(start).add(end);
    }
    @Override
    public int getAllAvailableCars(int start, int end) {
        int count = 0;
        for(Map.Entry<Integer, List<Integer>> es: bookings.entrySet()){
            int bookStart = es.getKey();
            boolean isStart = bookStart<=end && bookStart>=start;
            List<Integer> bookFinishs = es.getValue();
            for(int endTimes: bookFinishs){
                if(isStart){
                    count++;
                }
                else{
                    if(endTimes>=start && endTimes <= end){

                    }
                }
            }
        }

        return 0;
    }   
}