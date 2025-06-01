package cn.wavelet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QueryBridgeWordsBlackBoxTest {
    private Graph graph;

    @BeforeEach
    public void setUp() {
        // 构建一个测试用的图
        // 图结构:
        // cat -> apple -> banana
        // cat -> dog -> banana
        // apple -> dog
        // banana -> cat
        graph = new Graph();
        graph.addEdge("cat", "apple");
        graph.addEdge("apple", "banana");
        graph.addEdge("cat", "dog");
        graph.addEdge("dog", "banana");
        graph.addEdge("apple", "dog");
        graph.addEdge("banana", "cat");
        
        Main.graph = graph; // 设置Main的静态graph变量
    }

    @Test
    public void testBothWordsExistWithBridgeWords() {
        // cat -> apple -> banana
        // cat -> dog -> banana
        // 所以从cat到banana的桥接词是apple和dog
        String result = Main.queryBridgeWords("cat", "banana");
        assertTrue(result.contains("apple") && result.contains("dog"),
                "Should find both bridge words");
    }

    @Test
    public void testBothWordsExistNoBridgeWords() {
        // 没有从 cat 到 apple 的桥接词
        String result = Main.queryBridgeWords("cat", "apple");
        assertEquals("No bridge words from \"cat\" to \"apple\"!", result);
    }

    @Test
    public void testWord1NotInGraph() {
        String result = Main.queryBridgeWords("elephant", "cat");
        assertEquals("No \"elephant\" in the graph!", result);
    }

    @Test
    public void testWord2NotInGraph() {
        String result = Main.queryBridgeWords("cat", "elephant");
        assertEquals("No \"elephant\" in the graph!", result);
    }

    @Test
    public void testBothWordsNotInGraph() {
        String result = Main.queryBridgeWords("elephant", "zebra");
        assertEquals("No \"elephant\" and \"zebra\" in the graph!", result);
    }

    @Test
    public void testSingleBridgeWord() {
        // apple -> dog -> banana
        String result = Main.queryBridgeWords("apple", "banana");
        assertEquals("The bridge word from \"apple\" to \"banana\" is: \"dog\"", result);
    }

    @Test
    public void testEmptyGraph() {
        Graph emptyGraph = new Graph();
        Main.graph = emptyGraph;
        String result = Main.queryBridgeWords("cat", "dog");
        assertEquals("No \"cat\" and \"dog\" in the graph!", result);
    }

    @Test
    public void testSameWord() {
        // 自环情况
        String result = Main.queryBridgeWords("cat", "cat");
        assertEquals("No bridge words from \"cat\" to \"cat\"!", result);
    }
}