package com.butent.bee.egg.client.grid;

import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.BeeKeeper;
import com.butent.bee.egg.client.data.JsData;
import com.butent.bee.egg.client.utils.BeeDuration;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeDate;
import com.butent.bee.egg.shared.data.BeeColumn;
import com.butent.bee.egg.shared.data.BeeView;
import com.butent.bee.egg.shared.data.StringData;
import com.butent.bee.egg.shared.utils.BeeUtils;

public class BeeGrid {

  public BeeGrid() {
  }

  public Widget createGrid(int c, JsArrayString data) {
    Assert.isPositive(c);
    Assert.notNull(data);

    int len = data.length();
    Assert.isTrue(len >= c * 2);

    boolean debug = BeeGlobal.isDebug();
    BeeDuration dur = null;

    String[] head = new String[c];
    BeeColumn z = new BeeColumn();

    for (int i = 0; i < c; i++) {
      z.deserialize(data.get(i));
      head[i] = z.getName();
    }

    int r = len / c - 1;
    String foot = BeeUtils.concat(1, r, '*', c, '=', len - c);

    BeeCellTable table = new BeeCellTable(r);

    if (debug) {
      dur = new BeeDuration("init data " + r);
    }
    table.initData(r);
    if (debug) {
      BeeKeeper.getLog().finish(dur);
      dur.restart("add cols " + c);
    }

    BeeView view = new JsData(data, c, c);

    for (int j = 0; j < c; j++) {
      String s = BeeUtils.iif(j == 0, foot, j == c - 1, new BeeDate().toLog(),
          BeeUtils.bracket(j + 1));

      table.addColumn(new BeeTextColumn(view, j), head[j], s);
    }

    if (debug) {
      BeeKeeper.getLog().finish(dur);
    }

    return table;
  }

  public Widget simpleGrid(String[] colNames, Object data) {
    Assert.notNull(colNames);
    Assert.notNull(data);

    int c = colNames.length;
    Assert.isPositive(c);

    int r = BeeConst.SIZE_UNKNOWN;
    BeeView view = null;

    if (data instanceof String[][]) {
      r = ((String[][]) data).length;
      view = new StringData((String[][]) data);
    } else if (data instanceof JsArrayString) {
      r = ((JsArrayString) data).length() / c;
      view = new JsData((JsArrayString) data, c);
    }

    Assert.isPositive(r);
    Assert.notNull(view);

    BeeCellTable table = new BeeCellTable(r);
    table.initData(r);

    for (int j = 0; j < c; j++) {
      table.addColumn(new BeeTextColumn(view, j), colNames[j]);
    }

    return table;
  }
}