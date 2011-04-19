package com.butent.bee.client.view;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.BeeLayoutPanel;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;

public class DataHeaderImpl extends BeeLayoutPanel implements DataHeaderView {
  private Presenter presenter = null;

  public DataHeaderImpl() {
    super();
  }

  public void create(String caption) {
    addStyleName(StyleUtils.WINDOW_HEADER);
    int x = 2;
    int y = 2;
    int w = 32;

    addRightWidthTop(new BeeImage(Global.getImages().close()), x, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().configure()), x += w * 2, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().save()), x += w, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().bookmarkAdd()), x += w, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().editDelete()), x += w, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().editAdd()), x += w, w, y);
    addRightWidthTop(new BeeImage(Global.getImages().reload()), x += w, w, y);

    BeeLabel label = new BeeLabel(caption);
    label.addStyleName(StyleUtils.WINDOW_CAPTION);
    addLeftWidthTop(label, 16, 200, 0);
  }

  public Presenter getViewPresenter() {
    return presenter;
  }

  public void setViewPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
