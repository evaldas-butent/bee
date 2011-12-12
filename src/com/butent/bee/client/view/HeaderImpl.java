package com.butent.bee.client.view;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.BeeCommand;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.HasId;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implements styling and user command capture for data headers.
 */

public class HeaderImpl extends Complex implements HeaderView {

  /**
   * Specifies which styling resources to use for a data header implementation.
   */

  public interface Resources extends ClientBundle {
    @Source("HeaderImpl.css")
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
  
  private static final int HEIGHT = 22;

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

  private final InlineLabel captionWidget = new InlineLabel();
  private final InlineLabel messageWidget = new InlineLabel();

  private boolean enabled = true;
  private final Map<Action, String> actionControls = Maps.newHashMap();

  public HeaderImpl() {
    super();
  }
  
  public void addCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.addStyleName(style);
  }

  public void create(String caption, boolean hasData, boolean readOnly,
      Collection<UiOption> options, Set<Action> enabledActions, Set<Action> disabledActions) {
    Style style = getDefaultStyle();
    addStyleName(StyleUtils.WINDOW_HEADER);
    addStyleName(style.container());
    
    boolean isWindow = UiOption.isWindow(options);
    
    captionWidget.setText(BeeUtils.trim(caption));
    if (isWindow) {
      captionWidget.addStyleName(StyleUtils.WINDOW_CAPTION);
    }
    messageWidget.addStyleName(StyleUtils.WINDOW_MESSAGE);
    
    Flow panel = new Flow();
    panel.add(captionWidget);
    panel.add(messageWidget);
    addLeftTop(panel, style.captionLeft(), style.captionTop());
    
    boolean hasClose = hasAction(Action.CLOSE, isWindow, enabledActions, disabledActions);
    
    int x = hasClose ? style.controlsRight() : style.closeRight();
    int y = style.controlTop();
    int w = style.controlWidth();

    String cst = style.control();
    
    if (hasAction(Action.CONFIGURE, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().configure(), Action.CONFIGURE, cst), x, y);
      x += w;
    }

    if (hasAction(Action.SAVE, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().save(), Action.SAVE, cst), x, y);
      x += w;
    }
    if (hasAction(Action.EDIT, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().edit(), Action.EDIT, cst), x, y);
      x += w;
    }

    if (hasAction(Action.BOOKMARK, isWindow, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().bookmarkAdd(), Action.BOOKMARK, cst), x, y);
      x += w;
    }

    if (hasAction(Action.DELETE, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().editDelete(), Action.DELETE, cst), x, y);
      x += w;
    }
    if (hasAction(Action.ADD, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().editAdd(), Action.ADD, cst), x, y);
      x += w;
    }

    if (hasAction(Action.REFRESH, hasData, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().reload(), Action.REFRESH, cst), x, y);
      x += w;
    }
    
    if (hasData) {
      BeeImage loadingIndicator = new BeeImage(Global.getImages().loading());
      setLoadingIndicatorId(loadingIndicator.getId());
      loadingIndicator.addStyleName(style.loadingIndicator());
      addRightTop(loadingIndicator,
          x + style.loadingIndicatorRightMargin(), style.loadingIndicatorTop());
    }
    
    if (hasClose) {
      addRightTop(createControl(Global.getImages().close(), Action.CLOSE, style.close()),
          style.closeRight(), style.closeTop());
    }
  }
  
  public String getCaption() {
    return captionWidget.getText();
  }

  public int getHeight() {
    return HEIGHT;
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
    if (!isAttached() || BeeUtils.isEmpty(getLoadingIndicatorId())) {
      return;
    }
    Assert.notNull(event);

    if (LoadingStateChangeEvent.LoadingState.LOADED.equals(event.getLoadingState())) {
      StyleUtils.hideDisplay(getLoadingIndicatorId());
    } else {
      StyleUtils.unhideDisplay(getLoadingIndicatorId());
    }
  }

  public void removeCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.removeStyleName(style);
  }

  public void setCaption(String caption) {
    captionWidget.setText(BeeUtils.trim(caption));
  }

  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;

    for (int i = 0; i < getWidgetCount(); i++) {
      Widget child = getWidget(i);
      if (child instanceof HasId
          && BeeUtils.containsSame(getActionControls().values(), ((HasId) child).getId())) {
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

  public void setMessage(String message) {
    messageWidget.setText(BeeUtils.trim(message));
  }

  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  public void showAction(Action action, boolean visible) {
    Assert.notNull(action);
    String widgetId = getActionControls().get(action);
    if (BeeUtils.isEmpty(widgetId)) {
      if (visible) {
        BeeKeeper.getLog().warning("showAction:", action.name(), "widget not found");
      }
      return;
    }
    
    if (visible) {
      StyleUtils.unhideDisplay(widgetId);
    } else {
      StyleUtils.hideDisplay(widgetId);
    }
  }
  
  private Widget createControl(ImageResource image, Action action, String styleName) {
    BeeImage control = new BeeImage(image, new ActionListener(action));
    if (!BeeUtils.isEmpty(styleName)) {
      control.addStyleName(styleName);
    }

    if (action != null) {
      getActionControls().put(action, control.getId());
    }
    return control;
  }

  private Map<Action, String> getActionControls() {
    return actionControls;
  }

  private String getLoadingIndicatorId() {
    return loadingIndicatorId;
  }
  
  private boolean hasAction(Action action, boolean def,
      Set<Action> enabledActions, Set<Action> disabledActions) {
    if (def) {
      return !BeeUtils.contains(disabledActions, action);
    } else {
      return BeeUtils.contains(enabledActions, action);
    }
  }

  private void setLoadingIndicatorId(String loadingIndicatorId) {
    this.loadingIndicatorId = loadingIndicatorId;
  }
}
