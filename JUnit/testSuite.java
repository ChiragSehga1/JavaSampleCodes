import Exceptions.InvalidQuantityException;
import Exceptions.OutofStockException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class TestingCartOperations {

    static Controller controller;
    static Admin admin;
    static Customer testCustomer;
    int expectedTotal = 0;

    @BeforeClass
    public static void setUpBeforeClass() {
        // Initialize shared objects before all tests
        controller = new Controller();
        admin = Admin.getInstance(controller);
        testCustomer = new Customer("111", "CorrectPass");
        MenuItem item1 = new MenuItem("Test1", "Tester", 50, "", "", "Available");
        MenuItem item2 = new MenuItem("Test2", "Tester", 100, "", "", "Limited");
        MenuItem item3 = new MenuItem("Test3", "Tester", 100, "", "", "Unavailable");
        admin.menu.put(item1.getItemID(), item1);
        admin.menu.put(item2.getItemID(), item2);
        admin.menu.put(item3.getItemID(), item3);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // Cleanup after all tests (if necessary)
        admin.menu.remove("Test1");
        admin.menu.remove("Test2");
        admin.menu.remove("Test3");
        controller.users.remove("111");
    }

    @Test(timeout = 1000)
    public void testCartAdd1() throws OutofStockException {
        // Start with empty cart
        testCustomer.cart = new HashMap<>();
        expectedTotal = 0;
        testCustomer.adjustQuantity("Test1");
        expectedTotal += 50;
        assertEquals(testCustomer.viewTotal(), expectedTotal);
    }

    @Test(timeout = 1000)
    public void testCartAdd2() throws OutofStockException {
        // Start with empty cart
        testCustomer.cart = new HashMap<>();
        expectedTotal = 0;
        testCustomer.adjustQuantity("Test1");
        expectedTotal += 50;
        testCustomer.adjustQuantity("Test2");
        expectedTotal += 100;
        assertEquals(testCustomer.viewTotal(), expectedTotal);
    }

    @Test(timeout = 1000)
    public void testCartUpdateQuantity1() throws OutofStockException, InvalidQuantityException {
        // Start with empty cart
        testCustomer.cart = new HashMap<>();
        expectedTotal = 0;
        testCustomer.adjustQuantity("Test1");
        expectedTotal += 50;
        testCustomer.adjustQuantity("Test2");
        expectedTotal += 100;
        testCustomer.moreThanOneQuantity("Test2",2);
        expectedTotal += 100;
        assertEquals(testCustomer.viewTotal(), expectedTotal);
    }


    @Test(timeout = 1000)
    public void testCartUpdateQuantity2() throws OutofStockException, InvalidQuantityException {
        // Start with empty cart
        testCustomer.cart = new HashMap<>();
        expectedTotal = 0;
        testCustomer.adjustQuantity("Test1");
        expectedTotal += 50;
        testCustomer.adjustQuantity("Test2");
        expectedTotal += 100;
        testCustomer.moreThanOneQuantity("Test2",0);
        expectedTotal -= 100;
        assertEquals(testCustomer.viewTotal(), expectedTotal);
    }

    @Test(timeout = 1000)
    public void testCartUpdateQuantity3(){
        // negative value check
        assertThrows(InvalidQuantityException.class, () -> testCustomer.moreThanOneQuantity("Test1",-1));
    }
}

import Exceptions.InvalidLoginException;
import Exceptions.OutofStockException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class TestingCustomerAdmin {

    static Controller controller;
    static Admin admin;
    static Customer testCustomer;

    @BeforeClass
    public static void setUpBeforeClass() {
        // Initialize shared objects before all tests
        controller = new Controller();
        admin = Admin.getInstance(controller);
        testCustomer = new Customer("111", "CorrectPass");
    }

    @AfterClass
    public static void tearDownAfterClass() {
        // Cleanup after all tests (if necessary)
        admin.menu.remove("Test");
        controller.users.remove("111");
    }

    @Test(timeout = 1000)
    public void testOrderOutOfStockItem() {
        MenuItem item = new MenuItem("Test", "Tester", 50, "", "", "Unavailable");
        admin.menu.put("Test", item);

        // Validate that OutofStockException is thrown
        System.out.println("Trying to order out of stock item");
        assertThrows(OutofStockException.class, () -> testCustomer.adjustQuantity("Test"));
    }

    @Test(timeout = 1000)
    public void testInvalidUser() {
        // Test for invalid user entry
        System.out.println("Trying to login with non-existent user");
        assertThrows(InvalidLoginException.class, () -> controller.customerEnter("2000", "ThisUserDoesNotExist"));
    }

    @Test(timeout = 1000)
    public void testInvalidPassword() {
        // Test for incorrect password
        System.out.println("Trying Customer login with incorrect password");
        assertThrows(InvalidLoginException.class, () -> controller.customerEnter("111", "WrongPassword"));
    }

    @Test(timeout = 1000)
    public void testInvalidAdmin() {
        // Test for invalid admin login
        System.out.println("Trying Admin login with incorrect password");
        assertThrows(InvalidLoginException.class, () -> admin.login("WrongPass"));
    }
}

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

// Specify the test classes to include
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestingCustomerAdmin.class
        , TestingCartOperations.class
})
public class TestSuite {
    // This class remains empty; its purpose is only to hold the annotations
}
