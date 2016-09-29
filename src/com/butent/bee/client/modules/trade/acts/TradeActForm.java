package com.butent.bee.client.modules.trade.acts;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
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
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation.Caching;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collection;

public class TradeActForm extends PrintFormInterceptor implements SelectorEvent.Handler,
    HandlesUpdateEvents {

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "form-";

  private static final String STYLE_CREATE = STYLE_PREFIX + "create";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";

  private static final String STYLE_HAS_SERVICES = STYLE_PREFIX + "has-services";
  private static final String STYLE_NO_SERVICES = STYLE_PREFIX + "no-services";

  private static final String STYLE_HAS_INVOICES = STYLE_PREFIX + "has-invoices";
  private static final String STYLE_NO_INVOICES = STYLE_PREFIX + "no-invoices";

  private final Collection<HandlerRegistration> dataHandlerRegistry = new ArrayList<>();

  private TradeActKind lastKind;

  private boolean hasInvoicesOrSecondaryActs;
  private DataSelector contractSelector;
  private DataSelector objectSelector;
  private DataSelector companySelector;
  private ChildGrid tradeActItemsGrid;
  private ChildGrid tradeActServicesGrid;

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

    if (widget instanceof DataSelector && BeeUtils.same(name, TradeActConstants.COL_TA_COMPANY)) {
      companySelector = (DataSelector) widget;
      companySelector.addSelectorHandler(this);
    }

    if (widget instanceof ChildGrid && BeeUtils.same(name, GRID_TRADE_ACT_ITEMS)) {
      tradeActItemsGrid = (ChildGrid) widget;
    }

    if (widget instanceof ChildGrid && BeeUtils.same(name, GRID_TRADE_ACT_SERVICES)) {
      tradeActServicesGrid = (ChildGrid) widget;
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, final IsRow row) {
    TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
    Button commandCompose;
    String caption;

    if (DataUtils.isNewRow(row)) {
      form.removeStyleName(STYLE_EDIT);
      form.addStyleName(STYLE_CREATE);

      caption = BeeUtils.join(" - ", Localized.dictionary().tradeActNew(),
          (kind == null) ? null : kind.getCaption());

    } else {
      form.removeStyleName(STYLE_CREATE);
      form.addStyleName(STYLE_EDIT);

      caption = (kind == null) ? Localized.dictionary().tradeAct() : kind.getCaption();
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

    if (companySelector != null) {
      contractSelector.getOracle().setAdditionalFilter(getRelatedDocumentsFilter(), true);
    }

    HeaderView header = form.getViewPresenter().getHeader();
    header.clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      commandCompose = new Button(
          Localized.dictionary().taInvoiceCompose(), new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
              FormFactory.openForm(FORM_INVOICE_BUILDER,
                  new TradeActInvoiceBuilder(row.getLong(Data.getColumnIndex(VIEW_TRADE_ACTS,
                      COL_TA_COMPANY)), row.getId()));
            }
          });

      if (kind != TradeActKind.RETURN) {
        header.addCommandItem(commandCompose);
      }
    }

    if (tradeActItemsGrid != null) {
      tradeActItemsGrid.setEnabled((row != null && row.hasPropertyValue(PROP_CONTINUOUS_COUNT)
          ? !BeeUtils.isPositive(row.getPropertyInteger(PROP_CONTINUOUS_COUNT)) : true)
          && kind != TradeActKind.CONTINUOUS);
    }

    if (tradeActServicesGrid != null) {
      tradeActServicesGrid.setEnabled(row != null && row.hasPropertyValue(PROP_CONTINUOUS_COUNT)
          ? !BeeUtils.isPositive(row.getPropertyInteger(PROP_CONTINUOUS_COUNT)) : true);
    }

    createReqLabels(form);
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
          form.notifySevere(Localized.dictionary().contact() + " "
              + Localized.dictionary().valueRequired());
          valid = false;
        } else {
          valid = true;
        }
      }

      if (TradeActKind.RETURN.ordinal() == BeeUtils.unbox(row.getInteger(idxKind))) {
        if (!BeeUtils.isEmpty(regNo)) {
          valid = true;
        } else {
          form.notifySevere(Localized.dictionary().taRegistrationNo() + " "
              + Localized.dictionary().valueRequired());
          valid = false;
        }
      }

      if (valid) {
        valid = createReqFields(form);
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
    return new PrintActForm();
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
  public void onCellUpdate(CellUpdateEvent event) {
    // if (event.hasView(ClassifierConstants.VIEW_COMPANIES) && companySelector != null) {
    // companySelector.getOracle().onCellUpdate(event);
    // }

  }

  @Override
  public void onLoad(FormView form) {
    dataHandlerRegistry.addAll(BeeKeeper.getBus().registerUpdateHandler(this, false));
    super.onLoad(form);
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    FormView form = getFormView();
    if (form == null) {
      return;
    }

    if (event.hasView(ClassifierConstants.VIEW_COMPANIES)
        && event.getRowId() == BeeUtils.unbox(form
            .getLongValue(COL_TA_COMPANY))) {

      String[][] cols = new String[][] {
          {ClassifierConstants.ALS_COMPANY_NAME, ClassifierConstants.COL_COMPANY_NAME},
          {ALS_CONTACT_PHYSICAL, "Physical"},
          {ClassifierConstants.ALS_COMPANY_TYPE_NAME, ClassifierConstants.ALS_COMPANY_TYPE_NAME}
      };

      IsRow row = form.getActiveRow();
      DataInfo eventData = Data.getDataInfo(event.getViewName());

      for (String[] col : cols) {
        row.setValue(form.getDataIndex(col[0]), event.getRow().getValue(eventData.getColumnIndex(
            col[1])));

        form.refreshBySource(col[0]);
      }
    }
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row,
      final ScheduledCommand focusCommand) {

    hasInvoicesOrSecondaryActs = false;

    if (form != null && DataUtils.hasId(row)) {
      TradeActKind kind = TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));

      if (tradeActItemsGrid != null) {
        tradeActItemsGrid.setEnabled((row != null && row.hasPropertyValue(PROP_CONTINUOUS_COUNT)
            ? !BeeUtils.isPositive(row.getPropertyInteger(PROP_CONTINUOUS_COUNT)) : true)
            && kind != TradeActKind.CONTINUOUS);
      }

      if (tradeActServicesGrid != null) {
        tradeActServicesGrid.setEnabled(row != null && row.hasPropertyValue(PROP_CONTINUOUS_COUNT)
            ? !BeeUtils.isPositive(row.getPropertyInteger(PROP_CONTINUOUS_COUNT)) : true);
      }

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

    if (BeeUtils.same(event.getRelatedViewName(), ClassifierConstants.VIEW_COMPANIES)) {

      if (event.isChanged()) {
        Filter relDocFilter = getRelatedDocumentsFilter();

        Filter relActiveDocFilter = Filter.and(relDocFilter,
            Filter.notNull(DocumentConstants.ALS_STATUS_MAIN));

        DataInfo viewDocuments = Data.getDataInfo(DocumentConstants.VIEW_DOCUMENTS);

        Queries.getRowSet(viewDocuments.getViewName(), viewDocuments.getColumnNames(false),
            relActiveDocFilter, null, new Queries.RowSetCallback() {
              @Override
              public void onSuccess(BeeRowSet result) {
                if (result.getNumberOfRows() != 1) {
                  if (companySelector != null) {
                    contractSelector.clearValue();
                  }
                  return;
                }

                if (companySelector != null) {
                  contractSelector.setSelection(result.getRow(0), null, true);
                }
              }
            });

        if (companySelector != null) {
          contractSelector.getOracle().setAdditionalFilter(relDocFilter, true);
        }
      }
    }
  }

  @Override
  public void onUnload(FormView form) {

    EventUtils.clearRegistry(dataHandlerRegistry);

    super.onUnload(form);
  }

  private Filter getRelatedDocumentsFilter() {
    return Filter.in(Data.getIdColumn(DocumentConstants.VIEW_DOCUMENTS),
        DocumentConstants.VIEW_RELATED_DOCUMENTS, DocumentConstants.COL_DOCUMENT, Filter
            .equals(ClassifierConstants.COL_COMPANY, getActiveRow()
                .getString(getDataIndex(COL_TA_COMPANY))));
  }

  private static boolean createReqFields(FormView form) {

    IsRow row = form.getActiveRow();
    boolean valid = true;

    if (row == null) {
      return false;
    }

    int kind = row.getInteger(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_KIND));

    String[] fields = EnumUtils.getEnumByIndex(TradeActKind.class, kind).getReqFields();

    if (fields != null) {
      for (String field : fields) {

        if (BeeUtils.inListSame(field, COL_TA_VEHICLE, COL_TA_INPUT_VEHICLE, COL_TA_DRIVER,
            COL_TA_INPUT_DRIVER)) {

          String v1 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, field));
          String v2;

          switch (field) {
            case COL_TA_DRIVER:
              v2 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_INPUT_DRIVER));
              break;
            case COL_TA_INPUT_DRIVER:
              v2 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_DRIVER));
              break;
            case COL_TA_VEHICLE:
              v2 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_INPUT_VEHICLE));
              break;
            case COL_TA_INPUT_VEHICLE:
              v2 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_VEHICLE));
              break;
            default:
              v2 = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, field));
          }

          valid = !BeeUtils.isEmpty(v1) || !BeeUtils.isEmpty(v2);

          if (!valid) {
            form.notifySevere(Data.getColumnLabel(VIEW_TRADE_ACTS, field) + " "
                + Localized.dictionary().valueRequired());

            return valid;
          }
        } else {

          String value = row.getString(Data.getColumnIndex(VIEW_TRADE_ACTS, field));
          valid = !BeeUtils.isEmpty(value);
          if (!valid) {
            form.notifySevere(Data.getColumnLabel(VIEW_TRADE_ACTS, field) + " "
                + Localized.dictionary().valueRequired());
            return valid;
          }
        }
      }
    }
    return valid;
  }

  private static void createReqLabels(FormView form) {
    IsRow row = form.getActiveRow();
    Widget wid;

    if (row == null) {
      return;
    }

    int kind = row.getInteger(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_KIND));

    String[] fields = EnumUtils.getEnumByIndex(TradeActKind.class, kind).getReqFields();

    if (fields != null) {
      for (String field : fields) {
        wid = form.getWidgetByName("label" + field);
        wid.addStyleName("bee-required");
      }
    }
  }
}
