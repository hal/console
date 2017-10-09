/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ProcessState implements Iterable<ServerState> {

    private final Set<ServerState> serverStates;

    ProcessState() {
        serverStates = new HashSet<>();
    }

    public void add(ServerState serverState) {
        serverStates.add(serverState);
    }

    @Override
    public Iterator<ServerState> iterator() {
        return serverStates.iterator();
    }

    public boolean isEmpty() {return serverStates.isEmpty();}

    public ServerState first() {
        if (!serverStates.isEmpty()) {
            return serverStates.iterator().next();
        }
        return null;
    }

    public int size() {return serverStates.size();}
}
