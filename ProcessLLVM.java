import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ProcessLLVM {
    private static final String OUTPUT_PATH = "optout.txt";

    public static void main(String[] args) {
        //HashSet<String> set = new HashSet<>();

        //Getting optout.txt comes from args[0], not from a file reader.
        HashMap<String, HashSet<String>> graph = getGraph();

        double cThresh = 0.65;
        Integer sThresh = 3;

        HashMap<String, Integer> covg = getCoverage(graph);

        /**
        //Printing to debug graph coverage.
         for (HashMap.Entry<String, Integer> entry : covg.entrySet()) {
            String key = entry.getKey();
            Integer val = entry.getValue();
            System.out.println(key + ": " + val);
        }//*/

        HashMap<String, HashMap<String, Integer>> covp = getPairCoverage(graph);

        /**
        //Printing to debug graph coverage.
        for (HashMap.Entry<String, HashMap<String, Integer>> entry : covp.entrySet()) {
            String key = entry.getKey();
            HashMap<String, Integer> map2 = entry.getValue();
            for(HashMap.Entry<String, Integer> entry2 : map2.entrySet()){
                System.out.println("("+key+", "+entry2.getKey()+"): "+entry2.getValue());
            }
            // System.out.println("("+key.getKey() + ", " + key.getValue() + "): " + val);
        }//*/

        HashMap<String, HashMap<String, Double>> confidences = new HashMap<String, HashMap<String, Double>>();
        for(HashMap.Entry<String, HashMap<String, Integer>> entry : covp.entrySet()){
            String key = entry.getKey();
            for(HashMap.Entry<String, Integer> entry2 : entry.getValue().entrySet()){
                if(entry2.getValue() >= sThresh){
                    double conf = (double)entry2.getValue() / (double)covg.get(key);
                    if(conf >= cThresh) {
                        if(confidences.get(key) == null){
                            HashMap<String, Double> tmp = new HashMap<String, Double>();
                            tmp.put(entry2.getKey(), conf);
                            confidences.put(key, tmp);
                        } else if(confidences.get(key).get(entry2.getKey()) == null) {
                            confidences.get(key).put(entry2.getKey(), conf);
                        } else {
                            System.out.println("CRITICAL ERROR: DUPLICATE IN HASHMAP");
                        }
                    }
                }
            }
        }

        for(HashMap.Entry<String, HashMap<String, Double>> entry : confidences.entrySet()){
            for(HashMap.Entry<String, Double> entry2 : entry.getValue().entrySet()){
                System.out.printf("%.2f%%\t%s\t%s\n", entry2.getValue().doubleValue()*100.00, entry.getKey(), entry2.getKey());
            }
        }

        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            String scope = entry.getKey();
            String[] vals = new String[entry.getValue().size()];
            vals = entry.getValue().toArray(vals);
            for (int i = 0; i < vals.length; i++) {
                if(confidences.get(vals[i]) != null) {
                    HashSet<String> used = new HashSet<String>();
                    for(int j = 0; j < vals.length; j++){
                        if(confidences.get(vals[i]).get(vals[j]) != null) {
                            used.add(vals[j]);
                        }
                        if(j == i) j++;
                    }
                    for(HashMap.Entry<String, Double> entry2 : confidences.get(vals[i]).entrySet()){
                        if(!used.contains(entry2.getKey())){
                            System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", vals[i], scope, vals[i], entry2.getKey(), covp.get(vals[i]).get(entry2.getKey()).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                        }
                    }
                }
            }
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
        }//*/
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public static HashMap<String, HashMap<String, Integer>> getPairCoverage(HashMap<String, HashSet<String>> graph) {
        HashMap<String, HashMap<String, Integer>> covp = new HashMap<String, HashMap<String, Integer>>();
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){
            String key = entry.getKey();
            String vals[] = new String[entry.getValue().size()];
            vals = entry.getValue().toArray(vals);
            for (int i = 0; i < vals.length; i++) {
                for(int j = i + 1; j < vals.length; j++){
                    //Pair<String, String> key = getPair(vals[i],vals[j], 0);
                    if(covp.get(vals[i]) == null) {
                        HashMap<String, Integer> tmp = new HashMap<>();
                        tmp.put(vals[j], 1);
                        covp.put(vals[i], tmp);
                    } else if(covp.get(vals[i]).get(vals[j]) == null) {
                        covp.get(vals[i]).put(vals[j], 1);
                    } else {
                        Integer val = covp.get(vals[i]).get(vals[j]);
                        covp.get(vals[i]).replace(vals[j], val+1);
                    }
                    if(covp.get(vals[j]) == null) {
                        HashMap<String, Integer> tmp = new  HashMap<>();
                        tmp.put(vals[i], 1);
                        covp.put(vals[i], tmp);
                    } else if(covp.get(vals[j]).get(vals[i]) == null) {
                        covp.get(vals[j]).put(vals[i], 1);
                    } else {
                        Integer val = covp.get(vals[j]).get(vals[i]);
                        covp.get(vals[j]).replace(vals[i], val+1);
                    }
                }
            }
        }
        return covp;
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
