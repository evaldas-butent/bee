package com.butent.bee.server.http;

import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.Transformable;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.security.Principal;
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

/**
 * Enables to manage HTTP requests - get and set their attributes and change their parameters.
 */

public class RequestInfo implements HasExtendedInfo, Transformable, HasOptions {
  private static int COUNTER = 0;

  private String id = null;

  private String service = null;
  private String dsn = null;
  private String locale = null;

  private String separator = null;
  private String options = null;

  private String method = null;
  private String query = null;

  private Map<String, String> headers = null;
  private Map<String, String> params = null;
  private Map<String, String> vars = null;

  private int contentLen = -1;
  private String contentTypeHeader = null;
  private String content = null;

  private ContentType contentType;

  private HttpServletRequest request = null;

  public RequestInfo(HttpServletRequest req) {
    super();
    COUNTER++;

    request = req;

    method = req.getMethod();
    query = req.getQueryString();

    headers = HttpUtils.getHeaders(req);
    params = HttpUtils.getParameters(req);

    if (!BeeUtils.isEmpty(headers)) {
      for (Map.Entry<String, String> el : headers.entrySet()) {
        setRpcInfo(el.getKey(), el.getValue());
      }
    }

    if (!BeeUtils.isEmpty(params)) {
      for (Map.Entry<String, String> el : params.entrySet()) {
        setRpcInfo(el.getKey(), el.getValue());
      }
    }

    contentLen = req.getContentLength();
    if (contentLen > 0) {
      contentTypeHeader = req.getContentType();
      content = CommUtils.getContent(getContentType(), HttpUtils.readContent(req));
    }

    if (isXml()) {
      vars = XmlUtils.getElements(content, Service.XML_TAG_DATA);
    }
  }

  public String getContent() {
    return content;
  }

  public int getContentLen() {
    return contentLen;
  }

  public ContentType getContentType() {
    return contentType;
  }

  public String getContentTypeHeader() {
    return contentTypeHeader;
  }

  public String getDsn() {
    return dsn;
  }

  @Override
  public List<ExtendedProperty> getExtendedInfo() {
    if (request == null) {
      return null;
    }

    List<ExtendedProperty> reqInfo = new ArrayList<ExtendedProperty>();

    if (request.isAsyncStarted()) {
      PropertyUtils.appendExtended(reqInfo, getAsyncContextInfo(request.getAsyncContext()));
    }

    PropertyUtils.appendExtended(reqInfo, getAttributeInfo(request));

    PropertyUtils.addProperties(reqInfo, false,
        "Auth Type", request.getAuthType(),
        "Character Encoding", request.getCharacterEncoding(),
        "Content Length", request.getContentLength(),
        "Content Type", request.getContentType(),
        "Context Path", request.getContextPath());

    PropertyUtils.appendExtended(reqInfo, getCookieInfo(request));

    DispatcherType dt = request.getDispatcherType();
    if (dt != null) {
      PropertyUtils.addExtended(reqInfo, "Dispatcher Type", null, dt.toString());
    }

    PropertyUtils.appendExtended(reqInfo, getHeaderInfo(request));
    PropertyUtils.appendExtended(reqInfo, getLocaleInfo(request));

    PropertyUtils.addProperties(reqInfo, false,
        "Local Addr", request.getLocalAddr(),
        "Local Name", request.getLocalName(),
        "Local Port", request.getLocalPort(),
        "Method", request.getMethod());

    PropertyUtils.appendExtended(reqInfo, getParameterInfo(request));

    PropertyUtils.addProperties(reqInfo, false,
        "Path Info", request.getPathInfo(),
        "Path Translated", request.getPathTranslated(),
        "Protocol", request.getProtocol(),
        "Query String", request.getQueryString(),
        "Remote Addr", request.getRemoteAddr(),
        "Remote Host", request.getRemoteHost(),
        "Remote Port", request.getRemotePort(),
        "Remote User", request.getRemoteUser(),
        "Requested Session Id", request.getRequestedSessionId(),
        "Request URI", request.getRequestURI(),
        "Request URL", request.getRequestURL(),
        "Scheme", request.getScheme(),
        "Server Name", request.getServerName(),
        "Server Port", request.getServerPort(),
        "Servlet Path", request.getServletPath());

    PropertyUtils.appendExtended(reqInfo, getServletContextInfo(request.getServletContext()));
    PropertyUtils.appendExtended(reqInfo, getSessionInfo(request.getSession(false)));

    Principal principal = request.getUserPrincipal();
    if (principal != null) {
      PropertyUtils.addChildren(reqInfo, "User Principal",
          "Name", principal.getName(), "String", principal.toString());
    }

    PropertyUtils.addProperties(reqInfo, false,
        "is Async Started", request.isAsyncStarted(),
        "is Async Supported", request.isAsyncSupported(),
        "is Requested Session Id From Cookie", request.isRequestedSessionIdFromCookie(),
        "is Requested Session Id From URL", request.isRequestedSessionIdFromURL(),
        "is Requested Session Id Valid", request.isRequestedSessionIdValid(),
        "is Secure", request.isSecure());

    return reqInfo;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getHeadersAsString() {
    return BeeUtils.transformMap(headers);
  }

  public String getId() {
    return id;
  }

  public String getLocale() {
    return locale;
  }

  public String getMethod() {
    return method;
  }

  @Override
  public String getOptions() {
    return options;
  }

  public String getParameter(int idx) {
    return getParameter(CommUtils.rpcParamName(idx));
  }

  public String getParameter(String name) {
    Assert.notEmpty(name);
    String value = null;

    if (!BeeUtils.isEmpty(getParams())) {
      value = getParams().get(name);
      if (!BeeUtils.isEmpty(value)) {
        return value;
      }
    }

    if (!BeeUtils.isEmpty(getHeaders())) {
      value = getHeaders().get(name);
      if (!BeeUtils.isEmpty(value)) {
        return value;
      }
    }

    if (!BeeUtils.isEmpty(getVars())) {
      value = getVars().get(name);
    }

    return value;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public String getParamsAsString() {
    return BeeUtils.transformMap(params);
  }

  public String getQuery() {
    return query;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public String getSeparator() {
    return separator;
  }

  public String getService() {
    return service;
  }

  public Map<String, String> getVars() {
    return vars;
  }

  public String getVarsAsString() {
    return BeeUtils.transformMap(vars);
  }

  public boolean hasParameter(int idx) {
    return hasParameter(CommUtils.rpcParamName(idx));
  }

  public boolean hasParameter(String name) {
    Assert.notEmpty(name);

    if (!BeeUtils.isEmpty(getParams()) && getParams().containsKey(name)) {
      return true;
    }
    if (!BeeUtils.isEmpty(getHeaders()) && getHeaders().containsKey(name)) {
      return true;
    }
    if (!BeeUtils.isEmpty(getVars()) && getVars().containsKey(name)) {
      return true;
    }

    return false;
  }

  public boolean isDebug() {
    return BeeUtils.containsSame(options, CommUtils.OPTION_DEBUG);
  }

  public boolean isXml() {
    return getContentLen() > 0 && CommUtils.equals(getContentType(), ContentType.XML);
  }

  public void logHeaders(BeeLogger logger) {
    if (BeeUtils.isEmpty(getHeaders())) {
      logger.warning("headers not available");
      return;
    }

    int n = getHeaders().size();
    int i = 0;

    for (Map.Entry<String, String> el : getHeaders().entrySet()) {
      logger.info("Header", BeeUtils.progress(++i, n), el.getKey(), el.getValue());
    }
  }

  public void logParams(BeeLogger logger) {
    if (BeeUtils.isEmpty(getParams())) {
      logger.warning("Parameters not available");
      return;
    }

    int n = getParams().size();
    int i = 0;

    for (Map.Entry<String, String> el : getParams().entrySet()) {
      logger.info("Parameter", BeeUtils.progress(++i, n), el.getKey(), el.getValue());
    }
  }

  public void logVars(BeeLogger logger) {
    if (BeeUtils.isEmpty(getVars())) {
      if (isXml()) {
        logger.warning("Vars not available");
      }
      return;
    }

    int n = getVars().size();
    int i = 0;

    for (Map.Entry<String, String> el : getVars().entrySet()) {
      logger.info("Var", BeeUtils.progress(++i, n), el.getKey(), el.getValue());
    }
  }

  public boolean parameterEquals(int idx, String value) {
    Assert.notEmpty(value);
    return BeeUtils.same(getParameter(idx), value);
  }

  public boolean parameterEquals(String name, String value) {
    Assert.notEmpty(name);
    Assert.notEmpty(value);
    return BeeUtils.same(getParameter(name), value);
  }

  public void setContent(String content) {
    this.content = content;
  }

  public void setContentLen(int contentLen) {
    this.contentLen = contentLen;
  }

  public void setContentTypeHeader(String contentTypeHeader) {
    this.contentTypeHeader = contentTypeHeader;
  }

  public void setDsn(String dsn) {
    this.dsn = dsn;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public void setService(String svc) {
    this.service = svc;
  }

  public void setVars(Map<String, String> vars) {
    this.vars = vars;
  }

  @Override
  public String toString() {
    return BeeUtils.concat(BeeConst.DEFAULT_ROW_SEPARATOR,
        BeeUtils.transformOptions("counter", COUNTER, "method", method, "id", id,
            "service", service, "dsn", dsn, "sep", separator, "opt", options), headers, params);
  }

  @Override
  public String transform() {
    return toString();
  }

  private Collection<ExtendedProperty> getAsyncContextInfo(AsyncContext ac) {
    if (ac == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Async Context";

    PropertyUtils.addProperties(info, true, root, "Timeout", ac.getTimeout(),
        root, "Has Original Request And Response", ac.hasOriginalRequestAndResponse());

    return info;
  }

  private Collection<ExtendedProperty> getAttributeInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Enumeration<String> lst = req.getAttributeNames();
    if (BeeUtils.isEmpty(lst)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Attribute";
    String nm;

    while (lst.hasMoreElements()) {
      nm = lst.nextElement();
      PropertyUtils.addExtended(info, root, nm, req.getAttribute(nm));
    }

    return info;
  }

  private Collection<ExtendedProperty> getCookieInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Cookie[] arr = req.getCookies();
    if (BeeUtils.isEmpty(arr)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    PropertyUtils.addExtended(info, "Cookies", "cnt", arr.length);

    String nm;

    for (Cookie coo : arr) {
      nm = coo.getName();
      if (BeeUtils.isEmpty(nm)) {
        continue;
      }

      PropertyUtils.addProperties(info, true, nm, "Value", coo.getValue(),
          nm, "Comment", coo.getComment(), nm, "Domain", coo.getDomain(),
          nm, "Max Age", coo.getMaxAge(), nm, "Path", coo.getPath(),
          nm, "Secure", coo.getSecure(), nm, "Version", coo.getVersion(),
          nm, "Http Only", coo.isHttpOnly());
    }
    return info;
  }

  private Collection<ExtendedProperty> getHeaderInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Map<String, String> lst = HttpUtils.getHeaders(req);
    if (BeeUtils.isEmpty(lst)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Header";
    String nm, v;

    for (Map.Entry<String, String> el : lst.entrySet()) {
      nm = el.getKey();
      if (BeeUtils.isEmpty(nm)) {
        continue;
      }

      v = el.getValue();
      if (BeeUtils.isEmpty(v)) {
        continue;
      }

      PropertyUtils.addExtended(info, root, nm, v);
    }
    return info;
  }

  private Collection<ExtendedProperty> getLocaleInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Locale loc;
    List<Locale> lst = new ArrayList<Locale>();

    Enumeration<Locale> z = req.getLocales();
    if (BeeUtils.isEmpty(z)) {
      loc = req.getLocale();
      if (loc != null) {
        lst.add(loc);
      }
    } else {
      while (z.hasMoreElements()) {
        lst.add(z.nextElement());
      }
    }

    if (lst.isEmpty()) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root;

    for (int i = 0; i < lst.size(); i++) {
      loc = lst.get(i);
      root = "Locale " + i;

      PropertyUtils.addProperties(info, true, root, "Country", loc.getCountry(),
          root, "Display Country", loc.getDisplayCountry(),
          root, "Display Country loc", loc.getDisplayCountry(loc),
          root, "Display Language", loc.getDisplayLanguage(),
          root, "Display Language loc", loc.getDisplayLanguage(loc),
          root, "Display Name", loc.getDisplayName(),
          root, "Display Name loc", loc.getDisplayName(loc),
          root, "Display Variant", loc.getDisplayVariant(),
          root, "Display Variant loc", loc.getDisplayVariant(loc),
          root, "ISO3 Country", loc.getISO3Country(),
          root, "ISO3 Language", loc.getISO3Language(),
          root, "Language", loc.getLanguage(),
          root, "Variant", loc.getVariant());
    }
    return info;
  }

  private Collection<ExtendedProperty> getParameterInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Map<String, String> lst = HttpUtils.getParameters(req);
    if (BeeUtils.isEmpty(lst)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Parameter";
    String nm, v;

    for (Map.Entry<String, String> el : lst.entrySet()) {
      nm = el.getKey();
      v = el.getValue();
      if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v)) {
        continue;
      }

      PropertyUtils.addExtended(info, root, nm, v);
    }
    return info;
  }

  private Collection<ExtendedProperty> getServletContextInfo(ServletContext sc) {
    if (sc == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Servlet Context";
    String nm, v;
    int c;
    String[] arr;

    Enumeration<String> lst = sc.getAttributeNames();
    if (!BeeUtils.isEmpty(lst)) {
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();

        v = BeeUtils.transform(sc.getAttribute(nm));
        if (BeeUtils.isEmpty(v)) {
          continue;
        }

        if (v.length() > 100) {
          arr = splitValue(v);
          if (arr == null) {
            c = 0;
          } else {
            c = arr.length;
          }

          if (c > 1) {
            for (int i = 0; i < c; i++) {
              PropertyUtils.addExtended(info, root + " attribute " + nm,
                  BeeUtils.transform(i), arr[i]);
            }
            continue;
          }
        }
        PropertyUtils.addExtended(info, root + " attribute " + nm, null, v);
      }
    }

    PropertyUtils.addExtended(info, root, "Context Path", sc.getContextPath());

    Set<SessionTrackingMode> trackMd = sc.getDefaultSessionTrackingModes();
    if (!BeeUtils.isEmpty(trackMd)) {
      for (SessionTrackingMode md : trackMd) {
        PropertyUtils.addExtended(info, root, "Default Session Tracking Mode", md.name());
      }
    }

    trackMd = sc.getEffectiveSessionTrackingModes();
    if (!BeeUtils.isEmpty(trackMd)) {
      for (SessionTrackingMode md : trackMd) {
        PropertyUtils.addExtended(info, root, "Effective Session Tracking Mode", md.name());
      }
    }

    PropertyUtils.addProperties(info, true,
        root, "Effective Major Version", sc.getEffectiveMajorVersion(),
        root, "Effective Minor Version", sc.getEffectiveMinorVersion(),
        root, "Major Version", sc.getMajorVersion(),
        root, "Minor Version", sc.getMinorVersion(),
        root, "Server Info", sc.getServerInfo(),
        root, "Servlet Context Name", sc.getServletContextName());

    lst = sc.getInitParameterNames();
    if (!BeeUtils.isEmpty(lst)) {
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();
        PropertyUtils.addExtended(info, root + " init parameter", nm, sc.getInitParameter(nm));
      }
    }

    return info;
  }

  private Collection<ExtendedProperty> getSessionInfo(HttpSession hs) {
    if (hs == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Session";
    String nm;

    Enumeration<String> lst = hs.getAttributeNames();
    if (!BeeUtils.isEmpty(lst)) {
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();
        PropertyUtils.addExtended(info, root + " attribute", nm, hs.getAttribute(nm));
      }
    }

    PropertyUtils.addProperties(info, true, root, "Id", hs.getId(),
        root, "Creation Time", hs.getCreationTime(),
        root, "Last Accessed Time", hs.getLastAccessedTime(),
        root, "Max Inactive Interval", hs.getMaxInactiveInterval(),
        root, "Is New", hs.isNew());

    return info;
  }

  private void setRpcInfo(String nm, String v) {
    if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v)) {
      return;
    }

    if (BeeUtils.same(nm, Service.RPC_VAR_QID)) {
      id = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_SVC)) {
      service = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_DSN)) {
      dsn = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_SEP)) {
      separator = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_OPT)) {
      options = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_CTP)) {
      contentType = CommUtils.getContentType(v);
    } else if (BeeUtils.same(nm, Service.RPC_VAR_LOC)) {
      locale = v;
    }
  }

  private String[] splitValue(String s) {
    if (BeeUtils.isEmpty(s)) {
      return null;
    } else if (s.contains(", ")) {
      return s.split(", ");
    } else if (s.contains(";")) {
      return s.split(";");
    } else {
      return null;
    }
  }
}
