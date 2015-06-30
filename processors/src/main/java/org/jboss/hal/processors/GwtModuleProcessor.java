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

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import java.util.HashMap;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Generates the different GWT module descriptors for WildFly (full|dev) and EAP (full|dev). The processor is
 * triggered by the {@link org.jboss.hal.spi.Entrypoint} annotation which has to be placed on the
 * GWT entrypoint.
 *
 * @author Harald Pehl
 */
@AutoService(Processor.class)
@SupportedOptions("hal.version")
@SupportedAnnotationTypes("org.jboss.hal.spi.Entrypoint")
public class GwtModuleProcessor extends AbstractHalProcessor {

    static final String PACKAGE_NAME = "org.jboss.hal";

    @Override
    protected boolean onLastRound(RoundEnvironment roundEnv) {
        String halVersion = processingEnv.getOptions().get("hal.version");
        for (GwtModule gwtModule : GwtModule.values()) {
            resource(gwtModule.template, PACKAGE_NAME, gwtModule.module, () -> {
                Map<String, Object> context = new HashMap<>();
                context.put("halVersion", halVersion);
                return context;
            });
            info("Successfully generated GWT module [%s]", gwtModule.module);
        }
        return true;
    }

    enum GwtModule {
        BASE("Base.gwt.xml", "Base.gwt.xml.ftl"),
        EAP("EAP.gwt.xml", "EAP.gwt.xml.ftl"),
        EAP_DEV("EAPDev.gwt.xml", "EAPDev.gwt.xml.ftl"),
        WILDFLY_DEV("WildFlyDev.gwt.xml", "WildFlyDev.gwt.xml.ftl"),
        WILDFLY_SLIM("WildFly.gwt.xml", "WildFly.gwt.xml.ftl"),
        WILDFLY_FULL("WildFlyFull.gwt.xml", "WildFlyFull.gwt.xml.ftl");

        final String module;
        final String template;

        GwtModule(final String module, final String template) {
            this.module = module;
            this.template = template;
        }
    }
}
