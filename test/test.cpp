#include <iostream>
#include <vector>
#include <unordered_map>
#include <iomanip>
#include <cmath>

using namespace std;

const double DAMPING_FACTOR = 0.85;
const int MAX_ITER = 100;
const double EPSILON = 1e-6;

struct Edge {
    int to;
    double weight;
};

class Graph {
private:
    int n; // number of nodes
    vector<vector<Edge>> adj; // adjacency list
    vector<double> pagerank;

public:
    Graph(int nodes) : n(nodes), adj(nodes), pagerank(nodes, 1.0 / nodes) {}

    void addEdge(int from, int to, double weight) {
        adj[from].push_back({to, weight});
    }

    void computePageRank() {
        vector<double> newPR(n, 0.0);
        for (int iter = 0; iter < MAX_ITER; ++iter) {
            // Reset newPR
            fill(newPR.begin(), newPR.end(), 0.0);

            // Distribute PageRank
            for (int u = 0; u < n; ++u) {
                double sumWeight = 0.0;
                for (const auto& edge : adj[u]) {
                    sumWeight += edge.weight;
                }
                if (sumWeight == 0) continue;

                for (const auto& edge : adj[u]) {
                    newPR[edge.to] += pagerank[u] * (edge.weight / sumWeight);
                }
            }

            // Apply damping factor
            for (int i = 0; i < n; ++i) {
                newPR[i] = (1.0 - DAMPING_FACTOR) / n + DAMPING_FACTOR * newPR[i];
            }

            // Check convergence
            double diff = 0.0;
            for (int i = 0; i < n; ++i) {
                diff += fabs(newPR[i] - pagerank[i]);
            }

            pagerank = newPR;

            if (diff < EPSILON) {
                cout << "Converged after " << iter + 1 << " iterations.\n";
                break;
            }
        }
    }

    void printPageRank() const {
        cout << fixed << setprecision(6);
        for (int i = 0; i < n; ++i) {
            cout << "Node " << i << ": " << pagerank[i] << endl;
        }
    }
};

// 示例用法
int main() {
    int nodes = 10;
    Graph g(nodes);

    // 添加边（带权）
    g.addEdge(0, 1, 1.0);
    g.addEdge(0, 2, 1.0);
    g.addEdge(1, 3, 1.0);
    g.addEdge(2, 4, 1.0);
    g.addEdge(3, 5, 1.0);
    g.addEdge(4, 5, 1.0);
    g.addEdge(5, 6, 1.0);
    g.addEdge(5, 7, 1.0);
    g.addEdge(5, 8, 1.0);
    g.addEdge(6, 9, 1.0);
    g.addEdge(9, 5, 1.0);
    g.addEdge(8, 0, 1.0);

    g.computePageRank();
    g.printPageRank();

    return 0;
}
