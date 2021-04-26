package com.aptvantage.twitter.ingestor.fake;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MarkovChain {
    private static Random r = new Random();

    public static String markov(int keySize, int outputSize) {
        try {
            if (keySize < 1) throw new IllegalArgumentException("Key size can't be less than 1");
            URL resource = MarkovChain.class.getClassLoader().getResource("alice_oz.txt");
            Path path = Paths.get(resource.toURI());
            byte[] bytes = Files.readAllBytes(path);
            String[] words = new String(bytes).trim().split(" ");
            if (outputSize < keySize || outputSize >= words.length) {
                throw new IllegalArgumentException("Output size is out of range");
            }
            Map<String, List<String>> dict = new HashMap<>();

            for (int i = 0; i < (words.length - keySize); ++i) {
                StringBuilder key = new StringBuilder(words[i]);
                for (int j = i + 1; j < i + keySize; ++j) {
                    key.append(' ').append(words[j]);
                }
                String value = (i + keySize < words.length) ? words[i + keySize] : "";
                if (!dict.containsKey(key.toString())) {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(value);
                    dict.put(key.toString(), list);
                } else {
                    dict.get(key.toString()).add(value);
                }
            }

            int n = 0;
            int rn = r.nextInt(dict.size());
            String prefix = (String) dict.keySet().toArray()[rn];
            List<String> output = new ArrayList<>(Arrays.asList(prefix.split(" ")));

            while (true) {
                List<String> suffix = dict.get(prefix);
                if (suffix.size() == 1) {
                    if (Objects.equals(suffix.get(0), "")) return output.stream().reduce("", (a, b) -> a + " " + b);
                    output.add(suffix.get(0));
                } else {
                    rn = r.nextInt(suffix.size());
                    output.add(suffix.get(rn));
                }
                if (output.size() >= outputSize)
                    return output.stream().limit(outputSize).reduce("", (a, b) -> a + " " + b);
                n++;
                prefix = output.stream().skip(n).limit(keySize).reduce("", (a, b) -> a + " " + b).trim();
            }
        } catch(Exception e) {
            throw new RuntimeException("could not generator text",e);
        }
    }

}
