package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

public class Stage implements Serializable {

    // file path as key and Sha1 as value
    private transient HashMap<String, String> tracked = new HashMap<>();

    // added file, file path as key and Sha1 as value
    private HashMap<String, String> added = new HashMap<>();

    // file path as key
    private HashSet<String> removed = new HashSet<>();

//    public Stage() {
//        removed = new HashSet<>();
//        added = new HashMap<>();
//        tracked = new HashMap<>();
//    }

    public HashMap<String, String> getAdded() {
        return added;
    }

    public HashSet<String> getRemoved() {
        return removed;
    }

    public void add(String fileName){
        File file = join(CWD, fileName);

        Blob blob = new Blob(file);
        String blobId = blob.getBlobId();

        // if the file had been saved in commit and had not changed
        if (getHeadCommitByHash().getStoredFile().get(fileName) == null){
            added.put(fileName, blobId);
            removeAdd(fileName);
            writeObject(Repository.INDEX, this);
        } else if (getHeadCommitByHash().getStoredFile().get(fileName).equals(blobId)) {
            // added area and removed area should remove it
            if (added.containsKey(fileName)) {
                added.remove(fileName);
            }
            removeAdd(fileName);
            writeObject(Repository.INDEX, this);
        } else {
            added.put(fileName, blobId);
            removeAdd(fileName);
            writeObject(Repository.INDEX, this);
        }
    }

    /**
     * return true if the staging area has changed
     * @param file
     * @return
     */
    public boolean remove(File file){
        //check both added and tracked
        String blobId = added.remove(file.getName());
        if(blobId != null){
            // staging area has been changed
            return true;
        }
        // tracked
        if (tracked.get(blobId) != null){
            if(file.exists())
                file.delete();
            return removed.add(file.getName());
        }
        return false;
    }



    public void removeAdd(String fileName){
        if(removed.contains(fileName)){
            removed.remove(fileName);
        }
    }

    public boolean isClear(){
        return added.isEmpty() && removed.isEmpty();
    }

    public void clear(){
        added.clear();
        removed.clear();
    }

    public HashMap commit(){
        tracked.putAll(added);
        for(String fileName : removed){
            tracked.remove(fileName);
        }
        return tracked;
    }

    public void save(){
        writeObject(INDEX, this);
    }


    public void status() {
        System.out.println("=== Staged Files ===");
        for (String s : added.keySet()) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println();
        for (String s : removed) {
            System.out.println(s);
        }
        System.out.println();
    }

    public void checkStage() {
        if (added.size() != 0 || removed.size() != 0) {
            printandExit("You have uncommitted changes.");
        }
    }
}
