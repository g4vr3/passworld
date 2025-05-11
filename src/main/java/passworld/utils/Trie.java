package passworld.utils;

import java.util.HashMap;
import java.util.Map;

public class Trie {

    private final TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    // Insertar una palabra en el Trie
    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
    }

    // Verificar si una palabra está en el Trie
    public boolean contains(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return false;
            }
            current = current.children.get(c);
        }
        return current.isEndOfWord;
    }

    // Nodo interno del Trie
    private static class TrieNode {
        private final Map<Character, TrieNode> children = new HashMap<>();
        private boolean isEndOfWord = false;
    }
}