package net.lorgen.easydb.access.sql;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lorgen.easydb.DataType;
import net.lorgen.easydb.DatabaseTypeAccessor;
import net.lorgen.easydb.field.FieldValue;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.WrappedIndex;
import net.lorgen.easydb.connection.ConnectionRegistry;
import net.lorgen.easydb.exception.DeleteQueryException;
import net.lorgen.easydb.exception.FindQueryException;
import net.lorgen.easydb.exception.SaveQueryException;
import net.lorgen.easydb.interact.JoinWrapper;
import net.lorgen.easydb.query.Query;
import net.lorgen.easydb.query.req.CombinedRequirement;
import net.lorgen.easydb.query.req.QueryRequirement;
import net.lorgen.easydb.query.req.SimpleRequirement;
import net.lorgen.easydb.response.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * SQL database type accessor
 *
 * @param <T> The type this accessor handles
 */
public class SQLAccessor<T> implements DatabaseTypeAccessor<T> {

    private final ItemRepository<T> repository;
    private final String table;
    private final SQLConfiguration sqlConfig;

    public SQLAccessor(SQLConfiguration sqlConfig, ItemRepository<T> repository, String table) {
        this.sqlConfig = sqlConfig;

        this.repository = repository;
        this.table = table;

        this.createTable();
    }

    /**
     * @return A {@link Connection connection} to the database, from this accessor's
     * {@link DataSource data source}.
     */
    public Connection getConnection() {
        return ConnectionRegistry.getInstance().<Connection>getPool(this.sqlConfig).getConnection();
    }

    /**
     * Runs the "CREATE TABLE" command, which will create the table this accessor
     * uses in the database, unless it already exists. Automatically ran whenever
     * an instance is created (Called in constructor).
     */
    public void createTable() {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + this.table + "(");

        PersistentField<T>[] fields = this.repository.getProfile().getStoredFields();
        PersistentField<T>[] keys = this.repository.getProfile().getKeys();

        for (PersistentField<T> field : fields) {
            builder.append(this.toColumnDeclaration(field));
            builder.append(", "); // We are adding primary keys anyway
        }

        builder.append("PRIMARY KEY(");
        for (int i = 0; i < keys.length; i++) {
            PersistentField<T> field = keys[i];
            builder.append("`").append(field.getName()).append("`");
            if ((i + 1) >= keys.length) {
                continue;
            }

            builder.append(", ");
        }

        builder.append("));");
        try (Connection connection = this.getConnection()) {
            String statement = builder.toString();
            connection.createStatement().execute(statement);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        this.createIndices();
    }

    /**
     * Creates all the indices to go along with this accessor's table. SQL handles
     * the rest for us, all we have to do is tell SQL to create the indices. Ran
     * automatically along with table creation.
     */
    public void createIndices() {
        WrappedIndex<T>[] indices = this.repository.getProfile().getIndices();
        if (indices.length == 0) {
            return;
        }

        Map<Integer, WrappedIndex<T>> indexMap = Maps.newHashMap();
        try (Connection connection = this.getConnection()) {
            Statement statement = connection.createStatement();

            statement.execute("DROP PROCEDURE IF EXISTS CREATE_INDEX");
            statement.execute(
              "CREATE PROCEDURE CREATE_INDEX(" +
                "    given_database VARCHAR(64)," +
                "    given_table    VARCHAR(64)," +
                "    given_index    VARCHAR(64)," +
                "    given_columns  VARCHAR(64)," +
                "    unique_index   TINYINT(1)" +
                ") BEGIN " +
                "    DECLARE IndexExists INTEGER;" +
                "    SELECT COUNT(1) INTO IndexExists" +
                "    FROM INFORMATION_SCHEMA.STATISTICS" +
                "    WHERE table_schema = given_database" +
                "    AND   table_name   = given_table" +
                "    AND   index_name   = given_index;" +
                "    IF IndexExists = 0 THEN" +
                "        IF unique_index = 1 THEN" +
                "            SET @sqlstmt = CONCAT('CREATE UNIQUE INDEX ',given_index,' ON '," +
                "            given_database,'.',given_table,' (',given_columns,')');" +
                "            PREPARE st FROM @sqlstmt;" +
                "            EXECUTE st;" +
                "            DEALLOCATE PREPARE st;" +
                "        ELSE" +
                "            SET @sqlstmt = CONCAT('CREATE INDEX ',given_index,' ON '," +
                "            given_database,'.',given_table,' (',given_columns,')');" +
                "            PREPARE st FROM @sqlstmt;" +
                "            EXECUTE st;" +
                "            DEALLOCATE PREPARE st;" +
                "        END IF;" +
                "    ELSE" +
                "        SELECT CONCAT('Index ',given_index,' already exists on Table '," +
                "        given_database,'.',given_table) CreateindexErrorMessage;   " +
                "    END IF;" +
                "" +
                "END");

            String database = "\"" + this.sqlConfig.getDatabase() + "\"";

            for (WrappedIndex<T> index : indices) {
                if (index.getFields().length != 1) {
                    indexMap.put(index.getId(), index);
                    continue;
                }

                PersistentField<T> field = index.getFields()[0];
                statement.addBatch("CALL CREATE_INDEX(" + database + ", \"" + this.table + "\", \"" + field.getName() +
                  "\", \"" + field.getName() + "\", " + (index.isUnique() ? 1 : 0) + ")");
            }

            for (Entry<Integer, WrappedIndex<T>> entry : indexMap.entrySet()) {
                String name = "\"index_" + entry.getKey() + "\"";
                StringBuilder builder = new StringBuilder();
                PersistentField<T>[] fields = entry.getValue().getFields();
                for (int i = 0; i < fields.length; i++) {
                    builder.append(fields[i].getName());

                    if ((i + 1) >= fields.length) {
                        continue;
                    }

                    builder.append(", ");
                }

                statement.addBatch("CALL CREATE_INDEX(" + database + ", \"" + this.table + "\", " + name + ", \"" +
                  builder.toString() + "\", " + (entry.getValue().isUnique() ? 1 : 0) + ")");
            }

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setUp() {
        this.createTable();
    }

    /**
     * Synchronous utility method for getting the next AUTO_INCREMENT value, if such a value
     * is present for this table.
     *
     * @return The next auto increment column value
     */
    public int getNextAutoIncrement() {
        try (Connection connection = this.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `AUTO_INCREMENT` FROM INFORMATION_SCHEMA.TABLES " +
              "WHERE TABLE_SCHEMA='" + this.sqlConfig.getDatabase() + "' AND TABLE_NAME='" + this.table + "';");

            ResultSet resultSet = statement.executeQuery();
            return (resultSet == null || !resultSet.next()) ? 2 : (resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Synchronous utility method for getting the current AUTO_INCREMENT value, if such a value
     * is present for this table.
     *
     * @return The next current increment column value
     */
    public int getCurrentAutoIncrement() {
        try (Connection connection = this.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT `AUTO_INCREMENT` FROM INFORMATION_SCHEMA.TABLES " +
              "WHERE TABLE_SCHEMA='" + this.sqlConfig.getDatabase() + "' AND TABLE_NAME='" + this.table + "';");

            ResultSet resultSet = statement.executeQuery();
            return (resultSet == null || !resultSet.next()) ? 1 : (resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public ResponseEntity<T> findFirst(Query<T> query) {
        StringBuilder builder = new StringBuilder("SELECT * FROM ")
          .append(this.getTableName());

        if (query.getRequirement() != null) {
            builder.append(" WHERE ").append(this.toString(query.getRequirement()));
        }

        builder.append(";");

        try (Connection connection = this.getConnection()) {
            ResultSet result = connection.createStatement().executeQuery(builder.toString());
            if (!result.next()) {
                return null;
            }

            return this.fromResultSet(result);
        } catch (SQLException e) {
            throw new FindQueryException("Statement: " + builder.toString(), e, query);
        }
    }

    @Override
    public List<ResponseEntity<T>> findAll(Query<T> query) {
        StringBuilder builder = new StringBuilder("SELECT ");

        PersistentField<T>[] fields = this.repository.getProfile().getFields(); // Get all fields

        for (int i = 0; i < fields.length; i++) {
            PersistentField<T> field = fields[i];
            if (i > 0) {
                builder.append(", ");
            }

            if (!field.isJoined()) {
                builder.append("`")
                  .append(this.table)
                  .append("`.`")
                  .append(field.getName())
                  .append("`");
                continue;
            }

            builder.append("`")
              .append(field.getJoinTable())
              .append("`.`")
              .append(field.getName())
              .append("`");
        }

        builder.append(" FROM ").append(this.getTableName());

        if (query.getRequirement() != null) {
            builder.append(" WHERE ").append(this.toString(query.getRequirement()));
        }

        for (JoinWrapper join : this.repository.getProfile().getJoins()) {
            builder.append(" INNER JOIN ")
              .append("`")
              .append(join.getTable())
              .append("` ON `")
              .append(this.table)
              .append("`.`")
              .append(join.getLocalField())
              .append("` = `")
              .append(join.getTable())
              .append("`.`")
              .append(join.getRemoteField())
              .append("`");
        }

        builder.append(";");

        try (Connection connection = this.getConnection()) {
            ResultSet result = connection.createStatement().executeQuery(builder.toString());
            List<ResponseEntity<T>> list = Lists.newArrayList();

            while (result.next()) {
                list.add(this.fromResultSet(result));
            }

            return list;
        } catch (SQLException e) {
            throw new FindQueryException("Statement: " + builder.toString(), e, query);
        }
    }

    @Override
    public void saveOrUpdate(Query<T> query) {
        // This is now simply an "UPDATE" statement
        if (query.getRequirement() != null) {
            StringBuilder builder = new StringBuilder("UPDATE ")
              .append(this.getTableName())
              .append(" SET ");

            FieldValue<T>[] values = query.getValues();
            for (int i = 0; i < values.length; i++) {
                FieldValue<T> value = values[i];
                Object fieldVal = value.getValue();
                DataType type = value.getField().getType();
                String quotedVal = this.quote(type.toString(this.repository, value.getField(), fieldVal), value.getField());

                builder
                  .append("`")
                  .append(value.getField().getName())
                  .append("`=")
                  .append(quotedVal);

                if ((i + 1) >= values.length) {
                    continue;
                }

                builder.append(", ");
            }

            builder.append(" WHERE ").append(this.toString(query.getRequirement())).append(";");

            try (Connection connection = this.getConnection()) {
                connection.createStatement().execute(builder.toString());
            } catch (SQLException e) {
                throw new SaveQueryException("Statement: " + builder.toString(), e, query);
            }

            return;
        }

        // In this case, we either INSERT a new row, or update an existing one

        StringBuilder builder = new StringBuilder("INSERT INTO " + this.getTableName() + "(");
        StringBuilder valuesBuilder = new StringBuilder();
        StringBuilder updateBuilder = new StringBuilder();

        PersistentField<T> autoIncrementField = this.repository.getProfile().getAutoIncrementField();

        PersistentField<T>[] fields = autoIncrementField == null
          ? this.repository.getProfile().getFields()
          : Arrays.stream(this.repository.getProfile().getFields())
          .filter(field -> !field.isAutoIncrement() || ((int) this.repository.getArrayValue(field, query.getValues())) != 0)
          .toArray(PersistentField[]::new);

        for (int i = 0; i < fields.length; i++) {
            PersistentField<T> field = fields[i];
            if (!query.hasValue(field)) {
                throw new SaveQueryException(new IllegalArgumentException("Missing value for " + field + "!"), query);
            }

            FieldValue<T> value = query.getValue(field);

            String valueStr = value.getValue() == null ? "null" : this.quote(field.getType().toString(this.repository, field, value.getValue()), field);

            builder.append("`").append(field.getName()).append("`");
            valuesBuilder.append(valueStr);
            updateBuilder.append("`").append(field.getName()).append("`=").append(valueStr);

            if ((i + 1) >= fields.length) {
                continue;
            }

            builder.append(", ");
            valuesBuilder.append(", ");
            updateBuilder.append(", ");
        }

        builder.append(") VALUES (").append(valuesBuilder).append(")");

        if (!updateBuilder.toString().isEmpty()) {
            builder.append(" ON DUPLICATE KEY UPDATE ").append(updateBuilder);
        }

        builder.append(";");

        try (Connection connection = this.getConnection()) {
            int autoIncrement = this.getCurrentAutoIncrement();

            Statement statement = connection.createStatement();
            statement.execute(builder.toString());

            if (autoIncrementField == null
              // If this is the case, the value is already assigned
              || ((int) query.getValue(autoIncrementField).getValue()) != 0
              // If there is no instance present in the query, we have nothing to update
              || !query.getObjectInstance().isPresent()) {
                return;
            }

            // Update the auto increment field
            autoIncrementField.set(query.getObjectInstance().get(), autoIncrement);
        } catch (SQLException e) {
            throw new SaveQueryException("Statement: " + builder.toString(), e, query);
        }
    }

    @Override
    public void delete(Query<T> query) {
        // Check if there is a requirement. If there is not, we use the "TRUNCATE" command instead
        if (query.getRequirement() == null) {
            String statement = "TRUNCATE " + this.getTableName() + ";";

            try (Connection connection = this.getConnection()) {
                connection.createStatement().execute(statement);
            } catch (SQLException e) {
                throw new DeleteQueryException("Statement: " + statement, e, query);
            }

            return;
        }

        StringBuilder builder = new StringBuilder("DELETE FROM ")
          .append(this.getTableName())
          .append(" WHERE ")
          .append(this.toString(query.getRequirement()))
          .append(";");

        try (Connection connection = this.getConnection()) {
            connection.createStatement().execute(builder.toString());
        } catch (SQLException e) {
            throw new DeleteQueryException("Statement: " + builder.toString(), e, query);
        }
    }

    @Override
    public void drop() {
        try (Connection connection = this.getConnection()) {
            connection.createStatement().execute("DROP TABLE " + this.getTableName());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Internals

    private String getTableName() {
        return "`" + this.table + "`";
    }

    private ResponseEntity<T> fromResultSet(ResultSet set) {
        return new ResponseEntity<>(this.repository.getProfile(), this.getValuesFromResultSet(set));
    }

    private FieldValue<T>[] getValuesFromResultSet(ResultSet set) {
        try {
            PersistentField<T>[] fields = this.repository.getProfile().getFields();
            FieldValue<T>[] values = new FieldValue[fields.length];
            for (int i = 0; i < fields.length; i++) {
                PersistentField<T> field = fields[i];
                Object value = set.getObject(field.getName());
                /*
                if (field.getType().returnsPrimitive(this.repository, field)) {
                    values[i] = new FieldValue<>(field, value);
                    continue;
                }
                */

                values[i] = new FieldValue<>(field, field.getType().fromString(this.repository, field, (String) value));
            }

            return values;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String quote(String serialized, PersistentField<T> field) {
        // Primitive types don't need escape characters
        if (field.getType().returnsPrimitive(this.repository, field)) {
            return serialized;
        }

        // Add escape characters etc.
        return "\"" + serialized.replace("\"", "\\\"") + "\"";
    }

    private String toString(QueryRequirement requirement) {
        if (requirement instanceof SimpleRequirement) {
            SimpleRequirement req = (SimpleRequirement) requirement;
            String value = req.getField().getType().toString(this.repository, req.getField(), req.getValue());
            return "`" + req.getField().getName() + "`" + req.getOperator() + this.quote(value, (PersistentField<T>) req.getField());
        }

        if (requirement instanceof CombinedRequirement) {
            CombinedRequirement req = (CombinedRequirement) requirement;
            String str = this.toString(req.getRequirement1()) + " " + req.getOperator() + " " + this.toString(req.getRequirement2());
            return req.isWrapped() ? "(" + str + ")" : str;
        }

        throw new IllegalArgumentException("Unrecognised requirement " + requirement.getClass().getSimpleName() + "!");
    }

    private String toColumnDeclaration(PersistentField<T> field) {
        return "`" + field.getName() + "` " + this.getColumnType(field) + (field.isAutoIncrement() ? " UNIQUE AUTO_INCREMENT" : "");
    }

    private String getColumnType(PersistentField<T> field) {
        switch (field.getType()) {
            case STRING:
                return "VARCHAR(" + field.getSize() + ")";
            case BYTE:
                return "BYTE";
            case SHORT:
                return "SHORT";
            case ENUM:
            case INTEGER:
                return "INTEGER";
            case LONG:
                return "LONG";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE";
            case BOOLEAN:
                return "BOOLEAN";
            case LIST:
            case SET:
            case ARRAY:
            case MAP:
            case CUSTOM:
                return "LONGTEXT";
            default:
                throw new UnsupportedOperationException("Missing column type for data type " + field.getType().name() + "!");
        }
    }
}
