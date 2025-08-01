package javaversion;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MailerTest {

    @Test
    void sendEmailChangedMessage_TEST() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            Mailer mailer = new Mailer();
            mailer.sendEmailChangedMessage("123", "test@example.com");

            String output = outputStream.toString();
            assertTrue(output.contains("Sending email changed message to 123 at test@example.com"));
        } finally {
            System.setOut(original);
        }
    }


}
