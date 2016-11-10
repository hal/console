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

    // the root element is either a vertical navigation or a list of content (mix of HTML, tabs, forms and/or tables)
    private VerticalNavigationInfo verticalNavigation;
    private final List<Content> content;

    private final Map<String, MetadataInfo> metadataInfos;
    private final Map<String, MbuiElementInfo> elements;
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

        this.verticalNavigation = null;
        this.content = new ArrayList<>();
        this.metadataInfos = new HashMap<>();
        this.elements = new HashMap<>();
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

    public VerticalNavigationInfo getVerticalNavigation() {
        return verticalNavigation;
    }

    void setVerticalNavigation(final VerticalNavigationInfo verticalNavigation) {
        this.verticalNavigation = verticalNavigation;
        this.elements.put(verticalNavigation.getSelector(), verticalNavigation);
        attachables.add(verticalNavigation);
    }

    public List<Content> getContent() {
        return content;
    }

    void addContent(Content content) {
        this.content.add(content);
    }

    Content findContent(final String id) {
        for (Content c : content) {
            if (id.equals(c.getReference())) {
                return c;
            }
        }
        return null;
    }

    MetadataInfo getMetadataInfo(String address) {
        return metadataInfos.get(address);
    }

    public Collection<MetadataInfo> getMetadataInfos() {
        return metadataInfos.values();
    }

    void addMetadata(String address) {
        metadataInfos.computeIfAbsent(address, MetadataInfo::new);
    }

    @SuppressWarnings("unchecked")
    <T extends MbuiElementInfo> T getElement(String selector) {
        return (T) elements.get(selector);
    }

    Collection<MbuiElementInfo> getElements() {
        return elements.values();
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
    
    public String findFormById(String id) {
        String found = null;
        for (FormInfo form: forms) {
            if (form.getSelector().equals(id)) {
                found = form.getName();
                break;
            }
        }
        return found;
    }
}
