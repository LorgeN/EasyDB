package net.lorgen.easydb;

public interface DatabaseRepository<T> extends ItemRepository<T> {

    DatabaseTypeAccessor<T> getDatabaseAccessor();

    DatabaseType getDatabaseType();
}

