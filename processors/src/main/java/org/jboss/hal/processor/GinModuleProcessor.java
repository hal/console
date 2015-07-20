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
package org.jboss.hal.processor;

import com.google.common.base.Joiner;
import org.jboss.auto.AbstractProcessor;
import org.jboss.hal.spi.GinModule;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Processor for GIN modules.
 *
 * @author Harald Pehl
 */
// Do not export this processor using @AutoService(Processor.class)
// It's executed explicitly in hal-app processing all GIN modules in all maven modules.
@SupportedAnnotationTypes("org.jboss.hal.spi.GinModule")
public class GinModuleProcessor extends AbstractProcessor {

    static final String MODULE_TEMPLATE = "CompositeModule.ftl";
    static final String MODULE_PACKAGE = "org.jboss.hal.client.gin";
    static final String MODULE_CLASS = "CompositeModule";

    private final Set<String> modules;

    public GinModuleProcessor() {
        super(GinModuleProcessor.class, "templates");
        modules = new HashSet<>();
    }

    @Override
    protected boolean onProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(GinModule.class)) {
            TypeElement moduleElement = (TypeElement) e;
            modules.add(moduleElement.getQualifiedName().toString());
            debug("Added %s as GIN module", moduleElement.getQualifiedName());
        }

        if (!modules.isEmpty()) {
            debug("Generating composite GIN module");
            code(MODULE_TEMPLATE, MODULE_PACKAGE, MODULE_CLASS, () -> {
                Map<String, Object> context = new HashMap<>();
                context.put("packageName", MODULE_PACKAGE);
                context.put("className", MODULE_CLASS);
                context.put("modules", modules);
                return context;
            });
            info("Successfully generated composite GIN module [%s] based on \n\t- %s.", MODULE_CLASS,
                    Joiner.on("\n\t- ").join(modules));
            modules.clear();
        }
        return false;
    }
}
