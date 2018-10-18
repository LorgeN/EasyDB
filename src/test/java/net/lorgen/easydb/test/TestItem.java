package net.lorgen.easydb.test;

import net.lorgen.easydb.DeserializerConstructor;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.Persist;
import net.lorgen.easydb.StorageKey;
import net.lorgen.easydb.StoredItem;

public class TestItem implements StoredItem {

    @Persist
    @StorageKey(autoIncrement = true)
    private int id;

    @Persist(name = "name", size = 24)
    @Index
    private String username;

    @Persist(typeParams = boolean.class) // Testing persistent field
    @Index(1)
    private String firstName;

    @Persist
    @Index(1)
    private String lastName;

    @Persist
    private String email;

    // To test it actually considering the "Persist" annotation for profiling
    private int age;

    @DeserializerConstructor({"id", "username"})
    public TestItem(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public TestItem(String username, String firstName, String lastName, String email, int age) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
    }
}
