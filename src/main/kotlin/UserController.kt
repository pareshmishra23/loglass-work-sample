import java.lang.RuntimeException

class UserController(
    private val database: Database,
    private val mailer: Mailer,
) {
    companion object {
        private val EMAIL_REGEX = 
            "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
    }

    fun changeEmail(userId: String, newEmail: String) {
        // Validate email format
        if (!EMAIL_REGEX.matches(newEmail)) {
            throw IllegalArgumentException("Invalid email format: $newEmail")
        }

        val user = database.getUserById(userId) 
            ?: throw RuntimeException("User not found. User ID: $userId")

        val currentEmail = user["email"]!!
        if (currentEmail == newEmail) {
            return // No change needed
        }

        val company = database.getCompany()
        val companyDomainName = company["companyDomainName"]!!
        val numberOfEmployees = company["numberOfEmployees"]!!.toInt()

        val currentUserType = user["userType"]!!
        val newEmailDomain = newEmail.split("@")[1]

        // Determine new user type based on email domain
        val newUserType = if (newEmailDomain == companyDomainName) "EMPLOYEE" else "CUSTOMER"

        // Calculate employee count delta
        val delta = when {
            currentUserType == newUserType -> 0
            newUserType == "EMPLOYEE" -> 1  // Customer to Employee
            else -> -1  // Employee to Customer
        }

        val newNumberOfEmployees = numberOfEmployees + delta

        // Update company data
        val newCompany = company.toMutableMap()
        newCompany["numberOfEmployees"] = newNumberOfEmployees.toString()
        newCompany["companyDomainName"] = companyDomainName
        database.saveCompany(newCompany)

        // Update user data
        val newUser = user.toMutableMap()
        newUser["email"] = newEmail
        newUser["userType"] = newUserType
        database.saveUser(newUser)

        // Send notification
        mailer.sendEmailChangedMessage(userId, newEmail)
    }
}