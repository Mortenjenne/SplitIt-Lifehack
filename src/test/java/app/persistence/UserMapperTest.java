package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private static ConnectionPool testPool;
    private UserMapper userMapper;

    @BeforeAll
    static void setUpDatabase() {
        testPool = ConnectionPool.getInstance(
                "postgres",
                "postgres",
                "jdbc:postgresql://localhost:5432/spiltit?currentSchema=test",
                "spilt-it"
        );

        try (Connection con = testPool.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS users CASCADE");
            stmt.execute("""
                CREATE TABLE users (
                    user_id SERIAL PRIMARY KEY,
                    email VARCHAR(50) NOT NULL UNIQUE,
                    username VARCHAR(50) NOT NULL,
                    password VARCHAR(50) NOT NULL,
                    role VARCHAR(20) DEFAULT 'user' NOT NULL
                )
            """);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            fail("Database connection failed");
        }
    }

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper(testPool);

        try (Connection con = testPool.getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("DELETE FROM users CASCADE");
            stmt.execute("ALTER SEQUENCE users_user_id_seq RESTART WITH 1");

            stmt.execute("""
                INSERT INTO users (email, username, password, role) VALUES
                ('daniel@example.com', 'Daniel', '1234', 'user'),
                ('morten@example.com', 'Morten', 'abcd', 'user'),
                ('jesper@example.com', 'Jesper', 'pass', 'user'),
                ('toby@example.com', 'Toby', '123abc', 'user')
            """);

        } catch (SQLException e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException {
        assertNotNull(testPool.getConnection());
    }

    @Test
    void testGetAllUsers() throws DatabaseException {
        List<User> users = userMapper.getAllUsers();
        assertEquals(4, users.size());
        assertEquals("Morten", users.get(1).getUserName());
    }

    @Test
    void testGetUserById() throws DatabaseException {
        User user = userMapper.getUserById(2);
        assertNotNull(user);
        assertEquals("Morten", user.getUserName());
        assertEquals("morten@example.com", user.getEmail());
    }

    @Test
    void testCreateUser() throws DatabaseException {
        boolean created = userMapper.createuser("laura@example.com", "Laura", "secret");
        assertTrue(created);

        List<User> users = userMapper.getAllUsers();
        assertEquals(5, users.size());
        assertEquals("Laura", users.get(4).getUserName());
    }

    @Test
    void testLoginSuccess() throws DatabaseException {
        User user = userMapper.login("morten@example.com", "abcd");
        assertNotNull(user);
        assertEquals("morten@example.com", user.getEmail());
    }

    @Test
    void testLoginFailure() {
        assertThrows(DatabaseException.class, () -> {
            userMapper.login("Morten", "forkertKode");
        });
    }
}
