package HuffmanEncoding;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

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

    public static byte[] readBytesFromFile(String filename){
        try{
            return Files.readAllBytes(Paths.get(filename));
        }
        catch (java.io.IOException e){
            System.err.println("Read bytes from file error!");
        }
        return null;
    }

    public static void writeToFile(String filename, String s){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
            writer.write(s);
        }
        catch (IOException e){
            System.err.println("Write to file error!");
        }
    }

    public static void writeBytesToFile(String filename, byte[] bytes) {
        try(FileOutputStream os = new FileOutputStream(filename)){
            os.write(bytes);
        }
        catch (IOException e){
            System.err.println("Write bytes to file error!");
        }
    }

    public static HashMap<String, Character> loadDecodeTable(String table_fn){
        HashMap<String, Character> decode_table = new HashMap<>();
        try(BufferedReader reader = new BufferedReader
                (new InputStreamReader(new FileInputStream(table_fn), StandardCharsets.UTF_8))){
            String line = reader.readLine();
            while(line != null){
                String letter_code = line.split(" : ")[0].replaceFirst("0x", "");
                Character letter = (char)Integer.parseInt(letter_code, 16);
                String code = line.split(" : ")[1];
                decode_table.put(code, letter);
                line = reader.readLine();
            }
            return decode_table;
        }
        catch (IOException e){
            System.err.println("Retrieve code-table error!");
        }
        return null;
    }

    public static void exportCodeTable(HashMap<Character, String> table, String filename){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
            String hex_repro;
            for(int i = 0; i < 65536; i++){
                if(table.get((char)i) != null){
                    hex_repro = String.format("0x%04X", i);
                    writer.write(hex_repro + " : " + table.get((char)i));
                    writer.newLine();
                }
            }
        }
        catch (IOException e){
            System.err.println("Write to file error!");
        }
    }
}
