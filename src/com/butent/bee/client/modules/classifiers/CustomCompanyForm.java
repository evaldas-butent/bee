package com.butent.bee.client.modules.classifiers;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.transport.TransportConstants.COL_SERVICE;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.modules.trade.TradeKeeper;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.Editor;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
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

import java.util.HashSet;
import java.util.Set;

public abstract class CustomCompanyForm extends AbstractFormInterceptor implements ClickHandler {

  private final Button toErp = new Button(Localized.dictionary().trSendToERP(), this);
  private Long company;

  @Override
  public void afterCreate(FormView form) {
    Global.getParameter(AdministrationConstants.PRM_COMPANY,
        input -> company = BeeUtils.toLongOrNull(input));

    super.afterCreate(form);
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
                  event.getSelector()
                      .setAdditionalFilter(Filter.and(Filter.equals(COL_COMPANY, company),
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
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    if (!checkRequired()) {
      event.consume();
      return;
    }
    super.onReadyForInsert(listener, event);
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (!checkRequired()) {
      event.consume();
      return;
    }
    super.onSaveChanges(listener, event);
  }

  private boolean checkRequired() {
    if (!BeeUtils.toBoolean(getStringValue("Offshore"))) {
      for (String field : new String[] {
          COL_COMPANY_CODE, COL_COMPANY_VAT_CODE, COL_ADDRESS, COL_CITY, COL_COUNTRY}) {

        if (BeeUtils.isEmpty(getStringValue(field))) {
          DomUtils.setFocus(getFormView().getWidgetBySource(field), true);

          getFormView().notifySevere(Data.getColumnLabel(getViewName(), field),
              Localized.dictionary().valueRequired());

          return false;
        }
      }
    }
    return true;
  }
}
