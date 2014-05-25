package wap;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;

public class MapCreator {

	public static final String MAP_FILE_NAME = "./data/map/g2.txt";
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;
	public static final int WAY_POINTS = 18;
	public static final double CLOSE_RATIO = 0.2;
	private static final Random rand = new Random();
	
	public static void main(String[] args) throws IOException {
		char[][] map = makeMap();
		PrintStream ps = new PrintStream(new FileOutputStream(MAP_FILE_NAME));
		ps.println(WIDTH + " " + HEIGHT);
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				ps.print(map[i][j]);
			}
			ps.println();
		}
		ps.close();
	}

	private static char[][] makeMap() {
		char[][] map = new char[HEIGHT][WIDTH];
		for (int i = 0; i < HEIGHT; i++) {
			for (int j = 0; j < WIDTH; j++) {
				if (rand.nextDouble() < CLOSE_RATIO) {
					map[i][j] = '#';
				} else {
					map[i][j] = '.';
				}
			}
		}
		int r, c;
		for (int i = 0; i < WAY_POINTS; i++) {
			do {
				r = rand.nextInt(HEIGHT);
				c = rand.nextInt(WIDTH);
			} while (map[r][c] == '@');
			map[r][c] = '@';
		}
		do {
			r = rand.nextInt(HEIGHT);
			c = rand.nextInt(WIDTH);
		} while (map[r][c] != '.');
		map[r][c] = 'S';
		do {
			r = rand.nextInt(HEIGHT);
			c = rand.nextInt(WIDTH);
		} while (map[r][c] != '.');
		map[r][c] = 'G';
		return map;
	}

}
