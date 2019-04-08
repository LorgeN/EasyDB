package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;
import net.lorgen.easydb.event.EventHandler;
import net.lorgen.easydb.event.Listener;

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
