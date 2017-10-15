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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
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
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.processor.TemplateNames;
import org.jboss.hal.processor.TypeSimplifier;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import static org.jboss.hal.processor.mbui.ElementType.Form;
import static org.jboss.hal.processor.mbui.ElementType.Table;
import static org.jboss.hal.processor.mbui.ElementType.VerticalNavigation;
import static org.jboss.hal.processor.mbui.XmlHelper.xmlAsString;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.jboss.hal.spi.MbuiView")
public class MbuiViewProcessor extends AbstractProcessor {

    public static final String GET = "get";
    public static final String IS = "is";
    private static final String MBUI_PREFIX = "Mbui_";
    private static final String SLASH_SLASH = "//";

    /**
     * Method to reset the various counters to generate unique variables names. Used to simplify unit testing - do not
     * use in production code!
     */
    static void resetCounter() {
        Content.counter = 0;
        MetadataInfo.counter = 0;
    }

    private static final String TEMPLATE = "MbuiView.ftl";

    private XPathFactory xPathFactory;

    public MbuiViewProcessor() {
        this(MbuiViewProcessor.class, TemplateNames.TEMPLATES);
    }

    protected MbuiViewProcessor(final Class resourceLoaderClass, final String templates) {
        super(resourceLoaderClass, templates);
    }

    @Override
    protected boolean onProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(MbuiView.class)) {
            TypeElement type = (TypeElement) e;
            MbuiView mbuiView = type.getAnnotation(MbuiView.class);
            validateType(type, mbuiView);
            processType(type, mbuiView);
        }
        return false;
    }


    // ------------------------------------------------------ general validation

    @SuppressWarnings("HardCodedStringLiteral")
    private void validateType(final TypeElement type, final MbuiView mbuiView) {
        if (mbuiView == null) {
            // This shouldn't happen unless the compilation environment is buggy,
            // but it has happened in the past and can crash the compiler.
            error(type, "Annotation processor for @%s was invoked with a type that does not have that " +
                    "annotation; this is probably a compiler bug", MbuiView.class.getSimpleName());
        }
        if (type.getKind() != ElementKind.CLASS) {
            error(type, "@%s only applies to classes", MbuiView.class.getSimpleName());
        }
        if (!isAssignable(type, MbuiViewImpl.class)) {
            error(type, "Missing base class %s", MbuiViewImpl.class.getSimpleName());
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

    protected void processType(final TypeElement type, final MbuiView mbuiView) {
        String subclass = TypeSimplifier.simpleNameOf(generatedClassName(type, "")); //NON-NLS
        String createMethod = verifyCreateMethod(type);
        MbuiViewContext context = new MbuiViewContext(TypeSimplifier.packageNameOf(type),
                TypeSimplifier.classNameOf(type), subclass, createMethod);

        // order is important! do not rearrange unless you know what you're doing!

        // parse and validate the MBUI XML
        xPathFactory = XPathFactory.instance();
        Document document = parseXml(type, mbuiView);
        validateDocument(type, document);

        // first process the metadata
        processMetadata(type, document, context);
        // processTabs(document, context);

        // then find and verify all @MbuiElement members
        processMbuiElements(type, document, context);
        processRoot(document, context);
        processCrossReferences(document, context);

        // init parameters and abstract properties
        processAbstractProperties(type, context);

        // find and verify all @PostConstruct methods
        processPostConstruct(type, context);

        // generate code
        code(TEMPLATE, context.getPackage(), context.getSubclass(),
                () -> ImmutableMap.of("context", context)); //NON-NLS
        info("Generated MBUI view implementation [%s] for [%s]", context.getSubclass(), context.getBase()); //NON-NLS
    }

    String generatedClassName(TypeElement type, String suffix) {
        String name = type.getSimpleName().toString();
        while (type.getEnclosingElement() instanceof TypeElement) {
            type = (TypeElement) type.getEnclosingElement();
            name = type.getSimpleName() + "_" + name;
        }
        String pkg = TypeSimplifier.packageNameOf(type);
        String dot = pkg.isEmpty() ? "" : ".";
        return pkg + dot + MBUI_PREFIX + name + suffix;
    }

    String verifyCreateMethod(TypeElement type) {
        Optional<ExecutableElement> createMethod = ElementFilter.methodsIn(type.getEnclosedElements())
                .stream()
                .filter(method -> method.getModifiers().contains(Modifier.STATIC) &&
                        method.getReturnType().equals(type.asType()))
                .findAny();
        if (createMethod.isPresent()) {
            return createMethod.get().getSimpleName().toString();
        } else {
            error(type, "@%s needs to define one static method which returns an %s instance", //NON-NLS
                    MbuiView.class.getSimpleName(), type.getSimpleName());
            return null;
        }
    }


    // ------------------------------------------------------ XML processing

    @SuppressWarnings("HardCodedStringLiteral")
    private Document parseXml(final TypeElement type, final MbuiView mbuiView) {
        String mbuiXml = Strings.isNullOrEmpty(mbuiView.value())
                ? type.getSimpleName().toString() + ".mbui.xml"
                : mbuiView.value();
        String fq = TypeSimplifier.packageNameOf(type).replace('.', '/') + File.separator + mbuiXml;

        try {
            FileObject file = processingEnv.getFiler().getResource(StandardLocation.CLASS_PATH, "", fq);
            return new SAXBuilder().build(file.openReader(true));

        } catch (IOException e) {
            error(type, "Cannot find MBUI XML \"%s\". " +
                    "Please make sure the file exists and resides in the source path.", fq);
        } catch (JDOMException e) {
            error(type, "Cannot parse MBUI XML \"%s\". Please verify that the file contains valid XML.", fq);
        }
        return null;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void validateDocument(final TypeElement type, final Document document) {
        // verify root element
        org.jdom2.Element root = document.getRootElement();
        if (!root.getName().equals(XmlTags.VIEW)) {
            error(type, "Invalid root element in MBUI XML. Allowed: \"%s\", found: \"%s\".", XmlTags.VIEW,
                    root.getName());
        }

        // verify first child
        List<org.jdom2.Element> children = root.getChildren();
        if (children.isEmpty()) {
            error(type, "No children found in MBUI XML.");
        } else if (children.size() > 1) {
            error(type, "Only one child allowed in MBUI XML.");
        }
        org.jdom2.Element child = children.get(0);
        if (!(child.getName().equals(XmlTags.VERTICAL_NAVIGATION) || child.getName().equals(XmlTags.METADATA))) {
            error(type, "Invalid child of root element in MBUI XML. Allowed: \"%s\" or \"%s\", found: \"%s\".",
                    XmlTags.VERTICAL_NAVIGATION, XmlTags.METADATA, child.getName());
        }
    }

    /**
     * Lookup all //metadata elements, verify the address attribute and store them in the context.
     */
    private void processMetadata(final TypeElement type, final Document document, final MbuiViewContext context) {
        XPathExpression<org.jdom2.Element> expression = xPathFactory
                .compile(SLASH_SLASH + XmlTags.METADATA, Filters.element());
        for (org.jdom2.Element element : expression.evaluate(document)) {
            String template = element.getAttributeValue(XmlTags.ADDRESS);
            if (template == null) {
                error(type, "Missing address attribute in metadata element \"%s\"", xmlAsString(element)); //NON-NLS
            } else {
                context.addMetadata(template);
            }
        }
    }


    // ------------------------------------------------------ process @MbuiElement

    @SuppressWarnings("HardCodedStringLiteral")
    private void processMbuiElements(final TypeElement type, final Document document, final MbuiViewContext context) {
        ElementFilter.fieldsIn(type.getEnclosedElements()).stream()
                .filter(field -> MoreElements.isAnnotationPresent(field, MbuiElement.class))
                .forEach(field -> {

                    // verify the field
                    if (field.getModifiers().contains(Modifier.PRIVATE)) {
                        error(field, "@%s member must not be private", MbuiElement.class.getSimpleName());
                    }
                    if (field.getModifiers().contains(Modifier.STATIC)) {
                        error(field, "@%s member must not be static", MbuiElement.class.getSimpleName());
                    }

                    // verify the selector
                    String selector = getSelector(field);
                    org.jdom2.Element element = verifySelector(selector, field, document);

                    // delegate to specific processors based on element type
                    ElementType elementType = getMbuiElementType(field.asType());
                    if (elementType == null) {
                        error(field, "Unsupported type %s. Please choose one of %s", field.asType(),
                                EnumSet.allOf(ElementType.class));
                    } else {
                        MbuiElementProcessor elementProcessor = null;
                        switch (elementType) {
                            case VerticalNavigation:
                                elementProcessor = new VerticalNavigationProcessor(this, typeUtils, elementUtils,
                                        xPathFactory);
                                break;
                            case Table:
                                elementProcessor = new DataTableProcessor(this, typeUtils, elementUtils, xPathFactory);
                                break;
                            case Form:
                                elementProcessor = new FormProcessor(this, typeUtils, elementUtils, xPathFactory);
                                break;
                            default:
                                break;
                        }
                        elementProcessor.process(field, element, selector, context);
                    }
                });
    }

    private String getSelector(Element element) {
        String selector = null;

        //noinspection Guava
        com.google.common.base.Optional<AnnotationMirror> annotationMirror = MoreElements
                .getAnnotationMirror(element, MbuiElement.class);
        if (annotationMirror.isPresent()) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = elementUtils
                    .getElementValuesWithDefaults(annotationMirror.get());
            if (!values.isEmpty()) {
                selector = String.valueOf(values.values().iterator().next().getValue());
            }
        }
        return Strings.emptyToNull(selector) == null ? element.getSimpleName().toString() : selector;
    }

    private org.jdom2.Element verifySelector(String selector, Element element, Document document) {
        XPathExpression<org.jdom2.Element> expression = xPathFactory
                .compile("//*[@" + XmlTags.ID + "='" + selector + "']", Filters.element());
        List<org.jdom2.Element> elements = expression.evaluate(document);
        if (elements.isEmpty()) {
            error(element,
                    "Cannot find a matching element in the MBUI XML with id \"%s\".", selector); //NON-NLS
        } else if (elements.size() > 1) {
            error(element,
                    "Found %d matching elements in the MBUI XML with id \"%s\". Id must be unique.", //NON-NLS
                    elements.size(), selector);
        }
        return elements.get(0);
    }

    private ElementType getMbuiElementType(TypeMirror dataElementType) {
        if (isAssignable(dataElementType, VerticalNavigation.class)) {
            return VerticalNavigation;
        } else if (isAssignable(dataElementType, Table.class)) {
            return Table;
        } else if (isAssignable(dataElementType, Form.class)) {
            return Form;
        } else {
            return null;
        }
    }


    // ------------------------------------------------------ process root

    private void processRoot(final Document document, final MbuiViewContext context) {
        // if the root is not a vertical navigation we need to parse its content.
        if (!XmlTags.VERTICAL_NAVIGATION.equals(document.getRootElement().getName())) {
            Content.parse(document.getRootElement(), context).forEach(context::addContent);
        }
    }


    // ------------------------------------------------------ process references

    private void processCrossReferences(final Document document, final MbuiViewContext context) {
        // table-form bindings
        XPathExpression<org.jdom2.Element> expression = xPathFactory
                .compile(SLASH_SLASH + XmlTags.TABLE + "[@" + XmlTags.FORM_REF + "]", Filters.element());
        for (org.jdom2.Element element : expression.evaluate(document)) {
            DataTableInfo tableInfo = context.getElement(element.getAttributeValue(XmlTags.ID));
            FormInfo formInfo = context.getElement(element.getAttributeValue(XmlTags.FORM_REF));
            if (tableInfo != null && formInfo != null) {
                tableInfo.setFormRef(formInfo);
            }
        }

        // content references
        VerticalNavigationInfo navigation = context.getVerticalNavigation();
        if (navigation == null) {
            for (MbuiElementInfo elementInfo : context.getElements()) {
                Content reference = context.findContent(elementInfo.getSelector());
                if (reference != null) {
                    reference.setReference(elementInfo.getName());
                }
            }
        } else {
            resolveItemReferences(navigation, SLASH_SLASH + XmlTags.ITEM + SLASH_SLASH + XmlTags.TABLE, document,
                    context);
            resolveItemReferences(navigation, SLASH_SLASH + XmlTags.ITEM + SLASH_SLASH + XmlTags.FORM, document,
                    context);
            resolveItemReferences(navigation, SLASH_SLASH + XmlTags.ITEM + SLASH_SLASH + XmlTags.SINGLETON_FORM,
                    document, context);
        }
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    private void resolveItemReferences(final VerticalNavigationInfo navigation, final String xpath,
            final Document document, final MbuiViewContext context) {

        XPathExpression<org.jdom2.Element> expression = xPathFactory.compile(xpath, Filters.element());
        for (org.jdom2.Element element : expression.evaluate(document)) {
            String id = element.getAttributeValue(XmlTags.ID);
            MbuiElementInfo elementInfo = context.getElement(id);
            if (elementInfo != null) {
                // find parent (sub)item
                XPathExpression<org.jdom2.Element> parentItemExpression = xPathFactory
                        .compile("ancestor::" + XmlTags.SUB_ITEM, //NON-NLS
                                Filters.element());
                org.jdom2.Element parentItemElement = parentItemExpression.evaluateFirst(element);
                if (parentItemElement == null) {
                    parentItemExpression = xPathFactory
                            .compile("ancestor::" + XmlTags.ITEM, Filters.element()); //NON-NLS
                    parentItemElement = parentItemExpression.evaluateFirst(element);
                }
                VerticalNavigationInfo.Item parentItem = navigation.getItem(parentItemElement.getAttributeValue("id"));
                Content reference = parentItem.findContent(id);
                if (reference != null) {
                    reference.setReference(elementInfo.getName());
                }
            }
        }
    }


    // ------------------------------------------------------ abstract properties

    void processAbstractProperties(final TypeElement type, final MbuiViewContext context) {
        ElementFilter.methodsIn(type.getEnclosedElements()).stream()
                .filter(method -> method.getModifiers().contains(Modifier.ABSTRACT))
                .forEach(method -> {

                    // verify method
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        error(method, "Abstract propertiers in a @%s class must not return void", //NON-NLS
                                MbuiView.class.getSimpleName());
                    }
                    if (!method.getParameters().isEmpty()) {
                        error(method, "Abstract properties in a @%s class must not have parameters", //NON-NLS
                                MbuiView.class.getSimpleName());
                    }

                    String typeName = TypeSimplifier.simpleTypeName(method.getReturnType());
                    String methodName = method.getSimpleName().toString();
                    String fieldName = (isGetter(method)) ? nameWithoutPrefix(methodName) : methodName;
                    String modifier = getModifier(method);
                    context.addAbstractProperty(new AbstractPropertyInfo(typeName, fieldName, methodName, modifier));
                });
    }

    private boolean isGetter(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        boolean get = name.startsWith(GET) && !name.equals(GET);
        boolean is = name.startsWith(IS) && !name.equals(IS)
                && method.getReturnType().getKind() == TypeKind.BOOLEAN;
        return get || is;
    }

    private String nameWithoutPrefix(String name) {
        String withoutPrefix;
        if (name.startsWith(GET) && !name.equals(GET)) {
            withoutPrefix = name.substring(3);
        } else {
            assert name.startsWith(IS);
            withoutPrefix = name.substring(2);
        }
        return Introspector.decapitalize(withoutPrefix);
    }

    private String getModifier(final ExecutableElement method) {
        String modifier = null;
        Set<Modifier> modifiers = method.getModifiers();
        if (modifiers.contains(Modifier.PUBLIC)) {
            modifier = "public"; //NON-NLS
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            modifier = "protected"; //NON-NLS
        }
        return modifier;
    }


    // ------------------------------------------------------ process @PostConstruct

    @SuppressWarnings("HardCodedStringLiteral")
    private void processPostConstruct(TypeElement type, final MbuiViewContext context) {
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

                    context.addPostConstruct(new PostConstructInfo(method.getSimpleName().toString()));
                });

        if (context.getPostConstructs().size() > 1) {
            warning(type, "%d methods annotated with @%s found. Order is not guaranteed!",
                    context.getPostConstructs().size(), PostConstruct.class.getSimpleName());
        }
    }
}
