package net.lorgen.easydb.interact.join;

import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.field.PersistentField;

import java.util.Objects;

public class JoinWrapper {

    private String table;
    private String localField;
    private String remoteField;
    private Class<? extends ItemRepository> repository;

    public JoinWrapper(JoinWrapper that) {
        this.table = that.table;
        this.localField = that.localField;
        this.remoteField = that.remoteField;
        this.repository = that.repository;
    }

    public JoinWrapper(PersistentField<?> field) {
        this.table = field.getJoinTable();
        this.localField = field.getJoinLocalField();
        this.remoteField = field.getJoinExternalField();
        this.repository = field.getRepository();
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

    public Class<? extends ItemRepository> getRepository() {
        return repository;
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
                Objects.equals(remoteField, that.remoteField) &&
                Objects.equals(repository, that.repository);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, localField, remoteField, repository);
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
