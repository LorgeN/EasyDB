package net.lorgen.easydb;

public class WrappedIndex<T extends StoredItem> {

    private PersistentField<T>[] fields;

    @SafeVarargs
    public WrappedIndex(PersistentField<T>... fields) {
        this.fields = fields;
    }

    public PersistentField<T>[] getFields() {
        return fields;
    }

    public boolean isField(PersistentField<T> field) {
        for (PersistentField<T> someField : this.getFields()) {
            if (!someField.equals(field)) {
                continue;
            }

            return true;
        }

        return false;
    }

    @SafeVarargs
    public final boolean areFields(PersistentField<T>... fields) {
        if (fields.length != this.fields.length) {
            return false;
        }

        return this.containsFields(fields);
    }

    @SafeVarargs
    public final boolean containsFields(PersistentField<T>... fields) {
        for (PersistentField<T> field : fields) {
            if (this.isField(field)) {
                continue;
            }

            return false;
        }

        return true;
    }
}
