package net.lorgen.easydb.interact;

import net.lorgen.easydb.PersistentField;

import java.util.Objects;

public class JoinWrapper {

    private String table;
    private String localField;
    private String remoteField;

    public JoinWrapper(PersistentField<?> field) {
        this.table = field.getJoinTable();
        this.localField  = field.getJoinLocalField();
        this.remoteField = field.getJoinExternalField();
    }

    public JoinWrapper(String table, String localField, String remoteField) {
        this.table = table;
        this.localField = localField;
        this.remoteField = remoteField;
    }

    public String getTable() {
        return table;
    }

    public String getLocalField() {
        return localField;
    }

    public String getRemoteField() {
        return remoteField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JoinWrapper that = (JoinWrapper) o;
        return Objects.equals(table, that.table) &&
          Objects.equals(localField, that.localField) &&
          Objects.equals(remoteField, that.remoteField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, localField, remoteField);
    }

    @Override
    public String toString() {
        return "JoinWrapper{" +
          "table='" + table + '\'' +
          ", localField='" + localField + '\'' +
          ", remoteField='" + remoteField + '\'' +
          '}';
    }
}
