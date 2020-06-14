package Classess;

import com.sun.xml.internal.ws.wsdl.writer.document.http.Binding;
import generated.MagitCommits;
import generated.MagitRepository;
import generated.MagitSingleCommit;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class GitManager {


    //members
    private Path dirPath;
    private UserManager UManager = new UserManager();

    public UserManager getUManager() {
        return UManager;
    }

    //    private class diffLogClass {
    private LinkedList<Path> updatedFiles = new LinkedList<>();
    private LinkedList<Path> createdFiles = new LinkedList<>();
    private LinkedList<Path> deletedFiles = new LinkedList<>();
    // }

    Map<String, Commit> commitTempMap;
    Map<String, Folder.Component> folderTempMap;
    Map<String, Folder.Component> blobTempMap;
    public HashMap<Conflict, Folder> conflictMap = new HashMap<>();

    public HashSet<PushRequest> getPRList() {
        return PRList;
    }

    HashSet<PushRequest> PRList = new HashSet<>();

    public GitManager() {
        //new File("c:\\magit-ex3").mkdirs();
        dirPath = Paths.get("c:\\magit-ex3");
    }
    //get\set
//    public void setGITRepository(Repository GITRepository) {
//        this.GITRepository = GITRepository;
//    }


    public void setDirPath(Path dirPath) {
        this.dirPath = dirPath;
    }

    public Path getDirPath() {
        return dirPath;
    }

    public LinkedList<Path> getUpdatedFiles() {
        return updatedFiles;
    }

    public LinkedList<Path> getCreatedFiles() {
        return createdFiles;
    }

    public LinkedList<Path> getDeletedFiles() {
        return deletedFiles;
    }

//    public Repository getGITRepository() {
//        return GITRepository;
//    }

//    public String getUserName() {
//        return userName;
//    }
//
//    public void updateNewUserNameInLogic(String NewUserName) {
//        userName = NewUserName;
//    }


    public static String generateSHA1FromString(String str) {
        return org.apache.commons.codec.digest.DigestUtils.sha1Hex(str);
    }


    //methods
    public LinkedList<String> getNotifications(User u) {
        LinkedList<String> list = new LinkedList<>();

        list.add(getnotString(u.getForknotificationToUser(), u));
//    u.getForknotificationToUser().clear();
        list.add(getnotString(u.getPRnotificationToUser(), u));
//    u.getPRnotificationToUser().clear();
        list.add(getnotString(u.getUpdatenotificationToUser(), u));
//    u.getUpdatenotificationToUser().clear();

        return list;
    }

    public LinkedList<String> getChangedCommits(PushRequest pr, Repository r) {
//    LinkedList<Commit> list = new LinkedList<>();
        Branch target = pr.getTargetBranch();
        Branch base = pr.getBasicBranch();
        Folder newF = target.getPointedCommit().getRootFolder();
        Folder oldF = base.getPointedCommit().getRootFolder();
        try {
            createShaAndZipForNewCommit(newF, oldF, false, r.getRepositoryPath(), pr.getRequsetedUser());
            LinkedList<String> list = new LinkedList<>();
            updateAddedList(list, createdFiles);
            updateUpdatedList(list, updatedFiles);
            updateDeletedList(list, deletedFiles);
            getCreatedFiles().clear();
            getDeletedFiles().clear();
            getUpdatedFiles().clear();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateUpdatedList(LinkedList<String> list, LinkedList<Path> Files) {
        for (Path p : Files) {
            list.add(p.toString() + " (Updated)");
        }
    }

    public void updateAddedList(LinkedList<String> list, LinkedList<Path> Files) {
        for (Path p : Files) {
            list.add(p.toString() + " (Added)");
        }
    }

    public void updateDeletedList(LinkedList<String> list, LinkedList<Path> Files) {
        for (Path p : Files) {
            list.add(p.toString() + " (Deleted)");
        }
    }

    //public  void getAllChangedFiles(Branch target,Branch base,Repository r,User u)
//{
////    for(Commit c :commitList)
////    {
//        Folder newF = c.getRootFolder();
//        Folder oldF = r.getCommitMap().get(c.getSHA1PreveiousCommit()).getRootFolder();
//        try {
//            createShaAndZipForNewCommit(newF,oldF,false,r.getRepositoryPath(),u);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////    }
//}
    private String getnotString(LinkedList<Notification> notification, User u) {
        StringBuilder b = new StringBuilder();
        for (Notification n : notification) {
            if (n.getUserToShowTo() == u) {
                b.append(n.toString());
                b.append(System.lineSeparator());
                notification.remove(n);
            }
        }
        return b.toString();
    }


    public String ExecuteCommit(String description, Boolean isCreateZip, User u) throws Exception {  //V
        deletedFiles.clear();
        updatedFiles.clear();
        this.createdFiles.clear();
        Path ObjectPath = Paths.get(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Objects");
        Path BranchesPath = Paths.get(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Branches");
        String headBranch = readTextFile(BranchesPath + "\\Head");
        String prevCommitSHA1 = readTextFile(BranchesPath + "\\" + headBranch);//לזה נעשה אןזיפ וגם לקובץ שהשם שלו הוא הsha1 שכתוב פה
        //Date
        //String creationDate = GitManager.
        // getDate();

        Folder newFolder = GenerateFolderFromWC(u.getCurRep().getRepositoryPath(), u);// ייצג את הספרייה הראשית
        Folder oldFolder = u.getCurRep().getHeadBranch().getPointedCommit().getRootFolder();
        if (!generateSHA1FromString(newFolder.getFolderContentString()).equals(generateSHA1FromString(oldFolder.getFolderContentString()))) {
            createShaAndZipForNewCommit(newFolder, oldFolder, isCreateZip, u.getCurRep().getRepositoryPath(), u);


            if (isCreateZip) {
                Commit c = new Commit(description, u);
//                String anotherPrev = GITRepository.getHeadBranch().getPointedCommit().getSHA1PreveiousCommit();//האם יש עוד אבא
//                if (anotherPrev != null) {
//                    c.setSHA1anotherPreveiousCommit(anotherPrev);
//                }
                u.getCurRep().getHeadBranch().setPointedCommit(c); //creation
                u.getCurRep().getHeadBranch().getPointedCommit().setSHA1PreveiousCommit(prevCommitSHA1); //setting old commits sha1
                u.getCurRep().getHeadBranch().getPointedCommit().setRootFolder(newFolder); //setting old commit
                u.getCurRep().getHeadBranch().getPointedCommit().setRootFolderSHA1(generateSHA1FromString(newFolder.getFolderContentString()));
                u.getCurRep().getHeadBranch().getPointedCommit().setCommitFileContentToSHA(); //
                u.getCurRep().getCommitList().put(u.getCurRep().getHeadBranch().getPointedCommit().getSHA(), u.getCurRep().getHeadBranch().getPointedCommit()); //adding to commits list of the current reposetory
                createFile(u.getCurRep().getHeadBranch().getBranchName(), u.getCurRep().getHeadBranch().getPointedCommit().getSHA(), BranchesPath, new Date().getTime());
                u.getCurRep().getHeadBranch().setPointedCommitSHA1(c.getSHA());
                createZipFile(ObjectPath, generateSHA1FromString(newFolder.getFolderContentString()), newFolder.getFolderContentString());

                createFileInMagit(u.getCurRep().getHeadBranch().getPointedCommit(), u.getCurRep().getRepositoryPath(), u.getCurRep());


                createFileInMagit(u.getCurRep().getHeadBranch().getPointedCommit(), u.getCurRep().getRepositoryPath(), u.getCurRep());

            }
        } else {
            return "No changes in WC!";
        }
        return "Commit Was successfully done";
    }

    public void createShaAndZipForNewCommit(Folder newFolder, Folder oldFolder, Boolean isCreateZip, Path path, User u) throws IOException { //V
        ArrayList<Folder.Component> newComponents = new ArrayList<>();
        ArrayList<Folder.Component> oldComponents = new ArrayList<>();
        Path objectPath = Paths.get(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Objects");
        int oldd = 0;
        int neww = 0;

        if ((oldFolder != null) && (newFolder != null)) {
            oldComponents = oldFolder.getComponents();
            newComponents = newFolder.getComponents();
            if (!oldComponents.isEmpty() && !newComponents.isEmpty()) {

// indexes of the component in the lists
                while (oldd < oldComponents.size() && neww < newComponents.size()) { // while two folders are not empty
                    if (oldComponents.get(oldd).getComponentName().equals(newComponents.get(neww).getComponentName())) { // if names are the same
                        if (oldComponents.get(oldd).getComponentSHA1().equals(newComponents.get(neww).getComponentSHA1())) { //if sha1 is the same
                            //point old object
                            newComponents.set(neww, oldComponents.get(oldd)); // if nothing changed, point at the original tree
                            neww++;
                            oldd++;
                        } else if (oldComponents.get(oldd).getComponentType().equals(newComponents.get(neww).getComponentType())) { //different sha1, updated file
                            if (oldComponents.get(oldd).getComponentType().equals(FolderType.Folder)) {
                                Folder newf = (Folder) newComponents.get(neww).getDirectObject();
                                Folder oldf = (Folder) oldComponents.get(oldd).getDirectObject();

                                createShaAndZipForNewCommit(newf, oldf, isCreateZip, Paths.get(path.toString() + "\\" + oldComponents.get(oldd).getComponentName()), u);
                                String newSHA = generateSHA1FromString(newf.getFolderContentString());
                                String oldSHA = generateSHA1FromString(oldf.getFolderContentString());

                                if (newSHA.equals(oldSHA)) { //if sha1 is the same
                                    newComponents.set(neww, oldComponents.get(oldd)); // if nothing changed, point at the original tree
                                }
                                if (isCreateZip == Boolean.TRUE) {
                                    createZipFile(objectPath, generateSHA1FromString(newf.getFolderContentString()), newf.getFolderContentString());
                                }
                                neww++;
                                oldd++;
                            } else {
                                //both blob - updated
                                if (isCreateZip == Boolean.TRUE) {
                                    File f = new File(objectPath.toString() + "\\" + newComponents.get(neww).getComponentSHA1() + ".zip");
                                    if (!f.exists()) {
                                        Blob b = (Blob) newComponents.get(neww).getDirectObject();
                                        createZipFile(objectPath, newComponents.get(neww).getComponentSHA1(), b.getContent());
                                        //add updated file zip
                                    }
                                }
                                //add to path
                                this.updatedFiles.add(Paths.get(path.toString() + "\\" + newComponents.get(neww).getComponentName()));
                                neww++;
                                oldd++;
                            }
                        }
                    } else {
                        int result = newComponents.get(neww).getComponentName().compareTo(oldComponents.get(oldd).getComponentName());
                        if (result > 0) {
                            //file was deleted from old
                            //add to list
                            if (oldComponents.get(oldd).getComponentType().equals(FolderType.Folder)) {
                                Folder f = (Folder) oldComponents.get(oldd).getDirectObject();

                                createShaAndZipForNewCommit(null, f, isCreateZip, Paths.get(path.toString() + "\\" + oldComponents.get(oldd).getComponentName()), u);
                            }
                            this.deletedFiles.add(Paths.get(path.toString() + "\\" + oldComponents.get(oldd).getComponentName()));
                            oldd++;

                        } else {
                            //new file was added
                            //add new zip
                            //createZipFile(path,newComponents.get(neww).getComponentSHA1(),newComponents.get(neww).);

                            //add to list
                            if (newComponents.get(neww).getComponentType().equals(FolderType.Blob)) {
                                if (isCreateZip == Boolean.TRUE) {
                                    File f = new File(objectPath.toString() + "\\" + newComponents.get(neww).getComponentSHA1() + ".zip");
                                    if (!f.exists()) {
                                        Blob b = (Blob) newComponents.get(neww).getDirectObject();
                                        createZipFile(objectPath, newComponents.get(neww).getComponentSHA1(), b.getContent());
                                    }
                                }
                            } else {
                                Folder f = (Folder) newComponents.get(neww).getDirectObject();

                                //Folder f = new Folder(newComponents.get(neww));
                                createShaAndZipForNewCommit(f, null, isCreateZip, Paths.get(path.toString() + "\\" + newComponents.get(neww).getComponentName()), u);
                                if (isCreateZip == Boolean.TRUE) {

                                    createZipFile(objectPath, generateSHA1FromString(f.getFolderContentString()), f.getFolderContentString());
                                }
                            }
                            this.createdFiles.add(Paths.get(path.toString() + "\\" + newComponents.get(neww).getComponentName()));
                            neww++;
                        }
                    }
                }
            }
        }

        if (oldFolder != null) {
            oldComponents = oldFolder.getComponents();
            while (oldd < oldComponents.size()) {
                if (oldComponents.get(oldd).getComponentType().equals(FolderType.Folder)) {
                    Folder f = (Folder) oldComponents.get(oldd).getDirectObject();

                    //Folder f = new Folder(newComponents.get(neww));
                    createShaAndZipForNewCommit(null, f, isCreateZip, Paths.get(path.toString() + "\\" + oldComponents.get(oldd).getComponentName()), u);
                }
                this.deletedFiles.add(Paths.get(path.toString() + "\\" + oldComponents.get(oldd).getComponentName()));
                oldd++;
            }
        }

        if (newFolder != null) {
            newComponents = newFolder.getComponents();
            while (neww < newComponents.size()) {
                if (newComponents.get(neww).getComponentType().equals(FolderType.Blob)) {
                    if (isCreateZip == Boolean.TRUE) {
                        File f = new File(objectPath.toString() + "\\" + newComponents.get(neww).getComponentSHA1() + ".zip");
                        if (!f.exists()) {
                            Blob b = (Blob) newComponents.get(neww).getDirectObject();
                            createZipFile(objectPath, newComponents.get(neww).getComponentSHA1(), b.getContent());
                        }
                    }
                } else {
                    Folder f = (Folder) newComponents.get(neww).getDirectObject();

                    //Folder f = new Folder(newComponents.get(neww).getDirectObject().);
                    createShaAndZipForNewCommit(f, null, isCreateZip, Paths.get(path.toString() + "\\" + newComponents.get(neww).getComponentName()), u);
                    if (isCreateZip == Boolean.TRUE) {

                        createZipFile(objectPath, generateSHA1FromString(f.getFolderContentString()), f.getFolderContentString());
                    }
                }
                this.createdFiles.add(Paths.get(path.toString() + "\\" + newComponents.get(neww).getComponentName()));
                neww++;
            }
        }
    }

    public Folder GenerateFolderFromWC(Path currentPath, User u) {
        File[] allFileComponents = currentPath.toFile().listFiles();
        String sh1Hex = "";
        String fileContent = "";
        String objectsPath = currentPath + "\\Objects";
        Folder currentFolder = new Folder();

        for (File f : allFileComponents) {
            if (!f.getName().equals(".magit")) {
                if (!f.isDirectory()) {
                    fileContent = readTextFile(f.toString());
                    sh1Hex = generateSHA1FromString((fileContent));
                    //לוגית יוצרת את האובייקט שהוא קומפוננט שמתאר בלוב
                    Folder.Component newComponent = new Folder.Component(f.getName(), sh1Hex, FolderType.Blob, u.getName(), getDateFromObject(f.lastModified()));
                    newComponent.setDirectObject(new Blob(fileContent));
                    currentFolder.getComponents().add(newComponent);

                } else {
                    Folder folder = GenerateFolderFromWC(Paths.get(f.getPath()), u);
                    Collections.sort(folder.getComponents());

                    sh1Hex = generateSHA1FromString(folder.stringComponentsToString());

                    Folder.Component newComponent = new Folder.Component(f.getName(), sh1Hex, FolderType.Folder, u.getName(), getDateFromObject(f.lastModified()));
                    newComponent.setDirectObject(folder);
                    currentFolder.getComponents().add(newComponent);
                }
            }
        }
        Collections.sort(currentFolder.getComponents());
        return currentFolder;
    }

    public void CreatBranch(String newBranchName, User u) {
        Path pathOfNewFile = Paths.get(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\");
        String nameOfBranch = readTextFile(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\Head");//name of main branch
        String sha1OfCurrCommit = readTextFile(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\" + nameOfBranch);//sha1 of main commit
        createFile(newBranchName, sha1OfCurrCommit, pathOfNewFile, new Date().getTime());// a file created in branches

        Branch newBranch = new Branch(newBranchName, u.getCurRep().getHeadBranch().getPointedCommit().getSHA(), false, false);
        newBranch.setPointedCommit(u.getCurRep().getHeadBranch().getPointedCommit());//creating and initialising

        u.getCurRep().getBranches().add(newBranch);//adding to logic//not good

    }

    public String DeleteBranch(String FileName, User u, User RRu) throws Exception { //V
        if (FileName.equals(readTextFile(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\Head")))
            return "The Branch is Head!";
        String intro = u.getCurRep().getRepositoryRemoteName() + "\\";
        Branch branch = u.getCurRep().getBranchByName(FileName);
        if(branch == null)
        {
            return "No such branch exist";
        }
        if (!branch.getRemoteTrackingBranch()) {
            File file = new File(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\" + FileName);
            file.delete();//// erasing it physically

            u.getCurRep().getBranches().remove(branch);// erasing it logically
        } else {
            File LRRfile = new File(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\" + intro + FileName);
            File RRfile = new File(u.getCurRep().getRepositoryRemotePath() + "\\" + ".magit\\branches\\" + FileName);
            File LRfile = new File(u.getCurRep().getRepositoryPath().toString() + "\\" + ".magit\\branches\\" + FileName);
            LRfile.delete();
            RRfile.delete();
            LRRfile.delete();

            u.getCurRep().getBranches().remove(branch);// erasing it logically
            Branch LRB = u.getCurRep().getBranchByName(intro + FileName);
            u.getCurRep().getBranches().remove(LRB);// erasing it logically
            Branch b = RRu.getUserRepo(u.getCurRep().getRepositoryName()).getBranchByName(FileName);
            RRu.getUserRepo(u.getCurRep().getRepositoryName()).getBranches().remove(b);
            Notification n = new Notification();
            n.setUserToShowTo(RRu);
            n.setDateOfNotification(GitManager.getDateFromObject(new Date()));
            n.setMessage("Branch " + FileName + " Was deleted by " + u.getName());
            n.setMessageToShow(n.getMessage());
            RRu.getUpdatenotificationToUser().add(n);
        }
                return "";
    }

    public void createEmptyRepositoryFolders(String repPath, String repName, User u) throws Exception {//V
        if (repPath.substring(repPath.length() - 1) != "/") {
            repPath += "\\";
        }

        new File(repPath + "\\.magit\\objects").mkdirs();
        new File(repPath + "\\.magit\\branches").mkdirs();
        Path workingPath = Paths.get(repPath + "\\");
        Repository rep = new Repository(workingPath, new Branch("Master"), repName, u);
        u.setCurRep(rep);
        u.getRepositories().add(rep);
        u.getCurRep().getHeadBranch().setPointedCommit(new Commit());
        //GITRepository.getHeadBranch().getPointedCommit().setRootfolder(workingPath.toString());
        u.getCurRep().getHeadBranch().getPointedCommit().setCommitFileContentToSHA();
        u.getCurRep().getHeadBranch().setPointedCommitSHA1(u.getCurRep().getHeadBranch().getPointedCommit().getSHA());
        u.getCurRep().getCommitMap().put(u.getCurRep().getHeadBranch().getPointedCommit().getSHA(), u.getCurRep().getHeadBranch().getPointedCommit());
        //Create commit file

        createFileInMagit(u.getCurRep().getHeadBranch().getPointedCommit(), workingPath, u.getCurRep());//commit
        createFileInMagit(u.getCurRep().getHeadBranch(), workingPath, u.getCurRep());

        createFile("Head", "Master", Paths.get(repPath + "\\.magit\\branches"), new Date().getTime());
        createFile("RepName", u.getCurRep().getRepositoryName(), Paths.get(repPath + "\\.magit"), new Date().getTime());
        createFile("RemoteRepName", "", Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit"), new Date().getTime());
        createFile("RemoteRepPath", "", Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit"), new Date().getTime());


        u.getCurRep().getBranchByName("Master").setPointedCommit(u.getCurRep().getHeadBranch().getPointedCommit());

//            //create origcommit
        Folder folder = GenerateFolderFromWC(u.getCurRep().getRepositoryPath(), u);
        u.getCurRep().getHeadBranch().getPointedCommit().setRootFolder(folder);
        u.setName("Administrator");
        u.getCurRep().getHeadBranch().getPointedCommit().setRootFolder(folder);
    }

    private boolean isFileMagit(String repPath) {
        return Files.exists(Paths.get(repPath));
    }

//    public void switchRepository(Path newRepPath,User u) throws IOException, IllegalArgumentException //V
//    {
//        File f = Paths.get(newRepPath.toString() + "\\.magit\\branches\\Head").toFile();//הקובץ הד
//        String content = readTextFile(newRepPath + "\\.magit\\branches\\" + f.getName());
//        //String name = readTextFile(newRepPath + "\\.magit\\branches\\" + content);
//        //this.GITRepository = new Repository(newRepPath,u);
//        Repository rep = new Repository(newRepPath,u);
//        u.setCurRep(rep);
//        u.getRepositories().add(rep);
//
//        File name = Paths.get(newRepPath.toString() + "\\.magit\\RepName").toFile();
//        String repname = readTextFile(name.getPath());
//        u.getCurRep().setRepositoryName(repname);
//        File remoteName = Paths.get(newRepPath.toString() + "\\.magit\\RemoteRepName").toFile();
//        if (remoteName.exists()) {
//            String remoteRepName = readTextFile(remoteName.toString());
//            u.getCurRep().setRepositoryRemoteName(remoteRepName);
//            File remotePath = Paths.get(newRepPath.toString() + "\\.magit\\RemoteRepPath").toFile();
//            u.getCurRep().setRepositoryRemotePath(readTextFile((remotePath.toString())));
//        }
//        if (u.getCurRep().getRepositoryRemoteName() != null)
//            new File(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Branches\\" + u.getCurRep().getRepositoryRemoteName()).mkdirs();
//
//        u.getCurRep().getRepositorysBranchesObjects(u.getCurRep().getRepositoryPath());
//        //setBranchesProp();
//        u.getCurRep().Switch(newRepPath);
//        u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(content));
//
//
//        getCommitForBranches(newRepPath.toString(),u);
//    }

    public void setBranchesProp(User u) {
        for (Branch b : u.getCurRep().getBranches()) {
            String s = u.getCurRep().getRepositoryRemoteName() + "\\";
            Branch RB = u.getCurRep().getBranchByName(s + b.getBranchName());
            if (RB != null) {
                b.setRemoteTrackingBranch(true);
            }
        }
    }

    public void createPRFile(Repository r, PushRequest p) {
        //
        String path = r.getRepositoryRemotePath() + "\\.magit\\PR" + p.getBasicBranch().getBranchName() + p.getTargetBranch().getBranchName();
        File f = new File(path);
        f.mkdirs();

    }

    public void CloneRepository(String pathRemoteRep, String pathNewRep, String repName, User u, User forkU, UserManager UM) throws Exception {
        Path clonedRep = Paths.get(pathNewRep + "\\" + repName);
        Path origRep = Paths.get(pathRemoteRep + "\\" + repName);
        File f = Paths.get(origRep + "\\.magit\\branches\\Head").toFile();//הקובץ הד
        String headBranch = readTextFile(origRep + "\\.magit\\branches\\" + f.getName());
        // this.GITRepository = new Repository(Paths.get(pathNewRep),u);

        Repository rep = new Repository(clonedRep, forkU);
        //u.setCurRep(rep);
        u.getRepositories().add(rep);
        u.setCurRep(rep);
        //repnames
//        File remoteRepName = Paths.get(origRep + "\\.magit\\RepName").toFile();
//        String remoteName = readTextFile(origRep + "\\.magit\\" + remoteRepName.getName());
        u.getCurRep().setRepositoryRemoteName(repName);
        u.getCurRep().setRepositoryName(repName);
        u.getCurRep().setRepositoryRemotePath(origRep.toString());
        //ctreate logic
        u.getCurRep().getRemoteRepositoryBranchesObjects(origRep);
        u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(headBranch));
        copyObjectsFiles(origRep.toString(), clonedRep.toString());
        getCommitForBranches(clonedRep.toString(), u, UM);


        //WC
        createMagitFiles(false, u);

        createFilesInWCFromCommitObject(u.getCurRep().getHeadBranch().getPointedCommit().getRootFolder(), u.getCurRep().getRepositoryPath());
        createFile("RepName", u.getCurRep().getRepositoryName(), Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit"), new Date().getTime());
        createFile("RemoteRepName", u.getCurRep().getRepositoryRemoteName(), Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit"), new Date().getTime());
        createFile("RemoteRepPath", u.getCurRep().getRepositoryRemotePath(), Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit"), new Date().getTime());

    }

    public void copyObjectsFiles(String pathRemoteRep, String pathNewRep) {
        String BString = "\\.magit\\Objects\\";
        File RemoteRep = new File(pathRemoteRep + BString);

        File NewRep = new File(pathNewRep + BString);

        try {
            FileUtils.copyDirectory(RemoteRep, NewRep);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean doesPathExist(String path) {
        return Files.exists(Paths.get(path));
    }


    /*
        public void switchRepository(Path newRepPath)
            throws ExceptionInInitializerError, UnsupportedOperationException, IllegalArgumentException, IOException {
        Path checkIfMagit = Paths.get(newRepPath + "\\.magit");
        if (Files.exists(newRepPath)) {
            if (Files.exists(checkIfMagit)) {

                File f = Paths.get(newRepPath.toString() + "\\.magit\\branches\\Head").toFile();
                String content = readTextFile(newRepPath + "\\.magit\\branches\\" + f.getName());
                String name = readTextFile(newRepPath + "\\.magit\\branches\\" + content);
                this.GITRepository = new Repository(newRepPath);

                this.GITRepository.getRepositorysBranchesObjecets();
                GITRepository.Switch(newRepPath);
                GITRepository.setHeadBranch(GITRepository.getBranchByName(content));


                getCommitForBranches(newRepPath);
            } else throw new ExceptionInInitializerError();//exeption forG not being magit

        } else throw new IllegalArgumentException();//exception for not existing

    }
     */

    public void getCommitForBranches(String repPath, User u, UserManager UM) throws IOException, IllegalArgumentException { //V
        Folder folder = null;
        for (Branch b : u.getCurRep().getBranches()) {
            //try{
            //if(b.getPointedCommit() == null) {
            updateCommit(b, repPath, u, UM);
//            Commit newCommit = createCommitRec(b.getPointedCommitSHA1(), repPath);
//            newCommit.setCommitFileContentToSHA();
//            getGITRepository().getCommitMap().put(newCommit.getSHA(), newCommit);
//            b.setPointedCommit(newCommit);
        }
    }

    public void updateCommit(Branch b, String repPath, User u, UserManager UM) throws IOException {
        Commit newCommit = createCommitRec(b.getPointedCommitSHA1(), repPath, u, UM);
        //newCommit.setCommitFileContentToSHA();
        u.getCurRep().getCommitMap().put(newCommit.getSHA(), newCommit);
        b.setPointedCommit(newCommit);
    }
    // }
    //}
    //catch(IOException e){}
//
//            Path commitPath = Paths.get(getGITRepository().getRepositoryPath().toString() + "\\.magit\\objects\\" + b.getPointedCommitSHA1() + ".zip");
//            try {
//
//                commitContent = extractZipFile(commitPath);//5 השורות
//            } catch (IOException e) {
//                throw new IOException();
//            }// opening zip file failed
//            BufferedReader br = new BufferedReader(new StringReader(commitContent));
//            ArrayList<String> st = new ArrayList<>();
//            String a;
//            int i = 0;
//            while ((a = br.readLine()) != null) {
//                if (a.equals("null")) {
//                    st.add(i, null);
//                } else {
//                    st.add(i, a);
//                }
//                i++;
//            }
//            Commit newCommit = new Commit(st);//--זה יקרה כבר בתוך הרקורסיה של לבנות את המפת קומיטים השלמה
////כאן

    //GITRepository.getRepositoryName() = ךהחליף שם של רפוסיטורי
    //לא יצרנו קומיט שההד יצביע עליו כי אין צורך
//            try {
//                folder = generateFolderFromCommitObject(newCommit.getRootFolderSHA1());
//            } catch (IOException er) {
//                throw new IllegalArgumentException();
//            }// was unable to generateFolderFromCommitObject
//            b.getPointedCommit().setRootFolder(folder);
    //newCommit.setCommitFileContentToSHA();
    //br.close();
    //GITRepository.getCommitList().put(newCommit.getSHA(), newCommit);
    //מיותר כבר הוספנו למעלה


    public Commit createCommitRec(String sha1, String repPath, User u, UserManager UM) throws IOException {
        String commitContent;
        Path commitPath = Paths.get(repPath + "\\.magit\\objects\\" + sha1 + ".zip");
        try {
            commitContent = extractZipFile(commitPath);//5 השורות
        } catch (IOException e) {
            throw new IOException();
        }// opening zip file failed
        BufferedReader br = new BufferedReader(new StringReader(commitContent));
        ArrayList<String> st = new ArrayList<>();
        String a;
        int i = 0;
        while ((a = br.readLine()) != null) {
            if (a.equals("null") || a == null) {
                st.add(i, null);
            } else {
                st.add(i, a);
            }
            i++;
        }
        br.close();

        Commit newCommit = new Commit(st);
        if(UM.getUser(st.get(5)) != null)
        {
            newCommit.setChanger(UM.getUser(st.get(5)));

        }
        else{
            User user = new User(st.get(5));
            newCommit.setChanger(user);

        }
        try {
            newCommit.setRootFolder(generateFolderFromCommitObject(newCommit.getRootFolderSHA1(), repPath));
        } catch (IOException er) {
            throw new IllegalArgumentException();
        }// was unable to generateFolderFromCommitObject
        newCommit.setSHA1(sha1);

        //הוספה של הקומיט למאפ אם לא קיים:
        if (u.getCurRep().getCommitMap().get(newCommit.getSHA()) == null) {
            u.getCurRep().getCommitMap().put(newCommit.getSHA(), newCommit);
        }

        //קריאות רקורסיביות:


        if (newCommit.getSHA1PreveiousCommit() != null)//father #1
        {
            createCommitRec(newCommit.getSHA1PreveiousCommit(), repPath, u, UM);
        }

        if (newCommit.getSHA1anotherPreveiousCommit() != null)//father #2
        {
            createCommitRec(newCommit.getSHA1anotherPreveiousCommit(), repPath, u, UM);
        }

        return newCommit;

    }

    public Map<String, Commit> createCommitRecOnlyForHead(Map<String, Commit> list, String sha1Headcommit, String repPath, User u,UserManager UM) throws IOException {
        String commitContent;
        //Map<String,Commit> list = new HashMap <String, Commit>();
        Path commitPath = Paths.get(repPath + "\\.magit\\objects\\" + sha1Headcommit + ".zip");
        try {
            commitContent = extractZipFile(commitPath);//5 השורות
        } catch (IOException e) {
            throw new IOException();
        }// opening zip file failed
        BufferedReader br = new BufferedReader(new StringReader(commitContent));
        ArrayList<String> st = new ArrayList<>();
        String a;
        int i = 0;
        while ((a = br.readLine()) != null) {
            if (a.equals("null") || a == null) {
                st.add(i, null);
            } else {
                st.add(i, a);
            }
            i++;
        }
        br.close();

        Commit newCommit = new Commit(st);
        if(UM.getUser(st.get(5)) != null)
        {
            newCommit.setChanger(UM.getUser(st.get(5)));

        }
        else{
            User user = new User(st.get(5));
            newCommit.setChanger(user);

        }        try {
            newCommit.setRootFolder(generateFolderFromCommitObject(newCommit.getRootFolderSHA1(), repPath));
        } catch (IOException er) {
            throw new IllegalArgumentException();
        }// was unable to generateFolderFromCommitObject
        newCommit.setSHA1(sha1Headcommit);

        //הוספה של הקומיט למאפ אם לא קיים:
        if (list.get(newCommit.getSHA()) == null) {
            list.put(newCommit.getSHA(), newCommit);
        }

        //קריאות רקורסיביות:


        if (newCommit.getSHA1PreveiousCommit() != null)//father #1
        {
            createCommitRecOnlyForHead(list, newCommit.getSHA1PreveiousCommit(), repPath, u,UM);
        }

        if (newCommit.getSHA1anotherPreveiousCommit() != null)//father #2
        {
            createCommitRecOnlyForHead(list, newCommit.getSHA1anotherPreveiousCommit(), repPath, u,UM);
        }

        return list;

    }

    public void CreatBranchToCommit(String newBranchName, Commit commit) {
        Branch b = new Branch(newBranchName);
        b.setPointedCommit(commit);
        b.setPointedCommitSHA1(commit.getSHA());
    }


    public Folder generateFolderFromCommitObject(String rootFolderName, String path) throws IOException {//V
        Path ObjectPath = Paths.get(path + "\\.magit\\Objects");
        String folderContent;
        Folder currentFolder;
        try {
            folderContent = extractZipFile(Paths.get(ObjectPath + "\\" + rootFolderName + ".zip"));
        } catch (IOException e) {
            return new Folder();
        }

        currentFolder = new Folder();
        currentFolder.setComponents(currentFolder.setComponentsFromString(folderContent));

        for (Folder.Component c : currentFolder.getComponents()) {

            if (c.getComponentType().equals(FolderType.Blob)) {
                String blocContent = extractZipFile(Paths.get(ObjectPath + "\\" + c.getComponentSHA1() + ".zip"));
                Blob b = new Blob(blocContent);
                c.setDirectObject(b);
            } else {
                Folder folder = generateFolderFromCommitObject(c.getComponentSHA1(), path);
                Collections.sort(folder.getComponents());
                c.setDirectObject(folder);
            }
        }
        return currentFolder;
    }


    public static void createFileInMagit(Object obj, Path path, Repository r) throws Exception {//V
        Path magitPath = Paths.get(path.toString() + "\\.magit");
        Path objectsPath = Paths.get(magitPath.toString() + "\\objects");
        Path branchesPath = Paths.get(magitPath.toString() + "\\branches");

        if (obj instanceof Commit) {
            createCommitZip((Commit) obj, objectsPath);
        } else if (obj instanceof Branch) {
            Branch branch = (Branch) obj;
            if (branch.getRemoteBranch() == true) {
                createFile(branch.getBranchName(), branch.getPointedCommit().getSHA(), branchesPath, new Date().getTime());
            } else {
                createFile(branch.getBranchName(), branch.getPointedCommit().getSHA(), branchesPath, new Date().getTime());
            }
        } else if (obj instanceof Folder) {
            createFolderZip((Folder) obj, objectsPath);
        } else if (obj instanceof Blob) {
            createBlobZip((Blob) obj, objectsPath);
        } else throw new Exception();

    }

    private static void createCommitZip(Commit commit, Path path) throws IOException {//V

        createZipFile(path, commit.getSHA(), commit.getSHAContent());
    }


    private static void createFolderZip(Folder folder, Path path) throws IOException {//V
        String content = folder.stringComponentsToString();
        String SHA = generateSHA1FromString(content);

        createZipFile(path, SHA, content);
    }

    private static void createBlobZip(Blob blob, Path path) throws IOException {//V
        String content = blob.getContent();
        String SHA = generateSHA1FromString(content);

        createZipFile(path, SHA, content);
    }

    public static String extractZipFile(Path path) throws IOException {//V
        ZipFile zip = new ZipFile(path.toString());
        ZipEntry entry = zip.entries().nextElement();
        StringBuilder out = getTxtFiles(zip.getInputStream(entry));
        zip.close();

        return out.toString();
    }

    private static StringBuilder getTxtFiles(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        reader.close();
        return out;
    }

    private static void createZipFile(Path path, String fileName, String fileContent) throws IOException {
        File f = new File(path + "\\" + fileName + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
        ZipEntry e = new ZipEntry(fileName);
        out.putNextEntry(e);

        byte[] data = fileContent.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();
    }

    public static void createFile(String fileName, String fileContent, Path path, long date) { // gets a name for new file,what to right inside, where to put it
        Writer out = null;

        File file = new File(path + "\\" + fileName);
        try {
            out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file)));
            out.write(fileContent);
            out.close();
            file.setLastModified(date);
        } catch (IOException e) {
            e.fillInStackTrace();
        }

    }

    public String getAllBranches(User u) {
        StringBuilder toReturn = new StringBuilder();
        for (Branch b : u.getCurRep().getBranches()) {
            if (u.getCurRep().getHeadBranch().getBranchName().equals(b.getBranchName())) {
                toReturn.append("Head");
                toReturn.append(System.lineSeparator());

            }
            toReturn.append("Branch name: ");
            toReturn.append(b.getBranchName());
            toReturn.append(System.lineSeparator());
            toReturn.append(b.getPointedCommitSHA1());
            toReturn.append(System.lineSeparator());
            toReturn.append(b.getPointedCommit().getDescription());
            toReturn.append(System.lineSeparator());
        }
        return toReturn.toString();
    }


    public String ShowHistoryActiveBranch(User u) throws Exception {//V
        String sha1OfMainBranch = u.getCurRep().getHeadBranch().getPointedCommit().getSHA();
        return ShowHistoryActiveBranchRec(sha1OfMainBranch, u);
    }


    public String ShowHistoryActiveBranchRec(String sha1OfMainBranch, User u) throws Exception {//V

        StringBuilder sb = new StringBuilder();
        Commit commit = u.getCurRep().getCommitList().get(sha1OfMainBranch);
        if (commit == null) {
            //get commit from the files
            commit = getCommitFromSha1UsingFiles(u.getCurRep().getRepositoryPath().toString(), sha1OfMainBranch, u);
            //add it to the list of commits (only a part of its fields are there
            u.getCurRep().getCommitList().put(sha1OfMainBranch, commit);
        }
        //either way now have a commit in my hands

        sb.append(commit.getSHA());
        sb.append(System.lineSeparator());
        sb.append(commit.getDescription());
        sb.append(System.lineSeparator());
        sb.append(commit.getCreationDate());//date
        sb.append(System.lineSeparator());
        sb.append(commit.getChanger());
        sb.append(System.lineSeparator());
        if (commit.getSHA1PreveiousCommit() == null)// difault commit
        {
            return sb.toString();
        }
        //
        return sb.toString() + System.lineSeparator() + ShowHistoryActiveBranchRec(commit.getSHA1PreveiousCommit(), u);
    }

    public void ShowStatus(User u) throws Exception {

        StringBuilder sb = new StringBuilder();

        sb.append("The current status of WC is:\n");
        sb.append(System.lineSeparator());


        if (u.getCurRep() != null) {
            sb.append("Repository's Name:" + u.getCurRep().getRepositoryName() + "\n"
                    + "Repository's Path:" + u.getCurRep().getRepositoryPath().toString() + "\n" +
                    "Repository's User:" + u.getName() + "\n");
            ExecuteCommit("", false, u);
            sb.append(System.lineSeparator());
            sb.append("Deleted Files's Paths:" + getDeletedFiles() + '\n');
            sb.append("Added Files's Paths:" + getCreatedFiles() + '\n');
            sb.append("Updated Files's Paths:" + getUpdatedFiles() + '\n');
            getCreatedFiles().clear();
            getDeletedFiles().clear();
            getUpdatedFiles().clear();


        }

    }


    public Commit getCommitFromSha1UsingFiles(String path, String sha1, User u) throws Exception {//V

        //read the file in folder objects that its name is sha1
        //take all i need to a commit
        // return it
        Path commitPath = Paths.get(path.toString() + "\\.magit\\objects\\" + sha1 + ".zip");
        String commitContent = extractZipFile(commitPath);
        BufferedReader br = new BufferedReader(new StringReader(commitContent));
        ArrayList<String> st = new ArrayList<>();
        String a;
        int i = 0;
        while ((a = br.readLine()) != null) {
            if (a.equals("null")) {
                st.add(i, null);
            } else {
                st.add(i, a);
            }
            i++;
        }

        br.close();
        Commit newCommit = new Commit(st);
        newCommit.setChanger(u);
        newCommit.setSHAContent(commitContent);
        return newCommit;
    }


    public static String readTextFile(String filePath) { //V
        File f = new File(filePath);
        StringBuilder content = new StringBuilder();

        try (Stream<String> stream = Files.lines(Paths.get(f.getPath()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> content.append(s).append("\n"));
            if (content.length() > 0) {
                content.deleteCharAt(content.length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return content.toString();
    }


    public static String getDateFromObject(Object date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        if (date != null) {
            return dateFormat.format(date);
        }
        return dateFormat.format(new Date());
    }

    public static long getDateFromString(String date) throws Exception {
        long returnedDate;
//        Date date1=new SimpleDateFormat("dd.MM.YYYY - hh:mm:ss:sss").parse(date);//////////////ענביתתתתתתת
//        return date1;
//long fj = date.to
        DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        try {
            returnedDate = sdf.parse(date).getTime();
        } catch (ParseException e) {
            throw new Exception("Invalid date structure");
        }
        return returnedDate;
    }

    public static Date getDateObjectFromString(String date) throws Exception {
        Date returnedDate;
//        Date date1=new SimpleDateFormat("dd.MM.YYYY - hh:mm:ss:sss").parse(date);//////////////ענביתתתתתתת
//        return date1;
//long fj = date.to
        DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS");
        try {
            returnedDate = sdf.parse(date);
        } catch (ParseException e) {
            throw new Exception("Invalid date structure");
        }
        return returnedDate;
    }


    @Override
    public String toString() {
        String separator = System.lineSeparator() + "*" + System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        sb.append("All the updated files:");
        for (Path updatedFilePath : updatedFiles) {
            sb.append(updatedFilePath.toString());
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append("All the created files:");
        for (Path addedFilePath : createdFiles) {
            sb.append(addedFilePath.toString());
            sb.append(System.lineSeparator());
        }
        sb.append(System.lineSeparator());
        sb.append("All the deleted files:");
        for (Path deletedFilePath : deletedFiles) {
            sb.append(deletedFilePath.toString());
            sb.append(System.lineSeparator());
        }

        sb.append(System.lineSeparator());

        return sb.toString();
    }

    public String executeCheckout(String branchName, User u) throws Exception {
        Branch b = u.getCurRep().getBranchByName(branchName);
        //add question to user
        if (b.getRemoteBranch() == true)
            return "Branch is an RR branch! cannot be checkOut to.";
        u.getCurRep().setHeadBranch(b);
        deleteFilesInFolder(u.getCurRep().getRepositoryPath().toFile());
        Commit c = u.getCurRep().getHeadBranch().getPointedCommit();

        //
        String fileName = u.getCurRep().getRepositoryPath() + "\\.magit\\branches\\head";
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        //DELETE THE OLD NAME INSIDE
        writer.write(branchName);

        writer.close();

        createFilesInWCFromCommitObject(c.getRootFolder(), u.getCurRep().getRepositoryPath());
        return "";
    }

    public static void deleteAllFilesInFolder(File mainFile) {
        File[] allFileComponents = mainFile.listFiles();
        for (File f : allFileComponents) {
            if (f.isDirectory()) {
                deleteAllFilesInFolder(f);
            } else {
                f.delete();
            }
        }
        mainFile.delete();
    }

    public void deleteFilesInFolder(File mainFile) {
        File[] allFileComponents = mainFile.listFiles();
        for (File f : allFileComponents) {
            if (!f.getName().equals(".magit")) {
                if (f.isDirectory()) {
                    deleteFilesInFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        mainFile.delete();
    }

    public void createFilesInWCFromCommitObject(Folder rootFolder, Path pathForFile) throws Exception {
        //
        for (Folder.Component c : rootFolder.getComponents()) {

            if (c.getComponentType().equals(FolderType.Blob)) {
                Blob b = (Blob) c.getDirectObject();
                if (b != null)
                    createFile(c.getComponentName(), b.getContent(), pathForFile, getDateFromString(c.getLastUpdateDate()));/////////////gbch,,,,,
            } else {
                new File(pathForFile.toString() + "\\" + c.getComponentName()).mkdirs();
                Folder f = (Folder) c.getDirectObject();
                createFilesInWCFromCommitObject(f, Paths.get(pathForFile.toString() + "\\" + c.getComponentName()));
                File file = Paths.get(pathForFile.toString() + "\\" + c.getComponentName()).toFile();
                file.setLastModified(getDateFromString(c.getLastUpdateDate()));
            }
        }

    }

    //inbar, need to debug

    public String showFilesOfCommit(Commit commit, User u) throws IOException {
        //Commit commit = u.getCurRep().getHeadBranch().getPointedCommit();
        //build a folder that represents the commit
        Folder folder = generateFolderFromCommitObject(commit.getRootFolderSHA1(), u.getCurRep().getRepositoryPath().toString());
        return showFilesOfCommitRec(folder, "");
    }


    //commitFolder
    public String showFilesOfCommitRec(Folder rootFolder, String toPrint) {

        StringBuilder builder = new StringBuilder();
        builder.append(toPrint);
        builder.append("Contains:");
        builder.append(System.lineSeparator());

        for (Folder.Component c : rootFolder.getComponents()) {

            if (c.getComponentType().equals(FolderType.Blob)) {
                builder.append(c.getComponentsStringFromComponent());                //return builder.toString();
                //add blob component to string, return;
            } else {

                builder.append(c.getComponentsStringFromComponent());
                builder.append(System.lineSeparator());
                builder.append(showFilesOfCommitRec((Folder) c.getDirectObject(), toPrint));

            }
            if (rootFolder.getComponents().size() != 1) {
                builder.append(System.lineSeparator());
            }

        }
        if (rootFolder.getComponents().size() != 1) {

            builder.append(System.lineSeparator());
        }
        return builder.toString();

    }

    public void showFilesOfCommitRecAsList(Folder rootFolder, List<String> toPrint, String path) {
//        StringBuilder builder = new StringBuilder();
//        builder.append(toPrint);
//        builder.append("Contains:");
//        builder.append(System.lineSeparator());

        for (Folder.Component c : rootFolder.getComponents()) {

            if (c.getComponentType().equals(FolderType.Blob)) {
//                builder.append(c.getComponentsStringFromComponent());                //return builder.toString();
                //add blob component to string, return;
                toPrint.add(path + "\\" + c.getComponentName());
            } else {
                String intro = "\\" + c.getComponentName();
                toPrint.add(path + "\\" + c.getComponentName());
                showFilesOfCommitRecAsList((Folder) c.getDirectObject(), toPrint, path + intro);
//                builder.append(c.getComponentsStringFromComponent());
//                builder.append(System.lineSeparator());
//                builder.append(showFilesOfCommitRec((Folder) c.getDirectObject(), toPrint));

            }


        }

//        return builder.toString();

    }
//    public void showFilesOfCommitRecAsListNew(Folder rootFolder, List<FileTypeForUI> toPrint,String path) {
////        StringBuilder builder = new StringBuilder();
////        builder.append(toPrint);
////        builder.append("Contains:");
////        builder.append(System.lineSeparator());
//
//        for (Folder.Component c : rootFolder.getComponents()) {
//
//            if (c.getComponentType().equals(FolderType.Blob)) {
////                builder.append(c.getComponentsStringFromComponent());                //return builder.toString();
//                //add blob component to string, return;
//                toPrint.add(path + "\\" + c.getComponentName());
//            } else {
//
//                String intro = "\\"+ c.getComponentName();
//                toPrint.add(path + "\\"+c.getComponentName());
//                showFilesOfCommitRecAsListNew((Folder)c.getDirectObject(),toPrint,path + intro);
////                builder.append(c.getComponentsStringFromComponent());
////                builder.append(System.lineSeparator());
////                builder.append(showFilesOfCommitRec((Folder) c.getDirectObject(), toPrint));
//
//            }
//
//
//        }
//
////        return builder.toString();
//
//    }
//    public static File getFileFromSHA1(String ShA1, Path path) {
//        Path objectsPath = Paths.get(path.toString() + "\\objects");
//        File f = Paths.get(objectsPath + ShA1 + ".zip").toFile();
//        return f;
//
//    }
//    public static String generateSHA1FromFile(File file) {
//        String str = file.toString();
//        return generateSHA1FromString(str);
//    }

    //    public void ImportRepFromXML() {
//
//    }
//
//    public void ShowFilesOfCurrCommit() {
//
//    }

    public void ImportRepositoryFromXML(Boolean isCreateFiles, String xmlPath, User u) throws Exception {//V
        if (isCreateFiles) {
            MagitRepository oldRepository;
            Repository newRep;
            try {
                oldRepository = Repository.loadFromXml(xmlPath);

            } catch (Exception e) {
                throw new Exception("Unable to load from xml");
            }
            newRep = new Repository(Paths.get(oldRepository.getLocation()), u);
            u.getRepositories().add(newRep);
            u.setCurRep(newRep);
            //convertOldRepoToNew(newRep,oldRepository,u);
            if (oldRepository.getMagitRemoteReference() != null) {
                newRep.setRepositoryRemoteName(oldRepository.getMagitRemoteReference().getName());
                newRep.setRepositoryRemotePath(oldRepository.getMagitRemoteReference().getLocation());
            } else {
                newRep.setRepositoryRemoteName("");
                newRep.setRepositoryRemotePath("");
            }

            if (Files.exists(Paths.get(oldRepository.getLocation())))//כבר קיים רפוזטורי כזה בעל אותו השם באותו המקום
            {
                throw new IOException(oldRepository.getLocation());//go ask question, with the location, c:\repo1
            }
        }
        Repository r = u.getCurRep();
        createMagitFiles(true, u);
        createFilesInWCFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolder(), r.getRepositoryPath());
        createFile("RepName", r.getRepositoryName(), Paths.get(r.getRepositoryPath() + "\\.magit"), new Date().getTime());
        createFile("RemoteRepName", r.getRepositoryRemoteName(), Paths.get(r.getRepositoryPath() + "\\.magit"), new Date().getTime());
        createFile("RemoteRepPath", r.getRepositoryRemotePath(), Paths.get(r.getRepositoryPath() + "\\.magit"), new Date().getTime());

//        u.getRepositories().add(newRep);
        //UManager.addUser(new User(GITRepository.getHeadBranch().getPointedCommit().getChanger()));
        this.blobTempMap.clear();
        this.folderTempMap.clear();
        this.commitTempMap.clear();
    }

    public void endXMLLoad(MagitRepository oldRepository, Repository r, User u) throws Exception {
//        if (oldRepository.getMagitRemoteReference() != null) {
//            r.setRepositoryRemoteName(oldRepository.getMagitRemoteReference().getName());
//            r.setRepositoryRemotePath(this.dirPath + "\\" + u.getName() + "\\"+ r.getRepositoryRemoteName());
//        } else {
//            r.setRepositoryRemoteName("");
//            r.setRepositoryRemotePath("");
//       }
//
////    if (Files.exists(Paths.get(oldRepository.getLocation())))//כבר קיים רפוזטורי כזה בעל אותו השם באותו המקום
////    {
////
////        throw new IOException(oldRepository.getLocation());//go ask question, with the location, c:\repo1
////    }


        createMagitFiles(true, u);
        createFilesInWCFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolder(), r.getRepositoryPath());
        createFile("RepName", r.getRepositoryName(), Paths.get(r.getRepositoryPath() + "\\.magit"), new Date().getTime());
//        createFile("RemoteRepName", r.getRepositoryRemoteName(), Paths.get(this.dirPath + "\\" + u.getName() + "\\.magit"), new Date().getTime());
//        createFile("RemoteRepPath", r.getRepositoryRemotePath(), Paths.get(this.dirPath + "\\" + u.getName() + "\\.magit"), new Date().getTime());

//        u.getRepositories().add(newRep);
        //UManager.addUser(new User(GITRepository.getHeadBranch().getPointedCommit().getChanger()));
        this.blobTempMap.clear();
        this.folderTempMap.clear();
        this.commitTempMap.clear();
    }

    public String convertOldRepoToNew(Repository r, MagitRepository oldRepository, User u, UserManager UN) {
        String errorToReturn = "";
        r.insertMembersToNewRepository(oldRepository);
        try {
            blobTempMap = Folder.getAllBlobsToMap(oldRepository.getMagitBlobs());
        } catch (Exception e) {
            errorToReturn = e.getMessage();
        }
        try {
            folderTempMap = Folder.getAllFoldersToMap(oldRepository.getMagitFolders());
        } catch (Exception e) {
            errorToReturn = e.getMessage();
        }

        try {
            Folder.createListOfComponents(folderTempMap, blobTempMap, oldRepository.getMagitFolders());//create list of component to each folder
        } catch (Exception e) {
            errorToReturn = e.getMessage();
        }
        setSHA1ToFolders();


        try {
            commitTempMap = Commit.getAllCommitsToMap(oldRepository.getMagitCommits(), folderTempMap, UN);
        } catch (Exception e) {
            errorToReturn = e.getMessage();
        }
        updateAllSHA1(oldRepository.getMagitCommits());//update prev and prevprev and cur SHA1
        r.addCommitsToRepositoryMAp(commitTempMap);//add all commit to comitmap in repository

        try {
            r.setBranches(Branch.getAllBranchesToMap(oldRepository.getMagitBranches(), commitTempMap));    //add all branched to branches list in repository
        } catch (Exception e) {
            errorToReturn = e.getMessage();
        }

        if (r.getBranchByName(oldRepository.getMagitBranches().getHead()) != null) {
            r.setHeadBranch(r.getBranchByName(oldRepository.getMagitBranches().getHead()));//set head
//            createMagitFiles();

        } else {
            //return head branch does not exist
            errorToReturn = "Head branch does not exist";

        }
        return errorToReturn;
    }


    public void updateAllSHA1(MagitCommits oldList)//update prev and prevprev
    {
        List<MagitSingleCommit> oldlist = oldList.getMagitSingleCommit();
        for (MagitSingleCommit c : oldlist) {
            updateCurrentSHA(c, oldList);
        }
    }

    public void updateCurrentSHA(MagitSingleCommit c, MagitCommits oldList) {
        if (commitTempMap.get(c.getId()).getSHA() == null) {
            if (c.getPrecedingCommits() == null) {
                Commit com = commitTempMap.get(c.getId());
                com.setCommitFileContentToSHA();
                return;
            } else if (c.getPrecedingCommits().getPrecedingCommit().isEmpty()) {
                Commit com = commitTempMap.get(c.getId());
                com.setCommitFileContentToSHA();
                return;
            } else if ((commitTempMap.get(c.getPrecedingCommits().getPrecedingCommit().get(0).getId()).getSHA() != null)) {
                commitTempMap.get((c.getId())).setSHA1PreveiousCommit(commitTempMap.get(c.getPrecedingCommits().getPrecedingCommit().get(0).getId()).getSHA());
                commitTempMap.get(c.getId()).setCommitFileContentToSHA();
                return;
            }

            MagitSingleCommit commit = getMagitCommit(c.getPrecedingCommits().getPrecedingCommit().get(0).getId(), oldList);
            updateCurrentSHA(commit, oldList);
            Commit com = commitTempMap.get(c.getId());
            com.setSHA1PreveiousCommit(commitTempMap.get(c.getPrecedingCommits().getPrecedingCommit().get(0).getId()).getSHA());
            com.setCommitFileContentToSHA();
        }
    }

    public MagitSingleCommit getMagitCommit(String ID, MagitCommits oldList) {
        List<MagitSingleCommit> oldlist = oldList.getMagitSingleCommit();
        for (MagitSingleCommit c : oldlist) {
            if (c.getId().equals(ID)) {
                return c;
            }
        }
        return null;
    }

    public void setSHA1ToFolders() {
        Iterator entries = folderTempMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Folder.Component c = (Folder.Component) thisEntry.getValue();
            Folder f = (Folder) c.getDirectObject();
            c.setSha1(createShaToFolderRec(f));
//            for(Folder.Component com:f.getComponents())
//            {
//                if(com.getComponentType().equals(FolderType.Folder))
//                {
//                    if (com.getComponentSHA1() == null) {
//                        setSHA1ToFolders();
//                    }
//                }
//            }
//            Collections.sort(f.getComponents());
//            c.setSha1(generateSHA1FromString(f.getFolderContentString()));
        }

    }

    public String createShaToFolderRec(Folder f) {
        for (Folder.Component com : f.getComponents()) {
            if (com.getComponentType().equals(FolderType.Folder)) {
                if (com.getComponentSHA1() == null) {
                    com.setSha1(createShaToFolderRec((Folder) com.getDirectObject()));
                }
            }
        }
        Collections.sort(f.getComponents());
        return generateSHA1FromString(f.getFolderContentString());
    }


    public void createMagitFiles(boolean isCreateObjects, User u) throws Exception { //V
        new File(u.getCurRep().getRepositoryPath() + "\\.magit\\objects").mkdirs();
        new File(u.getCurRep().getRepositoryPath() + "\\.magit\\branches").mkdirs();

        try {
            createBranchesFiles(u);
        } catch (Exception e) {
            throw new Exception("Could not create branches folder");
        }
        if (isCreateObjects) {
            try {
                createObjectFiles(u);
            } catch (Exception e) {
                throw new Exception("Could not create objects folder");
            }
        }
    }

    public void createBranchesFiles(User u) throws Exception {//V
        Path BranchesPath = Paths.get(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Branches");
        if (u.getCurRep().getRepositoryRemoteName() != null)
            new File(u.getCurRep().getRepositoryPath() + "\\.magit\\Branches\\" + u.getCurRep().getRepositoryRemoteName()).mkdirs();
        for (Branch b : u.getCurRep().getBranches()) {
            createFileInMagit(b, u.getCurRep().getRepositoryPath(), u.getCurRep());
        }
        createFile("Head", u.getCurRep().getHeadBranch().getBranchName(), BranchesPath, new Date().getTime());
    }

    public void createObjectFiles(User u) throws Exception {//V
        createBlobs(u);
        createFolders(u);
        createCommits(u);
    }

    public void createBlobs(User u) throws Exception {//V

        Iterator entries = blobTempMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Folder.Component c = (Folder.Component) thisEntry.getValue();
            Blob b = (Blob) c.getDirectObject();
            createFileInMagit(b, u.getCurRep().getRepositoryPath(), u.getCurRep());
        }

    }

    public void createFolders(User u) throws Exception {//V

        Iterator entries = folderTempMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Folder.Component c = (Folder.Component) thisEntry.getValue();
            Folder f = (Folder) c.getDirectObject();
            createFileInMagit(f, u.getCurRep().getRepositoryPath(), u.getCurRep());
        }
    }

    public void createCommits(User u) throws Exception {//V

        Iterator entries = commitTempMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            Commit c = (Commit) thisEntry.getValue();
            createFileInMagit(c, u.getCurRep().getRepositoryPath(), u.getCurRep());
        }
    }

    public void updateFile(String newSha1, User u) throws Exception//אמור להחליף את השא1 בתוך הקובץ
    {
        String fileName = u.getCurRep().getRepositoryPath() + "\\.magit\\branches\\" + u.getCurRep().getHeadBranch().getBranchName();//c:\repo1\\.magit\\branches\\mainBranchName
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(newSha1);

        writer.close();
    }

    public void newMerge(Branch base, Branch target, User u, User RRu, String repName) throws Exception {

        Folder oursFolder = target.getPointedCommit().getRootFolder();
        Folder theirsFolder = base.getPointedCommit().getRootFolder();
        Function<String, CommitRepresentative> sha1ToCommit = s -> u.getUserRepo(repName).sha1ToCommit(s);
        AncestorFinder AF = new AncestorFinder(sha1ToCommit);
        String fathersha1 = AF.traceAncestor(target.getPointedCommitSHA1(), base.getPointedCommitSHA1());
        Folder fatherFolder = u.getUserRepo(repName).getCommitMap().get(fathersha1).getRootFolder();// = traceAncestor

        Folder f = new Folder();
        f =  mergeBranches(u.getUserRepo(repName).getRepositoryPath(), oursFolder, theirsFolder, fatherFolder, f, u);


        Commit c = new Commit();
        c.setRootFolderSHA1(GitManager.generateSHA1FromString(f.stringComponentsToString()));
        c.setRootFolder(f);
        c.setSHA1PreveiousCommit(u.getUserRepo(repName).getBranchByName(target.getBranchName()).getPointedCommitSHA1());
        c.setSHA1anotherPreveiousCommit(u.getUserRepo(repName).getBranchByName(base.getBranchName()).getPointedCommitSHA1());
        c.setChanger(u);
        c.setCreationDate(GitManager.getDateFromObject(new Date()));
        c.setDescription("Pr approved Commit");
        c.setCommitFileContentToSHA();
        u.getUserRepo(repName).getCommitMap().put(c.getSHA(), c);
        u.getUserRepo(repName).getBranchByName(base.getBranchName()).setPointedCommit(c);
        u.getUserRepo(repName).getBranchByName(base.getBranchName()).setPointedCommitSHA1(c.getSHA());
        u.getUserRepo(repName).getBranchByName(target.getBranchName()).setPointedCommit(c);
        u.getUserRepo(repName).getBranchByName(target.getBranchName()).setPointedCommitSHA1(c.getSHA());
        createFile(base.getBranchName(), c.getSHA(), Paths.get(u.getUserRepo(repName).getRepositoryPath() + "\\.magit\\Branches"), new Date().getTime());
        createFile(target.getBranchName(), c.getSHA(), Paths.get(u.getUserRepo(repName).getRepositoryPath() + "\\.magit\\Branches"), new Date().getTime());
        createZipFile(Paths.get(u.getCurRep().getRepositoryPath().toString()+"\\.magit\\objects"),c.getSHA(),c.getSHAContent());
//        deleteFilesInFolder(GITRepository.getRepositoryPath().toFile());
//
////take care of commit
//        Commit c = new Commit(description, userName);
////       String anotherPrev = GITRepository.getHeadBranch().getPointedCommit().getSHA1PreveiousCommit();//האם יש עוד אבא
////       if (anotherPrev != null) {
////           c.setSHA1anotherPreveiousCommit(anotherPrev);
////       }
//        c.setSHA1PreveiousCommit(GITRepository.getHeadBranch().getPointedCommit().getSHA());
//        c.setSHA1anotherPreveiousCommit(their.getPointedCommit().getSHA());
//        c.setRootFolder(mergedFolder);
//        c.setCommitFileContentToSHA();
//        c.setRootFolderSHA1(generateSHA1FromString(mergedFolder.getFolderContentString()));
//        GITRepository.getCommitList().put(c.getSHA(), c); //adding to commits list of the current reposetory
//
//        createFile(GITRepository.getHeadBranch().getBranchName(), c.getSHA(), Paths.get(BranchesPath), new Date().getTime());
//        GITRepository.getHeadBranch().setPointedCommitSHA1(c.getSHA());
//        GITRepository.getHeadBranch().setPointedCommit(c); //creation
//
//        createZipFile(Paths.get(ObjectsPath), generateSHA1FromString(mergedFolder.getFolderContentString()), mergedFolder.getFolderContentString());
//
//        createFileInMagit(GITRepository.getHeadBranch().getPointedCommit(), GITRepository.getRepositoryPath());
//        createFilesInWCFromCommitObject(c.getRootFolder(), GITRepository.getRepositoryPath());
//
//        their.setPointedCommit(c);
//        their.setPointedCommitSHA1(c.getSHA());e
    }

    public Folder merge(String theirBranchName, User u) throws Exception {
        String BranchesPath = u.getCurRep().getRepositoryPath() + "\\.magit\\branches\\";
        String ObjectsPath = u.getCurRep().getRepositoryPath() + "\\.magit\\objects\\";

        Branch their = u.getCurRep().getBranchByName(theirBranchName);
        //לבנות פולדר חדש
        //לשנות הצבעה של ההד אליו
        //לשנות ןןרקינד קופי
        Folder mergedFolder = new Folder();
        Folder oursFolder = u.getCurRep().getHeadBranch().getPointedCommit().getRootFolder();
        Folder theirsFolder = u.getCurRep().getBranchByName(theirBranchName).getPointedCommit().getRootFolder();
        Function<String, CommitRepresentative> sha1ToCommit = s -> u.getCurRep().sha1ToCommit(s);
        AncestorFinder AF = new AncestorFinder(sha1ToCommit);
        String fathersha1 = AF.traceAncestor(u.getCurRep().getHeadBranch().getPointedCommitSHA1(), their.getPointedCommitSHA1());
        Folder fatherFolder = u.getCurRep().getCommitMap().get(fathersha1).getRootFolder();// = traceAncestor

        mergeBranches(u.getCurRep().getRepositoryPath(), oursFolder, theirsFolder, fatherFolder, mergedFolder, u);
        return mergedFolder;
//
//        deleteFilesInFolder(GITRepository.getRepositoryPath().toFile());
//
////take care of commit
//        Commit c = new Commit(description, userName);
////       String anotherPrev = GITRepository.getHeadBranch().getPointedCommit().getSHA1PreveiousCommit();//האם יש עוד אבא
////       if (anotherPrev != null) {
////           c.setSHA1anotherPreveiousCommit(anotherPrev);
////       }
//        c.setSHA1PreveiousCommit(GITRepository.getHeadBranch().getPointedCommit().getSHA());
//        c.setSHA1anotherPreveiousCommit(their.getPointedCommit().getSHA());
//        c.setRootFolder(mergedFolder);
//        c.setCommitFileContentToSHA();
//        c.setRootFolderSHA1(generateSHA1FromString(mergedFolder.getFolderContentString()));
//        GITRepository.getCommitList().put(c.getSHA(), c); //adding to commits list of the current reposetory
//
//        createFile(GITRepository.getHeadBranch().getBranchName(), c.getSHA(), Paths.get(BranchesPath), new Date().getTime());
//        GITRepository.getHeadBranch().setPointedCommitSHA1(c.getSHA());
//        GITRepository.getHeadBranch().setPointedCommit(c); //creation
//
//        createZipFile(Paths.get(ObjectsPath), generateSHA1FromString(mergedFolder.getFolderContentString()), mergedFolder.getFolderContentString());
//
//        createFileInMagit(GITRepository.getHeadBranch().getPointedCommit(), GITRepository.getRepositoryPath());
//        createFilesInWCFromCommitObject(c.getRootFolder(), GITRepository.getRepositoryPath());
//
//        their.setPointedCommit(c);
//        their.setPointedCommitSHA1(c.getSHA());
    }

    public void createFilesAfterMerge(String theirBranchName, String description, Folder mergedFolder, User u) throws Exception {
        String BranchesPath = u.getCurRep().getRepositoryPath() + "\\.magit\\branches\\";
        String ObjectsPath = u.getCurRep().getRepositoryPath() + "\\.magit\\objects\\";

        Branch their = u.getCurRep().getBranchByName(theirBranchName);

        deleteFilesInFolder(u.getCurRep().getRepositoryPath().toFile());

//take care of commit
        Commit c = new Commit(description, u);
//       String anotherPrev = GITRepository.getHeadBranch().getPointedCommit().getSHA1PreveiousCommit();//האם יש עוד אבא
//       if (anotherPrev != null) {
//           c.setSHA1anotherPreveiousCommit(anotherPrev);
//       }
        c.setSHA1PreveiousCommit(u.getCurRep().getHeadBranch().getPointedCommit().getSHA());
        c.setSHA1anotherPreveiousCommit(their.getPointedCommit().getSHA());
        c.setRootFolder(mergedFolder);
        c.setRootFolderSHA1(generateSHA1FromString(mergedFolder.getFolderContentString()));
        c.setCommitFileContentToSHA();
        u.getCurRep().getCommitList().put(c.getSHA(), c); //adding to commits list of the current reposetory

        createFile(u.getCurRep().getHeadBranch().getBranchName(), c.getSHA(), Paths.get(BranchesPath), new Date().getTime());
        u.getCurRep().getHeadBranch().setPointedCommitSHA1(c.getSHA());
        u.getCurRep().getHeadBranch().setPointedCommit(c); //creation

        u.getCurRep().getBranchByName(theirBranchName).setPointedCommit(c);
        u.getCurRep().getBranchByName(theirBranchName).setPointedCommitSHA1(c.getSHA());
        createZipFile(Paths.get(ObjectsPath), generateSHA1FromString(mergedFolder.getFolderContentString()), mergedFolder.getFolderContentString());

        createFile(theirBranchName, c.getSHA(), Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit\\Branches"), new Date().getTime());

        createFileInMagit(u.getCurRep().getHeadBranch().getPointedCommit(), u.getCurRep().getRepositoryPath(), u.getCurRep());
        createFilesInWCFromCommitObject(c.getRootFolder(), u.getCurRep().getRepositoryPath());


    }

    public Folder mergeBranches(Path path, Folder oursFolder, Folder theirsFolder, Folder fatherFolder, Folder mergedFolder, User u) {

        mergedFolder.setComponents(new ArrayList<>());

        ArrayList<Folder.Component> ourComponents = new ArrayList<>();
        ArrayList<Folder.Component> theirComponents = new ArrayList<>();
        ArrayList<Folder.Component> fatherComponents = new ArrayList<>();

        int ourIndex = 0;
        int theirIndex = 0;
        int fatherIndex = 0;

        if (oursFolder != null)
            ourComponents = oursFolder.getComponents();
        if (theirsFolder != null)
            theirComponents = theirsFolder.getComponents();
        if (fatherFolder != null)
            fatherComponents = fatherFolder.getComponents();


        //if (!ourComponents.isEmpty() && !theirComponents.isEmpty() && !fatherComponents.isEmpty()) {

// indexes of the component in the lists
        while (ourIndex < ourComponents.size() || theirIndex < theirComponents.size() || fatherIndex < fatherComponents.size()) { // while two folders are not empty

            String isOurExist = "0";
            String isTheirExist = "0";
            String isFatherExist = "0";
            String isOEqualT = "0";
            String isOEqualF = "0";
            String isFEqualT = "0";
            //לוודא שהאידקסים נכונים
            //לעשות בדיקה של מי הכי קטן

//find min
            //find who is not null
            String a = null;
            FolderType type = null;
            if (fatherIndex < fatherComponents.size()) {
                a = fatherComponents.get(fatherIndex).getComponentName();
                type = fatherComponents.get(fatherIndex).getComponentType();
            } else if (theirIndex < theirComponents.size()) {
                a = theirComponents.get(theirIndex).getComponentName();
                type = theirComponents.get(theirIndex).getComponentType();
            } else if (ourIndex < ourComponents.size()) {
                a = ourComponents.get(ourIndex).getComponentName();
                type = ourComponents.get(ourIndex).getComponentType();
            }

            if (ourIndex < ourComponents.size()) {
                if (a.compareTo(ourComponents.get(ourIndex).getComponentName()) > 0) {
                    a = ourComponents.get(ourIndex).getComponentName();
                    type = ourComponents.get(ourIndex).getComponentType();
                }
            }
            if (theirIndex < theirComponents.size()) {
                if (a.compareTo(theirComponents.get(theirIndex).getComponentName()) > 0) {
                    a = theirComponents.get(theirIndex).getComponentName();
                    type = theirComponents.get(theirIndex).getComponentType();
                }
            }

            //fill 3 bool
            if (fatherIndex < fatherComponents.size() && a.compareTo(fatherComponents.get(fatherIndex).getComponentName()) == 0) {
                isFatherExist = "1";
                if (ourIndex < ourComponents.size()) {
                    if (fatherComponents.get(fatherIndex).getComponentName().compareTo(ourComponents.get(ourIndex).getComponentName()) == 0) {
                        isOurExist = "1";
                        //   isOEqualF = "1";
                    }
                }
                if (theirIndex < theirComponents.size()) {
                    if (fatherComponents.get(fatherIndex).getComponentName().compareTo(theirComponents.get(theirIndex).getComponentName()) == 0) {
                        isTheirExist = "1";
                        //     isFEqualT = "1";
                    }
                }
            } else if (ourIndex < ourComponents.size() && a.compareTo(ourComponents.get(ourIndex).getComponentName()) == 0) {
                isOurExist = "1";
                if (fatherIndex < fatherComponents.size()) {
                    if (fatherComponents.get(fatherIndex).getComponentName().compareTo(ourComponents.get(ourIndex).getComponentName()) == 0) {
                        isFatherExist = "1";
                        // isOEqualF = "1";
                    }
                }
                if (theirIndex < theirComponents.size()) {
                    if (ourComponents.get(ourIndex).getComponentName().compareTo(theirComponents.get(theirIndex).getComponentName()) == 0) {
                        isTheirExist = "1";
                        // isOEqualT = "1";
                    }
                }
            } else if (theirIndex < theirComponents.size() && a.compareTo(theirComponents.get(theirIndex).getComponentName()) == 0) {
                isTheirExist = "1";
                if (fatherIndex < fatherComponents.size()) {
                    if (fatherComponents.get(fatherIndex).getComponentName().compareTo(theirComponents.get(theirIndex).getComponentName()) == 0) {
                        isFatherExist = "1";
                        // isFEqualT = "1";
                    }
                }
                if (ourIndex < ourComponents.size()) {
                    if (ourComponents.get(ourIndex).getComponentName().compareTo(theirComponents.get(theirIndex).getComponentName()) == 0) {
                        isOurExist = "1";
                        // isOEqualT = "1";
                    }
                }
            }

            if (isFatherExist.equals("1") && isOurExist.equals("1")) {
                if (ourComponents.get(ourIndex).getComponentSHA1().compareTo(fatherComponents.get(fatherIndex).getComponentSHA1()) == 0) {
                    isOEqualF = "1";
                }
            }
            if (isTheirExist.equals("1") && isOurExist.equals("1")) {
                if (theirComponents.get(theirIndex).getComponentSHA1().compareTo(ourComponents.get(ourIndex).getComponentSHA1()) == 0) {
                    isOEqualT = "1";
                }
            }
            if (isTheirExist.equals("1") && isFatherExist.equals("1")) {
                if (theirComponents.get(theirIndex).getComponentSHA1().compareTo(fatherComponents.get(fatherIndex).getComponentSHA1()) == 0)
                    isFEqualT = "1";
            }

            if (type.equals(FolderType.Folder)) {
                Folder our = null;
                Folder their = null;
                Folder father = null;
                if (isOurExist.equals("1")) {
                    our = (Folder) ourComponents.get(ourIndex).getDirectObject();
                }
                if (isTheirExist.equals("1")) {
                    their = (Folder) theirComponents.get(theirIndex).getDirectObject();
                }
                if (isFatherExist.equals("1")) {
                    father = (Folder) fatherComponents.get(fatherIndex).getDirectObject();
                }
                Folder newMergeFolder = new Folder();
                mergeBranches(Paths.get(path + "\\" + a), our, their, father, newMergeFolder, u);
                //check if there was no chnge- take our
                if (their != null)
                    if (generateSHA1FromString(newMergeFolder.getFolderContentString()).equals(generateSHA1FromString(their.getFolderContentString())))
                        newMergeFolder = their;
                if (father != null)
                    if (generateSHA1FromString(newMergeFolder.getFolderContentString()).equals(generateSHA1FromString(father.getFolderContentString())))
                        newMergeFolder = father;
                if (our != null)
                    if (generateSHA1FromString(newMergeFolder.getFolderContentString()).equals(generateSHA1FromString(our.getFolderContentString())))
                        newMergeFolder = our;

                Folder.Component c = new Folder.Component(a, generateSHA1FromString(newMergeFolder.getFolderContentString()), FolderType.Folder, u.getName(), getDateFromObject(new Date()));
                c.setDirectObject(newMergeFolder);

                if (theirIndex < theirComponents.size() && theirComponents.get(theirIndex).getDirectObject() == newMergeFolder) {
                    c = theirComponents.get(theirIndex);
                } else if (fatherIndex < fatherComponents.size() && fatherComponents.get(fatherIndex).getDirectObject() == newMergeFolder) {
                    c = fatherComponents.get(fatherIndex);
                } else if (ourIndex < ourComponents.size() && ourComponents.get(ourIndex).getDirectObject() == newMergeFolder) {
                    c = ourComponents.get(ourIndex);
                }
                mergedFolder.getComponents().add(c);
            } else {
                MergeType enumForFun = getEnumFromString(isOurExist, isTheirExist, isFatherExist, isOEqualT, isOEqualF, isFEqualT);
                MergeType e = MergeType.valueOf(enumForFun.toString().toUpperCase());
                Folder.Component o = null;
                Folder.Component t = null;
                Folder.Component f = null;

                if (ourIndex < ourComponents.size()) {
                    if (ourComponents.get(ourIndex).getDirectObject() instanceof Blob)
                        o = ourComponents.get(ourIndex);
                    else
                        o = null;
                }
                if (theirIndex < theirComponents.size()) {
                    if (theirComponents.get(theirIndex).getDirectObject() instanceof Blob)
                        t = theirComponents.get(theirIndex);
                    else
                        t = null;
                }
                if (fatherIndex < fatherComponents.size()) {
                    if (fatherComponents.get(fatherIndex).getDirectObject() instanceof Blob)
                        f = fatherComponents.get(fatherIndex);
                    else
                        f = null;
                }
                Folder.Component c = e.decideFile(path, conflictMap, o, t, f, mergedFolder);
                mergedFolder.getComponents().add(c);

            }
            if (isFatherExist.equals("1")) {
                fatherIndex++;
            }
            if (isOurExist.equals("1")) {
                ourIndex++;
            }
            if (isTheirExist.equals("1")) {
                theirIndex++;
            }
        }
        if (oursFolder != null)
            if (generateSHA1FromString(mergedFolder.getFolderContentString()).equals(generateSHA1FromString(oursFolder.getFolderContentString())))
                mergedFolder = oursFolder;
        if (theirsFolder != null)
            if (generateSHA1FromString(mergedFolder.getFolderContentString()).equals(generateSHA1FromString(theirsFolder.getFolderContentString())))
                mergedFolder = theirsFolder;
        if (fatherFolder != null)
            if (generateSHA1FromString(mergedFolder.getFolderContentString()).equals(generateSHA1FromString(fatherFolder.getFolderContentString())))
                mergedFolder = fatherFolder;

return mergedFolder;
    }

    public MergeType getEnumFromString(String isOurExist, String isTheirExist, String isFatherExist, String isOEqualT, String isOEqualF, String isFEqualT) {
        String s = isOurExist + isTheirExist + isFatherExist + isOEqualT + isOEqualF + isFEqualT;
        MergeType MT = null;
        switch (s) {
            case "001000":
                MT = MergeType.A;
                break;
            case "010000":
                MT = MergeType.B;
                break;

            case "011000":
                MT = MergeType.C;
                break;

            case "011001":
                MT = MergeType.D;
                break;

            case "100000":
                MT = MergeType.E;
                break;

            case "101000":
                MT = MergeType.F;
                break;

            case "101010":
                MT = MergeType.G;
                break;

            case "110000":
                MT = MergeType.H;
                break;

            case "110100":
                MT = MergeType.I;
                break;

            case "111000":
                MT = MergeType.J;
                break;

            case "111001":
                MT = MergeType.K;
                break;

            case "111010":
                MT = MergeType.L;
                break;

            case "111100":
                MT = MergeType.M;
                break;

            case "111111":
                MT = MergeType.N;
                break;
        }
        return MT;
    }

    public ArrayList<String> getStringsForConflict(Conflict c) {
        ArrayList<String> returnedList = new ArrayList<>();
        returnedList.add(c.conflictName);
        Blob b = null;
        if (c.our != null) {
            b = (Blob) c.our.getDirectObject();
            returnedList.add(b.getContent());
        } else {
            returnedList.add("");
        }
        if (c.their != null) {
            b = (Blob) c.their.getDirectObject();
            returnedList.add(b.getContent());
        } else {
            returnedList.add("");
        }
        if (c.father != null) {
            b = (Blob) c.father.getDirectObject();
            returnedList.add(b.getContent());
        } else
            returnedList.add("");

        return returnedList;
    }

    public void checkForEmptyFolders(Folder rootFolder) {
        //
        for (Folder.Component c : rootFolder.getComponents()) {
            if (c.getComponentType().equals(FolderType.Folder)) {
                // new File(pathForFile.toString() + "\\" + c.getComponentName()).mkdirs();
                Folder f = (Folder) c.getDirectObject();
                if (c.getDirectObject() == null) {
                    rootFolder.getComponents().remove(c);
                } else
                    checkForEmptyFolders(f);
            }
        }

    }

//    public void executeFetch(User u) throws Exception {
//        updatedBranchesToLR(u);
//        addNewObjects(true,u);
//
//    }

//    public void updatedBranchesToLR(User u) throws Exception {
//        addNewBranchesToLRInRep(Paths.get(u.getCurRep().getRepositoryRemotePath()),u);
//    }
//
//    public void addNewBranchesToLRInRep(Path RepPath,User u) throws Exception {
//        Path BranchesPath = Paths.get(RepPath.toString() + "\\.magit\\Branches");
//        File[] allBranches = BranchesPath.toFile().listFiles();
//        String fileContent;
//        String intro = RepPath.toFile().getName() + "\\";
//        for (File f : allBranches) {
//            updateSingleBranch(f, intro,u);
////            if (!f.getName().equals("Head")) {
////                if (this.GITRepository.getBranchByName(intro + f.getName()) == null) {
////                    fileContent = GitManager.readTextFile(f.toString());
////                    Branch b = new Branch(f.getName(), fileContent, true, false);
////                    GitManager.createFileInMagit(b, GITRepository.getRepositoryPath());
////                    this.GITRepository.getBranches().add(b);
////                    b.setPointedCommit(GITRepository.getCommitMap().get(fileContent));
////                }
////            } else {
////
////
////                //Branch rtb = this.GITRepository.getBranchByName(f.getName());
////                updateFastBranch(f, intro);
//////                        fileContent = GitManager.readTextFile(f.toString());
//////                        Branch b = this.getBranchByName(intro + f.getName());
//////                        if(!fileContent.equals(getBranchByName(b.getPointedCommitSHA1())))
//////                        {
//////                            b.setPointedCommitSHA1(fileContent);
//////                            b.setPointedCommit(this.getCommitList().get(fileContent));
//////                        }
////            }
//        }
//    }


    public void updateSingleBranch(File f, String intro, User u, UserManager UM) throws Exception {

        if (u.getCurRep().getBranchByName(intro + f.getName()) == null && !f.getName().equals("Head")) {
            String fileContent = GitManager.readTextFile(f.toString());
            Branch b = new Branch(f.getName(), fileContent, true, false);
            GitManager.createFileInMagit(b, u.getCurRep().getRepositoryPath(), u.getCurRep());
            u.getCurRep().getBranches().add(b);
            b.setPointedCommit(u.getCurRep().getCommitMap().get(fileContent));
            b.setPointedCommitSHA1(fileContent);
        } else {


            //Branch rtb = this.GITRepository.getBranchByName(f.getName());
            if (!f.getName().equals("Head"))
                updateFastBranch(f, intro, u, UM);
        }
//                        fileContent = GitManager.readTextFile(f.toString());
//
    }

    public void updateFastBranch(File f, String intro, User u, UserManager UM) throws IOException {
        String fileContent = GitManager.readTextFile(f.toString());
        Branch bra = u.getCurRep().getBranchByName(f.getName());
        Branch bran = u.getCurRep().getBranchByName((intro + f.getName()));
        //while(true) {
        //if (!fileContent.equals(getBranchByName(bra.getPointedCommitSHA1()))) {
        bra.setPointedCommitSHA1(fileContent);
        updateCommit(bra, u.getCurRep().getRepositoryRemotePath(), u, UM);
        bra.setPointedCommit(u.getCurRep().getCommitList().get(fileContent));
        // }
        bran.setPointedCommitSHA1(fileContent);
        bran.setPointedCommit(bra.getPointedCommit());
        // }


    }

    public String executePull(User u, boolean is_start, boolean isCommit, UserManager UM) throws Exception {
        if (is_start) {
            if (u.getCurRep().getHeadBranch().getRemoteTrackingBranch().equals(true)) {

                ExecuteCommit("", false, u);
                if (getDeletedFiles().size() != 0 ||
                        getUpdatedFiles().size() != 0 ||
                        getCreatedFiles().size() != 0) {
                    return "There are unsaved changes in the WC. would you like to save it before checkout?, Yes, No";
                }
            }
        }
        if (isCommit == true) {
            try {
                ExecuteCommit("commit before pull to " + u.getCurRep().getHeadBranch() + "Branch", true, u);
            } catch (Exception er) {
            }
        }


//        File f = new File(getGITRepository().getRepositoryRemotePath() + "\\.magit\\Branches\\Head");
//        String branchName = GitManager.readTextFile(f.toString());
        File f = new File(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Branches\\" + u.getCurRep().getHeadBranch().getBranchName());

        String intro = u.getCurRep().getRepositoryRemoteName() + "\\";
        updateSingleBranch(f, intro, u, UM);
//        Commit c = GITRepository.getBranchByName(intro+GITRepository.getHeadBranch().getBranchName()).getPointedCommit();
//        GITRepository.getHeadBranch().setPointedCommit(c);
//        c.setCommitFileContentToSHA();
//        GITRepository.getHeadBranch().setPointedCommitSHA1(c.getSHA());
        addNewObjects(true, u);
        String repPath = u.getCurRep().getRepositoryPath() + "\\.magit\\Branches";
        createFile(u.getCurRep().getHeadBranch().getBranchName(), u.getCurRep().getHeadBranch().getPointedCommitSHA1(), Paths.get(repPath + "\\" + intro), new Date().getTime());
        createFile(u.getCurRep().getHeadBranch().getBranchName(), u.getCurRep().getHeadBranch().getPointedCommitSHA1(), Paths.get(repPath), new Date().getTime());

        deleteFilesInFolder(u.getCurRep().getRepositoryPath().toFile());

        createFilesInWCFromCommitObject(u.getCurRep().getHeadBranch().getPointedCommit().getRootFolder(), u.getCurRep().getRepositoryPath());
        getCreatedFiles().clear();
        getDeletedFiles().clear();
        getUpdatedFiles().clear();
        return "Pull was done successfully";
    }


    public String executePush(User u, User RRu, UserManager UM) throws Exception {
        //לשנות קומיט בתוך הקובץ של הברנצ
        //להוסיף קבצים לאובגקטס
        //לשנות RB
        //לשנוצWC בRR
        if (u.getCurRep().getHeadBranch().getRemoteTrackingBranch().equals(true)) {
            String intro = u.getCurRep().getRepositoryRemoteName() + "\\";
            File headFile = Paths.get(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Branches\\Head").toFile();
            String headName = GitManager.readTextFile(headFile.toString());
            File headcom = Paths.get(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Branches\\" + headName).toFile();
            String heaccommitsha = GitManager.readTextFile(headcom.toString());
            String RBCommit = u.getCurRep().getBranchByName(intro + u.getCurRep().getHeadBranch().getBranchName()).getPointedCommitSHA1();
            if (heaccommitsha.equals(RBCommit)) {
                File f = new File(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Branches\\" + u.getCurRep().getHeadBranch().getBranchName());
                String RRHead = u.getCurRep().getRepositoryPath() + "\\.magit\\Branches\\" + intro + u.getCurRep().getHeadBranch().getBranchName();

                u.getCurRep().getBranchByName(intro + u.getCurRep().getHeadBranch().getBranchName()).setPointedCommitSHA1(u.getCurRep().getHeadBranch().getPointedCommitSHA1());
                u.getCurRep().getBranchByName(intro + u.getCurRep().getHeadBranch().getBranchName()).setPointedCommit(u.getCurRep().getHeadBranch().getPointedCommit());
                RRu.getUserRepo(u.getCurRep().getRepositoryName()).getBranchByName(u.getCurRep().getHeadBranch().getBranchName()).setPointedCommit(u.getCurRep().getHeadBranch().getPointedCommit());
                RRu.getUserRepo(u.getCurRep().getRepositoryName()).getBranchByName(u.getCurRep().getHeadBranch().getBranchName()).setPointedCommitSHA1(u.getCurRep().getHeadBranch().getPointedCommitSHA1());
                createFile(u.getCurRep().getRepositoryName(), u.getCurRep().getHeadBranch().getPointedCommitSHA1(), f.toPath(), new Date().getTime());
                createFile(u.getCurRep().getRepositoryName(), u.getCurRep().getHeadBranch().getPointedCommitSHA1(), Paths.get(RRHead), new Date().getTime());

                addNewObjects(false, u);
                createCommitRec(u.getUserRepo(RRu.getCurRep().getRepositoryName()).getBranchByName(RRu.getCurRep().getHeadBranch().getBranchName()).getPointedCommitSHA1(), RRu.getCurRep().getRepositoryRemotePath(), u, UM);

//    String repPath = GITRepository.getRepositoryPath() + "\\.magit\\Branches";
//    createFile(GITRepository.getHeadBranch().getBranchName(),GITRepository.getHeadBranch().getPointedCommitSHA1(),Paths.get(repPath+"\\"+intro),new Date().getTime());
//    createFile(GITRepository.getHeadBranch().getBranchName(),GITRepository.getHeadBranch().getPointedCommitSHA1(),Paths.get(repPath),new Date().getTime());
//
//                deleteFilesInFolder(Paths.get(u.getCurRep().getRepositoryRemotePath()).toFile());
//
//                createFilesInWCFromCommitObject(u.getCurRep().getHeadBranch().getPointedCommit().getRootFolder(), Paths.get(u.getCurRep().getRepositoryRemotePath()));
                return "Push was done successfully";

            }
            return "RR WC is not commited!";
        }
        return "";
    }

    public void addNewObjects(boolean isFromLocalToRemote, User u) throws IOException {
        File local = new File(u.getCurRep().getRepositoryPath().toString() + "\\.magit\\Objects");
        File remote = new File(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Objects");
        if (isFromLocalToRemote)
            mergeTwoDirectories(local, remote);
        else
            mergeTwoDirectories(remote, local);

    }

    public static void mergeTwoDirectories(File local, File remote) throws IOException {
        String targetDirPath = local.getAbsolutePath();
        File[] files = remote.listFiles();
        for (File file : files) {
            String content = extractZipFile(file.toPath());
            File f = Paths.get(local + "\\" + file.getName()).toFile();
            if (!f.exists())
                FileUtils.copyFile(file, f);
//            String content = readTextFile(file.toString());
//            createZipFile(local.toPath(),file.getName(),content);
            // file.renameTo(new File(targetDirPath + "\\" + file.getName()));
        }
    }

    public String executePushToRR(User u, UserManager UM, User RRu) throws Exception {

        if (u.getCurRep().getHeadBranch().getRemoteTrackingBranch()) {
            return executePush(u, RRu, UM);
        } else if (u.getCurRep().getHeadBranch().getRemoteBranch()) {
            return "Head Branch is an RB and cannot be pushed!";
        } else {
            String intro = u.getCurRep().getRepositoryRemoteName() + "\\";
            Branch headB = u.getCurRep().getHeadBranch();
            //Commit c = findCommitOfRRBranch(headB.getPointedCommit(), new Branch("aaa"), u);
//        Commit newCommit = new Commit("New Branch From LR", userName);
//        newCommit.setSHA1PreveiousCommit(c.getSHA());
//        newCommit.setRootFolder(headB.getPointedCommit().getRootFolder());
//        newCommit.setRootFolderSHA1(headB.getPointedCommit().getRootFolderSHA1());
//        newCommit.setCommitFileContentToSHA();
            Commit newCommit = headB.getPointedCommit();
            Branch rbB = new Branch(intro + u.getCurRep().getHeadBranch().getBranchName(), newCommit.getSha1(), true, false);
            Branch rrB = new Branch(u.getCurRep().getHeadBranch().getBranchName(), newCommit.getSha1(), false, false);
            RRu.getUserRepo(u.getCurRep().getRepositoryName()).getBranches().add(rrB);
            //Branch rtbB = new Branch( GITRepository.getHeadBranch().getBranchName(),newCommit.getSha1(),false,true);
            u.getCurRep().getBranches().add(rbB);
            //GITRepository.getBranches().add(rtbB);
            rbB.setPointedCommit(newCommit);
            rrB.setPointedCommit(newCommit);
            rrB.setPointedCommitSHA1(newCommit.getSHA());
            rbB.setPointedCommitSHA1(newCommit.getSHA());
//        rtbB.setPointedCommitS HA1(newCommit.getSHA());
//        rtbB.setPointedCommit(newCommit);
            u.getCurRep().getHeadBranch().setRemoteTrackingBranch(true);
            createFile(rbB.getBranchName(), newCommit.getSHA(), Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit\\Branches"), new Date().getTime());
            createFile(rrB.getBranchName(), newCommit.getSHA(), Paths.get(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Branches"), new Date().getTime());
            //createZipFile(u.getCurRep().getRepositoryPath(), newCommit.getSHA(), newCommit.getSHAContent());
            createZipFile(Paths.get(u.getCurRep().getRepositoryRemotePath() + "\\.magit\\Objects"), newCommit.getSHA(), newCommit.getSHAContent());
            addNewObjects(true, u);
            createCommitRec(headB.getPointedCommitSHA1(), u.getCurRep().getRepositoryRemotePath(), RRu, UM);

            //deleteFilesInFolder(Paths.get(u.getCurRep().getRepositoryRemoteName()).toFile());
            // createFilesInWCFromCommitObject(newCommit.getRootFolder(), Paths.get(u.getCurRep().getRepositoryRemotePath()));
            //  למצוא את הקומיט הראשון ברשימת קומיטים של הברנצ שמופיע בברנצ של RB זא שנמצא בברנאצ בRR
            //ליצור בתיקיות ברנצ חדש עם קומיטט חדשה של הדיפים מהקומיט שם(FF)
            //ליצור RB ולהפוך את ההד לRTB
            return "Push was done succssefully";
        }
    }

    public Commit   findCommitOfRRBranch(Commit c,Branch basicBranch,User u) {
        Commit returnCommit = null;
        Commit currCommit = c;
        boolean isFound = false;
        for (Branch b : u.getCurRep().getBranches()) {
            if (b.getRemoteBranch() == true) {
                if (b.getPointedCommit() == currCommit) {
                    returnCommit = b.getPointedCommit();
                    basicBranch = b;
                    isFound = true;
                }
            }
        }
        if (!isFound) {
            //קריאות רקורסיביות:
            if (currCommit.getSHA1PreveiousCommit() != null)//father #1
            {
                findCommitOfRRBranch(u.getCurRep().getCommitMap().get(currCommit.getSHA1PreveiousCommit()),basicBranch,u);
            }

            if (currCommit.getSHA1anotherPreveiousCommit() != null)//father #2
            {
                findCommitOfRRBranch(u.getCurRep().getCommitMap().get(currCommit.getSHA1anotherPreveiousCommit()),basicBranch,u);
            }
        }
        return returnCommit;
    }
    public void createPushRequest(Branch b,User u) throws Exception {
        Branch basicBranch = null;
        findCommitOfRRBranch(b.getPointedCommit(), basicBranch,u);
        //PushRequest pr = new PushRequest(basicBranch, b, "Pull Request from branch" + b.getBranchName() + "to" + b.getBranchName(),);
        //PRList.add(pr);

        StringBuilder sb = new StringBuilder();

        sb.append("The current status Between Branches is:\n");
        sb.append(System.lineSeparator());


        Folder newF = b.getPointedCommit().getRootFolder();
        Folder oldF = basicBranch.getPointedCommit().getRootFolder();
        createShaAndZipForNewCommit(newF,oldF,false,u.getCurRep().getRepositoryPath(),u);
        sb.append(System.lineSeparator());
        sb.append("Deleted Files's Paths:" + getDeletedFiles() + '\n');
        sb.append("Added Files's Paths:" + getCreatedFiles() + '\n');
        sb.append("Updated Files's Paths:" + getUpdatedFiles() + '\n');
        getCreatedFiles().clear();
        getDeletedFiles().clear();
        getUpdatedFiles().clear();
        //pr.setStatusOfDiff(sb.toString());
    }

    public void dealWithPR(PushRequest pr,User u) throws Exception {
        boolean isConfirm = true;
        if(isConfirm)
        {
            String intro = u.getCurRep().getRepositoryRemoteName() + "\\";

            Branch targetB = pr.getTargetBranch();
            Branch basicB = pr.getBasicBranch();

            //Commit c = findCommitOfRRBranch(headB.getPointedCommit(),new Branch("aaa"));
            //Commit newCommit = new Commit("New Branch From LR of PR", userName);
//        newCommit.setSHA1PreveiousCommit(basicB.getPointedCommitSHA1());
//        newCommit.setRootFolder(targetB.getPointedCommit().getRootFolder());
//        newCommit.setRootFolderSHA1(targetB.getPointedCommit().getRootFolderSHA1());
//        newCommit.setCommitFileContentToSHA();
            //Commit newCommit = targetB.getPointedCommit();
            u.getCurRep().getBranchByName(intro + u.getCurRep().getHeadBranch().getBranchName()).setPointedCommitSHA1(targetB.getPointedCommitSHA1());
            u.getCurRep().getBranchByName(intro + u.getCurRep().getHeadBranch().getBranchName()).setPointedCommit(targetB.getPointedCommit());

            //Branch rbB = new Branch(intro + GITRepository.getHeadBranch().getBranchName(), newCommit.getSha1(), true, false);
            //Branch rtbB = new Branch( GITRepository.getHeadBranch().getBranchName(),newCommit.getSha1(),false,true);
            //GITRepository.getBranches().add(rbB);
            //GITRepository.getBranches().add(rtbB);
//        rbB.setPointedCommit(newCommit);
//        rbB.setPointedCommitSHA1(newCommit.getSHA());
////        rtbB.setPointedCommitSHA1(newCommit.getSHA());
////        rtbB.setPointedCommit(newCommit);
            //GITRepository.getBranchByName(targetB.getBranchName()).setRemoteTrackingBranch(true);
            //GITRepository.getHeadBranch().setRemoteTrackingBranch(true);
            //
            //createFile(intro+targetB.getBranchName(),targetB.getPointedCommitSHA1(),Paths.get(GITRepository.getRepositoryPath()+"\\.magit\\Branches"),new Date().getTime());
            createFile(targetB.getBranchName(),targetB.getPointedCommitSHA1(),Paths.get(u.getCurRep().getRepositoryRemoteName()+"\\.magit\\Branches"),new Date().getTime());
            //createZipFile(GITRepository.getRepositoryPath(),newCommit.getSHA(),newCommit.getSHAContent());
            //createZipFile(Paths.get(GITRepository.getRepositoryRemotePath()),newCommit.getSHA(),newCommit.getSHAContent());
            addNewObjects(true,u);

            //לבדוק האם הוא ההד
//        deleteFilesInFolder(Paths.get(GITRepository.getRepositoryRemoteName()).toFile());
//        createFilesInWCFromCommitObject(targetB.getPointedCommit().getRootFolder(),Paths.get(GITRepository.getRepositoryRemotePath()));


            //להוריד את הPR מהרשימה בUI
        }
        else
        {

        }
        this.getPRList().remove(pr);
    }
}



//לתת אפשרות לעשות סוויץ רפוזטורי מתוך כלום
//אם עושים סוויצ רפוזטורי פעולה 11 לא עובדת, יכול להיות בגלל 2 סיבות: או שאין קישור בין קומיט לאבא שלו באובייקט עצמו, או שבסווית רפוזטורי לא מעדכנות את ההד להצביע על הקומיט הנחוץעל הקומיט הנחוץ


//            if (ourComponents.get(ourIndex).getComponentName().compareTo(theirComponents.get(theirIndex).getComponentName()) < 0) {
//                if (theirComponents.get(theirIndex).getComponentName().compareTo(fatherComponents.get(fatherIndex).getComponentName()) < 0) {
//                    isFatherExist = "1";
//
//                } else
//                    isTheirExist = "1";
//            } else if (ourComponents.get(ourIndex).getComponentName().compareTo(fatherComponents.get(fatherIndex).getComponentName()) < 0) {
//                if (theirComponents.get(theirIndex).getComponentName().compareTo(ourComponents.get(ourIndex).getComponentName()) < 0) {
//                    isTheirExist = "1";
////
//                } else {
//                    isOurExist = "1";
//                }
//            } else {
//                isOurExist = "1";
//            }
