package javaversion;

import java.util.*;
import java.util.regex.Pattern;

public class UserController {
    private final Database database;
    private final Mailer mailer;
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");

    public UserController(Database database, Mailer mailer) {
        this.database = Objects.requireNonNull(database);
        this.mailer = Objects.requireNonNull(mailer);
    }

    public void changeEmail(String userId, String newEmail) {
        // Validate email format
        if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + newEmail);
        }

        var user = database.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. User ID: " + userId));

        var currentEmail = user.get("email");
        if (currentEmail.equals(newEmail)) {
            return; // No change needed
        }

        var company = database.getCompany();
        var companyDomainName = company.get("companyDomainName");
        var numberOfEmployees = Integer.parseInt(company.get("numberOfEmployees"));

        var currentUserType = user.get("userType");
        var currentEmailDomain = currentEmail.split("@")[1];
        var newEmailDomain = newEmail.split("@")[1];

        // Determine new user type based on email domain
        var newUserType = newEmailDomain.equals(companyDomainName) ? "EMPLOYEE" : "CUSTOMER";

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