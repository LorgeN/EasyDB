package org.tanberg.easydb.interact.external;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.event.AccessorDeleteEvent;
import org.tanberg.easydb.access.event.AccessorRespondEvent;
import org.tanberg.easydb.access.event.AccessorSaveEvent;
import org.tanberg.easydb.field.PersistentField;
import org.tanberg.easydb.profile.external.ExternalFieldProfile;
import org.tanberg.easydb.query.response.Response;

public class ExternalFieldQueryHelper implements QueryHelper {

    private ExternalFieldProfile profile;

    public ExternalFieldQueryHelper(ExternalFieldProfile profile) {
        this.profile = profile;
    }

    @Override
    public void handle(ItemRepository repository, AccessorRespondEvent event) {
        Response response = this.profile.newQuery(event.getEntity()).findFirst();
        response.getOrCreateValue(this.getField().getName())
          .setValue(this.profile.extractValue(response));
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

        Object value = event.getQuery().getValue(this.getField()).getValue();
        this.profile.save(event.getQuery(), null, value);
    }

    // Internals

    private PersistentField getField() {
        return this.profile.getField();
    }
}
