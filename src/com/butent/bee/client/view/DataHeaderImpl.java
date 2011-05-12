package com.butent.bee.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.presenter.Action;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.utils.BeeUtils;

/**
 * Implements styling and user command capture for data headers.
 */

public class DataHeaderImpl extends Complex implements DataHeaderView {

  /**
   * Specifies which styling resources to use for a data header implementation.
   */

  public interface Resources extends ClientBundle {
    @Source("DataHeaderImpl.css")
    Style headerStyle();
  }

  /**
   * Specifies which styling aspects have to be implemented on data header implementations.
   */

  public interface Style extends CssResource {
    String caption();

    int captionLeft();

    int captionTop();

    String close();

    int closeRight();

    int closeTop();

    String container();

    String control();

    int controlsRight();

    int controlTop();

    int controlWidth();
  }
  
  private class ActionListener extends BeeCommand {
    private final Action action;

    private ActionListener(Action action) {
      super();
      this.action = action;
    }

    @Override
    public void execute() {
      if (getViewPresenter() != null) {
        getViewPresenter().handleAction(action);
      }
    }
  }
  
  private static Resources defaultResources = null;
  private static Style defaultStyle = null;

  private static Resources getDefaultResources() {
    if (defaultResources == null) {
      defaultResources = GWT.create(Resources.class);
    }
    return defaultResources;
  }

  private static Style getDefaultStyle() {
    if (defaultStyle == null) {
      defaultStyle = getDefaultResources().headerStyle();
      defaultStyle.ensureInjected();
    }
    return defaultStyle;
  }

  private Presenter viewPresenter = null;

  public DataHeaderImpl() {
    super();
  }

  public void create(String caption) {
    Style style = getDefaultStyle();
    addStyleName(StyleUtils.WINDOW_HEADER);
    addStyleName(style.container());

    BeeLabel label = new BeeLabel(caption);
    label.addStyleName(StyleUtils.WINDOW_CAPTION);
    addLeftTop(label, style.captionLeft(), style.captionTop());

    int x = style.controlsRight();
    int y = style.controlTop();
    int w = style.controlWidth();

    String cst = style.control();

    addRightTop(createControl(Global.getImages().configure(), Action.CONFIGURE, cst), x, y);
    addRightTop(createControl(Global.getImages().save(), Action.SAVE, cst), x += w, y);
    addRightTop(createControl(Global.getImages().bookmarkAdd(), Action.BOOKMARK, cst), x += w, y);
    addRightTop(createControl(Global.getImages().editDelete(), Action.DELETE, cst), x += w, y);
    addRightTop(createControl(Global.getImages().editAdd(), Action.ADD, cst), x += w, y);
    addRightTop(createControl(Global.getImages().reload(), Action.REFRESH, cst), x += w, y);

    addRightTop(createControl(Global.getImages().close(), Action.CLOSE, style.close()),
        style.closeRight(), style.closeTop());
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  private Widget createControl(ImageResource image, Action action, String styleName) {
    Widget control = new BeeImage(image, new ActionListener(action));
    if (!BeeUtils.isEmpty(styleName)) {
      control.addStyleName(styleName);
    }
    return control;
  }
}
