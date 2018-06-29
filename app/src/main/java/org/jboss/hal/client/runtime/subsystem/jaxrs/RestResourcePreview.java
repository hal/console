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
package org.jboss.hal.client.runtime.subsystem.jaxrs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import elemental2.core.JsRegExp;
import elemental2.dom.CSSProperties.MarginBottomUnionType;
import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.gwt.elemento.core.builder.HtmlContentBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.dialog.Dialog;
import org.jboss.hal.ballroom.form.Form;
import org.jboss.hal.ballroom.form.FormItem;
import org.jboss.hal.ballroom.form.TextBoxItem;
import org.jboss.hal.config.Environment;
import org.jboss.hal.core.finder.FinderPath;
import org.jboss.hal.core.finder.FinderPathFactory;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.mbui.form.ModelNodeForm;
import org.jboss.hal.core.mvp.Places;
import org.jboss.hal.core.runtime.server.Server;
import org.jboss.hal.core.runtime.server.ServerActions;
import org.jboss.hal.core.runtime.server.ServerUrl;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.token.NameTokens;
import org.jboss.hal.resources.CSS;
import org.jboss.hal.resources.Ids;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;
import org.jetbrains.annotations.NonNls;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromSafeConstant;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.jboss.gwt.elemento.core.Elements.*;
import static org.jboss.gwt.elemento.core.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.resources.CSS.*;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

class RestResourcePreview extends PreviewContent<RestResource> {

    private static final String LINK = "link";
    private static final JsRegExp REGEX = new JsRegExp("\\{(.+)\\}", "g"); //NON-NLS

    private final Environment environment;
    private final ServerActions serverActions;
    private final StatementContext statementContext;
    private final Resources resources;

    RestResourcePreview(RestResource restResource,
            Environment environment,
            FinderPathFactory finderPathFactory,
            Places places,
            ServerActions serverActions,
            StatementContext statementContext,
            Resources r) {
        super(abbreviateFqClassName(restResource.getName()), restResource.getPath());
        this.environment = environment;
        this.serverActions = serverActions;
        this.statementContext = statementContext;
        this.resources = r;

        getHeaderContainer().title = restResource.getName();
        getHeaderContainer().textContent = abbreviateFqClassName(restResource.getName());

        FinderPath path = finderPathFactory.deployment(restResource.getDeployment());
        PlaceRequest placeRequest = places.finderPlace(NameTokens.DEPLOYMENTS, path).build();
        Elements.removeChildrenFrom(getLeadElement());
        getLeadElement().appendChild(a(places.historyToken(placeRequest))
                .textContent(restResource.getPath())
                .title(r.messages().goTo(Names.DEPLOYMENTS))
                .asElement());
        getLeadElement().style.marginBottom = MarginBottomUnionType.of(0);


        List<ModelNode> resourcePaths = failSafeList(restResource, REST_RESOURCE_PATHS);
        if (!resourcePaths.isEmpty()) {
            previewBuilder().add(h(2, Names.RESOURCE_PATHS));
            resourcePaths.stream()
                    .collect(groupingBy(node -> node.get(RESOURCE_PATH).asString()))
                    .forEach((resourcePath, resources) -> {
                        HTMLElement body;
                        previewBuilder().add(
                                div().css(CSS.panel, panelDefault)
                                        .add(div().css(panelHeading)
                                                .add(h(3, resourcePath).css(panelTitle)))
                                        .add(body = div().css(panelBody, restResources).asElement()));
                        for (Iterator<ModelNode> iterator = resources.iterator(); iterator.hasNext(); ) {
                            ModelNode resource = iterator.next();
                            if (resource.hasDefined(CONSUMES)) {
                                appendMediaTypes(body, resource, CONSUMES, "&rarr;");
                            }
                            if (resource.hasDefined(PRODUCES)) {
                                appendMediaTypes(body, resource, PRODUCES, "&larr;");
                            }
                            if (resource.hasDefined(RESOURCE_METHODS)) {
                                List<String> resourceMethods = failSafeList(resource, RESOURCE_METHODS).stream()
                                        .map(ModelNode::asString)
                                        .collect(toList());
                                HTMLElement p;
                                body.appendChild(p = p().asElement());
                                for (Iterator<String> rmIterator = resourceMethods.iterator(); rmIterator.hasNext(); ) {
                                    String resourceMethod = rmIterator.next();
                                    if (resourceMethod.contains(" ")) {
                                        List<String> parts = Splitter.on(' ').limit(2).splitToList(resourceMethod);
                                        if (parts.size() == 2) {
                                            String method = parts.get(0);
                                            String url = parts.get(1);
                                            p.appendChild(strong().textContent(method).asElement());
                                            HtmlContentBuilder<HTMLElement> builder = span().css(marginLeft5)
                                                    .textContent(url);
                                            if (GET.name().equalsIgnoreCase(method)) {
                                                builder.data(LINK, String.join(",", extractParams(url)));
                                            }
                                            p.appendChild(builder.asElement());
                                        } else {
                                            p.appendChild(document.createTextNode(resourceMethod));
                                        }
                                    } else {
                                        p.appendChild(document.createTextNode(resourceMethod));
                                    }
                                    if (rmIterator.hasNext()) {
                                        p.appendChild(br().asElement());
                                    }
                                }
                            }
                            if (resource.hasDefined(JAVA_METHOD)) {
                                body.appendChild(pre().css(prettyPrint, langJava)
                                        .style("white-space:pre-wrap") //NON-NLS
                                        .textContent(resource.get(JAVA_METHOD).asString())
                                        .asElement());
                            }
                            if (iterator.hasNext()) {
                                body.appendChild(hr().asElement());
                            }
                        }
                    });
        }

        List<ModelNode> subResourceLocators = failSafeList(restResource, SUB_RESOURCE_LOCATORS);
        if (!subResourceLocators.isEmpty()) {
            previewBuilder().add(h(2, Names.SUB_RESOURCE_LOCATORS));
            // TODO Process sub resource locators
        }
    }

    @SuppressWarnings("HardCodedStringLiteral")
    private void appendMediaTypes(HTMLElement body, ModelNode resource, String type, @NonNls String arrow) {
        String consumes = failSafeList(resource, type).stream()
                .map(ModelNode::asString)
                .collect(joining(", "));
        body.appendChild(p()
                .add(span().title(type)
                        .style("cursor:help")
                        .innerHtml(fromSafeConstant(arrow)))
                .add(" " + consumes)
                .asElement());
    }

    private List<String> extractParams(String url) {
        String[] match;
        List<String> params = new ArrayList<>();
        do {
            match = REGEX.exec(url);
            if (match != null) {
                params.add(match[1]);
            }
        } while (match != null);
        return params;
    }

    @Override
    public void attach() {
        super.attach();
        PatternFly.prettyPrint();
        List<HTMLElement> linkContainers = new ArrayList<>();
        asElements().forEach(e -> {
            List<HTMLElement> elements = stream(e.querySelectorAll("[data-" + LINK + "]")) //NON-NLS
                    .filter(htmlElements())
                    .map(asHtmlElement())
                    .collect(toList());
            linkContainers.addAll(elements);
        });

        if (!linkContainers.isEmpty()) {
            String host = environment.isStandalone() ? Server.STANDALONE.getHost() : statementContext.selectedHost();
            String serverGroup = statementContext.selectedServerGroup();
            String server = environment.isStandalone() ? Server.STANDALONE.getName() : statementContext.selectedServer();
            serverActions.readUrl(environment.isStandalone(), host, serverGroup, server,
                    new AsyncCallback<ServerUrl>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            // noop
                        }

                        @Override
                        public void onSuccess(ServerUrl url) {
                            for (HTMLElement linkContainer : linkContainers) {
                                String link = linkContainer.textContent;
                                String params = linkContainer.dataset.get(LINK);
                                if (Strings.isNullOrEmpty(params)) {
                                    Elements.removeChildrenFrom(linkContainer);
                                    linkContainer.appendChild(a(url.getUrl() + link)
                                            .apply(a -> a.target = serverId())
                                            .textContent(link)
                                            .asElement());

                                } else {
                                    Elements.removeChildrenFrom(linkContainer);
                                    linkContainer.appendChild(a().css(clickable)
                                            .on(click, e -> specifyParameters(url.getUrl(), link, Splitter.on(',')
                                                    .splitToList(linkContainer.dataset.get(LINK))))
                                            .textContent(link)
                                            .asElement());
                                }
                            }
                        }
                    });
        }
    }

    private void specifyParameters(String serverUrl, String link, List<String> params) {
        ModelNodeForm.Builder<ModelNode> builder = new ModelNodeForm.Builder<>(Ids.REST_RESOURCE_PATH_PARAM_FORM,
                Metadata.empty())
                .addOnly()
                .onSave((form, changedValues) -> {
                    String withValues = link;
                    for (String param : params) {
                        String value = form.<String>getFormItem(param).getValue();
                        withValues = withValues.replace("{" + param + "}", value);
                    }
                    window.open(serverUrl + withValues, serverId());
                });
        int i = 0;
        for (String param : params) {
            FormItem<String> formItem = new TextBoxItem(param, param);
            formItem.setRequired(true);
            builder.unboundFormItem(formItem, i);
            i++;
        }
        Form<ModelNode> form = builder.build();
        Dialog dialog = new Dialog.Builder(resources.constants().specifyParameters())
                .add(p().innerHtml(resources.messages().specifyParameters(link)).asElement())
                .add(form.asElement())
                .primary(resources.constants().ok(), form::save)
                .cancel()
                .closeOnEsc(true)
                .closeIcon(true)
                .build();
        dialog.registerAttachable(form);
        dialog.show();
        form.edit(new ModelNode());
    }

    private String serverId() {
        return environment.isStandalone()
                ? Ids.hostServer(Server.STANDALONE.getHost(), Server.STANDALONE.getName())
                : Ids.hostServer(statementContext.selectedHost(), statementContext.selectedServer());
    }
}
