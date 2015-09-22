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
package org.jboss.hal.dmr.model;

import com.google.common.base.Joiner;
import org.jboss.hal.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Wrapper for a DMR address which might contain multiple variable parts.
 * <p>
 * An address template can be defined using the following BNF:
 * <pre>
 * &lt;address template&gt; ::= "/" | &lt;segment&gt;
 * &lt;segment&gt;          ::= &lt;tuple&gt; | &lt;segment&gt;"/"&lt;tuple&gt;
 * &lt;tuple&gt;            ::= &lt;variable&gt; | &lt;key&gt;"="&lt;value&gt;
 * &lt;variable&gt;         ::= "{"&lt;alpha&gt;"}"
 * &lt;key&gt;              ::= &lt;alpha&gt;
 * &lt;value&gt;            ::= &lt;variable&gt; | &lt;alpha&gt; | "*"
 * &lt;alpha&gt;            ::= &lt;upper&gt; | &lt;lower&gt;
 * &lt;upper&gt;            ::= "A" | "B" | … | "Z"
 * &lt;lower&gt;            ::= "a" | "b" | … | "z"
 * </pre>
 * <p>
 * Here are some examples for address templates:
 * <pre>
 *     AddressTemplate a1 = AddressTemplate.of("/");
 *     AddressTemplate a2 = AddressTemplate.of("{selected.profile}");
 *     AddressTemplate a3 = AddressTemplate.of("{selected.profile}/subsystem=mail");
 *     AddressTemplate a4 = AddressTemplate.of("{selected.profile}/subsystem=mail/mail-session=*");
 * </pre>
 * <p>
 * To get a fully qualified address from an address template use the {@link #resolve(StatementContext, String...)}
 * method.
 *
 * @author Harald Pehl
 */
public class AddressTemplate {

    // ------------------------------------------------------ factory

    public static AddressTemplate of(String template) {
        return new AddressTemplate(template);
    }


    // ------------------------------------------------------ template methods

    private static final String OPT = "opt:/";
    private static final Logger logger = LoggerFactory.getLogger(AddressTemplate.class);

    private final String template;
    private final LinkedList<Token> tokens;
    private final boolean optional;

    private AddressTemplate(String template) {
        assert template != null : "template must not be null";

        this.tokens = parse(template);
        this.optional = template.startsWith(OPT);
        this.template = join(optional, tokens);
    }

    private LinkedList<Token> parse(String template) {
        LinkedList<Token> tokens = new LinkedList<>();

        if (template.equals("/")) {
            return tokens;
        }

        String normalized = template.startsWith(OPT) ? template.substring(5) : template;
        StringTokenizer tok = new StringTokenizer(normalized, "/");
        while (tok.hasMoreTokens()) {
            String nextToken = tok.nextToken();
            if (nextToken.contains("=")) {
                String[] split = nextToken.split("=");
                tokens.add(new Token(split[0], split[1]));
            } else {
                tokens.add(new Token(nextToken));
            }

        }
        return tokens;
    }

    private String join(boolean optional, LinkedList<Token> tokens) {
        StringBuilder builder = new StringBuilder();
        if (optional) {
            builder.append(OPT);
        }
        Joiner.on('/').appendTo(builder, tokens);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof AddressTemplate)) { return false; }

        AddressTemplate that = (AddressTemplate) o;
        return optional == that.optional && template.equals(that.template);

    }

    @Override
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + (optional ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return getTemplate();
    }

    /**
     * @return {@code true} if this template contains no tokens, {@code false} otherwise
     */
    public boolean isEmpty() {return tokens.isEmpty();}

    /**
     * @return the number of tokens
     */
    public int size() {return tokens.size();}

    /**
     * Appends the specified template to this template and returns a new template. If the specified template does
     * not start with a slash, "/" is automatically appended.
     *
     * @param template the template to append (makes no difference whether it starts with "/" or not)
     *
     * @return a new template
     */
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

    /**
     * Works like {@link List#subList(int, int)} over the tokens of this template and throws the same exceptions.
     *
     * @param fromIndex low endpoint (inclusive) of the sub template
     * @param toIndex   high endpoint (exclusive) of the sub template
     *
     * @return a new address template containing the specified tokens.
     *
     * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *                                   (<tt>fromIndex &lt; 0 || toIndex &gt; size ||
     *                                   fromIndex &gt; toIndex</tt>)
     */
    public AddressTemplate subTemplate(int fromIndex, int toIndex) {
        LinkedList<Token> subTokens = new LinkedList<>();
        subTokens.addAll(this.tokens.subList(fromIndex, toIndex));
        return AddressTemplate.of(join(this.optional, subTokens));
    }

    /**
     * Replaces one or more wildcards with the specified values starting from left to right and returns a new
     * address template.
     * <p>
     * This method does <em>not</em> resolve the address template. The returned template is still unresolved.
     *
     * @param wildcard  the first wildcard (mandatory)
     * @param wildcards more wildcards (optional)
     *
     * @return a new (still unresolved) address template with the wildcards replaced by the specified values.
     */
    public AddressTemplate replaceWildcards(String wildcard, String... wildcards) {
        List<String> allWildcards = new ArrayList<>();
        allWildcards.add(wildcard);
        if (wildcards != null) {
            allWildcards.addAll(Arrays.asList(wildcards));
        }

        LinkedList<Token> replacedTokens = new LinkedList<>();
        Iterator<String> wi = allWildcards.iterator();
        for (Token token : tokens) {
            if (wi.hasNext() && token.hasKey() && "*".equals(token.getValue())) {
                replacedTokens.add(new Token(token.getKey(), wi.next()));
            } else {
                replacedTokens.add(new Token(token.key, token.value));
            }
        }
        return AddressTemplate.of(join(this.optional, replacedTokens));
    }

    /**
     * Returns the resource type of the last segment for this address template
     *
     * @return the resource type
     */
    public String getResourceType() {
        if (!tokens.isEmpty() && tokens.getLast().hasKey()) {
            return tokens.getLast().getKey();
        }
        return null;
    }

    public String getTemplate() {
        return template;
    }

    public boolean isOptional() {
        return optional;
    }

    // ------------------------------------------------------ resolve

    /**
     * Resolve this address template against the specified statement context.
     *
     * @param context   the statement context
     * @param wildcards An optional list of wildcards which are used to resolve any wildcards in this address template
     *
     * @return a full qualified resource address which might be empty, but which does not contain any tokens
     */
    public ResourceAddress resolve(StatementContext context, String... wildcards) {

        int wildcardCount = 0;
        ModelNode model = new ModelNode();
        Memory<String[]> tupleMemory = new Memory<>();
        Memory<String> valueMemory = new Memory<>();

        for (Token token : tokens) {
            if (!token.hasKey()) {
                // a single token or token expression
                String tokenRef = token.getValue();
                String[] resolvedValue;

                if (tokenRef.startsWith("{")) {
                    tokenRef = tokenRef.substring(1, tokenRef.length() - 1);
                    if (!tupleMemory.contains(tokenRef)) {
                        tupleMemory.memorize(tokenRef, context.collectTuples(tokenRef));
                    }
                    resolvedValue = tupleMemory.next(tokenRef);
                } else {
                    assert tokenRef.contains("=") : "Invalid token expression " + tokenRef;
                    resolvedValue = tokenRef.split("=");
                }

                if (resolvedValue == null) {
                    logger.warn("Suppress token expression '{}'. It cannot be resolved", tokenRef);
                } else {
                    model.add(resolvedValue[0], resolvedValue[1]);
                }

            } else {
                // a value expression. key and value of the expression might be resolved
                String keyRef = token.getKey();
                String valueRef = token.getValue();

                String resolvedKey;
                String resolvedValue;

                if (keyRef.startsWith("{")) {
                    keyRef = keyRef.substring(1, keyRef.length() - 1);
                    if (!valueMemory.contains(keyRef)) {
                        valueMemory.memorize(keyRef, context.collect(keyRef));
                    }
                    resolvedKey = valueMemory.next(keyRef);
                } else {
                    resolvedKey = keyRef;
                }

                if (valueRef.startsWith("{")) {
                    valueRef = valueRef.substring(1, valueRef.length() - 1);
                    if (!valueMemory.contains(valueRef)) {
                        valueMemory.memorize(valueRef, context.collect(valueRef));
                    }
                    resolvedValue = valueMemory.next(valueRef);
                } else {
                    resolvedValue = valueRef;
                }

                if (resolvedKey == null) { resolvedKey = "_blank"; }
                if (resolvedValue == null) { resolvedValue = "_blank"; }

                // wildcards
                String addressValue = resolvedValue;
                if ("*".equals(
                        resolvedValue) && wildcards != null && wildcards.length > 0 && wildcardCount < wildcards.length) {
                    addressValue = wildcards[wildcardCount];
                    wildcardCount++;
                }
                model.add(resolvedKey, addressValue);
            }
        }
        return new ResourceAddress(model);
    }


    // ------------------------------------------------------ inner classes


    private static class Token {

        String key;
        String value;

        Token(String key, String value) {
            this.key = key;
            this.value = value;
        }

        Token(String value) {
            this.key = null;
            this.value = value;
        }

        boolean hasKey() {
            return key != null;
        }

        String getKey() {
            return key;
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return hasKey() ? key + "=" + value : value;
        }
    }


    private static class StringTokenizer {

        private final String delim;
        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s, String delim) {
            this.s = s;
            this.delim = delim;
            len = s.length();
        }

        String nextToken() {
            if (!hasMoreTokens()) {
                throw new NoSuchElementException();
            }
            String result = next;
            next = null;
            return result;
        }

        boolean hasMoreTokens() {
            if (next != null) {
                return true;
            }
            // skip leading delimiters
            while (pos < len && delim.indexOf(s.charAt(pos)) != -1) {
                pos++;
            }

            if (pos >= len) {
                return false;
            }

            int p0 = pos++;
            while (pos < len && delim.indexOf(s.charAt(pos)) == -1) {
                pos++;
            }

            next = s.substring(p0, pos++);
            return true;
        }
    }


    private static class Memory<T> {

        Map<String, LinkedList<T>> values = new HashMap<>();
        Map<String, Integer> indexes = new HashMap<>();

        boolean contains(String key) {
            return values.containsKey(key);
        }

        void memorize(String key, LinkedList<T> resolved) {
            int startIdx = resolved.isEmpty() ? 0 : resolved.size() - 1;
            values.put(key, resolved);
            indexes.put(key, startIdx);
        }

        T next(String key) {
            T result = null;

            LinkedList<T> items = values.get(key);
            Integer idx = indexes.get(key);

            if (!items.isEmpty() && idx >= 0) {
                result = items.get(idx);
                indexes.put(key, --idx);
            }
            return result;
        }
    }
}
