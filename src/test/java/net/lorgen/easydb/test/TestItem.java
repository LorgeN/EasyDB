package net.lorgen.easydb.test;

import net.lorgen.easydb.DeserializerConstructor;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.Persist;
import net.lorgen.easydb.StorageKey;
import net.lorgen.easydb.StoredItem;
import net.lorgen.easydb.Unique;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class TestItem implements StoredItem {

    @Persist
    @StorageKey(autoIncrement = true)
    private int id;

    @Persist(name = "name", size = 24)
    @Index
    @Unique
    private String username;

    @Persist(typeParams = boolean.class) // Testing persistent field
    @Index(1)
    private String firstName;

    @Persist
    @Index(1)
    private String lastName;

    @Persist(size = 32)
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

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TestItem)) return false;

        TestItem testItem = (TestItem) o;

        return new EqualsBuilder()
          .append(id, testItem.id)
          .append(age, testItem.age)
          .append(username, testItem.username)
          .append(firstName, testItem.firstName)
          .append(lastName, testItem.lastName)
          .append(email, testItem.email)
          .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
          .append(id)
          .append(username)
          .append(firstName)
          .append(lastName)
          .append(email)
          .append(age)
          .toHashCode();
    }

    @Override
    public String toString() {
        return "TestItem{" +
          "id=" + id +
          ", username='" + username + '\'' +
          ", firstName='" + firstName + '\'' +
          ", lastName='" + lastName + '\'' +
          ", email='" + email + '\'' +
          ", age=" + age +
          '}';
    }
}
