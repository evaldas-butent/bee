package com.butent.bee.client.modules.trade;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.RowEditor;
import com.butent.bee.client.dialog.Popup;
import com.butent.bee.client.grid.GridFactory;
import com.butent.bee.client.grid.GridFactory.GridOptions;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.presenter.PresenterCallback;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.EditStartEvent;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.GridView.SelectedRows;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.view.search.AbstractFilterSupplier;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.CheckBox;
import com.butent.bee.client.widget.InputArea;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.filter.FilterValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.data.view.RowInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.ui.ColumnDescription;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    presenter.getHeader().addCommandItem(TradeKeeper.createAmountAction(presenter.getViewName(),
      () ->
        presenter.getDataProvider().getFilter(), TradeConstants.COL_SALE, presenter.getGridView()));
  }

  @Override
  public AbstractFilterSupplier getFilterSupplier(String columnName,
      ColumnDescription columnDescription) {

    if (BeeUtils.same(columnName, "Select")) {
      return new AbstractFilterSupplier(TradeConstants.VIEW_DEBT_REPORTS, null, null,
          columnDescription.getFilterOptions()) {

        private static final String F_OVERDUE = "termOverdue";
        private static final String F_NOT_EXPIRED = "termNotExpired";
        private static final String F_PAY_FROM = "termPayFrom";
        private static final String F_PAY_TO = "termPayTo";

        private boolean termOverdue;
        private boolean termNotExpired;
        private JustDate termPayFrom;
        private JustDate termPayTo;

        @Override
        public void setFilterValue(FilterValue filterValue) {
          if (filterValue == null) {
            setTermOverdue(false);
            setTermNotExpired(false);
            setTermPayFrom(null);
            setTermPayTo(null);
            return;
          }

          Map<String, String> val = Codec.deserializeHashMap(filterValue.getValue());

          if (val == null) {
            return;
          }

          setTermOverdue(BeeUtils.toBoolean(val.get(F_OVERDUE)));
          setTermNotExpired(BeeUtils.toBoolean(val.get(F_NOT_EXPIRED)));

          if (!BeeUtils.isEmpty(val.get(F_PAY_FROM))) {
            JustDate d = new JustDate();
            d.deserialize(val.get(F_PAY_FROM));
            setTermPayFrom(d);
          }

          if (!BeeUtils.isEmpty(val.get(F_PAY_TO))) {
            JustDate d = new JustDate();
            d.deserialize(val.get(F_PAY_TO));
            setTermPayTo(d);
          }
        }

        @Override
        public Filter parse(FilterValue input) {
          if (input == null) {
            return null;
          }

          Map<String, String> val = Codec.deserializeHashMap(input.getValue());

          if (val == null) {
            return null;
          }

          Filter f = Filter.isTrue();
          if (BeeUtils.toBoolean(val.get(F_OVERDUE))) {
            f = Filter.and(f, Filter.isLess(
                TradeConstants.COL_TRADE_TERM, Value.getValue(new JustDate())));
          }

          if (BeeUtils.toBoolean(val.get(F_NOT_EXPIRED))) {
            f = Filter.and(f, Filter.isMoreEqual(
                TradeConstants.COL_TRADE_TERM, Value.getValue(new JustDate())));
          }

          if (!BeeUtils.isEmpty(val.get(F_PAY_FROM))) {
            JustDate d = new JustDate();
            d.deserialize(val.get(F_PAY_FROM));
            f = Filter.and(f, Filter.isMoreEqual(TradeConstants.COL_TRADE_TERM, Value.getValue(
                d)));
          }

          if (!BeeUtils.isEmpty(val.get(F_PAY_TO))) {
            JustDate d = new JustDate();
            d.deserialize(val.get(F_PAY_TO));
            f = Filter.and(f, Filter.isLessEqual(TradeConstants.COL_TRADE_TERM, Value.getValue(
                d)));
          }

          return f;
        }

        @Override
        public void onRequest(Element target, ScheduledCommand onChange) {
          openDialog(target, getWidget(), null, onChange);

        }

        @Override
        public String getComponentLabel(String ownerLabel) {
          return BeeUtils.isEmpty(getLabel()) ? null : getLabel();
        }

        @Override
        public String getLabel() {
          return BeeUtils.join(BeeConst.DEFAULT_LIST_SEPARATOR, isTermOverdue() ? Localized
              .dictionary().trdTermInOverdue()
              : null, isTermNotExpired() ? Localized.dictionary().trdTermNotExpired() : null,
              getTermPayFrom() != null ? BeeUtils.joinWords(Localized.dictionary().trdTerm(),
                  Localized.dictionary().dateFromShort(), getTermPayFrom().toString()) : null,
              getTermPayTo() != null ? BeeUtils.joinWords(Localized.dictionary().trdTerm(),
                  Localized.dictionary().dateToShort(), getTermPayTo().toString()) : null);
        }

        @Override
        public FilterValue getFilterValue() {
          Map<String, String> val = new HashMap<>();

          if (isTermOverdue()) {
            val.put(F_OVERDUE, BeeUtils.toString(isTermOverdue()));
          }
          if (isTermNotExpired()) {
            val.put(F_NOT_EXPIRED, BeeUtils.toString(isTermNotExpired()));
          }
          val.put(F_PAY_FROM, getTermPayFrom() != null ? BeeUtils.toString(getTermPayFrom()
              .getDays()) : null);
          val.put(F_PAY_TO, getTermPayTo() != null ? BeeUtils.toString(getTermPayTo().getDays())
              : null);

          return val.isEmpty() ? null : FilterValue.of(Codec.beeSerialize(val));
        }

        private Widget getWidget() {
          Flow conatainer = new Flow();
          HtmlTable tbl = new HtmlTable();
          final CheckBox ovt = new CheckBox(Localized.dictionary().trdTermInOverdue());
          final CheckBox net = new CheckBox(Localized.dictionary().trdTermNotExpired());
          final InputDate pfd = new InputDate();
          final InputDate ptd = new InputDate();

          ovt.setChecked(isTermOverdue());

          net.setChecked(isTermNotExpired());
          pfd.setDate(getTermPayFrom());
          ptd.setDate(getTermPayTo());

          // Flow ckContainer = new Flow(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);

          tbl.setWidget(0, 0, ovt);
          tbl.setWidget(0, 1, net);
          conatainer.add(tbl);

          // Flow dateContainer = new Flow(StyleUtils.NAME_FLEX_BOX_HORIZONTAL);
          tbl.setWidget(1, 0, new Label(Localized.dictionary().trdTerm()));
          tbl.setWidget(1, 1, pfd);
          tbl.setWidget(1, 2, new Label(BeeConst.STRING_MINUS));
          tbl.setWidget(1, 3, ptd);
          // conatainer.add(dateContainer);

          Button filter = new Button(Localized.dictionary().doFilter());
          filter.addClickHandler(arg0 -> {
            setTermOverdue(ovt.isChecked());
            setTermNotExpired(net.isChecked());
            setTermPayFrom(pfd.getDate());
            setTermPayTo(ptd.getDate());
            update(true);
          });

          Button cancel = new Button(Localized.dictionary().cancel());
          cancel.addClickHandler(event -> closeDialog());

          Flow btnContainer = new Flow("bee-FilterSupplier-commandPanel");

          btnContainer.add(filter);
          btnContainer.add(cancel);
          conatainer.add(btnContainer);

          return conatainer;
        }

        private boolean isTermNotExpired() {
          return termNotExpired;
        }

        private boolean isTermOverdue() {
          return termOverdue;
        }

        private void setTermNotExpired(boolean termNotExpired) {
          this.termNotExpired = termNotExpired;
        }

        private void setTermOverdue(boolean termOverdue) {
          this.termOverdue = termOverdue;
        }

        public JustDate getTermPayFrom() {
          return termPayFrom;
        }

        public void setTermPayFrom(JustDate termPayFrom) {
          this.termPayFrom = termPayFrom;
        }

        public JustDate getTermPayTo() {
          return termPayTo;
        }

        public void setTermPayTo(JustDate termPayTo) {
          this.termPayTo = termPayTo;
        }
      };
    }

    return super.getFilterSupplier(columnName, columnDescription);
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

    if (TradeConstants.COL_TRADE_DEBT_COUNT.equals(event.getColumnId())) {
      int idxCurrency = getDataIndex(AdministrationConstants.COL_CURRENCY);
      GridOptions options = GridOptions.forFilter(Filter.and(Filter.equals(
          TradeConstants.COL_TRADE_CUSTOMER, activeRow.getId()),
          Filter.equals(AdministrationConstants.COL_CURRENCY, activeRow.getLong(idxCurrency)),
          Filter.isPositive(TradeConstants.COL_TRADE_DEBT)));

      GridFactory.openGrid(TradeConstants.GRID_ERP_SALES,
          GridFactory.getGridInterceptor(TradeConstants.GRID_ERP_SALES),
          options, PresenterCallback.SHOW_IN_NEW_TAB);
    } else if (ClassifierConstants.COL_COMPANY_NAME.equals(event.getColumnId())) {
      RowEditor.open(ClassifierConstants.VIEW_COMPANIES, activeRow.getId());
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

    BeeKeeper.getRpc().makePostRequest(rpc, response -> {
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
    });

  }

}
