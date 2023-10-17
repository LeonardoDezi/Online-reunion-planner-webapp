package it.polimi.tiw_ria.controllers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.polimi.tiw_ria.beans.Meeting;
import it.polimi.tiw_ria.beans.User;
import it.polimi.tiw_ria.dao.MeetingDAO;
import it.polimi.tiw_ria.dao.ParticipantDAO;
import it.polimi.tiw_ria.dao.UserDAO;
import it.polimi.tiw_ria.utils.ConnectionHandler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "CreateMeeting", value = "/CreateMeeting")
@MultipartConfig
public class CreateMeeting extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection;

    @Override
    public void init() throws ServletException {
        ServletContext servletContext = getServletContext();
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

        MeetingDAO meetingDAO = new MeetingDAO(connection);
        ParticipantDAO participantDAO = new ParticipantDAO(connection);
        UserDAO userDAO = new UserDAO(connection);

        Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();

        String selectedUsersJson = request.getParameter("userSelected");
        Type listOfMyClassObject = new TypeToken<ArrayList<String>>() {}.getType();
        ArrayList<String> participants = gson.fromJson(selectedUsersJson, listOfMyClassObject);
        ArrayList<User> users = new ArrayList<>();

        try {
            users = userDAO.getUsersFromUsername(participants);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }

        String meetingJson = request.getParameter("meeting");
        Meeting meeting = gson.fromJson(meetingJson, Meeting.class);

        try {
            if (users == null || users.size()==0){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Select at least a user");
            }
            else {
                if (users.size() > meeting.getNumberOfParticipants()) {
                    int attempts = (int) session.getAttribute("attempts");

                    if (attempts == 3) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().write("attempts");   // quando il client riceve "attempts", capisce che sono stati fatti troppi tentativi
                        request.getSession().removeAttribute("attempts");
                        return;
                    }

                    session.setAttribute("attempts", attempts+1);

                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println("The number of selected users is bigger than maximum admissions.\n" +
                            "Please retry.");
                    return;
                }

                int idMeeting = meetingDAO.createMeeting(meeting.getTitle(), meeting.getDate(), meeting.getTime(), meeting.getLength(), meeting.getNumberOfParticipants(), meeting.getIdCreator());
                participantDAO.createParticipant(users,idMeeting);
            }

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create meeting");
            e.printStackTrace();
        }

    }
}
