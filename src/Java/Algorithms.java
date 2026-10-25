import java.util.*;

public final class Algorithms {

    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }

        int components = n;
        int largest = n > 0 ? 1 : 0;
        List<List<Integer>> results = new ArrayList<>();

        for (int[] op : operations) {
            int u = op[0];
            int v = op[1];

            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU != rootV) {
                if (size[rootU] < size[rootV]) {
                    int temp = rootU; rootU = rootV; rootV = temp;
                }
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
                components--;
                if (size[rootU] > largest) {
                    largest = size[rootU];
                }
            }
            
            List<Integer> state = new ArrayList<>();
            state.add(components);
            state.add(largest);
            results.add(state);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("states", results);
        return result;
    }

    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<int[]> edgeList = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            edgeList.add(new int[]{edges[i][0], edges[i][1], edges[i][2], i});
        }
        edgeList.sort(Comparator.comparingInt(e -> e[2]));

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }

        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = n;

        for (int[] e : edgeList) {
            int u = e[0], v = e[1], w = e[2], id = e[3];
            int rootU = find(parent, u);
            int rootV = find(parent, v);

            if (rootU != rootV) {
                if (size[rootU] < size[rootV]) {
                    int temp = rootU; rootU = rootV; rootV = temp;
                }
                parent[rootV] = rootU;
                size[rootU] += size[rootV];
                totalCost += w;
                edgeIds.add(id);
                components--;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cost", totalCost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int i = 0; i < edges.length; i++) {
            int u = edges[i][0], v = edges[i][1], w = edges[i][2];
            adj.get(u).add(new int[]{v, w, i});
            adj.get(v).add(new int[]{u, w, i});
        }

        boolean[] visited = new boolean[n];
        long totalCost = 0;
        List<Integer> edgeIds = new ArrayList<>();
        int components = 0;

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                components++;
                pq.add(new int[]{0, i, -1});

                while (!pq.isEmpty()) {
                    int[] curr = pq.poll();
                    int w = curr[0], u = curr[1], id = curr[2];

                    if (visited[u]) continue;

                    visited[u] = true;
                    if (id != -1) {
                        totalCost += w;
                        edgeIds.add(id);
                    }

                    for (int[] neighbor : adj.get(u)) {
                        int v = neighbor[0], weight = neighbor[1], edgeId = neighbor[2];
                        if (!visited[v]) {
                            pq.add(new int[]{weight, v, edgeId});
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("cost", totalCost);
        result.put("edge_ids", edgeIds);
        result.put("components", components);
        return result;
    }

    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();
        List<List<Integer>> revAdj = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            adj.add(new ArrayList<>());
            revAdj.add(new ArrayList<>());
        }
        for (int[] e : edges) {
            adj.get(e[0]).add(e[1]);
            revAdj.get(e[1]).add(e[0]);
        }

        boolean[] visited = new boolean[n];
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) dfsScc(i, adj, visited, stack);
        }

        Arrays.fill(visited, false);
        int[] comp = new int[n];
        Arrays.fill(comp, -1);
        int compId = 0;

        while (!stack.isEmpty()) {
            int v = stack.pop();
            if (!visited[v]) {
                dfsSccRev(v, revAdj, visited, comp, compId);
                compId++;
            }
        }

        List<List<Integer>> components = new ArrayList<>();
        for (int i = 0; i < compId; i++) components.add(new ArrayList<>());
        for (int i = 0; i < n; i++) components.get(comp[i]).add(i);

        Set<String> pairSet = new HashSet<>();
        List<List<Integer>> pairs = new ArrayList<>();
        for (int[] e : edges) {
            int u = e[0], v = e[1];
            if (comp[u] != comp[v]) {
                String key = comp[u] + "," + comp[v];
                if (pairSet.add(key)) {
                    pairs.add(Arrays.asList(comp[u], comp[v]));
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("components", components);
        result.put("condensation", pairs);
        return result;
    }

    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int[] e : edges) adj.get(e[0]).add(e[1]);

        int[] state = new int[n];
        List<Integer> order = new ArrayList<>();
        boolean acyclic = true;

        for (int i = 0; i < n; i++) {
            if (state[i] == 0) {
                if (!dfsTopo(i, adj, state, order)) {
                    acyclic = false;
                    break;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("acyclic", acyclic);
        if (acyclic) {
            Collections.reverse(order);
            result.put("order", order);
        } else {
            result.put("order", new ArrayList<>());
        }
        return result;
    }

    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        List<List<int[]>> adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
        for (int i = 0; i < edges.length; i++) {
            adj.get(edges[i][0]).add(new int[]{edges[i][1], edges[i][2], i});
        }

        int[] state = new int[n];
        List<Integer> order = new ArrayList<>();
        boolean acyclic = true;

        for (int i = 0; i < n; i++) {
            if (state[i] == 0) {
                if (!dfsTopoWeighted(i, adj, state, order)) {
                    acyclic = false;
                    break;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("acyclic", acyclic);

        if (!acyclic) {
            result.put("distance", new ArrayList<>());
            result.put("parent_edge", new ArrayList<>());
            return result;
        }

        Collections.reverse(order);

        Long[] dist = new Long[n];
        int[] parentEdge = new int[n];
        Arrays.fill(dist, null);
        Arrays.fill(parentEdge, -1);
        dist[source] = 0L;

        for (int u : order) {
            if (dist[u] == null) continue;
            for (int[] edgeInfo : adj.get(u)) {
                int v = edgeInfo[0];
                int w = edgeInfo[1];
                int id = edgeInfo[2];
                if (dist[v] == null || dist[u] + w < dist[v]) {
                    dist[v] = dist[u] + w;
                    parentEdge[v] = id;
                }
            }
        }

        result.put("distance", Arrays.asList(dist));
        List<Integer> peList = new ArrayList<>();
        for (int x : parentEdge) peList.add(x);
        result.put("parent_edge", peList);
        return result;
    }

    private static int find(int[] parent, int x) {
        if (parent[x] != x) parent[x] = find(parent, parent[x]);
        return parent[x];
    }

    private static void dfsScc(int v, List<List<Integer>> adj, boolean[] visited, Stack<Integer> stack) {
        visited[v] = true;
        for (int u : adj.get(v)) {
            if (!visited[u]) dfsScc(u, adj, visited, stack);
        }
        stack.push(v);
    }

    private static void dfsSccRev(int v, List<List<Integer>> revAdj, boolean[] visited, int[] comp, int compId) {
        visited[v] = true;
        comp[v] = compId;
        for (int u : revAdj.get(v)) {
            if (!visited[u]) dfsSccRev(u, revAdj, visited, comp, compId);
        }
    }

    private static boolean dfsTopo(int v, List<List<Integer>> adj, int[] state, List<Integer> order) {
        state[v] = 1;
        for (int u : adj.get(v)) {
            if (state[u] == 1) return false;
            if (state[u] == 0) {
                if (!dfsTopo(u, adj, state, order)) return false;
            }
        }
        state[v] = 2;
        order.add(v);
        return true;
    }

    private static boolean dfsTopoWeighted(int v, List<List<int[]>> adj, int[] state, List<Integer> order) {
        state[v] = 1;
        for (int[] edge : adj.get(v)) {
            int u = edge[0];
            if (state[u] == 1) return false;
            if (state[u] == 0) {
                if (!dfsTopoWeighted(u, adj, state, order)) return false;
            }
        }
        state[v] = 2;
        order.add(v);
        return true;
    }
}
