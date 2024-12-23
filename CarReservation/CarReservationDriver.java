package CarReservation;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CarReservationDriver {

    public static void main(String[] args) {
        ReservationStrategy reservationStrategy = new LeastWastageStrategy(3);
        CarReservationSystem carReservationSystem = CarReservationSystem.getCarReservationSystemInstance();
        carReservationSystem.setReservationStrategy(reservationStrategy);
        int car = carReservationSystem.reserveCar(0, 5);
        System.out.println("Car - "+car);
        car = carReservationSystem.reserveCar(2, 10);
        System.out.println("Car - "+car);
        car = carReservationSystem.reserveCar(6, 8);
        System.out.println("Car - "+car);
        car = carReservationSystem.reserveCar(2, 7);
        System.out.println("Car - "+car);
        car = carReservationSystem.reserveCar(2, 3);
        System.out.println("Car - "+car);
        int p = carReservationSystem.getAllAvaialableCars(0, 1);
        System.out.println("Total Cars - "+p);
    }
    
}

class CarReservationSystem{

   static CarReservationSystem carReservationSystemInstance;

   ReservationStrategy reservationStrategy;



   private CarReservationSystem(){

   }

   public static CarReservationSystem getCarReservationSystemInstance(){
    if(carReservationSystemInstance == null) carReservationSystemInstance = new CarReservationSystem();
    return carReservationSystemInstance;
   }

   public void setReservationStrategy(ReservationStrategy reservationStrategy){
    this.reservationStrategy = reservationStrategy;
   }

   public int reserveCar(int start, int end){

    return reservationStrategy.reserveCar(start, end);
   }

   public int getAllAvaialableCars(int start, int end){
    return reservationStrategy.getAllAvaialableCars(start, end);
   }

}

interface ReservationStrategy{

    public int reserveCar(int start, int end);
    
    public int getAllAvaialableCars(int start, int end);
}

class LeastWastageStrategy implements ReservationStrategy{

    int totalCars;
    Map<Integer, TreeMap<Integer, Integer>> endTimeCarMap;

    ReentrantReadWriteLock readWriteLock;
    LeastWastageStrategy(int totalCars){
        this.totalCars = totalCars;
        readWriteLock = new ReentrantReadWriteLock();
        endTimeCarMap = new HashMap<>();

        for(int i=0;i<totalCars;i++){
            endTimeCarMap.put(i,new TreeMap<>());
        }
    }
    @Override
    public int reserveCar(int start, int end) {
       //System.out.println(endTimeCarMap);
        int bestCar = -1;
        readWriteLock.writeLock().lock();
        try{
            boolean found = false;
        
            int leastWastage = -1;
            for(int i=0;i<totalCars;i++){
                TreeMap<Integer, Integer> endMap = endTimeCarMap.get(i);
                Integer higher = endMap.higherKey(start);
                int currWastage = 0;
                if(higher != null){
                    
                    int startHigher = endMap.get(higher);
                    if(end>= startHigher){
                        //System.out.println("PP "+i);
                        continue;
                    }
                    else{
                        currWastage += (startHigher - end);
                        
    
                    }
                    
                }
                
                    Integer lower = endMap.lowerKey(start);
                    if(lower != null){
                        currWastage += start - lower;
                    }
    
                    //System.out.println("Wastage for car "+i+" "+currWastage);
                    if(leastWastage < currWastage){
                        leastWastage = currWastage;
                        bestCar = i;
                        found = true;
                    }
                
            }
    
            if(!found) return -1;
            endTimeCarMap.get(bestCar).put(end, start);
        }
        catch(Exception exception){

        }
        finally{
            readWriteLock.writeLock().unlock();
        }

        return bestCar;
    }

    @Override
    public int getAllAvaialableCars(int start, int end) {
        readWriteLock.readLock().lock();
        int ans = 0;
        try{
            for(int i=0;i<totalCars;i++){
                TreeMap<Integer, Integer> endMap = endTimeCarMap.get(i);
                Integer higher = endMap.higherKey(start);
                if(higher != null){
                    int startHigher = endMap.get(higher);
                    if(end>= startHigher){
                        continue;
                    }
                    else{
                        ans++;
                    }
                    
                }
                else{
                    ans++;
                }
                
            }
        }
        catch(Exception exception){

        }
        finally{
            readWriteLock.readLock().unlock();
        }
        return ans;

    }


    
}