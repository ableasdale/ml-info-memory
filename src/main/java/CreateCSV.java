import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class CreateCSV {

    private static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static String[] counters = {"phys", "size", "rss", "huge", "anon", "file", "forest", "cache", "registry", "unclosed", "swap"};
    private static String[] headers = {"time", "phys", "size", "rss", "huge", "anon", "file", "forest", "cache", "registry", "unclosed", "swap"};
    private static Map<String, List> group = new HashMap<String, List>();
    private static List<String> xaxis = new ArrayList<String>();

    public static void main(String[] args) {
        String fileName = "src/main/resources/SampleLogFile.txt";
        Arrays.stream(counters).forEach(s -> group.put(s, new ArrayList<String>()));
        try {
            Stream<String> stream = Files.lines(Paths.get(fileName));
            stream.filter(s -> s.contains("Info: Memory")).forEach(s -> processLine(s));
            CSVWriter writer = new CSVWriter(new FileWriter("yourfile.csv"));
            writer.writeNext(headers);
            for (int i = 0; i < xaxis.size(); i++) {
                List l = new ArrayList<String>();
                l.add(xaxis.get(i));
                for (String it : counters) {
                    l.add(group.get(it).get(i));
                }
                String[] strings = (String[]) l.parallelStream().toArray(String[]::new);
                writer.writeNext(strings);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void processLine(String s) {
        String[] sArr = s.split(" ");
        String[] xA = sArr[1].split("\\.");
        xaxis.add(xA[0]);
        List<String> set = new ArrayList<String>();
        for (String st : sArr) {
            if (st.contains("=")) {
                processPair(st, set);
            }
        }
        // zero the rest
        int size = group.get(set.get(0)).size();
        for (String it : counters) {
            if (group.get(it).size() < size) {
                List l = group.get(it);
                l.add("0");
                group.put(it, l);
            }
        }
    }

    private static void processPair(String st, List set) {
        String[] pair = st.split("=");
        set.add(pair[0]);
        String value = pair[1].split("\\(")[0];
        List l = group.get(pair[0]);
        l.add(value);
        group.put(pair[0], l);
    }
}
