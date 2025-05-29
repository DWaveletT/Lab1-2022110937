package cn.wavelet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalcShortestPathWhiteBoxTest {
    private Graph graph;
    private Main main;

    @BeforeEach
    public void setUp() {
        // 构建一个测试用的图
        // 图结构:
        // A -> B (1)
        // A -> C (3)
        // B -> C (1)
        // B -> D (5)
        // C -> D (1)
        graph = new Graph();
        graph.addEdge("A", "B"); // 默认权重1
        graph.addEdge("A", "C"); // 需要增加权重到3
        graph.addEdge("B", "C"); // 默认权重1
        graph.addEdge("B", "D"); // 需要增加权重到5
        graph.addEdge("C", "D"); // 默认权重1
        
        // 调整权重
        graph.getEdges("A").put("C", 3);
        graph.getEdges("B").put("D", 5);
        
        main = new Main();
        Main.graph = graph; // 设置Main的静态graph变量
    }

    @Test
    public void testPathExists() {
        // 最短路径: A -> B -> C -> D (总权重3)
        String result = main.calcShortestPath("A", "D");
        assertTrue(result.contains("A -> B -> C -> D") && result.contains("Length: 3"),
                "Should find the shortest path with correct length");
    }

    @Test
    public void testNoPathExists() {
        // D没有出边，无法到达其他节点
        String result = main.calcShortestPath("D", "A");
        assertEquals("No path exists between \"D\" and \"A\"", result);
    }

    @Test
    public void testStartNodeNotInGraph() {
        String result = main.calcShortestPath("X", "A");
        assertEquals("No path exists between \"X\" and \"A\"", result);
    }

    @Test
    public void testEndNodeNotInGraph() {
        String result = main.calcShortestPath("A", "X");
        assertEquals("No path exists between \"A\" and \"X\"", result);
    }

    @Test
    public void testBothNodesNotInGraph() {
        String result = main.calcShortestPath("X", "Y");
        assertEquals("No path exists between \"X\" and \"Y\"", result);
    }

    @Test
    public void testSameStartAndEndNode() {
        // 添加自环边
        graph.addEdge("A", "A");
        String result = main.calcShortestPath("A", "A");
        assertTrue(result.contains("A") && result.contains("Length: 0"),
                "Should handle self-loop correctly");
    }

    @Test
    public void testMultipleShortestPaths() {
        // 添加另一条最短路径 A -> C -> D (总权重也是3)
        graph.getEdges("A").put("C", 2); // 修改A->C的权重为2
        String result = main.calcShortestPath("A", "D");
        // 可能返回A->B->C->D或A->C->D，两者都是正确的
        assertTrue(result.contains("Length: 3"),
                "Should find one of the shortest paths with correct length");
    }

    @Test
    public void testDisconnectedGraph() {
        // 添加一个孤立的节点E
        graph.addEdge("E", "E"); // 自环，但不连接到其他节点
        String result = main.calcShortestPath("A", "E");
        assertEquals("No path exists between \"A\" and \"E\"", result);
    }

    @Test
    public void testEmptyGraph() {
        Graph emptyGraph = new Graph();
        Main.graph = emptyGraph;
        String result = main.calcShortestPath("A", "B");
        assertEquals("No path exists between \"A\" and \"B\"", result);
    }
}
