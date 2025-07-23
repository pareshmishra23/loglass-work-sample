package javaversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {

    private Database database;

    @BeforeEach
    void init() {
        database = new Database();
    }

    @Test
    void getUserById_shouldReturnExistingUser() {
        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@loglass.co.jp", user.get().get("email"));
    }

    @Test
    void getUserById_shouldReturnEmptyForInvalidUser() {
        assertFalse(database.getUserById("999").isPresent());
    }

    @Test
    void saveUser_shouldUpdateUserInfo() {
        Map<String, String> updatedUser = Map.of(
                "userId", "1",
                "email", "new@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "true"
        );
        database.saveUser(updatedUser);
        assertEquals("new@loglass.co.jp", database.getUserById("1").get().get("email"));
    }

    @Test
    void getCompany_shouldReturnCompanyInfo() {
        Map<String, String> company = database.getCompany();
        assertEquals("loglass.co.jp", company.get("companyDomainName"));
    }

    @Test
    void saveCompany_shouldUpdateCompany() {
        Map<String, String> newCompany = Map.of(
                "numberOfEmployees", "5",
                "companyDomainName", "loglass.co.jp"
        );
        database.saveCompany(newCompany);
        assertEquals("5", database.getCompany().get("numberOfEmployees"));
    }

    @Test
    void getAllUsers_shouldReturnListOfAllUsers() {
        List<Map<String, String>> users = database.getAllUsers();
        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(user -> "1".equals(user.get("userId"))));
    }
    @Test
    void saveUser_shouldUpdateExistingUser() {
        Map<String, String> updatedUser = Map.of(
                "userId", "1",
                "email", "updated@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "true"
        );
        database.saveUser(updatedUser);
        assertEquals("updated@loglass.co.jp", database.getUserById("1").get().get("email"));
    }

    @Test
    void saveUser_shouldNotThrowIfUserDoesNotExist() {
        Map<String, String> newUser = Map.of(
                "userId", "999",
                "email", "new@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "true"
        );
        database.saveUser(newUser);

        // ✅ Expect user to be present
        assertTrue(database.getUserById("999").isPresent());
    }
   //Saving user with no userId should not crash

    @Test
    void saveUser_shouldNotThrowIfUserIdMissing() {
        Map<String, String> invalidUser = new HashMap<>();
        invalidUser.put("email", "noid@loglass.co.jp");

        assertDoesNotThrow(() -> database.saveUser(invalidUser));
    }
   // Get user by ID returns empty if ID is null

    @Test
    void getUserById_shouldReturnEmptyForNullId() {
        Optional<Map<String, String>> user = database.getUserById(null);
        assertFalse(user.isPresent());
    }

}
