package HuffmanEncoding;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Encoder {

    public static Map<String, List<String>> getArgs(String[] args){
        final Map<String, List<String>> params = new HashMap<>();
        List<String> options = null;
        for(String arg : args){
            if(arg.charAt(0) == '-'){
                if(arg.length() < 2){
                    System.err.println("Invalid argument: " + arg);
                    throw new IllegalArgumentException();
                }
                options = new ArrayList<>();
                params.put(arg.substring(1), options);
            }
            else if(options != null){
                options.add(arg);
            }
            else{
                System.err.println("Illegal parameter usage");
                throw new IllegalArgumentException();
            }
        }
        return params;
    }

    private static double getCompressionRatio(String rawData, byte[] encodedData){
        byte[] data_bytes = rawData.getBytes(StandardCharsets.UTF_8);
        double dataByteCount = data_bytes.length;
        double encodedByteCount = encodedData.length;
        return dataByteCount / encodedByteCount;
    }

    private static byte[] convertBinaryStringToBytes(String s){
        byte[] res = new byte[s.length() / 8];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte)Integer.parseInt(s.substring(i * 8, (i + 1) * 8), 2);
        }
        return res;
    }

    private static String convertBytesToBinaryString(byte[] bytes){
        StringBuilder res = new StringBuilder();
        for (byte aByte : bytes) {
            res.append(String.format("%8s", Integer.toBinaryString(aByte & 0xff)).replace(" ", "0"));
        }
        return res.toString();
    }

    public static void encode(String filename){
        if(!filename.endsWith(".txt")) throw new IllegalArgumentException("Only plaintext (.txt) can be encoded!");
        String data = FileHelper.readFromFile(filename);
        if(data == null){
            throw new RuntimeException("Can't encode null-data!");
        }

        HuffmanTree tree = new HuffmanTree(data);
        String msg = tree.getMessage();
        HashMap<Character, String> codeTable = tree.getCodeTable();

        StringBuilder intermediate = new StringBuilder();
        for(int i = 0; i < msg.length(); i++){
            intermediate.append(codeTable.get(msg.charAt(i)));
        }
        byte counter = 0;
        for(int length = intermediate.length(), delta = 8 - length % 8; counter < delta; counter++){
            intermediate.append("0");
        }

        // Padding zeros count is the 1st byte of stream
        String compressed = String.format("%8s", Integer.toBinaryString(counter & 0xff)).replace(" ", "0")
                + intermediate;
        byte[] compressedBytes = convertBinaryStringToBytes(compressed);

        String fn_base = filename.split("\\.")[0];
        String out_fn =  fn_base + ".huff";
        String table_out_fn = fn_base + ".table";

        FileHelper.writeBytesToFile(out_fn, compressedBytes);
        FileHelper.exportCodeTable(codeTable, table_out_fn);
        double ratio = getCompressionRatio(msg, compressedBytes);
        System.out.println("Compression ratio: " + ratio);
        System.out.println("Saved encoded data to " + out_fn);
    }

    public static void decode(String filename){
        if(!filename.endsWith(".huff")) throw new IllegalArgumentException("Only coded text (.huff) can be decoded!");
        String fn_base = filename.split("\\.")[0];
        String decode_table_fn = fn_base + ".table";
        String output_fn = fn_base + ".decoded.txt";

        HashMap<String, Character> code_table = FileHelper.loadDecodeTable(decode_table_fn);
        if(code_table == null){
            System.err.println("Decode table load error!");
            return;
        }

        byte[] encoded_bytes = FileHelper.readBytesFromFile(filename);
        if(encoded_bytes == null){
            throw new RuntimeException("Can't decode null-data!");
        }
        String encoded_str = convertBytesToBinaryString(encoded_bytes);

        String delta = encoded_str.substring(0, 8);
        int ZEROES = Integer.parseInt(delta, 2);

        StringBuilder decoded = new StringBuilder();
        String current = "";
        for(int i = 8; i < encoded_str.length() - ZEROES; i++){
            current += encoded_str.charAt(i);
            if(code_table.get(current) != null){
                decoded.append(code_table.get(current));
                current = "";
            }
        }

        FileHelper.writeToFile(output_fn, decoded.toString());
        System.out.println("Saved decoded data to " + output_fn);
    }

}
