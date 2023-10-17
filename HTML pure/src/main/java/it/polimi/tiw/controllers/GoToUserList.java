package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Meeting;
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
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

@WebServlet(name = "GoToUserList", value = "/GoToUserList")
public class GoToUserList extends HttpServlet {
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
        doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        boolean isBadRequest;
        String title = null;
        Date date = null;
        Time time = null;
        Integer length = null;
        Integer numberOfParticipants = null;

        WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());

        try {

            title = request.getParameter("title");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            date = simpleDateFormat.parse(request.getParameter("date"));
            time = Time.valueOf(request.getParameter("time")+":00");
            length = Integer.parseInt(request.getParameter("duration"));
            numberOfParticipants = Integer.parseInt(request.getParameter("maxParticipantsNumber"));

            Date todayDate = simpleDateFormat.parse(simpleDateFormat.format(new Date()));

            isBadRequest = title == null || title.isEmpty() || date == null || date.before(todayDate)
                    || length < 0 || numberOfParticipants < 0;

        } catch (IllegalArgumentException | NullPointerException | ParseException e) {
            isBadRequest = true;
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }
        if (isBadRequest) {
            ctx.setVariable("errorMessage","Something went wrong with the meeting creation");
            templateEngine.process(PathUtils.pathToHomePage, ctx, response.getWriter());
            return;
        }

        if ( length == 0 || numberOfParticipants == 0) {
            ctx.setVariable("errorMessage", "Length of the meeting or participants number can't be 0");
            templateEngine.process(PathUtils.pathToHomePage, ctx, response.getWriter());
            return;
        }

        Meeting meetingToCreate = new Meeting();
        meetingToCreate.setTitle(title);
        meetingToCreate.setDate(date);
        meetingToCreate.setTime(time);
        meetingToCreate.setLength(length);
        meetingToCreate.setNumberOfParticipants(numberOfParticipants);

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        meetingToCreate.setIdCreator(user.getIdUser());

        session.setAttribute("meetingToCreate", meetingToCreate);

        UserDAO userDAO = new UserDAO(connection);
        ArrayList<User> users ;

        try {
            users = userDAO.getUsers();
        } catch (SQLException e){
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

        session.setAttribute("users", users);
        session.setAttribute("attempts", 1);

        templateEngine.process(PathUtils.pathToRegistryPage, ctx, response.getWriter());
    }
}
