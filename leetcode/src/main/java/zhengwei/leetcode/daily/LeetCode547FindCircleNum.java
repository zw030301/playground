package zhengwei.leetcode.daily;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 547. 省份数量
 *
 * @author zhengwei AKA zenv
 * @since 2021/1/7 19:05
 */
public class LeetCode547FindCircleNum {
    public int findCircleNumDFS(int[][] isConnected) {
        //城市数量
        final int n = isConnected.length;
        int result = 0;
        boolean[] isVisited = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!isVisited[i]) {
                dfs(isVisited, isConnected, i);
                result++;
            }
        }
        return result;
    }

    private void dfs(boolean[] isVisited, int[][] isConnected, int i) {
        for (int j = 0; j < isConnected.length; j++) {
            if (isConnected[i][j] == 1 && !isVisited[j]) {
                isVisited[j] = true;
                dfs(isVisited, isConnected, j);
            }
        }
    }

    public int findCircleNumBFS(int[][] isConnected) {
        int result = 0;
        final int n = isConnected.length;
        boolean[] isVisited = new boolean[n];
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (isVisited[i]) continue;
            result++;
            queue.add(i);
            while (!queue.isEmpty()) {
                final Integer x = queue.poll();
                isVisited[x] = true;
                for (int y = 0; y < n; y++) {
                    if (isConnected[x][y] == 1 && !isVisited[y]) {
                        queue.add(y);
                    }
                }
            }
        }
        return result;
    }

    public int findCircleNum(int[][] isConnected) {
        final int provinceLength = isConnected.length;
        int[] parent = new int[provinceLength];
        int result = 0;
        for (int i = 0; i < provinceLength; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < provinceLength; i++) {
            for (int j = i + 1; j < provinceLength; j++) {
                if (isConnected[i][j] == 1) {
                    union(parent, i, j);
                }
            }
        }
        for (int i = 0; i < provinceLength; i++) {
            if (find(parent, i) == i) {
                result++;
            }
        }
        return result;
    }

    private void union(int[] parent, int i1, int i2) {
        final int a = find(parent, i1);
        final int b = find(parent, i2);
        parent[a] = b;
    }

    private int find(int[] parent, int i) {
        if (parent[i] != i) {
            parent[i] = find(parent, parent[i]);
        }
        return parent[i];
    }
}
