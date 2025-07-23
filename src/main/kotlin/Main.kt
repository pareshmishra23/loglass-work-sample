fun main() {
    val database = Database()
    val mailer = Mailer()
    val userController = UserController(
        database = database,
        mailer = mailer,
    )
    userController.changeEmail("3", "michael@loglass.co.jp")

    println(database.getUserById("3"))
    println(database.getCompany())
}
