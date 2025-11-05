package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.UserMapper;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService
{
    private UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper)
    {
        this.userMapper = userMapper;
    }

    @Override
    public UserDTO authenticate(String email, String password) throws DatabaseException {
        return null;
    }

    @Override
    public boolean registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException {

        if (!createUserRequestDTO.getPassword1().equals(createUserRequestDTO.getPassword2()))
        {
            throw new IllegalArgumentException("Passwords er ikke ens");
        }

        validateEmail(createUserRequestDTO.getEmail());
        validatePassword(createUserRequestDTO.getPassword1());
        validateUserName(createUserRequestDTO.getUserName());

        String hashedPassword = BCrypt.hashpw(createUserRequestDTO.getPassword1(), BCrypt.gensalt());

        return userMapper.createuser(createUserRequestDTO.getEmail(), createUserRequestDTO.getUserName(), hashedPassword);
    }

    @Override
    public UserDTO getUserById(int userId) throws DatabaseException {
        return null;
    }

    @Override
    public List<UserDTO> getAllUsers() throws DatabaseException {
        return userMapper.getAllUsers().stream()
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    private UserDTO buildUserDTO(User user)
    {
        return new UserDTO(
                user.getUserId(),
                user.getUserName(),
                user.getEmail()
        );
    }

    private void validateUserName(String userName)
    {
        if (userName == null || userName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Brugernavn kan ikke være tomme");
        }

        if (userName.length() < 2)
        {
            throw new IllegalArgumentException("Brugernavn skal minimum være 2 tegn");
        }
    }

    private void validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tomt");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Ikke gyldig email format");
        }
    }

    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new IllegalArgumentException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et tal");
        }
    }
}
