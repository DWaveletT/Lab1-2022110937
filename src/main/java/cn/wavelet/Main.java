package cn.wavelet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import static guru.nidi.graphviz.model.Factory.*;

public class Main {
  private static Graph graph;
  
  static Random RANDOM = new Random();

  public static void main(String[] args) {
    if (args.length == 0) {
      System.err.println("Error: Please provide an input file as command line argument");
      System.err.println("Usage: java Main <input_file_path>");
      System.exit(1);
    }

    String inputFile = args[0];
    if (!Files.exists(Paths.get(inputFile))) {
      System.err.println("Error: Input file '" + inputFile + "' does not exist");
      System.exit(1);
    }

    try {
      System.out.println("Processing input file: " + inputFile);
      List<String> words = TextProcessor.processText(inputFile);
      if (words.isEmpty()) {
        System.err.println("Warning: Input file is empty or contains no valid words");
      }
      graph = Graph.buildGraph(words);

      Scanner scanner = new Scanner(System.in, "UTF8");
      while (true) {
        System.out.println("\nMenu Options:");
        System.out.println("1. Show Directed Graph");
        System.out.println("2. Query Bridge Words");
        System.out.println("3. Generate New Text");
        System.out.println("4. Calculate Shortest Path");
        System.out.println("5. Calculate PageRank");
        System.out.println("6. Random Walk");
        System.out.println("0. Exit");
        System.out.print("Please choose an option: ");
        
        try {
          int choice = scanner.nextInt();
          scanner.nextLine();

          switch (choice) {
            case 1 -> showDirectedGraph(graph);
            case 2 -> {
              System.out.print("Enter two words (space separated): ");
              String[] input = scanner.nextLine().trim().toLowerCase().split("\\s+");
              if (input.length != 2) {
                System.out.println("Invalid input! Please enter exactly two words.");
                break;
              }
              System.out.println(queryBridgeWords(input[0], input[1]));
            }
            case 3 -> {
              System.out.print("Enter new text: ");
              String newText = generateNewText(scanner.nextLine());
              System.out.println("Generated text: " + newText);
            }
            case 4 -> {
              System.out.print("Enter two words (space separated): ");
              String[] pathInput = scanner.nextLine().trim().toLowerCase().split("\\s+");
              if (pathInput.length != 2) {
                System.out.println("Invalid input! Please enter exactly two words.");
                break;
              }
              System.out.println(calcShortestPath(pathInput[0], pathInput[1]));
            }
            case 5 -> {
              System.out.print("Enter a word: ");
              String word = scanner.nextLine().trim().toLowerCase();
              System.out.println("PageRank value: " + calcPageRank(word));
            }
            case 6 -> {
              String walk = randomWalk();
              System.out.println("Random walk path: " + walk);
              try {
                Files.write(Paths.get("random_walk.txt"), walk.getBytes("UTF8"));
                System.out.println("Random walk saved to random_walk.txt");
              } catch (IOException e) {
                System.err.println("Failed to save random walk: " + e.getMessage());
              }
            }
            case 0 -> {
              scanner.close();
              System.out.println("Exiting program...");
              System.exit(0);
            }
            default -> System.out.println("Invalid option");
          }
        } catch (InputMismatchException e) {
          System.out.println("Invalid input! Please enter a number.");
          scanner.nextLine(); // clear the invalid input
        }
      }
    } catch (IOException e) {
      System.err.println("Error processing input file: " + e.getMessage());
      System.exit(1);
    }
  }

  public static void showDirectedGraph(Object G, String... args) {
    if (G instanceof Graph g) {
      System.out.println("Directed Graph Structure:");
      
      // 控制台输出保持不变
      g.getNodes().forEach(node -> {
        String edges = g.getEdges(node).entrySet().stream()
            .map(e -> e.getKey() + "(" + e.getValue() + ")")
            .collect(Collectors.joining(", "));
        System.out.println(node + " -> " + (edges.isEmpty() ? "no outgoing edges" : edges));
      });

      // 新增 Graphviz 可视化
      try {
        MutableGraph vizGraph = mutGraph("word_graph").setDirected(true);
        
        // 创建所有节点
        Map<String, MutableNode> nodes = new HashMap<>();
        g.getNodes().forEach(word -> {
          nodes.put(word, mutNode(word));
        });
        
        // 添加所有边
        g.getNodes().forEach(from -> {
          g.getEdges(from).forEach((to, weight) -> {
            nodes.get(from).addLink(to(nodes.get(to)).with("label", String.valueOf(weight)));
          });
        });
        
        // 添加节点到图
        nodes.values().forEach(vizGraph::add);
        
        // 生成图片
        Graphviz.fromGraph(vizGraph)
          .width(1000)
          .render(Format.PNG)
          .toFile(new File("word_graph.png"));
        
        System.out.println("\nGraph visualization saved to word_graph.png");
        
        // 尝试自动打开图片
        try {
          if (System.getProperty("os.name").toLowerCase().contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", "word_graph.png").start();
          } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            new ProcessBuilder("open", "word_graph.png").start();
          } else {
            new ProcessBuilder("xdg-open", "word_graph.png").start();
          }
        } catch (IOException e) {
          System.out.println("Could not open image automatically. Please open word_graph.png manually.");
        }
      } catch (Exception e) {
        System.err.println("Failed to generate graph visualization: " + e.getMessage());
      }
    }
  }

  public static String queryBridgeWords(String word1, String word2) {
    boolean hasWord1 = graph.getNodes().contains(word1);
    boolean hasWord2 = graph.getNodes().contains(word2);
    
    if (!hasWord1 && !hasWord2) {
      return "No \"" + word1 + "\" and \"" + word2 + "\" in the graph!";
    }
    if (!hasWord1) {
      return "No \"" + word1 + "\" in the graph!";
    }
    if (!hasWord2) {
      return "No \"" + word2 + "\" in the graph!";
    }

    List<String> bridges = graph.findBridgeWords(word1, word2);
    if (bridges == null || bridges.isEmpty()) {
      return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
    }
    
    if (bridges.size() == 1) {
      return "The bridge word from \"" + word1 + "\" to \"" + word2 + "\" is: \"" + bridges.get(0) + "\"";
    }
    
    return "The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: \"" +
        String.join(", \"", bridges.subList(0, bridges.size() - 1)) +
        "\" and \"" + bridges.get(bridges.size() - 1) + "\"";
  }

  public static String generateNewText(String inputText) {
    List<String> words = Arrays.stream(inputText.replaceAll("[^a-zA-Z\\s]", " ")
        .toLowerCase().split("\\s+"))
        .filter(s -> !s.isEmpty())
        .toList();
    List<String> result = new ArrayList<>();

    for (int i = 0; i < words.size(); i++) {
      result.add(words.get(i));
      if (i < words.size() - 1) {
        List<String> bridges = graph.findBridgeWords(words.get(i), words.get(i + 1));
        if (bridges != null && !bridges.isEmpty()) {
          result.add(bridges.get(RANDOM.nextInt(bridges.size())));
        }
      }
    }
    return String.join(" ", result);
  }

  public static String calcShortestPath(String word1, String word2) {
    List<String> path = graph.calcShortestPath(word1, word2);
    if (path == null) return "No path exists between \"" + word1 + "\" and \"" + word2 + "\"";
    int length = 0;
    for (int i = 0; i < path.size() - 1; i++) {
      length += graph.getEdges(path.get(i)).get(path.get(i + 1));
    }
    return "Path: " + String.join(" -> ", path) + "\nLength: " + length;
  }

  public static Double calcPageRank(String word) {
    return graph.calculatePageRank(100).getOrDefault(word, 0.0);
  }

  public static String randomWalk() {
    return graph.randomWalk();
  }
}