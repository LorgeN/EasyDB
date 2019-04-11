package org.tanberg.easydb.interact.external;

import com.google.common.collect.Maps;
import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.event.AccessorDeleteEvent;
import org.tanberg.easydb.access.event.AccessorRespondEvent;
import org.tanberg.easydb.access.event.AccessorSaveEvent;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.external.ExternalFieldProfile;
import org.tanberg.easydb.query.response.Response;

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

        Collection<Response> responses = this.profile.newQuery(event.getEntity()).findAll();

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

        this.profile.newQuery(event.getQuery()).delete();
    }

    @Override
    public void handle(ItemRepository repository, AccessorSaveEvent event) {
        if (this.getField().isTransient()) {
            return;
        }

        this.profile.newQuery(event.getQuery()).delete();
        Map<?, ?> map = (Map) event.getQuery().getValue(this.getField()).getValue();
        map.forEach((key, val) -> this.profile.save(event.getQuery(), key, val));
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
