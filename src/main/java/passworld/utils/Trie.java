package passworld.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode("");
    }

    // Normalizar palabras (minúsculas y quitar tildes)
    private static String normalize(String input) {
        if (input == null) return null;
        String lower = input.toLowerCase();
        String normalized = Normalizer.normalize(lower, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized;
    }

    // Insertar palabra normalizada en trie comprimido
    public void insert(String word) {
        word = normalize(word);
        insert(root, word);
    }

    private void insert(TrieNode node, String word) {
        int commonPrefixLen = commonPrefixLength(word, node.segment);

        if (commonPrefixLen < node.segment.length()) {
            // Dividir el nodo actual
            String nodeSuffix = node.segment.substring(commonPrefixLen);
            TrieNode child = new TrieNode(nodeSuffix, node.children, node.isEndOfWord);

            // Actualizar nodo actual
            node.segment = node.segment.substring(0, commonPrefixLen);
            node.children = new ArrayList<>();
            node.children.add(child);
            node.isEndOfWord = false;
        }

        if (commonPrefixLen < word.length()) {
            String wordSuffix = word.substring(commonPrefixLen);

            TrieNode child = findChildWithPrefix(node, wordSuffix.charAt(0));

            if (child != null) {
                insert(child, wordSuffix);
            } else {
                // Añadir nuevo hijo con el resto de la palabra
                node.children.add(new TrieNode(wordSuffix, true));
            }
        } else {
            // La palabra es igual al segmento del nodo
            node.isEndOfWord = true;
        }
    }

    // Buscar si la palabra existe normalizando antes
    public boolean contains(String word) {
        word = normalize(word);
        return contains(root, word);
    }

    private boolean contains(TrieNode node, String word) {
        int commonPrefixLen = commonPrefixLength(word, node.segment);

        if (commonPrefixLen == node.segment.length()) {
            if (commonPrefixLen == word.length()) {
                return node.isEndOfWord;
            }
            String wordSuffix = word.substring(commonPrefixLen);
            TrieNode child = findChildWithPrefix(node, wordSuffix.charAt(0));
            if (child == null) return false;
            return contains(child, wordSuffix);
        } else {
            return false;
        }
    }

    private TrieNode findChildWithPrefix(TrieNode node, char c) {
        if (node.children == null) return null;
        for (TrieNode child : node.children) {
            if (child.segment.charAt(0) == c) {
                return child;
            }
        }
        return null;
    }

    private int commonPrefixLength(String s1, String s2) {
        int len = Math.min(s1.length(), s2.length());
        for (int i = 0; i < len; i++) {
            if (s1.charAt(i) != s2.charAt(i)) return i;
        }
        return len;
    }

    private static class TrieNode {
        String segment;
        List<TrieNode> children;
        boolean isEndOfWord;

        TrieNode(String segment) {
            this.segment = segment;
            this.children = new ArrayList<>();
            this.isEndOfWord = false;
        }

        TrieNode(String segment, boolean isEndOfWord) {
            this.segment = segment;
            this.children = new ArrayList<>();
            this.isEndOfWord = isEndOfWord;
        }

        TrieNode(String segment, List<TrieNode> children, boolean isEndOfWord) {
            this.segment = segment;
            this.children = children;
            this.isEndOfWord = isEndOfWord;
        }
    }
}
