package gitlet;

import static gitlet.Utils.printandExit;
import static gitlet.Utils.validateNumArgs;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Samuel
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0 || args == null) {
            printandExit("Must have at least one argument");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(firstArg, args, 1);
                Repository.init();
                break;
            case "add":
                validateNumArgs(firstArg, args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(firstArg, args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(firstArg,args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                validateNumArgs(firstArg, args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(firstArg, args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateNumArgs(firstArg, args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(firstArg, args, 1);
                Repository.status();
                break;
            case "checkout":
                checkOut(args);
                break;
            case "branch":
                validateNumArgs(firstArg, args, 1);
                Repository.createBranch(args[0]);
                break;
            case "rm-branch":
                validateNumArgs(firstArg, args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(firstArg, args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(firstArg, args, 2);
                Repository.merge(args[1]);
                break;
            // TODO: FILL THE REST IN
        }
    }

    public static void checkOut(String[] args){
            if (args.length == 2) {
                String branchName = args[1];
                Repository.changeBranch(branchName);
            } else if (args.length == 3) {
                if (args[1].equals("--")) {
                    String fName = args[2];
                    Repository.checkOutFile(fName);
                } else {
                    printandExit("Incorrect operands.");
                }
            } else if (args.length == 4) {
                if (args[2].equals("--")) {
                    String bId = args[1];
                    String fName = args[3];
                    Repository.checkOutFileFromCommit(bId, fName);
                } else {
                    printandExit("Incorrect operands.");
                }
            } else {
                printandExit("Incorrect operands.");
            }
    }

}
