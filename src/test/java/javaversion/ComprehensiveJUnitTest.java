package javaversion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComprehensiveJUnitTest {

    private Database database;
    private UserController userController;
    
    @Mock
    private Mailer mockMailer;

    @BeforeEach
    void setUp() {
        database = new Database();
        userController = new UserController(database, mockMailer);
    }

    // Database Tests
    @Test
    void getUserById_既存ユーザーが取得できること() {
        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));
    }

    @Test
    void getUserById_存在しないユーザーの場合空のOptionalが返されること() {
        Optional<Map<String, String>> user = database.getUserById("999");
        assertFalse(user.isPresent());
    }

    @Test
    void saveUser_ユーザー情報が更新されること() {
        Map<String, String> updatedUser = Map.of(
            "userId", "1",
            "email", "alice.updated@loglass.co.jp",
            "userType", "EMPLOYEE",
            "isEmailConfirmed", "true"
        );
        
        database.saveUser(updatedUser);
        
        Optional<Map<String, String>> savedUser = database.getUserById("1");
        assertTrue(savedUser.isPresent());
        assertEquals("alice.updated@loglass.co.jp", savedUser.get().get("email"));
    }

    @Test
    void getCompany_会社情報が取得できること() {
        Map<String, String> company = database.getCompany();
        assertEquals("2", company.get("numberOfEmployees"));
        assertEquals("loglass.co.jp", company.get("companyDomainName"));
    }

    @Test
    void saveCompany_会社情報が更新されること() {
        Map<String, String> newCompany = Map.of(
            "numberOfEmployees", "3",
            "companyDomainName", "loglass.co.jp"
        );
        
        database.saveCompany(newCompany);
        
        Map<String, String> savedCompany = database.getCompany();
        assertEquals("3", savedCompany.get("numberOfEmployees"));
    }

    // UserController Tests
    @Test
    void changeEmail_顧客から従業員への変更が正しく処理されること() {
        // michael@example.com (CUSTOMER) -> michael@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("3", "michael@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("3");
        assertTrue(user.isPresent());
        assertEquals("michael@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));

        Map<String, String> company = database.getCompany();
        assertEquals("3", company.get("numberOfEmployees")); // 2 + 1

        verify(mockMailer).sendEmailChangedMessage("3", "michael@loglass.co.jp");
    }

    @Test
    void changeEmail_従業員から顧客への変更が正しく処理されること() {
        // alice@loglass.co.jp (EMPLOYEE) -> alice@example.com (CUSTOMER)
        userController.changeEmail("1", "alice@example.com");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@example.com", user.get().get("email"));
        assertEquals("CUSTOMER", user.get().get("userType"));

        Map<String, String> company = database.getCompany();
        assertEquals("1", company.get("numberOfEmployees")); // 2 - 1

        verify(mockMailer).sendEmailChangedMessage("1", "alice@example.com");
    }

    @Test
    void changeEmail_同じドメイン内での変更でユーザータイプが維持されること() {
        // alice@loglass.co.jp (EMPLOYEE) -> alice.new@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("1", "alice.new@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice.new@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));

        Map<String, String> company = database.getCompany();
        assertEquals("2", company.get("numberOfEmployees")); // No change

        verify(mockMailer).sendEmailChangedMessage("1", "alice.new@loglass.co.jp");
    }

    @Test
    void changeEmail_同じメールアドレスの場合何も変更されないこと() {
        userController.changeEmail("1", "alice@loglass.co.jp");

        // Verify no changes were made
        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));

        Map<String, String> company = database.getCompany();
        assertEquals("2", company.get("numberOfEmployees"));

        // Verify mailer was not called
        verifyNoInteractions(mockMailer);
    }

    @Test
    void changeEmail_存在しないユーザーの場合例外がスローされること() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userController.changeEmail("999", "test@example.com");
        });
        
        assertEquals("User not found. User ID: 999", exception.getMessage());
        verifyNoInteractions(mockMailer);
    }

    @Test
    void changeEmail_無効なメールフォーマットの場合例外がスローされること() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userController.changeEmail("1", "invalid-email");
        });
        
        assertTrue(exception.getMessage().contains("Invalid email format"));
        verifyNoInteractions(mockMailer);
    }

    @Test
    void changeEmail_従業員数が0になるケースが正しく処理されること() {
        // First change alice from EMPLOYEE to CUSTOMER
        userController.changeEmail("1", "alice@example.com");
        // Then change bob from EMPLOYEE to CUSTOMER
        userController.changeEmail("2", "bob@example.com");

        Map<String, String> company = database.getCompany();
        assertEquals("0", company.get("numberOfEmployees"));
    }

    // Mailer Tests
    @Test
    void sendEmailChangedMessage_正しいメッセージが出力されること() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Mailer mailer = new Mailer();
            mailer.sendEmailChangedMessage("123", "test@example.com");
            
            String output = outputStream.toString();
            assertTrue(output.contains("Sending email changed message to 123 at test@example.com"));
        } finally {
            System.setOut(originalOut);
        }
    }

    // Edge Cases
    @Test
    void changeEmail_特殊文字を含むメールアドレスが正しく処理されること() {
        userController.changeEmail("1", "alice+test@loglass.co.jp");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice+test@loglass.co.jp", user.get().get("email"));
        assertEquals("EMPLOYEE", user.get().get("userType"));
    }

    @Test
    void changeEmail_日本語ドメインでない外部ドメインへの変更が正しく処理されること() {
        userController.changeEmail("1", "alice@gmail.com");

        Optional<Map<String, String>> user = database.getUserById("1");
        assertTrue(user.isPresent());
        assertEquals("alice@gmail.com", user.get().get("email"));
        assertEquals("CUSTOMER", user.get().get("userType"));

        Map<String, String> company = database.getCompany();
        assertEquals("1", company.get("numberOfEmployees")); // 2 - 1
    }
}