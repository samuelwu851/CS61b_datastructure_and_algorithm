package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static gitlet.Repository.GLOBAL_LOGS;
import static gitlet.Utils.readObject;
import static gitlet.Utils.writeObject;

public class GlobalLogs implements Serializable {

    // contains hashcode of all commits
    private List<String> hashcodeOfCommits;

    public GlobalLogs() {
        if (!GLOBAL_LOGS.exists()){
            hashcodeOfCommits = new ArrayList<>();
        } else {
            GlobalLogs l = readObject(GLOBAL_LOGS, GlobalLogs.class);
            this.hashcodeOfCommits = l.hashcodeOfCommits;
        }
    }

    public void getGlobalLogs() {
        for (String id : hashcodeOfCommits){
            Commit commit = Repository.getCommitById(id);
            commit.getSelfLog();
        }
    }

    public void addLogs(String s) {
        hashcodeOfCommits.add(s);
    }

    public void writeToLogs() {
        writeObject(GLOBAL_LOGS, this);
    }

    public void find(String commitMsg) {
        boolean flag = true;
        for (String id : hashcodeOfCommits){
            Commit commit = Repository.getCommitById(id);
            if (commitMsg.equals(commit.getMessage())){
                System.out.println(commit.getSha1Value());
                flag = false;
            }
        }
        if (flag){
            System.out.println("Found no commit with that message.");
        }
    }
}
