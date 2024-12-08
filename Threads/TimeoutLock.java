import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class TimeoutLockExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void performTask() {
        try {
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                try {
                    // critical section
                } finally {
                    lock.unlock();
                }
            } else {
                System.out.println("Could not acquire lock within the specified timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
