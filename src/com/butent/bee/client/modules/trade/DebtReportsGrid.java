package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.Opener;
import com.butent.bee.client.view.edit.EditStartEvent;
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
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.HashSet;
import java.util.Set;

class DebtReportsGrid extends AbstractGridInterceptor implements ClickHandler {

  private final class DebtReportTemplateForm extends AbstractFormInterceptor {

    private static final String NAME_TEMPLATE = "Template";
    private static final String NAME_SEND = "Send";
    private static final String NAME_SUBJECT = "Subject";
    private static final String NAME_FIRST_PARAGRAPH = "FirstParagraph";
    private static final String NAME_LAST_PARAGRAPH = "LastParagraph";

    private final Set<Long> ids;

    private UnboundSelector template;
    private InputText subject;
    private InputArea firstParagraph;
    private InputArea lastParagraph;

    DebtReportTemplateForm(Set<Long> ids) {
      this.ids = ids;
    }

    @Override
    public void afterCreateWidget(String name, IdentifiableWidget widget,
        WidgetDescriptionCallback callback) {

      if (BeeUtils.same(name, NAME_TEMPLATE) && widget instanceof UnboundSelector) {
        template = (UnboundSelector) widget;

        template.addSelectorHandler(event -> {
          BeeRow row = event.getSelector().getRelatedRow();

          if (row == null) {
            return;
          }

          DataInfo info = Data.getDataInfo(TradeConstants.VIEW_DEBT_REMINDER_TEMPLATE);

          if (getSubject() != null) {
            getSubject().setText(row.getString(info
                .getColumnIndex(TradeConstants.COL_TEMPLATE_SUBJECT)));
          }

          if (getFirstParagraph() != null) {
            getFirstParagraph().setText(row.getString(info
                .getColumnIndex(TradeConstants.COL_TEMPLATE_FIRST_PARAGRAPH)));
          }

          if (getLastParagraph() != null) {
            getLastParagraph().setText(row.getString(info
                .getColumnIndex(TradeConstants.COL_TEMPLATE_LAST_PARAGRAPH)));
          }
        });
      }

      if (BeeUtils.same(name, NAME_SUBJECT) && widget instanceof InputText) {
        subject = (InputText) widget;
      }

      if (BeeUtils.same(name, NAME_FIRST_PARAGRAPH) && widget instanceof InputArea) {
        firstParagraph = (InputArea) widget;
      }

      if (BeeUtils.same(name, NAME_LAST_PARAGRAPH) && widget instanceof InputArea) {
        lastParagraph = (InputArea) widget;
      }

      if (BeeUtils.same(name, NAME_SEND) && widget instanceof Button) {
        final Button button = (Button) widget;
        button.setEnabled(true);
        button.addClickHandler(arg0 -> {
          button.setEnabled(false);
          String subjectText =
              getSubject() != null ? getSubject().getText() : BeeConst.STRING_EMPTY;
          String p1 =
              getFirstParagraph() != null ? getFirstParagraph().getText() : BeeConst.STRING_EMPTY;
          String p2 =
              getLastParagraph() != null ? getLastParagraph().getText() : BeeConst.STRING_EMPTY;
          sendMail(subjectText, p1, p2, ids);
        });
      }
    }

    @Override
    public FormInterceptor getInstance() {
      return new DebtReportTemplateForm(ids);
    }

    private InputText getSubject() {
      return subject;
    }

    private InputArea getFirstParagraph() {
      return firstParagraph;
    }

    private InputArea getLastParagraph() {
      return lastParagraph;
    }

  }

  private final Button action = new Button(Localized.dictionary().sendReminderMail(), this);

  @Override
  public void afterCreatePresenter(final GridPresenter presenter) {
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
    final Set<Long> rowIds = new HashSet<>();

    for (RowInfo row : presenter.getGridView().getSelectedRows(SelectedRows.ALL)) {
      rowIds.add(row.getId());
    }

    for (IsRow row : presenter.getGridView().getRowData()) {
      if (rowIds.contains(row.getId())) {
        Long companyId = row.getLong(getDataIndex(ClassifierConstants.COL_COMPANY));
        if (DataUtils.isId(companyId)) {
          ids.add(companyId);
        }
      }
    }

    if (ids.isEmpty()) {
      presenter.getGridView().notifyWarning(Localized.dictionary().selectAtLeastOneRow());
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

    int idxCompany = getDataIndex(ClassifierConstants.COL_COMPANY);

    if (TradeConstants.COL_TRADE_DEBT_COUNT.equals(event.getColumnId())) {
      int idxCurrency = getDataIndex(AdministrationConstants.COL_CURRENCY);
      GridOptions options = GridOptions.forFilter(
          Filter.and(
              Filter.or(
                  Filter.and(
                      Filter.equals(TradeConstants.COL_TRADE_CUSTOMER, activeRow.getLong(
                          idxCompany)),
                      Filter.isNull(TradeConstants.COL_SALE_PAYER)),
                  Filter.equals(TradeConstants.COL_SALE_PAYER, activeRow.getLong(idxCompany))),
              Filter.equals(AdministrationConstants.COL_CURRENCY, activeRow.getLong(idxCurrency)),
              Filter.isPositive(TradeConstants.COL_TRADE_DEBT)));

      GridFactory.openGrid(TradeConstants.GRID_SALES,
          GridFactory.getGridInterceptor(TradeConstants.GRID_SALES),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    } else if (ClassifierConstants.COL_COMPANY_NAME.equals(event.getColumnId())) {
      RowEditor.open(ClassifierConstants.VIEW_COMPANIES, activeRow.getId(),
          Opener.NEW_TAB);
    }
  }

  private void openForm(final Set<Long> ids) {

    FormFactory.createFormView(TradeConstants.FORM_DEBT_REPORT_TEMPLATE, null,
        null, false,
        new DebtReportTemplateForm(ids), (formDescription, result) -> {
          if (result != null) {
            result.start(null);
            Global.showModalWidget(result.getCaption(), result.asWidget());
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

        if (response.hasResponse() && response.getResponse() instanceof String) {
          getGridPresenter().getGridView().notifyInfo(response.getResponseAsString());
        }

        if (Popup.getActivePopup() != null) {
          Popup.getActivePopup().close();
        }
      }
    });

  }

}