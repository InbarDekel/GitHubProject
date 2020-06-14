package Classess;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import generated.*;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;


public class Repository {
//members
    private Path path;
    private LinkedList<Branch> branches;
    private Branch head;
    private Map<String, Commit> commitMap;
    private String repositoryName;
    private User userOfRepo;
private Folder WCFolder;

    public User getUserOfRepo() {
        return userOfRepo;
    }

    public void setUserOfRepo(User userOfRepo) {
        this.userOfRepo = userOfRepo;
    }


    public void setRepositoryRemoteName(String repositoryRemoteName) {
        this.repositoryRemoteName = repositoryRemoteName;
    }

    public String getRepositoryRemoteName() {
        return repositoryRemoteName;
    }

    public String getRepositoryRemotePath() {
        return repositoryRemotePath;
    }

    public void setRepositoryRemotePath(String repositoryRemotePath) {
        this.repositoryRemotePath = repositoryRemotePath;
    }

    private String repositoryRemotePath = null;

    private String repositoryRemoteName = null;



    //String remoteReferenceName;
    //String referenceNameLocation;


//con
public Repository(Path workingPath, User u) {
        path = workingPath;
        branches = new LinkedList<>();
        //repositoryName = "EmptyRepository";
        commitMap = new HashMap<>();
        userOfRepo = u;
    }

    public Repository() {
    }

    Repository(Path workingPath, Branch headBranch, String repName, User u) {
        path = workingPath;
        branches = new LinkedList<>();
        branches.add(headBranch);
        head = headBranch;
        repositoryName = repName;
        commitMap = new HashMap<>();
        userOfRepo = u;
    }

    //get\set

    public void setRepositoryName(String repositoryName) { this.repositoryName = repositoryName; }

    public Map<String, Commit> getCommitMap() { return commitMap; }

    public void setBranches(LinkedList<Branch> branches) { this.branches = branches; }

    public String getRepositoryName() { return repositoryName; }

    public Path getRepositoryPath() { return path; }

    public LinkedList<Branch> getBranches() { return branches; }

    public Branch getHeadBranch() { return head; }

    public void setHeadBranch(Branch b) { this.head = b; }


    Map<String, Commit> getCommitList() { return commitMap; }

//    public static MagitRepository loadFromXml(String i_XmlPath) throws Exception {
////            InputStream inputStream;
////            JAXBContext jc;
////            Unmarshaller u;
////
////            try{inputStream= new FileInputStream(i_XmlPath);}
////            catch(FileNotFoundException e) {throw new Exception("File not found");}
////            try{jc = JAXBContext.newInstance("generated");}
////            catch(JAXBException e) {throw new Exception("Cannot creat objects from xml");}
////            try{u = jc.createUnmarshaller();}
////            catch(JAXBException e) {throw new Exception("Cannot creat instance from xml");}
////            return (MagitRepository) u.unmarshal(inputStream);
////    }
public static MagitRepository loadFromXml(String i_XmlPath) throws Exception {

    JAXBContext jc = JAXBContext.newInstance("generated");
    Unmarshaller u = jc.createUnmarshaller();
    StringReader sr = new StringReader(i_XmlPath);
    return (MagitRepository) u.unmarshal(sr);


}

       public void insertMembersToNewRepository(MagitRepository oldRepo)
       {
           //path = Paths.get(oldRepo.getLocation());
           repositoryName = oldRepo.getName();
       }

    //methods
    void Switch(Path newPath) { path = newPath; }

    public Branch getBranchByName(String name)// אמורים לקרוא לו גט ולא סט
    {
        Branch newBranch = null;
        for (Branch b : branches) {
            if (b.getBranchName().equals(name))
                newBranch = b;
        }
        return newBranch;

    }
    public Branch getBranchByCommit(Commit commit)// אמורים לקרוא לו גט ולא סט
    {
        Branch newBranch = null;
        for (Branch b : branches) {
            if (b.getPointedCommit() == commit)
                newBranch = b;
        }
        return newBranch;

    }

   void getRepositorysBranchesObjects(Path RepPath) {
       Path BranchesPath = Paths.get(RepPath.toString() + "\\.magit\\Branches");
       File[] allBranches = BranchesPath.toFile().listFiles();
       String fileContent;
       String headContent = null;

       for (File f : allBranches) {


           if (!(f.getName().equals("Head") || f.isDirectory())) {
               fileContent = GitManager.readTextFile(f.toString());
               this.branches.add(new Branch(f.getName(), fileContent, false, false));

           }
           if (f.isDirectory()) {
               String intro = this.repositoryRemoteName + "\\";

               Path remoteBranchesPath = Paths.get(RepPath.toString() + "\\.magit\\Branches\\" + f.getName());
               File[] allRBranches = remoteBranchesPath.toFile().listFiles();
               for (File t : allRBranches) {
                   fileContent = GitManager.readTextFile(t.toString());
                   Path p = t.toPath();
                   this.branches.add(new Branch(intro + p.getFileName(), fileContent, true, false));

               }
           }
           if(f.getName().equals("Head"))
           {
               headContent = GitManager.readTextFile(f.toString());
           }

       }
               this.getBranchByName(headContent).setRemoteTrackingBranch(true);
   }


    void getRemoteRepositoryBranchesObjects(Path repRemotePath) {
        Path BranchesPath = Paths.get(repRemotePath.toString() + "\\.magit\\Branches");
        File[] allBranches = BranchesPath.toFile().listFiles();
        String fileContent;
Branch b;
        for (File f : allBranches) {
            {
                if(!f.getName().equals("Head")) {
                    fileContent = GitManager.readTextFile(f.toString());
                    b = new Branch(this.repositoryRemoteName + "\\"+ f.getName(), fileContent,true,false);
                    this.branches.add(b);
                }
                else
                {
                    fileContent = GitManager.readTextFile(f.toString());
                    File remoteRepName = Paths.get(repRemotePath.toString() + "\\.magit\\Branches\\"+ fileContent).toFile();
                    String branchsha = GitManager.readTextFile(remoteRepName.toString());
                    b = new Branch(fileContent, branchsha,false,true);
                    this.branches.add(b);
            }
            }
        }

    }
    public void addCommitsToRepositoryMAp(Map<String, Commit> commitList)//add all commit to comitmap in repository
    {
        for (Map.Entry<String, Commit> stringCommitEntry : commitList.entrySet()) {
            Map.Entry thisEntry = (Map.Entry) stringCommitEntry;
            Commit c = (Commit) thisEntry.getValue();
            commitMap.put(c.getSHA(), c);
        }
    }
    public CommitRepresentative sha1ToCommit(String sha1)
    {
        return commitMap.get(sha1);
    }


    public static List<Commit> getSortedCommitList(Map<String, Commit> commitsMap) {
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
        return commitList;
    }
}


