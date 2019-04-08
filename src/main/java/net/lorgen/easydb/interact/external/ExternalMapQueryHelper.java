package net.lorgen.easydb.interact.external;

import com.google.common.collect.Maps;
import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.external.ExternalFieldProfile;
import net.lorgen.easydb.query.response.Response;

import java.util.Collection;
import java.util.Map;

public class ExternalMapQueryHelper implements QueryHelper {

    private ExternalFieldProfile profile;

    public ExternalMapQueryHelper(ExternalFieldProfile profile) {
        this.profile = profile;
    }

    @Override
    public void handle(ItemRepository repository, AccessorRespondEvent event) {
        Map map = this.newInstance();

        assert map != null;

        Collection<Response> responses = this.profile.newQuery(repository, event.getEntity()).findAll();

        for (Response entity : responses) {
            Object key = this.profile.extractKey(entity);
            Object value = this.profile.extractValue(entity);

            map.put(key, value);
        }

        event.getEntity().getOrCreateValue(this.getField().getName()).setValue(map);
    }

    @Override
    public void handle(ItemRepository repository, AccessorDeleteEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        this.profile.newQuery(repository, event.getQuery()).delete();
    }

    @Override
    public void handle(ItemRepository repository, AccessorSaveEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        this.profile.newQuery(repository, event.getQuery()).delete();
        Map<?, ?> map = (Map) event.getQuery().getValue(this.getField()).getValue();
        map.forEach((key, val) -> this.profile.save(repository, event.getQuery(), key, val));
    }

    // Internals

    private PersistentField getField() {
        return this.profile.getField();
    }

    private Map<?, ?> newInstance() {
        Class<? extends Map> typeClass = (Class<? extends Map>) this.getField().getTypeClass();
        if (!typeClass.isInterface()) {
            try {
                return typeClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return Maps.newLinkedHashMap();
    }
}
