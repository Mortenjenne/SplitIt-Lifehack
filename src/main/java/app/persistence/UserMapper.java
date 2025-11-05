package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public User login(String email, String password) throws DatabaseException
    {
        String sql = "select * from public.\"users\" where username=? and password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt("user_id");
                String userName = rs.getString("username");
                String role = rs.getString("role");
                return buildUser(id, email, userName, password, role);
            } else
            {
                throw new DatabaseException("Fejl i login. Prøv igen");
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public boolean createuser(String email, String userName, String hashedPassword) throws DatabaseException
    {
        String sql = "insert into users (email, username, password) values (?,?)";
        boolean result = false;

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {
            ps.setString(1, email);
            ps.setString(2, userName);
            ps.setString(3, hashedPassword);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1)
            {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }
            else
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value "))
            {
                msg = "Brugernavnet findes allerede. Vælg et andet";
            }
            throw new DatabaseException(msg);
        }
        return result;
    }

    public List<User> getAllUsers() throws DatabaseException
    {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * from users";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        )
        {

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                int userId = rs.getInt("user_id");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");

                users.add(buildUser(userId, email, username, password, role));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
        return users;
    }

    public User getUserById(int userId) throws DatabaseException
    {
        ConnectionPool connectionPool = ConnectionPool.getInstance();

        User user = null;
        String sql = "SELECT user_id, username, password, role FROM users WHERE user_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("user_id");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String role = rs.getString("role");

                user = buildUser(id, email, username, password, role);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af bruger med id " + userId + ": " + e.getMessage());
        }
        return user;
    }

    private User buildUser(int userId, String email, String userName, String password, String role){
        return new User(
                userId,
                email,
                userName,
                password,
                role
        );
    }
}