package Classess;

import java.text.SimpleDateFormat;
import java.util.*;

public class RepInfo {
    String name;
    String activeBranch;
    int amountOfBranches;
    String dateOfLastCommit;
    String descriptionOfLastCommit;

    public String getName() {
        return name;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public int AmountOfBranches() {
        return amountOfBranches;
    }

    public String getDateOfLastCommit() {
        return dateOfLastCommit;
    }

    public String getDescriptionOfLastCommit() {
        return descriptionOfLastCommit;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public void setAmountOfBranches(int amountOfBranches) {
        this.amountOfBranches = amountOfBranches;
    }

    public void setDateOfLastCommit(String dateOfLastCommit) {
        this.dateOfLastCommit = dateOfLastCommit;
    }

    public void setDescriptionOfLastCommit(String descriptionOfLastCommit) {
        this.descriptionOfLastCommit = descriptionOfLastCommit;
    }

    public static Commit getTheMostUpdatesCommit(Map<String, Commit> commitsMap) {
        List<Commit> commitList = new ArrayList<>();
        for (
                Map.Entry<String, Commit> entry : commitsMap.entrySet()) {
            Commit commitNode = entry.getValue();
            commitList.add(commitNode);
        }

        Collections.sort(commitList, new Comparator<Commit>() {
            @Override
            public int compare(Commit o1, Commit o2) {
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = GitManager.getDateObjectFromString(o1.getCreationDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    date2 = GitManager.getDateObjectFromString(o2.getCreationDate());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return date2.compareTo(date1);
            }
        });
return commitList.get(0);
    }
}
