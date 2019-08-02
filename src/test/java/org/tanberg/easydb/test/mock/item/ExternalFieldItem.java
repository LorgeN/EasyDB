package org.tanberg.easydb.test.mock.item;

import org.tanberg.easydb.DeserializerConstructor;
import org.tanberg.easydb.Key;
import org.tanberg.easydb.Options;
import org.tanberg.easydb.interact.external.External;

import java.util.List;
import java.util.Map;

public class ExternalFieldItem {

    @Key(autoIncrement = true)
    private int itemId;

    private String name;

    @External(table = "basic_external_test", immutable = false, keyFields = "itemId")
    private SimpleFieldsItem item;

    @External(table = "list_key_external_test", immutable = false, keyFields = "name")
    @Options(typeParams = SimpleFieldsItem.class)
    private List<SimpleFieldsItem> list;

    @External(table = "list_nokey_external_test", immutable = false)
    @Options(typeParams = NoKeyItem.class)
    private List<NoKeyItem> list2;

    @External(table = "map_key_external_test", immutable = false)
    @Options(typeParams = {Integer.class, SimpleFieldsItem.class})
    private Map<Integer, SimpleFieldsItem> map;

    @External(table = "map_nokey_external_test", immutable = false)
    @Options(typeParams = {Integer.class, NoKeyItem.class})
    private Map<Integer, NoKeyItem> map2;

    @DeserializerConstructor({"itemId"})
    public ExternalFieldItem(int itemId) {
        this.itemId = itemId;
    }

    public ExternalFieldItem(String name) {
        this.name = name;
    }
}
