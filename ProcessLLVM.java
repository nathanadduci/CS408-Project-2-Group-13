import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ProcessLLVM {
    private static final String OUTPUT_PATH = "optout.txt";

    public static void main(String[] args) {
        //HashSet<String> set = new HashSet<>();

        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(OUTPUT_PATH));
            String line = reader.readLine();
            int firstIndex;
            while (line != null) {
                try {
                    if (line.charAt(0) == 'C') {
                        firstIndex = line.indexOf('\'') + 1;
                        String key;
                        if(firstIndex == 0) {
                            firstIndex = line.indexOf('<') + 2;
                            key = line.substring(firstIndex, line.indexOf('>'));
                        } else {
                            key = line.substring(firstIndex, line.indexOf('\'', firstIndex));
                        }
                        line = reader.readLine();
                        while(line != null && line.charAt(0) == ' ') {
                            firstIndex = line.indexOf('\'') + 1;
                            if(graph.get(key) == null) {
                                HashSet<String> gSet = new HashSet<String>();
                                gSet.add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                graph.put(key, (HashSet)gSet.clone());
                            }
                            //System.out.println("Key:" + key + "\nValue:"+line.substring(firstIndex, line.indexOf('\'', firstIndex))+"\nSize:"+graph.size()+"\n" );
                            graph.get(key).add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                            line = reader.readLine();
                        }
                    }
                } catch (IndexOutOfBoundsException e) {}

                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Hello World!\n"+graph.size());

        for (HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            String key = entry.getKey();
            HashSet<String> val = entry.getValue();
            System.out.print(key + ": {" + val.toArray()[0]);
            for (String str : val) {
                System.out.print(", " + str);
            }
            System.out.println("}");
        }
    }
}
