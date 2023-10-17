package it.polimi.tiw.dao;

import it.polimi.tiw.beans.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class ParticipantDAO {

    private Connection connection;

    public ParticipantDAO(Connection connection) {
        this.connection = connection;
    }

    public void createParticipant(ArrayList<User> participants, int idMeeting) throws SQLException {
        for (User user : participants){
            int idParticipant = user.getIdUser();
            String query = "INSERT into tiw.participants (idParticipant, idMetting) VALUES(?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setInt(1, idParticipant);
                preparedStatement.setInt(2, idMeeting);
                preparedStatement.executeUpdate();
            }
        }
    }
}
