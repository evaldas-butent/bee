package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.modules.transport.PrintInvoiceInterceptor;
import com.butent.bee.client.output.PrintFormInterceptor;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;

public class TradeActForm extends PrintFormInterceptor implements SelectorEvent.Handler {

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "form-";

  private static final String STYLE_CREATE = STYLE_PREFIX + "create";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";

  private static final String STYLE_HAS_SERVICES = STYLE_PREFIX + "has-services";
  private static final String STYLE_NO_SERVICES = STYLE_PREFIX + "no-services";

  private static final String STYLE_HAS_INVOICES = STYLE_PREFIX + "has-invoices";
  private static final String STYLE_NO_INVOICES = STYLE_PREFIX + "no-invoices";

  private TradeActKind lastKind;

  private boolean hasInvoicesOrSecondaryActs;
  private DataSelector contractSelector;

  TradeActForm() {
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector && BeeUtils.same(name, WIDGET_TA_CONTRACT)) {
      contractSelector = (DataSelector) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, IsRow row) {
    TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
    String caption;
    DataSelector ds;
    Widget widget = form.getWidgetBySource(TradeActConstants.COL_TA_OBJECT);

    if (widget instanceof DataSelector) {
      ds = (DataSelector) widget;
      ds.addSelectorHandler(this);
    }

    if (DataUtils.isNewRow(row)) {
      form.removeStyleName(STYLE_EDIT);
      form.addStyleName(STYLE_CREATE);

      caption = BeeUtils.join(" - ", Localized.getConstants().tradeActNew(),
          (kind == null) ? null : kind.getCaption());

    } else {
      form.removeStyleName(STYLE_CREATE);
      form.addStyleName(STYLE_EDIT);

      caption = (kind == null) ? Localized.getConstants().tradeAct() : kind.getCaption();
    }

    if (lastKind != kind) {
      if (lastKind != null) {
        form.removeStyleName(STYLE_PREFIX + lastKind.getStyleSuffix());
      }
      if (kind != null) {
        form.addStyleName(STYLE_PREFIX + kind.getStyleSuffix());
      }

      boolean hasServices = kind != null && kind.enableServices();
      form.setStyleName(STYLE_HAS_SERVICES, hasServices);
      form.setStyleName(STYLE_NO_SERVICES, !hasServices);

      boolean hasInvoices = kind != null && kind.enableInvoices();
      form.setStyleName(STYLE_HAS_INVOICES, hasInvoices);
      form.setStyleName(STYLE_NO_INVOICES, !hasInvoices);

      lastKind = kind;
    }

    if (form.getViewPresenter() != null && form.getViewPresenter().getHeader() != null) {
      form.getViewPresenter().getHeader().setCaption(caption);
    }

    Collection<UnboundSelector> unboundSelectors =
        UiHelper.getChildren(form.asWidget(), UnboundSelector.class);
    if (!BeeUtils.isEmpty(unboundSelectors)) {
      for (UnboundSelector us : unboundSelectors) {
        if (DataUtils.isNewRow(row) || !us.hasRelatedView(VIEW_TRADE_ACT_TEMPLATES)) {
          us.clearValue();
        }
      }
    }

    Filter relDocFilter =
        Filter.in(Data.getIdColumn(DocumentConstants.VIEW_DOCUMENTS),
            DocumentConstants.VIEW_RELATED_DOCUMENTS, DocumentConstants.COL_DOCUMENT, Filter
                .equals(ClassifierConstants.COL_COMPANY, row
                    .getString(getDataIndex(COL_TA_COMPANY))));

    contractSelector.getOracle().setAdditionalFilter(relDocFilter, true);

    super.afterRefresh(form, row);
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeActForm();
  }

  @Override
  public FormInterceptor getPrintFormInterceptor() {
    return new PrintInvoiceInterceptor();
  }

  @Override
  public boolean isWidgetEditable(EditableWidget editableWidget, IsRow row) {
    if (editableWidget != null && editableWidget.hasSource(COL_TA_COMPANY) && DataUtils.hasId(row)
        && !row.isNull(getDataIndex(COL_TA_COMPANY))) {

      return !hasInvoicesOrSecondaryActs;

    } else {
      return super.isWidgetEditable(editableWidget, row);
    }
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row,
      final ScheduledCommand focusCommand) {

    hasInvoicesOrSecondaryActs = false;

    if (form != null && DataUtils.hasId(row)) {
      TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));

      if (kind != null && kind.enableInvoices()) {
        ParameterList params = TradeActKeeper.createArgs(SVC_HAS_INVOICES_OR_SECONDARY_ACTS);
        params.addQueryItem(COL_TRADE_ACT, row.getId());

        BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
          @Override
          public void onResponse(ResponseObject response) {
            hasInvoicesOrSecondaryActs = response.hasResponse()
                && BeeConst.isTrue(response.getResponseAsString());

            form.updateRow(row, true);
            if (focusCommand != null) {
              focusCommand.execute();
            }
          }
        });

        return false;
      }
    }

    return super.onStartEdit(form, row, focusCommand);
  }

  @Override
  public void onDataSelector(SelectorEvent event) {
    if (event.isNewRow()) {

      final BeeRow row = event.getNewRow();
      Data.setColumnReadOnly(ClassifierConstants.VIEW_COMPANY_OBJECTS,
          ClassifierConstants.ALS_COMPANY_NAME);

      Data.setValue(ClassifierConstants.VIEW_COMPANY_OBJECTS, row,
          ClassifierConstants.ALS_COMPANY_NAME,
          BeeUtils.trim(getStringValue(ClassifierConstants.ALS_COMPANY_NAME)));

      Data.setValue(ClassifierConstants.VIEW_COMPANY_OBJECTS, row,
          ClassifierConstants.COL_COMPANY,
          BeeUtils.trim(getStringValue(ClassifierConstants.COL_COMPANY)));

      Data.setColumnReadOnly(ClassifierConstants.VIEW_COMPANY_OBJECTS,
          ClassifierConstants.COL_COMPANY);
    }
  }
}
