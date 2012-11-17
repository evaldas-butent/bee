package com.butent.bee.client.view;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.dom.StyleUtils;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.layout.Complex;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implements styling and user command capture for data headers.
 */

public class HeaderSilverImpl extends Complex implements HeaderView {

  private class ActionListener extends Command {
    private final Action action;
    private long lastTime = 0L;

    private ActionListener(Action action) {
      super();
      this.action = action;
    }

    @Override
    public void execute() {
      if (getViewPresenter() != null) {
        long now = System.currentTimeMillis();
        long last = getLastTime();
        setLastTime(now);

        if (now - last >= HeaderSilverImpl.ACTION_SENSITIVITY_MILLIS) {
          getViewPresenter().handleAction(action);
        }
      }
    }

    private long getLastTime() {
      return lastTime;
    }

    private void setLastTime(long lastTime) {
      this.lastTime = lastTime;
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(HeaderSilverImpl.class);
  
  private static final int HEIGHT = 30;

  private static final int CAPTION_LEFT = 8;
  private static final int CAPTION_TOP = 5;

  private static final int CONTROL_WIDTH = 43;
  private static final int CONTROL_TOP = 1;

  private static final int CONTROLS_RIGHT = 43;

  private static final int CLOSE_RIGHT = 2;
  private static final int CLOSE_TOP = 1;

  private static final String STYLE_CONTAINER = "bee-Header-container";

  private static final String STYLE_CAPTION = "bee-Header-caption";
  private static final String STYLE_MESSAGE = "bee-Header-message";

  private static final String STYLE_CONTROL = "bee-Header-control";
  private static final String STYLE_CLOSE = "bee-Header-close";

  private static final int ACTION_SENSITIVITY_MILLIS =
      BeeUtils.positive(Settings.getActionSensitivityMillis(), 300);

  private Presenter viewPresenter = null;

  private final InlineLabel captionWidget = new InlineLabel();
  private final InlineLabel messageWidget = new InlineLabel();

  private boolean enabled = true;

  private final Map<Action, String> actionControls = Maps.newHashMap();

  public HeaderSilverImpl() {
    super();
  }

  @Override
  public void addCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.addStyleName(style);
  }

  @Override
  public void create(String caption, boolean hasData, boolean readOnly,
      Collection<UiOption> options, Set<Action> enabledActions, Set<Action> disabledActions) {
    addStyleName(StyleUtils.WINDOW_HEADER);
    addStyleName(STYLE_CONTAINER);

    boolean isWindow = UiOption.isWindow(options);

    setCaption(caption);
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
      addRightTop(createControl(Global.getImages().silverConfigure(), Action.CONFIGURE,
          STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.SAVE, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverSave(), Action.SAVE, STYLE_CONTROL), x, y);
      x += w;
    }
    if (hasAction(Action.EDIT, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverEdit(), Action.EDIT, STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.BOOKMARK, false, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverBookmarkAdd(), Action.BOOKMARK,
          STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.PRINT, true, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverPrint(), Action.PRINT, STYLE_CONTROL),
          x, y);
      x += w;
    }

    if (hasAction(Action.DELETE, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverDelete(), Action.DELETE, STYLE_CONTROL),
          x, y);
      x += w;
    }
    if (hasAction(Action.ADD, hasData && !readOnly, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverAdd(), Action.ADD, STYLE_CONTROL), x, y);
      x += w;
    }

    if (hasAction(Action.REFRESH, hasData, enabledActions, disabledActions)) {
      addRightTop(createControl(Global.getImages().silverReload(), Action.REFRESH, STYLE_CONTROL),
          x, y);
      x += w;
    }

    if (hasClose) {
      addRightTop(createControl(Global.getImages().silverClose(), Action.CLOSE, STYLE_CLOSE),
          CLOSE_RIGHT, CLOSE_TOP);
    }
  }

  @Override
  public String getCaption() {
    return captionWidget.getText();
  }

  @Override
  public int getHeight() {
    return HEIGHT;
  }

  @Override
  public Element getPrintElement() {
    return getElement();
  }

  @Override
  public Presenter getViewPresenter() {
    return viewPresenter;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public boolean hasAction(Action action) {
    if (action == null) {
      return false;
    } else {
      return getActionControls().containsKey(action);
    }
  }

  @Override
  public boolean isActionEnabled(Action action) {
    if (action == null || !isEnabled()) {
      return false;
    }
    String id = getActionControls().get(action);
    if (BeeUtils.isEmpty(id)) {
      return false;
    }
    
    Widget child = DomUtils.getChildQuietly(this, id);
    if (child instanceof HasEnabled) {
      return child.isVisible() && ((HasEnabled) child).isEnabled();
    } else {
      return false;
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public boolean onPrint(Element source, Element target) {
    String id = source.getId();
    return BeeUtils.isEmpty(id) ? true : !actionControls.containsValue(id);
  }

  @Override
  public void removeCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.removeStyleName(style);
  }

  @Override
  public void setCaption(String caption) {
    String text =
        BeeUtils.isEmpty(caption) ? BeeConst.STRING_EMPTY : LocaleUtils.maybeLocalize(caption);
    captionWidget.setText(text);
  }

  @Override
  public void setEnabled(boolean enabled) {
    if (enabled == isEnabled()) {
      return;
    }
    this.enabled = enabled;

    for (int i = 0; i < getWidgetCount(); i++) {
      Widget child = getWidget(i);
      String id = DomUtils.getId(child);

      if (BeeUtils.containsSame(getActionControls().values(), id)) {
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

  @Override
  public void setMessage(String message) {
    messageWidget.setText(BeeUtils.trim(message));
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
    this.viewPresenter = viewPresenter;
  }

  @Override
  public void showAction(Action action, boolean visible) {
    Assert.notNull(action);
    String widgetId = getActionControls().get(action);
    if (BeeUtils.isEmpty(widgetId)) {
      if (visible) {
        logger.warning("showAction:", action.name(), "widget not found");
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
      control.setTitle(action.getCaption());
      getActionControls().put(action, control.getId());
    }
    return control;
  }

  private Map<Action, String> getActionControls() {
    return actionControls;
  }

  private boolean hasAction(Action action, boolean def,
      Set<Action> enabledActions, Set<Action> disabledActions) {
    if (def) {
      return !BeeUtils.contains(disabledActions, action);
    } else {
      return BeeUtils.contains(enabledActions, action);
    }
  }
}
