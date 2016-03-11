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
package org.jboss.hal.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import org.jboss.auto.AbstractProcessor;
import org.jboss.hal.spi.Requires;
import org.jboss.hal.spi.Scope;
import org.jboss.hal.spi.Keywords;

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
import static org.jboss.hal.processor.TemplateNames.*;

/**
 * Processor which scans all {@code @NameToken}s annotations and generates several registry and helper classes based
 * on the additional annotations bound to each place.
 *
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("com.gwtplatform.mvp.client.annotations.NameToken")
public class NameTokenProcessor extends AbstractProcessor {

    private static final String NAME_TOKEN_TEMPLATE = "NameTokens.ftl";
    private static final String NAME_TOKEN_PACKAGE = "org.jboss.hal.meta.token";
    private static final String NAME_TOKEN_CLASS = "NameTokensImpl";

    private static final String REQUIRED_RESOURCES_TEMPLATE = "RequiredResources.ftl";
    private static final String REQUIRED_RESOURCES_PACKAGE = "org.jboss.hal.meta.resource";
    private static final String REQUIRED_RESOURCES_CLASS = "RequiredResourcesImpl";

    private static final String SEARCH_INDEX_TEMPLATE = "SearchIndex.ftl";
    private static final String SEARCH_INDEX_PACKAGE = "org.jboss.hal.meta.search";
    private static final String SEARCH_INDEX_CLASS = "SearchIndexImpl";

    private static final String REGISTRY_MODULE_TEMPLATE = "RegistryModule.ftl";
    private static final String REGISTRY_MODULE_PACKAGE = "org.jboss.hal.meta";
    private static final String REGISTRY_MODULE_CLASS = "GeneratedRegistryModule";

    private final Set<TokenInfo> tokenInfos;

    public NameTokenProcessor() {
        super(NameTokenProcessor.class, TEMPLATES);
        tokenInfos = new HashSet<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(NameToken.class)) {
            TypeElement tokenElement = (TypeElement) e;
            NameToken nameToken = tokenElement.getAnnotation(NameToken.class);
            TokenInfo tokenInfo = new TokenInfo(nameToken.value()[0]);
            tokenInfos.add(tokenInfo);

            Requires requires = tokenElement.getAnnotation(Requires.class);
            NoGatekeeper noGatekeeper = tokenElement.getAnnotation(NoGatekeeper.class);
            if (requires != null) {
                tokenInfo.addResources(requires.value());
                tokenInfo.setRecursive(requires.recursive());
            } else if (noGatekeeper == null) {
                warning(e, "Proxy with token \"#%s\" is missing @%s annotation.",
                        tokenInfo.getToken(), Requires.class.getSimpleName());
            }

            Keywords keywords = tokenElement.getAnnotation(Keywords.class);
            if (keywords != null) {
                tokenInfo.addKeywords(keywords.value());
                tokenInfo.setExclude(keywords.exclude());
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
                    new RegistryBinding(NAME_TOKEN_PACKAGE + ".NameTokens",
                            NAME_TOKEN_PACKAGE + "." + NAME_TOKEN_CLASS),
                    new RegistryBinding(REQUIRED_RESOURCES_PACKAGE + ".RequiredResources",
                            REQUIRED_RESOURCES_PACKAGE + "." + REQUIRED_RESOURCES_CLASS),
                    new RegistryBinding(SEARCH_INDEX_PACKAGE + ".SearchIndex",
                            SEARCH_INDEX_PACKAGE + "." + SEARCH_INDEX_CLASS));
            debug("Generating code for registry module");
            code(REGISTRY_MODULE_TEMPLATE, REGISTRY_MODULE_PACKAGE, REGISTRY_MODULE_CLASS,
                    () -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put(GENERATED_WITH, NameTokenProcessor.class.getName());
                        context.put(PACKAGE_NAME, REGISTRY_MODULE_PACKAGE);
                        context.put(CLASS_NAME, REGISTRY_MODULE_CLASS);
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
            context.put(GENERATED_WITH, NameTokenProcessor.class.getName());
            context.put(PACKAGE_NAME, packageName);
            context.put(CLASS_NAME, className);
            context.put("tokenInfos", tokenInfos);
            return context;
        };
    }


    public static class TokenInfo {

        private final String token;

        // required resources
        private final Set<String> resources;
        private boolean recursive;

        // search index
        private final Set<String> keywords;
        private boolean exclude;
        private boolean domainOnly;
        private boolean standaloneOnly;

        public TokenInfo(String token) {
            this.token = token;
            this.resources = new HashSet<>();
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
