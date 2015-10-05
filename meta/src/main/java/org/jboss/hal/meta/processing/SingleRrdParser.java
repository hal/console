/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.hal.meta.processing;

import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;

/**
 * Parser for a single rrd result. The model node is expected to look like
 * <pre>
 * {
 *     "outcome" => "success",
 *     "result" => {
 *         ....
 *     }
 * }
 * </pre>
 *
 * @author Harald Pehl
 */
public class SingleRrdParser implements RrdResultParser {

    private static final Logger logger = LoggerFactory.getLogger(ListRrdParser.class);

    private final ResourceAddress operationAddress;

    public SingleRrdParser(final ResourceAddress operationAddress) {this.operationAddress = operationAddress;}

    @Override
    public List<RrdResult> parse(final ModelNode modelNode) {
        ModelNode payload = modelNode.get(RESULT);
        if (payload.isDefined()) {
            RrdResult rr = RrdParserHelper.newRrdResult(operationAddress, payload);
            return rr.isDefined() ? Collections.singletonList(rr) : Collections.<RrdResult>emptyList();
        } else {
            logger.debug("Skip undefined rrd result at node " + modelNode);
            return Collections.emptyList();
        }
    }
}
