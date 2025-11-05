package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.exceptions.DatabaseException;
import java.util.List;

public interface UserService
{
    public UserDTO authenticate(String email, String password) throws DatabaseException;

    public boolean registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException;

    public UserDTO getUserById(int userId) throws DatabaseException;

    public List<UserDTO> getAllUsers() throws DatabaseException;

}
