package com.butent.bee.client.modules.projects;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.projects.ProjectConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowFactory;
import com.butent.bee.client.grid.ColumnFooter;
import com.butent.bee.client.grid.ColumnHeader;
import com.butent.bee.client.grid.column.AbstractColumn;
import com.butent.bee.client.grid.column.CalculatedColumn;
import com.butent.bee.client.modules.trade.acts.ItemPricePicker;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.render.HasCellRenderer;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.edit.EditableColumn;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsColumn;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RowFunction;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectIncomesGrid extends AbstractGridInterceptor {

  @Override
  public boolean afterCreateColumn(String columnName, List<? extends IsColumn> dataColumns,
      AbstractColumn<?> column, ColumnHeader header, ColumnFooter footer,
      EditableColumn editableColumn) {

    switch (columnName) {
      case PROP_ITEM_PRICES:
        if (column instanceof CalculatedColumn) {

          int idxItemPrice = DataUtils.getColumnIndex(COL_PROJECT_ITEM_PRICE, dataColumns);
          CellSource cellSource = CellSource.forColumn(dataColumns.get(idxItemPrice), idxItemPrice);

          RowFunction<Long> currencyFunction = new RowFunction<Long>() {

            @Override
            public Long apply(IsRow arg0) {
              if (getGridPresenter() == null) {
                return null;
              }

              return ViewHelper.getParentValueLong(getGridPresenter().getMainView().asWidget(),
                  VIEW_PROJECTS, COL_PROJECT_CURENCY);
            }
          };

          ItemPricePicker pricePicker =
              new ItemPricePicker(cellSource, dataColumns, currencyFunction);
          ((HasCellRenderer) column).setRenderer(pricePicker);
        }
        break;

      default:
        break;
    }

    return super.afterCreateColumn(columnName, dataColumns, column, header, footer, editableColumn);
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    GridView gridView = presenter.getGridView();

    if (gridView == null || gridView.isReadOnly()) {
      return;
    }
    FaLabel createInvoiceButton = new FaLabel(FontAwesome.LIST_ALT);
    createInvoiceButton.setTitle(Localized.getConstants().createInvoice());

    createInvoiceButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent arg0) {
        createInvoice();
      }
    });

    presenter.getHeader().addCommandItem(createInvoiceButton);
  }

  @Override
  public GridInterceptor getInstance() {
    return new ProjectIncomesGrid();
  }

  private void createInvoice() {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> ids = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      ids.add(row.getId());
    }
    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    Queries.getRowSet(VIEW_PROJECT_INCOMES, null, Filter.and(Filter.idIn(ids), Filter
        .isNull(TradeConstants.COL_SALE)), new RowSetCallback() {

      @SuppressWarnings("unused")
      @Override
      public void onSuccess(BeeRowSet result) {
        FormView parentForm = ViewHelper.getForm(presenter.getMainView());
        Long customerId = null;
        String customerName = null;

        final DataInfo salesInfo = Data.getDataInfo(VIEW_PROJECT_INVOICES);
        final BeeRow newSalesRow = RowFactory.createEmptyRow(salesInfo, true);

        newSalesRow.setValue(salesInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER), BeeKeeper
            .getUser().getUserId());

        newSalesRow.setValue(salesInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER
            + ClassifierConstants.COL_FIRST_NAME), BeeKeeper.getUser().getFirstName());

        newSalesRow.setValue(salesInfo.getColumnIndex(TradeConstants.COL_TRADE_MANAGER
            + ClassifierConstants.COL_LAST_NAME), BeeKeeper.getUser().getLastName());

        if (parentForm != null && parentForm.getActiveRow() != null) {
          IsRow projectRow = parentForm.getActiveRow();
          DataInfo projectDataInfo = Data.getDataInfo(parentForm.getViewName());

          customerId =
              projectRow.getLong(projectDataInfo.getColumnIndex(ClassifierConstants.COL_COMPANY));
          customerName =
              projectRow.getString(projectDataInfo
                  .getColumnIndex(ClassifierConstants.ALS_COMPANY_NAME));
        }

      }
    });
  }

}
