package net.lorgen.easydb.profile.external.strategy;

import net.lorgen.easydb.Key;
import net.lorgen.easydb.interact.external.External;
import net.lorgen.easydb.profile.ItemProfile;
import net.lorgen.easydb.query.Query;

/**
 * A strategy for creating an {@link ItemProfile item profile} for a specific field to be stored
 * in its own separate table.
 * <p>
 * Example being a class called {@code Player}. The player object stores various information regarding
 * the specific player, such as statistics. Since statistics should be it's own object, and stored in
 * a separate table it is annotated {@link External}. To store it in a separate table, we need an
 * {@link ItemProfile item profile} for the repository to base the data structure. There are several
 * ways we can go about computing this profile, and this class outlines which one is the most appropriate
 * for each specific scenario.
 */
public enum ProfilerStrategy {
    /**
     * Use the object directly, not changing any values.
     * <p>
     * Cases where this may apply:
     * <ul>
     * <li>
     * Simple fields, with a direct key-to-key link between declaring class and this object type. Requires that the
     * object we're storing in this field has a field marked as a {@link Key key}.
     * </li>
     * <li>
     * Simple lists, where there is an available common indexed field, preferable a {@link Key key} in both classes.
     * </li>
     * </ul>
     * <p>
     * In normal use, one would not use all the keys from either object. Potentially one would have two classes
     * with a common type of key (e. g. a unique user ID), and whenever a {@link Query query} would be executed,
     * it would pull the key value from the values given in the query, and use them to retrieve this object from
     * the database.
     * <p>
     * One can specify which fields to combine if they are named differently, e. g. one class may be storing some
     * player data, and have a key field {@code id}. The object may be storing some statistics, but in the statistics
     * the field is called {@code playerId}. You can then specify in {@link External#keyFields()} that {@code id} is
     * equivalent to the keys in the statistics object.
     */
    DIRECT_USE,
    /**
     * Use the keys in the declaring class, and not the keys (if they exist) in the stored class.
     * <p>
     * Cases where this may apply:
     * <ul>
     * <li>
     * Simple fields, where there are no keys in the stored class.
     * </li>
     * </ul>
     */
    DECLARING_KEYS,
    /**
     * Use the keys in the declaring class, as well as some unknown index field. This means that the keys (if there
     * are any) in the stored class will be ignored.
     * <p>
     * Cases where this may apply:
     * <ul>
     * <li>
     * Collections of objects with no keys. In this case we will use an {@link Integer int} to mark the position
     * in the collection (if relevant), so that we can differentiate between the different values.
     * </li>
     * <li>
     * A map. If the stored class is a map, we will use the key in the map as the added index. This could be a
     * primitive, in which case it will just be made into its own field in the profile. It could also be another
     * object, in which case we need to compute it and its fields into the profile.
     * </li>
     * </ul>
     */
    DECLARING_KEYS_WITH_INDEX
}
