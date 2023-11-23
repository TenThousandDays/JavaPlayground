package HuffmanEncoding;

import java.util.*;

public class HuffmanEncoder {

    // Used to parse CLI args like '-arg1 val_1 -arg2 val_2'
    private static Map<String, List<String>> getArgs(String[] args){
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

    public static void encode(String filename){
        if(!filename.endsWith(".txt")) throw new IllegalArgumentException("Only plaintext (.txt) can be encoded!");
        String data = FileHelper.readFromFile(filename);
        if(data == null){
            throw new RuntimeException("Can't encode null-data!");
        }
        BinaryTree huffmanTree = HuffmanHelper.getHuffmanCodeTree(
                HuffmanHelper.getQueueFromHuffmanTable(
                        HuffmanHelper.makeHuffmanTable(data)));
        String[] codeTable = new String[256];
        HuffmanHelper.fillCodeTable(codeTable, huffmanTree.root(), "", "");
        StringBuilder output = new StringBuilder();
        for(char i : data.toCharArray()){
            output.append(codeTable[i]);
        }
        String fn_prefix = filename.split("\\.")[0];
        String out_fn =  fn_prefix + ".huff";
        String table_out_fn = fn_prefix + ".table";
        FileHelper.writeToFile(out_fn, output.toString());
        HuffmanHelper.exportCodeTable(codeTable, table_out_fn);
    }

    public static void decode(String filename){
        if(!filename.endsWith(".huff")) throw new IllegalArgumentException("Only coded text (.huff) can be decoded!");
        String fn_prefix = filename.split("\\.")[0];
        String decode_table_fn = fn_prefix + ".table";
        HashMap<String, Character> code_table = HuffmanHelper.getDecodeTable(decode_table_fn);
        String encoded_data = FileHelper.readFromFile(filename);
        if(encoded_data == null){
            throw new RuntimeException("Can't decode null-data!");
        }
        StringBuilder plaintext = new StringBuilder();
        while(encoded_data.length() > 0){
            for(String code : code_table.keySet()){
                if(encoded_data.startsWith(code)){
                    plaintext.append(code_table.get(code));
                    encoded_data = encoded_data.replaceFirst(code, "");
                }
            }
        }
        String output_fn = fn_prefix + ".decoded.txt";
        FileHelper.writeToFile(output_fn, plaintext.toString());
    }

    public static void main(String[] args){
        Map<String, List<String>> parsedArgs = getArgs(args);
        if(parsedArgs.size() == 0){
            throw new IllegalArgumentException("You should specify arguments! -mode encode/decode -fn filename");
        }
        String filename, mode;
        try{
            filename = parsedArgs.get("fn").get(0);
        }
        catch(NullPointerException e){
            throw new IllegalArgumentException("You should specify filename! (-fn)");
        }
        try{
            mode = parsedArgs.get("mode").get(0);
        }
        catch(NullPointerException e){
            throw new IllegalArgumentException("You should specify encode/decode mode! (-mode)");
        }
        if(mode.equals("encode")) encode(filename);
        else if(mode.equals("decode")) decode(filename);
        else throw new IllegalArgumentException("Incorrect mode!");
    }

}