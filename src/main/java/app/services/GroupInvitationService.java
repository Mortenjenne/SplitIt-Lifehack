package app.services;

import app.entities.GroupInvitation;
import app.exceptions.DatabaseException;

import java.util.List;

public interface GroupInvitationService
{
    public List<GroupInvitation> getPendingGroupInvitations(int userId) throws DatabaseException;
    public boolean sendGroupInvitation(int userId, int friendId);
    public boolean acceptGroupInvitation(int invitationId, int userId) throws DatabaseException;
    public boolean rejectGroupInvitation(int invitationId) throws DatabaseException;

}
