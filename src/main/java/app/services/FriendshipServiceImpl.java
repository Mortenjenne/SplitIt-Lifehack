package app.services;

import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import app.persistence.FriendshipMapper;

import java.util.List;

public class FriendshipServiceImpl implements FriendshipService
{
    private FriendshipMapper friendshipMapper;

    public FriendshipServiceImpl(FriendshipMapper friendshipMapper)
    {
        this.friendshipMapper = friendshipMapper;
    }
    @Override
    public List<UserDTO> getFriends(int userId) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<UserDTO> getPendingFriendRequest(int userId) throws DatabaseException {
        return List.of();
    }

    @Override
    public List<UserDTO> searchUsers(String query, int userId) throws DatabaseException {
        return List.of();
    }

    @Override
    public boolean sendFriendRequest(int userId, int friendId) throws DatabaseException {
        return false;
    }

    @Override
    public boolean acceptFriendRequest(int friendship) throws DatabaseException {
        return false;
    }

    @Override
    public boolean rejectFriendRequest(int friendship) throws DatabaseException {
        return false;
    }
}
