import java.lang.RuntimeException

class UserController(
    private val database: Database,
    private val mailer: Mailer,
) {

    fun changeEmail(userId: String, newEmail: String) {
        val user = database.getUserById(userId) ?: throw RuntimeException("User not found. User ID: $userId")

        val email = user["email"]!!

        if (email == newEmail) {
            return
        }

        val company = database.getCompany()
        val companyDomainName = company["companyDomainName"]!!
        val numberOfEmployees = company["numberOfEmployees"]!!.toInt()

        val emailDomain = newEmail.split("@")[1]

        val newType = if (emailDomain == companyDomainName) "EMPLOYEE" else "CUSTOMER"

        val delta = if (newType == "EMPLOYEE") 1 else -1
        val newNumber = numberOfEmployees + delta
        val newCompany = company.toMutableMap()
        newCompany["numberOfEmployees"] = newNumber.toString()
        newCompany["companyDomainName"] = companyDomainName
        database.saveCompany(newCompany)

        val newUser = user.toMutableMap()
        newUser["email"] = newEmail
        newUser["userType"] = newType

        database.saveUser(newUser)
        mailer.sendEmailChangedMessage(userId, newEmail)
    }
}
