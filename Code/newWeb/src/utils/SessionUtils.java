package utils;


import Classess.PushRequest;
import Classess.Repository;
import constants.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;

public class SessionUtils {

    public static String getUsername (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }
    public static String getRepname (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.REP) : null;
        Repository r = (Repository)sessionAttribute;
        return sessionAttribute != null ? r.getRepositoryName() : null;
    }
    public static String getFilePath (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.FILE) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }
    public static PushRequest getPR(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.PR) : null;

        return sessionAttribute != null ? (PushRequest)sessionAttribute : null;
    }
    public static void clearSession (HttpServletRequest request) {
        request.getSession().invalidate();
    }
}