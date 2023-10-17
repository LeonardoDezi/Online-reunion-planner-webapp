package it.polimi.tiw.dao;

import it.polimi.tiw.beans.Meeting;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeetingDAO {
    private Connection connection;

    public MeetingDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Meeting> findMeetingsNotExpiredByUser(int idCreator) throws SQLException {

        List<Meeting> meetings = new ArrayList<>();

        String query = "SELECT * FROM tiw.meeting WHERE idCreator = ? AND date >= CURDATE()";//" and time > CURTIME()";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idCreator);

            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Meeting meeting = new Meeting();
                    meeting.setIdMeeting(result.getInt("idMeeting"));
                    meeting.setTitle(result.getString("title"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setTime(result.getTime("time"));
                    meeting.setLength(result.getInt("length"));
                    meeting.setNumberOfParticipants(result.getInt("numberOfParticipants"));
                    meeting.setIdCreator(result.getInt("idCreator"));
                    meetings.add(meeting);
                }
            }
        }

        return meetings;

    }

    public List<Meeting> findMeetingById(int idUser) throws SQLException {

        List<Meeting> meetings = new ArrayList<>();

        String query = "SELECT M.idMeeting, title, date, time, length, numberOfParticipants, idCreator FROM tiw.meeting as M, tiw.participants as P WHERE P.idParticipant = ? AND M.idMeeting = P.idMetting AND date >= CURDATE()";//" and time > CURTIME()";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, idUser);

            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Meeting meeting = new Meeting();
                    meeting.setIdMeeting(result.getInt("idMeeting"));
                    meeting.setTitle(result.getString("title"));
                    meeting.setDate(result.getDate("date"));
                    meeting.setTime(result.getTime("time"));
                    meeting.setLength(result.getInt("length"));
                    meeting.setNumberOfParticipants(result.getInt("numberOfParticipants"));
                    meeting.setIdCreator(result.getInt("idCreator"));
                    meetings.add(meeting);
                }
            }
        }

        return meetings;

    }

    public int createMeeting(String title, Date date, Time time, int length, int numberOfParticipants, int idCreator) throws SQLException {

        String query = "INSERT into tiw.meeting (title, date, time, length, numberOfParticipants, idCreator) VALUES(?, ?, ?, ?, ?, ?)";


        try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, title);
            preparedStatement.setDate(2, new java.sql.Date(date.getTime()));
            preparedStatement.setTime(3, time);
            preparedStatement.setInt(4, length);
            preparedStatement.setInt(5, numberOfParticipants);
            preparedStatement.setInt(6, idCreator);
            preparedStatement.executeUpdate();

            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Creating meeting failed, no ID obtained.");
            }
        }

    }


}
