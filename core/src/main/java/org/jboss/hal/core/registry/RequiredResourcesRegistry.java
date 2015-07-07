package org.jboss.hal.core.registry;

import java.util.Set;

public interface RequiredResourcesRegistry {

    Set<String> getResources(String token);
    Set<String> getOperations(String token);
    boolean isRecursive(String token);
}
