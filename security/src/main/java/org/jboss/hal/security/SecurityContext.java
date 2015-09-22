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
package org.jboss.hal.security;

/**
 * @author Harald Pehl
 */
public interface SecurityContext {

    SecurityContext READ_ONLY = new SecurityContext() {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean isReadable(final String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(final String attribute) {
            return false;
        }

        @Override
        public boolean isExecutable(final String operation) {
            return false;
        }
    };

    SecurityContext RWX = new SecurityContext() {
        @Override
        public boolean isReadable() {
            return true;
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isReadable(final String attribute) {
            return true;
        }

        @Override
        public boolean isWritable(final String attribute) {
            return true;
        }

        @Override
        public boolean isExecutable(final String operation) {
            return true;
        }
    };


    boolean isReadable();

    boolean isWritable();

    boolean isReadable(final String attribute);

    boolean isWritable(final String attribute);

    boolean isExecutable(String operation);
}
