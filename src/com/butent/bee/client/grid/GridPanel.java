package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.GridInterceptor;

import java.util.EnumSet;

public class GridPanel extends Simple implements HasEnabled {

  private final String gridName;
  private GridFactory.GridOptions gridOptions;

  private Presenter presenter = null;
  private GridInterceptor gridInterceptor = null;

  public GridPanel(String gridName, GridFactory.GridOptions gridOptions) {
    super();
    this.gridName = gridName;
    this.gridOptions = gridOptions;

    addStyleName("bee-GridPanel");
  }

  public GridFactory.GridOptions getGridOptions() {
    return gridOptions;
  }

  @Override
  public String getIdPrefix() {
    return "grid-panel";
  }

  public Presenter getPresenter() {
    return presenter;
  }

  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getMainView().isEnabled();
  }

  @Override
  public void onResize() {
  }

  public void setEnabled(boolean enabled) {
    if (getPresenter() != null) {
      getPresenter().getMainView().setEnabled(enabled);
    }
  }

  public void setGridInterceptor(GridInterceptor gridInterceptor) {
    this.gridInterceptor = gridInterceptor;
  }

  public void setGridOptions(GridFactory.GridOptions gridOptions) {
    this.gridOptions = gridOptions;
  }

  @Override
  public void setWidget(Widget w) {
    if (w != null) {
      StyleUtils.makeAbsolute(w);
    }
    super.setWidget(w);
  }
  
  @Override
  protected void onLoad() {
    super.onLoad();
    if (getPresenter() != null) {
      return;
    }

    GridInterceptor gcb = getGridInterceptor();
    if (gcb == null) {
      gcb = GridFactory.getGridInterceptor(getGridName());
    }

    GridFactory.createGrid(getGridName(), gcb, EnumSet.of(UiOption.EMBEDDED), getGridOptions(),
        new PresenterCallback() {
          public void onCreate(Presenter gp) {
            if (gp != null) {
              setPresenter(gp);
              setWidget(gp.getWidget());
              gp.setEventSource(getId());
            }
          }
        });
  }

  private GridInterceptor getGridInterceptor() {
    return gridInterceptor;
  }

  private String getGridName() {
    return gridName;
  }

  private void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }
}
