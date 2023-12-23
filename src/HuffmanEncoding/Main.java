package HuffmanEncoding;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Map<String, List<String>> parsedArgs = Encoder.getArgs(args);
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
        if(mode.equals("encode")) Encoder.encode(filename);
        else if(mode.equals("decode")) Encoder.decode(filename);
        else throw new IllegalArgumentException("Incorrect mode!");
    }
}
