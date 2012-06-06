package com.butent.bee.client.view;

import com.google.common.collect.Maps;
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

public class HeaderSilverImpl extends Complex implements HeaderView {

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
  
  private static final int HEIGHT = 35;

  private static final int CAPTION_LEFT = 8;
  private static final int CAPTION_TOP = 1;

  private static final int CONTROL_WIDTH = 43;
  private static final int CONTROL_TOP = 2;

  private static final int CONTROLS_RIGHT = 43;

  private static final int CLOSE_RIGHT = 2;
  private static final int CLOSE_TOP = 2;
  
  private static final int LOADING_INDICATOR_TOP = 2;
  private static final int LOADING_INDICATOR_RIGHT_MARGIN = 48;
  
  private static final String STYLE_CONTAINER = "bee-Header-container";

  private static final String STYLE_CAPTION = "bee-Header-caption";
  private static final String STYLE_MESSAGE = "bee-Header-message";

  private static final String STYLE_LOADING_INDICATOR = "bee-Header-loadingIndicator";
  private static final String STYLE_CONTROL = "bee-Header-control";
  private static final String STYLE_CLOSE = "bee-Header-close";
  
  private Presenter viewPresenter = null;

  private String loadingIndicatorId = null;

  private final InlineLabel captionWidget = new InlineLabel();
  private final InlineLabel messageWidget = new InlineLabel();

  private boolean enabled = true;
  private final Map<Action, String> actionControls = Maps.newHashMap();
  

  public HeaderSilverImpl() {
    super();
  }
  
  public void addCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.addStyleName(style);
  }

  public void create(String caption, boolean hasData, boolean readOnly,
      Collection<UiOption> options, Set<Action> enabledActions, Set<Action> disabledActions) {
    addStyleName(StyleUtils.WINDOW_HEADER);
    addStyleName(STYLE_CONTAINER);
    
    boolean isWindow = UiOption.isWindow(options);
    
    captionWidget.setText(BeeUtils.trim(caption));
    captionWidget.addStyleName(STYLE_CAPTION);
    if (isWindow) {
      captionWidget.addStyleName(StyleUtils.WINDOW_CAPTION);
    }

    messageWidget.addStyleName(STYLE_MESSAGE);
    
    Flow panel = new Flow();
    panel.add(captionWidget);
    panel.add(messageWidget);
    addLeftTop(panel, CAPTION_LEFT, CAPTION_TOP);
    
    boolean hasClose = hasAction(Action.CLOSE, isWindow, enabledActions, disabledActions);
        
    int x = hasClose ? CONTROLS_RIGHT : CLOSE_RIGHT;
    int y = CONTROL_TOP;
    int w = CONTROL_WIDTH;

    if (hasAction(Action.CONFIGURE, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().configure(), Action.CONFIGURE, STYLE_CONTROL),
          x, y);
      x += w;
    }

    if (hasAction(Action.SAVE, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().save(), Action.SAVE, STYLE_CONTROL), x, y);
      x += w;
    }
    if (hasAction(Action.EDIT, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().edit(), Action.EDIT, STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.BOOKMARK, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().bookmarkAdd(), Action.BOOKMARK, STYLE_CONTROL),
          x, y);
      x += w;
    }

    if (hasAction(Action.DELETE, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().editDelete(), Action.DELETE, STYLE_CONTROL),
          x, y);
      x += w;
    }
    if (hasAction(Action.ADD, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().editAdd(), Action.ADD, STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.REFRESH, hasData, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().reload(), Action.REFRESH, STYLE_CONTROL), x, y);
      x += w;
    }
    
    if (hasData) {
      BeeImage loadingIndicator = new BeeImage(Global.getImages().loading());
      setLoadingIndicatorId(loadingIndicator.getId());
      loadingIndicator.addStyleName(STYLE_LOADING_INDICATOR);
      addRightTop(loadingIndicator, x + LOADING_INDICATOR_RIGHT_MARGIN, LOADING_INDICATOR_TOP);
    }
    
    if (hasClose) {
      addRightTop(createControl(Global.getImages().headerClose(), Action.CLOSE, STYLE_CLOSE),
          CLOSE_RIGHT, CLOSE_TOP);
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

  public boolean hasAction(Action action) {
    if (action == null) {
      return false;
    } else {
      return getActionControls().containsKey(action);
    }
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
