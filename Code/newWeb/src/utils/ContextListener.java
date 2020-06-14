package utils;

import Classess.GitManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.nio.file.Paths;

public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        new File("c:\\magit-ex3").mkdirs();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        GitManager.deleteAllFilesInFolder(Paths.get("c:\\magit-ex3").toFile());
                }
}
