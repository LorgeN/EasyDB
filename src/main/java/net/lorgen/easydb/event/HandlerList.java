package net.lorgen.easydb.event;

import com.google.common.collect.Lists;

import java.util.Comparator;
import java.util.List;

public class HandlerList<T> {

    private List<RegisteredListener<? super T>> listeners;

    public HandlerList() {
        this.listeners = Lists.newArrayList();
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
