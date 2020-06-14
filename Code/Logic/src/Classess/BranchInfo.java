package Classess;

public class BranchInfo {

    public String getName() {
        return branchName;
    }

    public BranchInfo(String name, String descOfCommit) {
        this.branchName = name;
        this.description = descOfCommit;
    }

    public String getDescOfCommit() {
        return description;
    }

    String branchName;

    public void setName(String name) {
        this.branchName = name;
    }

    public void setDescOfCommit(String descOfCommit) {
        this.description = descOfCommit;
    }

    String description;

}
