package javaversion;

import java.util.*;

/**
 * Simulates an in-memory database representing user and company data.
 * Designed for testing and prototyping purposes only.
 */
public class Database {
    private final List<Map<String, String>> users = new ArrayList<>();
    private Map<String, String> company;

    public Database() {
        // Initialize sample users
        users.add(new HashMap<>(Map.of(
                "userId", "1",
                "email", "alice@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "true"
        )));
        users.add(new HashMap<>(Map.of(
                "userId", "2",
                "email", "bob@loglass.co.jp",
                "userType", "EMPLOYEE",
                "isEmailConfirmed", "false"
        )));
        users.add(new HashMap<>(Map.of(
                "userId", "3",
                "email", "michael@example.com",
                "userType", "CUSTOMER",
                "isEmailConfirmed", "true"
        )));

        // Initialize company
        company = new HashMap<>(Map.of(
                "numberOfEmployees", "2",
                "companyDomainName", "loglass.co.jp"
        ));
    }


    /**
     * updated method to handle null user id
     *
     */
    public Optional<Map<String, String>> getUserById(String userId) {
        if (userId == null) {
            // may be -  throw new IllegalArgumentException("User ID cannot be null");
            return Optional.empty();
        }

        return users.stream()
                .filter(user -> userId.equals(user.get("userId")))
                .findFirst();
    }



    /**
     * method updated to cover
      * Update the saveUser method to handle the case when userId is missing.
     * add the user if its not avaialble
     */
    public void saveUser(Map<String, String> newUser) {
        String newUserId = newUser.get("userId");
        if (newUserId == null) {
            // Silently ignore or log a warning
            return;
        }

        boolean updated = false;

        for (int i = 0; i < users.size(); i++) {
            if (newUserId.equals(users.get(i).get("userId"))) {
                users.set(i, newUser);
                updated = true;
                break;
            }
        }

        if (!updated) {
            users.add(newUser); // Add as new user
        }
    }


    /**
     * Returns current company data.
     */
    public Map<String, String> getCompany() {
        return new HashMap<>(company);
    }

    /**
     * Updates the company data.
     */
    public void saveCompany(Map<String, String> newCompany) {
        this.company = new HashMap<>(newCompany);
    }

    /**
     * Returns a list of all users.
     * is to return a deep copy of the internal list of user maps
     * — not just a shallow copy of references.
     * to avoid original copy of list manipulation
     */
    public List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> copy = new ArrayList<>();
        for (Map<String, String> user : users) {
            copy.add(new HashMap<>(user));
        }
        return copy;
    }
}
