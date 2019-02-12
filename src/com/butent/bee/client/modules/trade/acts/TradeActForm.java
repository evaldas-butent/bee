package com.butent.bee.client.modules.trade.acts;

import com.butent.bee.client.view.edit.EditEndEvent;
import com.butent.bee.client.widget.*;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Element;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.COL_BACKGROUND;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.TabbedPages;
import com.butent.bee.client.modules.calendar.RelatedAppointmentsGrid;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.style.StyleUtils;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.HeaderView;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.HandlesUpdateEvents;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.documents.DocumentConstants;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActConstants;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Relation.Caching;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TradeActForm extends PrintFormInterceptor implements SelectorEvent.Handler,
    HandlesUpdateEvents {

  private static final String STYLE_PREFIX = TradeActKeeper.STYLE_PREFIX + "form-";

  private static final String STYLE_CREATE = STYLE_PREFIX + "create";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";

  private static final String STYLE_HAS_INVOICES = STYLE_PREFIX + "has-invoices";
  private static final String STYLE_NO_INVOICES = STYLE_PREFIX + "no-invoices";

  private static final String STYLE_HAS_CT_ACTS = STYLE_PREFIX + "has-cta";
  private static final String STYLE_HAS_RPR_ACTS = STYLE_PREFIX + "has-prp";
  private static final String STYLE_NO_CT_ACTS = STYLE_PREFIX + "no-cta";
  private static final String STYLE_NO_RPR_ACTS = STYLE_PREFIX + "no-rpr";

  private final Collection<HandlerRegistration> dataHandlerRegistry = new ArrayList<>();

  private TradeActKind lastKind;

  private boolean hasInvoicesOrSecondaryActs;
  private DataSelector contractSelector;
  private DataSelector companySelector;
  private DataSelector rentProject;

  private Button commandCompose;
  private HeaderView headerView;
  private DataSelector objectSelector;

  private ChildGrid tradeActItemsGrid;
  private ChildGrid rpTradeActItemsGrid;
  private ChildGrid tradeActsGrid;
  private ChildGrid rpTradeActsGrid;
  private ChildGrid relTradeActServices;
  private TabbedPages tabbedPages;

  TradeActForm() {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {

    if (widget instanceof DataSelector && editableWidget.getColumnId() != null) {
      switch (editableWidget.getColumnId()) {
        case COL_TA_COMPANY:
          editableWidget.getRelation().setCaching(Caching.NONE);
          ((DataSelector) widget).addSelectorHandler(this::showCompanyFinancialState);
          break;
        case COL_TRADE_SERIES:
          ((DataSelector) widget).addSelectorHandler(this);
          break;
        case COL_TA_RENT_PROJECT:
          rentProject = (DataSelector) widget;
          rentProject.addSelectorHandler(this);
          break;
      }
    }

    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof DataSelector && name != null) {
      switch (name) {
        case WIDGET_TA_CONTRACT:
          contractSelector = (DataSelector) widget;
          contractSelector.addSelectorHandler(this);
          break;
        case TradeActConstants.COL_TA_OBJECT:
          objectSelector = (DataSelector) widget;
          objectSelector.addSelectorHandler(this);
          break;
        case TradeActConstants.COL_TA_COMPANY:
          companySelector = (DataSelector) widget;
          companySelector.addSelectorHandler(this);
          break;
      }
    }

    if (widget instanceof ChildGrid && name != null) {
      switch (name) {
        case GRID_TRADE_ACT_ITEMS:
          tradeActItemsGrid = (ChildGrid) widget;
          break;
        case "RPTradeActItems":
          rpTradeActItemsGrid = (ChildGrid) widget;
          rpTradeActItemsGrid.setGridInterceptor(new TradeActItemsGrid() {
            @Override
            protected void renderHeader(IsRow parentRow, GridView gridView) {
              if (commandSale != null) {
                commandSale.removeFromParent();
              }

              commandSale = new Button(Localized.dictionary().trdTypeSale());

              commandSale.addClickHandler(arg0 -> createSale());

              HeaderView formHeader = getFormHeader(gridView);

              if (formHeader != null && isSaleTradeAct(parentRow)
                  && BeeKeeper.getUser().canCreateData(VIEW_SALES)
                  && !TradeActKeeper.isClientArea() && !hasContinuousAct(parentRow)) {

                gridView.getViewPresenter().getHeader().addCommandItem(commandSale);
              }

              if (commandImportItems != null) {
                commandImportItems.setVisible(TradeActKeeper.isEnabledItemsGrid(getKind(parentRow),
                    ViewHelper.getForm(gridView), parentRow));
              }
            }
          });
        case GRID_TRADE_ACTS:
          tradeActsGrid = (ChildGrid) widget;
          break;
        case "RPTradeActs":
          rpTradeActsGrid = (ChildGrid) widget;
          rpTradeActsGrid.setGridInterceptor(new TradeActGrid(null));
          break;
        case "RelTradeActServices":
          relTradeActServices = (ChildGrid) widget;
          relTradeActServices.setGridInterceptor(new TradeActServicesGrid());
          break;
        case "CompActionList":
          ((ChildGrid) widget).setGridInterceptor(new RelatedAppointmentsGrid(COL_TRADE_ACT));
          break;
      }
    }

    if (widget instanceof TabbedPages && "TabbedPages".equals(name)) {
      tabbedPages = (TabbedPages) widget;
    }
    if (widget instanceof FaLabel && Objects.equals(name, "InputTime")) {
      ((FaLabel) widget).addClickHandler(clickEvent -> {
        DateTime current = getDateTimeValue(COL_TA_DATE);

        if (Objects.isNull(current)) {
          getFormView().notifyWarning("Įveskite datą");
          return;
        }
        InputTimeOfDay inputTime = new InputTimeOfDay();
        inputTime.setStepValue(10);
        inputTime.setValue(current.toTimeString());

        Global.inputWidget("Įveskite laiką", inputTime, () -> {
          getActiveRow().setValue(getDataIndex(COL_TA_DATE),
              TimeUtils.combine(current, inputTime.getMillis()));

          getFormView().refreshBySource(COL_TA_DATE);
        });
      });
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void afterRefresh(FormView form, final IsRow row) {
    TradeActKind kind = getKind(row);

    setFormCaption(form, row);
    setFormStyles(form, row);
    clearUnboundSelectorValues(form);

    if (companySelector != null) {
      if (DataUtils.isId(getRentProjectCompany(row))) {
        companySelector.getOracle()
            .setAdditionalFilter(Filter.idIn(Arrays.asList(getRentProjectCompany(row))), true);
      } else {
        companySelector.getOracle().setAdditionalFilter(Filter.isTrue(), true);
      }

      contractSelector.getOracle().setAdditionalFilter(getRelatedDocumentsFilter(), true);
    }

    HeaderView header = getHeaderView(form);
    header.clearCommandPanel();

    if (!DataUtils.isNewRow(row)) {
      commandCompose =
          new Button(Localized.dictionary().taInvoiceCompose(), e -> onCommandComposeClick(row));

      if (!isReturnAct(row) && !isRelatedWithContinuousAct(row) && !TradeActKeeper.isClientArea()) {
        header.addCommandItem(commandCompose);
      }
    }

    setEnabledItemsGrid(kind, form, row);
    createReqLabels(form, !TradeActUtils.getMultiReturnData(row).isNull());

    if (!TradeActKeeper.isClientArea() && !header.isActionEnabled(Action.SAVE)) {
      header.showAction(Action.SAVE, true);
    }

    super.afterRefresh(form, row);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    if (action == null) {
      return super.beforeAction(null, presenter);
    }

    if (getFormView() == null) {
      return super.beforeAction(action, presenter);
    }

    if (getActiveRow() == null) {
      return super.beforeAction(action, presenter);
    }

    switch (action) {
      case SAVE:
        if (DataUtils.isId(getSeries()) && !BeeUtils.isEmpty(getSeriesName())) {
          BeeKeeper.getStorage()
              .set(TradeActKeeper.getStorageKey(COL_SERIES), BeeUtils.toString(getSeries()));
          BeeKeeper.getStorage()
              .set(TradeActKeeper.getStorageKey(COL_SERIES_NAME), getSeriesName());
        }

        return validateBeforeSave(getFormView(), getActiveRow(), true);
    }

    return super.beforeAction(action, presenter);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    objectSelector.setEnabled(true);

    super.beforeRefresh(form, row);
  }

  @Override
  public boolean beforeCreateWidget(String name, Element description) {
    if (BeeUtils.same(COL_TA_COMPANY, name)) {
      description.setAttribute("editForm", TradeActKeeper.isClientArea()
          ? ClassifierConstants.FORM_NEW_COMPANY : ClassifierConstants.FORM_COMPANY);
    }

    if (TradeActKeeper.isClientArea() && description.hasAttribute("disablable")) {
      description.setAttribute("disablable", "true");
    }

    return super.beforeCreateWidget(name, description);
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
  public void onEditEnd(EditEndEvent event, Object source) {
    if (event == null || BeeUtils.isEmpty(event.getColumnId()))  {
      return;
    }

    if (COL_TA_DATE.equals(event.getColumnId())) {
      appendReturnTime(event);
    }

    super.onEditEnd(event, source);
  }

  @Override
  public void onCellUpdate(CellUpdateEvent event) {
  }

  @Override
  public void onLoad(FormView form) {
    dataHandlerRegistry.addAll(BeeKeeper.getBus().registerUpdateHandler(this, false));
    super.onLoad(form);
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    event.setConsumed(!validateBeforeSave(getFormView(), getActiveRow(), false));

    if (!event.isConsumed()) {
      getActiveRow().setProperty(PRP_INSERT_COLS,
          Codec.beeSerialize(DataUtils.getColumnNames(event.getColumns())));
    }
  }

  @Override
  public void onRowUpdate(RowUpdateEvent event) {
    FormView form = getFormView();
    if (form == null) {
      return;
    }

    if (event.hasView(ClassifierConstants.VIEW_COMPANIES)
        && event.getRowId() == BeeUtils.unbox(form.getLongValue(COL_TA_COMPANY))) {

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
  public void onSourceChange(IsRow row, String source, String value) {
    if (COL_TA_DATE.equals(source)) {
      getFormView().refreshBySource(source);
    }

    super.onSourceChange(row, source, value);
  }

  @Override
  public boolean onStartEdit(final FormView form, final IsRow row,
      final ScheduledCommand focusCommand) {

    hasInvoicesOrSecondaryActs = false;

    if (form != null && DataUtils.hasId(row)) {
      TradeActKind kind = getKind(row);

      if (kind != null && kind.enableInvoices()) {
        ParameterList params = TradeActKeeper.createArgs(SVC_HAS_INVOICES_OR_SECONDARY_ACTS);
        params.addQueryItem(COL_TRADE_ACT, row.getId());

        BeeKeeper.getRpc().makeRequest(params, response -> {
          hasInvoicesOrSecondaryActs = response.hasResponse()
              && BeeConst.isTrue(response.getResponseAsString());

          form.updateRow(row, true);
          if (focusCommand != null) {
            focusCommand.execute();
          }
        });

        return false;
      }
    }

    if (form != null && !row.hasPropertyValue(PRP_MULTI_RETURN_DATA)
        && DataUtils.isId(row.getLong(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_RENT_PROJECT)))) {
      DataInfo dataInfo = Data.getDataInfo(VIEW_TRADE_ACTS);
      Queries.getRowSet(VIEW_TRADE_ACTS, dataInfo.getColumnNames(false),
          Filter.and(Filter.equals(COL_TA_RENT_PROJECT,
              row.getLong(Data.getColumnIndex(VIEW_TRADE_ACTS, COL_TA_RENT_PROJECT))),
              Filter.or(Filter.equals(COL_TA_KIND, TradeActKind.SALE),
                  Filter.equals(COL_TA_KIND, TradeActKind.SUPPLEMENT))),
          result -> {
            List<IsRow> rows = new ArrayList<>(result.getRows());
            ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_MULTI_RETURN);
            params.addQueryItem(Service.VAR_LIST, DataUtils.buildIdList(DataUtils.getRowIds(rows)));
            params.addQueryItem("DEBUG", "[TradeActFrom][onStartEdit]");

            BeeKeeper.getRpc().makePostRequest(params, response -> {
              if (response.hasResponse(BeeRowSet.class)) {
                BeeRowSet parentItems = BeeRowSet.restore(response.getResponseAsString());

                BeeRowSet parentActs = Data.createRowSet(VIEW_TRADE_ACTS);
                for (IsRow parent : rows) {
                  parentActs.addRow(DataUtils.cloneRow(parent));
                }

                row.setProperty(PRP_MULTI_RETURN_DATA, Codec.beeSerialize(Pair.of(parentActs,
                    parentItems)));
              }
            });
          });
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

    } else if (BeeUtils.same(event.getRelatedViewName(),
        ClassifierConstants.VIEW_COMPANY_OBJECTS)) {

      TradeActKind actKind = TradeActKeeper.getKind(getActiveRow(), getDataIndex(COL_TA_KIND));

      Queries.getRowCount(VIEW_TRADE_ACTS, Filter.or(Filter.equals(COL_TA_PARENT,
          getActiveRowId()), Filter.equals(COL_TA_CONTINUOUS, getActiveRowId()),
          Filter.equals(COL_TA_RETURN, getActiveRowId())), parentsCount -> {

        Long parentId = getFormView().getLongValue(COL_TA_PARENT);
        Long returnActId = getFormView().getLongValue(COL_TA_RETURN);
        Long continuousActId = getFormView().getLongValue(COL_TA_CONTINUOUS);

        if (objectSelector != null && DataUtils.isId(getCompany())) {
          objectSelector.getOracle()
              .setAdditionalFilter(Filter.equals(COL_TA_COMPANY, getCompany()), true);
        }

        if (DataUtils.isId(parentId) || BeeUtils.isPositive(parentsCount)
            || BeeUtils.isPositive(returnActId) || BeeUtils.isPositive(continuousActId)) {
          objectSelector.setEnabled(false);
        } else if (Objects.equals(TradeActKind.RETURN, actKind)
            || Objects.equals(TradeActKind.CONTINUOUS, actKind)) {
          objectSelector.setEnabled(false);
        }
      });
    }

    if (BeeUtils.same(event.getRelatedViewName(), ClassifierConstants.VIEW_COMPANIES)) {

      if (event.isChanged()) {
        Filter relDocFilter = getRelatedDocumentsFilter();

        Filter relActiveDocFilter = Filter.and(relDocFilter,
            Filter.notNull(DocumentConstants.ALS_STATUS_MAIN));

        DataInfo viewDocuments = Data.getDataInfo(DocumentConstants.VIEW_DOCUMENTS);

        Queries.getRowSet(viewDocuments.getViewName(), viewDocuments.getColumnNames(false),
            relActiveDocFilter, null, result -> {
              if (result.getNumberOfRows() != 1) {
                if (companySelector != null) {
                  contractSelector.clearValue();
                }
                return;
              }

              if (companySelector != null) {
                contractSelector.setSelection(result.getRow(0), null, true);
              }
            });

        if (companySelector != null) {
          contractSelector.getOracle().setAdditionalFilter(relDocFilter, true);
        }
      }
    }

    if (BeeUtils.same(event.getRelatedViewName(), VIEW_TRADE_SERIES)) {
      if (event.isChanged()) {
        Long seriesId = event.getValue();

        if (BeeUtils.isPositive(seriesId)) {
          ParameterList params = TradeActKeeper.createArgs(SVC_GET_NEXT_ACT_NUMBER);

          params.addDataItem(COL_TA_SERIES, seriesId);
          params.addDataItem(Service.VAR_COLUMN, COL_TRADE_NUMBER);
          params.addDataItem(VAR_VIEW_NAME, getViewName());

          BeeKeeper.getRpc().makePostRequest(params, response -> {
            if (!response.hasErrors() && !response.isEmpty()) {

              String number = response.getResponseAsString();
              Data.setValue(getViewName(), getActiveRow(), COL_TRADE_NUMBER, number);
              getFormView().refreshBySource(COL_TRADE_NUMBER);
            }
          });
        }
      }
    }

    if (BeeUtils.same(event.getRelatedViewName(), VIEW_TRADE_ACTS)) {
      if (rentProject != null && DataUtils.isId(getCompany())) {
        rentProject.getOracle()
            .setAdditionalFilter(Filter.equals(COL_TA_COMPANY, getCompany()), true);
      }

      if (event.getSelector() == rentProject && event.getRelatedRow() != null && !DataUtils
          .isId(getContract())) {
        IsRow rentProject = event.getRelatedRow();

        Data.setValue(VIEW_TRADE_ACTS, getActiveRow(), COL_TA_CONTRACT,
            Data.getLong(VIEW_TRADE_ACTS, rentProject, COL_TA_CONTRACT));
        RelationUtils
            .setRelatedValues(Data.getDataInfo(VIEW_TRADE_ACTS), COL_TA_CONTRACT, getActiveRow(),
                rentProject);
      }
    }
  }

  @Override
  public void onUnload(FormView form) {
    EventUtils.clearRegistry(dataHandlerRegistry);

    super.onUnload(form);
  }

  boolean validateBeforeSave(FormView form, IsRow row, boolean beforeSave) {
     return checkMultiReturnItems(form, row, beforeSave)
            && TradeActValidator.validateTradeActForm(form, getItemsGrid(row), row, form.getViewName())
            && createReqFields(form, !TradeActUtils.getMultiReturnData(row).isNull());
  }

  private void appendReturnTime(EditEndEvent e) {
    if (!TradeActKind.RETURN.equals(getKind(getActiveRow()))) {
      return;
    }

    if (!BeeUtils.isEmpty(e.getOldValue())) {
      return;
    }

    Long defTime = Global.getParameterTime(PRM_DEFAULT_RETURN_ACT_TIME);

    if (!BeeUtils.isPositive(defTime)) {
      return;
    }

    DateTime newTime = TimeUtils.combine(new DateTime(BeeUtils.toLong(e.getNewValue())), defTime);
    e.setNewValue(BeeUtils.toString(newTime.getTime()));
  }

  private boolean checkMultiReturnItems(FormView form, IsRow row, boolean beforeSave) {
    boolean valid = !(isNewRow(row) && isReturnAct(row) && !TradeActUtils.getMultiReturnData(row).isNull()
            && beforeSave);

    if (!valid) {
      form.notifySevere(Localized.dictionary().allValuesEmpty(Localized.dictionary()
              .list(), Localized.dictionary().tradeActItems()));
    }

    return valid;
  }

  private String getColumnLabel(String column) {
    return Data.getColumnLabel(getViewName(), column);
  }

  private Long getCompany() {
    return getCompany(getActiveRow());
  }

  private Long getCompany(IsRow row) {
    return row.getLong(getDataIndex(COL_TA_COMPANY));
  }

  private Long getContract() {
    return getContract(getActiveRow());
  }

  private Long getContact(IsRow row) {
    return row.getLong(getDataIndex(COL_CONTACT));
  }

  private Long getContract(IsRow row) {
    return row.getLong(getDataIndex(COL_TA_CONTRACT));
  }

  private Boolean getContactPhysical(IsRow row) {
    return row.getBoolean(getDataIndex(ALS_CONTACT_PHYSICAL));
  }

  private Long getContinuousAct(IsRow row) {
    return row.getLong(getDataIndex(COL_TA_CONTINUOUS));
  }

  private DateTime getDate(IsRow row) {
    return row.getDateTime(getDataIndex(COL_TA_DATE));
  }

  private HeaderView getHeaderView(FormView formView) {

    if (headerView == null) {
      headerView = formView.getViewPresenter().getHeader();
    }

    return headerView;
  }

  private GridView getItemsGrid(IsRow row) {
    return TradeActKind.RENT_PROJECT.equals(getKind(row))
            ? ViewHelper.getChildGrid(getFormView().asWidget(), "RPTradeActItems")
            : ViewHelper.getChildGrid(getFormView().asWidget(), GRID_TRADE_ACT_ITEMS);
  }

  private TradeActKind getKind(IsRow row) {
    return TradeActKeeper.getKind(row, getDataIndex(COL_TA_KIND));
  }

  private String getRegistrationNo(IsRow row) {
    return row.getString(getDataIndex(COL_TA_REGISTRATION_NO));
  }

  private Filter getRelatedDocumentsFilter() {
    return Filter.in(Data.getIdColumn(DocumentConstants.VIEW_DOCUMENTS),
        DocumentConstants.VIEW_RELATED_DOCUMENTS, DocumentConstants.COL_DOCUMENT, Filter
            .equals(ClassifierConstants.COL_COMPANY, getCompany()));
  }

  private Long getRentProject(IsRow row) {
    return row.getLong(getDataIndex(COL_TA_RENT_PROJECT));
  }

  private DateTime getRentProjectDate(IsRow row) {
    return row.getDateTime(getDataIndex(ALS_RENT_PROJECT_DATE));
  }

  private Long getRentProjectCompany(IsRow row) {
    return row != null ? Data.getLong(VIEW_TRADE_ACTS, row, ALS_RENT_PROJECT_COMPANY) : null;
  }

  private Long getSeries() {
    return getSeries(getActiveRow());
  }

  private Long getSeries(IsRow row) {
    return row.getLong(getDataIndex(COL_SERIES));
  }

  private String getSeriesName() {
    return getSeriesName(getActiveRow());
  }

  private String getSeriesName(IsRow row) {
    return row.getString(getDataIndex(COL_SERIES_NAME));
  }

  private Widget getTabWidgetByKey(String key) {
    if (tabbedPages == null) {
      return null;
    }

    int index = tabbedPages.getTabIndexByDataKey(key);

    if (BeeUtils.clamp(index, 0, tabbedPages.getPageCount() - 1) != index) {
      return null;
    }

    return tabbedPages.getTabWidget(index);
  }

  private TradeActInvoiceBuilder getTradeActInvoiceBuilder(IsRow row) {
    return new TradeActInvoiceBuilder(getCompany(row), Collections.singletonList(row.getId()),
        false);
  }

  private Collection<UnboundSelector> getUnboundSelectors(FormView formView) {
    return UiHelper.getChildren(formView.asWidget(), UnboundSelector.class);
  }

  private boolean createReqFields(FormView form, boolean mayDefaults) {

    final String clsHasDefaults = "bee-hasDefaults";
    IsRow row = form.getActiveRow();
    boolean valid;
    Widget labelName = form.getWidgetByName("label" + COL_TA_NAME);

    if (row == null) {
      return false;
    }

    valid = (mayDefaults && StyleUtils.hasClassName(labelName.getElement(), clsHasDefaults))
        || DataUtils.isId(row.getLong(form.getDataIndex(COL_TA_NAME)));

    if (!valid) {
      form.notifySevere(getColumnLabel(COL_TA_NAME) + " "
          + Localized.dictionary().valueRequired());
      return false;
    }

    String[] fields = getKind(row) != null ? getKind(row).getReqFields() : new String[] {};

    for (String field : fields) {
      String value = row.getString(getDataIndex(field));
      Widget w = form.getWidgetByName("label" + field);

      valid = !BeeUtils.isEmpty(value) || (mayDefaults && StyleUtils
          .hasClassName(w.getElement(), clsHasDefaults));

      if (!valid) {
        form.notifySevere(getColumnLabel(field) + " " + Localized.dictionary().valueRequired());
        return false;
      }
    }

    return true;
  }

  private void clearUnboundSelectorValues(FormView formView) {
    if (BeeUtils.isEmpty(getUnboundSelectors(formView))) {
      return;
    }

    getUnboundSelectors(formView).forEach(UnboundSelector::clearValue);
  }

  private void createReqLabels(FormView form, boolean markAsDefaults) {
    IsRow row = form.getActiveRow();
    Widget wid;

    if (row == null) {
      return;
    }

    String[] fields = getKind(row) != null ? getKind(row).getReqFields() : new String[] {};

    for (String field : fields) {
      wid = form.getWidgetByName("label" + field, true);

      if (wid == null) {
        continue;
      }

      wid.addStyleName("bee-required");

      if (BeeUtils.same(field, COL_TA_DATE) && isReturnAct(row)) {
        wid.removeStyleName("bee-hasDefaults");
      }
    }

    if (!markAsDefaults) {
      return;
    }

    for (String field : VAR_COPY_TA_COLUMN_NAMES) {
      if (!BeeUtils.isEmpty(form.getStringValue(field))
          || BeeUtils.inListSame(field, COL_TA_INPUT_DRIVER, COL_TA_INPUT_VEHICLE)) {
        continue;
      }

      wid = form.getWidgetByName("label" + field, true);

      if (wid == null) {
        continue;
      }

      wid.addStyleName("bee-hasDefaults");
    }

    wid = form.getWidgetByName("labelNumber", true);
    if (wid != null) {
      wid.addStyleName("bee-hasDefaults");
    }
  }

  private boolean isContinuousAct(IsRow row) {
    return TradeActKind.CONTINUOUS == getKind(row);
  }

  private boolean isNewRow(IsRow row) {
    return DataUtils.isNewRow(row);
  }

  private boolean isProjectRentAct(IsRow row) {
    return TradeActKind.RENT_PROJECT == getKind(row);
  }

  private boolean isRelatedWithContinuousAct(IsRow row) {
    return DataUtils.isId(getContinuousAct(row));
  }

  private boolean isReturnAct(IsRow row) {
    return TradeActKind.RETURN == getKind(row);
  }

  private boolean isStatusPhaseApproved(IsRow row) {
    return BeeUtils.unbox(row.getBoolean(getDataIndex(TradeDocumentPhase.APPROVED.getStatusColumnName())));
  }

  private boolean isStatusPhaseCompleted(IsRow row) {
    return BeeUtils.unbox(row.getBoolean(getDataIndex(TradeDocumentPhase.COMPLETED.getStatusColumnName())));
  }

  private void onCommandComposeClick(IsRow row) {
    FormFactory.openForm(FORM_INVOICE_BUILDER, getTradeActInvoiceBuilder(row));
  }

  private void setEnabledItemsGrid(TradeActKind kind, FormView form, IsRow row) {
    if (tradeActItemsGrid == null) {
      return;
    }

    GridInterceptor interceptor;

    tradeActItemsGrid.setEnabled(TradeActKeeper.isEnabledItemsGrid(kind, form, row));
    tradeActItemsGrid.setPendingEnabled(TradeActKeeper.isEnabledItemsGrid(kind, form, row));
    interceptor = tradeActItemsGrid.getGridInterceptor();

    if (interceptor instanceof TradeActItemsGrid) {
      ((TradeActItemsGrid) interceptor).getCustomUIBuilder().setParentRow(row);
    }

    rpTradeActItemsGrid.setEnabled(false);
    interceptor = rpTradeActItemsGrid.getGridInterceptor();

    if (interceptor instanceof TradeActItemsGrid) {
      ((TradeActItemsGrid) interceptor).getCustomUIBuilder().setParentRow(row);
    }
  }

  private void setFormCaption(FormView form, IsRow row) {
    String caption;

    form.removeStyleName(isNewRow(row) ? STYLE_EDIT : STYLE_CREATE);
    form.addStyleName(isNewRow(row) ? STYLE_CREATE : STYLE_EDIT);

    if (DataUtils.isNewRow(row)) {
      caption = BeeUtils.join(" - ", Localized.dictionary().tradeActNew(),
          (getKind(row) == null) ? null : getKind(row).getCaption());

    } else {
      caption =
          (getKind(row) == null) ? Localized.dictionary().tradeAct() : getKind(row).getCaption();
    }

    if (form.getViewPresenter() != null && form.getViewPresenter().getHeader() != null) {
      form.getViewPresenter().getHeader().setCaption(caption);
    }
  }

  private void setFormStyles(FormView form, IsRow row) {
    TradeActKind kind = getKind(row);
    if (lastKind != kind) {
      if (lastKind != null) {
        form.removeStyleName(STYLE_PREFIX + lastKind.getStyleSuffix());
      }
      if (kind != null) {
        form.addStyleName(STYLE_PREFIX + kind.getStyleSuffix());
      }

      boolean hasServices = kind != null && kind.enableServices();
      boolean hasRelatedServices = kind != null && kind.enableRelatedServices();
      int selPage = tabbedPages != null ? tabbedPages.getTabIndexByDataKey(GRID_TRADE_ACT_ITEMS) : 0;

      if (getTabWidgetByKey(GRID_TRADE_ACT_SERVICES) != null) {
        getTabWidgetByKey(GRID_TRADE_ACT_SERVICES).setVisible(hasServices);
      }
      if (getTabWidgetByKey("RelTradeActServices") != null) {
        getTabWidgetByKey("RelTradeActServices").setVisible(hasRelatedServices);
      }
      if(getTabWidgetByKey(VIEW_TRADE_ACT_ITEMS_GROUPED) != null) {
        getTabWidgetByKey(VIEW_TRADE_ACT_ITEMS_GROUPED).setVisible(TradeActKind.RENT_PROJECT.equals(kind));

        selPage = TradeActKind.RENT_PROJECT.equals(kind)
                ? tabbedPages.getTabIndexByDataKey(VIEW_TRADE_ACT_ITEMS_GROUPED) : selPage;
      }


      if (tabbedPages != null) {
        tabbedPages.selectPage(selPage, TabbedPages.SelectionOrigin.CLICK);
      }

      boolean hasInvoices = kind != null && kind.enableInvoices();
      form.setStyleName(STYLE_HAS_INVOICES, hasInvoices);
      form.setStyleName(STYLE_NO_INVOICES, !hasInvoices);

      form.setStyleName(STYLE_HAS_CT_ACTS, isContinuousAct(row));
      form.setStyleName(STYLE_NO_CT_ACTS, !isContinuousAct(row));

      form.setStyleName(STYLE_HAS_RPR_ACTS, isProjectRentAct(row));
      form.setStyleName(STYLE_NO_RPR_ACTS, !isProjectRentAct(row));

      if (tradeActsGrid != null) {
        tradeActsGrid.setEnabled(false);

      }

      lastKind = kind;
    }
  }

  private void showCompanyFinancialState(SelectorEvent event) {
    if (event.isChanged() && event.getRelatedRow() != null) {
      ParameterList args = TradeActKeeper.createArgs(SVC_CREDIT_INFO);
      args.addDataItem(ClassifierConstants.COL_COMPANY, event.getRelatedRow().getId());

      BeeKeeper.getRpc().makePostRequest(args, response -> {
        response.notify(getFormView());
        if (response.hasErrors()) {
          return;
        }
        Map<String, String> result = Codec.deserializeLinkedHashMap(
            response.getResponseAsString());

        double limit = BeeUtils.toDouble(
            result.get(ClassifierConstants.COL_COMPANY_CREDIT_LIMIT));
        double debt = BeeUtils.toDouble(result.get(TradeConstants.VAR_DEBT));
        double overdue = BeeUtils.toDouble(result.get(TradeConstants.VAR_OVERDUE));
        double untolerated = BeeUtils.toDouble(result.get(TradeConstants.VAR_UNTOLERATED));
        String oldestDate = result.get(TradeConstants.COL_TRADE_DATE);
        String financialState = BeeUtils.trim(result.get(COL_COMPANY_FINANCIAL_STATE));
        String financialStateColor = BeeUtils.trim(result.get(COL_BACKGROUND));

        HtmlTable table = new HtmlTable();

        Label label = new Label(financialState);
        StyleUtils.setBackgroundColor(label, financialStateColor);
        table.setWidget(0, 1, label);
        table.setText(0, 0, Localized.dictionary().financialState() + ":");
        table.setText(1, 0, Localized.dictionary().creditLimit() + ":");
        table.setText(1, 1, BeeUtils.joinWords(limit,
            result.get(AdministrationConstants.COL_CURRENCY)));
        table.setText(2, 0, Localized.dictionary().trdDebt() + ":");
        table.setText(2, 1, String.valueOf(debt));
        table.setText(3, 0, Localized.dictionary().trdOverdue() + ":");
        table.setText(3, 1, String.valueOf(overdue));
        StyleUtils.setColor(table.getCellFormatter().getElement(3, 1), "red");
        table.setText(4, 0, "Netoleruojama skola:");
        table.setText(4, 1, String.valueOf(untolerated));
        StyleUtils.setBackgroundColor(table.getCellFormatter().getElement(4, 1), "red");
        StyleUtils.setColor(table.getCellFormatter().getElement(4, 1), "white");
        table.setText(5, 0, "Seniausia neapmokėta sąskaita:");
        table.setText(5, 1, oldestDate);

        String cap = BeeUtils.joinWords(result.get(ClassifierConstants.COL_COMPANY_NAME),
            result.get(ClassifierConstants.COL_COMPANY_TYPE));

        if (BeeUtils.isPositive(overdue) || BeeUtils.isPositive(untolerated)) {
          Global.showModalWidget(cap, table).hideOnEscape();
        }
      });
    }
  }
}
