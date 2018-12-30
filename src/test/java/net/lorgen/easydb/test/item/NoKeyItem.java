package net.lorgen.easydb.test.item;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.ThreadLocalRandom;

public class NoKeyItem {

    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private int age;

    public NoKeyItem() {
    }

    public NoKeyItem(String username, String firstName, String lastName, String email, int age) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
    }

    public static NoKeyItem getRandom() {
        return new NoKeyItem(randomString(), randomString(), randomString(), randomString(), ThreadLocalRandom.current().nextInt(100));
    }

    private static String randomString() {
        return RandomStringUtils.randomAlphabetic(ThreadLocalRandom.current().nextInt(6) + 5);
    }
}
