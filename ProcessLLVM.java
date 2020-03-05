import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class ProcessLLVM {
    private static final String OUTPUT_PATH = "optout.txt";

    public static void main(String[] args) {
        HashSet<String> set = new HashSet<>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(OUTPUT_PATH));
            String line = reader.readLine();
            int firstIndex;
            while (line != null) {
                try {
                    if (line.charAt(0) == 'C') {
                        firstIndex = line.indexOf('\'') + 1;
                        if(firstIndex == 0) {
                            firstIndex = line.indexOf('<')+2;
                            set.add(line.substring(firstIndex, line.indexOf('>')));
                        } else {
                            set.add(line.substring(firstIndex, line.indexOf('\'', firstIndex)));
                        }
                    }
                } catch (IndexOutOfBoundsException e) {}

                line = reader.readLine();
            }

            System.out.println(set.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
