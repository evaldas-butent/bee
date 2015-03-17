package com.butent.bee.client.view.form.interceptor;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.output.Exporter;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportInfo;
import com.butent.bee.client.output.ReportItem;
import com.butent.bee.client.output.ReportItem.Function;
import com.butent.bee.client.output.ReportNumericItem;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputBoolean;
import com.butent.bee.client.widget.InputText;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.ListBox;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.value.NumberValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XRow;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.LocalizableConstants;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.EnumUtils;
import com.butent.bee.shared.utils.StringList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExtendedReportInterceptor extends ReportInterceptor {

  private static final String NAME_REPORT_CONTAINER = "ReportContainer";
  private static final String NAME_DETALIZATION_CONTAINER = "DetalizationContainer";
  private static final String NAME_CALCULATION_CONTAINER = "CalculationContainer";
  private static final String NAME_FILTER_CONTAINER = "FilterContainer";

  private static final String NAME_CURRENCY = COL_CURRENCY;
  private static final String NAME_VAT = TradeConstants.COL_TRADE_VAT;

  private static final String STYLE_PREFIX = "bee-rep";

  private static final String STYLE_ITEM = STYLE_PREFIX + "-item";
  private static final String STYLE_CAPTION = STYLE_ITEM + "-cap";
  private static final String STYLE_CALCULATION = STYLE_ITEM + "-calc";
  private static final String STYLE_REMOVE = STYLE_ITEM + "-remove";

  private static final String STYLE_REPORT = STYLE_PREFIX + "-report";
  private static final String STYLE_REPORT_NORMAL = STYLE_PREFIX + "-report-normal";
  private static final String STYLE_REPORT_ACTIVE = STYLE_PREFIX + "-report-active";

  private static final String STYLE_FILTER_CAPTION = STYLE_PREFIX + "-filter-cap";
  private static final String STYLE_OPTION_CAPTION = STYLE_PREFIX + "-option-cap";

  private static final String STYLE_COLGROUP = STYLE_PREFIX + "-cgroup";
  private static final String STYLE_COLGROUP_HEADER = STYLE_COLGROUP + "-hdr";
  private static final String STYLE_COLGROUP_SUMMARY_HEADER = STYLE_COLGROUP + "-tot-hdr";

  private static final String STYLE_ROWGROUP = STYLE_PREFIX + "-rgroup";
  private static final String STYLE_ROWGROUP_COL_SUMMARY = STYLE_ROWGROUP + "-col-tot";
  private static final String STYLE_ROWGROUP_SUMMARY = STYLE_ROWGROUP + "-tot";

  private static final String STYLE_ROW = STYLE_PREFIX + "-row";
  private static final String STYLE_ROW_HEADER = STYLE_ROW + "-hdr";
  private static final String STYLE_ROW_SUMMARY = STYLE_ROW + "-tot";
  private static final String STYLE_ROW_SUMMARY_HEADER = STYLE_ROW_SUMMARY + "-hdr";

  private static final String STYLE_COL = STYLE_PREFIX + "-col";
  private static final String STYLE_COL_HEADER = STYLE_COL + "-hdr";
  private static final String STYLE_COL_SUMMARY = STYLE_COL + "-tot";

  private static final String STYLE_SUMMARY = STYLE_PREFIX + "-tot";
  private static final String STYLE_SUMMARY_HEADER = STYLE_SUMMARY + "-hdr";

  private final Report report;

  private final Set<ReportInfo> reports = new LinkedHashSet<>();
  private ReportInfo activeReport;

  public ExtendedReportInterceptor(Report report) {
    this.report = Assert.notNull(report);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      WidgetDescriptionCallback callback) {

    if (widget instanceof HasClickHandlers) {
      switch (name) {
        case NAME_FILTER_CONTAINER + "Add":
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              addFilterItems();
            }
          });
          break;

        case NAME_DETALIZATION_CONTAINER + "Add":
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              addDetalizationItems();
            }
          });
          break;

        case NAME_CALCULATION_CONTAINER + "Add":
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              addCalculationItems();
            }
          });
          break;
      }
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public boolean beforeAction(Action action, Presenter presenter) {
    switch (action) {
      case SAVE:
        if (activeReport != null && !activeReport.isEmpty()) {
          Global.inputString(Localized.getConstants().name(), null, new StringCallback() {
            @Override
            public void onSuccess(String value) {
              final ReportInfo rep = new ReportInfo(value);
              rep.deserialize(activeReport.serialize());

              for (ReportInfo reportInfo : reports) {
                if (Objects.equals(rep, reportInfo)) {
                  rep.setId(reportInfo.getId());
                  break;
                }
              }
              if (reports.contains(rep)) {
                Global.confirm(Localized.getConstants().reports(), Icon.QUESTION,
                    Arrays.asList(Localized.getMessages().valueExists(value),
                        Localized.getConstants().actionChange()), new ConfirmationCallback() {
                      @Override
                      public void onConfirm() {
                        saveReport(rep);
                      }
                    });
              } else {
                saveReport(rep);
              }
            }
          }, activeReport.getCaption());
        }
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public Set<Action> getEnabledActions(Set<Action> defaultActions) {
    Set<Action> actions = super.getEnabledActions(defaultActions);
    actions.add(Action.SAVE);
    return actions;
  }

  @Override
  public FormInterceptor getInstance() {
    return new ExtendedReportInterceptor(getReport());
  }

  @Override
  public void onLoad(FormView form) {
    ReportParameters parameters = readParameters();

    if (parameters != null) {
      loadId(parameters, NAME_CURRENCY, form);
      loadBoolean(parameters, NAME_VAT, form);
    }
    getReports();
    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeEditorValues(NAME_CURRENCY);
    storeBooleanValues(NAME_VAT);
  }

  @Override
  protected void clearFilter() {
    if (activeReport != null) {
      for (ReportItem item : activeReport.getFilterItems()) {
        item.clearFilter();
      }
    }
  }

  @Override
  protected void doReport() {
    if (getDataContainer() == null) {
      return;
    }
    getDataContainer().clear();

    if (activeReport == null || activeReport.isEmpty()) {
      return;
    }
    ParameterList params = BeeKeeper.getRpc()
        .createParameters(getReport().getModuleAndSub().getModule(), getReport().getReportName());

    String currency = getEditorValue(NAME_CURRENCY);

    if (DataUtils.isId(currency)) {
      params.addDataItem(COL_CURRENCY, currency);
    }
    if (getBoolean(NAME_VAT)) {
      params.addDataItem(TradeConstants.COL_TRADE_VAT, "1");
    }
    for (ReportItem item : activeReport.getFilterItems()) {
      String filterValue = item.getFilter();

      if (!BeeUtils.isEmpty(filterValue)) {
        params.addDataItem(item.getName(), filterValue);
      }
    }
    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        response.notify(getFormView());

        if (response.hasErrors()) {
          return;
        }
        renderData(SimpleRowSet.restore(response.getResponseAsString()));
      }
    });
  }

  @Override
  protected String getBookmarkLabel() {
    List<String> labels = StringList.of(getReportCaption(), getFilterLabel(NAME_CURRENCY));
    return BeeUtils.joinWords(labels);
  }

  @Override
  protected Report getReport() {
    return report;
  }

  @Override
  protected ReportParameters getReportParameters() {
    ReportParameters parameters = new ReportParameters();

    addEditorValues(parameters, NAME_CURRENCY);
    addBooleanValues(parameters, NAME_VAT);

    return parameters;
  }

  @Override
  protected void export() {
    if (getDataContainer() != null && !getDataContainer().isEmpty()) {
      XSheet sheet = getSheet((HtmlTable) getDataContainer().getWidget(0));

      if (!sheet.isEmpty()) {
        Exporter.maybeExport(sheet, getReportCaption());
      }
    }
  }

  @Override
  protected boolean validateParameters(ReportParameters parameters) {
    return true;
  }

  private void activateReport(ReportInfo activeRep) {
    activeReport = activeRep;
    HasWidgets container = (HasWidgets) getFormView().getWidgetByName(NAME_REPORT_CONTAINER);

    if (container != null) {
      container.clear();

      HtmlTable ft = new HtmlTable(STYLE_REPORT);
      int r = 0;

      for (final ReportInfo rep : reports) {
        if (Objects.equals(rep, activeReport)) {
          ft.getRowFormatter().addStyleName(r, STYLE_REPORT_ACTIVE);
        } else {
          ft.getRowFormatter().addStyleName(r, STYLE_REPORT_NORMAL);
        }
        Label name = new Label(rep.getCaption());
        name.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            if (!Objects.equals(rep, activeReport)) {
              activateReport(rep);
            }
          }
        });
        ft.setWidget(r, 0, name);
        CustomDiv remove = new CustomDiv(STYLE_REMOVE);

        if (DataUtils.isId(rep.getId())) {
          remove.setText(String.valueOf(BeeConst.CHAR_TIMES));
          remove.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              Global.confirmRemove(Localized.getConstants().reports(), rep.getCaption(),
                  new ConfirmationCallback() {
                    @Override
                    public void onConfirm() {
                      Queries.deleteRow(VIEW_REPORT_SETTINGS, rep.getId());
                      reports.remove(rep);
                      activateReport(Objects.equals(rep, activeReport)
                          ? BeeUtils.peek(reports) : activeReport);
                    }
                  });
            }
          });
        }
        ft.setWidget(r, 1, remove);
        r++;
      }
      container.add(ft);
      renderFilters();
      render(NAME_DETALIZATION_CONTAINER);
      render(NAME_CALCULATION_CONTAINER);
      getDataContainer().clear();
    }
  }

  private void addCalculationItems() {
    if (activeReport != null) {
      chooseItems(new HashSet<>(activeReport.getColItems()), true,
          new Consumer<List<ReportItem>>() {
            @Override
            public void accept(List<ReportItem> input) {
              for (ReportItem item : input) {
                activeReport.addColItem(item);
              }
              render(NAME_CALCULATION_CONTAINER);
            }
          });
    }
  }

  private void addDetalizationItems() {
    if (activeReport != null) {
      Set<ReportItem> skip = new HashSet<>(activeReport.getRowItems());

      for (ReportItem entry : getReport().getItems()) {
        if (entry instanceof ReportNumericItem) {
          skip.add(entry);
        }
      }
      chooseItems(skip, true, new Consumer<List<ReportItem>>() {
        @Override
        public void accept(List<ReportItem> input) {
          for (ReportItem item : input) {
            activeReport.addRowItem(item);
          }
          render(NAME_DETALIZATION_CONTAINER);
        }
      });
    }
  }

  private void addFilterItems() {
    if (activeReport != null) {
      final Collection<ReportItem> filterItems = activeReport.getFilterItems();
      Set<ReportItem> skip = new HashSet<>(filterItems);

      for (ReportItem item : getReport().getItems()) {
        if (item.getFilterWidget() == null) {
          skip.add(item);
        }
      }
      chooseItems(skip, true, new Consumer<List<ReportItem>>() {
        @Override
        public void accept(List<ReportItem> input) {
          for (ReportItem item : input) {
            filterItems.add(item);
          }
          renderFilters();
        }
      });
    }
  }

  private void chooseItems(Set<ReportItem> skip, boolean multi,
      final Consumer<List<ReportItem>> consumer) {

    LocalizableConstants loc = Localized.getConstants();
    final ListBox list = new ListBox(multi);

    for (ReportItem item : getReport().getItems()) {
      if (!BeeUtils.contains(skip, item)) {
        list.addItem(item.getCaption(), item.getName());
      }
    }
    if (list.isEmpty()) {
      getFormView().notifyWarning(loc.noData());
      return;
    }
    list.setVisibleItemCount(BeeUtils.max(BeeUtils.min(list.getItemCount(), 20), 5));

    Global.inputWidget(multi ? loc.values() : loc.value(), list, new InputCallback() {
      @Override
      public void onSuccess() {
        List<ReportItem> selected = new ArrayList<>();

        for (int i = 0; i < list.getItemCount(); i++) {
          OptionElement optionElement = list.getOptionElement(i);

          if (optionElement.isSelected()) {
            for (ReportItem item : getReport().getItems()) {
              if (BeeUtils.same(item.getName(), optionElement.getValue())) {
                selected.add(item);
                break;
              }
            }
          }
        }
        if (!BeeUtils.isEmpty(selected)) {
          consumer.accept(selected);
        }
      }
    });
  }

  private static String getItemStyle(ReportItem item) {
    if (item.getFunction() != null) {
      switch (item.getFunction()) {
        case LIST:
          return ReportItem.STYLE_TEXT;

        case COUNT:
        case SUM:
          return ReportItem.STYLE_NUM;

        default:
          break;
      }
    }
    return item.getStyle();
  }

  private void getReports() {
    reports.clear();

    Queries.getRowSet(VIEW_REPORT_SETTINGS, Arrays.asList(COL_RS_CAPTION, COL_RS_PARAMETERS),
        Filter.and(Filter.equals(COL_RS_USER, BeeKeeper.getUser().getUserId()),
            Filter.equals(COL_RS_REPORT, getReport().getReportName()),
            Filter.notNull(COL_RS_IS_REPORT)), new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            reports.addAll(getReport().getDefaults());

            for (int i = 0; i < result.getNumberOfRows(); i++) {
              ReportInfo rep = new ReportInfo(result.getString(i, COL_RS_CAPTION));
              rep.setId(result.getRow(i).getId());
              rep.deserialize(result.getString(i, COL_RS_PARAMETERS));

              if (reports.contains(rep)) {
                reports.remove(rep);
              }
              reports.add(rep);
            }
            activateReport(BeeUtils.peek(reports));
          }
        });
  }

  private static XSheet getSheet(HtmlTable table) {
    XSheet sheet = new XSheet();
    int bold = sheet.registerFont(XFont.bold());
    int italic = sheet.registerFont(XFont.italic());
    int boldItalic = sheet.registerFont(XFont.boldItalic());
    Map<String, Integer> styleMap = new HashMap<>();

    Integer idx = sheet.registerStyle(XStyle.right());
    styleMap.put(ReportItem.STYLE_NUM, idx);
    styleMap.put(STYLE_COLGROUP_HEADER, idx);

    styleMap.put(STYLE_COLGROUP, sheet.registerStyle(XStyle.center()));

    XStyle xs = XStyle.center();
    xs.setFontRef(bold);
    idx = sheet.registerStyle(xs);
    styleMap.put(STYLE_COL_HEADER, idx);
    styleMap.put(STYLE_COLGROUP_SUMMARY_HEADER, idx);
    styleMap.put(STYLE_ROW_SUMMARY_HEADER, idx);

    xs = new XStyle();
    xs.setFontRef(bold);
    idx = sheet.registerStyle(xs);
    styleMap.put(STYLE_ROW_HEADER, idx);
    styleMap.put(STYLE_COL_SUMMARY, idx);
    styleMap.put(STYLE_ROW_SUMMARY, idx);
    styleMap.put(STYLE_SUMMARY, idx);

    xs = XStyle.right();
    xs.setFontRef(bold);
    styleMap.put(STYLE_SUMMARY_HEADER, sheet.registerStyle(xs));

    xs = XStyle.background("whitesmoke");
    xs.setFontRef(italic);
    idx = sheet.registerStyle(xs);
    styleMap.put(STYLE_ROWGROUP, idx);
    styleMap.put(STYLE_ROWGROUP_COL_SUMMARY, idx);

    xs = XStyle.background("whitesmoke");
    xs.setFontRef(boldItalic);
    styleMap.put(STYLE_ROWGROUP_SUMMARY, sheet.registerStyle(xs));

    for (int r = 0; r < table.getRowCount(); r++) {
      int colSpan = 0;
      XRow xr = new XRow(r);
      sheet.add(xr);

      for (int c = 0; c < table.getCellCount(r); c++) {
        XCell xc = new XCell(c + colSpan);
        xr.add(xc);
        TableCellElement cell = table.getRowCells(r).get(c);
        xc.setColSpan(Math.max(cell.getColSpan(), 1));
        String text = cell.getInnerText();
        String[] styles = BeeUtils.split(table.getCellFormatter().getStyleName(r, c), ' ');
        Value value = null;
        xs = null;

        if (styles != null) {
          for (String style : styles) {
            if (styleMap.containsKey(style)) {
              if (xs == null) {
                xs = new XStyle();
              }
              xs = xs.merge(sheet.getStyle(styleMap.get(style)));
            }
            if (BeeUtils.same(style, ReportItem.STYLE_NUM)) {
              value = new NumberValue(BeeUtils.toDoubleOrNull(text));
            }
          }
        }
        if (xs != null) {
          xc.setStyleRef(sheet.registerStyle(xs));
        }
        xc.setValue(value != null ? value : new TextValue(text));
        colSpan += xc.getColSpan() - 1;
      }
    }
    sheet.autoSizeAll();
    return sheet;
  }

  private void render(final String containerName) {
    HasWidgets container = (HasWidgets) getFormView().getWidgetByName(containerName);
    HasWidgets groupContainer = (HasWidgets) getFormView().getWidgetByName(containerName + "Group");

    if (groupContainer != null) {
      groupContainer.clear();
    }
    if (container == null) {
      return;
    }
    container.clear();

    if (activeReport != null) {
      final Collection<ReportItem> items;
      ReportItem groupItem = null;
      final Consumer<ReportItem> groupSetter;

      switch (containerName) {
        case NAME_DETALIZATION_CONTAINER:
          items = activeReport.getRowItems();
          groupItem = activeReport.getRowGrouping();
          groupSetter = new Consumer<ReportItem>() {
            @Override
            public void accept(ReportItem input) {
              activeReport.setRowGrouping(input);
            }
          };
          break;

        case NAME_CALCULATION_CONTAINER:
          items = activeReport.getColItems();
          groupItem = activeReport.getColGrouping();
          groupSetter = new Consumer<ReportItem>() {
            @Override
            public void accept(ReportItem input) {
              activeReport.setColGrouping(input);
            }
          };
          break;

        default:
          Assert.untouchable();
          items = null;
          groupSetter = null;
          break;
      }
      for (final ReportItem item : items) {
        container.add(renderItem(containerName, item, new Runnable() {
          @Override
          public void run() {
            items.remove(item);
          }
        }));
      }
      if (groupContainer != null) {
        if (!BeeUtils.isEmpty(items)) {
          groupContainer.add(new InlineLabel(Localized.getConstants().groupBy() + ": "));

          if (groupItem != null) {
            groupContainer.add(renderItem(containerName, groupItem, new Runnable() {
              @Override
              public void run() {
                groupSetter.accept(null);
              }
            }));
          } else {
            FaLabel add = new FaLabel(FontAwesome.PLUS, true);
            add.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                Set<ReportItem> skip = new HashSet<>();

                for (ReportItem entry : getReport().getItems()) {
                  if (entry instanceof ReportNumericItem) {
                    skip.add(entry);
                  }
                }
                chooseItems(skip, false, new Consumer<List<ReportItem>>() {
                  @Override
                  public void accept(List<ReportItem> input) {
                    groupSetter.accept(BeeUtils.peek(input));
                    render(containerName);
                  }
                });
              }
            });
            groupContainer.add(add);
          }
        } else {
          groupSetter.accept(null);
        }
      }
    }
  }

  private void renderData(SimpleRowSet rowSet) {
    if (activeReport == null || activeReport.isEmpty()) {
      return;
    }
    if (DataUtils.isEmpty(rowSet)) {
      getFormView().notifyWarning(Localized.getConstants().nothingFound());
      return;
    }
    Map<String, Map<String, String[]>> rowGroups = new TreeMap<>();
    Set<String> colGroups = new TreeSet<>();
    Table<String, String, Object> values = HashBasedTable.create();

    Collection<ReportItem> rowItems = activeReport.getRowItems();
    Collection<ReportItem> colItems = activeReport.getColItems();

    for (final SimpleRow row : rowSet) {
      boolean ok = true;

      for (ReportItem filterItem : activeReport.getFilterItems()) {
        if (!filterItem.validate(row)) {
          ok = false;
          break;
        }
      }
      if (!ok) {
        continue;
      }
      String rowGroup = null;
      String colGroup = null;

      if (activeReport.getRowGrouping() != null) {
        rowGroup = activeReport.getRowGrouping().evaluate(row);
      }
      if (activeReport.getColGrouping() != null) {
        colGroup = activeReport.getColGrouping().evaluate(row);
      }
      rowGroup = BeeUtils.nvl(rowGroup, "");
      colGroup = BeeUtils.nvl(colGroup, "");
      String[] details = new String[rowItems.size()];
      StringBuilder sb = new StringBuilder();
      int i = 0;

      for (ReportItem item : rowItems) {
        String value = item.evaluate(row);
        sb.append(i).append(BeeUtils.notEmpty(value, " "));
        details[i++] = value;
      }
      String key = "r" + rowGroup + "v" + sb.toString() + "c" + colGroup;
      ok = BeeUtils.isEmpty(colItems);

      for (ReportItem item : colItems) {
        String col = item.getName();
        Object value = item.calculate(values.get(key, col), item.evaluate(row));

        if (value != null) {
          ok = true;
          values.put(key, col, value);
        }
      }
      if (ok) {
        if (!rowGroups.containsKey(rowGroup)) {
          rowGroups.put(rowGroup, new TreeMap<String, String[]>());
        }
        if (!colGroups.contains(colGroup)) {
          colGroups.add(colGroup);
        }
        Map<String, String[]> rowMap = rowGroups.get(rowGroup);
        key = sb.toString();

        if (!rowMap.containsKey(key)) {
          rowMap.put(key, details);
        }
      }
    }
    if (BeeUtils.isEmpty(rowGroups)) {
      getFormView().notifyWarning(Localized.getConstants().nothingFound());
      return;
    }
    HtmlTable table = new HtmlTable(STYLE_PREFIX);
    // HEADER
    int r = 0;
    int c = 0;

    if (activeReport.getColGrouping() != null) {
      if (!BeeUtils.isEmpty(rowItems)) {
        table.getCellFormatter().setColSpan(r, c, rowItems.size());
      }
      table.setText(r, c++, activeReport.getColGrouping().getCaption(), STYLE_COLGROUP_HEADER);

      for (String colGroup : colGroups) {
        table.getCellFormatter().setColSpan(r, c, colItems.size());
        table.setText(r, c++, colGroup, STYLE_COLGROUP);
      }
      r++;
    }
    c = 0;

    for (ReportItem item : rowItems) {
      table.setText(r, c++, item.getCaption(), STYLE_ROW_HEADER);
    }
    c = Math.max(c, 1);

    for (int i = 0; i < colGroups.size(); i++) {
      for (ReportItem item : colItems) {
        table.setText(r, c++, item.getCaption(), STYLE_COL_HEADER);
      }
    }
    if (activeReport.getColGrouping() != null) {
      int x = c;

      for (ReportItem item : colItems) {
        if (item.isRowSummary()) {
          table.setText(r, x++, item.getCaption(), STYLE_ROW_SUMMARY_HEADER);
        }
      }
      if (x > c) {
        table.getCellFormatter().setColSpan(0, colGroups.size() + 1, x - c);
        table.setText(0, colGroups.size() + 1, Localized.getConstants().total(),
            STYLE_COLGROUP_SUMMARY_HEADER);
      }
    }
    Table<String, String, Object> colTotals = null;

    for (ReportItem item : colItems) {
      if (item.isColSummary()) {
        colTotals = HashBasedTable.create();
        break;
      }
    }
    r++;
    // DETAILS
    for (String rowGroup : rowGroups.keySet()) {
      int groupIdx = BeeConst.UNDEF;
      Table<String, String, Object> groupTotals = null;

      if (activeReport.getRowGrouping() != null) {
        if (colTotals != null) {
          groupIdx = r;
          groupTotals = HashBasedTable.create();
        }
        if (!BeeUtils.isEmpty(rowItems)) {
          table.getCellFormatter().setColSpan(r, 0, rowItems.size());
        }
        table.setText(r++, 0, activeReport.getRowGrouping().getCaption() + ": " + rowGroup,
            STYLE_ROWGROUP);
      }
      for (Entry<String, String[]> entry : rowGroups.get(rowGroup).entrySet()) {
        c = 0;

        for (String detail : entry.getValue()) {
          table.setText(r, c++, detail, STYLE_ROW);
        }
        c = Math.max(c, 1);
        Map<String, Object> rowTotals = new HashMap<>();

        for (String colGroup : colGroups) {
          String key = "r" + rowGroup + "v" + entry.getKey() + "c" + colGroup;

          for (ReportItem item : colItems) {
            String col = item.getName();
            Object value = values.get(key, col);
            table.setText(r, c++, value != null ? value.toString() : null, STYLE_COL,
                getItemStyle(item));

            if (value != null) {
              if (item.isRowSummary() && activeReport.getColGrouping() != null) {
                rowTotals.put(col, item.summarize(rowTotals.get(col), value));
              }
              if (item.isColSummary()) {
                if (groupTotals != null) {
                  groupTotals.put(colGroup, col, item.summarize(groupTotals.get(colGroup, col),
                      value));
                }
                colTotals.put(colGroup, col, item.summarize(colTotals.get(colGroup, col), value));
              }
            }
          }
        }
        for (ReportItem item : colItems) {
          if (item.isRowSummary() && activeReport.getColGrouping() != null) {
            Object value = rowTotals.get(item.getName());
            table.setText(r, c++, value != null ? value.toString() : null, STYLE_ROW_SUMMARY,
                getItemStyle(item));
          }
        }
        r++;
      }
      if (groupTotals != null) {
        c = 1;
        Map<String, Object> rowTotals = new HashMap<>();

        for (String colGroup : colGroups) {
          for (ReportItem item : colItems) {
            Object value = null;

            if (item.isColSummary()) {
              String col = item.getName();
              value = groupTotals.get(colGroup, col);

              if (item.isRowSummary() && activeReport.getColGrouping() != null) {
                rowTotals.put(col, item.summarize(rowTotals.get(col), value));
              }
            }
            table.setText(groupIdx, c++, value != null ? value.toString() : null,
                STYLE_ROWGROUP_COL_SUMMARY, getItemStyle(item));
          }
        }
        for (ReportItem item : colItems) {
          if (item.isRowSummary() && activeReport.getColGrouping() != null) {
            Object value = rowTotals.get(item.getName());
            table.setText(groupIdx, c++, value != null ? value.toString() : null,
                STYLE_ROWGROUP_SUMMARY, getItemStyle(item));
          }
        }
      }
    }
    if (colTotals != null) {
      c = 0;

      if (!BeeUtils.isEmpty(rowItems)) {
        table.getCellFormatter().setColSpan(r, c, rowItems.size());
      }
      table.setText(r, c++, Localized.getConstants().totalOf(), STYLE_SUMMARY_HEADER);
      Map<String, Object> rowTotals = new HashMap<>();

      for (String colGroup : colGroups) {
        for (ReportItem item : colItems) {
          if (item.isColSummary()) {
            String col = item.getName();
            Object value = colTotals.get(colGroup, col);

            if (item.isRowSummary() && activeReport.getColGrouping() != null) {
              rowTotals.put(col, item.summarize(rowTotals.get(col), value));
            }
            table.setText(r, c, value != null ? value.toString() : null, STYLE_COL_SUMMARY,
                getItemStyle(item));
          }
          c++;
        }
      }
      for (ReportItem item : colItems) {
        if (item.isRowSummary() && activeReport.getColGrouping() != null) {
          Object value = rowTotals.get(item.getName());
          table.setText(r, c++, value != null ? value.toString() : null, STYLE_SUMMARY,
              getItemStyle(item));
        }
      }
    }
    getDataContainer().add(table);
  }

  private void renderFilters() {
    HasWidgets container = (HasWidgets) getFormView().getWidgetByName(NAME_FILTER_CONTAINER);

    if (container == null) {
      return;
    }
    container.clear();

    if (activeReport != null) {
      final Collection<ReportItem> filterItems = activeReport.getFilterItems();
      HtmlTable table = new HtmlTable();
      table.setColumnCellStyles(1, "width:100%;");
      int c = 0;

      for (final ReportItem item : filterItems) {
        Label label = new Label(item.getCaption());
        label.addStyleName(STYLE_FILTER_CAPTION);
        table.setWidget(c, 0, label);

        table.setWidget(c, 1, item.getFilterWidget());

        CustomDiv remove = new CustomDiv(STYLE_REMOVE);
        remove.setText(String.valueOf(BeeConst.CHAR_TIMES));
        remove.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            filterItems.remove(item);
            renderFilters();
          }
        });
        table.setWidget(c, 2, remove);
        c++;
      }
      container.add(table);
    }
  }

  private Widget renderItem(final String containerName, final ReportItem item,
      final Runnable removeCallback) {

    Flow box = new Flow(STYLE_ITEM);

    if (item.getFunction() != null) {
      InlineLabel label = new InlineLabel(item.getFunction().getCaption());
      label.addStyleName(STYLE_CALCULATION);
      box.add(label);
    }
    InlineLabel label = new InlineLabel(item.getCaption());
    label.addStyleName(STYLE_CAPTION);
    box.add(label);

    if (removeCallback != null) {
      CustomDiv remove = new CustomDiv(STYLE_REMOVE);
      remove.setText(String.valueOf(BeeConst.CHAR_TIMES));

      remove.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          removeCallback.run();
          render(containerName);
        }
      });
      box.add(remove);
    }
    box.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        HtmlTable table = new HtmlTable();
        table.setColumnCellClasses(0, STYLE_OPTION_CAPTION);
        int c = 0;

        final InputText expression = new InputText();
        expression.setValue(item.getExpression());
        expression.setEnabled(false);

        table.setText(c, 0, Localized.getConstants().expression());
        table.setWidget(c++, 1, expression);

        if (item.getOptionsWidget() != null) {
          table.setText(c, 0, item.getOptionsCaption());
          table.setWidget(c++, 1, item.getOptionsWidget());
        }
        final ListBox function;
        final InputBoolean colSummary;
        final InputBoolean rowSummary;

        if (item.getFunction() != null) {
          function = new ListBox();

          for (Function fnc : item.getAvailableFunctions()) {
            function.addItem(fnc.getCaption(), fnc.name());
          }
          function.setValue(item.getFunction().name());

          table.setText(c, 0, Localized.getConstants().value());
          table.setWidget(c++, 1, function);

          colSummary = new InputBoolean(Localized.getConstants().columnResults());
          colSummary.setChecked(item.isColSummary());
          table.setWidget(c++, 1, colSummary);

          if (activeReport.getColGrouping() != null) {
            rowSummary = new InputBoolean(Localized.getConstants().rowResults());
            rowSummary.setChecked(item.isRowSummary());
            table.setWidget(c++, 1, rowSummary);
          } else {
            rowSummary = null;
          }
        } else {
          function = null;
          colSummary = null;
          rowSummary = null;
        }
        Global.inputWidget(item.getCaption(), table, new InputCallback() {
          @Override
          public void onSuccess() {
            item.setExpression(expression.getValue()).saveOptions();

            if (function != null) {
              item.setFunction(EnumUtils.getEnumByName(Function.class, function.getValue()));

              if (colSummary != null) {
                item.setColSummary(colSummary.isChecked());
              }
              if (rowSummary != null) {
                item.setRowSummary(rowSummary.isChecked());
              }
            }
            render(containerName);
          }
        });
      }
    });
    return box;
  }

  private void saveReport(final ReportInfo rep) {
    if (DataUtils.isId(rep.getId())) {
      Queries.update(VIEW_REPORT_SETTINGS, rep.getId(), COL_RS_PARAMETERS,
          TextValue.of(rep.serialize()));

      if (!Objects.equals(rep, activeReport)) {
        activateReport(rep);
      }
    } else {
      Queries.insert(VIEW_REPORT_SETTINGS, Data.getColumns(VIEW_REPORT_SETTINGS,
          Arrays.asList(COL_RS_USER, COL_RS_REPORT, COL_RS_CAPTION, COL_RS_PARAMETERS,
              COL_RS_IS_REPORT)),
          Arrays.asList(BeeUtils.toString(BeeKeeper.getUser().getUserId()),
              getReport().getReportName(), rep.getCaption(), rep.serialize(), "1"), null,
          new RowCallback() {
            @Override
            public void onSuccess(BeeRow result) {
              if (reports.contains(rep)) {
                reports.remove(rep);
              }
              rep.setId(result.getId());
              reports.add(rep);
              activateReport(rep);
            }
          });
    }
  }
}
