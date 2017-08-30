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
package org.jboss.hal.client.runtime.subsystem.transaction;

import java.util.HashMap;
import java.util.Map;

import elemental2.dom.HTMLElement;
import org.jboss.gwt.elemento.core.Elements;
import org.jboss.hal.ballroom.EmptyState;
import org.jboss.hal.ballroom.Format;
import org.jboss.hal.ballroom.LabelBuilder;
import org.jboss.hal.ballroom.PatternFly;
import org.jboss.hal.ballroom.chart.Donut;
import org.jboss.hal.core.finder.PreviewAttributes;
import org.jboss.hal.core.finder.PreviewContent;
import org.jboss.hal.core.subsystem.SubsystemMetadata;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.dmr.Operation;
import org.jboss.hal.dmr.ResourceAddress;
import org.jboss.hal.dmr.dispatch.Dispatcher;
import org.jboss.hal.meta.AddressTemplate;
import org.jboss.hal.meta.StatementContext;
import org.jboss.hal.meta.security.Constraint;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Names;
import org.jboss.hal.resources.Resources;

import static org.jboss.gwt.elemento.core.Elements.section;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTION_CONFIGURATION_TEMPLATE;
import static org.jboss.hal.client.runtime.subsystem.transaction.AddressTemplates.TRANSACTION_RUNTIME_TEMPLATE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.*;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_HOST;
import static org.jboss.hal.meta.StatementContext.Tuple.SELECTED_SERVER;
import static org.jboss.hal.resources.CSS.fontAwesome;

public class TransactionsPreview extends PreviewContent<SubsystemMetadata> {

    enum TransactionStatus {
        COMMITTED, ABORTED, HEURISTICS, TIMEDOUT_ROLLBACK, SYSTEM_ROLLBACK, RESOURCE_ROLLBACK, APPLICATION_ROLLBACK
    }

    private Donut transactions;
    private EmptyState noStatistics;
    private Dispatcher dispatcher;
    private StatementContext statementContext;
    private HTMLElement attributesElement;
    private PreviewAttributes<ModelNode> attributes;


    public TransactionsPreview(final Dispatcher dispatcher, final StatementContext statementContext,
            final Resources resources) {
        super(Names.TRANSACTION);
        this.dispatcher = dispatcher;
        this.statementContext = statementContext;

        Constants cons = resources.constants();
        ResourceAddress address = AddressTemplate.of(SELECTED_HOST, SELECTED_SERVER)
                .resolve(statementContext);
        Operation operation = new Operation.Builder(address, READ_RESOURCE_OPERATION)
                .param(ATTRIBUTES_ONLY, true)
                .build();
        dispatcher.execute(operation, result -> {

            String profile = result.get(PROFILE_NAME).asString();
            noStatistics = new EmptyState.Builder(cons.statisticsDisabledHeader())
                    .description(resources.messages().transactionsStatisticsDisabled(profile))
                    .icon(fontAwesome("line-chart"))
                    .primaryAction(cons.enableStatistics(), () -> enableStatistics(profile),
                            Constraint.writable(TRANSACTION_CONFIGURATION_TEMPLATE, STATISTICS_ENABLED))
                    .build();

            // to prevent flickering we initially hide everything
            Elements.setVisible(noStatistics.asElement(), false);

            previewBuilder().add(noStatistics);
        });

        attributes = new PreviewAttributes<>(new ModelNode(), cons.attributes())
                .append(model -> {
                    String lbl = new LabelBuilder().label(AVERAGE_COMMIT_TIME);
                    String value = "";
                    if (model.hasDefined(AVERAGE_COMMIT_TIME)) {
                        long averageCommit = model.get(AVERAGE_COMMIT_TIME).asLong();
                        value = Format.humanReadableDurationNanoseconds(averageCommit);
                    }
                    return new PreviewAttributes.PreviewAttribute(lbl, value);

                })
                .append(NUMBER_OF_INFLIGHT_TRANSACTIONS)
                .append(NUMBER_OF_NESTED_TRANSACTIONS);

        attributesElement = section()
                .addAll(attributes)
                .asElement();

        transactions = new Donut.Builder(Names.TRANSACTIONS)
                .add(TransactionStatus.COMMITTED.name(), cons.committed(), PatternFly.colors.green)
                .add(TransactionStatus.ABORTED.name(), cons.aborted(), PatternFly.colors.red)
                //.add(TransactionStatus.HEURISTICS.name(), cons.heuristics(), PatternFly.colors.orange)
                .add(TransactionStatus.TIMEDOUT_ROLLBACK.name(), cons.timedOut(), PatternFly.colors.red300)
                .add(TransactionStatus.SYSTEM_ROLLBACK.name(), cons.systemRollback(), PatternFly.colors.purple)
                .add(TransactionStatus.RESOURCE_ROLLBACK.name(), cons.resourceRollback(),
                        PatternFly.colors.black500)
                .add(TransactionStatus.APPLICATION_ROLLBACK.name(), cons.applicationRollback(),
                        PatternFly.colors.blue300)
                .legend(Donut.Legend.BOTTOM)
                .responsive(true)
                .build();
        registerAttachable(transactions);

        getHeaderContainer().appendChild(refreshLink(() -> update(null)));
        previewBuilder()
                .add(attributesElement)
                .add(transactions);

        Elements.setVisible(transactions.asElement(), false);
    }

    @Override
    public void update(final SubsystemMetadata item) {
        ResourceAddress addressWeb = TRANSACTION_RUNTIME_TEMPLATE.resolve(statementContext);
        Operation opWeb = new Operation.Builder(addressWeb, READ_RESOURCE_OPERATION)
                .param(INCLUDE_RUNTIME, true)
                .build();
        dispatcher.execute(opWeb, result -> {
            boolean statsEnabled = result.get(STATISTICS_ENABLED).asBoolean();
            Elements.setVisible(noStatistics.asElement(), !statsEnabled);

            if (statsEnabled) {
                attributes.refresh(result);
                long maxTransactions = result.get(NUMBER_OF_TRANSACTIONS).asLong();
                long committed = result.get(NUMBER_OF_COMMITTED_TRANSACTIONS).asLong();
                long aborted = result.get(NUMBER_OF_ABORTED_TRANSACTIONS).asLong();
                long timedout = result.get(NUMBER_OF_TIMEDOUT_TRANSACTIONS).asLong();
                long heuristics = result.get(NUMBER_OF_HEURISTICS_TRANSACTIONS).asLong();
                long appRollbacks = result.get(NUMBER_OF_APPLICATION_ROLLBACKS).asLong();
                long resourceRollbacks = result.get(NUMBER_OF_RESOURCE_ROLLBACKS).asLong();
                long systemRollbacks = result.get(NUMBER_OF_SYSTEM_ROLLBACKS).asLong();

                Map<String, Long> txUpdates = new HashMap<>(7);
                txUpdates.put(TransactionStatus.COMMITTED.name(), committed);
                txUpdates.put(TransactionStatus.ABORTED.name(), aborted);
                //txUpdates.put(TransactionStatus.HEURISTICS.name(), heuristics);
                txUpdates.put(TransactionStatus.TIMEDOUT_ROLLBACK.name(), timedout);
                txUpdates.put(TransactionStatus.SYSTEM_ROLLBACK.name(), systemRollbacks);
                txUpdates.put(TransactionStatus.RESOURCE_ROLLBACK.name(), resourceRollbacks);
                txUpdates.put(TransactionStatus.APPLICATION_ROLLBACK.name(), appRollbacks);
                transactions.update(txUpdates);

                Elements.setVisible(attributesElement, true);
                Elements.setVisible(transactions.asElement(), true);
            } else {
                Elements.setVisible(attributesElement, false);
                Elements.setVisible(transactions.asElement(), false);
            }
        });
    }

    private void enableStatistics(String profile) {
        ResourceAddress address = new ResourceAddress()
                .add(PROFILE, profile)
                .add(SUBSYSTEM, TRANSACTIONS);
        Operation operation = new Operation.Builder(address, WRITE_ATTRIBUTE_OPERATION)
                .param(NAME, STATISTICS_ENABLED)
                .param(VALUE, true)
                .build();
        dispatcher.execute(operation, result -> update(null));
    }
}
