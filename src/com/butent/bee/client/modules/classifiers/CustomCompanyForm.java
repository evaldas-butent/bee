package com.butent.bee.client.modules.classifiers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.modules.transport.TransportHandler;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.rights.RegulatedWidget;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomCompanyForm extends CompanyForm implements ClickHandler {

  private final Button toErp = new Button(Localized.dictionary().trSendToERP(), this);

  @Override
  public FormInterceptor getInstance() {
    return new CustomCompanyForm();
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, "CompanyPayAccounts") && widget instanceof ChildGrid) {
      ((ChildGrid) widget).setGridInterceptor(new AbstractGridInterceptor() {
        @Override
        public void afterCreateEditor(String source, Editor editor, boolean embedded) {
          if ((BeeUtils.same(source, "Account") || BeeUtils.same(source, COL_SERVICE))
              && editor instanceof DataSelector) {

            ((DataSelector) editor).addSelectorHandler(event -> {
              if (BeeUtils.same(event.getRelatedViewName(), TBL_COMPANY_BANK_ACCOUNTS)) {
                if (event.isOpened()) {
                  int idx = getDataIndex("Account");
                  Set<Long> ids = new HashSet<>();

                  for (IsRow row : getGridView().getRowData()) {
                    ids.add(row.getLong(idx));
                  }
                  event.getSelector().setAdditionalFilter(Filter.and(Filter.equals(COL_COMPANY,
                      Global.getParameterRelation(AdministrationConstants.PRM_COMPANY)),
                      Filter.idNotIn(ids)));
                }
              }
            });
          }
          super.afterCreateEditor(source, editor, embedded);
        }

        @Override
        public GridInterceptor getInstance() {
          return null;
        }
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    toErp.setVisible(!DataUtils.isNewRow(row));
    super.afterRefresh(form, row);
  }

  @Override
  public void onClick(ClickEvent event) {
    if (DataUtils.isNewRow(getActiveRow())) {
      return;
    }
    Global.confirm(Localized.dictionary().trSendToERP() + "?", () -> {
      toErp.setVisible(false);

      ParameterList args = TradeKeeper.createArgs(TradeConstants.SVC_SEND_COMPANY_TO_ERP);
      args.addDataItem(COL_COMPANY, getActiveRowId());

      BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
        @Override
        public void onResponse(ResponseObject response) {
          toErp.setVisible(true);
          response.notify(getFormView());

          if (!response.hasErrors()) {
            getFormView().notifyInfo(Localized.dictionary().ok() + ":",
                response.getResponseAsString());
          }
        }
      });
    });
  }

  @Override
  public void onLoad(FormView form) {
    if (BeeKeeper.getUser().isWidgetVisible(RegulatedWidget.TO_ERP)) {
      form.getViewPresenter().getHeader().addCommandItem(toErp);
    }
    super.onLoad(form);
  }

  @Override
  protected void refreshCreditInfo() {
    final FormView form = getFormView();
    final Widget widget = form.getWidgetByName(SVC_CREDIT_INFO, false);

    if (widget != null) {
      widget.getElement().setInnerText(null);

      ParameterList args = TransportHandler.createArgs(SVC_GET_CREDIT_INFO);
      args.addDataItem(COL_COMPANY, getActiveRow().getId());

      BeeKeeper.getRpc().makePostRequest(args, response -> {
        response.notify(form);

        if (response.hasErrors()) {
          return;
        }
        Map<String, String> result =
            Codec.deserializeLinkedHashMap(response.getResponseAsString());

        if (!BeeUtils.isEmpty(result)) {
          HtmlTable table = new HtmlTable();
          table.setColumnCellStyles(1, "text-align:right; font-weight:bold;color:red;");
          int c = 0;

          NumberFormat numberFormat = Format.getDecimalFormat(2);
          Double income = BeeUtils.toDouble(result.get(VAR_INCOME));

          if (BeeUtils.isPositive(income)) {
            table.setHtml(c, 0, Localized.dictionary().trOrdersSumWithProforma());
            table.setHtml(c, 1, numberFormat.format(income));
            c++;
          }
          Double amount = BeeUtils.toDouble(result.get(VAR_DEBT));

          if (BeeUtils.isPositive(amount)) {
            table.setHtml(c, 0, Localized.dictionary().trdDebt());
            table.setHtml(c, 1, numberFormat.format(amount));
            double limit = BeeUtils.toDouble(result.get(COL_COMPANY_CREDIT_LIMIT));

            if (amount <= limit) {
              StyleUtils.setColor(table.getCellFormatter().getElement(c, 1), "black");
            }
            c++;
          }
          Double liabilities = income + amount;

          if (BeeUtils.isPositive(liabilities)) {
            table.setHtml(c, 0, Localized.dictionary().trTotalLiabilities());
            table.setHtml(c, 1, numberFormat.format(liabilities));
            c++;
          }
          amount = BeeUtils.toDouble(result.get(VAR_OVERDUE));

          if (BeeUtils.isPositive(amount)) {
            table.setHtml(c, 0, Localized.dictionary().trdOverdue());
            table.setHtml(c, 1, numberFormat.format(amount));
          }
          widget.getElement().setInnerHTML(table.getElement().getString());
        }
      });
    }
  }
}
