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

@WebServlet(name = "CheckLogin", value = "/CheckLogin")
public class CheckLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;
    private TemplateEngine templateEngine;

    public CheckLogin() {
        super();
    }

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
        this.templateEngine = TemplateHandler.getEngine(servletContext, ".html");
        this.connection = ConnectionHandler.getConnection(servletContext);
    }

    @Override
    public void destroy(){
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

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            ctx.setVariable("error","Please insert your credentials");
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        UserDAO userDAO = new UserDAO(connection);
        User user;

        try {
            user = userDAO.findUser(email, password);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        if (user == null) {
            ctx.setVariable("warning","Email or password incorrect");
            templateEngine.process(PathUtils.pathToLoginPage, ctx, response.getWriter());
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("currentUser",user);
        response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToHomeServletPath);
    }

}
