package org.jboss.hal.client.runtime.managementinterface;

import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import org.jboss.hal.dmr.ModelNode;
import org.jboss.hal.meta.Metadata;

/** Not a real presenter, but common methods for {@code HostPresenter} and {@code StandaloneServerPresenter} */
public interface ConstantHeadersPresenter {

    void addConstantHeaderPath(ModelNode payload, SafeHtml successMessage);

    void saveConstantHeaderPath(int index, String path, SafeHtml successMessage);

    void removeConstantHeaderPath(int index, String path, SafeHtml successMessage);

    void addHeader(int pathIndex, ModelNode model, SafeHtml successMessage);

    void saveHeader(int pathIndex, int index, String header, Metadata metadata, Map<String, Object> changedValues, SafeHtml successMessage);

    void removeHeader(int pathIndex, int index, String header, SafeHtml successMessage);
}
