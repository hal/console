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
package org.jboss.hal.client.configuration.subsystem.elytron;

import java.util.List;
import javax.annotation.PostConstruct;

import org.jboss.hal.ballroom.VerticalNavigation;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.table.Table;
import org.jboss.hal.core.mbui.MbuiContext;
import org.jboss.hal.core.mbui.MbuiViewImpl;
import org.jboss.hal.core.mbui.ResourceElement;
import org.jboss.hal.dmr.NamedNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.spi.MbuiElement;
import org.jboss.hal.spi.MbuiView;

import static java.util.Arrays.asList;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;

/**
 * @author Claudio Miranda <claudio@redhat.com>
 */
@MbuiView
@SuppressWarnings({"DuplicateStringLiteralInspection", "HardCodedStringLiteral", "WeakerAccess"})
public class MapperDecoderView extends MbuiViewImpl<MapperDecoderPresenter>
        implements MapperDecoderPresenter.MyView {

    public static MapperDecoderView create(final MbuiContext mbuiContext) {
        return new Mbui_MapperDecoderView(mbuiContext);
    }

    // @formatter:off
    @MbuiElement("mappers-decoders-vertical-navigation") VerticalNavigation navigation;

    @MbuiElement("mappers-decoders-add-prefix-role-mapper-table") Table<NamedNode> addPrefixRoleMapperTable;
    @MbuiElement("mappers-decoders-add-prefix-role-mapper-form") Form<NamedNode> addPrefixRoleMapperForm;
    @MbuiElement("mappers-decoders-add-suffix-role-mapper-table") Table<NamedNode> addSuffixRoleMapperTable;
    @MbuiElement("mappers-decoders-add-suffix-role-mapper-form") Form<NamedNode> addSuffixRoleMapperForm;
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
    // TODO Replace 'simple-permission-mapper' with custom element
    @MbuiElement("mappers-decoders-simple-permission-mapper-table") Table<NamedNode> simplePermissionMapperTable;
    @MbuiElement("mappers-decoders-simple-permission-mapper-form") Form<NamedNode> simplePermissionMapperForm;
    @MbuiElement("mappers-decoders-simple-role-decoder-table") Table<NamedNode> simpleRoleDecoderTable;
    @MbuiElement("mappers-decoders-simple-role-decoder-form") Form<NamedNode> simpleRoleDecoderForm;
    @MbuiElement("mappers-decoders-x500-attribute-principal-decoder-table") Table<NamedNode> x500AttributePrincipalDecoderTable;
    @MbuiElement("mappers-decoders-x500-attribute-principal-decoder-form") Form<NamedNode> x500AttributePrincipalDecoderForm;
    // @formatter:on


    MapperDecoderView(final MbuiContext mbuiContext) {
        super(mbuiContext);
    }

    @PostConstruct
    void init() {
        Metadata metadata = mbuiContext.metadataRegistry().lookup(AddressTemplates.CONSTANT_PERMISSION_MAPPER_TEMPLATE);
        constantPermissionMapperElement = new ResourceElement.Builder(Ids.ELYTRON_CONSTANT_PERMISSION_MAPPER,
                CONSTANT_PERMISSION_MAPPER, metadata, mbuiContext)
                .column(NAME, (cell, type, row, meta) -> row.getName())
                .setComplexListAttribute(PERMISSIONS, asList(CLASS_NAME, MODULE), asList(CLASS_NAME, MODULE),
                        modelNode -> Ids.build(modelNode.get(CLASS_NAME).asString(), modelNode.get(MODULE).asString()))
                .onCrud(() -> presenter.reload(CONSTANT_PERMISSION_MAPPER, this::updateConstantPermissionMapper))
                .build();

        navigation.insertSecondary("mappers-decoders-permission-mapper-item",
                Ids.build(Ids.ELYTRON_CONSTANT_PERMISSION_MAPPER, Ids.ENTRY_SUFFIX),
                "mappers-decoders-custom-permission-mapper-item",
                "Constant Permission Mapper",
                constantPermissionMapperElement.asElement());

        registerAttachable(constantPermissionMapperElement);
    }

    @Override
    public void updateAddPrefixRoleMapper(final List<NamedNode> model) {
        addPrefixRoleMapperForm.clear();
        addPrefixRoleMapperTable.update(model);
    }

    @Override
    public void updateAddSuffixRoleMapper(final List<NamedNode> model) {
        addSuffixRoleMapperForm.clear();
        addSuffixRoleMapperTable.update(model);
    }

    @Override
    public void updateAggregatePrincipalDecoder(final List<NamedNode> model) {
        aggregatePrincipalDecoderForm.clear();
        aggregatePrincipalDecoderTable.update(model);
    }

    @Override
    public void updateAggregateRoleMapper(final List<NamedNode> model) {
        aggregateRoleMapperForm.clear();
        aggregateRoleMapperTable.update(model);
    }

    @Override
    public void updateConcatenatingPrincipalDecoder(final List<NamedNode> model) {
        concatenatingPrincipalDecoderForm.clear();
        concatenatingPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateConstantPermissionMapper(final List<NamedNode> model) {
        constantPermissionMapperElement.update(model);
    }

    @Override
    public void updateConstantPrincipalDecoder(final List<NamedNode> model) {
        constantPrincipalDecoderForm.clear();
        constantPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateConstantRoleMapper(final List<NamedNode> model) {
        constantRoleMapperForm.clear();
        constantRoleMapperTable.update(model);
    }

    @Override
    public void updateCustomPermissionMapper(final List<NamedNode> model) {
        customPermissionMapperForm.clear();
        customPermissionMapperTable.update(model);
    }

    @Override
    public void updateCustomPrincipalDecoder(final List<NamedNode> model) {
        customPrincipalDecoderForm.clear();
        customPrincipalDecoderTable.update(model);
    }

    @Override
    public void updateCustomRoleDecoder(final List<NamedNode> model) {
        customRoleDecoderForm.clear();
        customRoleDecoderTable.update(model);
    }

    @Override
    public void updateCustomRoleMapper(final List<NamedNode> model) {
        customRoleMapperForm.clear();
        customRoleMapperTable.update(model);
    }

    @Override
    public void updateLogicalPermissionMapper(final List<NamedNode> model) {
        logicalPermissionMapperForm.clear();
        logicalPermissionMapperTable.update(model);
    }

    @Override
    public void updateLogicalRoleMapper(final List<NamedNode> model) {
        logicalRoleMapperForm.clear();
        logicalRoleMapperTable.update(model);
    }

    @Override
    public void updateSimplePermissionMapper(final List<NamedNode> model) {
        simplePermissionMapperForm.clear();
        simplePermissionMapperTable.update(model);
    }

    @Override
    public void updateSimpleRoleDecoder(final List<NamedNode> model) {
        simpleRoleDecoderForm.clear();
        simpleRoleDecoderTable.update(model);
    }

    @Override
    public void updateX500AttributePrincipalDecoder(final List<NamedNode> model) {
        x500AttributePrincipalDecoderForm.clear();
        x500AttributePrincipalDecoderTable.update(model);
    }
}