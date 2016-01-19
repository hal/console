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
package org.jboss.hal.ballroom.tageditor;

import com.google.gwt.core.client.GWT;
import jsinterop.annotations.JsType;
import org.jboss.hal.resources.Constants;

import static jsinterop.annotations.JsPackage.GLOBAL;
import static org.jboss.hal.resources.Names.OBJECT;

/**
 * @author Harald Pehl
 */
@JsType(isNative = true)
public class TagEditor {

    @JsType(isNative = true, namespace = GLOBAL, name = OBJECT)
    public static class Options {

        public String delimiter;
        public boolean forceLowercase;
        public String placeholder;
        public boolean sortable;
    }


    // Helper class to get hold of the default options,
    // since native JS types can neither hold static references nor initializer
    public static class Defaults {

        private static final Options DEFAULT_OPTIONS = new Options();
        private static final Constants CONSTANTS = GWT.create(Constants.class);

        static {
            DEFAULT_OPTIONS.delimiter = ", ";
            DEFAULT_OPTIONS.forceLowercase = false;
            DEFAULT_OPTIONS.placeholder = CONSTANTS.tagEditorPlaceholder();
            DEFAULT_OPTIONS.sortable = false;
        }

        public static Options get() {
            return DEFAULT_OPTIONS;
        }
    }
}
