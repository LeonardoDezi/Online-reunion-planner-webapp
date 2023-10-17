package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TemplateHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "CheckRegistration", value = "/CheckRegistration")
public class CheckRegistration extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public static boolean validateEmail(String email) {
        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public CheckRegistration() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
        this.connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String password_double = request.getParameter("password_repeat");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        if (name == null || surname == null || username == null || email == null || password == null || password_double == null) {
            ctx.setVariable("error","Register module missing some data");
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        if (!password.equals(password_double)) {
            ctx.setVariable("error","Passwords don't match");
            templateEngine.process(PathUtils.pathToHomePage, ctx, response.getWriter());
            return;
        }

        if (!validateEmail(email)) {
            ctx.setVariable("error","Email is not valid");
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user;

        try {
            user = userDAO.getUserByUsername(username);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        if (user != null) {
            ctx.setVariable("warning","username already exists");
            templateEngine.process(PathUtils.pathToRegisterPage, ctx, response.getWriter());
            return;
        }

        try {
            user = userDAO.getUserByEmail(email);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        if (user != null) {
            ctx.setVariable("warning","email already in use");
            templateEngine.process(PathUtils.pathToRegisterPage, ctx, response.getWriter());
            return;
        }

        try {
            userDAO.registerUser(password,name,surname,username,email);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        try {
            user = userDAO.getUserByEmailAndUsername(email, username);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("currentUser", user);
        response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToHomeServletPath);

    }
}
