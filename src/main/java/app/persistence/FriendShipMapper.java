package app.persistence;

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

    public Friendship createFriendship(int userId, int friendId) throws DatabaseException {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        Friendship friendship = null;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, friendId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int friendshipId = rs.getInt(1);
                friendship = new Friendship(friendshipId, userId, friendId, Status.PENDING, new Timestamp(System.currentTimeMillis()));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette venskab: " + e.getMessage());
        }

        return friendship;
    }

    public List<Friendship> getFriendshipsByUserId(int userId) throws DatabaseException {
        List<Friendship> friendships = new ArrayList<>();
        String sql = "SELECT * FROM friendships WHERE user_id = ? OR friend_id = ? ORDER BY created_at DESC";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

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

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af venskaber for bruger " + userId + ": " + e.getMessage());
        }

        return friendships;
    }

    public boolean updateFriendshipStatus(int friendshipId, Status newStatus) throws DatabaseException {
        String sql = "UPDATE friendships SET status = ? WHERE friendship_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newStatus.getValue());
            ps.setInt(2, friendshipId);

            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere venskabsstatus: " + e.getMessage());
        }

        return result;
    }

    public boolean deleteFriendship(int friendshipId) throws DatabaseException {
        String sql = "DELETE FROM friendships WHERE friendship_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, friendshipId);
            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af venskab: " + e.getMessage());
        }

        return result;
    }
}
