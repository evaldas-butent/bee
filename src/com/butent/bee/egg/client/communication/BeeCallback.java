package com.butent.bee.egg.client.communication;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.client.utils.BeeJs;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestTimeoutException;
import com.google.gwt.http.client.Response;

public class BeeCallback implements RequestCallback {

  @Override
  public void onError(Request req, Throwable ex) {
    if (ex instanceof RequestTimeoutException)
      BeeKeeper.getLog().log("request timeout", ex);
    else
      BeeKeeper.getLog().log("request failure", ex);
  }

  @Override
  public void onResponseReceived(Request req, Response resp) {
    if (resp.getStatusCode() == Response.SC_OK) {
      BeeDuration dur = new BeeDuration("response get");

      String txt = resp.getText();
      int len = txt.length();

      String hSep = resp.getHeader(BeeService.RPC_FIELD_SEP);
      String sep;

      if (BeeUtils.isHexString(hSep))
        sep = new String(BeeUtils.fromHex(hSep));
      else
        sep = Character.toString(BeeService.DEFAULT_INFORMATION_SEPARATOR);

      int cc = BeeUtils.toInt(resp.getHeader(BeeService.RPC_FIELD_COLS));
      boolean hasSep = txt.indexOf(sep) > 0;

      BeeKeeper.getLog().finish(dur,
          BeeUtils.addName(BeeService.RPC_FIELD_COLS, cc),
          BeeUtils.addName("len", len),
          hasSep ? BeeConst.STRING_EMPTY : BeeUtils.addName("text", txt));
      BeeKeeper.getLog().log("response headers", resp.getHeadersAsString());

      if (hasSep) {
        dur.restart("split");
        JsArrayString arr = BeeJs.split(txt, sep);
        BeeKeeper.getLog().finish(dur,
            BeeUtils.addName("arr size", arr.length()));

        if (cc > 0)
          BeeKeeper.getUi().updateActivePanel(BeeGlobal.createGrid(cc, arr));
        else
          for (int i = 0; i < arr.length(); i++)
            BeeKeeper.getLog().log(arr.get(i));
      }

      BeeKeeper.getLog().addSeparator();

    } else
      BeeKeeper.getLog().log("response status", resp.getStatusCode(),
          resp.getStatusText());
  }

}
