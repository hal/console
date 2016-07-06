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
package org.jboss.hal.core.finder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.core.runtime.group.ServerGroup;
import org.jboss.hal.core.runtime.host.Host;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.IdBuilder;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;

import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Harald Pehl
 */
public class FinderPath implements Iterable<FinderSegment> {

    // ------------------------------------------------------ static factory methods

    private static final Constants CONSTANTS = GWT.create(Constants.class);

    public static FinderPath from(String path) {
        List<FinderSegment> segments = new ArrayList<>();

        Map<String, String> parts = Splitter.on('/').withKeyValueSeparator('=').split(path);
        for (Map.Entry<String, String> entry : parts.entrySet()) {
            segments.add(new FinderSegment(entry.getKey(), entry.getValue()));
        }

        return new FinderPath(segments);
    }

    public static FinderPath configurationSubsystemPath(String profile, String subsystem) {
        FinderPath path = new FinderPath();
        if (profile == null) {
            path.append(CONFIGURATION, Names.SUBSYSTEMS.toLowerCase(), Names.CONFIGURATION, Names.SUBSYSTEMS);
        } else {
            path.append(CONFIGURATION, Names.PROFILES.toLowerCase(), Names.CONFIGURATION, Names.PROFILES)
                    .append(PROFILE, profile, Names.PROFILES);
        }
        String subsystemLabel = new LabelBuilder().label(subsystem);
        path.append(SUBSYSTEM, subsystem, Names.SUBSYSTEM, subsystemLabel);
        return path;
    }

    public static FinderPath runtimeHostPath(String host) {
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY_COLUMN, IdBuilder.asId(Names.HOSTS), CONSTANTS.browseBy(), Names.HOSTS)
                .append(HOST, Host.id(host), Names.HOST, host);
    }

    public static FinderPath runtimeHostPath(String host, String server) {
        return runtimeHostPath(host).append(SERVER, Server.id(server), Names.SERVER, server);
    }

    public static FinderPath runtimeServerGroupPath(String serverGroup) {
        return new FinderPath()
                .append(Ids.DOMAIN_BROWSE_BY_COLUMN, IdBuilder.asId(Names.SERVER_GROUPS),
                        CONSTANTS.browseBy(), Names.SERVER_GROUPS)
                .append(SERVER_GROUP, ServerGroup.id(serverGroup), Names.SERVER_GROUP, serverGroup);
    }

    public static FinderPath runtimeServerGroupPath(String serverGroup, String server) {
        return runtimeServerGroupPath(serverGroup).append(SERVER, Server.id(server), Names.SERVER, server);
    }


    // ------------------------------------------------------ instance section

    private final List<FinderSegment> segments;

    public FinderPath() {
        this(Collections.emptyList());
    }

    public FinderPath(final List<FinderSegment> segments) {
        // this.segments needs to be modified. So make sure the underlying implementation
        // supports this even if the list passed as parameters does not.
        this.segments = new ArrayList<>();
        this.segments.addAll(segments);
    }

    public FinderPath append(String key, String value) {
        return append(key, value, key, value);
    }

    public FinderPath append(final String key, String value, String breadcrumbKey) {
        return append(key, value, breadcrumbKey, value);
    }

    public FinderPath append(String key, String value, String breadcrumbKey, String breadcrumbValue) {
        segments.add(new FinderSegment(key, value, breadcrumbKey, breadcrumbValue));
        return this;
    }

    public <T> FinderPath append(FinderColumn<T> finderColumn) {
        FinderSegment<T> segment = new FinderSegment<>(finderColumn);
        segments.add(segment);
        return this;
    }

    @Override
    public Iterator<FinderSegment> iterator() {
        return segments.iterator();
    }

    public boolean isEmpty() {return segments.isEmpty();}

    public int size() {return segments.size();}

    public void clear() {segments.clear();}

    /**
     * @return a reversed copy of this path. The current path is not modified.
     */
    FinderPath reversed() {
        return new FinderPath(Lists.reverse(segments));
    }

    @Override
    public String toString() {
        return segments.stream()
                .filter(segment -> segment.getValue() != null)
                .map(FinderSegment::toString)
                .collect(Collectors.joining("/"));
    }
}
