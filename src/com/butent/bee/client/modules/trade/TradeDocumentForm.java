package com.butent.bee.client.modules.trade;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.TBL_RELATIONS;
import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.mail.MailConstants.COL_MESSAGE;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.Storage;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.composite.DataSelector;
import com.butent.bee.client.composite.TabGroup;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.event.logical.SelectorEvent;
import com.butent.bee.client.grid.ChildGrid;
import com.butent.bee.client.i18n.Format;
import com.butent.bee.client.layout.Direction;
import com.butent.bee.client.layout.Split;
import com.butent.bee.client.modules.classifiers.ClassifierUtils;
import com.butent.bee.client.modules.mail.NewMailMessage;
import com.butent.bee.client.modules.trade.acts.TradeActForm;
import com.butent.bee.client.output.ReportUtils;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.ViewHelper;
import com.butent.bee.client.view.add.ReadyForInsertEvent;
import com.butent.bee.client.view.edit.EditableWidget;
import com.butent.bee.client.view.edit.SaveChangesEvent;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.view.form.interceptor.PrintFormInterceptor;
import com.butent.bee.client.view.grid.GridView;
import com.butent.bee.client.widget.DecimalLabel;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InputDate;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.HasHtml;
import com.butent.bee.shared.HasOptions;
import com.butent.bee.shared.State;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.CellSource;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.RelationUtils;
import com.butent.bee.shared.data.event.CellUpdateEvent;
import com.butent.bee.shared.data.event.DataChangeEvent;
import com.butent.bee.shared.data.event.RowUpdateEvent;
import com.butent.bee.shared.data.filter.CompoundFilter;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.data.view.DataInfo;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.i18n.PredefinedFormat;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.administration.AdministrationConstants;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.payroll.PayrollConstants;
import com.butent.bee.shared.modules.trade.OperationType;
import com.butent.bee.shared.modules.trade.TradeDiscountMode;
import com.butent.bee.shared.modules.trade.TradeDocumentPhase;
import com.butent.bee.shared.modules.trade.TradeDocumentSums;
import com.butent.bee.shared.modules.trade.TradeVatMode;
import com.butent.bee.shared.modules.trade.acts.TradeActUtils;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.ui.HasCheckedness;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TradeDocumentForm extends PrintFormInterceptor {

  private static final BeeLogger logger = LogUtils.getLogger(TradeDocumentForm.class);

  private static final String NAME_SPLIT = "Split";

  private static final String NAME_STATUS_UPDATED = "StatusUpdated";

  private static final String NAME_SUM_EXPANDER = "SumExpander";
  private static final String NAME_SOUTH_EXPANDER = "SouthExpander";

  private static final String STYLE_SUM_EXPANDED =
      BeeConst.CSS_CLASS_PREFIX + "trade-document-sum-expanded";

  private static final double DEFAULT_SOUTH_PERCENT = 50d;

  private static String getStorageKey(Direction direction) {
    return Storage.getUserKey(NameUtils.getClassName(TradeDocumentForm.class),
        direction.name().toLowerCase());
  }

  private final TradeDocumentSums tdSums = new TradeDocumentSums();

  private int southExpandedFrom;

  TradeDocumentForm() {
  }

  @Override
  public void afterCreateEditableWidget(EditableWidget editableWidget, IdentifiableWidget widget) {
    if (widget instanceof DataSelector) {
      switch (editableWidget.getColumnId()) {
        case COL_TRADE_SUPPLIER:
        case COL_TRADE_CUSTOMER:
          ((DataSelector) widget).addSelectorHandler(ev ->
              onCompanySelection(editableWidget.getColumnId(), ev));

          if (COL_TRADE_CUSTOMER.equals(editableWidget.getColumnId())) {
            ((DataSelector) widget).addSelectorHandler(TradeActForm::showCompanyFinancialState);
          }
          break;
      }
    }
    super.afterCreateEditableWidget(editableWidget, widget);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (BeeUtils.same(name, COL_TRADE_DOCUMENT_PHASE) && widget instanceof TabGroup) {
      ((TabGroup) widget).addBeforeSelectionHandler(this::onPhaseTransition);

    } else if (BeeUtils.same(name, COL_TRADE_OPERATION) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          event.getSelector().setAdditionalFilter(getOperationFilter());
        } else if (event.isChanged()) {
          onOperationChange(event.getRelatedRow());
        }
      });

    } else if (BeeUtils.same(name, COL_TRADE_SERIES) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          event.getSelector().setAdditionalFilter(getSeriesFilter());
        }
      });

    } else if (BeeUtils.same(name, COL_TRADE_DOCUMENT_STATUS) && widget instanceof DataSelector) {
      ((DataSelector) widget).addSelectorHandler(event -> {
        if (event.isOpened()) {
          event.getSelector().setAdditionalFilter(getStatusFilter());
        }
      });

    } else if (BeeUtils.same(name, GRID_TRADE_DOCUMENT_ITEMS) && widget instanceof ChildGrid) {
      TradeDocumentItemsGrid tdiGrid = new TradeDocumentItemsGrid();

      tdiGrid.setTdsSupplier(() -> tdSums);
      tdiGrid.setTdsListener(this::refreshSums);

      ((ChildGrid) widget).setGridInterceptor(tdiGrid);

    } else if (BeeUtils.same(name, GRID_TRADE_PAYMENTS) && widget instanceof ChildGrid) {
      TradePaymentsGrid tpGrid = new TradePaymentsGrid();

      tpGrid.setTdsSupplier(() -> tdSums);
      tpGrid.setTdsListener((update) -> {
        double paid = tdSums.getPaid();
        IsRow row = BeeUtils.isTrue(update) ? getActiveRow() : null;

        refreshSum(PROP_TD_PAID, paid, row);
        refreshDebt(tdSums.getTotal(), paid, row);
      });

      ((ChildGrid) widget).setGridInterceptor(tpGrid);

    } else if (BeeUtils.same(name, NAME_SPLIT) && widget instanceof Split) {
      ((Split) widget).addMutationHandler(event -> {
        if (event.getSource() instanceof Split) {
          Direction direction = Direction.parse(event.getOptions());

          if (direction != null) {
            Split split = (Split) event.getSource();
            switch (direction) {
              case EAST:
                saveEastSize(split);
                break;

              case SOUTH:
                if (southExpandedFrom <= 0) {
                  saveSouthSize(split);
                }
                break;

              default:
                saveSplitLayout(split);
            }
          }
        }
      });

    } else if (BeeUtils.same(name, NAME_SUM_EXPANDER) && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(event -> {
        if (event.getSource() instanceof HasCheckedness && getFormView() != null) {
          if (((HasCheckedness) event.getSource()).isChecked()) {
            getFormView().addStyleName(STYLE_SUM_EXPANDED);
          } else {
            getFormView().removeStyleName(STYLE_SUM_EXPANDED);
          }
        }
      });

    } else if (BeeUtils.same(name, NAME_SOUTH_EXPANDER) && widget instanceof HasClickHandlers) {
      ((HasClickHandlers) widget).addClickHandler(event -> {
        if (event.getSource() instanceof HasCheckedness) {
          boolean expand = ((HasCheckedness) event.getSource()).isChecked();

          int top = BeeConst.UNDEF;
          if (event.getSource() instanceof HasOptions) {
            top = BeeUtils.toInt(((HasOptions) event.getSource()).getOptions());
          }

          expandSouth(expand, top);
        }
      });

    } else if (COL_TRADE_DATE.equals(name) || COL_TRADE_DOCUMENT_RECEIVED_DATE.equals(name)) {
      JustDate minDate = Global.getParameterDate(PRM_PROTECT_TRADE_DOCUMENTS_BEFORE);

      if (minDate != null && widget instanceof InputDate) {
        ((InputDate) widget).setMinDate(minDate);
      }
    }

    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public void beforeRefresh(FormView form, IsRow row) {
    Split split = getSplit(form);

    if (split != null) {
      Integer eastSize = BeeKeeper.getStorage().getInteger(getStorageKey(Direction.EAST));

      Double southPercent = BeeKeeper.getStorage().getDouble(getStorageKey(Direction.SOUTH));
      if (!BeeUtils.isPositive(southPercent)) {
        southPercent = DEFAULT_SOUTH_PERCENT;
      }

      boolean doLayout = false;

      if (southExpandedFrom <= 0) {
        int height = split.getOffsetHeight();
        int size = BeeUtils.round(height * southPercent / BeeConst.DOUBLE_ONE_HUNDRED);

        if (size > 0 && size < height
            && !Objects.equals(split.getDirectionSize(Direction.SOUTH), size)) {

          split.setDirectionSize(Direction.SOUTH, size, false);
          doLayout = true;

          logger.debug(getClass().getSimpleName(), State.LOADED, Direction.SOUTH,
              southPercent, height, size);
        }
      }

      if (BeeUtils.isPositive(eastSize) && BeeUtils.isLess(eastSize, split.getOffsetWidth())
          && !Objects.equals(split.getDirectionSize(Direction.EAST), eastSize)) {

        split.setDirectionSize(Direction.EAST, eastSize, false);
        doLayout = true;

        logger.debug(getClass().getSimpleName(), State.LOADED, Direction.EAST,
            split.getOffsetWidth(), eastSize);
      }

      if (doLayout) {
        split.doLayout();
      }
    }

    super.beforeRefresh(form, row);
  }

  @Override
  protected ReportUtils.ReportCallback getReportCallback() {
    return new ReportUtils.ReportCallback() {
      @Override
      public Widget getActionWidget() {
        FaLabel action = new FaLabel(FontAwesome.ENVELOPE_O);
        action.setTitle(Localized.dictionary().trWriteEmail());
        return action;
      }

      @Override
      public void accept(FileInfo fileInfo) {
        TradeActUtils.getInvoiceEmails(getFormView().getLongValue(COL_TRADE_CUSTOMER), emails ->
            NewMailMessage.create(emails, null, null, "Sąskaita", null,
                Collections.singleton(fileInfo), null, false, (messageId, saveMode) -> {
                  Queries.insertAndFire(TBL_RELATIONS, Data.getColumns(TBL_RELATIONS,
                      Queries.asList(COL_TRADE_DOCUMENT, COL_MESSAGE)),
                      Queries.asList(getActiveRowId(), messageId));

                  Queries.insert(GRID_TRADE_DOCUMENT_FILES,
                      Data.getColumns(GRID_TRADE_DOCUMENT_FILES,
                          Queries.asList(COL_TRADE_DOCUMENT, AdministrationConstants.COL_FILE)),
                      Queries.asList(getActiveRowId(), fileInfo.getId()), null,
                      result -> Data.refreshLocal(GRID_TRADE_DOCUMENT_FILES));
                },
                Global.getParameterText(PRM_INVOICE_MAIL_SIGNATURE)));
      }
    };
  }

  @Override
  protected void getReportData(Consumer<BeeRowSet[]> dataConsumer) {
    Queries.getRowSet(TBL_TRADE_DOCUMENT_ITEMS, null,
        Filter.equals(COL_TRADE_DOCUMENT, getActiveRowId()),
        result -> {
          result.addColumn(Data.getColumn(getViewName(), COL_TRADE_DOCUMENT_VAT_MODE));
          String mode = getStringValue(COL_TRADE_DOCUMENT_VAT_MODE);
          int idx = result.getNumberOfColumns() - 1;
          Map<Long, Double> prices = new HashMap<>();

          result.forEach(beeRow -> {
            prices.put(beeRow.getId(), beeRow.getDouble(result.getColumnIndex(COL_ITEM_PRICE)));
            beeRow.setValue(idx, mode);
          });
          if (prices.values().stream().anyMatch(Objects::isNull)) {
            Global.choiceWithCancel("Spausdinti prekes be kainos?", null,
                Arrays.asList("Taip", "Ne"), (choice) -> {
                  if (choice == 1) {
                    prices.entrySet().stream().filter(e -> Objects.isNull(e.getValue()))
                        .forEach(e -> result.removeRowById(e.getKey()));
                  }
                  dataConsumer.accept(new BeeRowSet[] {result});
                });
          } else {
            dataConsumer.accept(new BeeRowSet[] {result});
          }
        });
  }

  @Override
  protected void getReportParameters(Consumer<Map<String, String>> parametersConsumer) {
    Map<String, Long> companies = new HashMap<>();

    for (String col : Arrays.asList(COL_TRADE_SUPPLIER, COL_TRADE_CUSTOMER, COL_TRADE_PAYER)) {
      Long id = getLongValue(col);

      if (DataUtils.isId(id)) {
        companies.put(col, id);
      }
    }
    if (!companies.containsKey(COL_TRADE_SUPPLIER)) {
      companies.put(COL_TRADE_SUPPLIER, BeeKeeper.getUser().getCompany());
    }
    super.getReportParameters(defaultParameters ->
        ClassifierUtils.getCompaniesInfo(companies, companiesInfo -> {
          defaultParameters.putAll(companiesInfo);
          defaultParameters.put(COL_TRADE_AMOUNT, BeeUtils.toString(tdSums.getTotal()));
          parametersConsumer.accept(defaultParameters);
        }));
  }

  @Override
  public FormInterceptor getInstance() {
    return new TradeDocumentForm();
  }

  @Override
  public boolean isRowEditable(IsRow row) {
    return super.isRowEditable(row) && TradeUtils.isDocumentEditable(row);
  }

  private boolean missingRequired(TradeDocumentPhase phase) {
    OperationType operationType = getOperationType();

    if (operationType != null && TradeDocumentPhase.getStockPhases().contains(phase)) {
      Set<String> required = new HashSet<>();
      required.add(COL_TRADE_MANAGER);

      switch (operationType) {
        case SALE:
          required.add(COL_TRADE_SERIES);
          required.add(COL_TRADE_CUSTOMER);

          if (TradeDocumentPhase.APPROVED.equals(phase)) {
            required.add(COL_TRADE_NUMBER);
          }
          break;

        case PURCHASE:
          required.add(COL_TRADE_SUPPLIER);
          break;

        default:
          required.clear();
      }
      IsRow row = getActiveRow();

      String missing = required.stream()
          .filter(s -> row.isNull(getDataIndex(s)))
          .map(s -> Data.getColumnLabel(getViewName(), s))
          .collect(Collectors.joining(", "));

      if (!BeeUtils.isEmpty(missing)) {
        getFormView().notifySevere(Localized.dictionary().valueRequired(), missing);
        return true;
      }
    }
    return false;
  }

  private void onCompanySelection(String source, SelectorEvent event) {
    if (event.isChanged()) {
      long id = event.getValue();
      IsRow dataRow = getActiveRow();

      OperationType operationType = EnumUtils.getEnumByIndex(OperationType.class,
          dataRow.getInteger(getDataIndex(COL_OPERATION_TYPE)));

      if (operationType == null || !DataUtils.isId(id)) {
        return;
      }
      String col;

      if (operationType.consumesStock() && Objects.equals(source, COL_TRADE_CUSTOMER)) {
        col = COL_COMPANY_CREDIT_DAYS;

      } else if (operationType.producesStock() && Objects.equals(source, COL_TRADE_SUPPLIER)) {
        col = COL_COMPANY_SUPPLIER_DAYS;

      } else {
        return;
      }
      Queries.getValue(TBL_COMPANIES, id, col, days -> {
        String term = null;

        if (!BeeUtils.isEmpty(days)) {
          term = TimeUtils.startOfNextDay(TimeUtils.nextDay(getDateTimeValue(COL_TRADE_DATE),
              BeeUtils.toInt(days))).serialize();
        }
        getFormView().updateCell(COL_TRADE_TERM, term);
      });
    }
  }

  @Override
  public void onReadyForInsert(HasHandlers listener, ReadyForInsertEvent event) {
    if (missingRequired(getPhase())) {
      event.consume();
      return;
    }
    super.onReadyForInsert(listener, event);
  }

  @Override
  public void onSaveChanges(HasHandlers listener, SaveChangesEvent event) {
    if (missingRequired(getPhase())) {
      event.consume();
      return;
    }
    super.onSaveChanges(listener, event);
  }

  @Override
  public void onSetActiveRow(IsRow row) {
    if (row == null) {
      tdSums.clear();

    } else {
      if (tdSums.updateDocumentId(row.getId())) {
        tdSums.clearItems();
      }

      tdSums.updateDocumentDiscount(getDocumentDiscount(row));

      tdSums.updateDiscountMode(getDiscountMode(row));
      tdSums.updateVatMode(getVatMode(row));
    }

    refreshSums(false);
    refreshStatusLastUpdated(row);

    super.onSetActiveRow(row);
  }

  @Override
  public void onSourceChange(IsRow row, String source, String value) {
    if (row != null && !BeeUtils.isEmpty(source)) {
      switch (source) {
        case COL_TRADE_DOCUMENT_DISCOUNT:
          if (tdSums.updateDocumentDiscount(BeeUtils.toDoubleOrNull(value))) {
            refreshSums(true);
          }
          break;

        case COL_TRADE_DOCUMENT_DISCOUNT_MODE:
          if (tdSums.updateDiscountMode(EnumUtils.getEnumByIndex(TradeDiscountMode.class, value))) {
            refreshSums(true);
            refreshItems();
          }
          break;

        case COL_TRADE_DOCUMENT_VAT_MODE:
          if (tdSums.updateVatMode(EnumUtils.getEnumByIndex(TradeVatMode.class, value))) {
            refreshSums(true);
            refreshItems();
          }
          break;
      }
    }

    super.onSourceChange(row, source, value);
  }

  @Override
  public void onStartNewRow(final FormView form, IsRow row) {
    if (row.getEnum(form.getDataIndex(COL_TRADE_DOCUMENT_PHASE), TradeDocumentPhase.class)
        .modifyStock()) {
      Queries.getDistinctLongs(VIEW_SERIES_MANAGERS, COL_SERIES,
          Filter.and(Filter.equals(COL_SERIES_MANAGER, BeeKeeper.getUser().getUserId()),
              Filter.notNull(COL_SERIES_DEFAULT)), series -> {

            if (!BeeUtils.isEmpty(series)) {
              Queries.getRowSet(TBL_TRADE_SERIES, Collections.singletonList(COL_SERIES_NAME),
                  Filter.and(Filter.idIn(series), Filter.isNull(COL_FOR_ACTS)), result -> {

                    if (!DataUtils.isEmpty(result)) {
                      row.setValue(getDataIndex(COL_TRADE_SERIES), result.getString(0, 0));
                      form.refreshBySource(COL_TRADE_SERIES);
                    }
                  });
            }
          });
    }
    Queries.getRowSet(PayrollConstants.TBL_EMPLOYEES, null,
        Filter.equals(ClassifierConstants.COL_COMPANY_PERSON,
            BeeKeeper.getUser().getUserData().getCompanyPerson()), empl -> {

          if (!DataUtils.isEmpty(empl)) {
            RelationUtils.updateRow(Data.getDataInfo(getViewName()), COL_TRADE_MANAGER, row,
                Data.getDataInfo(PayrollConstants.TBL_EMPLOYEES), empl.getRow(0), true);

            form.refreshBySource(COL_TRADE_MANAGER);
          }
        });
    if (DataUtils.isId(Global.getParameterRelation(PRM_TRADE_WAREHOUSE))) {
      Queries.getRow(TBL_WAREHOUSES, Global.getParameterRelation(PRM_TRADE_WAREHOUSE), res -> {
        RelationUtils.updateRow(Data.getDataInfo(getViewName()), COL_TRADE_WAREHOUSE_FROM, row,
            Data.getDataInfo(TBL_WAREHOUSES), res, true);
        form.refreshBySource(COL_TRADE_WAREHOUSE_FROM);
      });
    }
    if (DataUtils.isId(Global.getParameterRelation(PRM_TRADE_ACT_WAREHOUSE))) {
      Queries.getRow(TBL_WAREHOUSES, Global.getParameterRelation(PRM_TRADE_ACT_WAREHOUSE), res -> {
        RelationUtils.updateRow(Data.getDataInfo(getViewName()), COL_TRADE_ACT_WAREHOUSE_FROM,
            row, Data.getDataInfo(TBL_WAREHOUSES), res, true);
        form.refreshBySource(COL_TRADE_ACT_WAREHOUSE_FROM);
      });
    }
    super.onStartNewRow(form, row);
  }

  double getTotal() {
    return tdSums.getTotal();
  }

  private Double getDocumentDiscount(IsRow row) {
    return DataUtils.getDoubleQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_DISCOUNT));
  }

  private TradeDiscountMode getDiscountMode(IsRow row) {
    return EnumUtils.getEnumByIndex(TradeDiscountMode.class,
        DataUtils.getIntegerQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_DISCOUNT_MODE)));
  }

  private static Split getSplit(FormView form) {
    Widget widget = (form == null) ? null : form.getWidgetByName(NAME_SPLIT);
    return (widget instanceof Split) ? (Split) widget : null;
  }

  private TradeVatMode getVatMode(IsRow row) {
    return EnumUtils.getEnumByIndex(TradeVatMode.class,
        DataUtils.getIntegerQuietly(row, getDataIndex(COL_TRADE_DOCUMENT_VAT_MODE)));
  }

  private Filter getOperationFilter() {
    CompoundFilter filter = Filter.and();

    if (getGridView() != null
        && getGridView().getGridInterceptor() instanceof TradeDocumentsGrid) {
      filter.add(((TradeDocumentsGrid) getGridView().getGridInterceptor()).getOperationsFilter());
    }
    if (DataUtils.isId(getActiveRowId())) {
      OperationType operationType = getOperationType();
      TradeDocumentPhase phase = getPhase();

      if (operationType != null && phase != null && phase.modifyStock()) {
        filter.add(Filter.equals(COL_OPERATION_TYPE, operationType));
      }
    }
    return filter;
  }

  private OperationType getOperationType() {
    return EnumUtils.getEnumByIndex(OperationType.class, getIntegerValue(COL_OPERATION_TYPE));
  }

  private TradeDocumentPhase getPhase() {
    return EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        getIntegerValue(COL_TRADE_DOCUMENT_PHASE));
  }

  private static Filter getSeriesFilter() {
    Long userId = BeeKeeper.getUser().getUserId();

    if (DataUtils.isId(userId)) {
      return Filter.custom(FILTER_USER_TRADE_SERIES, userId);
    } else {
      return null;
    }
  }

  private String getShortCaption() {
    String number = getStringValue(COL_TRADE_NUMBER);

    String s1;
    if (BeeUtils.isEmpty(number)) {
      s1 = BeeUtils.joinItems(getStringValue(COL_TRADE_DOCUMENT_NUMBER_1),
          getStringValue(COL_TRADE_DOCUMENT_NUMBER_2));
    } else {
      s1 = BeeUtils.joinWords(getStringValue(COL_TRADE_SERIES), number);
    }

    return BeeUtils.joinItems(s1, getStringValue(COL_OPERATION_NAME));
  }

  private Filter getStatusFilter() {
    TradeDocumentPhase phase = getPhase();
    return (phase == null) ? null : Filter.notNull(phase.getStatusColumnName());
  }

  private boolean isOwner(IsRow row) {
    Long owner = row.getLong(getDataIndex(COL_TRADE_DOCUMENT_OWNER));
    return owner == null || BeeKeeper.getUser().is(owner);
  }

  private void maybeClearStatus(final IsRow row) {
    TradeDocumentPhase phase = TradeUtils.getDocumentPhase(row);

    final int statusIndex = getDataIndex(COL_TRADE_DOCUMENT_STATUS);
    Long status = row.getLong(statusIndex);

    if (DataUtils.isId(status) && phase != null) {
      Queries.getValue(VIEW_TRADE_STATUSES, status, phase.getStatusColumnName(),
          result -> {
            if (!BeeConst.isTrue(result) && DataUtils.sameId(row, getActiveRow())) {
              getActiveRow().clearCell(statusIndex);
              RelationUtils.clearRelatedValues(Data.getDataInfo(getViewName()),
                  COL_TRADE_DOCUMENT_STATUS, getActiveRow());

              getFormView().refreshBySource(COL_TRADE_DOCUMENT_STATUS);
            }
          });
    }
  }

  private void onOperationChange(IsRow operationRow) {
    if (operationRow != null) {
      getFormView().updateCell(COL_TRADE_DOCUMENT_PRICE_NAME,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_PRICE));

      getFormView().updateCell(COL_TRADE_DOCUMENT_VAT_MODE,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_VAT_MODE));

      getFormView().updateCell(COL_TRADE_DOCUMENT_DISCOUNT_MODE,
          Data.getString(VIEW_TRADE_OPERATIONS, operationRow, COL_OPERATION_DISCOUNT_MODE));

      OperationType operationType = Data.getEnum(VIEW_TRADE_OPERATIONS, operationRow,
          COL_OPERATION_TYPE, OperationType.class);
      TradeDocumentPhase phase = getPhase();

      Long warehouseFrom = Data.getLong(VIEW_TRADE_OPERATIONS, operationRow,
          COL_OPERATION_WAREHOUSE_FROM);
      Long warehouseTo = Data.getLong(VIEW_TRADE_OPERATIONS, operationRow,
          COL_OPERATION_WAREHOUSE_TO);

      if (operationType != null && phase != null && !phase.modifyStock()
          && operationType.consumesStock() == DataUtils.isId(warehouseFrom)
          && operationType.producesStock() == DataUtils.isId(warehouseTo)) {

        DataInfo targetInfo = Data.getDataInfo(getViewName());
        DataInfo sourceInfo = Data.getDataInfo(VIEW_TRADE_OPERATIONS);

        if (!Objects.equals(warehouseFrom, getLongValue(COL_TRADE_WAREHOUSE_FROM))) {
          RelationUtils.maybeUpdateColumn(targetInfo, COL_TRADE_WAREHOUSE_FROM, getActiveRow(),
              sourceInfo, COL_OPERATION_WAREHOUSE_FROM, operationRow);

          RelationUtils.maybeUpdateColumn(targetInfo, ALS_WAREHOUSE_FROM_CODE, getActiveRow(),
              sourceInfo, ALS_WAREHOUSE_FROM_CODE, operationRow);
          RelationUtils.maybeUpdateColumn(targetInfo, ALS_WAREHOUSE_FROM_NAME, getActiveRow(),
              sourceInfo, ALS_WAREHOUSE_FROM_NAME, operationRow);

          getFormView().refreshBySource(COL_TRADE_WAREHOUSE_FROM);
        }

        if (!Objects.equals(warehouseTo, getLongValue(COL_TRADE_WAREHOUSE_TO))) {
          RelationUtils.maybeUpdateColumn(targetInfo, COL_TRADE_WAREHOUSE_TO, getActiveRow(),
              sourceInfo, COL_OPERATION_WAREHOUSE_TO, operationRow);

          RelationUtils.maybeUpdateColumn(targetInfo, ALS_WAREHOUSE_TO_CODE, getActiveRow(),
              sourceInfo, ALS_WAREHOUSE_TO_CODE, operationRow);
          RelationUtils.maybeUpdateColumn(targetInfo, ALS_WAREHOUSE_TO_NAME, getActiveRow(),
              sourceInfo, ALS_WAREHOUSE_TO_NAME, operationRow);

          getFormView().refreshBySource(COL_TRADE_WAREHOUSE_TO);
        }
      }

      GridView itemsGrid = ViewHelper.getChildGrid(getFormView(), GRID_TRADE_DOCUMENT_ITEMS);
      if (itemsGrid != null && itemsGrid.getGridInterceptor() instanceof TradeDocumentItemsGrid) {
        ((TradeDocumentItemsGrid) itemsGrid.getGridInterceptor()).refreshCommands();
      }
    }
  }

  private void onPhaseTransition(BeforeSelectionEvent<Integer> event) {
    final IsRow row = getActiveRow();

    final TradeDocumentPhase from = getPhase();
    final TradeDocumentPhase to = EnumUtils.getEnumByIndex(TradeDocumentPhase.class,
        event.getItem());

    boolean fromStock = from != null && from.modifyStock();
    boolean toStock = to != null && to.modifyStock();

    if (row == null || to == null || from == to) {
      event.cancel();
      return;
    }

    if (TradeUtils.isDocumentProtected(row) || !super.isRowEditable(row)) {
      event.cancel();
      return;
    }

    if (from != null && !from.isEditable(BeeKeeper.getUser().isAdministrator()) && !isOwner(row)) {
      event.cancel();
      return;
    }

    if (missingRequired(to)) {
      event.cancel();
      return;
    }

    if (DataUtils.isNewRow(row)) {
      setPhase(row, to);
      if (setOwner(row)) {
        getFormView().refreshBySource(COL_TRADE_DOCUMENT_OWNER);
      }

      maybeClearStatus(row);

    } else if (fromStock == toStock) {
      setPhase(row, to);
      if (setOwner(row)) {
        getFormView().refreshBySource(COL_TRADE_DOCUMENT_OWNER);
      }

      getFormView().saveChanges(result -> {
        getFormView().setEnabled(isRowEditable(result));
        maybeClearStatus(result);
      });

    } else {
      event.cancel();

      String frLabel = (from == null) ? BeeConst.NULL : from.getCaption();
      String toLabel = to.getCaption();
      String message = Localized.dictionary().trdDocumentPhaseTransitionQuestion(frLabel, toLabel);

      Global.confirm(getShortCaption(), Icon.QUESTION, Collections.singletonList(message),
          Localized.dictionary().actionChange(), Localized.dictionary().actionCancel(), () -> {
            if (DataUtils.sameId(row, getActiveRow())) {
              BeeRow newRow = DataUtils.cloneRow(getActiveRow());
              setPhase(newRow, to);
              setOwner(newRow);

              BeeRowSet rowSet = new BeeRowSet(getViewName(), getFormView().getDataColumns());
              rowSet.addRow(newRow);

              ParameterList params = TradeKeeper.createArgs(SVC_DOCUMENT_PHASE_TRANSITION);
              params.setSummary(getViewName(), newRow.getId());

              BeeKeeper.getRpc().sendText(params, rowSet.serialize(), response -> {
                if (Queries.checkRowResponse(SVC_DOCUMENT_PHASE_TRANSITION, getViewName(),
                    response)) {

                  BeeRow r = BeeRow.restore(response.getResponseAsString());

                  int numberIndex = getDataIndex(COL_TRADE_NUMBER);
                  String newNumber = r.getString(numberIndex);

                  IsRow oldRow = getFormView().getOldRow();

                  if (!BeeUtils.isEmpty(newNumber) && oldRow != null
                      && !Objects.equals(newNumber, oldRow.getString(numberIndex))) {

                    oldRow.setValue(numberIndex, newNumber);
                    getActiveRow().setValue(numberIndex, newNumber);
                  }

                  RowUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), r, true);
                  DataChangeEvent.fireRefresh(BeeKeeper.getBus(), VIEW_TRADE_STOCK);

                  getFormView().setEnabled(isRowEditable(getActiveRow()));
                }
              });
            }
          });
    }
  }

  private boolean setOwner(IsRow row) {
    int index = getDataIndex(COL_TRADE_DOCUMENT_OWNER);

    if (row == null || BeeKeeper.getUser().is(row.getLong(index))) {
      return false;

    } else {
      row.setValue(index, BeeKeeper.getUser().getUserId());
      RelationUtils.setUserFields(Data.getDataInfo(getViewName()), row, COL_TRADE_DOCUMENT_OWNER,
          BeeKeeper.getUser().getUserData());

      return true;
    }
  }

  private void setPhase(IsRow row, TradeDocumentPhase phase) {
    row.setValue(getDataIndex(COL_TRADE_DOCUMENT_PHASE), phase.ordinal());
  }

  private void refreshItems() {
    GridView gridView = ViewHelper.getChildGrid(getFormView(), GRID_TRADE_DOCUMENT_ITEMS);

    if (gridView != null) {
      gridView.refresh(false, false);
    }
  }

  private void refreshStatusLastUpdated(IsRow row) {
    final Widget widget = getWidgetByName(NAME_STATUS_UPDATED);

    if (widget instanceof HasHtml) {
      ((HasHtml) widget).setText(BeeConst.STRING_EMPTY);

      if (DataUtils.hasId(row)) {
        final long id = row.getId();

        Queries.getLastUpdated(TBL_TRADE_DOCUMENTS, id, COL_TRADE_DOCUMENT_STATUS,
            result -> {
              if (result != null && Objects.equals(getActiveRowId(), id)) {
                ((HasHtml) widget).setText(BeeUtils.joinWords(
                    Localized.dictionary().statusUpdated(),
                    Format.render(PredefinedFormat.DATE_SHORT_TIME_MEDIUM, result)));
              }
            });
      }
    }
  }

  private void clearSum(String name, IsRow row) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof DecimalLabel) {
      ((DecimalLabel) widget).setValue(null);
    }

    if (row != null) {
      String oldValue = row.getProperty(name);
      row.removeProperty(name);

      if (!BeeUtils.isEmpty(oldValue)) {
        CellUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), row.getId(), row.getVersion(),
            CellSource.forProperty(name, null, ValueType.DECIMAL), null);
      }
    }
  }

  private void refreshSum(String name, double value, IsRow row) {
    Widget widget = getFormView().getWidgetByName(name);
    if (widget instanceof DecimalLabel) {
      ((DecimalLabel) widget).setValue(BeeUtils.toDecimalOrNull(Localized.normalizeMoney(value)));
    }

    if (row != null) {
      String oldValue = row.getProperty(name);
      row.setNonZero(name, value);

      String newValue = row.getProperty(name);
      if (!Objects.equals(oldValue, newValue)) {
        CellUpdateEvent.fire(BeeKeeper.getBus(), getViewName(), row.getId(), row.getVersion(),
            CellSource.forProperty(name, null, ValueType.DECIMAL), newValue);
      }
    }
  }

  private void refreshSums(Boolean update) {
    double amount = tdSums.getAmount();
    double discount = tdSums.getDiscount();
    double vat = tdSums.getVat();
    double total = tdSums.getTotal();

    double paid = tdSums.getPaid();

    IsRow row = BeeUtils.isTrue(update) ? getActiveRow() : null;

    refreshSum(PROP_TD_AMOUNT, amount, row);
    refreshSum(PROP_TD_DISCOUNT, discount, row);
    refreshSum(PROP_TD_WITHOUT_VAT, total - vat, row);
    refreshSum(PROP_TD_VAT, vat, row);
    refreshSum(PROP_TD_TOTAL, total, row);

    refreshSum(PROP_TD_PAID, paid, row);
    refreshDebt(total, paid, row);
  }

  private void refreshDebt(double total, double paid, IsRow row) {
    if (Localized.isMoney(total - paid) && (BeeUtils.isPositive(paid) || showDebt())) {
      refreshSum(PROP_TD_DEBT, total - paid, row);
    } else {
      clearSum(PROP_TD_DEBT, row);
    }
  }

  private boolean showDebt() {
    OperationType operationType = getOperationType();
    return operationType != null && operationType.hasDebt();
  }

  private static void saveSplitLayout(Split split) {
    saveSouthSize(split);
    saveEastSize(split);
  }

  private static void saveSouthSize(Split split) {
    int southSize = split.getDirectionSize(Direction.SOUTH);
    int height = split.getOffsetHeight();

    if (height > 2) {
      southSize = BeeUtils.clamp(southSize, 1, height - 1);
      double southPercent = southSize * BeeConst.DOUBLE_ONE_HUNDRED / height;
      BeeKeeper.getStorage().set(getStorageKey(Direction.SOUTH), southPercent);
    }
  }

  private static void saveEastSize(Split split) {
    int eastSize = split.getDirectionSize(Direction.EAST);
    int width = split.getOffsetWidth();

    if (width > 2) {
      eastSize = BeeUtils.clamp(eastSize, 1, width - 1);
      BeeKeeper.getStorage().set(getStorageKey(Direction.EAST), eastSize);
    }
  }

  private void expandSouth(boolean expand, int top) {
    Split split = getSplit(getFormView());

    if (split != null) {
      int splitHeight = split.getOffsetHeight();

      int oldSize = split.getDirectionSize(Direction.SOUTH);
      int newSize;

      if (expand) {
        newSize = splitHeight - top;
        setSouthExpandedFrom(oldSize);

      } else {
        newSize = southExpandedFrom;
        setSouthExpandedFrom(BeeConst.UNDEF);
      }

      if (splitHeight <= 2) {
        newSize = oldSize;

      } else if (newSize <= 0 || newSize >= splitHeight) {
        if (expand) {
          newSize = BeeUtils.percent(splitHeight, 90d);
        } else {
          newSize = BeeUtils.percent(splitHeight, DEFAULT_SOUTH_PERCENT);
        }

        newSize = BeeUtils.clamp(newSize, 1, splitHeight - 1);
      }

      if (oldSize != newSize) {
        split.setDirectionSize(Direction.SOUTH, newSize, true);
      }
    }
  }

  private void setSouthExpandedFrom(int southExpandedFrom) {
    this.southExpandedFrom = southExpandedFrom;
  }
}
