package Classess;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

public class User {

    public User(String name) {
        this.username = name;
        this.repositories = new HashSet<>();
        this.isConnected = true;
        this.pushRequests = new LinkedList<>();
        this.PRnotificationToUser = new LinkedList<>();
        this.UpdatenotificationToUser = new LinkedList<>();
        this.ForknotificationToUser = new LinkedList<>();

    }

    private String username;
    private HashSet<Repository> repositories;
    private boolean isConnected;
    private LinkedList<PushRequest> pushRequests;

    public LinkedList<Notification> getForknotificationToUser() {
        return ForknotificationToUser;
    }

    public LinkedList<Notification> getPRnotificationToUser() {
        return PRnotificationToUser;
    }

    public LinkedList<Notification> getUpdatenotificationToUser() {
        return UpdatenotificationToUser;
    }

    public void setForknotificationToUser(LinkedList<Notification> forknotificationToUser) {
        ForknotificationToUser = forknotificationToUser;
    }

    public void setPRnotificationToUser(LinkedList<Notification> PRnotificationToUser) {
        this.PRnotificationToUser = PRnotificationToUser;
    }

    public void setUpdatenotificationToUser(LinkedList<Notification> updatenotificationToUser) {
        UpdatenotificationToUser = updatenotificationToUser;
    }

    private LinkedList<Notification> ForknotificationToUser;
    private LinkedList<Notification> PRnotificationToUser;
    private LinkedList<Notification> UpdatenotificationToUser;

    public void setCurRep(Repository curRepName) {
        this.curRepName = curRepName;
    }

    public Repository getCurRep() {
        return curRepName;
    }

    private Repository curRepName;

    public void setName(String name) {
        this.username = name;
    }

    public void setRepositories(HashSet<Repository> repositories) {
        this.repositories = repositories;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public void setPushRequests(LinkedList<PushRequest> pushRequests) {
        this.pushRequests = pushRequests;
    }



    public String getName() {
        return username;
    }

    public HashSet<Repository> getRepositories() {
        return repositories;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public LinkedList<PushRequest> getPushRequests() {
        return pushRequests;
    }



    public Repository getUserRepo(String repName)
    {
        Repository repo = null;
        for(Repository r:repositories)
        {
            if(r.getRepositoryName().equals(repName))
                repo = r;
        }
        return repo;
    }
    public boolean isRepoExsitByName(String name)
    {
        boolean isRepoExist = false;
        for(Repository r:repositories)
        {
            if(r.getRepositoryName().equals(name))
                isRepoExist = true;
        }
        return isRepoExist;

    }
    public Set<String> getRepStringList()
    {
        Set<String> list = new HashSet<>();
        for (Repository r:repositories)
        {
            list.add(r.getRepositoryName());
        }
        return list;
    }
    public PushRequest getPRByDesc(String desc)
    {
        PushRequest pr = null;
        for (PushRequest p:pushRequests)
        {
            if(p.getDescriptionOfPush().equals(desc))
            {
                pr = p;
            }
        }
        return pr;
    }
}
