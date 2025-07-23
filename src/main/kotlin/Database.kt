class Database {

    private val users: MutableList<Map<String, String>> = mutableListOf(
        mapOf(
            "userId" to "1",
            "email" to "alice@loglass.co.jp",
            "userType" to "EMPLOYEE",
            "isEmailConfirmed" to "true",
        ),
        mapOf(
            "userId" to "2",
            "email" to "bob@loglass.co.jp",
            "userType" to "EMPLOYEE",
            "isEmailConfirmed" to "false",
        ),
        mapOf(
            "userId" to "3",
            "email" to "michael@example.com",
            "userType" to "CUSTOMER",
            "isEmailConfirmed" to "true",
        ),
    )

    private var company: Map<String, String> = mapOf(
        "numberOfEmployees" to "2",
        "companyDomainName" to "loglass.co.jp",
    )

    fun getUserById(userId: String): Map<String, String>? = users.find { it["userId"] == userId }

    fun saveUser(newUser: Map<String, String>) {
        val index = users.indexOfFirst { it["userId"] == newUser["userId"] }
        users[index] = newUser
    }

    fun getCompany(): Map<String, String> = company

    fun saveCompany(newCompany: Map<String, String>) {
        this.company = newCompany
    }
}
