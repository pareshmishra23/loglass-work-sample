@file:Suppress("NonAsciiCharacters")

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.io.ByteArrayOutputStream
import java.io.PrintStream

@ExtendWith(MockitoExtension::class)
class ComprehensiveJUnitTest {

    private lateinit var database: Database
    private lateinit var userController: UserController

    @Mock
    private lateinit var mockMailer: Mailer

    @BeforeEach
    fun setUp() {
        database = Database()
        userController = UserController(database, mockMailer)
    }

    // Database Tests
    @Test
    fun `getUserById - 既存ユーザーが取得できること`() {
        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice@loglass.co.jp", user!!["email"])
        assertEquals("EMPLOYEE", user["userType"])
    }

    @Test
    fun `getUserById - 存在しないユーザーの場合nullが返されること`() {
        val user = database.getUserById("999")
        assertNull(user)
    }

    @Test
    fun `saveUser - ユーザー情報が更新されること`() {
        val updatedUser = mapOf(
            "userId" to "1",
            "email" to "alice.updated@loglass.co.jp",
            "userType" to "EMPLOYEE",
            "isEmailConfirmed" to "true"
        )

        database.saveUser(updatedUser)

        val savedUser = database.getUserById("1")
        assertNotNull(savedUser)
        assertEquals("alice.updated@loglass.co.jp", savedUser!!["email"])
    }

    @Test
    fun `getCompany - 会社情報が取得できること`() {
        val company = database.getCompany()
        assertEquals("2", company["numberOfEmployees"])
        assertEquals("loglass.co.jp", company["companyDomainName"])
    }

    @Test
    fun `saveCompany - 会社情報が更新されること`() {
        val newCompany = mapOf(
            "numberOfEmployees" to "3",
            "companyDomainName" to "loglass.co.jp"
        )

        database.saveCompany(newCompany)

        val savedCompany = database.getCompany()
        assertEquals("3", savedCompany["numberOfEmployees"])
    }

    // UserController Tests
    @Test
    fun `changeEmail - 顧客から従業員への変更が正しく処理されること`() {
        // michael@example.com (CUSTOMER) -> michael@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("3", "michael@loglass.co.jp")

        val user = database.getUserById("3")
        assertNotNull(user)
        assertEquals("michael@loglass.co.jp", user!!["email"])
        assertEquals("EMPLOYEE", user["userType"])

        val company = database.getCompany()
        assertEquals("3", company["numberOfEmployees"]) // 2 + 1

        verify(mockMailer).sendEmailChangedMessage("3", "michael@loglass.co.jp")
    }

    @Test
    fun `changeEmail - 従業員から顧客への変更が正しく処理されること`() {
        // alice@loglass.co.jp (EMPLOYEE) -> alice@example.com (CUSTOMER)
        userController.changeEmail("1", "alice@example.com")

        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice@example.com", user!!["email"])
        assertEquals("CUSTOMER", user["userType"])

        val company = database.getCompany()
        assertEquals("1", company["numberOfEmployees"]) // 2 - 1

        verify(mockMailer).sendEmailChangedMessage("1", "alice@example.com")
    }

    @Test
    fun `changeEmail - 同じドメイン内での変更でユーザータイプが維持されること`() {
        // alice@loglass.co.jp (EMPLOYEE) -> alice.new@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("1", "alice.new@loglass.co.jp")

        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice.new@loglass.co.jp", user!!["email"])
        assertEquals("EMPLOYEE", user["userType"])

        val company = database.getCompany()
        assertEquals("2", company["numberOfEmployees"]) // No change

        verify(mockMailer).sendEmailChangedMessage("1", "alice.new@loglass.co.jp")
    }

    @Test
    fun `changeEmail - 同じメールアドレスの場合何も変更されないこと`() {
        userController.changeEmail("1", "alice@loglass.co.jp")

        // Verify no changes were made
        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice@loglass.co.jp", user!!["email"])
        assertEquals("EMPLOYEE", user["userType"])

        val company = database.getCompany()
        assertEquals("2", company["numberOfEmployees"])

        // Verify mailer was not called
        verifyNoInteractions(mockMailer)
    }

    @Test
    fun `changeEmail - 存在しないユーザーの場合例外がスローされること`() {
        val exception = assertThrows(RuntimeException::class.java) {
            userController.changeEmail("999", "test@example.com")
        }

        assertEquals("User not found. User ID: 999", exception.message)
        verifyNoInteractions(mockMailer)
    }

    @Test
    fun `changeEmail - 無効なメールフォーマットの場合例外がスローされること`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            userController.changeEmail("1", "invalid-email")
        }

        assertTrue(exception.message!!.contains("Invalid email format"))
        verifyNoInteractions(mockMailer)
    }

    @Test
    fun `changeEmail - 従業員数が0になるケースが正しく処理されること`() {
        // First change alice from EMPLOYEE to CUSTOMER
        userController.changeEmail("1", "alice@example.com")
        // Then change bob from EMPLOYEE to CUSTOMER
        userController.changeEmail("2", "bob@example.com")

        val company = database.getCompany()
        assertEquals("0", company["numberOfEmployees"])
    }

    // Mailer Tests
    @Test
    fun `sendEmailChangedMessage - 正しいメッセージが出力されること`() {
        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))

        try {
            val mailer = Mailer()
            mailer.sendEmailChangedMessage("123", "test@example.com")

            val output = outputStream.toString()
            assertTrue(output.contains("Sending email changed message to 123 at test@example.com"))
        } finally {
            System.setOut(originalOut)
        }
    }

    // Edge Cases
    @Test
    fun `changeEmail - 特殊文字を含むメールアドレスが正しく処理されること`() {
        userController.changeEmail("1", "alice+test@loglass.co.jp")

        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice+test@loglass.co.jp", user!!["email"])
        assertEquals("EMPLOYEE", user["userType"])
    }

    @Test
    fun `changeEmail - 外部ドメインへの変更が正しく処理されること`() {
        userController.changeEmail("1", "alice@gmail.com")

        val user = database.getUserById("1")
        assertNotNull(user)
        assertEquals("alice@gmail.com", user!!["email"])
        assertEquals("CUSTOMER", user["userType"])

        val company = database.getCompany()
        assertEquals("1", company["numberOfEmployees"]) // 2 - 1
    }
}