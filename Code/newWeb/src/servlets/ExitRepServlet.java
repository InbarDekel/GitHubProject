package servlets;

import Classess.GitManager;
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

public class ExitRepServlet extends HttpServlet {
    private  final String ALL_REP="Pages//AllRep/AllRep.html";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        UserManager userManager = ServletUtils.getUserManager(getServletContext());
//        GitManager manager = ServletUtils.getGITManager(getServletContext());
        response.sendRedirect(ALL_REP);

//        File f = Paths.get(manager.getDirPath().toString() + "\\"+SessionUtils.getUsername(request)).toFile();
//        manager.deleteFilesInFolder(f);
       // userManager.removeUser(SessionUtils.getUsername(request));
        //manager.getUManager().getUser(SessionUtils.getUsername(request)).setConnected(false);
//        SessionUtils.clearSession(request);
    }
}
