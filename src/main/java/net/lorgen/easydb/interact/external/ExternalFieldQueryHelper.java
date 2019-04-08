package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.field.PersistentField;
import net.lorgen.easydb.profile.external.ExternalFieldProfile;
import net.lorgen.easydb.query.response.Response;

public class ExternalFieldQueryHelper implements QueryHelper {

    private ExternalFieldProfile profile;

    public ExternalFieldQueryHelper(ExternalFieldProfile profile) {
        this.profile = profile;
    }

    @Override
    public void handle(ItemRepository repository, AccessorRespondEvent event) {
        Response response = this.profile.newQuery(repository, event.getEntity()).findFirst();
        response.getOrCreateValue(this.getField().getName())
          .setValue(this.profile.extractValue(response));
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

        Object value = event.getQuery().getValue(this.getField()).getValue();
        this.profile.save(repository, event.getQuery(), null, value);
    }

    // Internals

    private PersistentField getField() {
        return this.profile.getField();
    }
}
