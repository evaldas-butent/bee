package com.butent.bee.client.communication;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Enables to show response's xml info and to apply unicode test to response messages.
 */

public class ResponseHandler {

  private static final BeeLogger logger = LogUtils.getLogger(ResponseHandler.class);
  
  public static void unicodeTest(RpcInfo info, String respTxt,
      Collection<ResponseMessage> messages) {
    Assert.notNull(info);
    Assert.notEmpty(respTxt);
    Assert.notEmpty(messages);

    @SuppressWarnings("unchecked")
    Map<String, String> reqData = (Map<String, String>) info.getUserData();
    Assert.notEmpty(reqData);

    String reqTxt = reqData.get("data");
    Assert.notEmpty(reqTxt);

    int reqLen = reqTxt.length();
    int respLen = respTxt.length();

    boolean ok = (reqLen == respLen && reqTxt.equals(respTxt));

    if (!ok) {
      logger.log(reqLen == respLen ? LogLevel.INFO : LogLevel.WARNING,
          "length req", reqLen, "resp", respLen);

      for (int i = 0; i < respLen && i < reqLen; i++) {
        if (reqTxt.charAt(i) != respTxt.charAt(i)) {
          logger.warning("charAt", i,
              "req", Integer.toHexString(reqTxt.charAt(i)),
              BeeUtils.bracket(reqTxt.charAt(i)),
              "resp", Integer.toHexString(respTxt.charAt(i)),
              BeeUtils.bracket(respTxt.charAt(i)));
          break;
        }
      }
      return;
    }

    if (reqLen <= 100) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < reqLen; i++) {
        if (i % 10 == 0) {
          if (sb.length() > 0) {
            logger.info(sb);
            sb.setLength(0);
          }
          sb.append(i);
        }

        sb.append(BeeConst.CHAR_SPACE);
        sb.append(Integer.toHexString(reqTxt.charAt(i)));
      }

      if (sb.length() > 0) {
        logger.info(sb);
      }
    }

    String[] arr;
    String k, v, z;

    for (ResponseMessage message : messages) {
      arr = BeeUtils.split(message.getMessage(), BeeConst.CHAR_SPACE);
      if (ArrayUtils.length(arr) != 2) {
        logger.warning(ArrayUtils.length(arr), message);
        continue;
      }

      k = arr[0];
      v = arr[1];

      if (reqData.containsKey(k)) {
        z = reqData.get(k);
      } else if (k.contains(BeeConst.STRING_POINT)) {
        z = reqData.get(BeeUtils.getPrefix(k, BeeConst.CHAR_POINT));
      } else {
        z = BeeConst.STRING_EMPTY;
      }

      if (v.equals(z)) {
        logger.info(k, v);
      } else {
        logger.warning(k, "req", z, "resp", v);
      }
    }
  }
}
