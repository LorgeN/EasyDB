package net.lorgen.easydb.test.item;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class NoKeyItem {

    public static NoKeyItem getRandom() {
        return new NoKeyItem(randomString(), randomString(), randomString(), randomString(), ThreadLocalRandom.current().nextInt(100));
    }

    public static NoKeyItem getRandom(String name) {
        return new NoKeyItem(name, randomString(), randomString(), randomString(), ThreadLocalRandom.current().nextInt(100));
    }

    private static String randomString() {
        return RandomStringUtils.randomAlphabetic(ThreadLocalRandom.current().nextInt(6) + 5);
    }

    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private int age;

    public NoKeyItem() {
    }

    public NoKeyItem(String name, String firstName, String lastName, String email, int age) {
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof NoKeyItem)) {
            return false;
        }

        NoKeyItem noKeyItem = (NoKeyItem) o;
        return age == noKeyItem.age &&
          Objects.equals(name, noKeyItem.name) &&
          Objects.equals(firstName, noKeyItem.firstName) &&
          Objects.equals(lastName, noKeyItem.lastName) &&
          Objects.equals(email, noKeyItem.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, firstName, lastName, email, age);
    }

    @Override
    public String toString() {
        return "NoKeyItem{" +
          "username='" + name + '\'' +
          ", firstName='" + firstName + '\'' +
          ", lastName='" + lastName + '\'' +
          ", email='" + email + '\'' +
          ", age=" + age +
          '}';
    }
}
