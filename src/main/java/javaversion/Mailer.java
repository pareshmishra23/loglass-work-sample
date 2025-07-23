package javaversion;

public class Mailer {
    public void sendEmailChangedMessage(String userId, String email) {
        System.out.printf("Sending email changed message to %s at %s%n", userId, email);
    }
}