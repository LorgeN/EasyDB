package net.lorgen.easydb.example;

import net.lorgen.easydb.DataType;
import net.lorgen.easydb.profile.ItemProfileBuilder;
import net.lorgen.easydb.query.QueryBuilder;

public class BuilderExample {

    static {
        // QUERY BUILDER

        // I added some indents here to make it easier to visualize. If someone was to run clean-up on this it would
        // disappear though, so please don't do that
        new QueryBuilder<>(null) // This is NOT how you make a new builder, but for this example we use it
          .set("amount", 18) // Define a new value we update to
          .where() // Start building a requirement
              .open()  // We move down a level, so we can make a sub requirement of some form
                .equals("id", 15) // We are looking for entries where the "id" is 15
                .orEquals("id", 16) // Or the "id" is 16
              .closeCurrent() // We move back up a level, we are now back at the "main" level of the requirement
              .andOpen() // We now move back up. This is a different requirement. So far the ID can be either 15 or 16.
                .equals("number", 12) // Now "number" has to be 12
                .orEquals("number", 13) // Now it can also be 14
              .closeCurrent() // Back up to "main" level
              .orEquals("id", 17) // We can also add an argument at the "main" level
          .closeAll() // Complete the requirement (In SQL, this would be after the "WHERE")
          .build(); // Build the query
            // Alternatively we have a few methods for executing it directly from the builder which can be quite useful

        // FIELD BUILDER

        new ItemProfileBuilder<>(Object.class)
          .newField()
            .setName("Test")
            .setType(DataType.STRING)
            .setAsKey(true)
            .setSize(16)
            .buildAndAddField()
          .fromTypeClass() // If Object now has any fields, this would add them after the "Test" key field
          .build();
    }
}
