import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ProcessLLVM {
    private static String OUTPUT_PATH;// = "mainout.txt";
    private static double cThresh;// = 0.65;
    private static Integer sThresh;// = 3;
    private static int expandBy;// = 2;

    public static void main(String[] args) {
        //HashSet<String> set = new HashSet<>();
        /**
        for(String s : args) {
            System.out.println(s);
        }
        System.out.println();//*/
        if(args.length > 2) {
            OUTPUT_PATH = args[0];
            cThresh = Double.parseDouble(args[1]);
            sThresh = Integer.parseInt(args[2]);
        } if(args.length > 4) {
            if(args[3].equals("-c")) {
                expandBy = Integer.parseInt(args[4]);
            } else if(args[3].equals("-d")) {
                //Correction for
            }
        }


        //Getting optout.txt comes from args[0], not from a file reader.
        HashMap<String, HashSet<String>> graph = new HashMap<String, HashSet<String>>();
        if(expandBy > 0){
            graph = getExpandedGraph(getGraph(), expandBy);
        } else {
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
                //System.out.println("("+key+", "+entry2.getKey()+"): "+entry2.getValue());
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
        /**
        for(HashMap.Entry<String, HashMap<String, Double>> entry : confidences.entrySet()){
            for(HashMap.Entry<String, Double> entry2 : entry.getValue().entrySet()){
                System.out.printf("%.2f%%\t%s\t%s\n", entry2.getValue().doubleValue()*100.00, entry.getKey(), entry2.getKey());
            }
        }//*/

        int bugCount = 0;

        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()) {
            String scope = entry.getKey();
            String[] vals = new String[0];//entry.getValue().size()];
            vals = entry.getValue().toArray(vals);
            //System.out.println("-----"+scope+"-----");
            //System.out.println("...."+entry.getValue().toString()+"....");
            for (int i = 0; i < vals.length; i++) {
                //**
                //System.out.println(i+":::");
                if(confidences.get(vals[i]) != null) {
                    HashSet<String> used = new HashSet<String>();
                    //System.out.print(vals[i]+": ");
                    for(int j = 0; j < vals.length; j++){
                        //System.out.print("["+vals[j]+"],");
                        if(j == i) {
                            continue;
                        }
                        //System.out.print(vals[j]+",");
                        //if(confidences.get(vals[i]).get(vals[j]) != null) {
                        //System.out.print(vals[j]+", ");
                        used.add(vals[j]);
                        //}
                    }
                    //System.out.println();
                    for(HashMap.Entry<String, Double> entry2 : confidences.get(vals[i]).entrySet()){
                        if(!used.contains(entry2.getKey())){
                            //bugCount++;
                            if(vals[i].compareTo(entry2.getKey()) < 0 ){
                                System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%%s", vals[i], scope, vals[i], entry2.getKey(), covp.get(vals[i]).get(entry2.getKey()).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00, System.getProperty("line.separator"));
                                //System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d/%d, confidence: %.2f%%\n", vals[i], scope, vals[i], entry2.getKey(), covp.get(vals[i]).get(entry2.getKey()).intValue(), covg.get(vals[i]).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                            } else {
                                System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%%s", vals[i], scope, entry2.getKey(), vals[i], covp.get(vals[i]).get(entry2.getKey()).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00, System.getProperty("line.separator"));
                                //System.out.printf("bug: %s in %s, pair: (%s, %s), support: %d/%d, confidence: %.2f%%\n", vals[i], scope, entry2.getKey(), vals[i], covp.get(vals[i]).get(entry2.getKey()).intValue(), covg.get(vals[i]).intValue(), confidences.get(vals[i]).get(entry2.getKey()).doubleValue()*100.00);
                            }
                            //System.out.println();
                        }
                    }
                }
                //*/
            }
            //System.out.println();
        }
        /**
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }//*/
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
            //System.out.println(key+":");
            for (int a = 0; a < vals.length; a++) {
                for(int b = a + 1; b < vals.length; b++){
                    //if((vals[a].equals("ap_exists_config_define") && vals[b].equals("apr_file_open_stdout")) || (vals[b].equals("ap_exists_config_define") && vals[a].equals("apr_file_open_stdout"))){
                    //    System.out.println(key+": "+ vals[a] + ", " + vals[b]);
                    //}
                    if(covp.get(vals[a]) == null) {
                        HashMap<String, Integer> tmp = new HashMap<>();
                        tmp.put(vals[b], 1);
                        covp.put(vals[a], tmp);
                        //System.out.print("1("+vals[i]+","+vals[j]+","+covp.get(vals[i]).get(vals[j])+")\t");
                    } else if(covp.get(vals[a]).get(vals[b]) == null) {
                        //System.out.println(covp.get(vals[a]).get(vals[b]));
                        covp.get(vals[a]).put(vals[b], 1);
                        //System.out.print("2("+vals[i]+","+vals[j]+","+1+")\t");
                    } else {
                        Integer val = covp.get(vals[a]).get(vals[b]);
                        covp.get(vals[a]).replace(vals[b], val+1);
                        //System.out.print("3("+vals[i]+","+vals[j]+","+(val+1)+")\t");
                    }
                    if(covp.get(vals[b]) == null) {
                        HashMap<String, Integer> tmp = new  HashMap<>();
                        tmp.put(vals[a], 1);
                        covp.put(vals[b], tmp);
                        //System.out.print("a("+vals[i]+","+vals[j]+","+covp.get(vals[j]).get(vals[i])+")\t");
                    } else if(covp.get(vals[b]).get(vals[a]) == null) {
                        //System.out.println(covp.get(vals[b]).get(vals[a]));
                        covp.get(vals[b]).put(vals[a], 1);
                        //System.out.print("b("+vals[i]+","+vals[j]+","+1+")\t");
                    } else {
                        Integer val = covp.get(vals[b]).get(vals[a]);
                        //System.out.print("c("+vals[i]+","+vals[j]+","+(val+1)+")\t");
                        covp.get(vals[b]).replace(vals[a], val+1);
                    }
                }
            }
            //System.out.println();
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
                        String key = "";
                        //To get the function <<null function>> this case is required.
                        if(firstIndex != 0) {
                            key = line.substring(firstIndex, line.indexOf('\'', firstIndex));
                            //if(key.equals("ap_read_config")) System.out.print(key + ": {");
                            line = reader.readLine();
                            while (line != null && line.charAt(0) == ' ') {
                                firstIndex = line.indexOf('\'') + 1;
                                if (line.indexOf('\'') != -1 && graph.get(key) == null) {
                                    HashSet<String> gSet = new HashSet<String>();
                                    gSet.add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                    //if(key.equals("ap_read_config")) System.out.println("Key:" + key + "\nValue:"+line.substring(firstIndex, line.indexOf('\'', firstIndex))+"\nSize:"+graph.size()+"\n" );
                                    //if(key.equals("ap_read_config")) System.out.print(line.substring(firstIndex, line.indexOf('\'', firstIndex)) + ", ");
                                    graph.put(key, (HashSet) gSet.clone());
                                } else if(line.indexOf('\'') != -1) {
                                    //if(key.equals("ap_read_config")) System.out.println("Key:" + key + "\nValue:"+line.substring(firstIndex, line.indexOf('\'', firstIndex))+"\nSize:"+graph.size()+"\n" );
                                    graph.get(key).add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                    //if(key.equals("ap_read_config")) System.out.print(line.substring(firstIndex, line.indexOf('\'', firstIndex)) + ", ");
                                }
                                line = reader.readLine();
                                //if(key.equals("ap_read_config")) System.out.println(""+(line != null) + " && " + (line.charAt(0) == ' '));
                            }
                        }
                        //if(key.equals("ap_read_config")) System.out.println();
                    }
                } catch (IndexOutOfBoundsException e) {}
                //System.out.println("}");
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static HashMap<String, HashSet<String>> getExpandedGraph(HashMap<String, HashSet<String>> graph, int expandBy){
        HashMap<String, HashSet<String>> graphExp = new HashMap<String, HashSet<String>>();
        /**
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(OUTPUT_PATH));
            String line = reader.readLine();
            int firstIndex;
            while (line != null) {
                try {
                    if (line.charAt(0) == 'C') {
                        firstIndex = line.indexOf('\'') + 1;
                        String key = "";
                        //To get the function <<null function>> this case is required.
                        if(firstIndex != 0) {
                            key = line.substring(firstIndex, line.indexOf('\'', firstIndex));
                            //if(key.equals("ap_read_config")) System.out.print(key + ": {");
                            line = reader.readLine();
                            while (line != null && line.charAt(0) == ' ') {
                                firstIndex = line.indexOf('\'') + 1;
                                if (line.indexOf('\'') != -1 && graph.get(key) == null) {
                                    HashSet<String> gSet = new HashSet<String>();
                                    gSet.add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                    //if(key.equals("ap_read_config")) System.out.println("Key:" + key + "\nValue:"+line.substring(firstIndex, line.indexOf('\'', firstIndex))+"\nSize:"+graph.size()+"\n" );
                                    //if(key.equals("ap_read_config")) System.out.print(line.substring(firstIndex, line.indexOf('\'', firstIndex)) + ", ");
                                    graph.put(key, (HashSet) gSet.clone());
                                } else if(line.indexOf('\'') != -1) {
                                    //if(key.equals("ap_read_config")) System.out.println("Key:" + key + "\nValue:"+line.substring(firstIndex, line.indexOf('\'', firstIndex))+"\nSize:"+graph.size()+"\n" );
                                    graph.get(key).add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                                    //if(key.equals("ap_read_config")) System.out.print(line.substring(firstIndex, line.indexOf('\'', firstIndex)) + ", ");
                                }
                                line = reader.readLine();
                                //if(key.equals("ap_read_config")) System.out.println(""+(line != null) + " && " + (line.charAt(0) == ' '));
                            }
                        }
                        //if(key.equals("ap_read_config")) System.out.println();
                    }
                } catch (IndexOutOfBoundsException e) {}
                //System.out.println("}");
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }//*/
        for(HashMap.Entry<String, HashSet<String>> entry : graph.entrySet()){
            graphExp.put(entry.getKey(), new HashSet<String>());
            HashSet<String> srcExpanded = new HashSet<String>();
            int expanded = 0;
            while(expandBy >= expanded) {
                //System.out.println(expanded);
                for (String src : entry.getValue().toArray(new String[0])) {
                    graphExp.get(entry.getKey()).add(src);
                    //System.out.println(graph.get(src));
                    if (graph.get(src) != null && !srcExpanded.contains(src)) {
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
