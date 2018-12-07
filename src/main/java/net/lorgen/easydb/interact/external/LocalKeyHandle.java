package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.access.ListenableTypeAccessor;
import net.lorgen.easydb.field.PersistentField;

public abstract class LocalKeyHandle<T> extends FieldHandle<T> {

    public LocalKeyHandle(ListenableTypeAccessor<T> accessor, PersistentField<T> field) {
        super(accessor, field);
    }
}
