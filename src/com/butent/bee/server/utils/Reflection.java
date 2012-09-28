package com.butent.bee.server.utils;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Enables to invoke methods from a given name reference.
 */

public class Reflection {
  private static BeeLogger logger = LogUtils.getLogger(Reflection.class);

  public static void invoke(Object obj, String methodName, RequestInfo req, ResponseBuffer resp) {
    Assert.notNull(obj);
    if (BeeUtils.isEmpty(methodName)) {
      resp.addSevere("method name not specified");
      return;
    }

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
    Class<?>[] parameterTypes = method.getParameterTypes();
    boolean hasReq = ArrayUtils.contains(parameterTypes, RequestInfo.class);
    boolean hasResp = ArrayUtils.contains(parameterTypes, ResponseBuffer.class);

    try {
      if (hasReq && hasResp) {
        method.invoke(obj, req, resp);
      } else if (hasReq) {
        method.invoke(obj, req);
      } else if (hasResp) {
        method.invoke(obj, resp);
      } else {
        method.invoke(obj);
      }

    } catch (IllegalArgumentException ex) {
      logger.error(ex, method);
    } catch (IllegalAccessException ex) {
      logger.error(ex, method);
    } catch (InvocationTargetException ex) {
      logger.error(ex, method);
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

      if (BeeUtils.containsSame(method.getName(), name)) {
        if (found == null) {
          found = method;
        } else {
          found = null;
        }
      }
    }
    return found;
  }

  private Reflection() {
  }
}
