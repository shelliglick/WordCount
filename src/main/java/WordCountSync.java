import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class WordCountSync {

    private Map<String, Integer> wordToCount;

    public WordCountSync() {
        wordToCount = new HashMap<>();
    }

    public void addToCountSyncBlock(String file) {
        InputStream stream = this.getClass().getResourceAsStream(file);
        if (stream == null) return;
        Scanner scanner = new Scanner(stream);
        while (scanner.hasNext()) {
            String word = scanner.next();
            synchronized (wordToCount) {
                if (!wordToCount.containsKey(word)) {
                    wordToCount.put(word, 1);
                    continue;
                }
                int count = wordToCount.get(word);
                wordToCount.put(word, count + 1);
            }
        }
    }

    public synchronized void addToCountSyncMethod(String file) {
        InputStream stream = this.getClass().getResourceAsStream(file);
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

    public Map<String, Integer> getWordToCount() {
        return wordToCount;
    }
}
