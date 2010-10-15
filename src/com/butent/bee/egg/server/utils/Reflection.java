package com.butent.bee.egg.server.utils;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class Reflection {
  private static Logger logger = Logger.getLogger(Reflection.class.getName());
  
  public static void invoke(Object obj, String methodName, RequestInfo req, ResponseBuffer resp) {
    Assert.noNulls(obj, req, resp);
    Assert.notEmpty(methodName);
    
    Method method = findMethod(obj.getClass(), methodName);

    if (method == null) {
      resp.addSevere("method", methodName, "not found");
      return;
    }
    if (!method.isAccessible()) {
      method.setAccessible(true);
    }
    
    doMethod(obj, method, req, resp);
  }

  private static void doMethod(Object obj, Method method, RequestInfo req, ResponseBuffer resp) {
    try {
      method.invoke(obj, req, resp);
    } catch (IllegalArgumentException ex) {
      LogUtils.error(logger, ex, method);
    } catch (IllegalAccessException ex) {
      LogUtils.error(logger, ex, method);
    } catch (InvocationTargetException ex) {
      LogUtils.error(logger, ex, method);
    }
  }

  private static Method findMethod(Class<?> cls, String name) {
    Method found = null;
    Method[] arr = cls.getDeclaredMethods();

    for (Method method : arr) {
      if (BeeUtils.same(method.getName(), name)) {
        found = method;
        break;
      }
      
      if (BeeUtils.context(name, method.getName())) {
        if (found == null) {
          found = method;
        } else {
          found = null;
        }
      }
    }
    
    return found;
  }
  
}
