package com.butent.bee.shared.communication;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

/**
 * Contains utility functions for communication between server and client sides, for example
 * determining type of message type or encoding.
 */

public class CommUtils {

  public static final char DEFAULT_INFORMATION_SEPARATOR = '\u001d';

  public static final String QUERY_STRING_SEPARATOR = "?";
  public static final String QUERY_STRING_PAIR_SEPARATOR = "&";
  public static final String QUERY_STRING_VALUE_SEPARATOR = "=";

  public static final String OPTION_DEBUG = "debug";

  public static final String CONTENT_TYPE_HEADER = "content-type";
  public static final String CONTENT_LENGTH_HEADER = "content-length";

  public static ContentType defaultRequestContentType = ContentType.XML;
  public static ContentType defaultResponseContentType = ContentType.TEXT;

  public static ContentType formResponseContentType = ContentType.HTML;

  public static String buildContentType(String type) {
    return buildContentType(type, getCharacterEncoding(getContentType(type)));
  }

  public static String buildContentType(String type, String encoding) {
    Assert.notEmpty(type);
    if (BeeUtils.isEmpty(encoding)) {
      return type;
    } else {
      return type.trim() + ";charset=" + encoding.trim();
    }
  }

  public static boolean equals(ContentType z1, ContentType z2) {
    if (z1 == null || z2 == null) {
      return false;
    } else {
      return z1 == z2;
    }
  }

  public static String getCharacterEncoding(ContentType ctp) {
    if (ctp == null) {
      return null;
    } else {
      return BeeConst.CHARSET_UTF8;
    }
  }

  public static String getContent(ContentType type, String data) {
    if ((isBinary(type) || isHtml(type)) && BeeUtils.length(data) > 0) {
      return Codec.decodeBase64(data);
    } else {
      return data;
    }
  }

  public static ContentType getContentType(String s) {
    ContentType ctp = null;
    if (BeeUtils.isEmpty(s)) {
      return ctp;
    }

    for (ContentType z : ContentType.values()) {
      if (BeeUtils.same(z.transform(), s)) {
        ctp = z;
        break;
      }
    }
    return ctp;
  }

  public static ResponseObject getFormResonse(String result) {
    if (BeeUtils.isEmpty(result)) {
      return null;
    }
    return ResponseObject.restore(getContent(formResponseContentType, result));
  }

  public static String getMediaType(ContentType ctp) {
    String mt;

    switch (ctp) {
      case TEXT:
        mt = "text/plain";
        break;
      case XML:
        mt = "text/xml";
        break;
      case HTML:
        mt = "text/html";
        break;
      case ZIP:
        mt = "application/zip";
        break;
      default:
        mt = "application/octet-stream";
    }
    return mt;
  }

  public static boolean isBinary(ContentType ctp) {
    return ctp == ContentType.BINARY;
  }

  public static boolean isHtml(ContentType ctp) {
    return ctp == ContentType.HTML;
  }

  public static boolean isReservedParameter(String name) {
    Assert.notEmpty(name);
    return BeeUtils.startsSame(name, Service.RPC_VAR_SYS_PREFIX);
  }

  public static boolean isResource(ContentType ctp) {
    return ctp == ContentType.RESOURCE;
  }

  public static boolean isValidParameter(String name) {
    Assert.notEmpty(name);
    return BeeUtils.isIdentifier(name) && !isReservedParameter(name);
  }

  public static ContentType normalizeRequest(ContentType ctp) {
    return (ctp == null) ? defaultRequestContentType : ctp;
  }

  public static ContentType normalizeResponse(ContentType ctp) {
    return (ctp == null) ? defaultResponseContentType : ctp;
  }

  public static String prepareContent(ContentType type, String data) {
    if ((isBinary(type) || isHtml(type)) && BeeUtils.length(data) > 0) {
      return Codec.encodeBase64(data);
    } else {
      return data;
    }
  }

  public static String rpcMessageName(int i) {
    return Service.RPC_VAR_MSG + i;
  }

  public static String rpcParamName(int i) {
    return Service.RPC_VAR_PRM + i;
  }

  public static String rpcPartName(int i) {
    return Service.RPC_VAR_PART + i;
  }

  private CommUtils() {
  }
}
