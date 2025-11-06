package app.services;

import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import java.util.List;

public interface FriendshipService
{
    public List<UserDTO> getFriends(int userId) throws DatabaseException;
    public List<UserDTO> getPendingFriendRequest(int userId) throws DatabaseException;
    public List<UserDTO> searchUsers(String query, int userId) throws DatabaseException;
    public boolean sendFriendRequest(int userId, int friendId) throws DatabaseException;
    public boolean acceptFriendRequest(int friendship) throws DatabaseException;
    public boolean rejectFriendRequest(int friendship) throws DatabaseException;
}
