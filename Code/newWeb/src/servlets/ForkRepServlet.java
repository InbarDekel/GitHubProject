package servlets;

import Classess.*;
import com.google.gson.Gson;
import constants.Constants;

import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Set;

import static utils.SessionUtils.getUsername;

public class ForkRepServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String returned = "";
            String repName = request.getParameter("repName");
            UserManager userManager = ServletUtils.getUserManager(getServletContext());

            GitManager manager = ServletUtils.getGITManager(getServletContext());
            User u = userManager.getUser(SessionUtils.getUsername(request));
            if (u.getUserRepo(repName) != null) {
                returned = "Repository with the same name already exist!";
            } else {
                User forkU = userManager.getUser(request.getParameter("user"));
                Repository r = forkU.getUserRepo(repName);
                Notification n = new Notification();
                n.setUserToShowTo(forkU);
                n.setDateOfNotification(GitManager.getDateFromObject(new Date()));
                n.setMessage(repName + "was forked by " + u.getName());
                forkU.getForknotificationToUser().add(n);
                try {
                    manager.CloneRepository(manager.getDirPath() + "\\" + forkU.getName(), manager.getDirPath() + "\\" + u.getName(), r.getRepositoryName(), u, forkU, userManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //u.getRepositories().add(r);
            //request.getSession(true).setAttribute(Constants.GAMENAME, gameName.trim());
            String json = gson.toJson(returned);

            out.println(json);
            out.flush();
            //}
        }
    }
}
