package net.lorgen.easydb.event;

import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

public class HandlerList<T> {

    private static final List<HandlerList<?>> HANDLER_LISTS = Lists.newArrayList();

    public HandlerList() {
        this.listeners = Lists.newArrayList();

        HANDLER_LISTS.add(this);
    }

    private List<RegisteredListener<? super T>> listeners;

    public static void unregister(Listener listener) {
        HANDLER_LISTS.forEach(list -> list.getListeners().removeIf(regList -> regList.getListener() == listener));
    }

    public List<RegisteredListener<? super T>> getListeners() {
        return listeners;
    }

    public void addListener(RegisteredListener<? super T> listener) {
        this.listeners.add(listener);
        this.listeners.sort(Comparator.comparingInt(RegisteredListener::getPriority));
    }

    public void call(T event) {
        for (RegisteredListener<? super T> listener : this.listeners) {
            try {
                listener.call(event);
            } catch (EventException e) {
                e.printStackTrace();
            }
        }
    }
}
