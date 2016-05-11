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

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import org.jboss.hal.processor.TemplateNames;
import org.jboss.hal.processor.TypeSimplifier;
import org.jboss.hal.spi.MbuiView;

/**
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("org.jboss.hal.spi.MbuiView")
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
public class MbuiViewGinProcessor extends MbuiViewProcessor {

    private static final String TEMPLATE = "MbuiViewProvider.ftl";

    public MbuiViewGinProcessor() {
        super(MbuiViewGinProcessor.class, TemplateNames.TEMPLATES);
    }

    @Override
    protected void processType(final TypeElement type, final MbuiView mbuiView) {
        String subclass = TypeSimplifier.simpleNameOf(generatedClassName(type, "Mbui_", "_Provider"));
        String createMethod = verifyCreateMethod(type);
        MbuiViewContext context = new MbuiViewContext(TypeSimplifier.packageNameOf(type),
                TypeSimplifier.classNameOf(type), subclass, createMethod);

        processAbstractProperties(type, context);

        code(TEMPLATE, context.getPackage(), context.getSubclass(),
                () -> ImmutableMap.of("context", context));
        info("Generated MBUI view provider [%s] for [%s]", context.getSubclass(), context.getBase());
    }
}
