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
package org.jboss.hal.meta.processing;

import org.jboss.hal.meta.AddressTemplate;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HOST;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PROFILE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SERVER_GROUP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_GROUP;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_PROFILE;

/**
 * Turn the address from the r-r-d payload back into a template, which will become part of the metadata.
 * The implementation of this class plays a central role in the metadata registries. Touch only if you know what you're
 * doing!
 *
 * @author Harald Pehl
 */
class MetadataAddressToTemplate implements AddressTemplate.Unresolver {

    private final boolean asIs;

    MetadataAddressToTemplate(final boolean asIs) {this.asIs = asIs;}

    @Override
    public String unresolve(final String name, final String value, final boolean first, final boolean last,
            final int index, final int size) {

        StringBuilder segment = new StringBuilder();
        if (asIs) {
            // don't change anything!
            segment.append(name).append("=").append(value);

        } else {
            if (size == 1) {
                switch (name) {
                    case HOST:
                        segment.append(SELECTED_HOST.variable());
                        break;
                    case SERVER_GROUP:
                        segment.append(SELECTED_GROUP.variable());
                        break;
                    default:
                        segment.append(name).append("=*");
                        break;
                }

            } else {
                switch (name) {
                    case HOST:
                        segment.append(SELECTED_HOST.variable());
                        break;
                    case PROFILE:
                        segment.append(SELECTED_PROFILE.variable());
                        break;
                    case SERVER_GROUP:
                        segment.append(SELECTED_GROUP.variable());
                        break;
                    case SUBSYSTEM:
                        segment.append(SUBSYSTEM).append("=").append(value);
                        break;
                    default:
                        segment.append(name).append("=");
                        if (last) {
                            segment.append("*");
                        } else {
                            segment.append(value);
                        }
                        break;
                }
            }
        }

        return segment.toString();
    }
}
