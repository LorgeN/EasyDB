package org.tanberg.easydb.test.mock.item;

public class NoKeyItem {

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
}
