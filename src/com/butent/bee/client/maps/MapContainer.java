package com.butent.bee.client.maps;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.HeaderImpl;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.View;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.NameUtils;

import java.util.EnumSet;

public class MapContainer extends Flow implements Presenter, View {

  private static final BeeLogger logger = LogUtils.getLogger(MapContainer.class);

  private static final String STYLE_NAME = "bee-MapContainer";

  private final HeaderView headerView;

  private boolean enabled = true;

  public MapContainer(String caption, MapWidget mapWidget) {
    super(STYLE_NAME);

    this.headerView = new HeaderImpl();
    headerView.create(caption, false, true, null, EnumSet.of(UiOption.ROOT),
        EnumSet.of(Action.CLOSE), Action.NO_ACTIONS, Action.NO_ACTIONS);

    headerView.setViewPresenter(this);
    add(headerView);
    
    add(mapWidget);
  }

  @Override
  public String getCaption() {
    return headerView.getCaption();
  }

  @Override
  public String getEventSource() {
    return null;
  }

  @Override
  public HeaderView getHeader() {
    return headerView;
  }

  @Override
  public String getIdPrefix() {
    return "map-container";
  }

  @Override
  public View getMainView() {
    return this;
  }

  @Override
  public Presenter getViewPresenter() {
    return this;
  }

  @Override
  public IdentifiableWidget getWidget() {
    return this;
  }

  @Override
  public String getWidgetId() {
    return getId();
  }

  @Override
  public void handleAction(Action action) {
    switch (action) {
      case CANCEL:
      case CLOSE:
        BeeKeeper.getScreen().closeWidget(this);
        break;

      default:
        logger.warning(NameUtils.getName(this), action, "not implemented");
    }
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void onViewUnload() {
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public void setEventSource(String eventSource) {
  }

  @Override
  public void setViewPresenter(Presenter viewPresenter) {
  }
}
