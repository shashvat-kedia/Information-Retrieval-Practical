import java.io.*;
import java.util.*;

public class Inverted_Index {

    List<String> stopwords = new ArrayList<String>();

    Map<String, MapEntry> index = new HashMap<String, MapEntry>();
    static List<String> files = new ArrayList<String>();

    private void readStopwordsFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("stopwords.csv")));
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            while (tokenizer.hasMoreTokens()) {
                stopwords.add(tokenizer.nextToken());
            }
        }
        System.out.println(stopwords.size());
    }

    public void indexFile(File file) throws IOException {
        System.out.println("Here");
        int fileno = files.indexOf(file.getPath());
        if (fileno == -1) {
            files.add(file.getPath());
            fileno = files.size() - 1;
        }

        int pos = 0;
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            for (String _word : line.split("\\W+")) {
                String word = _word.toLowerCase();
                pos++;
                if (stopwords.contains(word))
                    continue;
                MapEntry idx = index.get(word);
                if (idx == null) {
                    idx = new MapEntry(new LinkedList<Tuple>(), 1);
                    index.put(word, idx);
                } else {
                    idx.freq += 1;
                }
                idx.list.add(new Tuple(fileno, pos));
            }
        }
        System.out.println("indexed " + file.getPath() + " " + pos + " words");
    }

    public void splitFile() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("articles1.csv")));
        int count = 1;
        String rr = "";
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            int flag = 1;
            while (tokenizer.hasMoreTokens()) {
                if (flag <= 8) {
                    tokenizer.nextToken();
                    flag++;
                } else {
                    rr += tokenizer.nextToken() + " ";
                    if (tokenizer.countTokens() == 1) {
                        break;
                    }
                }
            }
            count++;
            if (count % 5 == 0) {
                FileWriter writer = new FileWriter("data/data_split_" + count + ".csv");
                writer.write(rr);
                writer.flush();
                rr = "";
            }
            if (count == 100) {
                break;
            }
        }
    }

    public void search(List<String> words) {
        for (String _word : words) {
            Set<String> answer = new HashSet<String>();
            String word = _word.toLowerCase();
            MapEntry idx = index.get(word);
            if (idx != null) {
                for (Tuple t : idx.list) {
                    answer.add(files.get(t.fileno));
                }
                System.out.println("Frequency: " + idx.freq);
            }
            System.out.print(word);
            for (String f : answer) {
                System.out.print(" " + f);
            }
            System.out.println("");
        }
    }

    private void and(String word1, String word2) {
        MapEntry entry = index.get(word1);
        MapEntry entry1 = index.get(word2);
        if (entry == null || entry1 == null) {
            System.out.println("No common files");
        }
        for (int i = 0; i < entry.list.size(); i++) {
            for (int j = 0; j < entry1.list.size(); j++) {
                if (entry.list.get(i).fileno == entry1.list.get(j).fileno) {
                    System.out.print(files.get(entry.list.get(i).fileno));
                    break;
                }
            }
        }
    }

    private void not(String word1) {
        MapEntry entry = index.get(word1);
        int flag = 0;
        if (entry == null) {
            for (int i = 0; i < files.size(); i++) {
                System.out.print(files.get(i) + " ");
            }
        } else {
            for (int i = 0; i < files.size(); i++) {
                for (int j = 0; j < entry.list.size(); j++) {
                    if (i == entry.list.get(j).fileno) {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    System.out.print(files.get(i) + " ");
                }
                flag = 0;
            }
        }
    }

    private void or(String word1, String word2) {
        MapEntry entry = index.get(word1);
        MapEntry entry1 = index.get(word2);
        Set<Integer> temp = new HashSet<Integer>();
        for (int i = 0; i < entry.list.size(); i++) {
            for (int j = 0; j < entry1.list.size(); j++) {
                temp.add(entry.list.get(i).fileno);
            }
        }
        Iterator<Integer> iterator = temp.iterator();
        while(iterator.hasNext()){
            System.out.print(files.get(iterator.next()) + " ");
        }
    }

    public static void main(String[] args) {
        files.add("data.csv");
        try {
            Inverted_Index idx = new Inverted_Index();
            idx.readStopwordsFile();
            for (int i = 5; i <= 100; i += 5) {
                idx.indexFile(new File("data/data_split_" + i + ".csv"));
            }
            idx.and("salary", "income");
            System.out.println("");
            idx.or("salary","income");
            System.out.println("");
            idx.not("salary");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Tuple {
        private int fileno;
        private int position;

        public Tuple(int fileno, int position) {
            this.fileno = fileno;
            this.position = position;
        }
    }

    private class MapEntry {
        private List<Tuple> list;
        private int freq;

        public MapEntry(List<Tuple> list, int freq) {
            this.list = list;
            this.freq = freq;
        }
    }
}