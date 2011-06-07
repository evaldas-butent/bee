package com.butent.bee.client.communication;

import com.butent.bee.shared.communication.ResponseObject;

/**
 * Requires that classes implementing this interface would have <code>ResponseCallback</code>
 * method.
 */

public interface ResponseCallback {
  void onResponse(ResponseObject response);
}
