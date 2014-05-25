package jp.co.worksap.y2014.orienteering;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Orienteering {
	public static class Point {
		int r;
		int c;
		
		public Point (int r, int c) {
			this.r = r;
			this.c = c;
		}
	}
	
	public static class Map {
		private static char OPEN = '.';
		
		public Point start;
		public Point goal;
		
		public List<Point> waypoints;
		
		public int width;
		public int height;
		public char[][] block;
		
		public Map(int height, int width) {
			this.height = height;
			this.width = width;
			block = new char[height][width];
			waypoints = new ArrayList<Point>();
		}
		
		boolean isOpen(int r, int c) {
			return block[r][c] == OPEN;
		}
		
		boolean inBound(int r, int c) {
			return (0 <= r) && (r < height) && (0 <= c) && (c < width);
		}
	}
	
	public static class SearchParam {
		int n;				// the number of all vertexes
		int dst;			// the destination
		int[][] dist;		// the distance matrix
		int[] minOutEdge;	// the minimum in edge for each vertex
		int best;			// the current best solution
	}
	
	private static SearchParam params = new SearchParam();
	public static final int UNREACHABLE = -1;
	private static final String INPUT_FILE_NAME = "./data/map/g1.txt";
	
	public static void main(String[] args) throws IOException {
		Map m = loadMap();
		long start = System.currentTimeMillis();
		int[][] dist = reduction(m);
		int result = tsp(dist, m.waypoints.size());
		System.out.println(result);
		System.out.println("time = " + (System.currentTimeMillis() - start) + "ms");
	}
    
	private static int tsp(int[][] dist, int n) {
		int[] minOutEdge = new int[n];
		for (int i = 0; i < n; i++) {
			int min = Integer.MAX_VALUE;
			for (int j = 0; j < n; j++) {
				if (i != j && dist[i][j] < min) {
					min = dist[i][j];
				}
			}
			minOutEdge[i] = min;
			// quick judge
			if (min == Integer.MAX_VALUE)
				return UNREACHABLE;
		}
		int[] pending = new int[n - 2];
		for (int i = 2; i < n; i++) {
			pending[i - 2] = i;
		}
		int minsum = 0;
		for (int i = 0; i + 1 < n; i++) {
			// excluding the destination
			minsum += minOutEdge[i];
		}
		params.n = n;
		params.dist = dist;
		params.dst = 1;
		params.minOutEdge = minOutEdge;
		params.best = Integer.MAX_VALUE;
		search(0, 0, minsum, n - 2, pending);
		if (params.best < Integer.MAX_VALUE)
			return params.best;
		else
			return UNREACHABLE;
	}
    
	/**
	 * return the remaining minimal cost
	 * @param now
	 * @param cost
	 * @param minsum
	 * @param left
	 * @param pending, excluding src and dst
	 */
	private static void search(int now, int cost, int minsum, int left, int[] pending) {
		if (minsum + cost < params.best) {
			if (left == 0) {
				if (cost + params.dist[now][params.dst] < params.best) {
					params.best = cost + params.dist[now][params.dst];
				}
			} else {
				for (int i = 0; i < left; i++) {
					int next = pending[i];
					if (params.dist[now][next] < Integer.MAX_VALUE) {
						pending[i] = pending[left - 1];
						search(next, cost + params.dist[now][next], minsum - params.minOutEdge[now], left - 1, pending);
						pending[i] = next;
					}
				}
			}
		}
	}
    
	private static int[][] reduction(Map m) {
		int n = m.waypoints.size();
		int[][] dist = new int[n][n];
		for (int i = 0; i < n; i++) {
			Point p = m.waypoints.get(i);
			int[][] td = calcDistance(m, p);
			for (int j = 0; j < n; j++) {
				Point q = m.waypoints.get(j);
				dist[i][j] = dist[j][i] = td[q.r][q.c];
			}
		}
		return dist;
	}
    
	private static int[][] calcDistance(Map m, Point p) {
		final int DIRS = 4;
		final int[] DR = new int[] {-1, 0, 1, 0};
		final int[] DC = new int[] {0, 1, 0, -1};
        
		int[][] td = new int[m.height][m.width];
		for (int i = 0; i < m.height; i ++) {
			for (int j = 0; j < m.width; j++) {
				td[i][j] = Integer.MAX_VALUE;
			}
		}
		td[p.r][p.c] = 0;
		Queue<Point> pending = new LinkedList<Point>();
		pending.add(p);
        
		while (pending.size() > 0) {
			Point now = pending.poll();
			int base = td[now.r][now.c];
			for (int d = 0; d < DIRS; d++) {
				int nr = now.r + DR[d];
				int nc = now.c + DC[d];
				if (m.inBound(nr, nc) && m.isOpen(nr, nc) && (base + 1 < td[nr][nc])) {
					td[nr][nc] = base + 1;
					pending.add(new Point(nr, nc));
				}
			}
		}
		
		return td;
	}
    
	private static Map loadMap() throws IOException {
		File file = new File(INPUT_FILE_NAME);
		Scanner scanner = new Scanner(new FileInputStream(file));
		int width = scanner.nextInt();
		int height = scanner.nextInt();
		scanner.nextLine();
		Map map = new Map(height, width);
		List<Point> wp = new ArrayList<Point>();
		for (int i = 0; i < height; i ++) {
			String line = scanner.nextLine();
			for (int j = 0; j < width; j ++) {
				char c = line.charAt(j);
				if (c == 'G') {
					map.block[i][j] = Map.OPEN;
					map.goal = new Point(i, j);
				} else if (c == 'S') {
					map.block[i][j] = Map.OPEN;
					map.start = new Point(i, j);
				} else if (c == '@') {
					wp.add(new Point(i, j));
					map.block[i][j] = Map.OPEN;
				} else {
					map.block[i][j] = c;
				}
			}
		}
		map.waypoints.add(map.start);
		map.waypoints.add(map.goal);
		map.waypoints.addAll(wp);
		scanner.close();
		return map;
	}
}
