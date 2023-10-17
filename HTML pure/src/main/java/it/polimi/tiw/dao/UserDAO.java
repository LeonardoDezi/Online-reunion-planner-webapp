package it.polimi.tiw.dao;

import it.polimi.tiw.beans.User;

import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class UserDAO {

    private Connection connection;

    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    public User findUser(String email, String password) throws SQLException {

        String query = "SELECT * FROM tiw.user WHERE email = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setIdUser(resultSet.getInt("idUser"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    return user;
                }
            }
        }
    }

    public User getUserByEmailAndUsername (String email, String username) throws SQLException {

        String query = "SELECT * FROM tiw.user WHERE email = ? AND username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,email);
            preparedStatement.setString(2,username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setIdUser(resultSet.getInt("idUser"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    return user;
                }
            }
        }
    }

    public User getUserByEmail (String email) throws SQLException {

        String query = "SELECT * FROM tiw.user WHERE email = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setIdUser(resultSet.getInt("idUser"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    return user;
                }
            }
        }
    }

    public User getUserByUsername (String username) throws SQLException {

        String query = "SELECT * FROM tiw.user WHERE username = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1,username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.isBeforeFirst())
                    return null;
                else {
                    resultSet.next();
                    User user = new User();
                    user.setIdUser(resultSet.getInt("idUser"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    return user;
                }
            }
        }
    }

    public ArrayList<User> getUsersFromUsername(Collection<String[]> usernames) throws SQLException, NumberFormatException {
        ArrayList<User> result = new ArrayList<>();
        for (String[] us : usernames) {
            for (String username : us) {
                User cur = getUserByUsername(username);
                result.add(cur);
            }
        }

        return result;
    }

    public void registerUser(String password, String name, String surname, String username, String email) throws InvalidParameterException, SQLException {

        String query = "INSERT INTO tiw.user (password,name,surname,username,email) VALUES(?,?,?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setString(1, password);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, surname);
            preparedStatement.setString(4, username);
            preparedStatement.setString(5, email);
            preparedStatement.executeUpdate();

        }
    }

    public ArrayList<User> getUsers() throws SQLException {
        String query = "SELECT * FROM tiw.user";
        ArrayList<User> users = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery(query)) {
                while (resultSet.next()){
                    User user = new User();
                    user.setIdUser(resultSet.getInt("idUser"));
                    user.setName(resultSet.getString("name"));
                    user.setSurname(resultSet.getString("surname"));
                    user.setUsername(resultSet.getString("username"));
                    user.setEmail(resultSet.getString("email"));
                    users.add(user);
                }
            }
        }

        return users;
    }

}
