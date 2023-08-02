package dev.pascan;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;

public class MazeGenerator {
    private int width; // Width of the maze
    private int height; // Height of the maze
    private int multiplier; // Multiplier for pixel size
    private int[][] maze; // Maze representation
    private int stepCounter; // Counter for steps
    private int currentX; // Current x position
    private int currentY; // Current y position

    public MazeGenerator(int width, int height, int multiplier) {
        this.width = width;
        this.height = height;
        this.multiplier = multiplier;
        this.maze = new int[width][height];
        this.stepCounter = 0;
        this.currentX = 0;
        this.currentY = 0;
    }

    private void generateMaze() {
        initializeMaze();

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(0, 0));

        while (!stack.isEmpty()) {
            Point current = stack.peek();
            int x = current.x;
            int y = current.y;
            maze[x][y] = 0; // mark as traversable

            boolean hasUnvisitedNeighbor = false;

            int[] directions = generateRandomDirections();
            for (int direction : directions) {
                int newX = x;
                int newY = y;

                switch (direction) {
                    case 0: // Up
                        newY -= 2;
                        break;
                    case 1: // Right
                        newX += 2;
                        break;
                    case 2: // Down
                        newY += 2;
                        break;
                    case 3: // Left
                        newX -= 2;
                        break;
                }

                if (isValidCell(newX, newY)) {
                    if (maze[newX][newY] == 1) {
                        maze[(x + newX) / 2][(y + newY) / 2] = 0; // path
                        stack.push(new Point(newX, newY));
                        hasUnvisitedNeighbor = true;
                        currentX = stack.isEmpty() ? -1 : stack.peek().x;
                        currentY = stack.isEmpty() ? -1 : stack.peek().y;

                        stepCounter++;
                        saveMazeImage("C:/output/maze_" + stepCounter + ".png", currentX, currentY, maze.clone());
                        break;
                    }
                }
            }

            if (!hasUnvisitedNeighbor) {
                stack.pop();
            }


        }
        // save one image where all black and white probably
        currentX = -1;
        currentY = -1;
        saveMazeImage("C:/output/maze_" + stepCounter + ".png", currentX, currentY, maze);
        solveMaze();
    }

    // sets everything black
    private void initializeMaze() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                maze[x][y] = 1;
            }
        }
    }

    private void solveMaze() {
        int[][] shortestPath = new int[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(shortestPath[x], -1);
        }

        int endX = width - 1;
        int endY = height - 1;
        int startX = 0;
        int startY = 0;

        shortestPath[endX][endY] = 0; // end point

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(endX, endY));

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            int x = current.x;
            int y = current.y;

            int[] dx = {0, 1, 0, -1};
            int[] dy = {-1, 0, 1, 0};

            for (int i = 0; i < 4; i++) {
                int newX = x + dx[i];
                int newY = y + dy[i];

                if (isValidCell(newX, newY) && maze[newX][newY] == 0 && shortestPath[newX][newY] == -1) {
                    shortestPath[newX][newY] = shortestPath[x][y] + 1;
                    queue.add(new Point(newX, newY));
                }
            }
        }

        currentX = startX;
        currentY = startY;

        do {
            int x = currentX;
            int y = currentY;

            maze[x][y] = 2; // mark as part of shortest path
            stepCounter++;
            saveMazeImage("C:/output/maze_" + stepCounter + ".png", currentX, currentY, maze);

            int[] dx = {0, 1, 0, -1};
            int[] dy = {-1, 0, 1, 0};

            for (int i = 0; i < 4; i++) {
                int newX = x + dx[i];
                int newY = y + dy[i];

                if (isValidCell(newX, newY) && maze[newX][newY] == 0 && shortestPath[newX][newY] == shortestPath[x][y] - 1) {
                    currentX = newX;
                    currentY = newY;
                    break;
                }
            }
        } while (currentX != endX || currentY != endY);
    }

    private int[] generateRandomDirections() {
        ArrayList<Integer> directions = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(directions);
        int[] randomDirections = new int[4];
        for (int i = 0; i < 4; i++) {
            randomDirections[i] = directions.get(i);
        }
        return randomDirections;
    }

    private boolean isValidCell(int x, int y) {
        return (x >= 0 && x < width && y >= 0 && y < height);
    }

    public void saveMazeImage(String filename, int currentX, int currentY, int[][] mazei) {
        int[][] maze = new int[width][height];
        for (int i = 0; i < mazei.length; i++) {
            maze[i] = Arrays.copyOf(mazei[i], mazei[i].length);
        }

        Thread t = new Thread(() -> {
            int mazeWidth = width * multiplier;
            int mazeHeight = height * multiplier;

            BufferedImage image = new BufferedImage(mazeWidth, mazeHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, mazeWidth, mazeHeight);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixelColor;
                    if (x == currentX && y == currentY) {
                        pixelColor = Color.RED.getRGB();
                    } else if (maze[x][y] == 2) {
                        pixelColor = Color.GREEN.getRGB();
                    } else {
                        pixelColor = (maze[x][y] == 1) ? Color.BLACK.getRGB() : Color.WHITE.getRGB();
                    }

                    for (int i = 0; i < multiplier; i++) {
                        for (int j = 0; j < multiplier; j++) {
                            int pixelX = x * multiplier + i;
                            int pixelY = y * multiplier + j;
                            image.setRGB(pixelX, pixelY, pixelColor);
                        }
                    }
                }
            }

            g2d.dispose();

            try {
                ImageIO.write(image, "png", new File(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();
    }

    private static void clearOutputFolder() {
        File folder = new File("C:/output");
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static void main(String[] args) {
        clearOutputFolder();
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the width of the maze (odd number): ");
        int width = scanner.nextInt();

        System.out.print("Enter the height of the maze (odd number): ");
        int height = scanner.nextInt();

        System.out.print("Enter the pixel multiplier: ");
        int multiplier = scanner.nextInt();

        scanner.close();

        MazeGenerator mazeGenerator = new MazeGenerator(width, height, multiplier);
        mazeGenerator.generateMaze();
    }
}