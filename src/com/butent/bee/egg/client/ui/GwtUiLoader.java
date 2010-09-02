package com.butent.bee.egg.client.ui;

import java.util.ArrayList;
import java.util.List;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.ui.UiLoader;
import com.google.gwt.core.client.JsArrayString;

public class GwtUiLoader extends UiLoader {

  @Override
  protected List<UiRow> getFormData(String formName, Object... params) {
    int c = (Integer) params[0];
    JsArrayString data = (JsArrayString) params[1];

    Assert.parameterCount(params.length, 2, 2);

    List<UiRow> res = new ArrayList<UiRow>();

    for (int row = 1; row < data.length() / c; row++) {
      String id = data.get(row * c + 0);
      String cls = data.get(row * c + 1);
      String parent = data.get(row * c + 2);
      String props = data.get(row * c + 3);

      res.add(new UiRow(id, cls, parent, props));
    }
    return res;
  }
}
