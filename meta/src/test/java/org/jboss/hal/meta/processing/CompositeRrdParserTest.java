/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.meta.processing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.hal.dmr.Composite;
import org.jboss.hal.dmr.CompositeResult;
import org.jboss.hal.dmr.ExternalModelNode;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.junit.Test;

import static java.util.stream.Collectors.toList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.READ_RESOURCE_DESCRIPTION_OPERATION;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RECURSIVE;
import static org.jboss.hal.meta.processing.RrdParserTestHelper.assertResourceDescriptions;

@SuppressWarnings({ "HardCodedStringLiteral", "DuplicateStringLiteralInspection" })
public class CompositeRrdParserTest {

    private static final String[] FLAT_TEMPLATES = new String[] {
            "/subsystem=undertow",
            "/subsystem=undertow/server=*",
            "/subsystem=undertow/server=*/host=*",
            "/subsystem=undertow/server=*/http-listener=*",
            "/subsystem=undertow/server=*/https-listener=*",
            "/subsystem=undertow/server=*/ajp-listener=*"
    };
    private static final String[] RECURSIVE_TEMPLATES = Stream.concat(Arrays.stream(FLAT_TEMPLATES),
            Arrays.stream(new String[] {
                    // additional templates
                    "/subsystem=undertow/configuration=filter",
                    "/subsystem=undertow/configuration=filter/error-page=*",
                    "/subsystem=undertow/configuration=filter/expression-filter=*",
                    "/subsystem=undertow/configuration=filter/gzip=*",
                    "/subsystem=undertow/configuration=filter/mod-cluster=*",
                    "/subsystem=undertow/configuration=filter/mod-cluster=*/balancer=*",
                    "/subsystem=undertow/configuration=filter/mod-cluster=*/balancer=*/node=*",
                    "/subsystem=undertow/configuration=filter/mod-cluster=*/balancer=*/node=*/context=*",
                    "/subsystem=undertow/configuration=filter/mod-cluster=*/balancer=*/load-balancing-group=*",
                    "/subsystem=undertow/configuration=filter/rewrite=*",
                    "/subsystem=undertow/configuration=filter/connection-limit=*",
                    "/subsystem=undertow/configuration=filter/response-header=*",
                    "/subsystem=undertow/configuration=filter/custom-filter=*",
                    "/subsystem=undertow/configuration=handler",
                    "/subsystem=undertow/configuration=handler/file=*",
                    "/subsystem=undertow/configuration=handler/reverse-proxy=*",
                    "/subsystem=undertow/configuration=handler/reverse-proxy=*/host=*",
                    "/subsystem=undertow/servlet-container=*",
                    "/subsystem=undertow/servlet-container=*/welcome-file=*",
                    "/subsystem=undertow/servlet-container=*/mime-mapping=*",
                    "/subsystem=undertow/servlet-container=*/setting=jsp",
                    "/subsystem=undertow/servlet-container=*/setting=persistent-sessions",
                    "/subsystem=undertow/servlet-container=*/setting=session-cookie",
                    "/subsystem=undertow/servlet-container=*/setting=websockets",
                    "/subsystem=undertow/server=*/host=*/setting=access-log",
                    "/subsystem=undertow/server=*/host=*/setting=single-sign-on",
                    "/subsystem=undertow/server=*/host=*/location=*",
                    "/subsystem=undertow/server=*/host=*/location=*/filter-ref=*",
                    "/subsystem=undertow/server=*/host=*/filter-ref=*",
                    "/subsystem=undertow/buffer-cache=*",
            })).toArray(String[]::new);

    @Test
    public void parseFlat() {
        List<Operation> operations = Arrays.stream(FLAT_TEMPLATES)
                .map(template -> new Operation.Builder(AddressTemplate.of(template).resolve(StatementContext.NOOP),
                        READ_RESOURCE_DESCRIPTION_OPERATION).build())
                .collect(toList());
        Composite composite = new Composite(operations);

        ModelNode modelNode = ExternalModelNode
                .read(CompositeRrdParserTest.class.getResourceAsStream("composite_rrd_flat_description_only.dmr"));
        RrdResult rrdResult = new CompositeRrdParser(composite).parse(new CompositeResult(modelNode));

        assertResourceDescriptions(rrdResult, 6, FLAT_TEMPLATES);
    }

    @Test
    public void parseRecursive() {
        List<Operation> operations = Arrays.stream(FLAT_TEMPLATES)
                .map(template -> new Operation.Builder(AddressTemplate.of(template).resolve(StatementContext.NOOP),
                        READ_RESOURCE_DESCRIPTION_OPERATION).param(RECURSIVE, true).build())
                .collect(toList());
        Composite composite = new Composite(operations);

        ModelNode modelNode = ExternalModelNode
                .read(CompositeRrdParserTest.class.getResourceAsStream("composite_rrd_recursive_description_only.dmr"));
        RrdResult rrdResult = new CompositeRrdParser(composite).parse(new CompositeResult(modelNode));

        // There must be no duplicates!
        assertResourceDescriptions(rrdResult, 36, RECURSIVE_TEMPLATES);
    }
}