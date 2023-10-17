package it.polimi.tiw.controllers;

import it.polimi.tiw.beans.Meeting;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.MeetingDAO;
import it.polimi.tiw.dao.ParticipantDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.PathUtils;
import it.polimi.tiw.utils.TemplateHandler;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import javax.sound.midi.MidiMessage;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;

@WebServlet(name = "CheckInvitations", value = "/CheckInvitations")
public class CheckInvitations extends HttpServlet {
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
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        Meeting meeting = (Meeting) session.getAttribute("meetingToCreate");
        ArrayList<User> users = (ArrayList<User>) session.getAttribute("users");

        MeetingDAO meetingDAO = new MeetingDAO(connection);
        ParticipantDAO participantDAO = new ParticipantDAO(connection);

        Time time = meeting.getTime();
        Date date = meeting.getDate();
        String title = meeting.getTitle();
        int length = meeting.getLength();
        int numberOfParticipants = meeting.getNumberOfParticipants();
        int idCreator = meeting.getIdCreator();

        WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());

        Map<String, String[]> usersInRequest = request.getParameterMap();
        usersInRequest = mapCleaner(usersInRequest);
        ArrayList<User> selectedUsersList;
        Collection<String[]> selectedUsernames = new ArrayList<>(usersInRequest.values());

        try {
            selectedUsersList = new UserDAO(connection).getUsersFromUsername(selectedUsernames);
        } catch (SQLException e) {
            ctx.setVariable("error",e.getMessage());
            templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
            return;
        }

            if (selectedUsersList == null) {
                session.setAttribute("errorMessage", "Select at least a user");
                templateEngine.process(PathUtils.pathToRegistryPage, ctx, response.getWriter());
                return;
            }
            else {

                if (selectedUsersList.size() > numberOfParticipants) {
                    for (int i = 0; i < users.size(); i++) {
                        for (User value : selectedUsersList) {
                            if (users.get(i).getIdUser() == value.getIdUser()) {
                                users.remove(i);
                            }
                        }
                    }
                    session.setAttribute("errorMessage", "Too many users selected, delete at least " + (selectedUsersList.size()-numberOfParticipants));
                    session.setAttribute("alreadySelectedUsers", selectedUsersList);
                    session.setAttribute("users", users);
                    int attempts = (int) session.getAttribute("attempts");
                    if (attempts==3) {
                        session.removeAttribute("meetingToCreate");
                        session.removeAttribute("alreadySelectedUsers");
                        session.removeAttribute("attempts");
                        session.removeAttribute("users");
                        session.removeAttribute("errorMessage");
                        templateEngine.process(PathUtils.pathToCancellationPage, ctx, response.getWriter());
                        return;
                    }

                    session.setAttribute("attempts", attempts+1);
                    templateEngine.process(PathUtils.pathToRegistryPage, ctx, response.getWriter());
                    return;
                }

                int idMeeting;

                try {
                    idMeeting = meetingDAO.createMeeting(title, date, time, length, numberOfParticipants, idCreator);
                } catch (SQLException e) {
                    ctx.setVariable("error",e.getMessage());
                    templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
                    return;
                }

                try {
                    participantDAO.createParticipant(selectedUsersList,idMeeting);
                } catch (SQLException e) {
                    ctx.setVariable("error",e.getMessage());
                    templateEngine.process(PathUtils.pathToErrorPage, ctx, response.getWriter());
                    return;
                }

            }

        session.removeAttribute("meetingToCreate");
        session.removeAttribute("alreadySelectedUsers");
        session.removeAttribute("attempts");
        session.removeAttribute("users");
        session.removeAttribute("errorMessage");

        response.sendRedirect(getServletContext().getContextPath() + PathUtils.goToHomeServletPath);

    }

    private Map<String, String[]> mapCleaner (Map<String, String[]> map) {
        Map<String, String[]> cleanedMap = new LinkedHashMap<>(map);
        cleanedMap.remove("meeting");
        return cleanedMap;
    }
}
