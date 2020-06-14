package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import Classess.GitManager;
import Classess.Repository;
import Classess.User;
import Classess.UserManager;
import com.google.gson.Gson;
import generated.MagitRepository;
import utils.ServletUtils;
import utils.SessionUtils;

public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String xmlContent = request.getParameter("file");
        String creator = SessionUtils.getUsername(request);
        Gson gson = new Gson();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        GitManager manager = ServletUtils.getGITManager(getServletContext());
        User u = ServletUtils.getUserManager(getServletContext()).getUser(creator);
        String message = "";

        try {
            MagitRepository newRep = Repository.loadFromXml(xmlContent);
            if (u.isRepoExsitByName(newRep.getName()))
                message = "Repository Name already exist!";
            else {

                Path mainPath = manager.getDirPath();
                Path repPath = Paths.get(mainPath.toString() + "\\" + u.getName() + "\\" + newRep.getName());
                UserManager userManager = ServletUtils.getUserManager(getServletContext());
                Repository r = new Repository(repPath, u);

                message = manager.convertOldRepoToNew(r, newRep, u, userManager);

                if (message == "") {
                    u.getRepositories().add(r);
                    u.setCurRep(r);
                    manager.endXMLLoad(newRep, r, u);

                }
            }
            String json = gson.toJson(message);
            out.println(json);
            out.flush();
        } catch (Exception e) {
        }
    }

//    private void buildGameAndAddToGamesList(GameDescriptor gameDescriptor, String creator) {
//        GameManager gameManager = ServletUtils.getGameManager(getServletContext());
//        gameManager.addGame(new Game(gameDescriptor, creator));
//    }
}
