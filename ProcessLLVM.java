import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProcessLLVM {
    private static final String OUTPUT_PATH = "optout.txt";

    public static void main(String[] args) {
        //HashSet<String> set = new HashSet<>();

        //Getting optout.txt comes from args[0], not from a file reader.
        HashMap<String, HashSet<String>> graph = getGraph();

        HashMap<String, Integer> covg = getCoverage(graph);

        for (HashMap.Entry<String, Integer> entry : covg.entrySet()) {
            String key = entry.getKey();
            Integer val = entry.getValue();
            System.out.println(key + ": " + val);
        }


        /**
        // Basic printing for testing graph completion / correctness.
        for (HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            String key = entry.getKey();
            HashSet<String> val = entry.getValue();
            System.out.print(key + ": {" + val.toArray()[0]);
            boolean b = false;
            for (String str : val) {
                if(b) {
                    System.out.print(", " + str);
                } else {
                    b = true;
                }
            }
            System.out.println("}");
        }// */
    }

    public static HashMap<String, Integer> getCoverage(HashMap<String, HashSet<String>> graph) {
        HashMap<String, Integer> covg = new HashMap<String, Integer>();
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){
            //String key = entry.getKey();
            for (String str : entry.getValue()) {
                if(covg.get(str) == null) {
                    covg.put(str, 1);
                } else {
                    Integer val = covg.get(str);
                    covg.replace(str, val+1);
                }
            }
        }
        return covg;
    }

    public static HashMap<Map.Entry<String, String>, Integer> getPairCoverage(HashMap<String, HashSet<String>> graph) {

        return null;
    }

    public static HashMap<String, HashSet<String>> getGraph(){
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
                        //To get the function <<null function>> this case is required.
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
                                continue;
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
        return graph;
    }
}
