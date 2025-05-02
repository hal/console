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
package org.jboss.hal.client.runtime.subsystem.jaxrs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jboss.elemento.Elements;
import org.jboss.elemento.HtmlContentBuilder;
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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import elemental2.core.JsRegExp;
import elemental2.core.RegExpResult;
import elemental2.dom.CSSProperties.MarginBottomUnionType;
import elemental2.dom.HTMLElement;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static com.google.gwt.safehtml.shared.SafeHtmlUtils.fromSafeConstant;
import static elemental2.dom.DomGlobal.document;
import static elemental2.dom.DomGlobal.window;
import static org.jboss.elemento.Elements.a;
import static org.jboss.elemento.Elements.asHtmlElement;
import static org.jboss.elemento.Elements.br;
import static org.jboss.elemento.Elements.div;
import static org.jboss.elemento.Elements.h;
import static org.jboss.elemento.Elements.hr;
import static org.jboss.elemento.Elements.htmlElements;
import static org.jboss.elemento.Elements.p;
import static org.jboss.elemento.Elements.pre;
import static org.jboss.elemento.Elements.span;
import static org.jboss.elemento.Elements.stream;
import static org.jboss.elemento.Elements.strong;
import static org.jboss.elemento.EventType.click;
import static org.jboss.hal.dmr.ModelDescriptionConstants.CONSUMES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.JAVA_METHOD;
import static org.jboss.hal.dmr.ModelDescriptionConstants.PRODUCES;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_METHODS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.RESOURCE_PATH;
import static org.jboss.hal.dmr.ModelDescriptionConstants.REST_RESOURCE_PATHS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.SUB_RESOURCE_LOCATORS;
import static org.jboss.hal.dmr.ModelNodeHelper.failSafeList;
import static org.jboss.hal.dmr.dispatch.Dispatcher.HttpMethod.GET;
import static org.jboss.hal.resources.CSS.clickable;
import static org.jboss.hal.resources.CSS.langJava;
import static org.jboss.hal.resources.CSS.marginLeft5;
import static org.jboss.hal.resources.CSS.panelBody;
import static org.jboss.hal.resources.CSS.panelDefault;
import static org.jboss.hal.resources.CSS.panelHeading;
import static org.jboss.hal.resources.CSS.panelTitle;
import static org.jboss.hal.resources.CSS.prettyPrint;
import static org.jboss.hal.resources.CSS.restResources;
import static org.jboss.hal.resources.Strings.abbreviateFqClassName;

class RestResourcePreview extends PreviewContent<RestResource> {

    private static final String LINK = "link";
    private static final JsRegExp REGEX = new JsRegExp("\\{(.+)\\}", "g"); // NON-NLS

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
                .title(r.messages().goTo(Names.DEPLOYMENTS)).element());
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
                                        .add(body = div().css(panelBody, restResources).element()));
                        for (Iterator<ModelNode> iterator = resources.iterator(); iterator.hasNext();) {
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
                                body.appendChild(p = p().element());
                                for (Iterator<String> rmIterator = resourceMethods.iterator(); rmIterator.hasNext();) {
                                    String resourceMethod = rmIterator.next();
                                    if (resourceMethod.contains(" ")) {
                                        List<String> parts = Splitter.on(' ').limit(2).splitToList(resourceMethod);
                                        if (parts.size() == 2) {
                                            String method = parts.get(0);
                                            String url = parts.get(1);
                                            p.appendChild(strong().textContent(method).element());
                                            HtmlContentBuilder<HTMLElement> builder = span().css(marginLeft5)
                                                    .textContent(url);
                                            if (GET.name().equalsIgnoreCase(method)) {
                                                builder.data(LINK, String.join(",", extractParams(url)));
                                            }
                                            p.appendChild(builder.element());
                                        } else {
                                            p.appendChild(document.createTextNode(resourceMethod));
                                        }
                                    } else {
                                        p.appendChild(document.createTextNode(resourceMethod));
                                    }
                                    if (rmIterator.hasNext()) {
                                        p.appendChild(br().element());
                                    }
                                }
                            }
                            if (resource.hasDefined(JAVA_METHOD)) {
                                body.appendChild(pre().css(prettyPrint, langJava)
                                        .style("white-space:pre-wrap") // NON-NLS
                                        .textContent(resource.get(JAVA_METHOD).asString()).element());
                            }
                            if (iterator.hasNext()) {
                                body.appendChild(hr().element());
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
    private void appendMediaTypes(HTMLElement body, ModelNode resource, String type, String arrow) {
        String consumes = failSafeList(resource, type).stream()
                .map(ModelNode::asString)
                .collect(joining(", "));
        body.appendChild(p()
                .add(span().title(type)
                        .style("cursor:help")
                        .innerHtml(fromSafeConstant(arrow)))
                .add(" " + consumes).element());
    }

    private List<String> extractParams(String url) {
        RegExpResult match;
        List<String> params = new ArrayList<>();
        do {
            match = REGEX.exec(url);
            if (match != null) {
                params.add(match.getAt(1));
            }
        } while (match != null);
        return params;
    }

    @Override
    public void attach() {
        super.attach();
        PatternFly.prettyPrint();
        List<HTMLElement> linkContainers = new ArrayList<>();
        forEach(e -> {
            List<HTMLElement> elements = stream(e.querySelectorAll("[data-" + LINK + "]")) // NON-NLS
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
                                            .textContent(link).element());

                                } else {
                                    Elements.removeChildrenFrom(linkContainer);
                                    linkContainer.appendChild(a().css(clickable)
                                            .on(click, e -> specifyParameters(url.getUrl(), link, Splitter.on(',')
                                                    .splitToList(linkContainer.dataset.get(LINK))))
                                            .textContent(link).element());
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
                        String value = form.<String> getFormItem(param).getValue();
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
                .add(p().innerHtml(resources.messages().specifyParameters(link)).element())
                .add(form.element())
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
