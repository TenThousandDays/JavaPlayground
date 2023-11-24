package HuffmanEncoding;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileHelper {

    public static String readFromFile(String filename){
        try(BufferedReader reader = new BufferedReader
                (new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8))){
            return reader.readLine();
        }
        catch (IOException e){
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
