import java.util.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        StockMarket market = StockMarket.getInstance();
        Stock s1 = new Stock("A","Porsche",100);
        Stock s2 = new Stock("B","BMW",200);
        Stock s3 = new Stock("C","VW",10);
        Stock s4 = new Stock("D","Benz",10);
        Stock s5 = new Stock("E","Hyunda",1);

        market.stocks.put(s1.symbol,s1);
        market.stocks.put(s2.symbol,s2);
        market.stocks.put(s3.symbol,s3);
        market.stocks.put(s4.symbol,s4);
        market.stocks.put(s5.symbol,s5);

        market.trade();
    }
}

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Stock {
    public String symbol;
    public String company;
    private double price;
    private int quanity;
    final Object lock = new Object();

    public Stock(String symbol, String company, double price) {
        this.symbol = symbol;
        this.company = company;
        this.price = price;
        this.quanity = 100;
    }

    public Stock(String symbol, String company, double price,int quanity) {
        this.symbol = symbol;
        this.company = company;
        this.price = price;
        this.quanity = quanity;
    }

    void updatePrice(double price) {
        this.price = price;
    }

    void sellStocks(int q){
        synchronized (lock) {
            quanity += q;
        }
    }

    void buyStocks(int q){
        synchronized (lock) {
            quanity -=q;
        }
    }

    void logTransaction(String logFileName,String transactionDetails){
        System.out.println(transactionDetails);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName,true))){ //true => append
            synchronized (StockMarket.getInstance().fileLock){
                writer.write(transactionDetails);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public double getPrice() {
        return price;
    }

}


import java.util.HashMap;

public class Trader implements Runnable {
    String logFile;
    int num;

    public Trader(String file,int num){
        logFile = file;
        this.num = num;
    }

    @Override
    public void run() {
        Stock stock1 = StockMarket.getInstance().stocks.get("A");
        Stock stock2 = StockMarket.getInstance().stocks.get("B");
        Stock stock3 = StockMarket.getInstance().stocks.get("C");
        if(num%2==0) {
            StockMarket.getInstance().buyStock(stock1, 10);
            StockMarket.getInstance().buyStock(stock2, 15);
            StockMarket.getInstance().sellStock(stock1, 5);
            StockMarket.getInstance().sellStock(stock2, 15);
        }
        else{
            StockMarket.getInstance().buyStock(stock3, 10);
            StockMarket.getInstance().buyStock(stock3, 15);
            StockMarket.getInstance().sellStock(stock3, 5);
            StockMarket.getInstance().sellStock(stock3, 15);
        }
    }
}


import java.util.HashMap;

public class StockMarket {
    private static StockMarket instance;
    HashMap<String,Stock> stocks;
    public static String logFile = "logs.txt";
    public Object fileLock = new Object();

    private StockMarket() {
        stocks = new HashMap<>();
    }

    public static StockMarket getInstance() {
        if (instance == null) {
            instance = new StockMarket();
        }
        return instance;
    }

    void buyStock(Stock stock,int quantity){
        stock.buyStocks(quantity);
        String details = System.currentTimeMillis() + " | + "+quantity + " of " + stock.symbol + " at $" + stock.getPrice() + "\n";
        stock.logTransaction(logFile,details);
    }

    void sellStock(Stock stock,int quantity){
        stock.sellStocks(quantity);
        String details = System.currentTimeMillis() + " | - "+quantity + " of " + stock.symbol + " at $" + stock.getPrice()+"\n";
        stock.logTransaction(logFile,details);
    }

    public void trade() throws InterruptedException {
        Trader trader1 = new Trader(logFile,0);
        Trader trader2 = new Trader(logFile,1);
        Trader trader3 = new Trader(logFile,2);

        Thread t1 = new Thread(trader1);
        Thread t2 = new Thread(trader2);
        Thread t3 = new Thread(trader3);
        t1.start(); t2.start(); t3.start();
        t1.join(); t2.join(); t3.join();
    }
}
