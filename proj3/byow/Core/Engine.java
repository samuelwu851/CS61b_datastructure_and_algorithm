package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.io.File;
import java.io.Serializable;

import static byow.Core.MyUtils.*;

public class Engine implements Serializable {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private Long seed;

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    private Menu menu = new Menu(40, 40);
    // the world and the generator of world
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private WorldGenerator worldGenerator;
    private boolean gameInit = true;
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .save directory. */
    public static final File SAVE_DIR = join(CWD, ".save");

    public void interactWithKeyboard() {
        // if it is the initialization of game, we should draw menu
        if (gameInit) {
            menu.drawMenu();
        }
        String inputString = "";
        char typedKey;
        // using loop(or "dead loop") to wait the typed input from player!
        // note: "x" is string(double queue); 'x' is char(single queue)
        while (true) {
            typedKey = MyUtils.getNextKey();
            // don't care about other char
            if (isNumber(typedKey) || isValidChar(typedKey)) {
                inputString += typedKey;
            }
            // if it is the initialization of game and getting 'S',
            // then, we can get seed(number) to create a new world
            if (gameInit && typedKey =='S'){
                int stepIndex = inputString.indexOf("S");
                inputString = inputString.substring(1, stepIndex);
                break;
            }
            if (gameInit) {
                if (typedKey == 'Q') {
                    System.exit(0);
                }
                if (typedKey == 'N') {
                    continue;
                }
                if (typedKey == 'L') {
                    load();
                    break;
                }
                // why not work when using switch()? -> just break condition fo 'if'
                // because we need 'break;' statement
            }
            // if not game init, we should either move avatar or quit/save the game
            // :Q,W,S,A,D
            if (!gameInit && inputString.equals(":Q")) {
                saveAndQuit();
            }
            if (!gameInit && inputString.length() == 1) {
                if (inputString.equals("W") || inputString.equals("S")
                        || inputString.equals("A") || inputString.equals("D")
                        || inputString.equals("P")) {
                    break;
                }
            }
        }
        renderWorld(inputString);
        // next typed in keyboard
        interactWithKeyboard();

    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        // when user is playing the game
        switch (input){
            case "W":
                return worldGenerator.moveAvatarAndGenerateWorld("W");
            case "S":
                return worldGenerator.moveAvatarAndGenerateWorld("S");
            case "A":
                return worldGenerator.moveAvatarAndGenerateWorld("A");
            case "D":
                return worldGenerator.moveAvatarAndGenerateWorld("D");
        }
        // if it is the initialization of the game
        if (seed == null) {
            seed = Long.parseLong(input);
            TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
            worldGenerator = new WorldGenerator(seed, finalWorldFrame, false);
            return worldGenerator.generateWorld();
        }
        return world;
    }

    public void renderWorld(String inputString){
        world = interactWithInputString(inputString);
        if (gameInit){
            // if the game haven't inited yet, set it to true
            // and it will not longer draw menu
            gameInit = false;
            renderWorldAtInit(world);
        } else {
            // game has already benn inited
            // just move the avatar
            renderWorldWithMoving(world);
        }

    }

    private void renderWorldWithMoving(TETile[][] world) {
        ter.renderFrame(world);
    }

    public void renderWorldAtInit(TETile[][] world){
        ter.initialize(WIDTH,HEIGHT);
        ter.renderFrame(world);
    }
    // load engine of saving
    private void load() {
        // read obj, then get some variables
        Engine loadEngine = readObject(join(SAVE_DIR, "saveEngine.txt"), Engine.class);
        worldGenerator = loadEngine.getWorldGenerator();
        world = loadEngine.getWorld();
        ter = loadEngine.getTer();
        seed = loadEngine.getSeed();
        System.out.println(seed);
    }

    // save engine and quit process
    private void saveAndQuit() {
        // save obj in file
        // note: we must serialize all classes (i.e. implements Serializable)
        if (!SAVE_DIR.exists()) {
            SAVE_DIR.mkdir();
        }
        writeObject(join(SAVE_DIR, "saveEngine.txt"), this);
        System.exit(0);
    }

    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }

    public Long getSeed() {
        return seed;
    }

    public TERenderer getTer() {
        return ter;
    }

    public TETile[][] getWorld() {
        return world;
    }
}
