@file:Suppress("NonAsciiCharacters")

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ComprehensiveKotestTest : StringSpec({

    "getUserById - 既存ユーザーが取得できること" {
        val database = Database()
        val user = database.getUserById("1")
        
        user.shouldNotBeNull()
        user["email"] shouldBe "alice@loglass.co.jp"
        user["userType"] shouldBe "EMPLOYEE"
    }

    "getUserById - 存在しないユーザーの場合nullが返されること" {
        val database = Database()
        val user = database.getUserById("999")
        
        user.shouldBeNull()
    }

    "saveUser - ユーザー情報が更新されること" {
        val database = Database()
        val updatedUser = mapOf(
            "userId" to "1",
            "email" to "alice.updated@loglass.co.jp",
            "userType" to "EMPLOYEE",
            "isEmailConfirmed" to "true"
        )

        database.saveUser(updatedUser)

        val savedUser = database.getUserById("1")
        savedUser.shouldNotBeNull()
        savedUser["email"] shouldBe "alice.updated@loglass.co.jp"
    }

    "getCompany - 会社情報が取得できること" {
        val database = Database()
        val company = database.getCompany()
        
        company["numberOfEmployees"] shouldBe "2"
        company["companyDomainName"] shouldBe "loglass.co.jp"
    }

    "saveCompany - 会社情報が更新されること" {
        val database = Database()
        val newCompany = mapOf(
            "numberOfEmployees" to "3",
            "companyDomainName" to "loglass.co.jp"
        )

        database.saveCompany(newCompany)

        val savedCompany = database.getCompany()
        savedCompany["numberOfEmployees"] shouldBe "3"
    }

    "changeEmail - 顧客から従業員への変更が正しく処理されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        // michael@example.com (CUSTOMER) -> michael@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("3", "michael@loglass.co.jp")

        val user = database.getUserById("3")
        user.shouldNotBeNull()
        user["email"] shouldBe "michael@loglass.co.jp"
        user["userType"] shouldBe "EMPLOYEE"

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "3" // 2 + 1

        verify { mockMailer.sendEmailChangedMessage("3", "michael@loglass.co.jp") }
    }

    "changeEmail - 従業員から顧客への変更が正しく処理されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        // alice@loglass.co.jp (EMPLOYEE) -> alice@example.com (CUSTOMER)
        userController.changeEmail("1", "alice@example.com")

        val user = database.getUserById("1")
        user.shouldNotBeNull()
        user["email"] shouldBe "alice@example.com"
        user["userType"] shouldBe "CUSTOMER"

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "1" // 2 - 1

        verify { mockMailer.sendEmailChangedMessage("1", "alice@example.com") }
    }

    "changeEmail - 同じドメイン内での変更でユーザータイプが維持されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        // alice@loglass.co.jp (EMPLOYEE) -> alice.new@loglass.co.jp (EMPLOYEE)
        userController.changeEmail("1", "alice.new@loglass.co.jp")

        val user = database.getUserById("1")
        user.shouldNotBeNull()
        user["email"] shouldBe "alice.new@loglass.co.jp"
        user["userType"] shouldBe "EMPLOYEE"

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "2" // No change

        verify { mockMailer.sendEmailChangedMessage("1", "alice.new@loglass.co.jp") }
    }

    "changeEmail - 同じメールアドレスの場合何も変更されないこと" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        userController.changeEmail("1", "alice@loglass.co.jp")

        // Verify no changes were made
        val user = database.getUserById("1")
        user.shouldNotBeNull()
        user["email"] shouldBe "alice@loglass.co.jp"
        user["userType"] shouldBe "EMPLOYEE"

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "2"

        // Verify mailer was not called
        verify(exactly = 0) { mockMailer.sendEmailChangedMessage(any(), any()) }
    }

    "changeEmail - 存在しないユーザーの場合例外がスローされること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        val exception = shouldThrow<RuntimeException> {
            userController.changeEmail("999", "test@example.com")
        }

        exception.message shouldBe "User not found. User ID: 999"
        verify(exactly = 0) { mockMailer.sendEmailChangedMessage(any(), any()) }
    }

    "changeEmail - 無効なメールフォーマットの場合例外がスローされること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        val exception = shouldThrow<IllegalArgumentException> {
            userController.changeEmail("1", "invalid-email")
        }

        exception.message shouldContain "Invalid email format"
        verify(exactly = 0) { mockMailer.sendEmailChangedMessage(any(), any()) }
    }

    "changeEmail - 従業員数が0になるケースが正しく処理されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        // First change alice from EMPLOYEE to CUSTOMER
        userController.changeEmail("1", "alice@example.com")
        // Then change bob from EMPLOYEE to CUSTOMER
        userController.changeEmail("2", "bob@example.com")

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "0"
    }

    "sendEmailChangedMessage - 正しいメッセージが出力されること" {
        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))

        try {
            val mailer = Mailer()
            mailer.sendEmailChangedMessage("123", "test@example.com")

            val output = outputStream.toString()
            output shouldContain "Sending email changed message to 123 at test@example.com"
        } finally {
            System.setOut(originalOut)
        }
    }

    "changeEmail - 特殊文字を含むメールアドレスが正しく処理されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        userController.changeEmail("1", "alice+test@loglass.co.jp")

        val user = database.getUserById("1")
        user.shouldNotBeNull()
        user["email"] shouldBe "alice+test@loglass.co.jp"
        user["userType"] shouldBe "EMPLOYEE"
    }

    "changeEmail - 外部ドメインへの変更が正しく処理されること" {
        val database = Database()
        val mockMailer = mockk<Mailer>(relaxed = true)
        val userController = UserController(database, mockMailer)

        userController.changeEmail("1", "alice@gmail.com")

        val user = database.getUserById("1")
        user.shouldNotBeNull()
        user["email"] shouldBe "alice@gmail.com"
        user["userType"] shouldBe "CUSTOMER"

        val company = database.getCompany()
        company["numberOfEmployees"] shouldBe "1" // 2 - 1
    }
})