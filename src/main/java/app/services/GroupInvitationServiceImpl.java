package app.services;

import app.entities.GroupInvitation;
import app.exceptions.DatabaseException;
import app.persistence.GroupInvitationMapper;
import java.util.List;

public class GroupInvitationServiceImpl implements GroupInvitationService
{
    private GroupInvitationMapper groupInvitationMapper;

    public GroupInvitationServiceImpl(GroupInvitationMapper groupInvitationMapper)
    {
        this.groupInvitationMapper = groupInvitationMapper;
    }

    @Override
    public List<GroupInvitation> getPendingGroupInvitations(int userId) throws DatabaseException {
        return List.of();
    }

    @Override
    public boolean sendGroupInvitation(int userId, int friendId) {
        return false;
    }

    @Override
    public boolean acceptGroupInvitation(int invitationId, int userId) throws DatabaseException {
        return false;
    }

    @Override
    public boolean rejectGroupInvitation(int invitationId) throws DatabaseException {
        return false;
    }
}
