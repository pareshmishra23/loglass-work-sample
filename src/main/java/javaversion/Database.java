package javaversion;

import java.util.*;

public class Database {
    private final List<Map<String, String>> users = new ArrayList<>();
    private Map<String, String> company;

    public Database() {
        users.add(Map.of(
            "userId", "1",
            "email", "alice@loglass.co.jp",
            "userType", "EMPLOYEE",
            "isEmailConfirmed", "true"
        ));
        users.add(Map.of(
            "userId", "2",
            "email", "bob@loglass.co.jp",
            "userType", "EMPLOYEE",
            "isEmailConfirmed", "false"
        ));
        users.add(Map.of(
            "userId", "3",
            "email", "michael@example.com",
            "userType", "CUSTOMER",
            "isEmailConfirmed", "true"
        ));

        company = Map.of(
            "numberOfEmployees", "2",
            "companyDomainName", "loglass.co.jp"
        );
    }

    public Optional<Map<String, String>> getUserById(String userId) {
        return users.stream()
                .filter(user -> userId.equals(user.get("userId")))
                .findFirst();
    }

    public void saveUser(Map<String, String> newUser) {
        var index = -1;
        for (int i = 0; i < users.size(); i++) {
            if (newUser.get("userId").equals(users.get(i).get("userId"))) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            users.set(index, newUser);
        }
    }

    public Map<String, String> getCompany() {
        return company;
    }

    public void saveCompany(Map<String, String> newCompany) {
        this.company = newCompany;
    }
}