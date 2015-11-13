package com.butent.bee.client.modules.transport;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.event.logical.ParentRowEvent;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.ReadyForUpdateEvent;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.utils.BeeUtils;

public class TripDriversGrid extends AbstractGridInterceptor implements ClickHandler {

  private final FaLabel main = new FaLabel(FontAwesome.CHECK);
  private final TripForm tripForm;

  public TripDriversGrid(TripForm tripForm) {
    this.tripForm = tripForm;
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    main.setTitle(Localized.getConstants().setAsPrimary());
    main.addClickHandler(this);
    presenter.getHeader().addCommandItem(main);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public void afterInsertRow(IsRow result) {
    IsRow tripRow = tripForm.getActiveRow();

    if (BeeUtils.isEmpty(tripRow.getString(tripForm.getDataIndex(COL_MAIN_DRIVER)))) {
      tripForm.setMainDriver(tripRow, result.getId());
    }
    super.afterInsertRow(result);
  }

  @Override
  public GridInterceptor getInstance() {
    return null;
  }

  @Override
  public void onClick(ClickEvent clickEvent) {
    IsRow row = getGridView().getActiveRow();

    if (row == null) {
      getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }
    tripForm.setMainDriver(tripForm.getActiveRow(), row.getId());
  }

  @Override
  public void onParentRow(ParentRowEvent event) {
    main.setVisible(DataUtils.isId(event.getRowId()));
    super.onParentRow(event);
  }

  @Override
  public void onReadyForInsert(GridView gridView, ReadyForInsertEvent event) {
    int i = 0;

    for (BeeColumn column : event.getColumns()) {
      if (BeeUtils.same(column.getId(), COL_DRIVER)) {
        event.consume();
        tripForm.checkDriver(gridView, event, BeeUtils.toLongOrNull(event.getValues().get(i)));
        return;
      }
      i++;
    }
    super.onReadyForInsert(gridView, event);
  }

  @Override
  public void onReadyForUpdate(GridView gridView, ReadyForUpdateEvent event) {
    if (BeeUtils.same(event.getColumn().getId(), COL_DRIVER)) {
      event.consume();
      tripForm.checkDriver(gridView, event, BeeUtils.toLongOrNull(event.getNewValue()));
      return;
    }
    super.onReadyForUpdate(gridView, event);
  }
}
