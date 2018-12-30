package net.lorgen.easydb.test.item;

import net.lorgen.easydb.DeserializerConstructor;
import net.lorgen.easydb.Key;
import net.lorgen.easydb.Options;
import net.lorgen.easydb.interact.external.External;

import java.util.List;
import java.util.Map;

public class ExternalFieldItem {

    @Key(autoIncrement = true)
    private int id;

    private String name;

    @External(table = "basic_external_test", immutable = false)
    private TestItem item;

    @External(table = "list_key_external_test", immutable = false)
    @Options(typeParams = TestItem.class)
    private List<TestItem> list;

    @External(table = "list_nokey_external_test", immutable = false)
    @Options(typeParams = NoKeyItem.class)
    private List<NoKeyItem> list2;

    @External(table = "map_key_external_test", immutable = false)
    @Options(typeParams = {Integer.class, TestItem.class})
    private Map<Integer, TestItem> map;

    @External(table = "map_nokey_external_test", immutable = false)
    @Options(typeParams = {Integer.class, NoKeyItem.class})
    private Map<Integer, NoKeyItem> map2;

    @DeserializerConstructor({"id"})
    public ExternalFieldItem(int id) {
        this.id = id;
    }

    public ExternalFieldItem(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public TestItem getItem() {
        return item;
    }

    public void setItem(TestItem item) {
        this.item = item;
    }

    public List<TestItem> getList() {
        return list;
    }

    public void setList(List<TestItem> list) {
        this.list = list;
    }

    public Map<Integer, TestItem> getMap() {
        return map;
    }

    public void setMap(Map<Integer, TestItem> map) {
        this.map = map;
    }

    public List<NoKeyItem> getList2() {
        return list2;
    }

    public void setList2(List<NoKeyItem> list2) {
        this.list2 = list2;
    }

    public Map<Integer, NoKeyItem> getMap2() {
        return map2;
    }

    public void setMap2(Map<Integer, NoKeyItem> map2) {
        this.map2 = map2;
    }
}
