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

    // Changes user type from CUSTOMER to EMPLOYEE based on domain match
    @Test
    void changeEmail_convertCustomer_EmployeeTEST() {
        userController.changeEmail("3", "michael@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("3");
        assertTrue(user.isPresent());
        assertEquals("michael@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));
        assertEquals("3", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("3", "michael@loglass.co.jp");
    }

    // Changes user type from EMPLOYEE to CUSTOMER when domain does not match
    @Test
    void changeEmail_convertEmployee_CustTEST() {
        userController.changeEmail("1", "alice@example.com");
        Optional<Map<String, String>> user = database.getUserById("1");
        assertEquals("alice@example.com", user.get().get("email"));
        assertEquals("CUSTOMER", user.get().get("userType"));
        assertEquals("1", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("1", "alice@example.com");
    }

    // Keeps the user type unchanged if domain remains the same
    @Test
    void changeEmail_RetainUserType_Domain_NotChangeTEST() {
        userController.changeEmail("1", "alice.new@loglass.co.jp");
        Optional<Map<String, String>> user = database.getUserById("1");
        assertEquals("EMPLOYEE", user.get().get("userType"));
        assertEquals("2", database.getCompany().get("numberOfEmployees"));
        verify(mailer).sendEmailChangedMessage("1", "alice.new@loglass.co.jp");
    }

    // Throws exception when user ID does not exist
    @Test
    void changeEmail_throwException_InvalidUserIdTEST() {
        // Throws exception when user ID does not exist
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userController.changeEmail("999", "test@example.com"));
        assertEquals("User not found. User ID: 999", ex.getMessage());
        verifyNoInteractions(mailer);
    }

    // Throws exception for Invalid email address
    @Test
    void changeEmail_invalidEmailFormatTEST() {
        assertThrows(IllegalArgumentException.class,
                () -> userController.changeEmail("1", "invalid-email"));
        verifyNoInteractions(mailer);
    }

    //  if new email is same as current email
    @Test
    void changeEmail_emailUnchangedTEST() {
        userController.changeEmail("1", "alice@loglass.co.jp");
        assertEquals("2", database.getCompany().get("numberOfEmployees"));
        verifyNoInteractions(mailer);
    }

    //  employee count to 0 when  both employees to customers
    @Test
    void changeEmail_employeeCountTEST() {
        userController.changeEmail("1", "alice@example.com");
        userController.changeEmail("2", "bob@example.com");
        assertEquals("0", database.getCompany().get("numberOfEmployees"));
    }

    // Allows special characters  email
    @Test
    void changeEmail_specialCharacterEmailsTEST() {
        userController.changeEmail("1", "alice+test@loglass.co.jp");
        assertEquals("alice+test@loglass.co.jp", database.getUserById("1").get().get("email"));
    }

    // Changes user type to CUSTOMER for non-company domains
    @Test
    void changeEmail_convertToCustomer() {
        userController.changeEmail("1", "alice@gmail.com");
        assertEquals("CUSTOMER", database.getUserById("1").get().get("userType"));
    }

    //   email is null
    @Test
    void changeEmail_exceptionForNullEmailTEST() {
        try {
            userController.changeEmail("1", null);
            fail("Expected IllegalArgumentException ");
        }catch (IllegalArgumentException ex){
            // test pass as full string is matched
            assertEquals("Invalid email format: null",ex.getMessage());
        }


    }

    // Throws exception when id , email is null
    @Test
    void userid_exceptionForNullEmail_UserIdTEST() {
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail(null, null));
    }

    // Throws exception when email is an empty
    @Test
    void changeEmail_emptyEmailTEST() {
        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail("1", ""));
    }

    //   email with no  @ symbol
    @Test
    void changeEmail_missingAtSymbolTEST() {

        assertThrows(IllegalArgumentException.class, () ->
                userController.changeEmail("1", "alice.loglass.co.jp"));
    }

    //  NullPointerException if "email" field is missing in user data
    @Test
    void changeEmail_userMissingEmailFieldTEST() {
        Map<String, String> mapUser = new HashMap<>();
        mapUser.put("userId", "99");
        mapUser.put("userType", "EMPLOYEE");
        mapUser.put("isEmailConfirmed", "true");
        database.saveUser(mapUser);

        assertThrows(NullPointerException.class, () ->
                userController.changeEmail("99", "test@loglass.co.jp"));
    }

    /*
     // Add a user without isEmailConfirmed to the list directly
    @Test
    void changeEmail_emailConfirmedMissingTest() {

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
