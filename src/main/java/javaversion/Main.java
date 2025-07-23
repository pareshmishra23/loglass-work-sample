package javaversion;

public class Main {
    public static void main(String[] args) {
        var database = new Database();
        var mailer = new Mailer();
        var userController = new UserController(database, mailer);

        userController.changeEmail("3", "michael@loglass.co.jp");
        System.out.println(database.getUserById("3"));
        System.out.println(database.getCompany());
    }
}