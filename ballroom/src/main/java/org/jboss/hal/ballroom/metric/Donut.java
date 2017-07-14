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
package org.jboss.hal.ballroom.metric;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.IsElement;

import static org.jboss.gwt.elemento.core.Elements.div;

public class Donut implements IsElement<HTMLElement> {

    public enum Size {
        MEDIUM(new int[]{200, 171}, new int[]{251, 161}, new int[]{271, 191});

        private final int[] noLegend;
        private final int[] rightLegend;
        private final int[] bottomLegend;

        Size(int[] noLegend, int[] rightLegend, int[] bottomLegend) {
            this.noLegend = noLegend;
            this.rightLegend = rightLegend;
            this.bottomLegend = bottomLegend;
        }
    }

    public enum Legend {
        NONE, LEFT, BOTTOM
    }


    public static class Builder {

        private final String unit;
        private Legend legend;
        private Size size;

        public Builder(String unit) {
            this.unit = unit;
            this.legend = Legend.NONE;
            this.size = Size.MEDIUM;
        }

        public Builder add(String text, String color) {
            return this;
        }

        public Builder size(Size size) {
            this.size = size;
            return this;
        }

        public Builder legend(Legend legend) {
            this.legend = legend;
            return this;
        }

        public Donut build() {
            return new Donut(this);
        }
    }


    private final HTMLElement root;

    private Donut(Builder builder) {
        this.root = div().asElement();
    }

    @Override
    public HTMLElement asElement() {
        return root;
    }
}
