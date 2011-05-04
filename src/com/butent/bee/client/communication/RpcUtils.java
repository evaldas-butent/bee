package com.butent.bee.client.communication;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Contains utility functions for working with remote procedure calls, for example building query
 * strings or appending query parameters.
 * 
 */

public class RpcUtils {
  public static String addQueryString(String url, String qs) {
    Assert.notEmpty(url);

    if (BeeUtils.isEmpty(qs)) {
      return url;
    } else {
      return url.trim() + CommUtils.QUERY_STRING_SEPARATOR + qs.trim();
    }
  }

  public static String appendQueryParameters(String qs, Map<String, String> params) {
    if (BeeUtils.isEmpty(params)) {
      return qs;
    }

    StringBuilder sb = new StringBuilder();
    return sb.toString();
  }

  public static String buildQueryString(String... x) {
    Assert.notNull(x);
    int c = x.length;
    Assert.parameterCount(c, 2);

    StringBuilder s = new StringBuilder();

    for (int i = 0; i < c - 1; i += 2) {
      if (!BeeUtils.isEmpty(x[i]) && !BeeUtils.isEmpty(x[i + 1])) {
        if (s.length() > 0) {
          s.append(CommUtils.QUERY_STRING_PAIR_SEPARATOR);
        }

        s.append(x[i].trim());
        s.append(CommUtils.QUERY_STRING_VALUE_SEPARATOR);
        s.append(x[i + 1].trim());
      }
    }
    return s.toString();
  }

  public static Collection<Property> requestInfo(RequestBuilder rb) {
    Assert.notNull(rb);
    Collection<Property> prp = new ArrayList<Property>();

    PropertyUtils.addProperties(prp, "Url", rb.getUrl(),
        "Http Method", rb.getHTTPMethod(),
        "Request Data", rb.getRequestData(),
        "Password", rb.getPassword(),
        "User", rb.getUser(),
        "Timeout", rb.getTimeoutMillis());
    return prp;
  }

  public static Collection<ExtendedProperty> responseInfo(Response resp) {
    Assert.notNull(resp);

    Collection<ExtendedProperty> prp = new ArrayList<ExtendedProperty>();

    PropertyUtils.addExtended(prp, "Status",
        BeeUtils.addName("Code", resp.getStatusCode()), resp.getStatusText());

    Header[] h = resp.getHeaders();
    int c = h.length;

    if (c > 0) {
      PropertyUtils.addExtended(prp, "Headers", BeeUtils.addName("Cnt", c),
          BeeUtils.addName("Length", resp.getHeadersAsString().length()));

      for (int i = 0; i < c; i++) {
        if (BeeUtils.isEmpty(h[i])) {
          PropertyUtils.addExtended(prp, "Header", "Empty");
        } else {
          PropertyUtils.addExtended(prp, "Header", h[i].getName(), h[i].getValue());
        }
      }
    }
    return prp;
  }

  private RpcUtils() {
  }
}
