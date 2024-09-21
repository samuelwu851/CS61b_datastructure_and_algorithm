package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.TETileWrapper;
import byow.TileEngine.Tileset;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;


public class Room implements Serializable {

    private TETileWrapper[][] worldWrapper;
    private Random RANDOM;

    //the number of room
    private int roomNum;

    // the size and location of room
    private int width;
    private int height;
    private int x;
    private int y;


    public Room(TETileWrapper[][] worldWrapper, int roomNum, long seed) {
        this.worldWrapper = worldWrapper;
        this.RANDOM = new Random(seed);
        this.roomNum = roomNum;
        this.width = randomRoomSide();
        this.height = randomRoomSide();
        this.x = randomRoomPosition(true);
        this.y = randomRoomPosition(false);
    }

    public TETileWrapper[][] makeRoom() {
        correctRoom();
        fillAllTilesInRoom();
        return worldWrapper;
    }

    private void fillAllTilesInRoom() {
        //top row of room
        for (int i = x; i < x + width; i++){
            fillOneTileInRoom(i, y, Tileset.WALL);
        }
        //bottom
        for (int i = x; i < x + width; i++){
            fillOneTileInRoom(i, y + 1 - height, Tileset.WALL);
        }
        //middle
        int dy = height - 2;
        while (dy > 0) {
            for (int i = x; i < x + width; i++){
                if (i == x || i == x + width - 1){
                    fillOneTileInRoom(i, y - dy, Tileset.WALL);
                } else {
                    fillOneTileInRoom(i, y - dy, Tileset.FLOOR);
                }
            }
            dy--;
        }
    }

    public int randomRoomSide(){
        return RANDOM.nextInt(10) + 3;
    }


    public boolean validRoom(){
        int worldWidth = worldWrapper.length;
        if (x + width >= worldWidth || y - height - 1 < 0){
            return false;
        }
        for (int i = x; i < x + width; i++){
            for (int j = y; j > y - height; j--){
                if (!worldWrapper[i][j].getTile().equals(Tileset.NOTHING)){
                    //TODO
                    return false;
                }
            }
        }
        return true;
    }

    public void fillOneTileInRoom(int x, int y, TETile tileType){
        worldWrapper[x][y].setTile(tileType);
        if (tileType.equals(Tileset.WALL)){
            worldWrapper[x][y].setIsAround();
        }
        worldWrapper[x][y].markTile(true);
        worldWrapper[x][y].markRoom();
        worldWrapper[x][y].setRoomNum(roomNum);
    }

    private int randomRoomPosition(boolean isXPosition){
        return isXPosition? RANDOM.nextInt(worldWrapper.length) : RANDOM.nextInt(worldWrapper[0].length);
    }

    private void correctRoom(){
        while (!validRoom()) {
            width = randomRoomSide();
            height = randomRoomSide();
            x = randomRoomPosition(true);
            y  = randomRoomPosition(false);
        }
    }


    /**
     *
     * room:
     * #1
     * w w w w w
     * w       w
     * w       w
     * w       w
     * w w w w w
     *
     * #2
     * w w w w w
     * w       w
     * w       d
     * w       w
     * w w w w w
     *
     * #3
     * w w w w w
     * w       w
     * w       d e
     * w       w
     * w w w w w
     *
     */




    /**
     * Except corners, all other WALL tiles can be choosen as door
     * @return all possible tiles that can be used as door
     */
    private LinkedList<TETileWrapper> getDoorsInRoom() {
        LinkedList<TETileWrapper> door = new LinkedList<>();
        //Top side
        for (int dx = x + 1; dx < x + width - 1; dx++){
            door.add(worldWrapper[dx][y]);
        }
        //bottom side
        for (int dx = x + 1; dx < x + width - 1; dx++){
            door.add(worldWrapper[dx][y + 1 - height]);
        }
        //left side
        for (int dy = y - 1; dy > y + 1 - height; dy--){
            door.add(worldWrapper[x][dy]);
        }
        //right side
        for (int dy = y - 1; dy > y + 1 - height; dy--){
            door.add(worldWrapper[x + width - 1][dy]);
        }
        return door;
    }

    /**
     * Get all possible exits of room
     * @return linkedlist of exits
     */
    public Map<TETileWrapper, TETileWrapper> getAllExitsOfRoom(){
        LinkedList<TETileWrapper> doors = getDoorsInRoom();
        //LinkedList<TETileWrapper> exits = new LinkedList<>();
        //use exit as key and door as value
        Map<TETileWrapper, TETileWrapper> exits = new HashMap<>();
        for (TETileWrapper door : doors){
            TETileWrapper exit = getExitOfDoor(door.getX(), door.getY());
            if(exit != null){
                exits.put(exit, door);
            }
        }
        return exits;
    }

    public TETileWrapper getRandomExitOfRoom(){
        Map<TETileWrapper, TETileWrapper> exits = getAllExitsOfRoom();
        TETileWrapper[] arrayOfExits = exits.keySet().toArray(new TETileWrapper[0]);
        TETileWrapper randomExitOfRoom = arrayOfExits[RANDOM.nextInt(arrayOfExits.length)];
        int x = randomExitOfRoom.getX();
        int y = randomExitOfRoom.getY();
        worldWrapper[x][y].setTile(Tileset.FLOOR);
        TETileWrapper doorOfRoom = exits.get(randomExitOfRoom);
        x = doorOfRoom.getX();
        y = doorOfRoom.getY();
        worldWrapper[x][y].setTile(Tileset.FLOOR);
        return randomExitOfRoom;

    }

    private TETileWrapper getExitOfDoor(int x, int y) {
        // North of room
        if (isPossibleExit(x, y + 1)){
            return worldWrapper[x][y + 1];
        }
        //South of room
        if (isPossibleExit(x, y - 1)){
            return worldWrapper[x][y - 1];
        }
        //West of room
        if (isPossibleExit(x - 1, y)){
            return worldWrapper[x - 1][y];
        }
        //East of room
        if (isPossibleExit(x + 1, y)){
            return worldWrapper[x + 1][y];
        }
        return null;
    }

    /**
     *
     * @param x location of exit
     * @param y location of exit
     * @return can this tile be a possible exit of the room
     */
    private boolean isPossibleExit(int x, int y) {
        int worldWidth = worldWrapper.length;
        int worldHeight = worldWrapper[0].length;
        return x > 0 && y > 0 && x < worldWidth - 1
                && y  < worldHeight - 1 && !worldWrapper[x][y].isRoom();
    }


    private TETile randomTile() {
        int tileNum = RANDOM.nextInt(4);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            case 3: return Tileset.SAND;
            default: return Tileset.NOTHING;
        }
    }

    public Random getRANDOM() {
        return RANDOM;
    }

    public void setRANDOM(Random RANDOM) {
        this.RANDOM = RANDOM;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(int roomNum) {
        this.roomNum = roomNum;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

