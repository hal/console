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
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import com.google.auto.common.MoreElements;
import com.google.common.base.Optional;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.jboss.auto.AbstractProcessor;

import static java.util.stream.Collectors.joining;
import static org.jboss.hal.processor.TemplateNames.GENERATED_WITH;
import static org.jboss.hal.processor.TemplateNames.TEMPLATES;

/**
 * @author Harald Pehl
 */
// Do not export this processor using @AutoService(Processor.class)
// It's executed explicitly in hal-app to process all exported js types in all maven modules.
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("jsinterop.annotations.JsType")
public class JsDocProcessor extends AbstractProcessor {

    private static final String AUTO = "<auto>";
    private static final String TEMPLATE = "JsDoc.ftl";
    private static final String PACKAGE = "jsdoc";
    private static final String JS_TYPE = "jsType";


    public JsDocProcessor() {
        super(JsDocProcessor.class, TEMPLATES);
    }

    @Override
    protected boolean onProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(JsType.class)) {
            JsType annotation = element.getAnnotation(JsType.class);
            if (annotation.isNative()) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            PackageElement packageElement = elementUtils.getPackageOf(typeElement);
            if (!packageElement.getQualifiedName().toString().startsWith("org.jboss.hal")) {
                continue;
            }

            String namespace = namespace(packageElement, typeElement);
            String typeName = name(typeElement);
            String typeComment = elementUtils.getDocComment(typeElement);
            JsTypeInfo jsType = new JsTypeInfo(namespace, typeName, typeComment);
            debug("Discovered JsType [%s.%s]", namespace, typeName);

            ElementFilter.methodsIn(typeElement.getEnclosedElements())
                    .stream()
                    .filter(method -> method.getAnnotation(JsIgnore.class) == null &&
                            method.getModifiers().contains(Modifier.PUBLIC))
                    .forEach(method -> {
                        // custom namespace on @JsMethod is not yet supported!
                        // TODO Parse @JsProperty annotations
                        AnnotationValue annotationValue = extractAnnotationValue(method, "name");
                        String name = annotationValue == null
                                ? method.getSimpleName().toString()
                                : String.valueOf(annotationValue.getValue());
                        String parameters = method.getParameters().stream()
                                .map(parameter -> parameter.getSimpleName().toString())
                                .collect(joining(", "));
                        String methodComment = elementUtils.getDocComment(method);

                        jsType.addMethod(new JsMethodInfo(name, parameters, methodComment));
                        debug("Added JsMethod [%s(%s)]", name, parameters);
                    });

            resource(TEMPLATE, PACKAGE, namespace + "." + typeName + ".js", () -> {
                Map<String, Object> context = new HashMap<>();
                context.put(GENERATED_WITH, JsDocProcessor.class.getName());
                context.put(JS_TYPE, jsType);
                return context;
            });
        }
        return false;
    }

    private String namespace(final PackageElement packageElement, final TypeElement typeElement) {
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

    private String name(final TypeElement typeElement) {
        JsType jsType = typeElement.getAnnotation(JsType.class);
        return AUTO.equals(jsType.name()) ? typeElement.getSimpleName().toString() : jsType.name();
    }

    private AnnotationValue extractAnnotationValue(Element element, String name) {
        //noinspection Guava
        Optional<AnnotationMirror> am = MoreElements.getAnnotationMirror(element, JsMethod.class);
        if (am.isPresent()) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = elementUtils.getElementValuesWithDefaults(
                    am.get());
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : values.entrySet()) {
                if (entry.getKey().getSimpleName().contentEquals(name)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }


    public static class JsTypeInfo {

        private final String namespace;
        private final String name;
        private final String comment;
        private final List<JsMethodInfo> methods;

        JsTypeInfo(final String namespace, final String name, final String comment) {
            this.namespace = namespace;
            this.name = name;
            this.comment = comment;
            this.methods = new ArrayList<>();
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

        public List<JsMethodInfo> getMethods() {
            return methods;
        }
    }


    public static class JsMethodInfo {

        private final String name;
        private final String parameters;
        private final String comment;

        JsMethodInfo(final String name, final String parameters, final String comment) {
            this.name = name;
            this.parameters = parameters;
            this.comment = comment;
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
    }
}

