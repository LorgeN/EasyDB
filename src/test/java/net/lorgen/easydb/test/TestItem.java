package net.lorgen.easydb.test;

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
}
