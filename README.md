# BYTE-IT!
## Online Food Ordering System

## Description
An online food ordering system that allows customers to browse menus, manage their cart, place orders, track their status, and provide feedback through reviews. This system distinguishes between VIP and regular customers, enhancing the user experience and catering to different customer needs.

## Features
### Graphical User Interface
- **Menu Viewing**: Browse the full menu with details.
- **Item and Review Viewing**: View item details and customer reviews.
- **Cart Management**: Add items to the cart, adjust quantities, and view cart contents.
- **Order Checkout**: Place an order from the cart.
- **Pending Orders**: View and manage pending orders.
- **Order Completion**: Mark orders as complete once processed.

### Command Line Interface
- **User Management**: Register and login for customers, with differentiation between VIP and regular customers.
- **Menu Browsing**: Search and filter items by category and price.
- **Shopping Cart**: Add, adjust quantities, and remove items from the cart.
- **Order Management**: Place orders with special instructions, track ongoing orders, and view order history.
- **Review System**: Customers can leave reviews for items in their completed orders.
- **I/O Streams for Serialization**: Object serialization and deserialization for user and order data management.
- **JUnit Testing**: Added unit tests for functionality verification.

## Usage
### Normal Operations
1. Run the `Main` class to start the application.

### Testing
1. There are 9 pre-existing JUnit test cases.
2. Run class `TestSuite` to run these test cases.

## Collections Used
- **Lists** for storage where uniqueness is guaranteed.
- **Hashmaps** for storage of unique items, ensuring that items that exist are only updated.
- **Treemaps** for automatically sorting `Orders` based on `OrderID`.
- **OrderID** are cleverly set so that VIP orders are always tended to first, and always in chronological order.

## JUnit Testing
Run the `TestSuite` class to test for the following:
- Ordering out of stock items (1)
- Adding items to cart (2)
- Updating quantity of items in cart (3)
- Login as non existent user (1)
- Login with the wrong password as Customer or Admin (2)

## Serialisation
- The directory `data` contains 4 text files, that are written to and loaded from using the `Memory` class
- `ToSave` interface is implemented by all savable classes, so they must define `fromText` and `toText` methods for serialisation
- Items are loaded from the file in the beginning (including order histories and user data), and saved after the user have exited from the CLI

## Credit
- Chirag Sehgal
