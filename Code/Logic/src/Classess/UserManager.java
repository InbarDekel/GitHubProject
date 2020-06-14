package Classess;

import java.util.HashSet;
import java.util.Set;

public class UserManager {

    public HashSet<User> getUsersL() {
        return users;
    }

    private HashSet<User> users = new HashSet<>();

    public boolean isUserExists(String userName)
    {
        boolean isExist = false;
        for(User u:users)
        {
            if(u.getName().equals(userName))
                isExist = true;
        }
        return isExist;
    }
    public User getUser(String userName)
    {
        User user = null;
        for(User u:users)
        {
            if(u.getName().equals(userName))
                user = u;
        }
        return user;
    }
    public void addUser(String userName)
    {
        User u = new User(userName);
        users.add(u);
    }
    public void removeUser(String userName)
    {
        User user = null;
        for(User u:users)
        {
            if(u.getName().equals(userName))
                user = u;
        }
        if(user != null)
            users.remove(user);
    }
    public Set<String> getUsers()
    {
        Set<String> list = new HashSet<>();
        for (User u:users) {
            list.add(u.getName());
        }
        return list;
    }
}
