package net.lorgen.easydb.interact.external;

import net.lorgen.easydb.ItemRepository;
import net.lorgen.easydb.access.event.AccessorDeleteEvent;
import net.lorgen.easydb.access.event.AccessorRespondEvent;
import net.lorgen.easydb.access.event.AccessorSaveEvent;

public interface QueryHelper {

    void handle(ItemRepository repository, AccessorRespondEvent event);

    void handle(ItemRepository repository, AccessorDeleteEvent event);

    void handle(ItemRepository repository, AccessorSaveEvent event);
}
