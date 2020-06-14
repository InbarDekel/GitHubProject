package utils;


import Classess.*;
import chat.ChatManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static constants.Constants.INT_PARAMETER_ERROR;


public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
	private static final String GIT_MANAGER_ATTRIBUTE_NAME = "GITManager";
	private static final String GIT_REP_ATTRIBUTE_NAME = "RepManager";
	private static final String GIT_CHAT_ATTRIBUTE_NAME = "chatManagerLock";


	/*
	Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
	the actual fetch of them is remained un-synchronized for performance POV
	 */
	private static final Object userManagerLock = new Object();
	private static final Object gitManagerLock = new Object();
	private static final Object gitRepLock = new Object();
	private static final Object chatManagerLock = new Object();


	public static UserManager getUserManager(ServletContext servletContext) {

		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}
//	public static User getGITUser(ServletContext servletContext,HttpServletRequest request) {
//		synchronized (gitUserLock) {
//			if (servletContext.getAttribute(GIT_USER_ATTRIBUTE_NAME) == null) {
//				servletContext.setAttribute(GIT_USER_ATTRIBUTE_NAME, new User(request.getParameter()));
//			}
//		}
//		return (User) servletContext.getAttribute(GIT_USER_ATTRIBUTE_NAME);
//	}
	public static GitManager getGITManager(ServletContext servletContext) {
		synchronized (gitManagerLock) {
			if (servletContext.getAttribute(GIT_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(GIT_MANAGER_ATTRIBUTE_NAME, new GitManager());
			}
		}
		return (GitManager) servletContext.getAttribute(GIT_MANAGER_ATTRIBUTE_NAME);
	}
	public static Repository getRepository(ServletContext servletContext) {
		synchronized (gitRepLock) {
			if (servletContext.getAttribute(GIT_REP_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(GIT_REP_ATTRIBUTE_NAME, new Repository());
			}
		}
		return (Repository) servletContext.getAttribute(GIT_REP_ATTRIBUTE_NAME);
	}
	public static int getIntParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException numberFormatException) {
			}
		}
		return INT_PARAMETER_ERROR;
	}
	public static ChatManager getChatManager(ServletContext servletContext) {
		synchronized (chatManagerLock) {
			if (servletContext.getAttribute(GIT_CHAT_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(GIT_CHAT_ATTRIBUTE_NAME, new ChatManager());
			}
		}
		return (ChatManager) servletContext.getAttribute(GIT_CHAT_ATTRIBUTE_NAME);
	}


}
