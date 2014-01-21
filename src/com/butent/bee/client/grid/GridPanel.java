package com.butent.bee.client.grid;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.HasFosterParent;
import com.butent.bee.client.ui.UiOption;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.EnumSet;

public class GridPanel extends Simple implements HasEnabled, HasFosterParent,
    ParentRowEvent.Handler {

  private final String gridName;
  private GridFactory.GridOptions gridOptions;

  private Presenter presenter;
  private GridInterceptor gridInterceptor;

  private String parentId;
  private HandlerRegistration parentRowReg;

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

  @Override
  public String getParentId() {
    return parentId;
  }

  public Presenter getPresenter() {
    return presenter;
  }

  @Override
  public boolean isEnabled() {
    if (getPresenter() == null) {
      return false;
    }
    return getPresenter().getMainView().isEnabled();
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    if (getGridInterceptor() != null) {
      getGridInterceptor().onParentRow(event);
    }
  }

  @Override
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
  public void setParentId(String parentId) {
    this.parentId = parentId;
    if (isAttached()) {
      register();
    }
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

    GridInterceptor gic = getGridInterceptor();
    if (gic == null) {
      gic = GridFactory.getGridInterceptor(getGridName());
    }

    GridFactory.createGrid(getGridName(), GridFactory.getSupplierKey(getGridName(), gic), gic,
        EnumSet.of(UiOption.EMBEDDED), getGridOptions(),
        new PresenterCallback() {
          @Override
          public void onCreate(Presenter gp) {
            if (gp != null) {
              setPresenter(gp);
              setWidget(gp.getWidget());
              gp.setEventSource(getId());
              register();
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

  private HandlerRegistration getParentRowReg() {
    return parentRowReg;
  }

  private void register() {
    unregister();
    if (!BeeUtils.isEmpty(getParentId())) {
      setParentRowReg(BeeKeeper.getBus().registerParentRowHandler(getParentId(), this, false));
    }
  }

  private void setParentRowReg(HandlerRegistration parentRowReg) {
    this.parentRowReg = parentRowReg;
  }

  private void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  private void unregister() {
    if (getParentRowReg() != null) {
      getParentRowReg().removeHandler();
      setParentRowReg(null);
    }
  }
}
