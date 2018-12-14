package net.lorgen.easydb.test;

import net.lorgen.easydb.DeserializerConstructor;
import net.lorgen.easydb.Index;
import net.lorgen.easydb.Persist;
import net.lorgen.easydb.StorageKey;
import net.lorgen.easydb.StoredItem;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.beans.ConstructorProperties;

public class TestItem implements StoredItem {

    @StorageKey(autoIncrement = true)
    private int id;

    @Persist(name = "name", size = 24)
    @Index(unique = true)
    private String username;

    @Persist(typeParams = boolean.class) // Testing persistent field type params
    @Index(1)
    private String firstName;

    @Index(1)
    private String lastName;

    @Persist(size = 32)
    private String email;

    private transient int age;

    @DeserializerConstructor({"id", "username"})
    public TestItem(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public TestItem(int id, String username, String firstName, String lastName, String email, int age) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.age = age;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof TestItem)) return false;

        TestItem testItem = (TestItem) o;

        return new EqualsBuilder()
          .append(id, testItem.id)
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
