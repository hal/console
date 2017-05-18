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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jsinterop.annotations.JsConstructor;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;
import org.jboss.auto.AbstractProcessor;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.util.stream.Collectors.joining;
import static org.jboss.hal.processor.TemplateNames.GENERATED_WITH;
import static org.jboss.hal.processor.TemplateNames.TEMPLATES;

/**
 * @author Harald Pehl
 */
// Do not export this processor using @AutoService(Processor.class)
// It's executed explicitly in hal-app to process all exported js types in all maven modules.
@SupportedAnnotationTypes("jsinterop.annotations.JsType")
@SuppressWarnings({"HardCodedStringLiteral", "Guava", "ResultOfMethodCallIgnored"})
public class JsDocProcessor extends AbstractProcessor {

    private static final String AUTO = "<auto>";
    private static final String JS_TYPES = "jsTypes";
    private static final String NAMESPACE = "namespace";
    private static final String PACKAGE = "jsdoc";
    private static final String TEMPLATE = "JsDoc.ftl";

    private final Multimap<String, JsTypeInfo> types;

    public JsDocProcessor() {
        super(JsDocProcessor.class, TEMPLATES);
        types = HashMultimap.create();
    }

    @Override
    protected boolean onProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(JsType.class)) {
            JsType jsType = element.getAnnotation(JsType.class);
            if (jsType.isNative()) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            PackageElement packageElement = elementUtils.getPackageOf(typeElement);
            if (!packageElement.getQualifiedName().toString().startsWith("org.jboss.hal")) {
                continue;
            }

            JsTypeInfo typeInfo = new JsTypeInfo(namespace(packageElement, typeElement), typeName(typeElement),
                    comment(typeElement));
            types.put(typeInfo.getNamespace(), typeInfo);
            debug("Discovered JsType [%s]", typeInfo);

            final List<? extends Element> elements = typeElement.getEnclosedElements();
            Predicate<Element> jsRelevant = e -> e != null &&
                    e.getAnnotation(JsIgnore.class) == null &&
                    e.getModifiers().contains(Modifier.PUBLIC);

            // Constructor
            ElementFilter.constructorsIn(elements)
                    .stream()
                    .filter(jsRelevant.and(e -> e.getAnnotation(JsConstructor.class) != null))
                    .findFirst()
                    .ifPresent(e -> typeInfo.setConstructor(new JsConstructorInfo(parameters(e), comment(e))));

            // Properties - Fields
            ElementFilter.fieldsIn(elements)
                    .stream()
                    .filter(jsRelevant)
                    .forEach(e -> {
                        boolean setter = !e.getModifiers().contains(Modifier.FINAL);
                        typeInfo.addProperty(
                                new JsPropertyInfo(propertyName(e), comment(e), true, setter, _static(e)));
                    });

            // Properties - Methods (only getters are supported)
            ElementFilter.methodsIn(elements)
                    .stream()
                    .filter(jsRelevant.and(e -> e.getAnnotation(JsProperty.class) != null))
                    .forEach(e -> typeInfo.addProperty(
                            new JsPropertyInfo(propertyName(e), comment(e), true, false, _static(e))));

            // Methods
            ElementFilter.methodsIn(elements)
                    .stream()
                    .filter(jsRelevant.and(e -> e.getAnnotation(JsProperty.class) == null))
                    .forEach(e -> typeInfo.addMethod(
                            new JsMethodInfo(methodName(e), parameters(e), comment(e), _static(e))));
        }

        if (!types.isEmpty()) {
            types.asMap().forEach((namespace, jsTypes) ->
                    resource(TEMPLATE, PACKAGE, namespace + ".es6",
                            () -> {
                                Map<String, Object> context = new HashMap<>();
                                context.put(GENERATED_WITH, JsDocProcessor.class.getName());
                                context.put(NAMESPACE, namespace);
                                context.put(JS_TYPES, jsTypes);
                                return context;
                            }));
            types.clear();
        }
        return false;
    }

    private String namespace(PackageElement packageElement, TypeElement typeElement) {
        JsPackage jsPackage = packageElement.getAnnotation(JsPackage.class);
        JsType jsType = typeElement.getAnnotation(JsType.class);

        String namespace;
        if (jsPackage != null) {
            namespace = jsPackage.namespace();
        } else {
            namespace = AUTO.equals(jsType.namespace())
                    ? packageElement.getQualifiedName().toString()
                    : jsType.namespace();
        }
        return namespace;
    }

    private String comment(Element element) {
        String comment = elementUtils.getDocComment(element);
        return comment == null ? "Undocumented" : comment;
    }

    private String typeName(Element element) {
        JsType annotation = element.getAnnotation(JsType.class);
        if (annotation != null) {
            return AUTO.equals(annotation.name()) ? element.getSimpleName().toString() : annotation.name();
        } else {
            return element.getSimpleName().toString();
        }
    }

    private String propertyName(Element element) {
        JsProperty annotation = element.getAnnotation(JsProperty.class);
        if (annotation != null) {
            return AUTO.equals(annotation.name()) ? asProperty(element) : annotation.name();
        } else {
            return asProperty(element);
        }
    }

    private String asProperty(Element method) {
        String simpleName = method.getSimpleName().toString();
        if (simpleName.startsWith("get") || simpleName.startsWith("set")) {
            simpleName = UPPER_CAMEL.to(LOWER_CAMEL, simpleName.substring(3));
        } else if (simpleName.startsWith("is")) {
            simpleName = UPPER_CAMEL.to(LOWER_CAMEL, simpleName.substring(2));
        }
        return simpleName;
    }

    private String methodName(Element element) {
        JsMethod annotation = element.getAnnotation(JsMethod.class);
        if (annotation != null) {
            return AUTO.equals(annotation.name()) ? element.getSimpleName().toString() : annotation.name();
        } else {
            return element.getSimpleName().toString();
        }
    }

    private String parameters(ExecutableElement element) {
        return element.getParameters()
                .stream()
                .map(variable -> variable.getSimpleName().toString())
                .collect(joining(", "));

    }

    private boolean _static(Element element) {
        return element.getModifiers().contains(Modifier.STATIC);
    }


    public static class JsTypeInfo {

        private final String namespace;
        private final String name;
        private final String comment;
        private JsConstructorInfo constructor;
        private final List<JsPropertyInfo> properties;
        private final List<JsMethodInfo> methods;

        JsTypeInfo(final String namespace, final String name, final String comment) {
            this.namespace = namespace;
            this.name = name;
            this.comment = comment;
            this.properties = new ArrayList<>();
            this.methods = new ArrayList<>();
        }

        @Override
        public String toString() {
            return String.format("%s.%s", namespace, name);
        }

        void addProperty(JsPropertyInfo property) {
            properties.add(property);
        }

        void addMethod(JsMethodInfo method) {
            methods.add(method);
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public JsConstructorInfo getConstructor() {
            return constructor;
        }

        public void setConstructor(final JsConstructorInfo constructor) {
            this.constructor = constructor;
        }

        public List<JsPropertyInfo> getProperties() {
            return properties;
        }

        public List<JsMethodInfo> getMethods() {
            return methods;
        }
    }


    public static class JsConstructorInfo {

        private final String parameters;
        private final String comment;

        JsConstructorInfo(final String parameters, final String comment) {
            this.parameters = parameters;
            this.comment = comment;
        }

        @Override
        public String toString() {
            return String.format("(%s)", parameters);
        }

        public String getParameters() {
            return parameters;
        }

        public String getComment() {
            return comment;
        }
    }


    public static class JsPropertyInfo {

        private final String name;
        private final String comment;
        private final boolean getter;
        private final boolean setter;
        private final boolean _static;

        JsPropertyInfo(final String name, final String comment, final boolean getter, final boolean setter,
                final boolean _static) {
            this.name = name;
            this.comment = comment;
            this.getter = getter;
            this.setter = setter;
            this._static = _static;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getName() {
            return name;
        }

        public String getComment() {
            return comment;
        }

        public boolean isGetter() {
            return getter;
        }

        public boolean isSetter() {
            return setter;
        }

        public boolean isStatic() {
            return _static;
        }
    }


    public static class JsMethodInfo {

        private final String name;
        private final String parameters;
        private final String comment;
        private final boolean _static;

        JsMethodInfo(final String name, final String parameters, final String comment, final boolean _static) {
            this.name = name;
            this.parameters = parameters;
            this.comment = comment;
            this._static = _static;
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", name, parameters);
        }

        public String getName() {
            return name;
        }

        public String getParameters() {
            return parameters;
        }

        public String getComment() {
            return comment;
        }

        public boolean isStatic() {
            return _static;
        }
    }
}

