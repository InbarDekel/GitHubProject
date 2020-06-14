package Classess;



import generated.MagitCommits;
import generated.MagitSingleCommit;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Commit implements CommitRepresentative {
    private String SHA1;
    private Folder rootFolder;
    private String rootFolderSHA1;
    private String SHA1PreveiousCommit;
    private String SHA1anotherPreveiousCommit;
    private String description;
    private String creationDate;
    private User changer;
    private String SHAContent;

    //constractors

    public Commit()
    {
        description = "Start The Machine";
        creationDate = GitManager.getDateFromObject(null);
        //changer = "Administrator";
        SHA1PreveiousCommit=null;
        SHA1anotherPreveiousCommit=null;
    }

    public Commit(String usersDescription, User userName)
    {
        description=usersDescription;
        creationDate = GitManager.getDateFromObject(null);
        changer= userName;
    }

    public Commit(ArrayList<String> args)
    {
        rootFolderSHA1 = args.get(0);
          SHA1PreveiousCommit = args.get(1);
        SHA1anotherPreveiousCommit = args.get(2);
          description = args.get(3);
          creationDate = args.get(4);
          //changer = args.get(5);
    }


    // getters, setters
    public String getCreationDate() { return creationDate; }
    public User getChanger() { return changer; }
    public String getSHA1anotherPreveiousCommit() { return SHA1anotherPreveiousCommit; }
    public void setSHA1anotherPreveiousCommit(String SHA1PrevPrevCommit) { this.SHA1anotherPreveiousCommit = SHA1PrevPrevCommit; }

    public String getRootFolderSHA1() { return rootFolderSHA1; }
    public void setRootFolderSHA1(String rootFolderSHA1) { this.rootFolderSHA1 = rootFolderSHA1; }

    public String getSHAContent() { return SHAContent; }

    public Folder getRootFolder() { return rootFolder; }
    public void setRootFolder(Folder folder) { rootFolder =  folder;}

    public String getSHA1PreveiousCommit() { return SHA1PreveiousCommit; }
    public void setSHA1PreveiousCommit(String SHA1PreveiousCommit) { this.SHA1PreveiousCommit = SHA1PreveiousCommit; }

    public void setChanger(User changer) { this.changer = changer; }

    public void setSHA1(String SHA1) { this.SHA1=SHA1; }
    public String getSHA(){ return SHA1; }

    public String getDescription (){ return description;}
    public void setDescription (String descriptionFromUser){ description =  descriptionFromUser; }

    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
    public void setSHAContent(String content)
    {
        this.SHAContent=content;
    }

        //methods
    public void setCommitFileContentToSHA() {
        String delimiter = ", ";
        StringBuilder s = new StringBuilder();
        if(rootFolder != null) {
         s.append(GitManager.generateSHA1FromString(rootFolder.getFolderContentString()));
            }
            else {
           s.append(GitManager.generateSHA1FromString(""));
            }
        s.append(System.lineSeparator());
        s.append(getSHA1PreveiousCommit());
        s.append(System.lineSeparator());
        s.append(SHA1anotherPreveiousCommit);
        s.append(System.lineSeparator());
        s.append(description);
        s.append(System.lineSeparator());
        s.append(creationDate);
        s.append(System.lineSeparator());
        s.append(changer.getName());
        SHA1 = GitManager.generateSHA1FromString(s.toString());
        SHAContent = s.toString();
    }

    public static Map<String, Commit> getAllCommitsToMap(MagitCommits commits, Map<String, Folder.Component> folderList,UserManager UManager) throws Exception {
        Map<String, Commit> newMap = new HashMap<>();
        List<MagitSingleCommit> commitlist = commits.getMagitSingleCommit();
        for (MagitSingleCommit c : commitlist) {
            ArrayList<String> args = new ArrayList<>();
            String rootFolderID = c.getRootFolder().getId();
            Folder curFolder = (Folder) folderList.get(rootFolderID).getDirectObject();
            if (folderList.containsKey(rootFolderID)) {
                if (curFolder.isRoot()) {
                    String rootFolderSHA1 = folderList.get(rootFolderID).getFolderSHA1();
                    args.add(0, rootFolderSHA1);
                    args.add(1, null);
                    args.add(2, null);
                    args.add(3, c.getMessage());
                    args.add(4, c.getDateOfCreation());
                    args.add(5, c.getAuthor());
                    Commit newcommit = new Commit(args);
                    if(UManager.getUser(args.get(5)) == null)
                    {
                        UManager.addUser(args.get(5));
                    }
                    newcommit.setChanger(UManager.getUser(args.get(5)));

                    if (!newMap.containsKey(c.getId())) {
                        newMap.put(c.getId(), newcommit);
                        newcommit.setRootFolder(curFolder);
                        //newcommit.setCommitFileContentToSHA();
                    } else {
                        //return exception id already exit
                        throw new Exception("The ID of the commit already exist");
                    }
                } else {
                    //return folder is not isroot
                    throw new Exception("The folder is not of the isRoot inside the xml");
                }
            } else {
                //return root folder does not exist
                throw new Exception("Root folder does not exist");
            }

        }
        return newMap;
    }
   public String getSha1()
    {
        return SHA1;
    }

    public String getFirstPrecedingSha1()
    {
        if(SHA1PreveiousCommit != null)
        return SHA1PreveiousCommit;
        else
            return new String();
    }

   public String getSecondPrecedingSha1()

    {
        if(SHA1anotherPreveiousCommit != null)
        return SHA1anotherPreveiousCommit;
        else
            return new String();
    }




}
