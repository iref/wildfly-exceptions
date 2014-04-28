package cz.muni.exceptions.source.integration;

import org.jboss.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Jan Ferko
 */
@WebServlet(urlPatterns = "/*")
public class LoggingMockServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoggingMockServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        IllegalArgumentException exp = new IllegalArgumentException("Something wrong happened");
        LOGGER.error("Error logged", exp);

        resp.getWriter().append("Hello World!");
        resp.getWriter().close();
    }
}
