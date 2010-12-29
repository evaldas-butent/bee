package com.butent.bee.egg.client.utils;

import com.google.gwt.user.client.Window;

import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Property;
import com.butent.bee.egg.shared.utils.PropertyUtils;

import java.util.List;
import java.util.Map;

public class Browser {
  public static class Screen {
    public static int getAvailHeight() {
      return getPropertyInt("availHeight");
    }

    public static int getAvailWidth() {
      return getPropertyInt("availWidth");
    }

    public static int getColorDepth() {
      return getPropertyInt("colorDepth");
    }
    
    public static int getHeight() {
      return getPropertyInt("height");
    }
    
    public static int getPixelDepth() {
      return getPropertyInt("pixelDepth");
    }
    
    public static int getWidth() {
      return getPropertyInt("width");
    }

    private static native int getPropertyInt(String name) /*-{
      var x;
      try {
        x = $wnd.screen[name];
        if (typeof(x) != "number") {
          x = -1;
        }
      } catch (err) {
        x = -1;
      }
      return x;  
    }-*/;
  }

  public static List<Property> getLocationInfo() {
    List<Property> lst = PropertyUtils.createProperties(
        "Hash", Window.Location.getHash(),
        "Host", Window.Location.getHost(),
        "Host Name", Window.Location.getHostName(),
        "Href", Window.Location.getHref(),
        "Path", Window.Location.getPath(),
        "Port", Window.Location.getPort(),
        "Protocol", Window.Location.getProtocol(),
        "Query String", Window.Location.getQueryString());
    
    Map<String, List<String>> params = Window.Location.getParameterMap();
    if (!BeeUtils.isEmpty(params)) {
      int parCnt = params.size();
      int parIdx = 0;

      for (Map.Entry<String, List<String>> entry : params.entrySet()) {
        parIdx++;
        int valCnt = entry.getValue().size();
        int valIdx = 1;
        
        String name = BeeUtils.concat(1, "Parameter",
            (parCnt > 1) ? BeeUtils.progress(parIdx, parCnt) : BeeConst.STRING_EMPTY,
            entry.getKey(),
            (valCnt > 1) ? BeeUtils.progress(valIdx, valCnt) : BeeConst.STRING_EMPTY);
        for (String value : entry.getValue()) {
          lst.add(new Property(name, value));
          valIdx++;
        }
      }
    }
    return lst;
  }
  
  public static List<Property> getNavigatorInfo() {
    return PropertyUtils.createProperties(
        "App Code Name", Window.Navigator.getAppCodeName(),
        "App Name", Window.Navigator.getAppName(),
        "App Version", Window.Navigator.getAppVersion(),
        "Platform", Window.Navigator.getPlatform(),
        "User Agent", Window.Navigator.getUserAgent(),
        "Cookie Enabled", Window.Navigator.isCookieEnabled(),
        "Java Enabled", Window.Navigator.isJavaEnabled());
  }
  
  public static List<Property> getScreenInfo() {
    return PropertyUtils.createProperties(
        "Avail Height", Screen.getAvailHeight(),
        "Avail Width", Screen.getAvailWidth(),
        "Color Depth", Screen.getColorDepth(),
        "Height", Screen.getHeight(),
        "Pixel Depth", Screen.getPixelDepth(),
        "Width", Screen.getWidth());
  }

  public static List<Property> getWindowInfo() {
    return PropertyUtils.createProperties(
        "Title", Window.getTitle(),
        "Client Height", Window.getClientHeight(),
        "Client Width", Window.getClientWidth(),
        "Scroll Left", Window.getScrollLeft(),
        "Scroll Top", Window.getScrollTop());
  }
}
