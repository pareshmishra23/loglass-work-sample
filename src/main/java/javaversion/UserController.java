package javaversion;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Handles logic related to changing a user's email,
 * including user type reassignment, employee count adjustment,
 * and email confirmation status reset.
 */
public class UserController {
    private final Database database;
    private final Mailer mailer;

    // Added: Basic pattern check for email format
    // to test cases as we have for better catch
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public UserController(Database database, Mailer mailer) {
        this.database = Objects.requireNonNull(database);
        this.mailer = Objects.requireNonNull(mailer);
    }

/**
* email changes main method take 2 params
 * @param newEmail
 * @param userId
*
 */
    public void changeEmail(String userId, String newEmail) {
        // Validate email format
        if (userId==null ){
            throw new IllegalArgumentException("Invalid Useer ID  format: " + userId);
        }

        if (newEmail==null || !EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + newEmail);
        }
       // get  user else throw runtime  fail
        var user = database.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. User ID: " + userId));

        var currentEmail = user.get("email");
        // No change needed
        if (currentEmail.equals(newEmail)) {
            return;
        }

        var company = database.getCompany();
        var companyDomainName = company.get("companyDomainName");
        var numberOfEmployees = Integer.parseInt(company.get("numberOfEmployees"));

        var currentUserType = user.get("userType");
        var currentEmailDomain = currentEmail.split("@")[1];
        var newEmailDomain = newEmail.split("@")[1];

        //  new user type based on email domain
        String newUserType;
        if (newEmailDomain.equalsIgnoreCase(companyDomainName)) {
            newUserType = "EMPLOYEE";
        } else {
            newUserType = "CUSTOMER";
        }

        // Calculate employee count delta
        var delta = 0;
        if (!currentUserType.equals(newUserType)) {
            if (newUserType.equals("EMPLOYEE")) {
                delta = 1; // Customer to Employee
            } else {
                delta = -1; // Employee to Customer
            }
        }

        var newNumberOfEmployees = numberOfEmployees + delta;

        // Update company data
        var newCompany = new HashMap<>(company);
        newCompany.put("numberOfEmployees", String.valueOf(newNumberOfEmployees));
        newCompany.put("companyDomainName", companyDomainName);
        database.saveCompany(newCompany);

        // Update user data
        var newUser = new HashMap<>(user);
        newUser.put("email", newEmail);
        newUser.put("userType", newUserType);
        database.saveUser(newUser);

        // Send notification
        mailer.sendEmailChangedMessage(userId, newEmail);
    }
}
