package org.tanberg.easydb.interact.external;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.event.AccessorDeleteEvent;
import org.tanberg.easydb.access.event.AccessorRespondEvent;
import org.tanberg.easydb.access.event.AccessorSaveEvent;
import org.tanberg.easydb.event.EventHandler;
import org.tanberg.easydb.event.Listener;

public class QueryHelperListener implements Listener {

    private ItemRepository repository;
    private QueryHelper helper;

    public QueryHelperListener(ItemRepository repository, QueryHelper helper) {
        this.repository = repository;
        this.helper = helper;
    }

    @EventHandler
    public void onRespond(AccessorRespondEvent event) {
        this.helper.handle(this.repository, event);
    }

    @EventHandler
    public void onDelete(AccessorDeleteEvent event) {
        this.helper.handle(this.repository, event);
    }

    @EventHandler
    public void onSave(AccessorSaveEvent event) {
        this.helper.handle(this.repository, event);
    }
}
