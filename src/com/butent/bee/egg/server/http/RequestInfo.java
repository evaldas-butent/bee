package com.butent.bee.egg.server.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.SessionTrackingMode;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.Transformable;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

public class RequestInfo implements Transformable {
  private static int COUNTER = 0;

  private String id = null;

  private String service = null;
  private String dsn = null;

  private String separator = null;
  private String options = null;

  private String method = null;
  private String query = null;

  private Map<String, String> headers = null;
  private Map<String, String> params = null;

  private int contentLen = -1;
  private String contentType = null;
  private String content = null;

  private HttpServletRequest request = null;

  public RequestInfo(HttpServletRequest req) {
    super();
    COUNTER++;

    request = req;

    method = req.getMethod();
    query = req.getQueryString();

    headers = HttpUtils.getHeaders(req);
    params = HttpUtils.getParameters(req);

    if (!BeeUtils.isEmpty(headers))
      for (Map.Entry<String, String> el : headers.entrySet())
        setRpcInfo(el.getKey(), el.getValue());

    if (!BeeUtils.isEmpty(params))
      for (Map.Entry<String, String> el : params.entrySet())
        setRpcInfo(el.getKey(), el.getValue());

    contentLen = req.getContentLength();
    if (contentLen > 0) {
      contentType = req.getContentType();
      content = HttpUtils.readContent(req);
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getService() {
    return service;
  }

  public void setService(String svc) {
    this.service = svc;
  }

  public String getDsn() {
    return dsn;
  }

  public void setDsn(String dsn) {
    this.dsn = dsn;
  }

  public String getSeparator() {
    return separator;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public String getOptions() {
    return options;
  }

  public void setOptions(String options) {
    this.options = options;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getContentLen() {
    return contentLen;
  }

  public void setContentLen(int contentLen) {
    this.contentLen = contentLen;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getHeadersAsString() {
    return BeeUtils.transformMap(headers);
  }

  public boolean isDebug() {
    return BeeUtils.context(BeeService.OPTION_DEBUG, options);
  }

  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_ROW_SEPARATOR, BeeUtils
        .transformOptions("counter", COUNTER, "method", method, "id", id,
            "service", service, "dsn", dsn, "sep", separator, "opt", options),
        headers, params);
  }

  public String transform() {
    return toString();
  }

  public Collection<SubProp> getInfo() {
    if (request == null)
      return null;

    Collection<SubProp> reqInfo = new ArrayList<SubProp>();

    if (request.isAsyncStarted())
      PropUtils.appendSub(reqInfo,
          getAsyncContextInfo(request.getAsyncContext()));

    PropUtils.appendSub(reqInfo, getAttributeInfo(request));

    PropUtils.addPropSub(reqInfo, false, "Auth Type", request.getAuthType(),
        "Character Encoding", request.getCharacterEncoding(), "Content Length",
        request.getContentLength(), "Content Type", request.getContentType(),
        "Context Path", request.getContextPath());

    PropUtils.appendSub(reqInfo, getCookieInfo(request));

    DispatcherType dt = request.getDispatcherType();
    if (dt != null)
      PropUtils.addSub(reqInfo, "Dispatcher Type", null, dt.toString());

    PropUtils.appendSub(reqInfo, getHeaderInfo(request));
    PropUtils.appendSub(reqInfo, getLocaleInfo(request));

    PropUtils.addPropSub(reqInfo, false, "Local Addr", request.getLocalAddr(),
        "Local Name", request.getLocalName(), "Local Port",
        request.getLocalPort(), "Method", request.getMethod());

    PropUtils.appendSub(reqInfo, getParameterInfo(request));

    PropUtils.addPropSub(reqInfo, false, "Path Info", request.getPathInfo(),
        "Path Translated", request.getPathTranslated(), "Protocol",
        request.getProtocol(), "Query String", request.getQueryString(),
        "Remote Addr", request.getRemoteAddr(), "Remote Host",
        request.getRemoteHost(), "Remote Port", request.getRemotePort(),
        "Remote User", request.getRemoteUser(), "Requested Session Id",
        request.getRequestedSessionId(), "Request URI",
        request.getRequestURI(), "Request URL", request.getRequestURL(),
        "Scheme", request.getScheme(), "Server Name", request.getServerName(),
        "Server Port", request.getServerPort(), "Servlet Path",
        request.getServletPath());

    PropUtils.appendSub(reqInfo,
        getServletContextInfo(request.getServletContext()));
    PropUtils.appendSub(reqInfo, getSessionInfo(request.getSession(false)));

    PropUtils.addPropSub(reqInfo, false, "is Async Started",
        request.isAsyncStarted(), "is Async Supported",
        request.isAsyncSupported(), "is Requested Session Id From Cookie",
        request.isRequestedSessionIdFromCookie(),
        "is Requested Session Id From URL",
        request.isRequestedSessionIdFromURL(), "is Requested Session Id Valid",
        request.isRequestedSessionIdValid(), "is Secure", request.isSecure());

    return reqInfo;
  }

  private Collection<SubProp> getAsyncContextInfo(AsyncContext ac) {
    if (ac == null)
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Async Context";

    PropUtils
        .addPropSub(info, true, root, "Timeout", ac.getTimeout(), root,
            "Has Original Request And Response",
            ac.hasOriginalRequestAndResponse());

    return info;
  }

  private Collection<SubProp> getAttributeInfo(HttpServletRequest req) {
    if (req == null)
      return null;

    Enumeration<String> lst = req.getAttributeNames();
    if (BeeUtils.isEmpty(lst))
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Attribute";
    String nm;

    while (lst.hasMoreElements()) {
      nm = lst.nextElement();
      PropUtils.addSub(info, root, nm, req.getAttribute(nm));
    }

    return info;
  }

  private Collection<SubProp> getCookieInfo(HttpServletRequest req) {
    if (req == null)
      return null;

    Cookie[] arr = req.getCookies();
    if (BeeUtils.isEmpty(arr))
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    PropUtils.addSub(info, "Cookies", "cnt", arr.length);

    String nm;

    for (Cookie coo : arr) {
      nm = coo.getName();
      if (BeeUtils.isEmpty(nm))
        continue;

      PropUtils.addPropSub(info, true, nm, "Value", coo.getValue(), nm,
          "Comment", coo.getComment(), nm, "Domain", coo.getDomain(), nm,
          "Max Age", coo.getMaxAge(), nm, "Path", coo.getPath(), nm, "Secure",
          coo.getSecure(), nm, "Version", coo.getVersion(), nm, "Http Only",
          coo.isHttpOnly());
    }

    return info;
  }

  private Collection<SubProp> getHeaderInfo(HttpServletRequest req) {
    if (req == null)
      return null;

    Map<String, String> lst = HttpUtils.getHeaders(req);
    if (BeeUtils.isEmpty(lst))
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Header";
    String nm, v;

    for (Map.Entry<String, String> el : lst.entrySet()) {
      nm = el.getKey();
      if (BeeUtils.isEmpty(nm))
        continue;

      v = el.getValue();
      if (BeeUtils.isEmpty(v))
        continue;

      PropUtils.addSub(info, root, nm, v);
    }

    return info;
  }

  private Collection<SubProp> getLocaleInfo(HttpServletRequest req) {
    if (req == null)
      return null;

    Locale loc;
    List<Locale> lst = new ArrayList<Locale>();

    Enumeration<Locale> z = req.getLocales();
    if (BeeUtils.isEmpty(z)) {
      loc = req.getLocale();
      if (loc != null)
        lst.add(loc);
    } else
      while (z.hasMoreElements())
        lst.add(z.nextElement());

    if (lst.isEmpty())
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root;

    for (int i = 0; i < lst.size(); i++) {
      loc = lst.get(i);
      root = "Locale " + i;

      PropUtils.addPropSub(info, true, root, "Country", loc.getCountry(), root,
          "Display Country", loc.getDisplayCountry(), root,
          "Display Country loc", loc.getDisplayCountry(loc), root,
          "Display Language", loc.getDisplayLanguage(), root,
          "Display Language loc", loc.getDisplayLanguage(loc), root,
          "Display Name", loc.getDisplayName(), root, "Display Name loc",
          loc.getDisplayName(loc), root, "Display Variant",
          loc.getDisplayVariant(), root, "Display Variant loc",
          loc.getDisplayVariant(loc), root, "ISO3 Country",
          loc.getISO3Country(), root, "ISO3 Language", loc.getISO3Language(),
          root, "Language", loc.getLanguage(), root, "Variant",
          loc.getVariant());
    }

    return info;
  }

  private Collection<SubProp> getParameterInfo(HttpServletRequest req) {
    if (req == null)
      return null;

    Map<String, String> lst = HttpUtils.getParameters(req);
    if (BeeUtils.isEmpty(lst))
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Parameter";
    String nm, v;

    for (Map.Entry<String, String> el : lst.entrySet()) {
      nm = el.getKey();
      v = el.getValue();
      if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v))
        continue;

      PropUtils.addSub(info, root, nm, v);
    }

    return info;
  }

  private Collection<SubProp> getServletContextInfo(ServletContext sc) {
    if (sc == null)
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Servlet Context";
    String nm, v;
    int c;
    String[] arr;

    Enumeration<String> lst = sc.getAttributeNames();
    if (!BeeUtils.isEmpty(lst))
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();

        v = BeeUtils.transform(sc.getAttribute(nm));
        if (BeeUtils.isEmpty(v))
          continue;

        if (v.length() > 100) {
          arr = splitValue(v);
          if (arr == null)
            c = 0;
          else
            c = arr.length;

          if (c > 1) {
            for (int i = 0; i < c; i++)
              PropUtils.addSub(info, root + " attribute " + nm,
                  BeeUtils.transform(i), arr[i]);
            continue;
          }
        }

        PropUtils.addSub(info, root + " attribute " + nm, null, v);
      }

    PropUtils.addSub(info, root, "Context Path", sc.getContextPath());

    Set<SessionTrackingMode> trackMd = sc.getDefaultSessionTrackingModes();
    if (!BeeUtils.isEmpty(trackMd))
      for (SessionTrackingMode md : trackMd)
        PropUtils
            .addSub(info, root, "Default Session Tracking Mode", md.name());

    trackMd = sc.getEffectiveSessionTrackingModes();
    if (!BeeUtils.isEmpty(trackMd))
      for (SessionTrackingMode md : trackMd)
        PropUtils.addSub(info, root, "Effective Session Tracking Mode",
            md.name());

    PropUtils.addPropSub(info, true, root, "Effective Major Version",
        sc.getEffectiveMajorVersion(), root, "Effective Minor Version",
        sc.getEffectiveMinorVersion(), root, "Major Version",
        sc.getMajorVersion(), root, "Minor Version", sc.getMinorVersion(),
        root, "Server Info", sc.getServerInfo(), root, "Servlet Context Name",
        sc.getServletContextName());

    lst = sc.getInitParameterNames();
    if (!BeeUtils.isEmpty(lst))
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();
        PropUtils.addSub(info, root + " init parameter", nm,
            sc.getInitParameter(nm));
      }

    return info;
  }

  private Collection<SubProp> getSessionInfo(HttpSession hs) {
    if (hs == null)
      return null;

    Collection<SubProp> info = new ArrayList<SubProp>();
    String root = "Session";
    String nm;

    Enumeration<String> lst = hs.getAttributeNames();
    if (!BeeUtils.isEmpty(lst))
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();
        PropUtils.addSub(info, root + " attribute", nm, hs.getAttribute(nm));
      }

    PropUtils.addPropSub(info, true, root, "Id", hs.getId(), root,
        "Creation Time", hs.getCreationTime(), root, "Last Accessed Time",
        hs.getLastAccessedTime(), root, "Max Inactive Interval",
        hs.getMaxInactiveInterval(), root, "Is New", hs.isNew());

    return info;
  }

  private void setRpcInfo(String nm, String v) {
    if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v))
      return;

    if (nm.equalsIgnoreCase(BeeService.RPC_FIELD_QID))
      id = v;
    else if (nm.equalsIgnoreCase(BeeService.RPC_FIELD_QNM))
      service = v;
    else if (nm.equalsIgnoreCase(BeeService.RPC_FIELD_DSN))
      dsn = v;
    else if (nm.equalsIgnoreCase(BeeService.RPC_FIELD_SEP))
      separator = v;
    else if (nm.equalsIgnoreCase(BeeService.RPC_FIELD_OPT))
      options = v;
  }

  private String[] splitValue(String s) {
    if (BeeUtils.isEmpty(s))
      return null;
    else if (s.contains(", "))
      return s.split(", ");
    else if (s.contains(";"))
      return s.split(";");
    else
      return null;
  }

}
