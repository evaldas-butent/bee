package com.butent.bee.client.view;

import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.presenter.Action;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.BeeLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Set;

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

    String loadingIndicator();

    int loadingIndicatorRightMargin();

    int loadingIndicatorTop();
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

  private String loadingIndicatorId = null;

  private final String captionId;

  private boolean enabled = true;
  private final Set<String> actionControls = Sets.newHashSet();

  public DataHeaderImpl() {
    super();
    this.captionId = DomUtils.createUniqueId("caption");
  }

  public void create(String caption, boolean hasData, boolean readOnly) {
    Style style = getDefaultStyle();
    addStyleName(StyleUtils.WINDOW_HEADER);
    addStyleName(style.container());

    BeeLabel label = new BeeLabel(caption);
    label.setId(captionId);
    label.addStyleName(StyleUtils.WINDOW_CAPTION);
    addLeftTop(label, style.captionLeft(), style.captionTop());

    int x = style.controlsRight();
    int y = style.controlTop();
    int w = style.controlWidth();

    String cst = style.control();

    addRightTop(createControl(Global.getImages().configure(), Action.CONFIGURE, cst), x, y);
    addRightTop(createControl(Global.getImages().save(), Action.SAVE, cst), x += w, y);
    addRightTop(createControl(Global.getImages().bookmarkAdd(), Action.BOOKMARK, cst), x += w, y);

    if (hasData) {
      if (!readOnly) {
        addRightTop(createControl(Global.getImages().editDelete(), Action.DELETE, cst), x += w, y);
        addRightTop(createControl(Global.getImages().editAdd(), Action.ADD, cst), x += w, y);
      }
      addRightTop(createControl(Global.getImages().reload(), Action.REFRESH, cst), x += w, y);

      BeeImage loadingIndicator = new BeeImage(Global.getImages().loading());
      setLoadingIndicatorId(loadingIndicator.getId());
      loadingIndicator.addStyleName(style.loadingIndicator());
      addRightTop(loadingIndicator,
          x + style.loadingIndicatorRightMargin(), style.loadingIndicatorTop());
    }

    addRightTop(createControl(Global.getImages().close(), Action.CLOSE, style.close()),
        style.closeRight(), style.closeTop());
  }

  public String getCaption() {
    return DomUtils.getHtml(captionId);
  }

  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  public String getWidgetId() {
    return getId();
  }

  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onLoadingStateChanged(LoadingStateChangeEvent event) {
    if (BeeUtils.isEmpty(getLoadingIndicatorId())) {
      return;
    }
    Assert.notNull(event);

    if (LoadingStateChangeEvent.LoadingState.LOADED.equals(event.getLoadingState())) {
      StyleUtils.hideDisplay(getLoadingIndicatorId());
    } else {
      StyleUtils.unhideDisplay(getLoadingIndicatorId());
    }
  }

  public void setCaption(String caption) {
    DomUtils.setHtml(captionId, BeeUtils.trim(caption));
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;

    for (int i = 0; i < getWidgetCount(); i++) {
      Widget child = getWidget(i);
      if (child instanceof HasId
          && BeeUtils.containsSame(getActionControls(), ((HasId) child).getId())) {
        if (child instanceof HasEnabled) {
          ((HasEnabled) child).setEnabled(enabled);
        }
        if (enabled) {
          StyleUtils.unhideDisplay(child);
        } else {
          StyleUtils.hideDisplay(child);
        }
      }
    }
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  private Widget createControl(ImageResource image, Action action, String styleName) {
    BeeImage control = new BeeImage(image, new ActionListener(action));
    if (!BeeUtils.isEmpty(styleName)) {
      control.addStyleName(styleName);
    }

    if (action != null && action != Action.CLOSE) {
      getActionControls().add(control.getId());
    }
    return control;
  }

  private Set<String> getActionControls() {
    return actionControls;
  }

  private String getLoadingIndicatorId() {
    return loadingIndicatorId;
  }

  private void setLoadingIndicatorId(String loadingIndicatorId) {
    this.loadingIndicatorId = loadingIndicatorId;
  }
}
