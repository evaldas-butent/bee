package com.butent.bee.client.modules.transport;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.AbstractCellRenderer;
import com.butent.bee.client.render.FileLinkRenderer;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.GridInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.transport.TransportConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.List;

class CargoRequestFilesGrid extends AbstractGridInterceptor {

  private FileCollector collector;

  CargoRequestFilesGrid() {
  }

  @Override
  public boolean beforeAction(Action action, final GridPresenter presenter) {
    if (Action.ADD.equals(action)) {
      if (collector != null) {
        collector.clickInput();
      }
      return false;

    } else {
      return super.beforeAction(action, presenter);
    }
  }

  @Override
  public GridInterceptor getInstance() {
    return new CargoRequestFilesGrid();
  }

  @Override
  public AbstractCellRenderer getRenderer(String columnName,
      List<? extends IsColumn> dataColumns, ColumnDescription columnDescription,
      CellSource cellSource) {

    if (BeeUtils.same(columnName, TransportConstants.COL_CRF_FILE)) {
      return new FileLinkRenderer(DataUtils.getColumnIndex(columnName, dataColumns),
          DataUtils.getColumnIndex(TransportConstants.COL_CRF_CAPTION, dataColumns),
          DataUtils.getColumnIndex(CommonsConstants.COL_FILE_NAME, dataColumns));

    } else {
      return super.getRenderer(columnName, dataColumns, columnDescription, cellSource);
    }
  }

  @Override
  public void onAttach(final GridView gridView) {
    if (collector == null) {
      collector = FileCollector.headless(new Consumer<Collection<NewFileInfo>>() {
        @Override
        public void accept(final Collection<NewFileInfo> input) {
          if (!BeeUtils.isEmpty(input)) {
            gridView.ensureRelId(new IdCallback() {
              @Override
              public void onSuccess(Long result) {
                SelfServiceUtils.sendFiles(result, input, new ScheduledCommand() {
                  @Override
                  public void execute() {
                    gridView.getViewPresenter().handleAction(Action.REFRESH);
                  }
                });
              }
            });
          }
        }
      });

      gridView.add(collector);

      FormView form = UiHelper.getForm(gridView.asWidget());
      if (form != null) {
        collector.bindDnd(form);
      }
    }
  }
}
