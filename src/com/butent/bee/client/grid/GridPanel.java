package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;

import com.butent.bee.client.layout.ResizePanel;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridCallback;

import java.util.EnumSet;

public class GridPanel extends ResizePanel implements HasEnabled {

  private final String gridName;
  private GridFactory.GridOptions gridOptions;

  private GridPresenter presenter = null;
  private GridCallback gridCallback = null;

  public GridPanel(String gridName, GridFactory.GridOptions gridOptions) {
    super();
    this.gridName = gridName;
    this.gridOptions = gridOptions;

    addStyleName("bee-grid-panel");
    setDummyWidget();
  }

  public GridFactory.GridOptions getGridOptions() {
    return gridOptions;
  }

  @Override
  public String getIdPrefix() {
    return "grid-panel";
  }

  public GridPresenter getPresenter() {
    return presenter;
  }

  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getView().isEnabled();
  }

  public void setEnabled(boolean enabled) {
    if (getPresenter() != null) {
      getPresenter().getView().setEnabled(enabled);
    }
  }

  public void setGridCallback(GridCallback gridCallback) {
    this.gridCallback = gridCallback;
  }

  public void setGridOptions(GridFactory.GridOptions gridOptions) {
    this.gridOptions = gridOptions;
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (getPresenter() != null) {
      return;
    }

    GridCallback gcb = getGridCallback();
    if (gcb == null) {
      gcb = GridFactory.getGridCallback(getGridName());
    }

    GridFactory.createGrid(getGridName(), gcb, EnumSet.of(UiOption.EMBEDDED), getGridOptions(),
        new GridFactory.PresenterCallback() {
          public void onCreate(GridPresenter gp) {
            if (gp != null) {
              setPresenter(gp);
              setWidget(gp.getWidget());
              gp.setEventSource(getId());
            }
          }
        });
  }

  private GridCallback getGridCallback() {
    return gridCallback;
  }

  private String getGridName() {
    return gridName;
  }

  private void setPresenter(GridPresenter presenter) {
    this.presenter = presenter;
  }
}
