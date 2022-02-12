/*
 *  Copyright 2022 Red Hat
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jboss.hal.config.rebind;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.auto.CodeGenerator;
import org.jboss.hal.config.Environment;

import com.google.common.base.Strings;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.i18n.rebind.LocaleUtils;
import com.google.gwt.i18n.shared.GwtLocale;

import static org.jboss.hal.config.rebind.GeneratorUtils.failSafeGetProperty;

@SuppressWarnings({ "HardCodedStringLiteral", "DuplicateStringLiteralInspection" })
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
        if (printWriter == null) {
            return;
        }

        String halVersion = failSafeGetProperty(generatorContext.getPropertyOracle(), "hal.version", "0.0.0");
        String halBuild = failSafeGetProperty(generatorContext.getPropertyOracle(), "hal.build", null);
        LocaleUtils localeUtils = LocaleUtils.getInstance(logger, generatorContext.getPropertyOracle(),
                generatorContext);
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
            context.put("halBuild", halBuild);
            context.put("locales", localeValues);
            return context;
        });
        printWriter.write(code.toString());
        generatorContext.commit(logger, printWriter);
    }
}
