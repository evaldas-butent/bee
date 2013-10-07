package com.butent.bee.server.http;

import com.butent.bee.server.concurrency.Counter;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 * Contains utility HTTP request related functions like getting header information or execute binary
 * read of requests.
 */

public final class HttpUtils {

  private static BeeLogger logger = LogUtils.getLogger(HttpUtils.class);

  public static String counterInfo(String name, Object obj) {
    if (obj instanceof Counter) {
      return NameUtils.addName(name, ((Counter) obj).toString());
    } else {
      return BeeConst.STRING_EMPTY;
    }
  }

  public static Map<String, String> getHeaders(HttpServletRequest req, boolean decode) {
    Map<String, String> headers = new HashMap<String, String>();
    if (req == null) {
      return headers;
    }

    Enumeration<String> lst = req.getHeaderNames();
    if (lst == null) {
      return headers;
    }

    String nm;
    String v;

    while (lst.hasMoreElements()) {
      nm = lst.nextElement();
      if (BeeUtils.isEmpty(nm)) {
        continue;
      }
      
      Enumeration<String> values = req.getHeaders(nm);
      if (values != null && values.hasMoreElements()) {
        v = BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, Collections.list(values));
      } else {
        v = req.getHeader(nm);
      }

      if (!BeeUtils.isEmpty(v)) {
        headers.put(nm, decode ? Codec.decodeBase64(v) : v);
      }
    }
    return headers;
  }
  
  public static String getLanguage(HttpServletRequest req) {
    if (req == null) {
      return null;
    } else {
      Locale locale = req.getLocale();
      return (locale == null) ? null : locale.getLanguage();
    }
  }

  public static Map<String, String> getParameters(HttpServletRequest req, boolean decode) {
    Map<String, String> params = new HashMap<String, String>();
    if (req == null) {
      return params;
    }

    Map<String, String[]> lst = req.getParameterMap();
    String nm;
    String[] v;

    if (BeeUtils.isEmpty(lst)) {
      Enumeration<String> z = req.getParameterNames();
      if (z == null) {
        return params;
      }

      lst = new HashMap<String, String[]>();

      while (z.hasMoreElements()) {
        nm = z.nextElement();
        if (BeeUtils.isEmpty(nm)) {
          continue;
        }

        v = req.getParameterValues(nm);
        if (ArrayUtils.isEmpty(v)) {
          v = new String[] {req.getParameter(nm)};
        }
        lst.put(nm, v);
      }
    }

    if (BeeUtils.isEmpty(lst)) {
      return params;
    }

    for (Map.Entry<String, String[]> el : lst.entrySet()) {
      nm = el.getKey();
      v = el.getValue();
      if (BeeUtils.isEmpty(nm) || ArrayUtils.isEmpty(v)) {
        continue;
      }

      for (int i = 0; i < v.length; i++) {
        params.put(nm, decode ? Codec.decodeBase64(v[i]) : v[i]);
      }
    }
    return params;
  }

  public static void incrCounter(Object obj) {
    if (obj instanceof Counter) {
      ((Counter) obj).incCounter();
    }
  }

  public static String readBinary(HttpServletRequest req, int len) {
    Assert.notNull(req);
    Assert.isPositive(len);
    Assert.isEven(len);

    byte[] arr = new byte[len];
    boolean ok = true;

    try {
      ServletInputStream stream = req.getInputStream();
      int tot = stream.read(arr, 0, len);
      Assert.isPositive(tot);

      while (tot < len) {
        int cnt = stream.read(arr, tot, len - tot);
        Assert.isPositive(cnt);
        tot += cnt;
      }
      stream.close();
    } catch (IOException ex) {
      logger.error(ex);
      ok = false;
    }

    if (ok) {
      return Codec.fromBytes(arr);
    } else {
      return null;
    }
  }

  public static String readContent(HttpServletRequest req) {
    Assert.notNull(req);

    StringBuffer sb = new StringBuffer();
    int cbSize = 2048;
    char[] cbuf = new char[cbSize];
    int len;

    try {
      BufferedReader reader = req.getReader();

      do {
        len = reader.read(cbuf, 0, cbSize);
        if (len > 0) {
          sb.append(cbuf, 0, len);
        } else {
          break;
        }
      } while (len > 0);

      reader.close();
    } catch (IOException ex) {
      logger.error(ex);
    }

    return sb.toString();
  }

  public static String readPart(HttpServletRequest req, String name) {
    Assert.notNull(req);
    Assert.notEmpty(name);

    String content = null;
    try {
      Part part = req.getPart(name);
      if (part != null) {
        content = FileUtils.streamToString(part.getInputStream());
      }
    } catch (IOException ex) {
      logger.error(ex);
    } catch (ServletException ex) {
      logger.error(ex);
    }
    return content;
  }

  private HttpUtils() {
  }
}
