package app.persistence;

import app.dto.UserDTO;
import app.entities.Friendship;
import app.entities.Status;
import app.exceptions.DatabaseException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendshipMapper {
    private ConnectionPool connectionPool;

    public FriendshipMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public List<UserDTO> getFriends(int userId) throws DatabaseException
    {
        List<UserDTO> friends = new ArrayList<>();
        String sql = """
            SELECT u.user_id, u.user_name, u.email 
            FROM users u
            INNER JOIN friendships f ON (u.user_id = f.friend_id OR u.user_id = f.user_id)
            WHERE (f.user_id = ? OR f.friend_id = ?) 
            AND f.status = 'accepted'
            AND u.user_id != ?
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserDTO user = new UserDTO(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("email")
                );
                friends.add(user);
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Error getting friends: " + e.getMessage());
        }
        return friends;
    }

    public List<UserDTO> searchUsers(String query, int currentUserId) throws DatabaseException
    {
        List<UserDTO> users = new ArrayList<>();
        String sql = """
            SELECT u.user_id, u.user_name, u.email 
            FROM users u
            WHERE u.user_name LIKE ? 
            AND u.user_id != ?
            AND u.user_id NOT IN (
                SELECT CASE 
                    WHEN f.user_id = ? THEN f.friend_id
                    ELSE f.user_id
                END
                FROM friendships f
                WHERE (f.user_id = ? OR f.friend_id = ?)
                AND f.status = 'accepted'
            )
            AND u.user_id NOT IN (
                SELECT f.friend_id
                FROM friendships f
                WHERE f.user_id = ? AND f.status = 'pending'
            )
            LIMIT 10
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setString(1, "%" + query + "%");
            ps.setInt(2, currentUserId);
            ps.setInt(3, currentUserId);
            ps.setInt(4, currentUserId);
            ps.setInt(5, currentUserId);
            ps.setInt(6, currentUserId);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                UserDTO user = new UserDTO(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("email")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching users: " + e.getMessage());
        }
        return users;
    }

    public List<UserDTO> getPendingFriendRequests(int userId) throws DatabaseException
    {
        List<UserDTO> requests = new ArrayList<>();
        String sql = """
            SELECT f.friendship_id, u.user_id, u.user_name, u.email 
            FROM friendships f
            INNER JOIN users u ON f.user_id = u.user_id
            WHERE f.friend_id = ? AND f.status = 'pending'
        """;

        try (Connection conn = connectionPool.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                UserDTO user = new UserDTO(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("email")
                );
                requests.add(user);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting friend requests: " + e.getMessage());
        }
        return requests;
    }

    public Friendship createFriendshipRequest(int userId, int friendId) throws DatabaseException
    {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        Friendship friendship = null;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int friendshipId = rs.getInt(1);
                friendship = new Friendship(friendshipId, userId, friendId, Status.PENDING, new Timestamp(System.currentTimeMillis()));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette venskab: " + e.getMessage());
        }

        return friendship;
    }

    public List<Friendship> getFriendshipsByUserId(int userId) throws DatabaseException
    {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships WHERE user_id = ? OR friend_id = ? ORDER BY created_at DESC";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, userId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int friendshipId = rs.getInt("friendship_id");
                int user = rs.getInt("user_id");
                int friend = rs.getInt("friend_id");
                Status status = Status.valueOf(rs.getString("status").toUpperCase());
                Timestamp createdAt = rs.getTimestamp("created_at");

                friendships.add(new Friendship(friendshipId, user, friend, status, createdAt));
            }

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af venskaber for bruger " + userId + ": " + e.getMessage());
        }

        return friendships;
    }

    public boolean updateFriendshipStatus(int friendshipId, Status newStatus) throws DatabaseException {
        String sql = "UPDATE friendships SET status = ? WHERE friendship_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setString(1, newStatus.getValue());
            ps.setInt(2, friendshipId);

            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke opdatere venskabsstatus: " + e.getMessage());
        }

        return result;
    }

    public boolean deleteFriendship(int friendshipId) throws DatabaseException {
        String sql = "DELETE FROM friendships WHERE friendship_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, friendshipId);
            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af venskab: " + e.getMessage());
        }

        return result;
    }
}

