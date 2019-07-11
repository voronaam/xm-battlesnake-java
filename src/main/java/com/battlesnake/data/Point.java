package com.battlesnake.data;

public class Point {
    public int x;
    public int y;
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public Point(int[] c) {
        this.x = c[0];
        this.y = c[1];
    }

    public boolean theSame(Point target) {
        return x == target.x && y == target.y;
    }

    public Point leftOf() {
        return new Point(x-1, y);
    }
    public Point rightOf() {
        return new Point(x+1, y);
    }
    public Point upOf() {
        return new Point(x, y-1);
    }
    public Point downOf() {
        return new Point(x, y+1);
    }
    public Point move(Move selectedMove) {
        switch (selectedMove) {
        case DOWN:
            return new Point(x, y + 1);
        case UP:
            return new Point(x, y - 1);
        case LEFT:
            return new Point(x - 1, y);
        case RIGHT:
            return new Point(x + 1, y);
        }
        return this;
    }
    public boolean isValid(int width, int height) {
        return x >= 0 && y >= 0 && x <= width - 1 && y <= height - 1;
    }
    public double distanceTo(int[] other) {
        return Math.sqrt(Math.pow(other[0] - x, 2) + Math.pow(other[1] - y, 2));
    }

}
