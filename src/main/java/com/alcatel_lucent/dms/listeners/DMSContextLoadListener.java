package com.alcatel_lucent.dms.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Created with IntelliJ IDEA.
 * User: guoshunw
 * Date: 13-11-12
 * <p/>
 * see http://www.slf4j.org/api/org/slf4j/bridge/SLF4JBridgeHandler.html
 */
public class DMSContextLoadListener implements ServletContextListener {
    private static Logger log = LoggerFactory.getLogger(DMSContextLoadListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // Optionally remove existing handlers attached to j.u.l root logger
        SLF4JBridgeHandler.removeHandlersForRootLogger();  // (since SLF4J 1.7.5)

        // add SLF4JBridgeHandler to j.u.l's root logger, should be done once during
        // the initialization phase of your application
        SLF4JBridgeHandler.install();

        // print internal state
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        StatusPrinter.print(lc);
        final ServletContext context = sce.getServletContext();
        SessionCookieConfig scc = context.getSessionCookieConfig();
        String path = context.getContextPath();
        if (!path.endsWith("/")) path += "/";
        scc.setName("DMS_JSESSIONID");
        scc.setPath(path);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
//        the driver should be removed before undeploying
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            log.info("De register driver {}", driver.getClass().getName());
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
