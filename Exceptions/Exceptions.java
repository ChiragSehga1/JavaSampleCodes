package Exceptions;

public class InvalidLoginException extends Exception {
    public String message;
    public String pass;
    public InvalidLoginException(String message) {
        super(message);
        this.message = message;
    }

    public InvalidLoginException(String message,String pass) {
        super(message);
        this.message = message;
        this.pass = pass;
    }

    @Override
    public String toString() {
        return "InvalidLoginException";
    }

    @Override
    public String getMessage() {
        return "Incorrect username or password";
    }
}

package Exceptions;

public class OutofStockException extends Exception {
    String itemOrdered;

    public OutofStockException(String message) {
        super(message);
        itemOrdered = message;
    }

    @Override
    public String toString() {
        return "OutOfStockException";
    }

    @Override
    public String getMessage() {
        return this.itemOrdered + " is out of stock";
    }
}
