import java.util.*;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();

        Runnable depositTask = () -> {
            for (int i = 0; i < 10; i++) {
                account.deposit(100);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable withdrawTask = () -> {
            for (int i = 0; i < 10; i++) {
                account.withdraw(50);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t1 = new Thread(depositTask);
        Thread t2 = new Thread(depositTask);
        Thread t3 = new Thread(withdrawTask);
        Thread t4 = new Thread(withdrawTask);

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try {
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final balance: " + account.getBalance());
    }
}

public class BankAccount {
    final Object lock = new Object();
    double balance;

    public void deposit(int amount){
        synchronized(lock){
            balance += amount;
        }
        System.out.println("+ " + amount);
    }

    public void withdraw(int amount){
        synchronized(lock){
            balance -= amount;
        }
        System.out.println("- " + amount);
    }

    public double getBalance(){
        synchronized(lock){
            return balance;
        }
    }
}
