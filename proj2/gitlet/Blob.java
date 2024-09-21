package gitlet;


import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import static gitlet.Repository.OBJ_DIR;
import static gitlet.Utils.*;

/**
 * each blob contains all content of one file
 */
public class Blob implements Serializable {

    // save content of the file
    private byte[] content;

    // hash value of the bold
    private String blobId;

    // the first two letter of hash value to decide where to put
    private File blobDir;

    // the name of bold start from the third letter
    private File blobFile;

    // source file
    private File sourceFile;

    public Blob(File file) {
        this.sourceFile = file;
        //read as String and serialize it to objects
        content = readContents(file);
        blobId = sha1(content);
        blobDir = join(OBJ_DIR, blobId.substring(0, 2));
        blobFile = join(blobDir, blobId.substring(2));
        //save blob to objects
        writeObject(blobFile, this);
    }

    public Blob(String fileName, String sha1, byte[] con) {
        File f = join(Repository.CWD, fileName);
        this.sourceFile = f;
        this.content = con;
        this.blobId = sha1;
        blobDir = join(Repository.OBJ_DIR, blobId.substring(0, 2));
        blobFile = join(blobDir, blobId.substring(2));
        writeObject(blobFile, this);
    }

    public byte[] getContent() {
        return content;
    }

    public String getBlobId() {
        return blobId;
    }

    public File getBlobFile() {
        return blobFile;
    }

    public void writeToSourceFile(){
        writeContents(sourceFile, content);
    }

     public static Blob getBlobById(String sha1){
        return readObject(getFileById(sha1), Blob.class);
     }

    public String readContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
