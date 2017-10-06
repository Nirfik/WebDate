package accounts;

import java.sql.*;

public class UserDAO {
    private final Connection connection;

    public UserDAO(Connection connection) throws SQLException {
        this.connection = connection;
    }

    public UserProfile getUserByName(String name) throws SQLException {
        UserProfile user = null;
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE name = ?")){
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                user = new UserProfile(resultSet.getString("name"), resultSet.getString("password"));
            }
            return user;
        }
    }

    public void createUser(UserProfile user) throws Exception {
        try(PreparedStatement preparedStatement = connection.prepareStatement("insert into users values(?, ?)");){
            if(getUserByName(user.getName()) != null)
                throw new Exception("уже существует");
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.execute();
        }
    }
}
