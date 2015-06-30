package org.jboss.hal.dmr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Heiko Braun
 */
public class ResourceAccessLog {

    public interface Listener {

        void onChange();
    }
    public static ResourceAccessLog INSTANCE = new ResourceAccessLog();
    private Map<String, Set<String>> token2address = new HashMap<String, Set<String>>();
    private Set<Listener> listeners = new HashSet<Listener>();

    public void log(String token, String address) {
        if (null == token2address.get(token)) { token2address.put(token, new HashSet<String>()); }


        if (!token2address.get(token).contains(address)) { token2address.get(token).add(address); }

        fireChange();
    }

    public Iterator<String> getKeys() {
        return token2address.keySet().iterator();
    }

    private void fireChange() {
        for (Listener l : listeners) { l.onChange(); }
    }

    public Set<String> getAddresses(String token) {
        if (null == token2address.get(token)) { token2address.put(token, new HashSet<String>()); }

        return token2address.get(token);
    }

    public void flush() {
        token2address.clear();
        fireChange();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }
}
