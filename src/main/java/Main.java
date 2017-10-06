import accounts.UserDAO;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import servlets.DateServlet;
import servlets.Errors.Error500;
import servlets.MainServlet;
import utils.FSUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws SQLException {
        Map<String, String> cfg = null;
        try {
            cfg = FSUtils.file2Map(Paths.get("../WebDate/cfg.txt"));
        } catch (IOException e) {
            System.out.println("не могу найти cfg.txt");
            System.out.println(Paths.get("cfg.txt").toAbsolutePath());
            System.exit(0);
        }
        Server server = new Server(Integer.parseInt(cfg.get("port")));
        Connection connection = DriverManager.getConnection(
                cfg.get("jdbcurl"),
                cfg.get("login"),
                cfg.get("pass")
        );
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        contextHandler.getServletContext().setAttribute("Connection", connection);
        contextHandler.getServletContext().setAttribute("UserDAO", new UserDAO(connection));
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("public_html");
        resourceHandler.setDirectoriesListed(true);
        HandlerList list = new HandlerList();
        list.setHandlers(new Handler[]{contextHandler, resourceHandler});
        server.setHandler(list);
        contextHandler.addServlet(MainServlet.class, "/*");
        contextHandler.addServlet(DateServlet.class, "/date");
        contextHandler.addServlet(Error500.class, "/error500");

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        System.out.println("connection не закрыт!");
                    }
                }
        ));
        try {
            server.start();
            System.out.println("Server started");
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
