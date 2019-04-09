package org.tanberg.easydb.interact.external;

import org.tanberg.easydb.ItemRepository;
import org.tanberg.easydb.access.event.AccessorDeleteEvent;
import org.tanberg.easydb.access.event.AccessorRespondEvent;
import org.tanberg.easydb.access.event.AccessorSaveEvent;

public interface QueryHelper {

    void handle(ItemRepository repository, AccessorRespondEvent event);

    void handle(ItemRepository repository, AccessorDeleteEvent event);

    void handle(ItemRepository repository, AccessorSaveEvent event);
}
