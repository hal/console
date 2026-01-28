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
package org.jboss.hal.ballroom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.hal.dmr.Property;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import static org.jboss.hal.dmr.ModelDescriptionConstants.HAL_LABEL;

/** Generates human readable labels from terms used in the management model. */
public class LabelBuilder {

    private static final String QUOTE = "'";
    private static final String SPACE = " ";

    @SuppressWarnings("HardCodedStringLiteral") private final ImmutableMap<String, String> SPECIALS = ImmutableMap
            .<String, String> builder()
            .put("ajp", "AJP")
            .put("ccm", "CCM")
            .put("crls", "CRLs")
            .put("dn", "DN")
            .put("ear", "EAR")
            .put("ee", "EE")
            .put("ejb", "EJB")
            .put("ejb3", "EJB3")
            .put("giop", "GIOP")
            .put("gss", "GSS")
            .put("ha", "HA")
            .put("http", "HTTP")
            .put("https", "HTTPS")
            .put("http2", "HTTP/2")
            .put("id", "ID")
            .put("iiop", "IIOP")
            .put("iiop-ssl", "IIOP SSL")
            .put("io", "IO")
            .put("ip", "IP")
            .put("jaas", "JAAS")
            .put("jacc", "JACC")
            .put("jaspi", "JASPI")
            .put("jaxrs", "JAX-RS")
            .put("jboss", "JBoss")
            .put("jdbc", "JDBC")
            .put("jca", "JCA")
            .put("jdr", "JDA")
            .put("jgroups", "JGroups")
            .put("jms", "JMS")
            .put("jmx", "JMX")
            .put("jndi", "JNDI")
            .put("jpa", "JPA")
            .put("jsf", "JSF")
            .put("json", "JSON")
            .put("jsse", "JSSE")
            .put("jsr", "JSR")
            .put("jta", "JTA")
            .put("jts", "JTS")
            .put("jvm", "JVM")
            .put("jwt", "JWT")
            .put("lra", "LRA")
            .put("mcp", "MCP")
            .put("mdb", "MDB")
            .put("mbean", "MBean")
            .put("microprofile", "MicroProfile")
            .put("oauth2", "OAuth 2")
            .put("ocsp", "OCSP")
            .put("oidc", "OIDC")
            .put("openapi", "OpenAPI")
            .put("Opentelemetry", "OpenTelemetry")
            .put("otp", "OTP")
            .put("rdn", "RDN")
            .put("sar", "SAR")
            .put("sasl", "SASL")
            .put("sfsb", "SFSB")
            .put("slsb", "SLSB")
            .put("sni", "SNI")
            .put("sql", "SQL")
            .put("ssl", "SSL")
            .put("tcp", "TCP")
            .put("tls", "TLS")
            .put("ttl", "TTL")
            .put("tx", "TX")
            .put("udp", "UDP")
            .put("uri", "URI")
            .put("url", "URL")
            .put("uuid", "UUID")
            .put("vm", "VM")
            .put("xa", "XA")
            .put("wsdl", "WSDL")
            .build();

    public String label(Property property) {
        return property.getValue().hasDefined(HAL_LABEL)
                ? label(property.getValue().get(HAL_LABEL).asString())
                : label(property.getName());
    }

    public String label(String name) {
        String label = name;
        label = label.replace('-', ' ');
        label = replaceSpecial(label);
        label = capitalize(label);
        return label;
    }

    /**
     * Turns a list of names from the management model into a human readable enumeration wrapped in quotes and separated with
     * commas. The last name is separated with the specified conjunction.
     *
     * @return The list of names as human readable string or an empty string if the names are null or empty.
     */
    public String enumeration(Iterable<String> names, String conjunction) {
        String enumeration = "";
        if (names != null && !Iterables.isEmpty(names)) {
            int size = Iterables.size(names);
            if (size == 1) {
                return QUOTE + label(names.iterator().next()) + QUOTE;
            } else if (size == 2) {
                return QUOTE + label(Iterables.getFirst(names, "")) + QUOTE +
                        SPACE + conjunction + SPACE +
                        QUOTE + label(Iterables.getLast(names)) + QUOTE;
            } else {
                String last = Iterables.getLast(names);
                LinkedList<String> allButLast = new LinkedList<>();
                Iterables.addAll(allButLast, names);
                allButLast.removeLast();
                enumeration = allButLast.stream()
                        .map(name -> QUOTE + label(name) + QUOTE)
                        .collect(Collectors.joining(", "));
                enumeration = enumeration + SPACE + conjunction + SPACE + QUOTE + label(last) + QUOTE;
            }
        }
        return enumeration;
    }

    private String replaceSpecial(String label) {
        List<String> replacedParts = new ArrayList<>();
        for (String part : Splitter.on(' ').split(label)) {
            String replaced = part;
            for (Map.Entry<String, String> entry : SPECIALS.entrySet()) {
                if (replaced.length() == entry.getKey().length()) {
                    replaced = replaced.replace(entry.getKey(), entry.getValue());
                }
            }
            replacedParts.add(replaced);
        }
        return Joiner.on(SPACE).join(replacedParts);
    }

    private String capitalize(String str) {
        char[] buffer = str.toCharArray();
        boolean capitalizeNext = true;
        for (int i = 0; i < buffer.length; i++) {
            char ch = buffer[i];
            if (Character.isWhitespace(ch)) {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                buffer[i] = Character.toUpperCase(ch);
                capitalizeNext = false;
            }
        }
        return new String(buffer);
    }
}
