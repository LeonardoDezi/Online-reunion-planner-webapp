package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.Participant;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.dao.ParticipantDAO;
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
import java.util.List;

@WebServlet(name = "GoToHome", value = "/GoToHome")
public class GoToHome extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemplateEngine templateEngine;
    private Connection connection;

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
        HttpSession session = request.getSession();
        User currentUser = (User)session.getAttribute("currentUser");

        MeetingDAO meetingDAO = new MeetingDAO(connection);
        List<Meeting> myMeetings;

        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

        try {
            myMeetings = meetingDAO.findMeetingsNotExpiredByUser(currentUser.getIdUser());
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        List<Meeting> otherMeetings;

        try {
            otherMeetings = meetingDAO.findMeetingById(currentUser.getIdUser());
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        ctx.setVariable("myMeetings",myMeetings);
        ctx.setVariable("otherMeetings", otherMeetings);
        templateEngine.process(PathUtils.pathToHomePage,ctx,response.getWriter());

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
