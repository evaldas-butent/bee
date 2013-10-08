package com.butent.bee.server.http;

import com.google.common.net.HttpHeaders;

import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasExtendedInfo;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.Service;
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

public class RequestInfo implements HasExtendedInfo, HasOptions {

  private static int counter;

  private static Collection<ExtendedProperty> getAsyncContextInfo(AsyncContext ac) {
    if (ac == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Async Context";

    PropertyUtils.addProperties(info, true, root, "Timeout", ac.getTimeout(),
        root, "Has Original Request And Response", ac.hasOriginalRequestAndResponse());

    return info;
  }
  private static Collection<ExtendedProperty> getAttributeInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Enumeration<String> lst = req.getAttributeNames();
    if (lst == null) {
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

  private static Collection<ExtendedProperty> getCookieInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Cookie[] arr = req.getCookies();
    if (arr == null) {
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
  private static Collection<ExtendedProperty> getHeaderInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Map<String, String> lst = HttpUtils.getHeaders(req, false);
    if (BeeUtils.isEmpty(lst)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Header";
    String nm;
    String v;

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

  private static Collection<ExtendedProperty> getLocaleInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Locale loc;
    List<Locale> lst = new ArrayList<Locale>();

    Enumeration<Locale> z = req.getLocales();
    if (z == null) {
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
  private static Collection<ExtendedProperty> getParameterInfo(HttpServletRequest req) {
    if (req == null) {
      return null;
    }

    Map<String, String> lst = HttpUtils.getParameters(req, false);
    if (BeeUtils.isEmpty(lst)) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Parameter";
    String nm;
    String v;

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
  private static Collection<ExtendedProperty> getServletContextInfo(ServletContext sc) {
    if (sc == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Servlet Context";
    String nm;
    String v;
    int c;
    String[] arr;

    Enumeration<String> lst = sc.getAttributeNames();
    if (lst != null) {
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();

        Object attribute = sc.getAttribute(nm);
        if (attribute == null) {
          continue;
        }
        v = BeeUtils.trim(attribute.toString());
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
                  BeeUtils.toString(i), arr[i]);
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
    if (lst != null) {
      while (lst.hasMoreElements()) {
        nm = lst.nextElement();
        PropertyUtils.addExtended(info, root + " init parameter", nm, sc.getInitParameter(nm));
      }
    }

    return info;
  }

  private static Collection<ExtendedProperty> getSessionInfo(HttpSession hs) {
    if (hs == null) {
      return null;
    }

    Collection<ExtendedProperty> info = new ArrayList<ExtendedProperty>();
    String root = "Session";
    String nm;

    Enumeration<String> lst = hs.getAttributeNames();
    if (lst != null) {
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
  private static String[] splitValue(String s) {
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
  private final HttpServletRequest request;
  
  private final String method;
  private final String query;

  private final Map<String, String> headers;
  private final Map<String, String> params;

  private final Map<String, String> vars;

  private final int contentLen;

  private String contentTypeHeader;

  private String content;

  private String id;

  private String service;

  private String separator;

  private String options;

  private ContentType contentType;

  public RequestInfo(HttpServletRequest req) {
    super();
    counter++;

    this.request = req;

    this.method = req.getMethod();
    this.query = req.getQueryString();

    this.headers = HttpUtils.getHeaders(req, false);
    this.params = HttpUtils.getParameters(req, false);

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
    
    this.contentLen = req.getContentLength();
    if (contentLen > 0) {
      this.contentTypeHeader = req.getContentType();
      this.content = CommUtils.getContent(getContentType(), HttpUtils.readContent(req));
    } else {
      this.contentTypeHeader = null;
      this.content = null;
    }

    if (isXml()) {
      this.vars = XmlUtils.getElements(content, Service.XML_TAG_DATA);
    } else {
      this.vars = null;
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

  public String getId() {
    return id;
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

  public String getQuery() {
    return query;
  }
  
  public String getRemoteAddr() {
    return request.getRemoteAddr();
  }

  public String getRemoteHost() {
    return request.getRemoteHost();
  }

  public String getRemoteUser() {
    return request.getRemoteUser();
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

  public String getUserAgent() {
    return getHeaders().get(HttpHeaders.USER_AGENT);
  }

  public Map<String, String> getVars() {
    return vars;
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

  public void setContentTypeHeader(String contentTypeHeader) {
    this.contentTypeHeader = contentTypeHeader;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public void setOptions(String options) {
    this.options = options;
  }

  public void setSeparator(String separator) {
    this.separator = separator;
  }

  public void setService(String svc) {
    this.service = svc;
  }

  @Override
  public String toString() {
    return BeeUtils.join(BeeConst.DEFAULT_ROW_SEPARATOR,
        BeeUtils.joinOptions("counter", BeeUtils.toString(counter), "method", method, "id", id,
            "service", service, "sep", separator, "opt", options), headers, params);
  }

  private void setRpcInfo(String nm, String v) {
    if (BeeUtils.isEmpty(nm) || BeeUtils.isEmpty(v)) {
      return;
    }

    if (BeeUtils.same(nm, Service.RPC_VAR_QID)) {
      id = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_SVC)) {
      service = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_SEP)) {
      separator = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_OPT)) {
      options = v;
    } else if (BeeUtils.same(nm, Service.RPC_VAR_CTP)) {
      contentType = CommUtils.getContentType(v);
    }
  }
}
