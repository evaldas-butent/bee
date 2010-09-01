package com.butent.bee.egg.client.communication;

import java.util.ArrayList;
import java.util.Collection;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;

import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;

public class RpcUtil {
  public static final String buildQueryString(String... x) {
    int c = x.length;
    if (c < 2)
      return null;

    StringBuilder s = new StringBuilder();

    for (int i = 0; i < c - 1; i += 2)
      if (!BeeUtils.isEmpty(x[i]) && !BeeUtils.isEmpty(x[i + 1])) {
        if (s.length() > 0)
          s.append(BeeService.QUERY_STRING_PAIR_SEPARATOR);

        s.append(x[i].trim());
        s.append(BeeService.QUERY_STRING_VALUE_SEPARATOR);
        s.append(x[i + 1].trim());
      }

    return s.toString();
  }

  public static final String addQueryString(String url, String qs) {
    if (BeeUtils.isEmpty(url))
      return null;
    else if (BeeUtils.isEmpty(qs))
      return url;
    else
      return url.trim() + BeeService.QUERY_STRING_SEPARATOR + qs.trim();
  }

  public static final Collection<StringProp> requestInfo(RequestBuilder rb,
      Request req) {
    if (rb == null && req == null)
      return null;

    Collection<StringProp> prp = new ArrayList<StringProp>();

    if (rb != null) {
      PropUtils.addString(prp, "Url", rb.getUrl(), "Http Method",
          rb.getHTTPMethod(), "Request Data", rb.getRequestData(), "Password",
          rb.getPassword(), "User", rb.getUser(), "Timeout",
          rb.getTimeoutMillis());
    }

    if (req != null)
      PropUtils.addString(prp, "Pending", req.isPending());

    return prp;
  }

  public static final Collection<SubProp> responseInfo(Response resp,
      int maxTextLength) {
    if (resp == null)
      return null;

    Collection<SubProp> prp = new ArrayList<SubProp>();

    PropUtils.addSub(prp, "Status",
        BeeUtils.addName("Code", resp.getStatusCode()), resp.getStatusText());

    Header[] h = resp.getHeaders();
    int c = h.length;

    if (c > 0) {
      PropUtils.addSub(prp, "Headers", BeeUtils.addName("Cnt", c),
          BeeUtils.addName("Length", resp.getHeadersAsString().length()));

      for (int i = 0; i < c; i++)
        PropUtils.addSub(prp, "Header", h[i].getName(), h[i].getValue());
    }

    int l = resp.getText().length();

    PropUtils.addSub(
        prp,
        "Text",
        BeeUtils.addName("Length", l),
        maxTextLength > 0 && l > maxTextLength ? BeeUtils.left(resp.getText(),
            maxTextLength) + BeeConst.ELLIPSIS : resp.getText());

    return prp;
  }

}
