import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class WordCount {

    public static void main(String[] args) {
        List<String> files = Arrays.asList("File1.txt", "File2.txt", "File3.txt", "File4.txt", "File5.txt",
                "File6.txt");
        WordCount wc = new WordCount();

        Map<String, Integer> wordToCount = new HashMap<>();
        for (String file : files) {
            wc.countWords(file, wordToCount);
        }
        System.out.println("count sequentially: " + wordToCount);


        ConcurrentHashMap<String, Integer> concurrentWordToCount = new ConcurrentHashMap<>();
        files.parallelStream().forEach(f -> wc.countWords(f, concurrentWordToCount));
        System.out.println("count concurrent files with stream: " + concurrentWordToCount);

        Map<String, Integer> wordToCountStream = wc.countWithStream(files);
        if (wordToCountStream != null) {
            System.out.println("count with stream: " + wordToCountStream);
        }

        WordCountSync wcs = new WordCountSync();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        pool.execute(() -> wcs.addToCountSyncMethod(files.get(0)));
        pool.execute(() -> wcs.addToCountSyncMethod(files.get(1)));
        pool.execute(() -> wcs.addToCountSyncMethod(files.get(2)));
        pool.execute(() -> wcs.addToCountSyncBlock(files.get(3)));
        pool.execute(() -> wcs.addToCountSyncBlock(files.get(4)));
        pool.execute(() -> wcs.addToCountSyncBlock(files.get(5)));

        pool.shutdown();
        wordToCount = wcs.getWordToCount();
        System.out.println("count with sync blocks and method: " + wordToCount);



    }


    public void countWords(String fileName, Map<String, Integer> wordToCount) {
        InputStream stream = this.getClass().getResourceAsStream(fileName);
        if (stream == null) return;
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNext()) {
            String word = scanner.next();
            if (!wordToCount.containsKey(word)) {
                wordToCount.put(word, 1);
                continue;
            }
            int count = wordToCount.get(word);
            wordToCount.put(word, count + 1);
        }
    }


    public Map<String, Integer> countWithStream(List<String> files) {
        List<String> lines = files.parallelStream().map(f -> {
            URL s = this.getClass().getResource(f);
            if (s == null) return null;
            try {
                return Files.readAllLines(Paths.get(s.toURI()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).flatMap(l -> l.stream()).collect(Collectors.toList());

        return lines.parallelStream().map(line -> line.split(" ")).flatMap(words -> Arrays.stream
                (words)).collect(Collectors.toMap(w -> w, w -> 1, (v1, v2) -> v1 + v2));
    }


}
