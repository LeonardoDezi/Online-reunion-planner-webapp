package it.polimi.tiw_ria.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.polimi.tiw_ria.beans.Meeting;
import it.polimi.tiw_ria.beans.User;
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
import java.sql.Connection;
import java.sql.SQLException;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet(name = "CheckMeetingParameters", value = "/CheckMeetingParameters")
@MultipartConfig
public class CheckMeetingParameters extends HttpServlet {
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

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");

        boolean isBadRequest = false;
        String title = null;
        Date date = null;
        Time time = null;
        Integer length = null;
        Integer numberOfParticipants = null;

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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
        if (isBadRequest) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Something went wrong");
            return;
        }

        if ( length == 0 || numberOfParticipants == 0) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().println("Length of the meeting or participants number can't be 0");
            return;
        }

        Meeting meeting = new Meeting();
        meeting.setTitle(title);
        meeting.setDate(date);
        meeting.setTime(time);
        meeting.setLength(length);
        meeting.setNumberOfParticipants(numberOfParticipants);
        meeting.setIdCreator(user.getIdUser());

        Gson gson = new GsonBuilder().setDateFormat("yyyy MMM dd").create();
        String json_meeting = gson.toJson(meeting);

        session.setAttribute("attempts", 1);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json_meeting);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }
}
