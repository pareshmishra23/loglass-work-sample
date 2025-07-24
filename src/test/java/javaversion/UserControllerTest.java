package javaversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private Database database;
    private UserController userController;

    @Mock
    private Mailer mailer;

    @BeforeEach
    void setUp() {
        database = new Database();

        // This is added for setup as primary
        // Add user without "isEmailConfirmed" field to test
        Map<String, String> mapUser = new HashMap<>();
        mapUser.put("userId", "98");
        mapUser.put("email", "test@loglass.co.jp");
        mapUser.put("userType", "EMPLOYEE");
        database.getAllUsers().add(mapUser);

        userController = new UserController(database, mailer);
    }

    @Test
    void changeEmail_shouldConvertCustomerToEmployeeCorrectly() {
        // Changes user type from CUSTOMER to EMPLOYEE based on domain match
        userController.changeEmail("3", "michael@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("3");
        assertTrue(user.isPresent());
        assertEquals("michael@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));
        assertEquals("3", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("3", "michael@loglass.co.jp");
    }

    @Test
    void changeEmail_shouldConvertEmployeeToCustomerCorrectly() {
        // Changes user type from EMPLOYEE to CUSTOMER when domain does not match
        userController.changeEmail("1", "alice@example.com");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertEquals("alice@example.com", user.get().get("email"));
        assertEquals("CUSTOMER", user.get().get("userType"));
        assertEquals("1", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("1", "alice@example.com");
    }

    @Test
    void changeEmail_shouldRetainUserTypeIfDomainDoesNotChange() {
        // Keeps the user type unchanged if domain remains the same
        userController.changeEmail("1", "alice.new@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertEquals("EMPLOYEE", user.get().get("userType"));
        assertEquals("2", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("1", "alice.new@loglass.co.jp");
    }

    @Test
    void changeEmail_shouldThrowExceptionForInvalidUserId() {
        // Throws exception when user ID does not exist
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userController.changeEmail("999", "test@example.com"));
        assertEquals("User not found. User ID: 999", ex.getMessage());
        verifyNoInteractions(mailer);
    }

    @Test
    void changeEmail_shouldThrowExceptionForInvalidEmailFormat() {
        // Throws exception for malformed email address
        assertThrows(IllegalArgumentException.class,
                () -> userController.changeEmail("1", "invalid-email"));
        verifyNoInteractions(mailer);
    }

    @Test
    void changeEmail_shouldDoNothingIfEmailUnchanged() {
        // Returns early if new email is same as current email
        userController.changeEmail("1", "alice@loglass.co.jp");
        assertEquals("2", database.getCompany().get("numberOfEmployees"));
        verifyNoInteractions(mailer);
    }

    @Test
    void changeEmail_shouldHandleZeroEmployeeCountCorrectly() {
        // Adjusts employee count to 0 after demoting both employees to customers
        userController.changeEmail("1", "alice@example.com");
        userController.changeEmail("2", "bob@example.com");
        assertEquals("0", database.getCompany().get("numberOfEmployees"));
    }

    @Test
    void changeEmail_shouldAcceptSpecialCharacterEmails() {
        // Allows special characters in local part of email
        userController.changeEmail("1", "alice+test@loglass.co.jp");
        assertEquals("alice+test@loglass.co.jp", database.getUserById("1").get().get("email"));
    }

    @Test
    void changeEmail_shouldConvertToCustomerForExternalDomains() {
        // Changes user type to CUSTOMER for non-company domains
        userController.changeEmail("1", "alice@gmail.com");
        assertEquals("CUSTOMER", database.getUserById("1").get().get("userType"));
    }

    @Test
    void changeEmail_shouldThrowExceptionForNullEmail() {
        // Throws exception when email is null
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail("1", null));
    }

    @Test
    void userid_shouldThrowExceptionForNullUserId() {
        // Throws exception when email is null
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail(null, null));
    }

    @Test
    void changeEmail_shouldThrowExceptionForEmptyEmail() {
        // Throws exception when email is an empty string
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail("1", ""));
    }

    @Test
    void changeEmail_shouldThrowExceptionForMissingAtSymbol() {
        // Throws exception when email lacks @ symbol
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail("1", "alice.loglass.co.jp"));
    }

    @Test
    void changeEmail_shouldThrowExceptionIfUserMissingEmailField() {
        // Throws NullPointerException if "email" field is missing in user data
        Map<String, String> brokenUser = new HashMap<>();
        brokenUser.put("userId", "99");
        brokenUser.put("userType", "EMPLOYEE");
        brokenUser.put("isEmailConfirmed", "true");
        database.saveUser(brokenUser);

        assertThrows(NullPointerException.class, () ->
                userController.changeEmail("99", "test@loglass.co.jp"));
    }

    /*@Test
    void changeEmail_shouldDefaultUnconfirmedIfIsEmailConfirmedMissing() {
        // Add a user without isEmailConfirmed to the list directly
        Map<String, String> userWithoutConfirmation = new HashMap<>();
        userWithoutConfirmation.put("userId", "98");
        userWithoutConfirmation.put("email", "no.confirm@loglass.co.jp");
        userWithoutConfirmation.put("userType", "EMPLOYEE");
        // adding user
        database.getAllUsers().add(userWithoutConfirmation);

        userController.changeEmail("98", "new@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("98");
        // Since isEmailConfirmed is missing, change should NOT happen
        assertEquals("no.confirm@loglass.co.jp", user.get().get("email"));
    }*/

}
