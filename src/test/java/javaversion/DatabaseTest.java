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
    void getUserByIdTEST() {
        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@loglass.co.jp", user.get().get("email"));
    }

    @Test
    void invalidUserTEST() {

        assertFalse(database.getUserById("999").isPresent());
    }

    @Test
    void  UpdateUserInfoTEST() {
        Map<String, String> mapUser = Map.of(
                "userId", "1",
                "email", "new@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "true"
        );
        database.saveUser(mapUser);
        assertEquals("new@loglass.co.jp", database.getUserById("1").get().get("email"));
    }

    @Test
    void getCompanyInfoTEST() {
        Map<String, String> company = database.getCompany();
        assertEquals("loglass.co.jp", company.get("companyDomainName"));
    }

    @Test
    void saveCompanyTEST() {
        Map<String, String> newCompany = Map.of(
                "numberOfEmployees", "5",
                "companyDomainName", "loglass.co.jp"
        );
        database.saveCompany(newCompany);
        assertEquals("5", database.getCompany().get("numberOfEmployees"));
    }

    @Test
    void getAllUsersTEST() {
        List<Map<String, String>> users = database.getAllUsers();
        assertEquals(3, users.size());

    }
    @Test
    void saveUserTEST() {
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
    void saveUserNotExistTEST() {
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
    void saveUser_IdMissingTEST() {
        Map<String, String> user = new HashMap<>();
        user.put("email", "noid@loglass.co.jp");

        assertDoesNotThrow(() -> database.saveUser(user));
    }
   // Get user by ID returns empty if ID is null

    @Test
    void getUserById_NULLTEST() {
        Optional<Map<String, String>> user = database.getUserById(null);
        assertFalse(user.isPresent());
    }

}
