package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.ALS_CONTACT_PHYSICAL;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_COMPANY;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_CONTACT;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_KIND;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TA_REGISTRATION_NO;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.COL_TRADE_ACT;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.FORM_INVOICE_BUILDER;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.SVC_HAS_INVOICES_OR_SECONDARY_ACTS;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.VIEW_TRADE_ACTS;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.VIEW_TRADE_ACT_TEMPLATES;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.WIDGET_TA_CONTRACT;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.modules.transport.PrintInvoiceInterceptor;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.State;
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
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation.Caching;
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
  private DataSelector objectSelector;

  TradeActForm() {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {

    if (widget instanceof DataSelector
        && BeeUtils.same(editableWidget.getColumnId(), COL_TA_COMPANY)) {
      editableWidget.getRelation().setCaching(Caching.NONE);
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector && BeeUtils.same(name, WIDGET_TA_CONTRACT)) {
      contractSelector = (DataSelector) widget;
      contractSelector.addSelectorHandler(this);
    }

    if (widget instanceof DataSelector && BeeUtils.same(name, TradeActConstants.COL_TA_OBJECT)) {
      objectSelector = (DataSelector) widget;
      objectSelector.addSelectorHandler(this);
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, final IsRow row) {
    TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
    Button commandCompose = null;
    String caption;

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

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      commandCompose = new Button(
          Localized.getConstants().taInvoiceCompose(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
              FormFactory.openForm(FORM_INVOICE_BUILDER, new TradeActInvoiceBuilder(row
                  .getLong(Data
                      .getColumnIndex(VIEW_TRADE_ACTS, COL_TA_COMPANY)), row.getId()));
            }
          });
      header.addCommandItem(commandCompose);
    }
    super.afterRefresh(form, row);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {

    if (action.equals(Action.SAVE) && getFormView() != null
        && getActiveRow() != null) {
      FormView form = getFormView();
      IsRow row = getActiveRow();
      Long company = null;
      boolean valid = true;
      String regNo = null;
      int idxCompany = form.getDataIndex(COL_TA_COMPANY);
      int idxRegNo = form.getDataIndex(COL_TA_REGISTRATION_NO);
      int idxKind = form.getDataIndex(COL_TA_KIND);

      if (idxCompany > -1) {
        company = row.getLong(idxCompany);
      }

      if (idxRegNo > -1 && idxKind > -1) {
        regNo = row.getString(idxRegNo);
      }

      if (company != null
          && TradeActKind.RETURN.ordinal() != BeeUtils.unbox(row.getInteger(idxKind))) {
        boolean value = BeeUtils.unbox(row
            .getBoolean(getDataIndex(ALS_CONTACT_PHYSICAL)));
        Long contact = row.getLong(form.getDataIndex(COL_TA_CONTACT));

        if (!value && contact == null) {
          form.notifySevere(Localized.getConstants().contact() + " "
              + Localized.getConstants().valueRequired());
          return false;
        } else {
          return true;
        }
      }

      if (TradeActKind.RETURN.ordinal() == BeeUtils.unbox(row.getInteger(idxKind))) {
        if (!BeeUtils.isEmpty(regNo)) {
          return true;
        } else {
          form.notifySevere(Localized.getConstants().taRegistrationNo() + " "
              + Localized.getConstants().valueRequired());
          return false;
        }
      }
      return valid;
    }
    return super.beforeAction(action, presenter);
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
    if (event.isNewRow()
        && BeeUtils.same(event.getRelatedViewName(), ClassifierConstants.VIEW_COMPANY_OBJECTS)) {

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

    if (event.getState() == State.OPEN && BeeUtils.same(event.getRelatedViewName(),
        DocumentConstants.VIEW_DOCUMENTS)) {
      Filter relDocFilter =
          Filter.in(Data.getIdColumn(DocumentConstants.VIEW_DOCUMENTS),
              DocumentConstants.VIEW_RELATED_DOCUMENTS, DocumentConstants.COL_DOCUMENT, Filter
                  .equals(ClassifierConstants.COL_COMPANY, getActiveRow()
                      .getString(getDataIndex(COL_TA_COMPANY))));

      contractSelector.getOracle().setAdditionalFilter(relDocFilter, true);
    }

  }
}
