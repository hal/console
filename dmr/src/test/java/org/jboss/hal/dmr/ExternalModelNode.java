/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.dmr;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExternalModelNode {

    public static ModelNode read(InputStream inputStream) {
        try {
            org.jboss.dmr.ModelNode fromStream = org.jboss.dmr.ModelNode.fromStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            fromStream.writeExternal(new DataOutputStream(baos));

            ModelNode modelNode = new ModelNode();
            modelNode.readExternal(new DataInput(baos.toByteArray()));
            return modelNode;
        } catch (IOException e) {
            throw new RuntimeException("Unable to read DMR from input stream: " + e.getMessage());
        }
    }
}
