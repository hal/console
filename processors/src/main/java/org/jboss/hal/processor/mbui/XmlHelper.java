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
package org.jboss.hal.processor.mbui;

import java.io.IOException;
import java.io.StringWriter;

import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

final class XmlHelper {

    private XmlHelper() {}

    static String xmlAsString(org.jdom2.Element element) {
        String asString;
        StringWriter writer = new StringWriter();
        try {
            new XMLOutputter(Format.getCompactFormat()).output(element, writer);
            asString = writer.toString();
        } catch (IOException e) {
            asString = "<" + element + "/>";
        }
        return asString;
    }
}
