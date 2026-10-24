package com.civicpulse.util;

import java.util.*;

public final class TextSimilarity {

    private TextSimilarity() {}

    /**
     * Computes similarity between 0.0 (completely distinct) and 1.0 (identical)
     * using combined token Jaccard and bigram similarity.
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        String norm1 = s1.trim().toLowerCase();
        String norm2 = s2.trim().toLowerCase();
        if (norm1.equals(norm2)) return 1.0;
        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0;

        double jaccard = tokenJaccard(norm1, norm2);
        double bigram = bigramSimilarity(norm1, norm2);

        return (jaccard * 0.6) + (bigram * 0.4);
    }

    public static double tokenJaccard(String s1, String s2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(s1.split("\\W+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(s2.split("\\W+")));

        words1.remove("");
        words2.remove("");

        if (words1.isEmpty() || words2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return (double) intersection.size() / union.size();
    }

    public static double bigramSimilarity(String s1, String s2) {
        List<String> pairs1 = wordLetterPairs(s1);
        List<String> pairs2 = wordLetterPairs(s2);

        if (pairs1.isEmpty() || pairs2.isEmpty()) return 0.0;

        int intersection = 0;
        List<String> tempPairs2 = new ArrayList<>(pairs2);

        for (String p1 : pairs1) {
            for (int i = 0; i < tempPairs2.size(); i++) {
                if (p1.equals(tempPairs2.get(i))) {
                    intersection++;
                    tempPairs2.remove(i);
                    break;
                }
            }
        }

        return (2.0 * intersection) / (pairs1.size() + pairs2.size());
    }

    private static List<String> wordLetterPairs(String str) {
        List<String> allPairs = new ArrayList<>();
        String[] words = str.split("\\s+");
        for (String w : words) {
            for (int i = 0; i < w.length() - 1; i++) {
                allPairs.add(w.substring(i, i + 2));
            }
        }
        return allPairs;
    }
}
