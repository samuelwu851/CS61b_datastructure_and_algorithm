package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.TETileWrapper;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.*;

public class WorldGenerator implements Serializable {

    private static final int RoomNum = 25; // total number of room
    private final int N;                   // dimension of worldWrappers
    private Random RANDOM;
    private Long seed;

    private TETileWrapper[][] worldWrappers;
    private final int width;
    private final int height;

    // avatar
    private TETileWrapper avatar;

    private LinkedList<Room> rooms = new LinkedList<>();
    private LinkedList<Room> randomRooms = new LinkedList<>();

    // A* algorithm
    private int source;
    private int target;
    private final int[] edgeTo;
    private final int[] distTo;
    // we found target?
    private boolean targetFound;

    // it is the source connect to target at first time?
    private boolean isFirst;
    // we always use center as target
    private boolean alwaysCenterTarget;

    public WorldGenerator(Long seed, TETile[][] world, boolean alwaysCenterTarget) {
        this.seed = seed;
        this.RANDOM = new Random(seed);

        this.width = world.length;
        this.height = world[0].length;
        this.N = Math.max(width, height);
        this.edgeTo = new int[V()];
        this.distTo = new int[V()];
        this.targetFound = false;
        this.worldWrappers = new TETileWrapper[width][height];
        //init world
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                worldWrappers[x][y] = new TETileWrapper(Tileset.NOTHING, x, y);
            }
        }
        reset();

        this.source = 0;
        setTarget(width / 2, height / 2);
        this.isFirst = true;
        this.alwaysCenterTarget = alwaysCenterTarget;

    }

    public TETile[][] moveAvatarAndGenerateWorld(String direction) {
        moveAvatar(direction);
        return getWorldByWorldWrapper();
    }

    public void moveAvatar(String direction) {
        if ("W".equals(direction) && validDirection("W")) {
            moveTo("W");
        }
        if ("S".equals(direction) && validDirection("S")) {
            moveTo("S");
        }
        if ("A".equals(direction) && validDirection("A")) {
            moveTo("A");
        }
        if ("D".equals(direction) && validDirection("D")) {
            moveTo("D");
        }
    }

    private boolean validDirection(String direction) {
        int x = avatar.getX();
        int y = avatar.getY();
        TETileWrapper teTileWrapper = switch (direction) {
            case "W" -> worldWrappers[x][y + 1];
            case "S" -> worldWrappers[x][y - 1];
            case "A" -> worldWrappers[x - 1][y];
            case "D" -> worldWrappers[x + 1][y];
            default -> null;
        };
        return teTileWrapper.getTile().equals(Tileset.FLOOR);
    }

    public void moveTo(String direction) {
        int x = avatar.getX();
        int y = avatar.getY();
        switch (direction) {
            case "W":
                worldWrappers[x][y].setTile(Tileset.FLOOR);
                worldWrappers[x][y + 1].setTile(Tileset.AVATAR);
                this.avatar = worldWrappers[x][y + 1];
                break;
            case "S":
                worldWrappers[x][y].setTile(Tileset.FLOOR);
                worldWrappers[x][y - 1].setTile(Tileset.AVATAR);
                this.avatar = worldWrappers[x][y - 1];
                break;
            case "A":
                worldWrappers[x][y].setTile(Tileset.FLOOR);
                worldWrappers[x - 1][y].setTile(Tileset.AVATAR);
                this.avatar = worldWrappers[x - 1][y];
                break;
            case "D":
                worldWrappers[x][y].setTile(Tileset.FLOOR);
                worldWrappers[x + 1][y].setTile(Tileset.AVATAR);
                this.avatar = worldWrappers[x + 1][y];
                break;
        }
    }

    private void setSource(int x, int y) {
        this.source = xyTo1D(x, y);
    }

    private void setTarget(int x, int y) {
        this.target = xyTo1D(x, y);
    }

    /**
     * Returns x coordinate for given vertex.
     * For example if N = 10, and V = 12, returns 2.
     */
    private int toX(int v) {
        return v % N + 1;
    }

    /**
     * Returns y coordinate for given vertex.
     * For example if N = 10, and V = 12, returns 1.
     */
    private int toY(int v) {
        return v / N + 1;
    }

    /**
     * Returns one dimensional coordinate for vertex in position x, y.
     */
    private int xyTo1D(int x, int y) {
        return (y - 1) * N + (x - 1);
    }

    /**
     * reset all distTo, edgeTo and tileWrapper without room
     */
    private void reset() {
        for (int i = 0; i < V(); i++) {
            distTo[i] = Integer.MAX_VALUE;
            edgeTo[i] = Integer.MAX_VALUE;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (!worldWrappers[x][y].isRoom()) {
                    worldWrappers[x][y].markTile(false);
                }
            }
        }
    }

    private int V() {
        return N * N;
    }

    private void connectRooms() {
        for (int i = 0; i < RoomNum; i++) {
            Room room = new Room(worldWrappers, i, seed);
            rooms.add(room);
            room.makeRoom();
            reset();
            connectRoomToTarget(room);
        }
    }

    private void connectRoomToTarget(Room room) {
        if (alwaysCenterTarget) {
            connectRoomToTargetByCenterTarget(room);
        } else {
            connectRoomToTargetByRandomTarget(room);
        }
    }

    private void connectRoomToTargetByCenterTarget(Room room) {
        targetFound = false;
        TETileWrapper randomExit = room.getRandomExitOfRoom();
        setSource(randomExit.getX(), randomExit.getY());
        astar();
        buildHallwayByShortestPath(target);
    }

    private void connectRoomToTargetByRandomTarget(Room room) {
        if (isFirst) {
            setFirstTargetAndSource(room);
            astar();
        } else {
            targetFound = false;
            setRandomTargetAndSource(room);
            astar();
        }
        buildHallwayByShortestPath(target);
    }

    private void buildHallwayByShortestPath(int v) {
        // center as floor
        int x = toX(v);
        int y = toY(v);
        worldWrappers[x][y].setTile(Tileset.FLOOR);
        // four directions as wall
        // north (x, y + 1)
        if (isHallwayWall(x, y + 1)) {
            worldWrappers[x][y + 1].setTile(Tileset.WALL);
        }
        // south (x, y - 1)
        if (isHallwayWall(x, y - 1)) {
            worldWrappers[x][y - 1].setTile(Tileset.WALL);
        }
        // west (x - 1, y)
        if (isHallwayWall(x - 1, y)) {
            worldWrappers[x - 1][y].setTile(Tileset.WALL);
        }
        // east (x + 1, y)
        if (isHallwayWall(x + 1, y)) {
            worldWrappers[x + 1][y].setTile(Tileset.WALL);
        }
        if (v == source) {
            return;
        }
        buildHallwayByShortestPath(edgeTo[v]);
    }

    private boolean isHallwayWall(int x, int y) {
        // can choose side of limbo and room as wall of hallway
        // but can't choose floor in already build hallway as wall of hallway
        return x < width && x >= 0
                && y < height && y >= 0
                && !worldWrappers[x][y].isRoom()
                && !worldWrappers[x][y].getTile().equals(Tileset.FLOOR);
    }

    private void astar(){
        ArrayDeque<Integer> fringe = new ArrayDeque<>();
        fringe.add(source);
        setMarkInWorldWrappers(source, true);
        while (!fringe.isEmpty()){
            int v = findMinUnmarked(fringe);
            fringe.remove(v);
            for (TETileWrapper tile :findNeighbor(v)){
                if (!tile.isMarked()){
                    int w = xyTo1D(tile.getX(), tile.getY());
                    fringe.add(w);
                    setMarkInWorldWrappers(w, true);
                    edgeTo[w] = v;
                    distTo[w] = distTo[v] + 1;
                    if (w == target){
                        targetFound = true;
                        return;
                    }
                }
            }
        }

    }


    private int findMinUnmarked(Queue<Integer> queue) {
        int minVertex = queue.peek();
        int minPath = distTo[minVertex] + h(minVertex);
        for (int vertex : queue) {
            if (distTo[vertex] + h(vertex) < minPath) {
                minVertex = vertex;
            }
        }
        return minVertex;
    }

    private int h(int v) {
        return Math.abs(toX(v) - toX(target)) + Math.abs(toY(v) - toY(target));
    }

    private LinkedList<TETileWrapper> findNeighbor(int v) {
        LinkedList<TETileWrapper> neighbors = new LinkedList<>();
        int x = toX(v);
        int y = toY(v);
        // top
        if (isNeighbor(x, y + 1)) {
            neighbors.add(worldWrappers[x][y + 1]);
        }
        // bottom
        if (isNeighbor(x, y - 1)) {
            neighbors.add(worldWrappers[x][y - 1]);
        }
        // left
        if (isNeighbor(x - 1, y)) {
            neighbors.add(worldWrappers[x - 1][y]);
        }
        // right
        if (isNeighbor(x + 1, y)) {
            neighbors.add(worldWrappers[x + 1][y]);
        }
        return neighbors;
    }

    private boolean isNeighbor(int x, int y) {
        return x > 0 && y > 0 && x < width - 1 && y < height - 1
                && !worldWrappers[x][y].isRoom();
    }

    private void setFirstTargetAndSource(Room room) {
        int randomNum = RANDOM.nextInt((width - 2) * (height - 2));
        int num = 0;
        // transfer 2d to 1d
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (num == randomNum) {
                    setTarget(x, y);
                    worldWrappers[x][y].setTile(Tileset.FLOOR);
                    isFirst = false;
                    //set first source
                    TETileWrapper randomExit = room.getRandomExitOfRoom();
                    setSource(randomExit.getX(), randomExit.getY());
                    return;
                }
                num++;
            }
        }
    }

    private void setRandomTargetAndSource(Room room) {
        LinkedList<TETileWrapper> notRoomButFloors = notRoomButFloors();

        Map<TETileWrapper, TETileWrapper> exitsAndDoor = room.getAllExitsOfRoom();
        int randomNum1, randomNum2;
        // set random target
        randomNum1 = RANDOM.nextInt(notRoomButFloors.size());
        TETileWrapper tileWrapper = notRoomButFloors.get(randomNum1);
        setTarget(tileWrapper.getX(), tileWrapper.getY());
        // set random source
        LinkedList<TETileWrapper> exits = new LinkedList<>(exitsAndDoor.keySet());
        randomNum2 = RANDOM.nextInt(exitsAndDoor.size());
        TETileWrapper randomExit = exits.get(randomNum2);
        int x = randomExit.getX();
        int y = randomExit.getY();
        setSource(x, y);
        // set door in room as floor by exit
        TETileWrapper door = exitsAndDoor.get(randomExit);
        door.setTile(Tileset.FLOOR);
    }

    private LinkedList<TETileWrapper> notRoomButFloors() {
        LinkedList<TETileWrapper> notRoomButFloors = new LinkedList<>();
        for (int x = 1; x < width - 1; x += 1) {
            for (int y = 1; y < height - 1; y += 1) {
                if (!worldWrappers[x][y].isRoom() && worldWrappers[x][y].getTile().equals(Tileset.FLOOR)) {
                    notRoomButFloors.add(worldWrappers[x][y]);
                }
            }
        }
        return notRoomButFloors;
    }


    private void setMarkInWorldWrappers(int v, boolean markedValue) {
        worldWrappers[toX(v)][toY(v)].markTile(markedValue);
    }

    public TETile[][] generateWorld() {
        connectRooms();
        randomAvatar();
        return getWorldByWorldWrapper();
    }

    private void randomAvatar() {
        LinkedList<TETileWrapper> floor = new LinkedList<>();
        for (int x = 0; x < width - 1; x++){
            for (int y = 0; y < height - 1; y++){
                if (worldWrappers[x][y].getTile().equals(Tileset.FLOOR)){
                    floor.add(worldWrappers[x][y]);
                }
            }
        }
        TETileWrapper avatarTile = floor.get(RANDOM.nextInt(floor.size()));
        worldWrappers[avatarTile.getX()][avatarTile.getY()].setTile(Tileset.AVATAR);
        this.avatar = worldWrappers[avatarTile.getX()][avatarTile.getY()];
    }

    private TETile[][] getWorldByWorldWrapper() {
        TETile[][] world = new TETile[width][height];
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                world[x][y] = worldWrappers[x][y].getTile();
            }
        }
        return world;
    }

}
