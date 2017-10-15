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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.gwtplatform.mvp.client.annotations.NameToken;
import org.jboss.auto.AbstractProcessor;
import org.jboss.hal.spi.AsyncColumn;
import org.jboss.hal.spi.Column;
import org.jboss.hal.spi.Requires;

import static java.util.Arrays.asList;
import static org.jboss.hal.processor.TemplateNames.CLASS_NAME;
import static org.jboss.hal.processor.TemplateNames.GENERATED_WITH;
import static org.jboss.hal.processor.TemplateNames.PACKAGE_NAME;
import static org.jboss.hal.processor.TemplateNames.TEMPLATES;

/** Processor which scans all {@code @Requires} annotations and generates the {@code RequiredResources} registry. */
@AutoService(Processor.class)
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("org.jboss.hal.spi.Requires")
public class RequiredResourcesProcessor extends AbstractProcessor {

    private static final String REQUIRED_RESOURCES_TEMPLATE = "RequiredResources.ftl";
    private static final String REQUIRED_RESOURCES_PACKAGE = "org.jboss.hal.meta.resource";
    private static final String REQUIRED_RESOURCES_CLASS = "RequiredResourcesImpl";

    private static final String REGISTRY_MODULE_TEMPLATE = "RegistryModule.ftl";
    private static final String REGISTRY_MODULE_PACKAGE = "org.jboss.hal.meta";
    private static final String REGISTRY_MODULE_CLASS = "RequiredResourcesRegistryModule";

    private final Map<String, RequiredInfo> requiredInfos;

    public RequiredResourcesProcessor() {
        super(RequiredResourcesProcessor.class, TEMPLATES);
        requiredInfos = new HashMap<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(Requires.class)) {
            TypeElement requiredElement = (TypeElement) e;
            Requires requires = requiredElement.getAnnotation(Requires.class);

            // Get the related id. Currently @Requires can be placed at named proxies or (async) columns
            String id = null;
            NameToken token = requiredElement.getAnnotation(NameToken.class);
            if (token != null) {
                id = token.value()[0];
            } else {
                AsyncColumn asyncColumn = requiredElement.getAnnotation(AsyncColumn.class);
                if (asyncColumn != null) {
                    id = asyncColumn.value();
                } else {
                    Column column = requiredElement.getAnnotation(Column.class);
                    if (column != null) {
                        id = column.value();
                    }
                }
            }

            //noinspection ConstantConditions
            if (id != null) {
                RequiredInfo requiredInfo = new RequiredInfo(id, requiredElement);
                requiredInfo.addResources(requires.value());
                requiredInfo.setRecursive(requires.recursive());

                if (requiredInfos.containsKey(id)) {
                    RequiredInfo other = requiredInfos.get(id);
                    if (!requiredInfo.getResources().equals(other.getResources())) {
                        error(requiredElement,
                                "Different required resources for the same id \"%s\". This class conflicts with %s",
                                id, other.source.getQualifiedName());
                    }
                }
                requiredInfos.put(id, requiredInfo);
            }
        }

        if (!requiredInfos.isEmpty()) {
            debug("Generating code for required resources registry");
            code(REQUIRED_RESOURCES_TEMPLATE, REQUIRED_RESOURCES_PACKAGE, REQUIRED_RESOURCES_CLASS,
                    context(REQUIRED_RESOURCES_PACKAGE, REQUIRED_RESOURCES_CLASS));

            List<RegistryBinding> bindings = ImmutableList.of(
                    new RegistryBinding(REQUIRED_RESOURCES_PACKAGE + ".RequiredResources",
                            REQUIRED_RESOURCES_PACKAGE + "." + REQUIRED_RESOURCES_CLASS));
            debug("Generating code for registry module");
            code(REGISTRY_MODULE_TEMPLATE, REGISTRY_MODULE_PACKAGE, REGISTRY_MODULE_CLASS,
                    () -> {
                        Map<String, Object> context = new HashMap<>();
                        context.put(GENERATED_WITH, RequiredResourcesProcessor.class.getName());
                        context.put(PACKAGE_NAME, REGISTRY_MODULE_PACKAGE);
                        context.put(CLASS_NAME, REGISTRY_MODULE_CLASS);
                        context.put("bindings", bindings);
                        return context;
                    });

            info("Successfully generated required resources registry [%s] and related module [%s].",
                    REQUIRED_RESOURCES_CLASS, REGISTRY_MODULE_CLASS);
            requiredInfos.clear();
        }
        return false;
    }

    private Supplier<Map<String, Object>> context(final String packageName, final String className) {
        return () -> {
            Map<String, Object> context = new HashMap<>();
            context.put(GENERATED_WITH, RequiredResourcesProcessor.class.getName());
            context.put(PACKAGE_NAME, packageName);
            context.put(CLASS_NAME, className);
            context.put("requiredInfos", requiredInfos.values());
            return context;
        };
    }


    public static class RequiredInfo {

        private final String id;
        private final TypeElement source;
        private final Set<String> resources;
        private boolean recursive;

        RequiredInfo(String id, TypeElement source) {
            this.id = id;
            this.source = source;
            this.resources = new HashSet<>();
            this.recursive = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RequiredInfo)) {
                return false;
            }

            RequiredInfo that = (RequiredInfo) o;
            return id.equals(that.id);

        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return "RequiredInfo{" + id + "}";
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

        public String getId() {
            return id;
        }
    }
}
