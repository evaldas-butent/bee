package com.butent.bee.client.view;

import com.google.common.collect.Maps;
import com.google.gwt.dom.client.Element;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.Global;
import com.butent.bee.client.Settings;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.layout.Horizontal;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.utils.Command;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Captions;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implements styling and user command capture for data headers.
 */

public class HeaderSilverImpl extends Flow implements HeaderView {

  private final class ActionListener extends Command {
    private final Action action;
    private long lastTime;

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

  private static final String STYLE_CONTAINER = "bee-Header-container";

  private static final String STYLE_CAPTION = "bee-Header-caption";
  private static final String STYLE_MESSAGE = "bee-Header-message";

  private static final String STYLE_COMMAND_PANEL = "bee-Header-commandPanel";

  private static final String STYLE_CONTROL = "bee-Header-control";

  private static final int ACTION_SENSITIVITY_MILLIS =
      BeeUtils.positive(Settings.getActionSensitivityMillis(), 300);

  private Presenter viewPresenter;

  private final Label captionWidget = new Label();
  private final Label messageWidget = new Label();

  private boolean enabled = true;

  private final Map<Action, String> actionControls = Maps.newHashMap();

  private final Horizontal commandPanel = new Horizontal();

  public HeaderSilverImpl() {
    super();
  }

  @Override
  public void addCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.addStyleName(style);
  }

  @Override
  public void addCommandItem(IdentifiableWidget widget) {
    getCommandPanel().add(widget);
  }

  @Override
  public void clearCommandPanel() {
    getCommandPanel().clear();
  }

  @Override
  public void create(String caption, boolean hasData, boolean readOnly,
      Collection<UiOption> options, Set<Action> enabledActions, Set<Action> disabledActions,
      Set<Action> hiddenActions) {

    addStyleName(STYLE_CONTAINER);

    captionWidget.addStyleName(STYLE_CAPTION);
    if (Captions.isCaption(caption)) {
      setCaption(caption);
    }
    add(captionWidget);

    messageWidget.addStyleName(STYLE_MESSAGE);
    add(messageWidget);

    commandPanel.addStyleName(STYLE_COMMAND_PANEL);
    add(commandPanel);

    if (hasAction(Action.REFRESH, hasData, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverReload(), Action.REFRESH, hiddenActions));
    }

    if (hasAction(Action.FILTER, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverFilter(), Action.FILTER, hiddenActions));
    }
    if (hasAction(Action.REMOVE_FILTER, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().closeSmallRed(), Action.REMOVE_FILTER, hiddenActions));
    }

    if (hasAction(Action.ADD, hasData && !readOnly, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverAdd(), Action.ADD, hiddenActions));
    }
    if (hasAction(Action.DELETE, hasData && !readOnly, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverDelete(), Action.DELETE, hiddenActions));
    }

    if (hasAction(Action.BOOKMARK, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverBookmarkAdd(), Action.BOOKMARK, hiddenActions));
    }

    if (hasAction(Action.EDIT, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverEdit(), Action.EDIT, hiddenActions));
    }
    if (hasAction(Action.SAVE, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverSave(), Action.SAVE, hiddenActions));
    }

    if (hasAction(Action.CONFIGURE, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverConfigure(), Action.CONFIGURE, hiddenActions));
    }

    if (hasAction(Action.AUDIT, false, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverChatIcon(), Action.AUDIT, hiddenActions));
    }

    if (hasAction(Action.PRINT, true, enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverPrint(), Action.PRINT, hiddenActions));
    }

    if (hasAction(Action.CLOSE, UiOption.isWindow(options), enabledActions, disabledActions)) {
      add(createControl(Global.getImages().silverClose(), Action.CLOSE, hiddenActions));
    }
  }

  @Override
  public String getCaption() {
    return captionWidget.getHtml();
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
  public boolean hasCommands() {
    return !commandPanel.isEmpty();
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
    if (DomUtils.isButtonElement(source)) {
      return false;
    } else {
      String id = source.getId();
      return BeeUtils.isEmpty(id) ? true : !actionControls.containsValue(id);
    }
  }

  @Override
  public void removeCaptionStyle(String style) {
    Assert.notEmpty(style);
    captionWidget.removeStyleName(style);
  }

  @Override
  public void setCaption(String caption) {
    captionWidget.setHtml(BeeUtils.trim(Localized.maybeTranslate(caption)));
  }

  @Override
  public void setCaptionTitle(String title) {
    captionWidget.setTitle(title);
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
    messageWidget.setHtml(BeeUtils.trim(message));
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

  private Widget createControl(ImageResource image, Action action, Set<Action> hiddenActions) {
    Image control = new Image(image, new ActionListener(action));
    control.addStyleName(STYLE_CONTROL);

    if (action != null) {
      control.addStyleName(action.getStyleName());
      control.setTitle(action.getCaption());

      if (hiddenActions != null && hiddenActions.contains(action)) {
        StyleUtils.hideDisplay(control);
      }

      getActionControls().put(action, control.getId());
    }
    return control;
  }

  private Map<Action, String> getActionControls() {
    return actionControls;
  }

  private Horizontal getCommandPanel() {
    return commandPanel;
  }

  private static boolean hasAction(Action action, boolean def,
      Set<Action> enabledActions, Set<Action> disabledActions) {
    if (def) {
      return !BeeUtils.contains(disabledActions, action);
    } else {
      return BeeUtils.contains(enabledActions, action);
    }
  }
}
