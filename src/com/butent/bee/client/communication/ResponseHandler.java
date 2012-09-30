package com.butent.bee.client.communication;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.composite.ResourceEditor;
import com.butent.bee.client.layout.Split;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.communication.ResponseMessage;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;

/**
 * Enables to show response's xml info and to apply unicode test to response messages.
 */

public class ResponseHandler {

  public static void showXmlInfo(int pc, int[] sizes, String content) {
    Assert.betweenInclusive(pc, 1, 3);
    Assert.notNull(sizes);
    Assert.isTrue(sizes.length >= pc);
    Assert.notEmpty(content);

    BeeResource[] resources = new BeeResource[pc];
    int start = 0;

    for (int i = 0; i < pc; i++) {
      resources[i] = new BeeResource();
      resources[i].deserialize(content.substring(start, start + sizes[i]));
      start += sizes[i];
    }

    if (pc <= 1) {
      BeeKeeper.getScreen().showResource(resources[0]);
      return;
    }

    int h = BeeKeeper.getScreen().getActivePanelHeight();

    Split panel = new Split();
    panel.addNorth(new ResourceEditor(resources[0]), h / pc);

    if (pc == 2) {
      panel.add(new ResourceEditor(resources[1]));
    } else {
      panel.addSouth(new ResourceEditor(resources[2]), h / pc);
      panel.add(new ResourceEditor(resources[1]));
    }

    BeeKeeper.getScreen().updateActivePanel(panel);
  }

  public static void unicodeTest(RpcInfo info, String respTxt,
      Collection<ResponseMessage> messages) {
    Assert.notNull(info);
    Assert.notEmpty(respTxt);
    Assert.notEmpty(messages);

    Map<String, String> reqData = info.getUserData();
    Assert.notEmpty(reqData);

    String reqTxt = reqData.get("data");
    Assert.notEmpty(reqTxt);

    int reqLen = reqTxt.length();
    int respLen = respTxt.length();

    boolean ok = (reqLen == respLen && reqTxt.equals(respTxt));

    if (!ok) {
      BeeKeeper.getLog().log(reqLen == respLen ? Level.INFO : Level.WARNING,
          "length req", reqLen, "resp", respLen);

      for (int i = 0; i < respLen && i < reqLen; i++) {
        if (reqTxt.charAt(i) != respTxt.charAt(i)) {
          BeeKeeper.getLog().warning("charAt", i,
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
            BeeKeeper.getLog().info(sb);
            sb.setLength(0);
          }
          sb.append(i);
        }

        sb.append(BeeConst.CHAR_SPACE);
        sb.append(Integer.toHexString(reqTxt.charAt(i)));
      }

      if (sb.length() > 0) {
        BeeKeeper.getLog().info(sb);
      }
    }

    String[] arr;
    String k, v, z;

    for (ResponseMessage message : messages) {
      arr = BeeUtils.split(message.getMessage(), BeeConst.CHAR_SPACE);
      if (ArrayUtils.length(arr) != 2) {
        BeeKeeper.getLog().warning(ArrayUtils.length(arr), message);
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
        BeeKeeper.getLog().info(k, v);
      } else {
        BeeKeeper.getLog().warning(k, "req", z, "resp", v);
      }
    }
  }
}
