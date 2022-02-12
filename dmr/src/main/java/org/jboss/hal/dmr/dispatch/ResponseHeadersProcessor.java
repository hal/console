/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.dmr.dispatch;

import javax.annotation.Nullable;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.resources.Names;

/**
 * Interface to parse data from the responses headers of a DMR response. This is not about {@linkplain ResponseHeader HTTP
 * response headers}, but related to DMR responses only.
 * <p>
 * In standalone mode there's only a single {@code response-headers} node in the DMR response, whereas in domain mode the
 * response headers are scoped to a specific server group, host and server. The {@link #process(Header[])} method will only
 * receive the {@code response-headers} payload w/o the surrounding 'noise'.
 * <p>
 * Standalone Mode:
 *
 * <pre>
 * {
 *     "outcome" => "success",
 *     "response-headers" => {
 *         "foo" => "bar",
 *         ...
 *     }
 * }
 * </pre>
 * <p>
 * Domain Mode:
 *
 * <pre>
 * {
 *     "outcome" => "success",
 *     "server-groups" => {
 *         "main-server-group" => {
 *             "host" => {
 *                 "master" => {
 *                     "server-one" => {
 *                         "response" => {
 *                             "outcome" => "success",
 *                             "response-headers" => {
 *                                 "foo" => "bar",
 *                                 ...
 *                             }
 *                         }
 *                     },
 *                     "server-two" => {
 *                         "response" => {
 *                             "outcome" => "success",
 *                             "response-headers" => {
 *                                 "foo" => "bar",
 *                                 ...
 *                             }
 *                         }
 *                     }
 *                 }
 *             }
 *         }
 *     }
 * }
 * </pre>
 */
@FunctionalInterface
public interface ResponseHeadersProcessor {

    /**
     * Method to process the response headers. It's guaranteed that the array contains at least one element. In domain mode the
     * array contains an element for each server group-host-server triple.
     */
    void process(Header[] headers);

    class Header {

        @Nullable private final String serverGroup;
        @Nullable private final String host;
        private final String server;
        private final ModelNode header;

        public Header(final ModelNode header) {
            this(null, null, Names.STANDALONE_SERVER, header);
        }

        public Header(@Nullable final String serverGroup, @Nullable final String host, final String server,
                final ModelNode header) {
            this.serverGroup = serverGroup;
            this.host = host;
            this.server = server;
            this.header = header;
        }

        @Nullable
        public String getServerGroup() {
            return serverGroup;
        }

        @Nullable
        public String getHost() {
            return host;
        }

        public String getServer() {
            return server;
        }

        public ModelNode getHeader() {
            return header;
        }
    }
}
