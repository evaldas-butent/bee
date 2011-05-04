package com.butent.bee.client.communication;

import com.google.gwt.core.client.JsArrayString;

/**
 * Requires that classes implementing this interface would have <code>ResponseCallback</code>
 * method.
 */

public interface ResponseCallback {
  void onResponse(JsArrayString arr);

}
