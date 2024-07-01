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
package org.jboss.hal.core.subsystem;

import org.jboss.hal.config.StabilityLevel;

import com.google.gwt.resources.client.ExternalTextResource;

public class SubsystemMetadata {

    private final String name;
    private final String title;
    private final String subtitle;
    private final String token;
    private final String nextColumn;
    private StabilityLevel stabilityLevel;
    private final ExternalTextResource externalTextResource;
    private final boolean generic;

    private SubsystemMetadata(Builder builder) {
        this.name = builder.name;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.token = builder.token;
        this.nextColumn = builder.nextColumn;
        this.externalTextResource = builder.externalTextResource;
        this.generic = builder.generic;
    }

    @Override
    public String toString() {
        return "Subsystem(" + name + ")";
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getToken() {
        return token;
    }

    public String getNextColumn() {
        return nextColumn;
    }

    public ExternalTextResource getExternalTextResource() {
        return externalTextResource;
    }

    public boolean isGeneric() {
        return generic;
    }

    public StabilityLevel getStabilityLevel() {
        return stabilityLevel;
    }

    public void setStabilityLevel(StabilityLevel stabilityLevel) {
        this.stabilityLevel = stabilityLevel;
    }

    public static class Builder {

        private final String name;
        private final String title;
        private String subtitle;
        private String token;
        private String nextColumn;
        private ExternalTextResource externalTextResource;
        private boolean generic;

        public Builder(String name, String title) {
            this.name = name;
            this.title = title;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * Registers a named token to this finder item, showing a "View" button.
         *
         * @param token A string based token from NameTokens class.
         *
         * @return This builder instance.
         */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder nextColumn(String nextColumn) {
            this.nextColumn = nextColumn;
            return this;
        }

        public Builder preview(ExternalTextResource externalTextResource) {
            this.externalTextResource = externalTextResource;
            return this;
        }

        public Builder generic() {
            this.generic = true;
            return this;
        }

        public SubsystemMetadata build() {
            return new SubsystemMetadata(this);
        }
    }
}
