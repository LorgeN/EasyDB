package org.tanberg.easydb.test.item;

import org.tanberg.easydb.DeserializerConstructor;
import org.tanberg.easydb.Key;
import org.tanberg.easydb.Options;
import org.tanberg.easydb.interact.external.External;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class ExternalFieldItem {

    @Key(autoIncrement = true)
    private int itemId;

    private String name;

    @External(table = "basic_external_test", immutable = false, keyFields = "itemId")
    private TestItem item;

    @External(table = "list_key_external_test", immutable = false, keyFields = "name")
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

    @DeserializerConstructor({"itemId"})
    public ExternalFieldItem(int itemId) {
        this.itemId = itemId;
    }

    public ExternalFieldItem(String name) {
        this.name = name;
    }

    public int getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExternalFieldItem)) {
            return false;
        }

        ExternalFieldItem that = (ExternalFieldItem) o;
        boolean basicSame = itemId == that.itemId &&
          Objects.equals(name, that.name) &&
          Objects.equals(item, that.item) &&
          Objects.equals(list.size(), that.list.size()) &&
          Objects.equals(list2.size(), that.list2.size()) &&
          Objects.equals(map.size(), that.map.size()) &&
          Objects.equals(map2.size(), that.map2.size());
        if (!basicSame) {
            return false;
        }

        for (TestItem testItem : this.list) {
            if (that.list.contains(testItem)) {
                continue;
            }

            return false;
        }

        for (NoKeyItem noKeyItem : this.list2) {
            if (that.list2.contains(noKeyItem)) {
                continue;
            }

            return false;
        }

        for (Entry<Integer, TestItem> entry : this.map.entrySet()) {
            if (that.map.containsKey(entry.getKey())
            && that.map.get(entry.getKey()).equals(entry.getValue())) {
                continue;
            }

            return false;
        }

        for (Entry<Integer, NoKeyItem> entry : this.map2.entrySet()) {
            if (that.map2.containsKey(entry.getKey())
              && that.map2.get(entry.getKey()).equals(entry.getValue())) {
                continue;
            }

            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, name, item, list, list2, map, map2);
    }

    @Override
    public String toString() {
        return "ExternalFieldItem{" +
          "id=" + itemId +
          ", name='" + name + '\'' +
          ", item=" + item +
          ", list=" + list +
          ", list2=" + list2 +
          ", map=" + map +
          ", map2=" + map2 +
          '}';
    }
}
