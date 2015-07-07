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
package org.jboss.hal.processors;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import org.jboss.hal.spi.RequiredResources;
import org.jboss.hal.spi.Scope;
import org.jboss.hal.spi.SearchIndex;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

/**
 * Processor which scans all {@code @NameToken}s annotations and generates several registry and helper classes based
 * on the additional annotations bound to each place.
 * <dl>
 * <dt>{@code @RequiredResources}</dt>
 * <dd>All required resources are collected and stored in implementations of {@code NameTokenRegistry} and
 * {@code RequiredResourcesRegistry}</dd>
 * <dt>{@code @SearchIndex}</dt>
 * <dd>All keywords are collected and stored in an implementation of {@code SearchIndexRegistry}</dd>
 * </dl>
 *
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.gwtplatform.mvp.client.annotations.NameToken")
public class NameTokenProcessor extends AbstractHalProcessor {

    static final String NAME_TOKEN_TEMPLATE = "NameTokens.ftl";
    static final String NAME_TOKEN_PACKAGE = "org.jboss.hal.core.registry";
    static final String NAME_TOKEN_CLASS = "NameTokenRegistryImpl";

    static final String REQUIRED_RESOURCES_TEMPLATE = "RequiredResources.ftl";
    static final String REQUIRED_RESOURCES_PACKAGE = "org.jboss.hal.core.registry";
    static final String REQUIRED_RESOURCES_CLASS = "RequiredResourcesRegistryImpl";

    static final String SEARCH_INDEX_TEMPLATE = "SearchIndex.ftl";
    static final String SEARCH_INDEX_PACKAGE = "org.jboss.hal.core.registry";
    static final String SEARCH_INDEX_CLASS = "SearchIndexRegistryImpl";

    static final String REGISTRY_MODULE_TEMPLATE = "RegistryModule.ftl";
    static final String REGISTRY_MODULE_PACKAGE = "org.jboss.hal.client.gin";
    static final String REGISTRY_MODULE_CLASS = "GeneratedRegistryModule";

    private final Set<TokenInfo> tokenInfos;

    public NameTokenProcessor() {
        tokenInfos = new HashSet<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(NameToken.class)) {
            TypeElement tokenElement = (TypeElement) e;
            NameToken nameToken = tokenElement.getAnnotation(NameToken.class);
            TokenInfo tokenInfo = new TokenInfo(nameToken.value()[0]);
            tokenInfos.add(tokenInfo);

            RequiredResources requiredResources = tokenElement.getAnnotation(RequiredResources.class);
            NoGatekeeper noGatekeeper = tokenElement.getAnnotation(NoGatekeeper.class);
            if (requiredResources != null) {
                tokenInfo.addResources(requiredResources.resources());
                tokenInfo.addOperations(requiredResources.operations());
                tokenInfo.setRecursive(requiredResources.recursive());
            } else if (noGatekeeper == null) {
                warning(e, "Proxy with token \"#%s\" is missing @%s annotation.",
                        tokenInfo.getToken(), RequiredResources.class.getSimpleName());
            }

            SearchIndex searchIndex = tokenElement.getAnnotation(SearchIndex.class);
            if (searchIndex != null) {
                tokenInfo.addKeywords(searchIndex.keywords());
                tokenInfo.setExclude(searchIndex.exclude());
                Scope scope = tokenElement.getAnnotation(Scope.class);
                if (scope != null) {
                    tokenInfo.setDomainOnly(scope.value() == Scope.Mode.DOMAIN);
                    tokenInfo.setStandaloneOnly(scope.value() == Scope.Mode.STANDALONE);
                }
            }
        }

        if (!tokenInfos.isEmpty()) {
            debug("Generating code for name token registry");
            code(NAME_TOKEN_TEMPLATE, NAME_TOKEN_PACKAGE, NAME_TOKEN_CLASS,
                    context(NAME_TOKEN_PACKAGE, NAME_TOKEN_CLASS));

            debug("Generating code for required resources registry");
            code(REQUIRED_RESOURCES_TEMPLATE, REQUIRED_RESOURCES_PACKAGE, REQUIRED_RESOURCES_CLASS,
                    context(REQUIRED_RESOURCES_PACKAGE, REQUIRED_RESOURCES_CLASS));

            debug("Generating code for search index registry");
            code(SEARCH_INDEX_TEMPLATE, SEARCH_INDEX_PACKAGE, SEARCH_INDEX_CLASS,
                    context(SEARCH_INDEX_PACKAGE, SEARCH_INDEX_CLASS));

            List<RegistryBinding> bindings = ImmutableList.of(
                    new RegistryBinding(NAME_TOKEN_PACKAGE + ".NameTokenRegistry",
                            NAME_TOKEN_PACKAGE + "." + NAME_TOKEN_CLASS),
                    new RegistryBinding(REQUIRED_RESOURCES_PACKAGE + ".RequiredResourcesRegistry",
                            REQUIRED_RESOURCES_PACKAGE + "." + REQUIRED_RESOURCES_CLASS),
                    new RegistryBinding(SEARCH_INDEX_PACKAGE + ".SearchIndexRegistry",
                            SEARCH_INDEX_PACKAGE + "." + SEARCH_INDEX_CLASS));
            debug("Generating code for registry module");
            code(REGISTRY_MODULE_TEMPLATE, REGISTRY_MODULE_PACKAGE, REGISTRY_MODULE_CLASS,
                    () -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put("packageName", REGISTRY_MODULE_PACKAGE);
                        context.put("className", REGISTRY_MODULE_CLASS);
                        context.put("bindings", bindings);
                        return context;
                    });

            info("Successfully generated name token registries [%s], [%s] and [%s] and related module [%s].",
                    NAME_TOKEN_CLASS, REQUIRED_RESOURCES_CLASS, SEARCH_INDEX_CLASS, REGISTRY_MODULE_CLASS);
            tokenInfos.clear();
        }
        return false;
    }

    private Supplier<Map<String, Object>> context(final String packageName, final String className) {
        return () -> {
            Map<String, Object> context = new HashMap<>();
            context.put("packageName", packageName);
            context.put("className", className);
            context.put("tokenInfos", tokenInfos);
            return context;
        };
    }


    public static class TokenInfo {

        private final String token;

        // required resources
        private Set<String> resources;
        private Set<String> operations;
        private boolean recursive;

        // search index
        private Set<String> keywords;
        private boolean exclude;
        private boolean domainOnly;
        private boolean standaloneOnly;

        public TokenInfo(String token) {
            this.token = token;
            this.resources = new HashSet<>();
            this.operations = new HashSet<>();
            this.recursive = false;
            this.keywords = new HashSet<>();
            this.exclude = false;
            this.domainOnly = false;
            this.standaloneOnly = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (!(o instanceof TokenInfo)) { return false; }

            TokenInfo that = (TokenInfo) o;
            return token.equals(that.token);

        }

        @Override
        public int hashCode() {
            return token.hashCode();
        }

        @Override
        public String toString() {
            return "TokenInfo{" + token + "}";
        }

        public boolean isExclude() {
            return exclude;
        }

        public void setExclude(boolean exclude) {
            this.exclude = exclude;
        }

        public Set<String> getKeywords() {
            return keywords;
        }

        public void addKeywords(String[] keywords) {
            this.keywords.addAll(asList(keywords));
        }

        public Set<String> getOperations() {
            return operations;
        }

        public void addOperations(String[] operations) {
            this.operations.addAll(asList(operations));
        }

        public boolean isRecursive() {
            return recursive;
        }

        public void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }

        public Set<String> getResources() {
            return resources;
        }

        public void addResources(String[] resources) {
            this.resources.addAll(asList(resources));
        }

        public String getToken() {
            return token;
        }

        public boolean isDomainOnly() {
            return domainOnly;
        }

        public void setDomainOnly(boolean domainOnly) {
            this.domainOnly = domainOnly;
        }

        public boolean isStandaloneOnly() {
            return standaloneOnly;
        }

        public void setStandaloneOnly(boolean standaloneOnly) {
            this.standaloneOnly = standaloneOnly;
        }
    }


    public static class RegistryBinding {

        private final String interface_;
        private final String implementation;

        public RegistryBinding(final String interface_, final String implementation) {
            this.interface_ = interface_;
            this.implementation = implementation;
        }

        public String getImplementation() {
            return implementation;
        }

        public String getInterface() {
            return interface_;
        }
    }
}
