package servlets;

import Classess.GitManager;
import Classess.User;
import Classess.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class ExitAllRepServlet extends HttpServlet {
    private  final String INDEX_URL="AllRep.html";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        GitManager manager = ServletUtils.getGITManager(getServletContext());
        String usernameFromSession = SessionUtils.getUsername(request);

        //File f = Paths.get(manager.getDirPath().toString() + "\\"+SessionUtils.getUsername(request)).toFile();
        //manager.deleteFilesInFolder(f);
        //userManager.removeUser(SessionUtils.getUsername(request));
        userManager.getUser(SessionUtils.getUsername(request)).setConnected(false);
        if (usernameFromSession != null) {
           // System.out.println("Clearing session for " + usernameFromSession);
            //userManager.removeUser(usernameFromSession);
            String user = SessionUtils.getUsername(request);
            User u = userManager.getUser(user);
            u.getForknotificationToUser().clear();
            u.getPRnotificationToUser().clear();
            u.getUpdatenotificationToUser().clear();
            SessionUtils.clearSession(request);

            /*
            when sending redirect, tomcat has a shitty logic how to calculate the URL given, weather its relative or not
            you can read about it here:
            https://tomcat.apache.org/tomcat-5.5-doc/servletapi/javax/servlet/http/HttpServletResponse.html#sendRedirect(java.lang.String)
            the best way (IMO) is to fetch the context path dynamically and build the redirection from it and on
             */

            //response.sendRedirect(request.getContextPath() + "/_sign.html");
        }

    }
}
