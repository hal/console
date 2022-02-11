/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.jboss.auto.AbstractProcessor;
import org.jboss.hal.spi.GinModule;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;

import static org.jboss.hal.processor.TemplateNames.CLASS_NAME;
import static org.jboss.hal.processor.TemplateNames.GENERATED_WITH;
import static org.jboss.hal.processor.TemplateNames.GIN_PACKAGE;
import static org.jboss.hal.processor.TemplateNames.PACKAGE_NAME;

/** Processor for GIN modules. */
@AutoService(Processor.class)
@SuppressWarnings("HardCodedStringLiteral")
@SupportedAnnotationTypes("org.jboss.hal.spi.GinModule")
public class GinModuleProcessor extends AbstractProcessor {

    private static final String MODULE_TEMPLATE = "CompositeModule.ftl";
    private static final String MODULE_PACKAGE = GIN_PACKAGE;
    private static final String MODULE_CLASS = "CompositeModule";

    private final Set<String> modules;

    public GinModuleProcessor() {
        super(GinModuleProcessor.class, TemplateNames.TEMPLATES);
        modules = new HashSet<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> roundModules = roundEnv.getElementsAnnotatedWith(GinModule.class);
        for (Element e : roundModules) {
            TypeElement moduleElement = (TypeElement) e;
            modules.add(moduleElement.getQualifiedName().toString());
            debug("Added %s as GIN module", moduleElement.getQualifiedName());
        }

        // TODO Don't rely on a specific round, but find other ways to run this processor last.
        if (round() == 1 && !modules.isEmpty()) {
            debug("Generating composite GIN module");
            code(MODULE_TEMPLATE, MODULE_PACKAGE, MODULE_CLASS, () -> {
                Map<String, Object> context = new HashMap<>();
                context.put(GENERATED_WITH, GinModuleProcessor.class.getName());
                context.put(PACKAGE_NAME, MODULE_PACKAGE);
                context.put(CLASS_NAME, MODULE_CLASS);
                context.put("modules", modules);
                return context;
            });

            info("Successfully generated composite GIN module [%s] based on \n\t%s.", MODULE_CLASS,
                    Joiner.on("\n\t").join(modules));
            modules.clear();
        }
        return false;
    }
}
