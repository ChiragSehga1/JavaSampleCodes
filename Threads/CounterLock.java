import java.util.*;
public class Main {

    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        Counter count = new Counter();
        Thread t1 = new Thread(() -> {for(int i=0;i<1000;i++){count.increment();}});
        Thread t2 = new Thread(() -> {for(int i=0;i<1000;i++){count.increment();}});
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println(count.counter);
    }
}

public class Counter{
    int counter = 0;
    Object lock;

    public Counter() {
        this.lock = new Object();
    }

    public void increment() {
        synchronized (lock) {counter++;}
    }

    public int getCounter() {
        return counter;
    }
}
