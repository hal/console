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

import java.util.ArrayList;
import java.util.List;

import static org.jboss.hal.dmr.ModelDescriptionConstants.ADDRESS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESULT;

/**
 * Parser for a list of rrd results. The model node is expected to look like
 * <pre>
 * {
 *     "outcome" => "success",
 *     "result" => [
 *         {
 *             "address" => [
 *                 ("first" => "resource"),
 *                 ("foo" => "*")
 *             ],
 *             "outcome" => "success",
 *             "result" => {
 *                 ...
 *             }
 *         },
 *         {
 *             "address" => [
 *                 ("second" => "resource"),
 *                 ("bar" => "*")
 *             ],
 *             "outcome" => "success",
 *             "result" => {
 *                 ...
 *             }
 *         },
 *         ...
 *     ]
 * }
 * </pre>
 *
 * @author Harald Pehl
 */
public class ListRrdParser implements RrdResultParser {

    private static final Logger logger = LoggerFactory.getLogger(ListRrdParser.class);

    @Override
    public List<RrdResult> parse(final ModelNode modelNode) {
        List<RrdResult> results = new ArrayList<>();
        List<ModelNode> nodes = modelNode.asList();
        for (ModelNode node : nodes) {
            if (node.isFailure()) {
                throw new ParserException("Failed outcome in list rrd result at node " + node);
            }
            ModelNode payload = node.get(RESULT);
            if (payload.isDefined()) {
                RrdResult rr = RrdParserHelper.newRrdResult(new ResourceAddress(node.get(ADDRESS)), payload);
                if (rr.isDefined()) {
                    results.add(rr);
                }
            } else {
                logger.debug("Skip undefined rrd result at node " + node);
            }
        }
        return results;
    }
}
