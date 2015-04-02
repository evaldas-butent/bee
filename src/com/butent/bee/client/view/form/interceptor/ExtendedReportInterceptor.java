package com.butent.bee.client.view.form.interceptor;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.HasWidgets;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.Queries.RowSetCallback;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ChoiceCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.output.Exporter;
import com.butent.bee.client.output.Report;
import com.butent.bee.client.output.ReportExpressionItem;
import com.butent.bee.client.output.ReportInfo;
import com.butent.bee.client.output.ReportItem;
import com.butent.bee.client.output.ReportNumericItem;
import com.butent.bee.client.output.ReportParameters;
import com.butent.bee.client.output.ReportValue;
import com.butent.bee.client.presenter.Presenter;
import com.butent.bee.client.ui.FormFactory.WidgetDescriptionCallback;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.view.form.FormView;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.Label;
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
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.trade.TradeConstants;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExtendedReportInterceptor extends ReportInterceptor {

  private final class DrillHandler implements ClickHandler {

    private ReportInfo current;
    private String target;
    private String rowGroup;
    private String[] rowValues;
    private String colGroup;

    private DrillHandler(ReportInfo current, String target,
        String rowGroup, String[] rowValues, String colGroup) {

      this.current = Assert.notNull(current);
      this.target = target;
      this.rowGroup = rowGroup;
      this.rowValues = rowValues;
      this.colGroup = colGroup;
    }

    @Override
    public void onClick(ClickEvent event) {
      ReportInfo rep = null;

      if (!BeeUtils.isEmpty(target)) {
        for (ReportInfo reportInfo : reports) {
          if (Objects.equals(reportInfo.getCaption(), target)) {
            rep = ReportInfo.restore(reportInfo.serialize());
            break;
          }
        }
        if (rep == null) {
          getFormView().notifyWarning(Localized.getMessages().keyNotFound(target));
          return;
        }
        rep.getFilterItems().clear();
        rep.getFilterItems().addAll(current.getFilterItems());
      } else {
        rep = current;
      }
      List<ReportItem> filters = rep.getFilterItems();

      if (!BeeUtils.isEmpty(target)) {
        setFilter(filters, rowGroup, current.getRowGrouping());
      }
      setFilter(filters, colGroup, current.getColGrouping());

      if (!ArrayUtils.isEmpty(rowValues)) {
        List<ReportItem> items = current.getRowItems();

        for (int i = 0; i < rowValues.length; i++) {
          setFilter(filters, rowValues[i], items.get(i));
        }
      }
      getReport().open(new ReportParameters(Collections.singletonMap(COL_RS_REPORT,
          rep.serialize())));
    }

    private void setFilter(List<ReportItem> filters, String value, ReportItem item) {
      if (!BeeUtils.isEmpty(value) && item != null && item.getFilterWidget() != null) {
        ReportItem filter = null;

        for (ReportItem filterItem : filters) {
          if (Objects.equals(item, filterItem)) {
            filter = filterItem;
            break;
          }
        }
        if (filter == null) {
          filter = ReportItem.restore(item.serialize());
          filters.add(filter);
        }
        filter.setFilter(value);
      }
    }
  }

  private static final String NAME_REPORT_CONTAINER = "ReportContainer";
  private static final String NAME_DETALIZATION_CONTAINER = "DetalizationContainer";
  private static final String NAME_CALCULATION_CONTAINER = "CalculationContainer";
  private static final String NAME_FILTER_CONTAINER = "FilterContainer";

  private static final String NAME_CURRENCY = COL_CURRENCY;
  private static final String NAME_VAT = TradeConstants.COL_TRADE_VAT;

  private static final String STYLE_PREFIX = "bee-rep";

  private static final String STYLE_ITEM = STYLE_PREFIX + "-item";
  private static final String STYLE_REMOVE = STYLE_ITEM + "-remove";

  private static final String STYLE_REPORT = STYLE_PREFIX + "-report";
  private static final String STYLE_REPORT_NORMAL = STYLE_PREFIX + "-report-normal";
  private static final String STYLE_REPORT_ACTIVE = STYLE_PREFIX + "-report-active";

  private static final String STYLE_FILTER_CAPTION = STYLE_PREFIX + "-filter-cap";

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
  private static final String STYLE_DRILLDOWN = STYLE_PREFIX + "-drill";

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
              addFilterItem();
            }
          });
          break;

        case NAME_DETALIZATION_CONTAINER + "Add":
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              addDetalizationItem();
            }
          });
          break;

        case NAME_CALCULATION_CONTAINER + "Add":
          ((HasClickHandlers) widget).addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              addCalculationItem();
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
          }, null, activeReport.getCaption());
        }
        return false;

      default:
        return super.beforeAction(action, presenter);
    }
  }

  @Override
  public String getCaption() {
    return getReport().getReportCaption();
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
    ReportInfo rep = null;

    if (parameters != null) {
      String data = parameters.getText(COL_RS_REPORT);

      if (!BeeUtils.isEmpty(data)) {
        rep = ReportInfo.restore(data);
      }
      loadId(parameters, NAME_CURRENCY, form);
      loadBoolean(parameters, NAME_VAT, form);
    }
    getReports(rep);
    super.onLoad(form);
  }

  @Override
  public void onUnload(FormView form) {
    storeEditorValues(NAME_CURRENCY);
    storeBooleanValues(NAME_VAT);
  }

  @Override
  protected void clearFilter() {
    clearFilters(activeReport);
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
      String filterValue = item.serializeFilter();

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
    return null;
  }

  @Override
  protected Report getReport() {
    return report;
  }

  @Override
  protected ReportParameters getReportParameters() {
    if (activeReport == null) {
      return null;
    }
    ReportParameters parameters = new ReportParameters();

    parameters.add(COL_RS_REPORT, activeReport.serialize());

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

  private void addCalculationItem() {
    if (activeReport != null) {
      final List<ReportItem> items = new ArrayList<>();
      List<String> options = new ArrayList<>();

      for (ReportItem item : getReport().getItems()) {
        items.add(item);
        options.add(item.getCaption());
      }
      options.add(Localized.getConstants().expression() + "...");
      options.add(Localized.getConstants().formula() + "...");

      Global.choice(null, null, options, new ChoiceCallback() {
        @Override
        public void onSuccess(int value) {
          if (BeeUtils.isIndex(items, value)) {
            activeReport.addColItem(BeeUtils.getQuietly(items, value));
            render(NAME_CALCULATION_CONTAINER);
          } else {
            final ReportItem item = value == items.size()
                ? new ReportExpressionItem(null) : new ReportFormulaItem(null);

            List<String> relations = new ArrayList<>();

            for (ReportInfo rep : reports) {
              relations.add(rep.getCaption());
            }
            item.edit(getReport(), relations, new Runnable() {
              @Override
              public void run() {
                activeReport.addColItem(item);
                render(NAME_CALCULATION_CONTAINER);
              }
            });
            return;
          }
        }
      });
    }
  }

  private void addDetalizationItem() {
    if (activeReport != null) {
      final List<ReportItem> items = new ArrayList<>();
      List<String> options = new ArrayList<>();

      for (ReportItem item : getReport().getItems()) {
        if (!(item instanceof ReportNumericItem)) {
          items.add(item);
          options.add(item.getCaption());
        }
      }
      options.add(Localized.getConstants().expression() + "...");

      Global.choice(null, null, options, new ChoiceCallback() {
        @Override
        public void onSuccess(int value) {
          if (BeeUtils.isIndex(items, value)) {
            activeReport.addRowItem(BeeUtils.getQuietly(items, value));
            render(NAME_DETALIZATION_CONTAINER);
          } else {
            final ReportItem item = new ReportExpressionItem(null);
            List<String> relations = new ArrayList<>();

            for (ReportInfo rep : reports) {
              relations.add(rep.getCaption());
            }
            item.edit(getReport(), relations, new Runnable() {
              @Override
              public void run() {
                activeReport.addRowItem(item);
                render(NAME_DETALIZATION_CONTAINER);
              }
            });
            return;
          }
        }
      });
    }
  }

  private void addFilterItem() {
    if (activeReport != null) {
      final List<ReportItem> items = new ArrayList<>();
      List<String> options = new ArrayList<>();

      for (ReportItem item : getReport().getItems()) {
        if (item.getFilterWidget() != null) {
          items.add(item);
          options.add(item.getCaption());
        }
      }
      Global.choice(null, null, options, new ChoiceCallback() {
        @Override
        public void onSuccess(int value) {
          activeReport.getFilterItems().add(BeeUtils.getQuietly(items, value));
          renderFilters();
        }
      });
    }
  }

  private static void clearFilters(ReportInfo rep) {
    if (rep != null) {
      for (ReportItem item : rep.getFilterItems()) {
        item.clearFilter();
      }
    }
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

  private void getReports(final ReportInfo initialReport) {
    reports.clear();

    Queries.getRowSet(VIEW_REPORT_SETTINGS, Arrays.asList(COL_RS_PARAMETERS),
        Filter.and(Filter.equals(COL_RS_USER, BeeKeeper.getUser().getUserId()),
            Filter.equals(COL_RS_REPORT, getReport().getReportName()),
            Filter.isNull(COL_RS_CAPTION)), new RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            reports.addAll(getReport().getReports());

            for (int i = 0; i < result.getNumberOfRows(); i++) {
              ReportInfo rep = ReportInfo.restore(result.getString(i, COL_RS_PARAMETERS));
              rep.setId(result.getRow(i).getId());
              reports.remove(rep);
              reports.add(rep);
            }
            if (initialReport != null) {
              ReportInfo rep = initialReport;

              if (reports.contains(rep)) {
                for (ReportInfo reportInfo : reports) {
                  if (Objects.equals(reportInfo, rep)) {
                    reportInfo.deserialize(rep.serialize());
                    rep = reportInfo;
                    break;
                  }
                }
              } else {
                reports.add(rep);
              }
              activateReport(rep);
              doReport();
            } else {
              activateReport(BeeUtils.peek(reports));
            }
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
      final List<ReportItem> items;
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
      final List<String> relations = new ArrayList<>();

      for (ReportInfo rep : reports) {
        relations.add(rep.getCaption());
      }
      for (int i = 0; i < items.size(); i++) {
        container.add(ReportItem.renderDnd(items.get(i), items, i, getReport(), relations,
            new Runnable() {
              @Override
              public void run() {
                render(containerName);
              }
            }));
      }
      if (groupContainer != null) {
        if (!BeeUtils.isEmpty(items)) {
          groupContainer.add(new InlineLabel(Localized.getConstants().groupBy() + ": "));

          if (groupItem != null) {
            groupContainer.add(groupItem.render(getReport(), relations, new Runnable() {
              @Override
              public void run() {
                groupSetter.accept(null);
                render(containerName);
              }
            }, new Runnable() {
              @Override
              public void run() {
                render(containerName);
              }
            }).asWidget());
          } else {
            FaLabel add = new FaLabel(FontAwesome.PLUS, true);
            add.addClickHandler(new ClickHandler() {
              @Override
              public void onClick(ClickEvent event) {
                final List<ReportItem> choiceItems = new ArrayList<>();
                List<String> options = new ArrayList<>();

                for (ReportItem item : getReport().getItems()) {
                  if (!(item instanceof ReportNumericItem)) {
                    choiceItems.add(item);
                    options.add(item.getCaption());
                  }
                }
                options.add(Localized.getConstants().expression() + "...");

                Global.choice(Localized.getConstants().groupBy(), null, options,
                    new ChoiceCallback() {
                      @Override
                      public void onSuccess(int value) {
                        if (BeeUtils.isIndex(choiceItems, value)) {
                          groupSetter.accept(BeeUtils.getQuietly(choiceItems, value));
                          render(containerName);
                        } else {
                          final ReportItem item = new ReportExpressionItem(null);

                          item.edit(getReport(), relations, new Runnable() {
                            @Override
                            public void run() {
                              groupSetter.accept(item);
                              render(containerName);
                            }
                          });
                          return;
                        }
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
    getDataContainer().clear();

    if (activeReport == null || activeReport.isEmpty()) {
      return;
    }
    if (DataUtils.isEmpty(rowSet)) {
      getFormView().notifyWarning(Localized.getConstants().nothingFound());
      return;
    }
    Map<ReportValue, Map<String, ReportValue[]>> rowGroups = new TreeMap<>();
    Set<ReportValue> colGroups = new TreeSet<>();
    Table<String, String, Object> values = HashBasedTable.create();

    List<ReportItem> rowItems = activeReport.getRowItems();
    List<ReportItem> colItems = activeReport.getColItems();

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
      ReportValue rowGroup;
      ReportValue colGroup;

      if (activeReport.getRowGrouping() != null) {
        rowGroup = activeReport.getRowGrouping().evaluate(row);
      } else {
        rowGroup = ReportValue.empty();
      }
      if (activeReport.getColGrouping() != null) {
        colGroup = activeReport.getColGrouping().evaluate(row);
      } else {
        colGroup = ReportValue.empty();
      }
      ReportValue[] details = new ReportValue[rowItems.size()];
      List<String> sb = new ArrayList<>();

      for (int i = 0; i < rowItems.size(); i++) {
        ReportValue value = rowItems.get(i).evaluate(row);
        details[i] = value;
        sb.add(value.getValue());
      }
      String key = BeeUtils.join("", rowGroup.getValue(), sb, colGroup.getValue());
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
          rowGroups.put(rowGroup, new TreeMap<String, ReportValue[]>());
        }
        if (!colGroups.contains(colGroup)) {
          colGroups.add(colGroup);
        }
        Map<String, ReportValue[]> rowMap = rowGroups.get(rowGroup);
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
    ReportInfo activeCopy = ReportInfo.restore(activeReport.serialize());
    HtmlTable table = new HtmlTable(STYLE_PREFIX);
    // HEADER
    int r = 0;
    int c = 0;

    if (activeReport.getColGrouping() != null) {
      if (!BeeUtils.isEmpty(rowItems)) {
        table.getCellFormatter().setColSpan(r, c, rowItems.size());
      }
      table.setText(r, c++, activeReport.getColGrouping().getFormatedCaption(),
          STYLE_COLGROUP_HEADER);

      for (ReportValue colGroup : colGroups) {
        table.getCellFormatter().setColSpan(r, c, colItems.size());
        table.getCellFormatter().addStyleName(r, c, STYLE_COLGROUP);
        String text = colGroup.toString();

        if (!BeeUtils.isEmpty(text)) {
          if (!BeeUtils.isEmpty(activeReport.getColGrouping().getRelation())) {
            // DRILL DOWN
            Label label = new Label(text);
            label.addClickHandler(new DrillHandler(activeCopy,
                activeReport.getColGrouping().getRelation(), null, null, colGroup.getValue()));
            table.getCellFormatter().addStyleName(r, c, STYLE_DRILLDOWN);
            table.setWidget(r, c, label);
          } else {
            table.setText(r, c, text);
          }
        }
        c++;
      }
      r++;
    }
    c = 0;

    for (ReportItem item : rowItems) {
      table.setText(r, c++, item.getFormatedCaption(), STYLE_ROW_HEADER);
    }
    c = Math.max(c, 1);

    for (int i = 0; i < colGroups.size(); i++) {
      for (ReportItem item : colItems) {
        table.setText(r, c++, item.getFormatedCaption(), STYLE_COL_HEADER);
      }
    }
    if (activeReport.getColGrouping() != null) {
      int x = c;

      for (ReportItem item : colItems) {
        if (item.isRowSummary()) {
          table.setText(r, x++, item.getFormatedCaption(), STYLE_ROW_SUMMARY_HEADER);
        }
      }
      if (x > c) {
        table.getCellFormatter().setColSpan(0, colGroups.size() + 1, x - c);
        table.setText(0, colGroups.size() + 1, Localized.getConstants().total(),
            STYLE_COLGROUP_SUMMARY_HEADER);
      }
    }
    Table<ReportValue, String, Object> colTotals = null;

    for (ReportItem item : colItems) {
      if (item.isColSummary()) {
        colTotals = HashBasedTable.create();
        break;
      }
    }
    r++;
    // DETAILS
    for (ReportValue rowGroup : rowGroups.keySet()) {
      int groupIdx = BeeConst.UNDEF;
      Table<ReportValue, String, Object> groupTotals = null;

      if (activeReport.getRowGrouping() != null) {
        if (colTotals != null) {
          groupIdx = r;
          groupTotals = HashBasedTable.create();
        }
        if (!BeeUtils.isEmpty(rowItems)) {
          table.getCellFormatter().setColSpan(r, 0, rowItems.size());
        }
        table.getCellFormatter().addStyleName(r, 0, STYLE_ROWGROUP);
        String text = activeReport.getRowGrouping().getFormatedCaption() + ": " + rowGroup;

        if (BeeUtils.allNotEmpty(rowGroup.toString(),
            activeReport.getRowGrouping().getRelation())) {
          // DRILL DOWN
          Label label = new Label(text);
          label.addClickHandler(new DrillHandler(activeCopy,
              activeReport.getRowGrouping().getRelation(), rowGroup.getValue(), null, null));
          table.getCellFormatter().addStyleName(r, 0, STYLE_DRILLDOWN);
          table.setWidget(r, 0, label);
        } else {
          table.setText(r, 0, text);
        }
        r++;
      }
      for (Entry<String, ReportValue[]> entry : rowGroups.get(rowGroup).entrySet()) {
        c = 0;

        for (ReportValue detail : entry.getValue()) {
          ReportItem item = rowItems.get(c);
          table.getCellFormatter().addStyleName(r, c, STYLE_ROW);
          table.getCellFormatter().addStyleName(r, c, getItemStyle(item));
          String text = detail.toString();
          // DRILL DOWN
          if (!BeeUtils.isEmpty(text)) {
            if (!BeeUtils.isEmpty(item.getRelation()) || activeReport.getRowGrouping() != null) {
              String[] rowValues = new String[rowItems.size()];
              rowValues[c] = detail.getValue();
              Label label = new Label(text);
              label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                  rowGroup.getValue(), rowValues, null));
              table.getCellFormatter().addStyleName(r, c, STYLE_DRILLDOWN);
              table.setWidget(r, c, label);
            } else {
              table.setText(r, c, text);
            }
          }
          c++;
        }
        c = Math.max(c, 1);
        Map<String, Object> rowTotals = new HashMap<>();

        for (ReportValue colGroup : colGroups) {
          String key = BeeUtils.join("", rowGroup.getValue(), entry.getKey(), colGroup.getValue());

          for (ReportItem item : colItems) {
            String col = item.getName();
            Object value = values.get(key, col);
            String text = value != null ? value.toString() : null;

            table.getCellFormatter().addStyleName(r, c, STYLE_COL);
            table.getCellFormatter().addStyleName(r, c, getItemStyle(item));

            if (!BeeUtils.isEmpty(item.getRelation())) {
              // DRILL DOWN
              Label label = new Label(BeeUtils.notEmpty(text, BeeConst.HTML_NBSP));
              String[] rowValues = new String[entry.getValue().length];

              for (int i = 0; i < rowValues.length; i++) {
                rowValues[i] = entry.getValue()[i].getValue();
              }
              label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                  rowGroup.getValue(), rowValues, colGroup.getValue()));
              table.getCellFormatter().addStyleName(r, c, STYLE_DRILLDOWN);
              table.setWidget(r, c, label);

            } else if (!BeeUtils.isEmpty(text)) {
              table.setText(r, c, text);
            }
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
            c++;
          }
        }
        for (ReportItem item : colItems) {
          if (item.isRowSummary() && activeReport.getColGrouping() != null) {
            Object value = rowTotals.get(item.getName());
            String text = value != null ? value.toString() : null;

            table.getCellFormatter().addStyleName(r, c, STYLE_ROW_SUMMARY);
            table.getCellFormatter().addStyleName(r, c, getItemStyle(item));

            if (!BeeUtils.isEmpty(item.getRelation())) {
              // DRILL DOWN
              Label label = new Label(BeeUtils.notEmpty(text, BeeConst.HTML_NBSP));
              String[] rowValues = new String[entry.getValue().length];

              for (int i = 0; i < rowValues.length; i++) {
                rowValues[i] = entry.getValue()[i].getValue();
              }
              label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                  rowGroup.getValue(), rowValues, null));
              table.getCellFormatter().addStyleName(r, c, STYLE_DRILLDOWN);
              table.setWidget(r, c, label);

            } else if (!BeeUtils.isEmpty(text)) {
              table.setText(r, c, text);
            }
            c++;
          }
        }
        r++;
      }
      if (groupTotals != null) {
        c = 1;
        Map<String, Object> rowTotals = new HashMap<>();

        for (ReportValue colGroup : colGroups) {
          for (ReportItem item : colItems) {
            Object value = null;

            table.getCellFormatter().addStyleName(groupIdx, c, STYLE_ROWGROUP_COL_SUMMARY);
            table.getCellFormatter().addStyleName(groupIdx, c, getItemStyle(item));

            if (item.isColSummary()) {
              String col = item.getName();
              value = groupTotals.get(colGroup, col);

              if (item.isRowSummary() && activeReport.getColGrouping() != null) {
                rowTotals.put(col, item.summarize(rowTotals.get(col), value));
              }
              String text = value != null ? value.toString() : null;

              if (!BeeUtils.isEmpty(item.getRelation())) {
                // DRILL DOWN
                Label label = new Label(BeeUtils.notEmpty(text, BeeConst.HTML_NBSP));
                label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                    rowGroup.getValue(), null, colGroup.getValue()));
                table.getCellFormatter().addStyleName(groupIdx, c, STYLE_DRILLDOWN);
                table.setWidget(groupIdx, c, label);

              } else if (!BeeUtils.isEmpty(text)) {
                table.setText(groupIdx, c, text);
              }
            }
            c++;
          }
        }
        for (ReportItem item : colItems) {
          if (item.isRowSummary() && activeReport.getColGrouping() != null) {
            Object value = rowTotals.get(item.getName());
            String text = value != null ? value.toString() : null;

            table.getCellFormatter().addStyleName(groupIdx, c, STYLE_ROWGROUP_SUMMARY);
            table.getCellFormatter().addStyleName(groupIdx, c, getItemStyle(item));

            if (!BeeUtils.isEmpty(item.getRelation())) {
              // DRILL DOWN
              Label label = new Label(BeeUtils.notEmpty(text, BeeConst.HTML_NBSP));
              label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                  rowGroup.getValue(), null, null));
              table.getCellFormatter().addStyleName(groupIdx, c, STYLE_DRILLDOWN);
              table.setWidget(groupIdx, c, label);

            } else if (!BeeUtils.isEmpty(text)) {
              table.setText(groupIdx, c, text);
            }
            c++;
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

      for (ReportValue colGroup : colGroups) {
        for (ReportItem item : colItems) {
          if (item.isColSummary()) {
            String col = item.getName();
            Object value = colTotals.get(colGroup, col);

            if (item.isRowSummary() && activeReport.getColGrouping() != null) {
              rowTotals.put(col, item.summarize(rowTotals.get(col), value));
            }
            String text = value != null ? value.toString() : null;

            table.getCellFormatter().addStyleName(r, c, STYLE_COL_SUMMARY);
            table.getCellFormatter().addStyleName(r, c, getItemStyle(item));

            if (!BeeUtils.isEmpty(item.getRelation())) {
              // DRILL DOWN
              Label label = new Label(BeeUtils.notEmpty(text, BeeConst.HTML_NBSP));
              label.addClickHandler(new DrillHandler(activeCopy, item.getRelation(),
                  null, null, colGroup.getValue()));
              table.getCellFormatter().addStyleName(r, c, STYLE_DRILLDOWN);
              table.setWidget(r, c, label);

            } else if (!BeeUtils.isEmpty(text)) {
              table.setText(r, c, text);
            }
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
      final List<ReportItem> filterItems = activeReport.getFilterItems();
      HtmlTable table = new HtmlTable();
      table.setColumnCellClasses(0, STYLE_FILTER_CAPTION);
      table.setColumnCellStyles(1, "width:100%;");
      int c = 0;

      for (final ReportItem item : filterItems) {
        table.setText(c, 0, item.getCaption());
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

  private void saveReport(final ReportInfo rep) {
    clearFilters(rep);

    if (DataUtils.isId(rep.getId())) {
      Queries.update(VIEW_REPORT_SETTINGS, rep.getId(), COL_RS_PARAMETERS,
          TextValue.of(rep.serialize()));

      activateReport(rep);
    } else {
      Queries.insert(VIEW_REPORT_SETTINGS, Data.getColumns(VIEW_REPORT_SETTINGS,
          Arrays.asList(COL_RS_USER, COL_RS_REPORT, COL_RS_PARAMETERS)),
          Arrays.asList(BeeUtils.toString(BeeKeeper.getUser().getUserId()),
              getReport().getReportName(), rep.serialize()), null,
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
