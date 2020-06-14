package servlets;//package servlets;

import Classess.*;
import com.google.gson.Gson;
import constants.Constants;

import org.apache.commons.io.FileUtils;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static utils.SessionUtils.getUsername;

public class ActionServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        GitManager manager = ServletUtils.getGITManager(getServletContext());
        User u = userManager.getUser(SessionUtils.getUsername(request));
        String rep = SessionUtils.getRepname(request);
        Repository r = u.getUserRepo(rep);
        switch (action) {
            case "getHeadBInfo":
                getHeadBInfo(request, response, r, userManager, u);
                break;
            case "GetBranchesInfo":
                GetBranchesInfo(request, response, r, userManager, u);
                break;
            case "GetCommitsInfo":
                GetCommitsInfo(request, response, r, userManager, u);
                break;
            case "showCommitFiles":
                String c = request.getParameter("commit");
                showCommitFiles(request, response, c, u, userManager);
                break;
            case "Checkout":
                    Checkout(request, response, u, manager);
                break;
            case "Push":
                Push(request, response, u, manager,userManager);
                break;
            case "Pull":
                Pull(request, response, u, manager,userManager);
                break;
            case "listOfChanges":
                listOfChanges(request, response, manager,u);
                break;
            case "getRemoteInfo":
                getRemoteInfo(request,response,r,u);
                break;
            case "newBranch":
                newBranch(request,response,r,manager,u);
                break;
            case "GetWC":
                GetWC(request,response,r,manager,u);
                break;
            case "OpenFile":
                OpenFile(request,response,manager);
                break;
            case "OpenChangedFile":
                OpenChangedFile(request,response,manager,u);
                break;
            case "AddNewFile":
                AddNewFile(request,response,r,manager,u);
                break;
            case "DeleteFile":
                DeleteFile(request,response,r,manager,u);
                break;
            case "SaveFile":
                SaveFile(request,response,r,manager,u);
                break;
            case "CreateCommit":
                CreateCommit(request,response,r,manager,u);
                break;
            case "createPR":
                createPR(request,response,r,manager,u,userManager);
                break;
            case "prList":
                prList(request,response,r,manager,u);
                break;
            case "PRCommit":
                PRCommit(request,response,r,manager,u);
                break;
            case "DeclinePR":
                DeclinePR(request,response,r,manager,u);
                break;
            case "AcceptPR":
                AcceptPR(request,response,r,manager,u);
                break;
            case "OpenFileWC":
                OpenFileWC(request,response,r,manager,u);
                break;
            case "AddNewFolder":
                AddNewFolder(request,response,r,manager,u);
                break;
            case "DeleteFolder":
                DeleteFolder(request,response,r,manager,u);
                break;
            case "DeleteBranch":
                DeleteBranch(request,response,r,manager,u,userManager);
                break;
            case "GetPR":
                GetPR(request,response,r,manager,u);
                break;

                    }
    }
    private void AcceptPR(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            String returned = "";
            Gson gson = new Gson();
            PushRequest pr = SessionUtils.getPR(request);
            if(pr.isOpenStatus().equals("Approved")||pr.isOpenStatus().equals("Declined")) {
                returned = "Pr already was handled!";
            }
            else {
                User RRu = pr.getProposedUser();
                Branch base = pr.getBasicBranch();
                Branch target = pr.getTargetBranch();
                manager.newMerge(base, target, u, RRu, pr.getRepName());

                pr.setOpenStatus("Approved");
                Notification n = new Notification();
                n.setUserToShowTo(pr.getProposedUser());
                n.setDateOfNotification(GitManager.getDateFromObject(new Date()));
                n.setMessage("PR" + pr.getDescriptionOfPush() + "\n from " + pr.getTargetBranch().getBranchName() + " to " + pr.getBasicBranch().getBranchName() + "\n was Approved!");
                n.setMessageToShow(n.getMessage());
                pr.getProposedUser().getUpdatenotificationToUser().add(n);
            }
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void DeclinePR(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        String returned = "";
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String desc = request.getParameter("Desc");
            if(desc == null)
            {
                returned = "no description was inserted";
            }
            else {
                PushRequest pr = SessionUtils.getPR(request);
                if(pr.isOpenStatus().equals("Approved")||pr.isOpenStatus().equals("Declined")) {
                    returned = "Pr already was handled!";
                }
                else {
                    pr.setOpenStatus("Declined");
                    Notification n = new Notification();
                    n.setUserToShowTo(pr.getProposedUser());
                    n.setDateOfNotification(GitManager.getDateFromObject(new Date()));
                    n.setMessage("PR" + pr.getDescriptionOfPush() + "\n from " + pr.getTargetBranch().getBranchName() + " to " + pr.getBasicBranch().getBranchName() + "\n was declined because \n" + desc);
                    n.setMessageToShow(n.getMessage());
                    pr.getProposedUser().getUpdatenotificationToUser().add(n);
                }
            }
            String json = gson.toJson(returned);

            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void PRCommit(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String description = request.getParameter("pr");
            String[] p = description.split(" ");
            if (p[p.length-1].equals("Status:Open")) {
                description = description.replace(" Status:Open","");
            } else if (p[p.length-1].equals("Status:Declined")) {
                description = description.replace(" Status:Declined","");

            } else if (p[p.length-1].equals("Status:Approved")) {
                description = description.replace(" Status:Approved","");

            }
            PushRequest pr = u.getPRByDesc(description);
            request.getSession(true).setAttribute(Constants.PR, pr);


            LinkedList<String> list = manager.getChangedCommits(pr,r);
            String json = gson.toJson(list);

            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void prList(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
LinkedList<String> list = new LinkedList<>();
for (PushRequest p:u.getPushRequests())
{

    list.add(p.getDescriptionOfPush()+" Status:"+p.isOpenStatus());
}
            String json = gson.toJson(list);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void GetPR(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String description = request.getParameter("pr");
            String[] p = description.split(" ");
            if (p[p.length-1].equals("Status:Open")) {
                description = description.replace(" Status:Open","");
            } else if (p[p.length-1].equals("Status:Declined")) {
                description = description.replace(" Status:Declined","");

            } else if (p[p.length-1].equals("Status:Approved")) {
                description = description.replace(" Status:Approved","");

            }
            PushRequest pr = u.getPRByDesc(description);
String s = pr.getProposedUser().getName()+" Created the PR from "+pr.getTargetBranch().getBranchName() + " to "+pr.getBasicBranch().getBranchName()+ " On "+ pr.getPRDate();
            String json = gson.toJson(s);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void createPR(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u,UserManager UM) {
        try (PrintWriter out = response.getWriter()) {
            String returned = "PR was created successfully";
            Gson gson = new Gson();
            String user = request.getParameter("u");
            User RRU = UM.getUser(user);
            String tName = request.getParameter("target");
            String BName = request.getParameter("base");
            String description = request.getParameter("desc");
            if (tName == null || BName == null || description == null) {
                returned = "Not all fields inserted correctly!";
            } else {
                Branch target = RRU.getUserRepo(r.getRepositoryName()).getBranchByName(request.getParameter("target"));
                if (target == null) {
                    returned = "Target branch is not a branch in RR Repository!";
                } else {
                    if (target.getRemoteBranch() == true || target.getRemoteTrackingBranch() == true) {
                        returned = "target branch is not a local branch!";
                    } else {
                        Branch base = RRU.getUserRepo(r.getRepositoryName()).getBranchByName(request.getParameter("base"));
                        if (base == null) {
                            returned = "Base branch is not a branch in RR Repository!";
                        } else {
                            PushRequest p = new PushRequest(base, target, description, RRU, u);
                            p.setMessageToShow("PR: " + p.getDescriptionOfPush() + "\n, on Repository " + r.getRepositoryName() + " by " + u.getName() + "\n from branch " + p.getTargetBranch().getBranchName() + "\n to branch " + p.getBasicBranch().getBranchName());
                            RRU.getPushRequests().add(p);
                            Notification n = new Notification();
                            n.setUserToShowTo(RRU);
                            n.setDateOfNotification(GitManager.getDateFromObject(new Date()));
                            n.setMessage(p.getMessageToShow());
                            n.setMessageToShow(p.getMessageToShow());
                            RRU.getPRnotificationToUser().add(n);
                            manager.createPRFile(r, p);
                            manager.createFilesInWCFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolder(), Paths.get(r.getRepositoryRemotePath() + "\\.magit\\PR" + p.getBasicBranch().getBranchName() + p.getTargetBranch().getBranchName()));
                        }
                    }
                }
            }
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void CreateCommit(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
       String returned = "";
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String description = request.getParameter("desc");
            if(description == null|description.isEmpty())
            {
                returned = "Please insert a description in order to perform commit";
            }
            else {
                returned = manager.ExecuteCommit(description, true, u);
            }
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void SaveFile(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String FilePath = SessionUtils.getFilePath(request);
            String FileContent = request.getParameter("content");
            File f = Paths.get(FilePath).toFile();
            String name = f.getName();
            Path p = Paths.get(FilePath.replace("\\" + name, ""));
            f.delete();
            GitManager.createFile(f.getName(), FileContent, p, new Date().getTime());

            //GitManager.createFile(N,"",Paths.get(P),new Date().getTime());
//            Folder f = manager.generateFolderFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
           // List<String> list = new LinkedList<>();
//            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson("");
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void DeleteFile(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
      String returned = "" ;
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String FilePath = SessionUtils.getFilePath(request);
            //String FileContent = SessionUtils.getUsername(request);
            File f = Paths.get(FilePath).toFile();
            if(f.exists()) {
                f.delete();
            }
            else
            {
                returned = "File does not exist!";
            }
            //GitManager.createFile(N,"",Paths.get(P),new Date().getTime());
//            Folder f = manager.generateFolderFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
//            List<String> list = new LinkedList<>();
//            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void AddNewFile(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
      String returned = "";
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String N = request.getParameter("name");
            String P = request.getParameter("path");
            if(N != null && P != null) {

                Path small = u.getCurRep().getRepositoryPath();
                Path big = Paths.get(P);
               try {
                   Path relativize = small.relativize(big);
                   GitManager.createFile(N, "", Paths.get(P), new Date().getTime());
                   returned = "";
               }
               catch (Exception e)
               {
                   returned = "Path is not relative to repository path!";

               }

            }
            else {
                returned = "Path or name was not inserted";

            }
//            Folder f = manager.generateFolderFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
//            List<String> list = new LinkedList<>();
//            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void AddNewFolder(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        String returned = "";

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String N = request.getParameter("name");
            String P = request.getParameter("path");
            if(N != null && P != null) {

                Path small = u.getCurRep().getRepositoryPath();
                Path big = Paths.get(P+"\\"+N);
                Path relativize = small.relativize(big);
                if (!small.toString().equals(relativize.toString())) {
                    returned = "The path in not in the Repository Path";
                } else {
                    new File(P + "\\" + N).mkdirs();
                }
            }
            else {
                returned = "Path or name was not inserted";
            }
//            GitManager.createFile(N,"",Paths.get(P),new Date().getTime());
//            Folder f = manager.generateFolderFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
//            List<String> list = new LinkedList<>();
//            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void DeleteFolder(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
       String returned = "" ;
       response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String N = request.getParameter("name");
            String P = request.getParameter("path");
            if(N != null && P != null) {
                File f = Paths.get(P + "\\" + N).toFile();
                if (f.exists()) {
                    manager.deleteFilesInFolder(f);//
                } else {
                    returned = "Folder does not exist!";
                }
            }
            else {
                returned = "Path or name was not inserted";
            }
                //         GitManager.createFile(N,"",Paths.get(P),new Date().getTime());
//            Folder f = manager.generateFolderFromCommitObject(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
//            List<String> list = new LinkedList<>();
//            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void GetWC(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();

            Folder f = manager.GenerateFolderFromWC(Paths.get(manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName()),u);
                    //(r.getHeadBranch().getPointedCommit().getRootFolderSHA1(),u.getCurRep().getRepositoryPath().toString());
List<String> list = new LinkedList<>();
            manager.showFilesOfCommitRecAsList(f,list,manager.getDirPath().toString()+"\\"+u.getName()+"\\"+r.getRepositoryName());
            String json = gson.toJson(f);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void DeleteBranch(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u,UserManager UM) {
        String  returned= "Deletion of branch was successfully done.";
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();

            String s = request.getParameter("Bname");
            if(r.getHeadBranch().getBranchName().equals(s))
            {
                returned = "Head branch cannot be deleted!";
            }
            else if(s ==null)
            {
                returned = "No branch was inserted";
            }
            else {
                String RRu = request.getParameter("u");
                User RU = UM.getUser(RRu);

                returned = manager.DeleteBranch(s, u, RU);
            }
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void newBranch(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        String  returned= "Creation of branch was successfully done.";

        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();


            String s = request.getParameter("Bname");
            if(s != null) {
                if (r.getBranchByName(s) != null) {
                    returned = "Branch name already exist!";
                } else {
                    manager.CreatBranch(s, u);
                }
            }
            else {
                returned = "no name was inserted";
            }
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void listOfChanges(HttpServletRequest request, HttpServletResponse response, GitManager manager,User u) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            manager.ExecuteCommit("",false,u);
            String json = gson.toJson(manager.toString());
            manager.getCreatedFiles().clear();
            manager.getDeletedFiles().clear();
            manager.getUpdatedFiles().clear();
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void OpenChangedFile(HttpServletRequest request, HttpServletResponse response, GitManager manager,User u) {

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String r = request.getParameter("file");
            String[] p = r.split(" ");
            String newR = r;
            PushRequest pr = SessionUtils.getPR(request);
            if (p[p.length-1].equals("(Updated)")) {
                newR = r.replace(" (Updated)","");
                Path small = u.getCurRep().getRepositoryPath();
                Path big = Paths.get(newR);
                Path relativize = small.relativize(big);
                Path Fp = Paths.get(u.getCurRep().getRepositoryPath() + "\\.magit\\PR"+pr.getBasicBranch().getBranchName()+pr.getTargetBranch().getBranchName()+"\\"+relativize);
            newR = Fp.toString();
            } else if (p[p.length-1].equals("(Added)")) {
                newR = r.replace(" (Added)","");

            } else if (p[p.length-1].equals("(Deleted)")) {
                newR = r.replace(" (Deleted)","");

            }

            File f = Paths.get(newR).toFile();
            List<String> list = new LinkedList<>();
            list.add(r);
            list.add(GitManager.readTextFile(f.toString()));

            String json = gson.toJson(list);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void OpenFile(HttpServletRequest request, HttpServletResponse response, GitManager manager) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String r = request.getParameter("file");
            String name = request.getParameter("name");
            List<String> list = new LinkedList<>();
            if(name == "")
            {
                list.add(r);
            }
            else {
                list.add(r + "\\" + name);
            }
            list.add(GitManager.readTextFile(r));
            request.getSession(true).setAttribute(Constants.FILE,r);
            request.getSession(true).setAttribute(Constants.FILE_CONTENT,GitManager.readTextFile(r));

//u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(r));
            //manager.executePushToRR(u);
//            GitManager manager = ServletUtils.getGITManager(getServletContext());
//            Commit c = u.getCurRep().getCommitMap().get(cSHA);
//            String files = manager.showFilesOfCommit(c,u);
////            Ma

            String json = gson.toJson(list);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void OpenFileWC(HttpServletRequest request, HttpServletResponse response,Repository r, GitManager manager,User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String fileName = request.getParameter("file");
            String path = request.getParameter("path");
            String rPath = r.getRepositoryPath().toString();
            String filePath = rPath +path+"\\"+ fileName;
            List<String> list = new LinkedList<>();
            list.add(filePath);
            list.add(GitManager.readTextFile(filePath));
            request.getSession(true).setAttribute(Constants.FILE, filePath);
            request.getSession(true).setAttribute(Constants.FILE_CONTENT, GitManager.readTextFile(filePath));

//u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(r));
            //manager.executePushToRR(u);
//            GitManager manager = ServletUtils.getGITManager(getServletContext());
//            Commit c = u.getCurRep().getCommitMap().get(cSHA);
//            String files = manager.showFilesOfCommit(c,u);
////            Ma

            String json = gson.toJson(list);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void Pull(HttpServletRequest request, HttpServletResponse response, User u, GitManager manager,UserManager UM) {
        String returned = "";
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            //String r = request.getParameter("brnach");
//u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(r));
            returned = manager.executePull(u,true,false,UM);
//            GitManager manager = ServletUtils.getGITManager(getServletContext());
//            Commit c = u.getCurRep().getCommitMap().get(cSHA);
//            String files = manager.showFilesOfCommit(c,u);
////            Ma
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Push(HttpServletRequest request, HttpServletResponse response, User u, GitManager manager,UserManager UM) {
        String answer = "";
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String user = request.getParameter("name");
            if(user.equals("no remote Owner."))
            {
                answer ="No Remote Repository defined!";
            }
            else {
                User RRU = UM.getUser(user);
                String r = request.getParameter("brnach");
//u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(r));
                answer = manager.executePushToRR(u, UM, RRU);
//            GitManager manager = ServletUtils.getGITManager(getServletContext());
//            Commit c = u.getCurRep().getCommitMap().get(cSHA);
//            String files = manager.showFilesOfCommit(c,u);
////            Ma
            }
            String json = gson.toJson(answer);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Checkout(HttpServletRequest request, HttpServletResponse response, User u,GitManager manager) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String r = request.getParameter("branch");
            //u.getCurRep().setHeadBranch(u.getCurRep().getBranchByName(r));
            String[] p = r.split(" ");
            if (p[p.length-1].equals("\n(RB)")) {
                r = r.replace(" \n(RB)","");
            } else if (p[p.length-1].equals("\n(RTB)")) {
                r = r.replace(" \n(RTB)", "");
            }
               String returned =  manager.executeCheckout(r,u);
//            GitManager manager = ServletUtils.getGITManager(getServletContext());
//            Commit c = u.getCurRep().getCommitMap().get(cSHA);
//            String files = manager.showFilesOfCommit(c,u);
////            Ma
            String json = gson.toJson(returned);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCommitFiles(HttpServletRequest request, HttpServletResponse response, String cSHA, User u, UserManager UN) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            GitManager manager = ServletUtils.getGITManager(getServletContext());
            Commit c = u.getCurRep().getCommitMap().get(cSHA);
            String files = manager.showFilesOfCommit(c, u);
            String json = gson.toJson(files);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void GetCommitsInfo(HttpServletRequest request, HttpServletResponse response, Repository r, UserManager UN, User u) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {

            Gson gson = new Gson();
            GitManager manager = ServletUtils.getGITManager(getServletContext());

            Map<String, Commit> map = new HashMap<>();
            manager.createCommitRecOnlyForHead(map, r.getHeadBranch().getPointedCommitSHA1(), r.getRepositoryPath().toString(), u,UN);
            List<Commit> list = r.getSortedCommitList(map);
            for (Commit c : list) {
                c.setChanger(r.getCommitMap().get(c.getSHA()).getChanger());
            }

            LinkedList<CommitInfo> Clist = new LinkedList<>();
            for(Commit c: list)
            {
                CommitInfo ci = new CommitInfo(c.getCreationDate(),c.getChanger().getName(),c.getSHA(),c.getDescription());
                Clist.add(ci);
            }
            String json = gson.toJson(Clist);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void GetBranchesInfo(HttpServletRequest request, HttpServletResponse response, Repository r, UserManager UN, User u) {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            LinkedList<Branch> list = r.getBranches();
LinkedList<BranchInfo> Blist = new LinkedList<>();
for(Branch b: list)
{
    BranchInfo bi = new BranchInfo(b.getBranchName(),b.getPointedCommit().getDescription());
    Blist.add(bi);
    if(b.getRemoteBranch())
    {
        bi.setName(b.getBranchName()+" \n(RB)");
    }
    if(b.getRemoteTrackingBranch())
    {
        bi.setName(b.getBranchName()+" \n(RTB)");
    }

}
            String json = gson.toJson(Blist);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getHeadBInfo(HttpServletRequest request, HttpServletResponse response, Repository r, UserManager UN, User u) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String json = gson.toJson(u.getCurRep().getHeadBranch().getBranchName());
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getRemoteInfo(HttpServletRequest request, HttpServletResponse response, Repository r,User u) {
        response.setContentType("application/json");
//        int fromIndex = Integer.parseInt(request.getParameter("indexMsg"));
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            LinkedList<String> list = new LinkedList<>();
            if (r.getRepositoryRemoteName() == null) {
                list.add("no remote Repository. ");
                list.add("no remote Owner.");

            } else {
                list.add(r.getRepositoryRemoteName());
                list.add(u.getCurRep().getUserOfRepo().getName());
            }
            String json = gson.toJson(list);
            out.println(json);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
