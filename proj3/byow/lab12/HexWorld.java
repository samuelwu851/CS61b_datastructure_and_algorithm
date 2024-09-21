package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 28713333;
    private static final Random RANDOM = new Random(SEED);

    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillBoardWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.MOUNTAIN;
            case 3: return Tileset.TREE;
            case 4: return Tileset.MOUNTAIN;
            default: return Tileset.NOTHING;
        }
    }

    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    public static void addHexagon(TETile[][] tiles, Position p, TETile t, int size) {
        if (size < 2) return;
        addHexagonHelper(tiles, p, t, size - 1, size);

    }

    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int b, int t){
        //print the first row
        Position startOfRow = p.shiftPosition(b, 0);
        drawRow(tiles,startOfRow, tile, t);

        //print the rest of the hexagon
        if (b > 0) {
            Position position = p.shiftPosition(0, -1);
            addHexagonHelper(tiles, position, tile, b - 1, t + 2);
        }

        //print the reflect row
        Position reflectRow = startOfRow.shiftPosition(0, -(2 * b + 1));
        drawRow(tiles,reflectRow, tile, t);
    }

    public static void addHexCol(TETile[][] tiles, Position p, int size, int num) {
        if (num < 1) return;

        // draw this hexagon
        addHexagon(tiles, p, randomTile(), size);

        if (num > 1) {
            addHexCol(tiles, getBottomNeighbor(p, size), size, num - 1);
        }
    }


    public static Position getBottomNeighbor(Position p, int n){
        return p.shiftPosition(0, - 2 * n);
    }

    public static Position getTopRightNeighbor(Position p, int n){
        return p.shiftPosition(2 * n -1, n);
    }

    public static Position getBottomRightNeighbor(Position p, int n){
        return p.shiftPosition(2 * n -1, -n);
    }



    private static class Position {
        int x;
        int y;

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shiftPosition(int dx, int dy){
            return new Position(this.x + dx, this.y + dy);
        }

    }

    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize){

        addHexCol(tiles, p, hexSize, tessSize);

        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexCol(tiles, p, hexSize, tessSize + i);
        }

        for (int i = tessSize - 2; i >= 0; i--){
            p = getBottomRightNeighbor(p, hexSize);
            addHexCol(tiles, p, hexSize, tessSize + i);
        }
    }


    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillBoardWithNothing(world);
        drawWorld(world, new Position(8,35), 3, 4);

        ter.renderFrame(world);
    }


}
