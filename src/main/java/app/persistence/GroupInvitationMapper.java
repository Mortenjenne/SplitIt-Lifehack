package app.persistence;

import app.entities.GroupInvitation;
import app.entities.Status;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupInvitationMapper
{
    private ConnectionPool connectionPool;

    public GroupInvitationMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public GroupInvitation createInvitation(int groupId, int invitedBy, int recipientId) throws DatabaseException
    {
        String sql = "INSERT INTO group_invitations (group_id, invited_by, recipient_id, status) VALUES (?, ?, ?, 'pending')";
        GroupInvitation invitation = null;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {

            ps.setInt(1, groupId);
            ps.setInt(2, invitedBy);
            ps.setInt(3, recipientId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int invitationId = rs.getInt(1);
                invitation = new GroupInvitation(invitationId, groupId, invitedBy, recipientId, Status.PENDING, new Timestamp(System.currentTimeMillis()));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette gruppeinvitation: " + e.getMessage());
        }

        return invitation;
    }

    public List<GroupInvitation> getInvitationsByRecipient(int recipientId) throws DatabaseException
    {
        List<GroupInvitation> invitations = new ArrayList<>();
        String sql = "SELECT * FROM group_invitations WHERE recipient_id = ? ORDER BY created_at DESC";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {

            ps.setInt(1, recipientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                int invitationId = rs.getInt("invitation_id");
                int groupId = rs.getInt("group_id");
                int invitedBy = rs.getInt("invited_by");
                Status status = Status.valueOf(rs.getString("status").toUpperCase());
                Timestamp createdAt = rs.getTimestamp("created_at");

                invitations.add(new GroupInvitation(invitationId, groupId, invitedBy, recipientId, status, createdAt));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved hentning af invitationer for bruger " + recipientId + ": " + e.getMessage());
        }

        return invitations;
    }

    public boolean updateInvitationStatus(int invitationId, Status newStatus) throws DatabaseException
    {
        String sql = "UPDATE group_invitations SET status = ? WHERE invitation_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newStatus.getValue());
            ps.setInt(2, invitationId);

            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere invitationens status: " + e.getMessage());
        }

        return result;
    }

    public boolean deleteInvitation(int invitationId) throws DatabaseException
    {
        String sql = "DELETE FROM group_invitations WHERE invitation_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, invitationId);
            int rows = ps.executeUpdate();
            result = (rows == 1);

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af invitation: " + e.getMessage());
        }

        return result;
    }
}
