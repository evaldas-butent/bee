package com.butent.bee.egg.client.dialog;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.egg.client.BeeGlobal;
import com.butent.bee.egg.client.grid.BeeCellTable;
import com.butent.bee.egg.client.layout.BeeVertical;
import com.butent.bee.egg.client.utils.BeeDom;
import com.butent.bee.egg.client.widget.BeeLabel;
import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Iterator;

public class BeeMessageBox {

  public boolean close(Object src) {
    boolean ok = false;

    if (src instanceof Widget) {
      PopupPanel p = BeeDom.parentPopup((Widget) src);

      if (p != null) {
        p.hide();
        ok = true;
      }
    }

    return ok;
  }

  public void showError(Object... x) {
    showInfo(x);
  }

  public void showGrid(String cap, String[] cols, Object data) {
    Assert.notEmpty(cols);
    Assert.notNull(data);

    showInfo(cap, BeeGlobal.createSimpleGrid(cols, data));
  }

  public void showInfo(Object... x) {
    int n = x.length;
    Assert.parameterCount(n, 1);

    BeeVertical vp = new BeeVertical();

    for (int i = 0; i < n; i++) {
      if (x[i] instanceof Widget) {
        vp.add((Widget) x[i]);
        if (x[i] instanceof BeeCellTable) {
          vp.setCellHeight((Widget) x[i], "200px");
          vp.setCellWidth((Widget) x[i], "400px");
        }
      } else if (x[i] instanceof String) {
        vp.add(new BeeLabel((String) x[i]));
      } else if (x[i] instanceof Collection) {
        for (Iterator<?> iter = ((Collection<?>) x[i]).iterator(); iter.hasNext();) {
          vp.add(new BeeLabel(iter.next()));
        }
      } else if (BeeUtils.isArray(x[i])) {
        for (int j = 0; j < BeeUtils.arrayLength(x[i]); j++) {
          vp.add(new BeeLabel(BeeUtils.arrayGet(x[i], j)));
        }
      } else if (x[i] != null) {
        vp.add(new BeeLabel(x[i]));
      }
    }

    BeeCloseButton b = new BeeCloseButton("ok");

    vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    vp.add(b);

    BeePopupPanel box = new BeePopupPanel();
    box.setAnimationEnabled(true);

    box.setWidget(vp);

    box.center();
    b.setFocus(true);
  }

}
