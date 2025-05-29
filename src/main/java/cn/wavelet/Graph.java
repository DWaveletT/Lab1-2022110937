package cn.wavelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph {
    protected Map<String, Map<String, Integer>> adjacencyList;
    protected Map<String, List<String>> inEdges;

    private static final Random RANDOM = new Random();

    public Graph() {
        adjacencyList = new HashMap<>();
        inEdges = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        adjacencyList.putIfAbsent(from, new HashMap<>());
        adjacencyList.get(from).put(to, adjacencyList.get(from).getOrDefault(to, 0) + 1);

        inEdges.putIfAbsent(to, new ArrayList<>());
        inEdges.get(to).add(from);
        adjacencyList.putIfAbsent(to, new HashMap<>());
    }

    public static Graph buildGraph(List<String> words) {
        Graph graph = new Graph();
        if (words.size() < 2) return graph;

        for (int i = 0; i < words.size() - 1; i++) {
            String from = words.get(i);
            String to = words.get(i + 1);
            graph.addEdge(from, to);
        }
        return graph;
    }

    public Set<String> getNodes() {
        return adjacencyList.keySet();
    }

    public Map<String, Integer> getEdges(String node) {
        return adjacencyList.getOrDefault(node, Collections.emptyMap());
    }

    public List<String> findBridgeWords(String word1, String word2) {
        if (!adjacencyList.containsKey(word1) || !adjacencyList.containsKey(word2)) return null;

        List<String> bridges = new ArrayList<>();
        Map<String, Integer> edgesFromWord1 = adjacencyList.get(word1);
        for (String candidate : edgesFromWord1.keySet()) {
            if (adjacencyList.get(candidate).containsKey(word2)) {
                bridges.add(candidate);
            }
        }
        return bridges;
    }

    public List<String> calcShortestPath(String start, String end) {
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) return null;

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

        adjacencyList.keySet().forEach(node -> distances.put(node, Integer.MAX_VALUE));
        distances.put(start, 0);
        queue.add(new NodeDistance(start, 0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (current.distance > distances.get(current.node)) continue;

            adjacencyList.get(current.node).forEach((neighbor, weight) -> {
                int newDist = current.distance + weight;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    predecessors.put(neighbor, current.node);
                    queue.add(new NodeDistance(neighbor, newDist));
                }
            });
        }

        if (distances.get(end) == Integer.MAX_VALUE) return null;

        LinkedList<String> path = new LinkedList<>();
        String current = end;
        while (current != null) {
            path.addFirst(current);
            current = predecessors.get(current);
        }
        return path.getFirst().equals(start) ? path : null;
    }

    public Map<String, Double> calculatePageRank(int iterations) {
        int N = adjacencyList.size();
        if (N == 0) return new HashMap<>();

        // 使用数组包装来绕过 final 限制
        Map<String, Double>[] prWrapper = new Map[]{new HashMap<>()};
        double initial = 1.0 / N;
        adjacencyList.keySet().forEach(node -> prWrapper[0].put(node, initial));

        // 预处理：找出所有无出度的节点（dangling nodes）
        Set<String> danglingNodes = adjacencyList.entrySet().stream()
                .filter(entry -> entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        for (int i = 0; i < iterations; i++) {
            Map<String, Double> newPr = new HashMap<>();
            
            // 计算dangling nodes的总PR值
            final double danglingSum = danglingNodes.stream()
                    .mapToDouble(node -> prWrapper[0].get(node))
                    .sum();

            // 计算每个节点的新PR值
            adjacencyList.keySet().forEach(node -> {
                // 来自正常链接的部分
                double sum = inEdges.getOrDefault(node, Collections.emptyList()).stream()
                        .mapToDouble(inNode -> {
                            int outDegree = adjacencyList.get(inNode).size();
                            return outDegree > 0 ? prWrapper[0].get(inNode) / outDegree : 0;
                        })
                        .sum();

                // 加上来自dangling nodes的部分（均分给所有节点）
                sum += danglingSum / N;

                // 加入随机跳转因子
                newPr.put(node, (1 - 0.85) / N + 0.85 * sum);
            });
            prWrapper[0] = newPr;
        }
        return prWrapper[0];
    }

    public String randomWalk() {
        List<String> nodes = new ArrayList<>(adjacencyList.keySet());
        if (nodes.isEmpty()) return "";

        String current = nodes.get(RANDOM.nextInt(nodes.size()));
        List<String> path = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        path.add(current);

        while (true) {
            Map<String, Integer> edges = adjacencyList.get(current);
            if (edges == null || edges.isEmpty()) break;

            List<String> candidates = new ArrayList<>(edges.keySet());
            String next = candidates.get(RANDOM.nextInt(candidates.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) break;
            visitedEdges.add(edge);

            path.add(next);
            current = next;
        }
        return String.join(" ", path);
    }

    private static class NodeDistance implements Comparable<NodeDistance> {
        String node;

        int distance;

        NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Integer.compare(this.distance, other.distance);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeDistance that = (NodeDistance) o;
            return distance == that.distance && node.equals(that.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, distance);
        }
    }
}