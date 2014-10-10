package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.Global;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormDescription;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

class DebtReportsGrid extends AbstractGridInterceptor implements ClickHandler {

  private final class DebtReportTemplateForm extends AbstractFormInterceptor {

    private final String NAME_TEMPLATE = "Template";
    private final String NAME_SEND = "Send";
    private final String NAME_SUBJECT = "Subject";
    private final String NAME_FIRST_PARAGRAPHAR = "FirstParagraph";

    UnboundSelector template;

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {

      if (BeeUtils.same(name, NAME_TEMPLATE) && widget instanceof UnboundSelector) {
        template = (UnboundSelector) widget;

        template.addSelectorHandler(new SelectorEvent.Handler() {

          @Override
          public void onDataSelector(SelectorEvent event) {
            event.getSelector().getValue();
          }
        });
      }

      if (BeeUtils.same(name, NAME_SEND) && widget instanceof Button) {
        Button button = (Button) widget;
        button.addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent arg0) {

          }
        });
      }
    }


    @Override
    public FormInterceptor getInstance() {
      return new DebtReportTemplateForm();
    }

  }

  private final Button action = new Button(Localized.getConstants().sendReminder(), this);


  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    presenter.getHeader().clearCommandPanel();
    presenter.getHeader().addCommandItem(action);
  }

  @Override
  public GridInterceptor getInstance() {
    return new DebtReportsGrid();
  }

  @Override
  public void onClick(ClickEvent event) {
    final GridPresenter presenter = getGridPresenter();
    final Set<Long> ids = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      ids.add(row.getId());
    }

    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.getConstants().selectAtLeastOneRow());
      return;
    }

    openForm();
  }

  @Override
  public void onEditStart(EditStartEvent event) {
    IsRow activeRow = event.getRowValue();

    if (activeRow == null) {
      return;
    }

    if (TradeConstants.COL_TRADE_DEBT_COUNT.equals(event.getColumnId())) {
      int idxCurrency = getDataIndex(AdministrationConstants.COL_CURRENCY);
      GridOptions options = GridOptions.forFilter(Filter.and(Filter.equals(
          TradeConstants.COL_TRADE_CUSTOMER, activeRow.getId()),
          Filter.equals(AdministrationConstants.COL_CURRENCY, activeRow.getLong(idxCurrency))));

      GridFactory.openGrid(TradeConstants.GRID_SALES,
          GridFactory.getGridInterceptor(TradeConstants.GRID_SALES),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    }
  }

  private void openForm() {
    FormFactory.createFormView(TradeConstants.FORM_DEBT_REPORT_TEMPLATE, null,
        null, false,
        new DebtReportTemplateForm(), new FormFactory.FormViewCallback() {

          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              Global.showModalWidget(result.getCaption(), result.asWidget());

            }
          }
        });
  }

}
