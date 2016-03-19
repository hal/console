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
package org.jboss.hal.meta;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.model.ResourceAddress;
import org.jetbrains.annotations.NonNls;

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
public final class AddressTemplate {

    // ------------------------------------------------------ factory

    public static AddressTemplate of(@NonNls String template) {
        return new AddressTemplate(template);
    }


    // ------------------------------------------------------ template methods

    private static final String BLANK = "_blank";
    private static final String OPT = "opt:/"; //NON-NLS

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
        StringTokenizer tok = new StringTokenizer(normalized);
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
        return template;
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
    public AddressTemplate append(@NonNls String template) {
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

    public boolean isOptional() {
        return optional;
    }

    String getTemplate() {
        return template;
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
                        if (context.resolveTuple(tokenRef) != null) {
                            tupleMemory.memorize(tokenRef,
                                    Lists.<String[]>newArrayList(context.resolveTuple(tokenRef)));
                        }
                    }
                    resolvedValue = tupleMemory.next(tokenRef);
                } else {
                    assert tokenRef.contains("=") : "Invalid token expression " + tokenRef;
                    resolvedValue = tokenRef.split("=");
                }

                if (resolvedValue != null) {
                    model.add(resolvedValue[0], resolvedValue[1]);
                }

            } else {
                // a value expression. key and value of the expression might be resolved
                String keyRef = token.getKey();
                String valueRef = token.getValue();

                String resolvedKey = resolveSome(context, valueMemory, keyRef);
                String resolvedValue = resolveSome(context, valueMemory, valueRef);

                if (resolvedKey == null) { resolvedKey = BLANK; }
                if (resolvedValue == null) { resolvedValue = BLANK; }

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

    private String resolveSome(StatementContext context, Memory<String> memory, String input) {
        String resolved;
        if (input.startsWith("{")) {
            input = input.substring(1, input.length() - 1);
            if (!memory.contains(input)) {
                if (context.resolve(input) != null) {
                    memory.memorize(input, Lists.newArrayList(context.resolve(input)));
                }
            }
            resolved = memory.next(input);
        } else {
            resolved = input;
        }
        return resolved;
    }


    // ------------------------------------------------------ inner classes


    private static class Token {

        final String key;
        final String value;

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


    // TODO tokenizer does not work for addresses like
    // "subsystem=undertow/server=default-server/host=default-host/location=/"
    private static class StringTokenizer {

        private final String delim;
        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s) {
            this.s = s;
            this.delim = "/";
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

        final Map<String, List<T>> values = new HashMap<>();
        final Map<String, Integer> indexes = new HashMap<>();

        boolean contains(String key) {
            return values.containsKey(key);
        }

        void memorize(String key, List<T> resolved) {
            int startIdx = resolved.isEmpty() ? 0 : resolved.size() - 1;
            values.put(key, resolved);
            indexes.put(key, startIdx);
        }

        T next(String key) {
            T result = null;

            if (values.containsKey(key) && indexes.containsKey(key)) {
                List<T> items = values.get(key);
                Integer idx = indexes.get(key);

                if (!items.isEmpty() && idx >= 0) {
                    result = items.get(idx);
                    indexes.put(key, --idx);
                }
            }
            return result;
        }
    }
}
