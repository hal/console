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
package org.jboss.hal.processor.mbui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MbuiViewContext {

    private final String pkg;
    private final String base;
    private final String subclass;
    private final String createMethod;

    private final Map<String, MetadataInfo> metadataInfos;
    private final Map<String, MbuiElementInfo> elements;
    private VerticalNavigationInfo verticalNavigation;
    private final List<DataTableInfo> dataTables;
    private final List<FormInfo> forms;
    private final List<MbuiElementInfo> attachables;
    private final List<AbstractPropertyInfo> abstractProperties;
    private final List<PostConstructInfo> postConstructs;

    public MbuiViewContext(final String pkg, final String base, final String subclass, final String createMethod) {
        this.pkg = pkg;
        this.base = base;
        this.subclass = subclass;
        this.createMethod = createMethod;

        this.metadataInfos = new HashMap<>();
        this.elements = new HashMap<>();
        this.verticalNavigation = null;
        this.dataTables = new ArrayList<>();
        this.forms = new ArrayList<>();
        this.attachables = new ArrayList<>();
        this.abstractProperties = new ArrayList<>();
        this.postConstructs = new ArrayList<>();
    }

    @Override
    public String toString() {
        return pkg + "." + subclass + " extends " + base;
    }

    public String getBase() {
        return base;
    }

    public String getPackage() {
        return pkg;
    }

    public String getCreateMethod() {
        return createMethod;
    }

    public String getSubclass() {
        return subclass;
    }

    public MetadataInfo getMetadataInfo(String template) {
        return metadataInfos.get(template);
    }

    public Collection<MetadataInfo> getMetadataInfos() {
        return metadataInfos.values();
    }

    void addMetadata(String template) {
        metadataInfos.computeIfAbsent(template, MetadataInfo::new);
    }

    @SuppressWarnings("unchecked")
    <T extends MbuiElementInfo> T getElement(String selector) {
        return (T) elements.get(selector);
    }

    public VerticalNavigationInfo getVerticalNavigation() {
        return verticalNavigation;
    }

    void setVerticalNavigation(final VerticalNavigationInfo verticalNavigation) {
        this.verticalNavigation = verticalNavigation;
        this.elements.put(verticalNavigation.getSelector(), verticalNavigation);
    }

    public List<DataTableInfo> getDataTables() {
        return dataTables;
    }

    void addDataTableInfo(DataTableInfo dataTableInfo) {
        dataTables.add(dataTableInfo);
        attachables.add(dataTableInfo);
        elements.put(dataTableInfo.getSelector(), dataTableInfo);
    }

    public List<FormInfo> getForms() {
        return forms;
    }

    void addFormInfo(FormInfo formInfo) {
        forms.add(formInfo);
        attachables.add(formInfo);
        elements.put(formInfo.getSelector(), formInfo);
    }

    public List<MbuiElementInfo> getAttachables() {
        return attachables;
    }

    public List<AbstractPropertyInfo> getAbstractProperties() {
        return abstractProperties;
    }

    void addAbstractProperty(AbstractPropertyInfo abstractPropertyInfo) {
        this.abstractProperties.add(abstractPropertyInfo);
    }

    public List<PostConstructInfo> getPostConstructs() {
        return postConstructs;
    }

    void addPostConstruct(PostConstructInfo postConstructInfo) {
        postConstructs.add(postConstructInfo);
    }
}
