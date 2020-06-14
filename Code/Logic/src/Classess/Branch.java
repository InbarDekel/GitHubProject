package Classess;


import generated.MagitBranches;
import generated.MagitSingleBranch;

import java.util.*;

public class Branch {

        //members
        private String branchName;
        private Commit pointedCommit;


    private  String pointedCommitSHA1="";

    public Boolean getRemoteTrackingBranch() {
        return isRemoteTrackingBranch;
    }

    public Boolean getRemoteBranch() {
        return isRemoteBranch;
    }

    public void setRemoteTrackingBranch(Boolean remoteTrackingBranch) {
        isRemoteTrackingBranch = remoteTrackingBranch;
    }

    public void setRemoteBranch(Boolean remoteBranch) {
        isRemoteBranch = remoteBranch;
    }

    private Boolean isRemoteTrackingBranch;
        private Boolean isRemoteBranch;

        //con
        Branch(String name)
        {
            branchName = name;
            isRemoteBranch = false;
            isRemoteTrackingBranch = false;
        }
        public Branch(String name, String commitSHA1,boolean isRemote,boolean isTracking)
        {
            branchName = name;
            pointedCommitSHA1 = commitSHA1;
            isRemoteBranch = isRemote;
            isRemoteTrackingBranch = isTracking;
        }


        //set\get

        public String getPointedCommitSHA1() { return pointedCommitSHA1; }
        public  void setPointedCommitSHA1(String pointedCommit) { pointedCommitSHA1 = pointedCommit; }

        public String getBranchName() { return branchName; }

        public Commit getPointedCommit() { return pointedCommit; }
        public void setPointedCommit(Commit newC) { pointedCommit=newC; }


        //methods
        public static LinkedList<Branch> getAllBranchesToMap(MagitBranches branches, Map<String, Commit> commits) throws Exception
        {
            LinkedList<Branch> newbranches = new LinkedList<>();
            List<MagitSingleBranch> brancheslist = branches.getMagitSingleBranch();
            for(MagitSingleBranch c: brancheslist)
            {
                Branch b = new Branch(c.getName());
                if(commits.containsKey(c.getPointedCommit().getId())) {
                    b.setPointedCommit(commits.get(c.getPointedCommit().getId()));
                    b.isRemoteBranch = c.isIsRemote();
                    b.isRemoteTrackingBranch = c.isTracking();
                    newbranches.add(b);
                    b.setPointedCommitSHA1(commits.get(c.getPointedCommit().getId()).getSHA());
                }
                else
                {
                    //return commit does not exist in xml
                    throw new Exception("Commit of branch does not exist in the xml");

                }
            }
            return newbranches;
        }
    }
