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

import java.beans.Introspector;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.jboss.auto.AbstractProcessor;
import org.jboss.hal.processor.TemplateNames;
import org.jboss.hal.processor.TypeSimplifier;
import org.jboss.hal.spi.GinModule;
import org.jboss.hal.spi.MbuiView;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("org.jboss.hal.spi.MbuiView")
public class MbuiViewProcessor extends AbstractProcessor {

    static final String MBUI_VIEW_TEMPLATE = "MbuiView.ftl";

    public MbuiViewProcessor() {
        super(MbuiViewProcessor.class, TemplateNames.TEMPLATES);
    }

    @Override
    protected boolean onProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(GinModule.class)) {
            TypeElement type = (TypeElement) e;
            MbuiView mbuiView = type.getAnnotation(MbuiView.class);
            validateType(type, mbuiView);
            processType(type, mbuiView);
        }
        return false;
    }


    // ------------------------------------------------------ validation

    private void validateType(final TypeElement type, final MbuiView templated) {
        if (templated == null) {
            // This shouldn't happen unless the compilation environment is buggy,
            // but it has happened in the past and can crash the compiler.
            error(type, "Annotation processor for @%s was invoked with a type that does not have that " +
                    "annotation; this is probably a compiler bug", MbuiView.class.getSimpleName());
        }
        if (type.getKind() != ElementKind.CLASS) {
            error(type, "@%s only applies to classes", MbuiView.class.getSimpleName());
        }
        if (ancestorIsMbuiView(type)) {
            error(type, "One @%s class may not extend another", MbuiView.class.getSimpleName());
        }
        if (isAssignable(type, Annotation.class)) {
            error(type, "@%s may not be used to implement an annotation interface", MbuiView.class.getSimpleName());
        }
    }

    private boolean ancestorIsMbuiView(TypeElement type) {
        while (true) {
            TypeMirror parentMirror = type.getSuperclass();
            if (parentMirror.getKind() == TypeKind.NONE) {
                return false;
            }
            TypeElement parentElement = (TypeElement) typeUtils.asElement(parentMirror);
            if (parentElement.getAnnotation(MbuiView.class) != null) {
                return true;
            }
            type = parentElement;
        }
    }

    private boolean isAssignable(TypeElement subType, Class<?> baseType) {
        return isAssignable(subType.asType(), baseType);
    }

    private boolean isAssignable(TypeMirror subType, Class<?> baseType) {
        return isAssignable(subType, getTypeMirror(baseType));
    }

    private boolean isAssignable(TypeMirror subType, TypeMirror baseType) {
        return typeUtils.isAssignable(typeUtils.erasure(subType), typeUtils.erasure(baseType));
    }

    private TypeMirror getTypeMirror(Class<?> c) {
        return processingEnv.getElementUtils().getTypeElement(c.getName()).asType();
    }


    // ------------------------------------------------------ processing

    private void processType(final TypeElement type, final MbuiView mbuiView) {
        String subclass = TypeSimplifier.simpleNameOf(generatedClassName(type, "Templated_", ""));
        String createMethod = verifyCreateMethod(type);
        MbuiViewContext context = new MbuiViewContext(TypeSimplifier.packageNameOf(type),
                TypeSimplifier.classNameOf(type), subclass, createMethod);

        // parse the mbui xml
        Document document = parseXml(type, mbuiView);

        // find and verify all @MbuiElement members
        List<String> mbuiElements = processMbuiElements(type, document);
        context.setMbuiElements(mbuiElements);

        // find and verify all @PostConstruct methods
        List<PostConstructInfo> postConstructs = processPostConstruct(type);
        context.setPostConstructs(postConstructs);

        // init parameters and abstract properties
        List<AbstractPropertyInfo> abstractProperties = processAbstractProperties(type);
        context.setAbstractProperties(abstractProperties);

        // generate code
        code(MBUI_VIEW_TEMPLATE, context.getPackage(), context.getSubclass(),
                () -> ImmutableMap.of("context", context));
        info("Generated MBUI view implementation [%s] for [%s]", context.getSubclass(), context.getBase());
    }

    private String generatedClassName(TypeElement type, String prefix, String suffix) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = TypeSimplifier.packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return pkg + dot + prefix + name + suffix;
    }

    private String verifyCreateMethod(TypeElement type) {
        Optional<ExecutableElement> createMethod = ElementFilter.methodsIn(type.getEnclosedElements())
                .stream()
                .filter(method -> method.getModifiers().contains(Modifier.STATIC) &&
                        method.getReturnType().equals(type.asType()))
                .findAny();
        if (createMethod.isPresent()) {
            return createMethod.get().getSimpleName().toString();
        } else {
            error(type, "@%s needs to define one static method which returns an %s instance",
                    MbuiView.class.getSimpleName(), type.getSimpleName());
            return null;
        }
    }

    private Document parseXml(final TypeElement type, final MbuiView mbuiView) {
        String mbuiXml = Strings.isNullOrEmpty(mbuiView.value())
                ? type.getSimpleName().toString() + ".xml"
                : mbuiView.value();
        String fq = TypeSimplifier.packageNameOf(type).replace('.', '/') + File.pathSeparator + mbuiXml;

        try {
            FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", fq);
            return new SAXBuilder().build(file.openReader(true));
        } catch (IOException e) {
            error(type, "Cannot find MBUI xml \"%s\". " +
                    "Please make sure the file exists and resides in the source path.", fq);
        } catch (JDOMException e) {
            error(type, "Cannot parse MBUI xml \"%s\". Please verify that the file contains valid XML.", fq);
        }
        return null;
    }

    private List<String> processMbuiElements(final TypeElement type, final Document document) {
        return null;
    }

    private List<PostConstructInfo> processPostConstruct(TypeElement type) {
        List<PostConstructInfo> postConstructs = new ArrayList<>();

        ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .filter(method -> MoreElements.isAnnotationPresent(method, PostConstruct.class))
                .forEach(method -> {

                    // verify method
                    if (method.getModifiers().contains(Modifier.PRIVATE)) {
                        error(method, "@%s method must not be private", PostConstruct.class.getSimpleName());
                    }
                    if (method.getModifiers().contains(Modifier.STATIC)) {
                        error(method, "@%s method must not be static", PostConstruct.class.getSimpleName());
                    }
                    if (!method.getReturnType().equals(typeUtils.getNoType(TypeKind.VOID))) {
                        error(method, "@%s method must return void", PostConstruct.class.getSimpleName());
                    }
                    if (!method.getParameters().isEmpty()) {
                        error(method, "@%s method must not have parameters",
                                PostConstruct.class.getSimpleName());
                    }

                    postConstructs.add(new PostConstructInfo(method.getSimpleName().toString()));
                });

        if (postConstructs.size() > 1) {
            warning(type, "%d methods annotated with @%s found. Order is not guaranteed!", postConstructs.size(),
                    PostConstruct.class.getSimpleName());
        }
        return postConstructs;
    }

    private List<AbstractPropertyInfo> processAbstractProperties(final TypeElement type) {
        List<AbstractPropertyInfo> abstractProperties = new ArrayList<>();

        ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .filter(method -> method.getModifiers().contains(Modifier.ABSTRACT))
                .forEach(method -> {

                    // verify method
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        error(method, "Abstract propertiers in a @%s class must not return void",
                                MbuiView.class.getSimpleName());
                    }
                    if (!method.getParameters().isEmpty()) {
                        error(method, "Abstract properties in a @%s class must not have parameters",
                                MbuiView.class.getSimpleName());
                    }

                    String typeName = TypeSimplifier.simpleTypeName(method.getReturnType());
                    String methodName = method.getSimpleName().toString();
                    String fieldName = (isGetter(method)) ? nameWithoutPrefix(methodName) : methodName;
                    String modifier = getModifier(method);
                    abstractProperties.add(new AbstractPropertyInfo(typeName, fieldName, methodName, modifier));
                });

        return abstractProperties;
    }

    private boolean isGetter(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        boolean get = name.startsWith("get") && !name.equals("get");
        boolean is = name.startsWith("is") && !name.equals("is")
                && method.getReturnType().getKind() == TypeKind.BOOLEAN;
        return get || is;
    }

    private String nameWithoutPrefix(String name) {
        String withoutPrefix;
        if (name.startsWith("get") && !name.equals("get")) {
            withoutPrefix = name.substring(3);
        } else {
            assert name.startsWith("is");
            withoutPrefix = name.substring(2);
        }
        return Introspector.decapitalize(withoutPrefix);
    }

    private String getModifier(final ExecutableElement method) {
        String modifier = null;
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.PUBLIC)) {
            modifier = "public";
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            modifier = "protected";
        }
        return modifier;
    }
}
