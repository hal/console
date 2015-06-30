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
package org.jboss.hal.generator;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A code generator which generates code / resources from freemarker templates.
 *
 * @author Harald Pehl
 */
public class CodeGenerator {

    private final Configuration config;
    private BufferedWriter bw;

    public CodeGenerator(final Class resourceLoaderClass, final String templates) {
        Version version = new Version(2, 3, 22);
        config = new Configuration(version);
        config.setDefaultEncoding("UTF-8");
        config.setClassForTemplateLoading(resourceLoaderClass, templates);
        config.setObjectWrapper(new DefaultObjectWrapperBuilder(version).build());
    }

    /**
     * Generates the code using the specified context and freemarker template. Wraps any kind of error inside a
     * {@code GenerationException}.
     *
     * @param template the relative template name
     * @param context  a supplier function to create the templates' context
     *
     * @return the generated content
     */
    public StringBuffer generate(String template, Supplier<Map<String, Object>> context) {
        final StringWriter sw = new StringWriter();
        final BufferedWriter bw = new BufferedWriter(sw);
        try {
            final Template t = config.getTemplate(template);
            t.process(context.get(), bw);
        } catch (IOException | TemplateException e) {
            throw new GenerationException("Error generating template " + template + ": " + e.getMessage(), e);
        } finally {
            try {
                bw.close();
                sw.close();
            } catch (IOException ioe) {
                //noinspection ThrowFromFinallyBlock
                throw new GenerationException("Error generating template " + template + ": " + ioe.getMessage(), ioe);
            }
        }
        return sw.getBuffer();
    }
}
