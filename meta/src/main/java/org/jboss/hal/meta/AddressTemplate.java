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
package org.jboss.hal.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.ws.rs.DELETE;

import com.google.common.collect.Lists;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.ModelNodeHelper;
import org.jboss.hal.dmr.Property;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.spi.EsParam;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * Template for a DMR address which might contain multiple variable parts.
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
 * Following variables are supported: - <code>{domain.controller}</code> - <code>{selected.profile}</code> -
 * <code>{selected.group}</code> - <code>{selected.server-config}</code> - <code>{selected.server}</code>
 * <p>
 * To get a fully qualified address from an address template use the method <code>resolve()</code>. For standalone mode
 * the variables will resolve to an empty string. The values of the variables are managed by the {@link
 * StatementContext}.
 *
 * @example AddressTemplate a2 = AddressTemplate.of("{selected.profile}"); AddressTemplate a3 =
 * AddressTemplate.of("{selected.profile}/subsystem=mail"); AddressTemplate a4 = AddressTemplate.of("{selected.profile}/subsystem=mail/mail-session=*");
 */
@JsType(namespace = "hal.meta")
public final class AddressTemplate implements Iterable<String> {

    /**
     * The root template
     */
    public static final AddressTemplate ROOT = AddressTemplate.of("/");
    public static final String EQUALS = "=";

    /** Creates a new address template from a well known placeholder. */
    @JsIgnore
    public static AddressTemplate of(StatementContext.Expression placeholder) {
        return AddressTemplate.of(String.join("/", placeholder.expression()));
    }

    /**
     * Creates a new address template from a placeholder and an encoded string template. '/' characters inside values
     * must have been encoded using {@link ModelNodeHelper#encodeValue(String)}.
     */
    @JsIgnore
    public static AddressTemplate of(StatementContext.Expression placeholder, String template) {
        return AddressTemplate.of(String.join("/", placeholder.expression(), withoutSlash(template)));
    }

    /** Creates a new address template from two placeholders. */
    @JsIgnore
    public static AddressTemplate of(StatementContext.Expression placeholder1,
            StatementContext.Expression placeholder2) {
        return AddressTemplate.of(
                String.join("/", placeholder1.expression(), placeholder2.expression()));
    }

    /**
     * Creates a new address template from two placeholders and an encoded string template. '/' characters inside values
     * must have been encoded using {@link ModelNodeHelper#encodeValue(String)}.
     */
    @JsIgnore
    public static AddressTemplate of(StatementContext.Expression placeholder1, StatementContext.Expression placeholder2,
            String template) {
        return AddressTemplate.of(
                String.join("/", placeholder1.expression(), placeholder2.expression(), withoutSlash(template)));
    }

    /** Creates a new address template from an encoded string template. */
    public static AddressTemplate of(String template) {
        return new AddressTemplate(withSlash(template));
    }

    /**
     * Turns a resource address into an address template which is the opposite of {@link #resolve(StatementContext,
     * String...)}.
     */
    @JsIgnore
    public static AddressTemplate of(ResourceAddress address) {
        return of(address, null);
    }

    /**
     * Turns a resource address into an address template which is the opposite of {@link #resolve(StatementContext,
     * String...)}. Use the {@link Unresolver} function to specify how the segments of the resource address are
     * "unresolved". It is called for each segment of the specified resource address.
     */
    @JsIgnore
    public static AddressTemplate of(ResourceAddress address, Unresolver unresolver) {
        int index = 0;
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        if (address.isDefined()) {
            int size = address.size();
            for (Iterator<Property> iterator = address.asPropertyList().iterator(); iterator.hasNext(); ) {
                Property property = iterator.next();
                String name = property.getName();
                String value = property.getValue().asString();
                if (value.contains("/")) {
                    value = ModelNodeHelper.encodeValue(value);
                }

                String segment = unresolver == null
                        ? name + EQUALS + value
                        : unresolver.unresolve(name, value, first, !iterator.hasNext(), index, size);
                builder.append(segment);

                if (iterator.hasNext()) {
                    builder.append("/");
                }
                first = false;
                index++;
            }
        }
        return of(builder.toString());
    }

    private static String withoutSlash(String template) {
        if (template != null) {
            return template.startsWith("/") ? template.substring(1) : template;
        }
        return null;
    }

    private static String withSlash(String template) {
        if (template != null && !template.startsWith(OPTIONAL) && !template.startsWith("/")) {
            return "/" + template;
        }
        return template;
    }


    // ------------------------------------------------------ template methods

    @JsIgnore public static final String OPTIONAL = "opt://";
    private static final String BLANK = "_blank";

    private final String template;
    private final LinkedList<Token> tokens;
    private final boolean optional;

    /**
     * Creates a new instance from an encoded string template. '/' characters inside values must have been encoded using
     * {@link ModelNodeHelper#encodeValue(String)}.
     *
     * @param template the encoded template.
     */
    private AddressTemplate(String template) {
        assert template != null : "template must not be null";

        this.tokens = parse(template);
        this.optional = template.startsWith(OPTIONAL);
        this.template = join(optional, tokens);
    }

    private LinkedList<Token> parse(String template) {
        LinkedList<Token> tokens = new LinkedList<>();

        if (template.equals("/")) {
            return tokens;
        }

        String normalized = template.startsWith(OPTIONAL) ? template.substring(OPTIONAL.length()) : template;
        StringTokenizer tok = new StringTokenizer(normalized);
        while (tok.hasMoreTokens()) {
            String nextToken = tok.nextToken();
            if (nextToken.contains(EQUALS)) {
                String[] split = nextToken.split(EQUALS);
                tokens.add(new Token(split[0], split[1]));
            } else {
                tokens.add(new Token(nextToken));
            }

        }
        return tokens;
    }

    private String join(boolean optional, List<Token> tokens) {
        String path = String.join("/", tokens.stream().map(Token::toString).collect(toList()));
        return optional ? OPTIONAL + path : path;
    }

    @Override
    @JsIgnore
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressTemplate)) {
            return false;
        }

        AddressTemplate that = (AddressTemplate) o;
        return optional == that.optional && template.equals(that.template);

    }

    @Override
    @JsIgnore
    public int hashCode() {
        int result = template.hashCode();
        result = 31 * result + (optional ? 1 : 0);
        return result;
    }

    /**
     * @return the string representation of this address template
     */
    @Override
    public String toString() {
        return template.length() == 0 ? "/" : template;
    }

    /**
     * @return true if this template contains no tokens, false otherwise
     */
    @JsProperty
    public boolean isEmpty() {
        return tokens.isEmpty();
    }

    /**
     * @return the number of tokens
     */
    @JsProperty(name = "size")
    public int size() {
        return tokens.size();
    }

    @Override
    @JsIgnore
    public Iterator<String> iterator() {
        return tokens.stream().map(Token::toString).collect(toList()).iterator();
    }

    /**
     * Appends the specified encoded template to this template and returns a new template. If the specified template
     * does not start with a slash, '/' is automatically appended. '/' characters inside values must have been encoded
     * using {@link ModelNodeHelper#encodeValue(String)}.
     *
     * @param template the encoded template to append (makes no difference whether it starts with '/' or not)
     * @return a new template
     */
    @JsIgnore
    public AddressTemplate append(String template) {
        String slashTemplate = template.startsWith("/") ? template : "/" + template;
        return AddressTemplate.of(this.template + slashTemplate);
    }

    @JsIgnore
    public AddressTemplate append(AddressTemplate template) {
        return append(template.toString());
    }

    /**
     * Works like {@link List#subList(int, int)} over the tokens of this template and throws the same exceptions.
     *
     * @param fromIndex low endpoint (inclusive) of the sub template
     * @param toIndex   high endpoint (exclusive) of the sub template
     * @return a new address template containing the specified tokens.
     * @throws IndexOutOfBoundsException for an illegal endpoint index value (<tt>fromIndex &lt; 0 || toIndex &gt; size
     *                                   || fromIndex &gt; toIndex</tt>)
     */
    @JsIgnore
    public AddressTemplate subTemplate(int fromIndex, int toIndex) {
        LinkedList<Token> subTokens = new LinkedList<>(this.tokens.subList(fromIndex, toIndex));
        return AddressTemplate.of(join(this.optional, subTokens));
    }

    /**
     * @return the parent address template or the root template
     */
    @JsProperty
    public AddressTemplate getParent() {
        if (isEmpty() || size() == 1) {
            return AddressTemplate.of("/");
        } else {
            return subTemplate(0, size() - 1);
        }
    }

    /**
     * Replaces one or more wildcards with the specified values starting from left to right and returns a new address
     * template.
     * <p>
     * This method does <em>not</em> resolve the address template. The returned template is still unresolved.
     *
     * @param wildcard  the first wildcard (mandatory)
     * @param wildcards more wildcards (optional)
     * @return a new (still unresolved) address template with the wildcards replaced by the specified values.
     */
    @JsIgnore
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
     * @return the name of the first segment or null if this address template is empty.
     */
    @JsProperty(name = "firstName")
    public String firstName() {
        if (!tokens.isEmpty() && tokens.getFirst().hasKey()) {
            return tokens.getFirst().getKey();
        }
        return null;
    }

    /**
     * @return the value of the first segment or null if this address template is empty.
     */
    @JsProperty(name = "firstValue")
    public String firstValue() {
        if (!tokens.isEmpty() && tokens.getFirst().hasKey()) {
            return tokens.getFirst().getValue();
        }
        return null;
    }

    /**
     * @return the name of the last segment or null if this address template is empty.
     */
    @JsProperty(name = "lastName")
    public String lastName() {
        if (!tokens.isEmpty() && tokens.getLast().hasKey()) {
            return tokens.getLast().getKey();
        }
        return null;
    }

    /**
     * @return the value of the last segment or null if this address template is empty.
     */
    @JsProperty(name = "lastValue")
    public String lastValue() {
        if (!tokens.isEmpty() && tokens.getLast().hasKey()) {
            return tokens.getLast().getValue();
        }
        return null;
    }

    @JsIgnore
    public boolean isOptional() {
        return optional;
    }

    /**
     * @return the address template
     */
    @JsProperty
    String getTemplate() {
        return template;
    }


    // ------------------------------------------------------ resolve

    /**
     * Resolve this address template against the specified statement context.
     *
     * @param context   the statement context
     * @param wildcards An optional list of values which are used to resolve any wildcards in this address template from
     *                  left to right
     * @return a fully qualified resource address which might be empty, but which does not contain any tokens
     */
    public ResourceAddress resolve(StatementContext context, @EsParam("...string") String... wildcards) {
        if (isEmpty()) {
            return ResourceAddress.root();
        }

        int wildcardCount = 0;
        ModelNode model = new ModelNode();
        Memory<String[]> tupleMemory = new Memory<>();
        Memory<String> valueMemory = new Memory<>();

        for (Token token : tokens) {
            if (!token.hasKey()) {
                // a single token, something like "{foo}" of "bar"
                String value = token.getValue();
                String[] resolvedValue;

                if (value.startsWith("{")) {
                    String variable = value.substring(1, value.length() - 1);
                    value = variable;
                    if (!tupleMemory.contains(variable)) {
                        String[] resolvedTuple = context.resolveTuple(variable, this);
                        if (resolvedTuple != null) {
                            tupleMemory.memorize(variable, singletonList(resolvedTuple));
                        }
                    }
                    resolvedValue = tupleMemory.next(value);
                } else {
                    assert value.contains(EQUALS) : "Invalid token expression " + value;
                    resolvedValue = value.split(EQUALS);
                }

                if (resolvedValue != null) {
                    model.add(resolvedValue[0], ModelNodeHelper.decodeValue(resolvedValue[1]));
                }

            } else {
                // a key/value token, something like "foo=bar", "foo=*", "{foo}=bar" or "foo={bar}"
                String keyRef = token.getKey();
                String valueRef = token.getValue();

                String resolvedKey = resolveSome(context, valueMemory, keyRef);
                String resolvedValue = resolveSome(context, valueMemory, valueRef);

                if (resolvedKey == null) {
                    resolvedKey = BLANK;
                }
                if (resolvedValue == null) {
                    resolvedValue = BLANK;
                }

                // wildcards
                String addressValue = resolvedValue;
                if ("*".equals(
                        resolvedValue) && wildcards != null && wildcards.length > 0 && wildcardCount < wildcards.length) {
                    addressValue = wildcards[wildcardCount];
                    wildcardCount++;
                }
                model.add(resolvedKey, ModelNodeHelper.decodeValue(addressValue));
            }
        }
        return new ResourceAddress(model);
    }

    private String resolveSome(StatementContext context, Memory<String> memory, String input) {
        String resolved;
        if (input.startsWith("{")) {
            input = input.substring(1, input.length() - 1);
            if (!memory.contains(input)) {
                if (context.resolve(input, this) != null) {
                    memory.memorize(input, Lists.newArrayList(context.resolve(input, this)));
                }
            }
            resolved = memory.next(input);
        } else {
            resolved = input;
        }
        return resolved;
    }


    // ------------------------------------------------------ JS methods

    /**
     * Append an address to this addrress template and return a new one.
     *
     * @param address The address to append.
     * @return a new address template with the specified address added at the end.
     */
    @JsMethod(name = "append")
    public AddressTemplate jsAppend(@EsParam("string|AddressTemplate") Object address) {
        if (address instanceof String) {
            return append(((String) address));
        } else if (address instanceof AddressTemplate) {
            return append(((AddressTemplate) address));
        }
        return this;
    }


    // ------------------------------------------------------ inner classes


    @FunctionalInterface
    public interface Unresolver {

        String unresolve(String name, String value, boolean first, boolean last, int index, int size);
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


    private static class StringTokenizer {

        private static final String DELIMITER = "/";

        private final String s;
        private final int len;

        private int pos;
        private String next;

        StringTokenizer(String s) {
            this.s = s;
            this.len = s.length();
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
            while (pos < len && DELIMITER.indexOf(s.charAt(pos)) != -1) {
                pos++;
            }

            if (pos >= len) {
                return false;
            }

            int p0 = pos++;
            while (pos < len && DELIMITER.indexOf(s.charAt(pos)) == -1) {
                pos++;
            }

            next = s.substring(p0, pos++);
            return true;
        }
    }


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
            return hasKey() ? key + EQUALS + value : value;
        }
    }
}
