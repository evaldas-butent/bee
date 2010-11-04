package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.composite.TextEditor;
import com.butent.bee.egg.client.layout.BeeSplit;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeResource;
import com.butent.bee.egg.shared.communication.ResponseMessage;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Map;
import java.util.logging.Level;

public class ResponseHandler {

  public static void showXmlInfo(int pc, int[] sizes, String content) {
    Assert.betweenInclusive(pc, 1, 3);
    Assert.arrayLength(sizes, pc);
    Assert.notEmpty(content);

    BeeResource[] resources = new BeeResource[pc];
    int start = 0;

    for (int i = 0; i < pc; i++) {
      resources[i] = new BeeResource();
      resources[i].deserialize(content.substring(start, start + sizes[i]));
      start += sizes[i];
    }

    if (pc <= 1) {
      BeeKeeper.getUi().showResource(resources[0]);
      return;
    }

    int h = BeeKeeper.getUi().getActivePanelHeight();

    BeeSplit panel = new BeeSplit();
    panel.addNorth(new TextEditor(resources[0]), h / pc);

    if (pc == 2) {
      panel.add(new TextEditor(resources[1]));
    } else {
      panel.addSouth(new TextEditor(resources[2]), h / pc);
      panel.add(new TextEditor(resources[1]));
    }

    BeeKeeper.getUi().updateActivePanel(panel);
  }

  public static void unicodeTest(RpcInfo info, String respTxt, int mc,
      ResponseMessage[] messages) {
    Assert.notNull(info);
    Assert.notEmpty(respTxt);
    Assert.isPositive(mc);
    Assert.arrayLength(messages, mc);

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
          BeeKeeper.getLog().warning("charAt", i, "req",
              Integer.toHexString(reqTxt.charAt(i)),
              BeeUtils.bracket((int) reqTxt.charAt(i)), "resp",
              Integer.toHexString(respTxt.charAt(i)),
              BeeUtils.bracket((int) respTxt.charAt(i)));
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

    for (int i = 0; i < mc; i++) {
      arr = BeeUtils.split(messages[i].getMessage(), BeeConst.STRING_SPACE);
      if (BeeUtils.length(arr) != 2) {
        BeeKeeper.getLog().warning(BeeUtils.length(arr), messages[i]);
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
