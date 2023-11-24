package HuffmanEncoding;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {
    public static String readFromFile(String filename){
        try{
            return Files.readString(Paths.get(filename));
        }
        catch (java.io.IOException e){
            System.err.println("Read from file error!");
        }
        return null;
    }

    public static void writeToFile(String filename, String data) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
            writer.write(data);
        }
        catch (IOException e){
            System.err.println("Write to file error!");
        }
    }
}
