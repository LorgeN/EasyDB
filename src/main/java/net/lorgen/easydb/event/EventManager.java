package net.lorgen.easydb.event;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Simple event system manager for a database accessor
 */
public class EventManager {

    private Map<Class<? extends Event>, HandlerList<?>> handlerListMap;

    public EventManager() {
        this.handlerListMap = Maps.newHashMap();
    }

    public <T extends Event> void callEvent(T event) {
        HandlerList<T> handlerList = (HandlerList<T>) this.handlerListMap.computeIfAbsent(event.getClass(), eClass -> new HandlerList<>());
        handlerList.call(event);
    }

    public void registerListener(Listener listener) {
        Class<? extends Listener> consider = listener.getClass();
        while (consider != null) {
            this.registerListener(listener, consider);

            Class<?> superClass = consider.getSuperclass();
            if (superClass == null || !Listener.class.isAssignableFrom(superClass)) {
                break;
            }

            consider = (Class<? extends Listener>) superClass;
        }
    }

    public void registerListener(Listener listener, Class<? extends Listener> consider) {
        for (Method method : consider.getDeclaredMethods()) {
            EventHandler annotation = method.getAnnotation(EventHandler.class);
            if (annotation == null) {
                continue;
            }

            Class<?>[] classes = method.getParameterTypes();
            if (classes.length != 1) {
                throw new ListenerRegisterException(method.getName() + " doesn't have 1 argument!");
            }

            HandlerList handlerList = this.handlerListMap.computeIfAbsent((Class<? extends Event>) classes[0], eClass -> new HandlerList<>());
            int priority = annotation.priority();

            RegisteredListener registeredListener = new RegisteredListener<>(listener, priority, method);

            handlerList.addListener(registeredListener);
        }
    }
}
