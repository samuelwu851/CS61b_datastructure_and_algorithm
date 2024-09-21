package gitlet;




import org.apache.commons.collections.ResettableIterator;

import javax.xml.transform.Result;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Samuel
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */


    /** The timestamp of this Commit. */
    private String timeStamp;

    /** The message of this Commit. */
    private String message;

    /** the file this commit contains
     * file path as key and sha1Value as value
     */
    private Map<String, String> storedFile = new HashMap<>();

    /** the uid of this commit */
    private String sha1Value;

    /** parent commit */
    private ArrayList<String> parentCommit;

    /** Date formatter */
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    /* TODO: fill in the rest of this class. */

    public String initialCommit(){
        message = "initial commit";
        timeStamp = "Thu Jan 1 08:00:00 1970 +0800";
        parentCommit = new ArrayList<>();
        sha1Value = sha1(message, timeStamp, parentCommit.toString(), storedFile.toString());
        File fileToSave = getFileById(sha1Value);
        writeToGlobalLog();
        writeObject(fileToSave, this);
        return sha1Value;
    }

    public String getSha1Value() {
        return sha1Value;
    }

    public ArrayList<String> getParentCommit() {
        return parentCommit;
    }

    public Map<String, String> getStoredFile() {
        return storedFile;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time,
     * creating a new commit. The commit is said to be tracking the saved files.
     *
     * Finally, files tracked in the current commit may be untracked in the new commit
     * as a result being staged for removal by the rm command (below).
     * @param msg
     */
    public String commit(String msg, Commit p) {
        message = msg;
        Date date = new Date();
        timeStamp = formatter.format(date);
        parentCommit = new ArrayList<>();
        parentCommit.add(p.getSha1Value());
        Stage stage = getIndex();
        if (stage.isClear()){
            //if true means no file was updated
            printandExit("No changes added to the commit.");
        }
        // storedFile from parentCommit should be executed first
        // since the later put() would overwrite the existing value associated with the same key
        storedFile = new HashMap<>(p.storedFile);
        HashMap<String, String> newTrackedFile = stage.getAdded();
        storedFile.putAll(newTrackedFile);
        stage.clear();
        stage.save();
        String sha1 = sha1(timeStamp, message, parentCommit.toString(), storedFile.toString());
        File fileById = getFileById(sha1);
        writeObject(fileById, this);
        writeToGlobalLog();
        return sha1;
    }

    public void getLog() {
        this.getSelfLog();
        if(parentCommit.size() != 0){
            Commit pc = getCommitById(parentCommit.get(0));
            pc.getLog();
        }
    }

    public void getSelfLog(){
        System.out.println("===");
        System.out.println("commit " + sha1Value);
        if (parentCommit.size() == 2) {
            StringBuilder s = new StringBuilder();
            s.append("Merge: ");
            s.append(parentCommit.get(0).substring(0,7));
            s.append(" ");
            s.append(parentCommit.get(1).substring(0,7));
            System.out.println(s);
        }
        System.out.println("Date: " + this.timeStamp);
        System.out.println(this.message);
        System.out.println();
    }

    public void writeToGlobalLog() {
        GlobalLogs logs = new GlobalLogs();
        logs.addLogs(this.sha1Value);
        logs.writeToLogs();
    }

    public boolean checkOutFileName(String fileName){
        String sha1 = storedFile.get(fileName);
        if (sha1 == null){
            return false;
        }
        Blob.getBlobById(sha1).writeToSourceFile();
        return true;
    }

    public void putFilesToCWD() {
        for (String s : storedFile.keySet()){
            String s1 = storedFile.get(s);
            Blob b = Blob.getBlobById(s1);
            File f = join(CWD, s);
            writeContents(f, b.getContent());
        }
    }

    public void reset(){
        for (String s : storedFile.keySet()){
            String s1 = storedFile.get(s);
            Blob blob = Blob.getBlobById(s1);
            blob.writeToSourceFile();
        }
        INDEX.delete();
    }

    public List<String> getParentList(HashSet<String> set) {
        List<String> list = new ArrayList<>();
        if (!set.contains(sha1Value)) {
            list.add(sha1Value);
            set.add(sha1Value);
        }
        if (this.parentCommit.size() == 1) {
            Commit parentCommit = getCommitById(this.parentCommit.get(0));
            list.addAll(parentCommit.getParentList(set));
        } else if (this.parentCommit.size() == 2) {
            if (!set.contains(parentCommit.get(0))) {
                list.add(parentCommit.get(0));
                set.add(parentCommit.get(0));
            }
            if (!set.contains(parentCommit.get(1))) {
                list.add(parentCommit.get(1));
                set.add(parentCommit.get(1));
            }
            list.addAll(getCommitById(parentCommit.get(0)).getParentList(set));
            list.addAll(getCommitById(parentCommit.get(1)).getParentList(set));
        }
        return list;
    }

    public String merge(String m, String p1, String p2, HashMap<String, String> resultFiles) {
        message = m;
        Date date = new Date();
        timeStamp = formatter.format(date);
        parentCommit = new ArrayList<>();
        parentCommit.add(p1);
        parentCommit.add(p2);
        this.storedFile = new HashMap<>(resultFiles);
        Repository.INDEX.delete();
        sha1Value = sha1(timeStamp, message, parentCommit.toString(), storedFile.toString());
        File fileToSave = getFileById(sha1Value);
        writeObject(fileToSave, this);
        this.putFilesToCWD();
        writeToGlobalLog();
        return sha1Value;
    }

}
