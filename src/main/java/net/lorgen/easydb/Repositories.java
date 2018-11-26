package net.lorgen.easydb;

import com.google.common.collect.Lists;
import net.lorgen.easydb.connection.configuration.ConnectionConfiguration;
import net.lorgen.easydb.profile.ItemProfile;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

public class Repositories {

    private static final List<WeakReference<ItemRepository>> REPOSITORIES = Lists.newArrayList();

    public static void registerRepository(ItemRepository<?> repository) {
        WeakReference<ItemRepository> ref = new WeakReference<>(repository);
        REPOSITORIES.add(ref);
    }

    public static List<ItemRepository> getRepositories() {
        // Assuming a fair amount of calls upon this method, we shouldn't need to schedule
        // any more cleanup than this
        REPOSITORIES.removeIf(ref -> ref.get() == null);
        return REPOSITORIES.stream().map(WeakReference::get).collect(Collectors.toList());
    }

    public static <G, T extends ItemRepository<G>> T getRepositoryByRepository(Class<T> repoClass) {
        return getRepository(null, null, null, repoClass);
    }

    public static <G, T extends ItemRepository<G>> T getRepositoryByRepository(DatabaseType type, Class<T> repoClass) {
        return getRepository(type, null, null, repoClass);
    }

    public static <G, T extends ItemRepository<G>> T getRepositoryByType(Class<G> typeClass) {
        return getRepository(null, null, typeClass, null);
    }

    public static <G, T extends ItemRepository<G>> T getRepositoryByType(DatabaseType type, Class<G> typeClass) {
        return getRepository(type, null, typeClass, null);
    }

    public static <G, T extends ItemRepository<G>> T geRepositoryByTable(String table) {
        return getRepository(null, table, null, null);
    }

    public static <G, T extends ItemRepository<G>> T geRepositoryByTable(DatabaseType type, String table) {
        return getRepository(type, table, null, null);
    }

    public static <G, T extends ItemRepository<G>> T getRepository(DatabaseType type, String table, Class<G> typeClass, Class<T> repoClass) {
        Validate.isTrue(table != null || repoClass != null || typeClass != null, "Type class, table and repo can not all be null!");

        return (T) getRepositories().stream().filter(repo -> {
            if (type != null && repo instanceof DatabaseRepository && !type.isAccessor(((DatabaseRepository) repo).getDatabaseAccessor())) {
                return false;
            }

            if (typeClass != null || repo.getTypeClass() != typeClass) {
                return false;
            }

            if (!StringUtils.isBlank(table) && !repo.getTableName().equalsIgnoreCase(table)) {
                return false;
            }

            return repoClass == null || repo.getClass() == repoClass;
        }).findFirst().orElse(null);
    }

    public static <G, T extends ItemRepository<G>> T getOrCreateRepository(ConnectionConfiguration config, DatabaseType type, String table, Class<G> typeClass, Class<T> repoClass) {
        T repository = getRepository(type, table, typeClass, repoClass);
        if (repository != null) {
            return repository;
        }

        return createRepository(config, type, table, typeClass, repoClass, null);
    }

    public static <G, T extends ItemRepository<G>> T createRepository(ConnectionConfiguration config, DatabaseType type, String table, Class<G> typeClass, Class<T> repoClass, ItemProfile<T> profile) {
        boolean hasType = type != null, hasTable = table != null, hasTypeClass = typeClass != null, hasConfig = config != null, hasProfile = profile != null;

        // Special case if the repo class is not given, as we the can not iterate through the constructors
        if (repoClass == null) {
            if (!hasType || !hasTable || !hasTypeClass) {
                throw new IllegalArgumentException("Not enough arguments given to create repository!");
            }

            // Assume it's a simple repository in this case
            return (T) new SimpleRepository<>(table, typeClass, type);
        }

        // We now know for sure that the repo class is not null, and can iterate through constructors
        // looking for a suitable one to use given the parameters we have
        constLoop:
        for (Constructor<?> constructor : repoClass.getConstructors()) {
            RepositoryConstructor data = constructor.getAnnotation(RepositoryConstructor.class);
            if (data == null) {
                continue;
            }

            // The array we will use to pass constructor params, so we don't need to iterate
            // over the arguments twice if we end up using this constructor
            Object[] passedValues = new Object[constructor.getParameterCount()];

            RepositoryOption[] requiredValues = data.value();
            for (int i = 0; i < requiredValues.length; i++) {
                RepositoryOption option = requiredValues[i];
                switch (option) {
                    case TYPE:
                        if (hasTypeClass) {
                            // We have the data required at this index -> Update it in the values array
                            passedValues[i] = typeClass;
                            break;
                        }

                        // We can't use this constructor, not enough data
                        continue constLoop;
                    case TABLE:
                        if (hasTable) {
                            // We have the data required at this index -> Update it in the values array
                            passedValues[i] = table;
                            break;
                        }

                        // We can't use this constructor, not enough data
                        continue constLoop;
                    case DATABASE:
                        if (hasType) {
                            // We have the data required at this index -> Update it in the values array
                            passedValues[i] = type;
                            break;
                        }

                        // We can't use this constructor, not enough data
                        continue constLoop;
                    case CONFIG:
                        if (hasConfig) {
                            // We have the data required at this index -> Update it in the values array
                            passedValues[i] = config;
                            break;
                        }

                        // We can't use this constructor, not enough data
                        continue constLoop;
                    case PROFILE:
                        if (hasProfile) {
                            // We have the data required at this index -> Update it in the values array
                            passedValues[i] = profile;
                            break;
                        }

                        // We can't use this constructor, not enough data
                        continue constLoop;
                }
            }

            // We have found a constructor we can use. We do not consider any more constructors.
            // In the future, we may want to add a feature where we looked for the most amount of
            // used parameters (Of the ones we have), but if the implementation of the repository
            // is done correctly, this will not be necessary

            try {
                // No need to register it, this should automatically happen in the constructor
                return (T) constructor.newInstance(passedValues);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        throw new IllegalArgumentException("Unable to find suitable constructor based on arguments given!");
    }
}
