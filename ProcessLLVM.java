import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


public class ProcessLLVM {
    private static String OUTPUT_PATH;// Output file path, what to read from.
    private static double cThresh;// coverage % threshold
    private static Integer sThresh;// scope threshold
    private static int expandBy; // expansion amount for part c.

    /**
     * This main runs the core of the program, and handles for different arguments.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        //long start = System.currentTimeMillis();

        if(args.length > 2) { //Get standard args
            OUTPUT_PATH = args[0];
            cThresh = Double.parseDouble(args[2]);
            cThresh /= 100.00;
            sThresh = Integer.parseInt(args[1]);
        } if(args.length > 4) {//Get improvement args.
            if(args[3].equals("-c")) {
                expandBy = Integer.parseInt(args[4]);
            } else if(args[3].equals("-d")) {
                //Improvement args for part D
            }
        }

        //The graph which will be used for all our work.
        //We chose a hashmap because they are incredibly fast compared to a normal data type, and don't use any string comparisons.
        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>>();
        if(expandBy > 0){//Expand is requested, so call getExpandGraph.
            graph = getExpandedGraph(getGraph(), expandBy);
        } else {//Expand isn't requested, get normal graph.
            graph = getGraph();
        }

         /**
         // Basic printing for testing graph completion / correctness.
         for (HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
             String key = entry.getKey();
             HashSet<String> val = entry.getValue();
             System.out.print(key + ": {" + val.toArray()[0]);
             boolean b = false;
             for (String str : val) {
                 if (b) {
                     System.out.print(", " + str);
                 } else {
                     b = true;
                 }
             }
             System.out.println("}");
         }//*/

        HashMap<String, Integer> covg = getCoverage(graph); //get coverage of graph.

        /**
        //Printing to debug graph coverage.
         for (HashMap.Entry<String, Integer> entry : covg.entrySet()) {
            String key = entry.getKey();
            Integer val = entry.getValue();
            System.out.println(key + ": " + val);
        }//*/

        HashMap<String, HashMap<String, Integer>> covp = getPairCoverage(graph); //get pair coverage.

        /**
        //Printing to debug graph pair coverage.
        for (HashMap.Entry<String, HashMap<String, Integer>> entry : covp.entrySet()) {
            String key = entry.getKey();
            HashMap<String, Integer> map2 = entry.getValue();
            for(HashMap.Entry<String, Integer> entry2 : map2.entrySet()){
                System.out.println("("+key+", "+entry2.getKey()+"): "+entry2.getValue());
            }
            System.out.println("("+key.getKey() + ", " + key.getValue() + "): " + val);
        }//*/

        //This is where the confidences are calculated. We elected to not put it into another loop.
        HashMap<String, HashMap<String, Double>> confidences = new HashMap<String, HashMap<String, Double>>();
        for(HashMap.Entry<String, HashMap<String, Integer>> entry : covp.entrySet()){
            String key = entry.getKey();
            for(HashMap.Entry<String, Integer> entry2 : entry.getValue().entrySet()){
                if(entry2.getValue() >= sThresh){ //support threshold passed.
                    double conf = (double)entry2.getValue() / (double)covg.get(key);
                    if(conf >= cThresh) { // confidence threshold passed.
                        if(confidences.get(key) == null){
                            HashMap<String, Double> tmp = new HashMap<String, Double>();
                            tmp.put(entry2.getKey(), conf);
                            confidences.put(key, tmp);
                        } else if(confidences.get(key).get(entry2.getKey()) == null) {
                            confidences.get(key).put(entry2.getKey(), conf);
                        } else {
                            System.err.println("CRITICAL ERROR: DUPLICATE IN HASHMAP");
                        }
                    }
                }
            }
        }
        /**
        //Print the confidence hashmap, for debugging purposes.
        for(HashMap.Entry<String, HashMap<String, Double>> entry : confidences.entrySet()){
            for(HashMap.Entry<String, Double> entry2 : entry.getValue().entrySet()){
                System.out.printf("%.2f%%\t%s\t%s\n", entry2.getValue().doubleValue()*100.00, entry.getKey(), entry2.getKey());
            }
        }//*/

        //Find and print all scope, A, B, coverage, and supports
        int count = 0;
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            String scope = entry.getKey();
            String[] vals = new String[0];
            vals = entry.getValue().toArray(vals);
            for (int i = 0; i < vals.length; i++) { //Looping through all A's of the (A,B) pairs.
                if(confidences.get(vals[i]) != null) { //
                    HashSet<String> used = new HashSet<String>();
                    for(int j = 0; j < vals.length; j++){
                        if(j == i) { //We need to ignore the first value (A).
                            continue;
                        }
                        used.add(vals[j]); //indicate the value was used.
                    }
                    for(HashMap.Entry<String, Double> entry2 : confidences.get(vals[i]).entrySet()){
                        //System.out.print(used.contains(entry2.getKey()+"["+i+"], "));
                        if(!used.contains(entry2.getKey())){ //If the value isn't used, it violates the confidence interval.
                            count++;
                            if(vals[i].compareTo(entry2.getKey()) < 0 ){ //Sort A,B.
                                System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", vals[i], scope, vals[i], entry2.getKey(), covp.get(vals[i]).get(entry2.getKey()).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                                //System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d/%d, confidence: %.2f%%\n", vals[i], scope, vals[i], entry2.getKey(), covp.get(vals[i]).get(entry2.getKey()).intValue(), covg.get(vals[i]).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                            } else { //Sort B,A.
                                System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n", vals[i], scope, entry2.getKey(), vals[i], covp.get(vals[i]).get(entry2.getKey()).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                                //System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d/%d, confidence: %.2f%%\n", vals[i], scope, entry2.getKey(), vals[i], covp.get(vals[i]).get(entry2.getKey()).intValue(), covg.get(vals[i]).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                            }
                        }
                    }
                }
            }
        }
        /**
        //Used for testing the memory useage.
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }//*/
        //long end = System.currentTimeMillis();
        //System.out.println("Count["+expandBy+"]: " + count);
        //System.out.println("Time: " + (end-start) + "ms");
    }

    /**
     * Gets a coverage hashmap based on the graph.
     * @param graph the coverage graph hashmap.
     * @return the coverage hashmap.
     */
    public static HashMap<String, Integer> getCoverage(HashMap<String, HashSet<String>> graph) {
        HashMap<String, Integer> covg = new HashMap<String, Integer>();
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){ //iterate through hashmap
            for (String str : entry.getValue()) { // iterate through hashset.
                if(covg.get(str) == null) { //The string isn't in the graph yet, so we initialize it with 1.
                    covg.put(str, 1);
                } else {
                    Integer val = covg.get(str);
                    covg.replace(str, val+1); //increment.
                }
            }
        }
        return covg;
    }

    /**
     * Gets the pair coverage map for our graph.
     * @param graph
     * @return
     */
    public static HashMap<String, HashMap<String, Integer>> getPairCoverage(HashMap<String, HashSet<String>> graph) {
        HashMap<String, HashMap<String, Integer>> covp = new HashMap<String, HashMap<String, Integer>>();
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){
            String key = entry.getKey();
            String vals[] = new String[entry.getValue().size()];
            vals = entry.getValue().toArray(vals);
            for (int a = 0; a < vals.length; a++) {
                for(int b = a + 1; b < vals.length; b++){
                    if(covp.get(vals[a]) == null) {//The key isn't in the hashmap. Insert the key, and the value as a new hashmap
                        HashMap<String, Integer> tmp = new HashMap<>();
                        tmp.put(vals[b], 1);
                        covp.put(vals[a], tmp);
                    } else if(covp.get(vals[a]).get(vals[b]) == null) {//The value isn't in the hashmap, input it into the hashmap.
                        covp.get(vals[a]).put(vals[b], 1);
                    } else {//The key and value are in both hashmaps, increment the count.
                        Integer val = covp.get(vals[a]).get(vals[b]);
                        covp.get(vals[a]).replace(vals[b], val+1);
                    }
                    //Below we do this again for the second type. This doubles memory useage, but it is still very small because it is a hashmap.
                    if(covp.get(vals[b]) == null) {//The key isn't in the hashmap. Insert the key, and the value as a new hashmap
                        HashMap<String, Integer> tmp = new  HashMap<>();
                        tmp.put(vals[a], 1);
                        covp.put(vals[b], tmp);
                    } else if(covp.get(vals[b]).get(vals[a]) == null) {//The value isn't in the hashmap, input it into the hashmap.
                        covp.get(vals[b]).put(vals[a], 1);
                    } else {//The key and value are in both hashmaps, increment the count.
                        Integer val = covp.get(vals[b]).get(vals[a]);
                        covp.get(vals[b]).replace(vals[a], val+1);
                    }
                }
            }
        }
        return covp;
    }

    /**
     * Get the standard coverage graph.
     * @return the coverage graph.
     */
    public static HashMap<String, HashSet<String>> getGraph(){
        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>>();

        //We read from a file rather than stdin. This isn't as efficient as just reading from stdin, but we already had it working fast enough and didn't want to risk insertion of bugs.
        BufferedReader reader;
        try {//Read the file below.
            reader = new BufferedReader(new FileReader(OUTPUT_PATH));
            String line = reader.readLine();
            int firstIndex;
            while (line != null) {//This loop keeps the function running while the second try block can fail, just to handle odd errors.
                try {
                    if (line.charAt(0) == 'C') { //Check if scope.
                        firstIndex = line.indexOf('\'') + 1;
                        String key = "";
                        if(firstIndex != 0) {//To get the function <<null function>> this case is required.
                            key = line.substring(firstIndex, line.indexOf('\'', firstIndex));
                            line = reader.readLine();
                            while (line != null && line.charAt(0) == ' ') {//Get if this is a function, log it if it is.
                                firstIndex = line.indexOf('\'') + 1; //Search for first quote.
                                if (line.indexOf('\'') != -1 && graph.get(key) == null) {//Add key and the node to the hashmap.
                                    HashSet<String> gSet = new HashSet<String>();
                                    gSet.add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                    graph.put(key, (HashSet) gSet.clone());
                                } else if(line.indexOf('\'') != -1) {//Add the node, so long as it is valid (not extended function).
                                    graph.get(key).add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                }
                                line = reader.readLine();
                            }
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

    /**
     * Get the expanded coverage graph, according to the number of expansions requested.
     * @param graph the normal coverage graph.
     * @param expandBy the number of expansions to make.
     * @return
     */
    public static HashMap<String, HashSet<String>> getExpandedGraph(HashMap<String, HashSet<String>> graph, int expandBy){
        HashMap<String, HashSet<String>> graphExp = new HashMap<String, HashSet<String>>();
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){
            graphExp.put(entry.getKey(), new HashSet<String>());
            HashSet<String> srcExpanded = new HashSet<String>();
            int expanded = 0;

            for(String src : entry.getValue().toArray(new String[0])) {
                graphExp.get(entry.getKey()).add(src);
            }

            while(expandBy > expanded) {//Make sure we don't expand too much.
                //System.out.print(expanded);

                for (String src : graphExp.get(entry.getKey()).toArray(new String[0])) {
                    //graphExp.get(entry.getKey()).add(src);
                    if (graph.get(src) != null && !srcExpanded.contains(src)) {//if the src has been expanded we don't want to waste time.
                        for (String exp : graph.get(src)) {
                            graphExp.get(entry.getKey()).add(exp);
                        }
                        graphExp.get(entry.getKey()).remove(entry.getKey());
                        srcExpanded.add(src);
                    }
                }
                expanded++;
            }
        }
        return graphExp;
    }
}