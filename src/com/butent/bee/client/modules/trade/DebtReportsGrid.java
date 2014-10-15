package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.dialog.Popup;
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
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
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

    private static final String NAME_TEMPLATE = "Template";
    private static final String NAME_SEND = "Send";
    private static final String NAME_SUBJECT = "Subject";
    private static final String NAME_FIRST_PARAGRAPHAR = "FirstParagraph";
    private static final String NAME_LAST_PARAGRAPHAR = "FirstParagraph";

    private final Set<Long> ids;

    UnboundSelector template;
    InputText subject;
    InputArea firstParagraph;
    InputArea lastParagraph;

    public DebtReportTemplateForm(Set<Long> ids) {
      this.ids = ids;
    }

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

      if (BeeUtils.same(name, NAME_SUBJECT) && widget instanceof InputText) {
        subject = (InputText) widget;
      }

      if (BeeUtils.same(name, NAME_FIRST_PARAGRAPHAR) && widget instanceof InputArea) {
        firstParagraph = (InputArea) widget;
      }

      if (BeeUtils.same(name, NAME_LAST_PARAGRAPHAR) && widget instanceof InputArea) {
        lastParagraph = (InputArea) widget;
      }

      if (BeeUtils.same(name, NAME_SEND) && widget instanceof Button) {
        Button button = (Button) widget;
        button.addClickHandler(new ClickHandler() {

          @Override
          public void onClick(ClickEvent arg0) {
            String subjectText = subject != null ? subject.getValue() : BeeConst.STRING_EMPTY;
            String p1 = firstParagraph != null ? firstParagraph.getValue() : BeeConst.STRING_EMPTY;
            String p2 = lastParagraph != null ? lastParagraph.getValue() : BeeConst.STRING_EMPTY;
            sendMail(subjectText, p1, p2, ids);
          }
        });
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new DebtReportTemplateForm(ids);
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

    openForm(ids);
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

  private void openForm(final Set<Long> ids) {

    FormFactory.createFormView(TradeConstants.FORM_DEBT_REPORT_TEMPLATE, null,
        null, false,
        new DebtReportTemplateForm(ids), new FormFactory.FormViewCallback() {

          @Override
          public void onSuccess(FormDescription formDescription, FormView result) {
            if (result != null) {
              Global.showModalWidget(result.getCaption(), result.asWidget());
            }
          }
        });
  }

  private void sendMail(String subject, String p1, String p2, Set<Long> ids) {
    ParameterList rpc = TradeKeeper.createArgs(TradeConstants.SVC_REMIND_DEBTS_EMAIL);
    rpc.addDataItem(TradeConstants.VAR_SUBJECT, subject);
    rpc.addDataItem(TradeConstants.VAR_HEADER, p1);
    rpc.addDataItem(TradeConstants.VAR_FOOTER, p2);
    rpc.addDataItem(TradeConstants.VAR_ID_LIST, DataUtils.buildIdList(ids));

    BeeKeeper.getRpc().makePostRequest(rpc, new ResponseCallback() {

      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasErrors()) {
          getGridPresenter().getGridView().notifySevere(response.getErrors());
          Popup.getActivePopup().close();
          return;
        }

        Popup.getActivePopup().close();
      }
    });


  }

}
