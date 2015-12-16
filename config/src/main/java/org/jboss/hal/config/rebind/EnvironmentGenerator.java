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
package org.jboss.hal.config.rebind;

import com.google.common.base.Strings;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.i18n.rebind.LocaleUtils;
import com.google.gwt.i18n.shared.GwtLocale;
import org.jboss.auto.CodeGenerator;
import org.jboss.hal.config.Environment;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.hal.config.rebind.GeneratorUtils.failSafeGetProperty;

public class EnvironmentGenerator extends Generator {

    private static final String PRODUCT_INFO_TEMPLATE = "Environment.ftl";
    private static final String PRODUCT_INFO_PACKAGE = Environment.class.getPackage().getName();
    private static final String PRODUCT_INFO_CLASS = Environment.class.getSimpleName() + "Impl";

    private final CodeGenerator generator;

    public EnvironmentGenerator() {
        generator = new CodeGenerator(EnvironmentGenerator.class, "templates");
    }

    @Override
    public String generate(TreeLogger logger, GeneratorContext generatorContext, String typeName)
            throws UnableToCompleteException {
        try {
            // Generate class source code
            generateProductInfo(logger, generatorContext);
        } catch (Throwable e) {
            logger.log(TreeLogger.ERROR, "Failed to generate " + PRODUCT_INFO_CLASS, e);
        }
        // return the fully qualified name of the class generated
        return PRODUCT_INFO_PACKAGE + "." + PRODUCT_INFO_CLASS;
    }

    private void generateProductInfo(final TreeLogger logger, final GeneratorContext generatorContext) {
        // get print writer that receives the source code
        PrintWriter printWriter = generatorContext.tryCreate(logger, PRODUCT_INFO_PACKAGE, PRODUCT_INFO_CLASS);

        // print writer if null, source code has ALREADY been generated, return
        if (printWriter == null) { return; }

        String halVersion = failSafeGetProperty(generatorContext.getPropertyOracle(), "hal.version", "n/a");
        LocaleUtils localeUtils = LocaleUtils.getInstance(logger, generatorContext.getPropertyOracle(), generatorContext);
        Set<GwtLocale> locales = localeUtils.getAllCompileLocales();
        List<String> localeValues = locales.stream()
                .map(GwtLocale::getAsString)
                .filter(locale -> !Strings.isNullOrEmpty(locale))
                .collect(Collectors.toList());

        StringBuffer code = generator.generate(PRODUCT_INFO_TEMPLATE, () -> {
            Map<String, Object> context = new HashMap<>();
            context.put("packageName", PRODUCT_INFO_PACKAGE);
            context.put("className", PRODUCT_INFO_CLASS);
            context.put("halVersion", halVersion);
            context.put("locales", localeValues);
            return context;
        });
        printWriter.write(code.toString());
        generatorContext.commit(logger, printWriter);
    }
}
