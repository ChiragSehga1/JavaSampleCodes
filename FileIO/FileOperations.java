import java.io.FileWriter;
import java.io.IOException;

public class WriteToFileExample {
    public static void main(String[] args) {
        try (FileWriter writer = new FileWriter("example.txt")) {
            writer.write("Hello, World!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFromFileExample {
    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("example.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


import java.io.FileWriter;
import java.io.IOException;

public class AppendToFileExample {
    public static void main(String[] args) {
        try (FileWriter writer = new FileWriter("example.txt", true)) { // 'true' indicates append mode
            writer.write("\nAppending this line.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

