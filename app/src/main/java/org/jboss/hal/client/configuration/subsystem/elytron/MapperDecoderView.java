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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;

import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.ResourceElement;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mbui.table.ModelNodeTable;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import elemental2.dom.HTMLElement;

import static java.util.Arrays.asList;

import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.section;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.CONSTANT_PERMISSION_MAPPER_TEMPLATE;
import static org.jboss.hal.client.configuration.subsystem.elytron.AddressTemplates.MAPPED_ROLE_MAPPER_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS_NAME;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSTANT_PERMISSION_MAPPER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.FROM;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MAPPED_ROLE_MAPPER;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PERMISSIONS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.ROLE_MAP;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TABLE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.TO;
import static org.jboss.hal.resources.Ids.FORM;
import static org.jboss.hal.resources.Ids.ITEM;
import static org.jboss.hal.resources.Ids.build;

@MbuiView
@SuppressWarnings({ "DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess" })
public class MapperDecoderView extends MbuiViewImpl<MapperDecoderPresenter>
        implements MapperDecoderPresenter.MyView {

    public static MapperDecoderView create(MbuiContext mbuiContext) {
        return new Mbui_MapperDecoderView(mbuiContext);
    }

    // @formatter:off
    @MbuiElement("mappers-decoders-vertical-navigation") VerticalNavigation navigation;

    @MbuiElement("mappers-decoders-add-prefix-role-mapper-table") Table<NamedNode> addPrefixRoleMapperTable;
    @MbuiElement("mappers-decoders-add-prefix-role-mapper-form") Form<NamedNode> addPrefixRoleMapperForm;
    @MbuiElement("mappers-decoders-add-suffix-role-mapper-table") Table<NamedNode> addSuffixRoleMapperTable;
    @MbuiElement("mappers-decoders-add-suffix-role-mapper-form") Form<NamedNode> addSuffixRoleMapperForm;
    @MbuiElement("mappers-decoders-aggregate-evidence-decoder-table") Table<NamedNode> aggregateEvidenceDecoderTable;
    @MbuiElement("mappers-decoders-aggregate-evidence-decoder-form") Form<NamedNode> aggregateEvidenceDecoderForm;
    @MbuiElement("mappers-decoders-aggregate-principal-decoder-table") Table<NamedNode> aggregatePrincipalDecoderTable;
    @MbuiElement("mappers-decoders-aggregate-principal-decoder-form") Form<NamedNode> aggregatePrincipalDecoderForm;
    @MbuiElement("mappers-decoders-aggregate-role-mapper-table") Table<NamedNode> aggregateRoleMapperTable;
    @MbuiElement("mappers-decoders-aggregate-role-mapper-form") Form<NamedNode> aggregateRoleMapperForm;
    @MbuiElement("mappers-decoders-concatenating-principal-decoder-table") Table<NamedNode> concatenatingPrincipalDecoderTable;
    @MbuiElement("mappers-decoders-concatenating-principal-decoder-form") Form<NamedNode> concatenatingPrincipalDecoderForm;
    ResourceElement constantPermissionMapperElement;
    @MbuiElement("mappers-decoders-constant-principal-decoder-table") Table<NamedNode> constantPrincipalDecoderTable;
    @MbuiElement("mappers-decoders-constant-principal-decoder-form") Form<NamedNode> constantPrincipalDecoderForm;
    @MbuiElement("mappers-decoders-constant-role-mapper-table") Table<NamedNode> constantRoleMapperTable;
    @MbuiElement("mappers-decoders-constant-role-mapper-form") Form<NamedNode> constantRoleMapperForm;
    @MbuiElement("mappers-decoders-custom-permission-mapper-table") Table<NamedNode> customPermissionMapperTable;
    @MbuiElement("mappers-decoders-custom-permission-mapper-form") Form<NamedNode> customPermissionMapperForm;
    @MbuiElement("mappers-decoders-custom-evidence-decoder-table") Table<NamedNode> customEvidenceDecoderTable;
    @MbuiElement("mappers-decoders-custom-evidence-decoder-form") Form<NamedNode> customEvidenceDecoderForm;
    @MbuiElement("mappers-decoders-custom-principal-decoder-table") Table<NamedNode> customPrincipalDecoderTable;
    @MbuiElement("mappers-decoders-custom-principal-decoder-form") Form<NamedNode> customPrincipalDecoderForm;
    @MbuiElement("mappers-decoders-custom-role-decoder-table") Table<NamedNode> customRoleDecoderTable;
    @MbuiElement("mappers-decoders-custom-role-decoder-form") Form<NamedNode> customRoleDecoderForm;
    @MbuiElement("mappers-decoders-custom-role-mapper-table") Table<NamedNode> customRoleMapperTable;
    @MbuiElement("mappers-decoders-custom-role-mapper-form") Form<NamedNode> customRoleMapperForm;
    @MbuiElement("mappers-decoders-logical-permission-mapper-table") Table<NamedNode> logicalPermissionMapperTable;
    @MbuiElement("mappers-decoders-logical-permission-mapper-form") Form<NamedNode> logicalPermissionMapperForm;
    @MbuiElement("mappers-decoders-logical-role-mapper-table") Table<NamedNode> logicalRoleMapperTable;
    @MbuiElement("mappers-decoders-logical-role-mapper-form") Form<NamedNode> logicalRoleMapperForm;
    private Table<NamedNode> mappedRoleMapperTable;
    private Form<NamedNode> mappedRoleMapperForm;
    private SimplePermissionMapperElement simplePermissionMapperElement;
    @MbuiElement("mappers-decoders-simple-role-decoder-table") Table<NamedNode> simpleRoleDecoderTable;
    @MbuiElement("mappers-decoders-simple-role-decoder-form") Form<NamedNode> simpleRoleDecoderForm;
    @MbuiElement("mappers-decoders-x500-attribute-principal-decoder-table") Table<NamedNode> x500AttributePrincipalDecoderTable;
    @MbuiElement("mappers-decoders-x500-attribute-principal-decoder-form") Form<NamedNode> x500AttributePrincipalDecoderForm;
    @MbuiElement("mappers-decoders-x500-subject-evidence-decoder-table") Table<NamedNode> x500SubjectEvidenceDecoderTable;
    @MbuiElement("mappers-decoders-x509-subject-alt-name-evidence-decoder-table") Table<NamedNode> x509SubjectAltNameEvidenceDecoderTable;
    @MbuiElement("mappers-decoders-x509-subject-alt-name-evidence-decoder-form") Form<NamedNode> x509SubjectAltNameEvidenceDecoderForm;
    // @formatter:on

    MapperDecoderView(MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(CONSTANT_PERMISSION_MAPPER_TEMPLATE);
        constantPermissionMapperElement = new ResourceElement.Builder(Ids.ELYTRON_CONSTANT_PERMISSION_MAPPER,
                CONSTANT_PERMISSION_MAPPER, metadata, mbuiContext)
                .nameColumn()
                .setComplexListAttribute(PERMISSIONS, asList(CLASS_NAME, MODULE), asList(CLASS_NAME, MODULE),
                        modelNode -> build(modelNode.get(CLASS_NAME).asString(), modelNode.get(MODULE).asString()))
                .onCrud(() -> presenter.reload(CONSTANT_PERMISSION_MAPPER, this::updateConstantPermissionMapper))
                .build();

        navigation.insertSecondary("mappers-decoders-permission-mapper-item",
                build(Ids.ELYTRON_CONSTANT_PERMISSION_MAPPER, ITEM),
                "mappers-decoders-custom-permission-mapper-item",
                "Constant Permission Mapper",
                constantPermissionMapperElement.element());

        registerAttachable(constantPermissionMapperElement);

        // =========
        String mappedId = "mappers-decoders-mapped-role-mapper";
        Metadata mappedMetadata = mbuiContext.metadataRegistry().lookup(MAPPED_ROLE_MAPPER_TEMPLATE);
        LabelBuilder labelBuilder = new LabelBuilder();
        String title = labelBuilder.label(MAPPED_ROLE_MAPPER);
        mappedRoleMapperTable = new ModelNodeTable.Builder<NamedNode>(build(mappedId, TABLE), mappedMetadata)
                .button(mbuiContext.tableButtonFactory().add(MAPPED_ROLE_MAPPER_TEMPLATE,
                        table -> presenter.addMappedRoleMapper()))
                .button(mbuiContext.tableButtonFactory().remove(title, MAPPED_ROLE_MAPPER_TEMPLATE,
                        table -> table.selectedRow().getName(),
                        () -> presenter.reload()))
                .nameColumn()
                .build();

        mappedRoleMapperForm = new ModelNodeForm.Builder<NamedNode>(build(mappedId, FORM), mappedMetadata)
                .customFormItem(ROLE_MAP, (desc) -> new MultiValueListItem(ROLE_MAP, FROM, TO))
                // .customFormItem(ROLE_MAP, desc -> new RoleMapListItem(ROLE_MAP, labelBuilder.label(ROLE_MAP)))
                .onSave((form, changedValues) -> {
                    String name = form.getModel().getName();
                    ResourceAddress address = MAPPED_ROLE_MAPPER_TEMPLATE.resolve(mbuiContext.statementContext(), name);
                    saveForm(title, name, address, changedValues, mappedMetadata);
                })
                .build();

        HTMLElement mappedSection = section()
                .add(h(1).textContent(Names.MAPPED_ROLE_MAPPER))
                .add(p().textContent(mappedMetadata.getDescription().getDescription()))
                .add(mappedRoleMapperTable)
                .add(mappedRoleMapperForm).element();

        registerAttachable(mappedRoleMapperTable, mappedRoleMapperForm);

        navigation.insertSecondary("mappers-decoders-role-mappers", build(mappedId, ITEM), null, title, mappedSection);

        // =========

        Metadata spmMetadata = mbuiContext.metadataRegistry()
                .lookup(AddressTemplates.SIMPLE_PERMISSION_MAPPER_TEMPLATE);
        simplePermissionMapperElement = new SimplePermissionMapperElement(spmMetadata,
                mbuiContext.tableButtonFactory());

        navigation.insertSecondary("mappers-decoders-permission-mapper-item",
                build(Ids.ELYTRON_SIMPLE_PERMISSION_MAPPER, ITEM),
                "mappers-decoders-simple-permission-mapper-item",
                "Simple Permission Mapper",
                simplePermissionMapperElement.element());

        registerAttachable(simplePermissionMapperElement);
    }

    @Override
    public void attach() {
        super.attach();
        mappedRoleMapperTable.bindForm(mappedRoleMapperForm);
    }

    @Override
    public void updateAddPrefixRoleMapper(List<NamedNode> model) {
        addPrefixRoleMapperForm.clear();
        addPrefixRoleMapperTable.update(model);
    }

    @Override
    public void updateAddSuffixRoleMapper(List<NamedNode> model) {
        addSuffixRoleMapperForm.clear();
        addSuffixRoleMapperTable.update(model);
    }

    @Override
    public void updateAggregateEvidenceDecoder(List<NamedNode> model) {
        aggregateEvidenceDecoderForm.clear();
        aggregateEvidenceDecoderTable.update(model);
    }

    @Override
    public void updateAggregatePrincipalDecoder(List<NamedNode> model) {
        aggregatePrincipalDecoderForm.clear();
        aggregatePrincipalDecoderTable.update(model);
    }

    @Override
    public void updateAggregateRoleMapper(List<NamedNode> model) {
        aggregateRoleMapperForm.clear();
        aggregateRoleMapperTable.update(model);
    }

    @Override
    public void updateConcatenatingPrincipalDecoder(List<NamedNode> model) {
        concatenatingPrincipalDecoderForm.clear();
        concatenatingPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateConstantPermissionMapper(List<NamedNode> model) {
        constantPermissionMapperElement.update(model);
    }

    @Override
    public void updateConstantPrincipalDecoder(List<NamedNode> model) {
        constantPrincipalDecoderForm.clear();
        constantPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateConstantRoleMapper(List<NamedNode> model) {
        constantRoleMapperForm.clear();
        constantRoleMapperTable.update(model);
    }

    @Override
    public void updateCustomEvidenceDecoder(List<NamedNode> model) {
        customEvidenceDecoderForm.clear();
        customEvidenceDecoderTable.update(model);
    }

    @Override
    public void updateCustomPermissionMapper(List<NamedNode> model) {
        customPermissionMapperForm.clear();
        customPermissionMapperTable.update(model);
    }

    @Override
    public void updateCustomPrincipalDecoder(List<NamedNode> model) {
        customPrincipalDecoderForm.clear();
        customPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateCustomRoleDecoder(List<NamedNode> model) {
        customRoleDecoderForm.clear();
        customRoleDecoderTable.update(model);
    }

    @Override
    public void updateCustomRoleMapper(List<NamedNode> model) {
        customRoleMapperForm.clear();
        customRoleMapperTable.update(model);
    }

    @Override
    public void updateLogicalPermissionMapper(List<NamedNode> model) {
        logicalPermissionMapperForm.clear();
        logicalPermissionMapperTable.update(model);
    }

    @Override
    public void updateLogicalRoleMapper(List<NamedNode> model) {
        logicalRoleMapperForm.clear();
        logicalRoleMapperTable.update(model);
    }

    @Override
    public void updateMappedRoleMapper(List<NamedNode> model) {
        mappedRoleMapperForm.clear();
        mappedRoleMapperTable.update(model);
    }

    @Override
    public void updateSimplePermissionMapper(List<NamedNode> model) {
        simplePermissionMapperElement.update(model);
    }

    @Override
    public void updateSimpleRoleDecoder(List<NamedNode> model) {
        simpleRoleDecoderForm.clear();
        simpleRoleDecoderTable.update(model);
    }

    @Override
    public void updateX500AttributePrincipalDecoder(List<NamedNode> model) {
        x500AttributePrincipalDecoderForm.clear();
        x500AttributePrincipalDecoderTable.update(model);
    }

    @Override
    public void updateX500SubjectEvidenceDecoder(List<NamedNode> model) {
        x500SubjectEvidenceDecoderTable.update(model);
    }

    @Override
    public void updateX509SubjectAltNameEvidenceDecoder(List<NamedNode> model) {
        x509SubjectAltNameEvidenceDecoderForm.clear();
        x509SubjectAltNameEvidenceDecoderTable.update(model);
    }

    @Override
    public void setPresenter(MapperDecoderPresenter presenter) {
        super.setPresenter(presenter);
        simplePermissionMapperElement.setPresenter(presenter);
    }
}
