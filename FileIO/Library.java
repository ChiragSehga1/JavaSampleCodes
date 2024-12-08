public class Main {
    public static void main(String[] args) {
        String memData = "members.txt";
        String bookData = "books.txt";
        Library<Member> library = new Library<Member>();
        library.loadBooksFromFile(bookData);
        library.loadMembersFromFile(memData);
        library.displayAvailableBooks();
        library.returnBook(library.members.get("02"),library.books.get("0000"));
        library.displayAvailableBooks();
//        library.loadMembersFromFile(memData);
//        Book book1 = new Book("1234","Book1","CS");
//        Book book2 = new Book("4321","Book2","SS");
//        Book book3 = new Book("0000","Book3","GS");
//        Member member1 = new Member("01","Mem1");
//        Member member2 = new Member("02","Mem2");
//        Member member3 = new Member("03","Mem3");
//        Library<Member> library = new Library<Member>();
//        library.books.put(book1.ISBM,book1);
//        library.books.put(book2.ISBM,book2);
//        library.books.put(book3.ISBM,book3);
//        library.members.put(member1.memberID,member1);
//        library.members.put(member2.memberID,member2);
//        library.members.put(member3.memberID,member3);
//        library.lendBook(member1,book1);
//        library.lendBook(member2,book2);
//        library.lendBook(member3,book2);
//        library.lendBook(member2,book3);
//        library.returnBook(member1,book1);
//        library.displayAvailableBooks();
        library.saveLibraryData(bookData,memData);
    }
}

public class Book {
    String ISBM;
    String title;
    String author;
    boolean isAvailable;

    public Book(String ISBM, String title, String author) {
        this.ISBM = ISBM;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    public Book(String ISBM, String title, String author,boolean isAvailable) {
        this.ISBM = ISBM;
        this.title = title;
        this.author = author;
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return ISBM + "|" + title + "|" + author + "|" + isAvailable + "\n";
    }
}


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Library <T extends Member> {
    HashMap<String, ArrayList<T>> records; //ISBN: Members borrowing the book
    HashMap<String,Book> books;
    HashMap<String,Member> members;
    String bookPath = "books.txt";
    String memberPath = "members.txt";

    public Library(){
        records = new HashMap<>();
        books = new HashMap<>();
        members = new HashMap<>();
    }

    void lendBook(T member, Book book){
        if(book.isAvailable == true) {
            book.isAvailable = false;
            member.borrowedBooks.add(book);
            if (records.containsKey(book.ISBM)) {
                records.get(book.ISBM).add(member);
            } else {
                ArrayList<T> arr = new ArrayList<>();
                arr.add(member);
                records.put(book.ISBM, arr);
            }
        }
        else{
            System.out.println("Book is not available");
        }
    }

    void returnBook(T member, Book book){
        book.isAvailable = true;
        member.borrowedBooks.remove(book);
//        records.get(book.ISBM).remove(member); //optional
    }

    void displayAvailableBooks(){
        for(Book book : books.values()){
            if(book.isAvailable){
                System.out.println(book);
            }
        }
    }

    void loadBooksFromFile(String fileName){
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
            String line;
            while((line = br.readLine()) != null){
                String[] tokens = line.split("\\|",-1);
                System.out.println(line);
                String ISMB = tokens[0];
                String title = tokens[1];
                String author = tokens[2];
                boolean isAvailable = Boolean.parseBoolean(tokens[3]);
                Book book = new Book(ISMB,title,author,isAvailable);
                this.books.put(ISMB,book);
            }
        }catch(FileNotFoundException e){
            try{
                File file= new File(fileName);
                if(!file.exists()){
                    file.createNewFile();
                    System.out.println("New file created: " + file.getName());
                }
            } catch(IOException e1){e1.printStackTrace();}
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    void loadMembersFromFile(String fileName){
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))){
            String line;
            while((line = br.readLine()) != null){
                String[] tokens = line.split(",",-1);
                System.out.println(line);
                String memberID = tokens[0];
                String name = tokens[1];
                Member member = new Member(memberID,name);
                this.members.put(memberID,member);
                for(int i=2;i< tokens.length;i++){
                    String ISMB = tokens[i];
                    member.borrowedBooks.add(this.books.get(ISMB));

                }
            }
        }catch(FileNotFoundException e){
            try{
                File file= new File(fileName);
                if(!file.exists()){
                    file.createNewFile();
                    System.out.println("New file created: " + file.getName());
                }
            } catch(IOException e1){e1.printStackTrace();}
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    void saveLibraryData(String booksFileName,String membersFileName) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(booksFileName))){
            for(Book book : books.values()) {
                writer.write(book.toString());
            }
        }catch(IOException e){
            e.printStackTrace();
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(membersFileName))){
            for(Member member : members.values()) {
                writer.write(member.toString());
            }
        }catch(IOException e){
            e.printStackTrace();
        }

    }


}

import java.util.ArrayList;
import java.util.List;

public class Member {
    String memberID;
    String name;
    List<Book> borrowedBooks;

    public Member(String memberID, String name) {
        this.memberID = memberID;
        this.name = name;
        borrowedBooks = new ArrayList<Book>();
    }

    public Member(String memberID, String name, List<Book> borrowedBooks) {
        this.memberID = memberID;
        this.name = name;
        this.borrowedBooks = borrowedBooks;
    }

    @Override
    public String toString() {
        String text = memberID + "," + name + ",";
        for(Book book : borrowedBooks) {
            text += book.ISBM + ",";
        }
        text = text.substring(0, text.length() - 1) + "\n";
        return text;
    }

}
