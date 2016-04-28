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
package org.jboss.hal.client.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import org.jboss.hal.resources.Constants;
import org.jboss.hal.resources.Messages;
import org.jboss.hal.spi.Message;
import org.jboss.hal.spi.Message.Level;

/**
 * Holds information about added, replaced and failed uploads and provides a message which summarizes the upload of one
 * or several files.
 *
 * @author Harald Pehl
 */
public class UploadStatistics {

    public enum UploadStatus {
        ADDED, REPLACED, FAILED
    }


    private final Constants CONSTANTS = GWT.create(Constants.class);
    private final Messages MESSAGES = GWT.create(Messages.class);

    private final Map<String, UploadStatus> status;

    public UploadStatistics() {status = new HashMap<>();}

    void recordAdded(String name) {
        status.put(name, UploadStatus.ADDED);
    }

    void recordReplaced(String name) {
        status.put(name, UploadStatus.REPLACED);
    }

    void recordFailed(String name) {
        status.put(name, UploadStatus.FAILED);
    }

    public Message getMessage() {
        SortedSet<String> added = new TreeSet<>();
        SortedSet<String> replaced = new TreeSet<>();
        SortedSet<String> failed = new TreeSet<>();
        for (Map.Entry<String, UploadStatus> entry : status.entrySet()) {
            switch (entry.getValue()) {
                case ADDED:
                    added.add(entry.getKey());
                    break;
                case REPLACED:
                    replaced.add(entry.getKey());
                    break;
                case FAILED:
                    failed.add(entry.getKey());
                    break;
            }
        }

        Level overallResult;
        if (status.isEmpty()) {
            overallResult = Level.INFO;
        } else if (failed.isEmpty()) {
            overallResult = Level.SUCCESS;
        } else if (added.isEmpty() && replaced.isEmpty()) {
            overallResult = Level.ERROR;
        } else {
            overallResult = Level.WARNING;
        }

        Message message;
        switch (overallResult) {
            // all deployments have been successfully added or replaced
            case SUCCESS:
                message = Message.success(sentences(added, replaced, failed));
                break;

            // no statistics
            case INFO:
                message = Message.info(CONSTANTS.noDeploymentsUploaded());
                break;

            // some deployments have been successfully added or replaced, but some couldn't
            case WARNING:
                message = Message.warning(sentences(added, replaced, failed));
                break;

            // only errors
            case ERROR:
                message = Message.error(sentences(added, replaced, failed));
                break;

            default:
                message = Message.error(CONSTANTS.unknownError());
        }

        return message;
    }

    private String sentences(SortedSet<String> added, SortedSet<String> replaced, SortedSet<String> failed) {
        List<String> sentences = new ArrayList<>();
        if (!added.isEmpty()) {
            sentences.add(MESSAGES.deploymentAdded(added.size()));
        }
        if (!replaced.isEmpty()) {
            sentences.add(MESSAGES.deploymentReplaced(replaced.size()));
        }
        if (!failed.isEmpty()) {
            sentences.add(MESSAGES.deploymentFailed(failed.size()));
        }
        return Joiner.on(' ').join(sentences);
    }
}
