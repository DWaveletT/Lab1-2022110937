package cn.wavelet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalcShortestPathWhiteBoxTest {
    private Graph graph;

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
        
        Main.graph = graph; // 设置Main的静态graph变量
    }

    @Test
    public void testPathExists() {
        // 最短路径: A -> B -> C -> D (总权重3)
        String result = Main.calcShortestPath("A", "D");
        assertTrue(result.contains("A -> B -> C -> D") && result.contains("Length: 3"),
                "Should find the shortest path with correct length");
    }

    @Test
    public void testNoPathExists() {
        // D没有出边，无法到达其他节点
        String result = Main.calcShortestPath("D", "A");
        assertEquals("No path exists between \"D\" and \"A\"", result);
    }

    @Test
    public void testStartNodeNotInGraph() {
        String result = Main.calcShortestPath("X", "A");
        assertEquals("No path exists between \"X\" and \"A\"", result);
    }
}
