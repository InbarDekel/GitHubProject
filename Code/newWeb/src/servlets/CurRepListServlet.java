package servlets;

import Classess.*;
import com.google.gson.Gson;

import utils.ServletUtils;
import utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class CurRepListServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            //GitManager manager = ServletUtils.getGITManager(getServletContext());
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            User u = userManager.getUser(SessionUtils.getUsername(request));

//            User u = ServletUtils.getGITUser(getServletContext());
            Set<RepInfo> repInfos = new HashSet<>();
            for (Repository r:u.getRepositories())
            {
                RepInfo RI = new RepInfo();
                RI.setName(r.getRepositoryName());
                RI.setActiveBranch(r.getHeadBranch().getBranchName());
                RI.setAmountOfBranches(r.getBranches().size());
                Commit c = RepInfo.getTheMostUpdatesCommit(r.getCommitMap());
                RI.setDateOfLastCommit(c.getCreationDate());
                RI.setDescriptionOfLastCommit(c.getDescription());
                                repInfos.add(RI);
            }
            //Set<String> repList = u.getRepStringList();
            String json = gson.toJson(repInfos);
            out.println(json);
            out.flush();
        }
    }
}