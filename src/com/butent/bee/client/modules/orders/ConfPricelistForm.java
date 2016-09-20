package com.butent.bee.client.modules.orders;

import com.google.common.base.Predicates;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.Widget;

import static com.butent.bee.shared.modules.orders.OrdersConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.TabBar;
import com.butent.bee.client.composite.UnboundSelector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.InputCallback;
import com.butent.bee.client.dom.DomUtils;
import com.butent.bee.client.event.DndHelper;
import com.butent.bee.client.event.EventUtils;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.layout.Flow;
import com.butent.bee.client.ui.FormFactory;
import com.butent.bee.client.ui.IdentifiableWidget;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.view.TreeContainer;
import com.butent.bee.client.view.form.interceptor.AbstractFormInterceptor;
import com.butent.bee.client.view.form.interceptor.FormInterceptor;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.client.widget.CustomSpan;
import com.butent.bee.client.widget.FaLabel;
import com.butent.bee.client.widget.InlineLabel;
import com.butent.bee.client.widget.InputSpinner;
import com.butent.bee.client.widget.Label;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Dictionary;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.modules.classifiers.ClassifierConstants;
import com.butent.bee.shared.modules.orders.Bundle;
import com.butent.bee.shared.modules.orders.Configuration;
import com.butent.bee.shared.modules.orders.Dimension;
import com.butent.bee.shared.modules.orders.Option;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.ui.Orientation;
import com.butent.bee.shared.ui.Relation;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class ConfPricelistForm extends AbstractFormInterceptor
    implements SelectionHandler<IsRow> {

  private class GroupChoice implements ClickHandler {
    private final boolean rowMode;

    GroupChoice(boolean rowMode) {
      this.rowMode = rowMode;
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
      Queries.getRowSet(TBL_CONF_GROUPS, Collections.singletonList(COL_GROUP_NAME),
          new Queries.RowSetCallback() {
            @Override
            public void onSuccess(BeeRowSet result) {
              if (DataUtils.isEmpty(result)) {
                return;
              }
              List<String> choice = new ArrayList<>();
              Map<String, Dimension> map = new HashMap<>();

              for (BeeRow row : result) {
                String name = row.getString(0);
                Dimension dimension = new Dimension(row.getId(), name);

                if (!configuration.getAllDimensions().contains(dimension)) {
                  choice.add(name);
                  map.put(name, dimension);
                }
              }
              Dictionary loc = Localized.dictionary();

              if (choice.isEmpty()) {
                getFormView().notifyWarning(loc.noData());
              } else {
                Global.choice(rowMode ? loc.rows() : loc.columns(), loc.optionGroups(), choice,
                    idx -> {
                      String name = choice.get(idx);
                      configuration.addDimension(map.get(name), rowMode ? null : Integer.MIN_VALUE);
                      saveDimensions();
                      refresh();
                    });
              }
            }
          });
    }
  }

  private class OptionChoice implements ClickHandler {
    private final boolean rowMode;

    OptionChoice(boolean rowMode) {
      this.rowMode = rowMode;
    }

    @Override
    public void onClick(ClickEvent clickEvent) {
      List<Dimension> dimensions = rowMode
          ? configuration.getRowDimensions() : configuration.getColDimensions();

      HtmlTable table = new HtmlTable();
      int row = 0;
      int col = 0;
      List<UnboundSelector> inputs = new ArrayList<>();

      for (Dimension dimension : dimensions) {
        table.setText(row, col, dimension.getName());

        Relation relation = Relation.create(TBL_CONF_OPTIONS,
            Arrays.asList(COL_CODE, COL_OPTION_NAME));

        relation.setFilter(Filter.equals(COL_GROUP, dimension.getId()));

        UnboundSelector selector = UnboundSelector.create(relation);

        table.setWidget(rowMode ? row + 1 : row++, rowMode ? col++ : col + 1, selector);
        inputs.add(selector);
      }
      Dictionary loc = Localized.dictionary();

      Global.inputWidget(rowMode ? loc.rows() : loc.columns(), table, () -> {
        List<Option> options = new ArrayList<>();

        for (UnboundSelector input : inputs) {
          BeeRow beeRow = input.getRelatedRow();

          if (beeRow != null) {
            options.add(new Option(beeRow.getId(),
                Data.getString(TBL_CONF_OPTIONS, beeRow, COL_OPTION_NAME),
                new Dimension(Data.getLong(TBL_CONF_OPTIONS, beeRow, COL_GROUP),
                    Data.getString(TBL_CONF_OPTIONS, beeRow, COL_GROUP_NAME)))
                .setCode(Data.getString(TBL_CONF_OPTIONS, beeRow, COL_CODE)));
          }
        }
        if (!options.isEmpty()) {
          configuration.addBundle(new Bundle(options));
          refresh();
        }
      });
    }
  }

  private class CellToggle implements ClickHandler {
    private final List<Bundle> rows;
    private final List<Bundle> cols;

    CellToggle(List<Bundle> rows, List<Bundle> cols) {
      this.rows = rows;
      this.cols = cols;
    }

    @Override
    public void onClick(ClickEvent event) {
      TableCellElement cell = DomUtils.getParentCell(EventUtils.getEventTargetElement(event), true);
      int rIdx = DomUtils.getDataIndexInt(cell);
      int cIdx = DomUtils.getDataColumnInt(cell);
      Bundle bundle;
      List<Bundle> list = new ArrayList<>();

      if (BeeConst.isUndef(rIdx) && BeeConst.isUndef(cIdx)) {
        return;
      } else if (BeeConst.isUndef(rIdx)) {
        bundle = cols.get(cIdx);
        list.addAll(rows);
      } else if (BeeConst.isUndef(cIdx)) {
        bundle = rows.get(rIdx);
        list.addAll(cols);
      } else {
        bundle = Bundle.of(rows.get(rIdx), cols.get(cIdx));
      }
      if (cell.hasClassName(STYLE_ROW_DEL) || cell.hasClassName(STYLE_COL_DEL)) {
        Global.confirmRemove(cell.hasClassName(STYLE_ROW_DEL) ? Localized.dictionary().rows()
            : Localized.dictionary().columns(), bundle.toString(), () -> {
          Set<Bundle> bundles = new HashSet<>();
          bundles.add(bundle);

          for (Bundle b : list) {
            bundles.add(Bundle.of(bundle, b));
          }
          removeBundles(configuration.removeBundles(bundles));
          refresh();
        });
      } else {
        String price = configuration.getBundlePrice(bundle);

        inputPrice(price, true, newPrice -> {
          ParameterList args = OrdersKeeper.createSvcArgs(SVC_SET_BUNDLE);
          args.addDataItem(COL_BRANCH, getBranchId());
          args.addDataItem(COL_BUNDLE, Codec.beeSerialize(bundle));
          args.addDataItem(ClassifierConstants.COL_ITEM_PRICE, newPrice);
          BeeKeeper.getRpc().makePostRequest(args, defaultResponse);

          configuration.setBundlePrice(bundle, newPrice);
          refresh();
        }, BeeUtils.isEmpty(price) ? null : dialog -> {
          Runnable exec = () -> {
            dialog.close();
            configuration.setBundlePrice(bundle, null);
            removeBundles(Collections.singleton(bundle));
            refresh();
          };
          if (configuration.hasRelations(bundle)) {
            Global.confirmRemove(null, bundle.toString(), exec::run);
          } else {
            exec.run();
          }
        }, cell);
      }
    }
  }

  private static final String STYLE_PREFIX = "bee-conf";
  private static final String STYLE_CHOICE = STYLE_PREFIX + "-choice";
  private static final String STYLE_HEADER = STYLE_PREFIX + "-hdr";
  private static final String STYLE_ADD = STYLE_PREFIX + "-add";
  private static final String STYLE_REMOVE = STYLE_PREFIX + "-remove";

  private static final String STYLE_GROUP = STYLE_PREFIX + "-grp";
  private static final String STYLE_ROW = STYLE_PREFIX + "-row";
  private static final String STYLE_ROW_HEADER = STYLE_ROW + "-hdr";
  private static final String STYLE_ROW_ADD = STYLE_ROW + "-add";
  private static final String STYLE_ROW_DEL = STYLE_ROW + "-del";

  private static final String STYLE_COL = STYLE_PREFIX + "-col";
  private static final String STYLE_COL_HEADER = STYLE_COL + "-hdr";
  private static final String STYLE_COL_ADD = STYLE_COL + "-add";
  private static final String STYLE_COL_DEL = STYLE_COL + "-del";

  private static final String STYLE_CELL = STYLE_PREFIX + "-cell";
  private static final String STYLE_RESTRICTED = STYLE_PREFIX + "-restricted";
  private static final String STYLE_DEFAULT = STYLE_PREFIX + "-default";
  private static final String STYLE_OPTIONAL = STYLE_PREFIX + "-optional";

  private Flow container;
  private final TabBar bar;
  private TreeContainer tree;
  private Configuration configuration;
  private Integer rpcId;

  private ResponseCallback defaultResponse = new ResponseCallback() {
    @Override
    public void onResponse(ResponseObject response) {
      response.notify(getFormView());

      if (response.hasErrors()) {
        requery();
      }
    }
  };

  public ConfPricelistForm() {
    bar = new TabBar(Orientation.HORIZONTAL);
    bar.addStyleName(STYLE_CHOICE);

    bar.addItem(Localized.dictionary().configuration());
    bar.addItem(Localized.dictionary().options());

    bar.addSelectionHandler(selectionEvent -> refresh());
    bar.selectTab(0, false);
  }

  @Override
  public void afterCreateWidget(String name, IdentifiableWidget widget,
      FormFactory.WidgetDescriptionCallback callback) {

    if (widget instanceof TreeContainer) {
      tree = (TreeContainer) widget;
      tree.addSelectionHandler(this);

    } else if (Objects.equals(name, "Container") && widget instanceof Flow) {
      container = (Flow) widget;
    }
    super.afterCreateWidget(name, widget, callback);
  }

  @Override
  public FormInterceptor getInstance() {
    return new ConfPricelistForm();
  }

  @Override
  public void onSelection(SelectionEvent<IsRow> selectionEvent) {
    requery();
  }

  private String getBranchCaption() {
    List<String> cap = new ArrayList<>();
    IsRow item = tree.getSelectedItem();

    while (item != null) {
      cap.add(Data.getString(tree.getViewName(), item, COL_OPTION_NAME));
      item = tree.getParentItem(item);
    }
    Collections.reverse(cap);
    return BeeUtils.joinItems(cap);
  }

  private Long getBranchId() {
    IsRow item = tree.getSelectedItem();

    if (item == null) {
      return null;
    }
    return item.getId();
  }

  private static void inputPrice(String defaultPrice, boolean required, Consumer<String> consumer,
      Consumer<DialogBox> destroyer, Element target) {
    InputSpinner inputPrice = new InputSpinner();
    inputPrice.setMinValue("0");
    inputPrice.setValue(defaultPrice);

    Global.inputWidget(Localized.dictionary().price(), inputPrice, new InputCallback() {
      @Override
      public String getErrorMessage() {
        if (required && !BeeUtils.isNonNegativeInt(inputPrice.getValue())) {
          inputPrice.setFocus(true);
          return Localized.dictionary().valueRequired();
        }
        return null;
      }

      @Override
      public void onDelete(DialogBox dialog) {
        destroyer.accept(dialog);
      }

      @Override
      public void onSuccess() {
        consumer.accept(BeeUtils.isNonNegativeInt(inputPrice.getValue()) ? inputPrice.getValue()
            : null);
      }
    }, null, target, destroyer != null ? EnumSet.of(Action.DELETE) : null);
  }

  private void inputRestrictions(Option option, Element target) {
    HtmlTable table = new HtmlTable(STYLE_PREFIX);
    Map<Option, Boolean> restrictions = new TreeMap<>();
    restrictions.putAll(configuration.getRestrictions(option));

    Holder<Runnable> renderer = Holder.absent();
    renderer.set(() -> {
      table.clear();
      int r = 0;

      for (Option opt : restrictions.keySet()) {
        table.setText(r, 0, opt.getCode());
        table.setText(r, 1, opt.getName());

        Toggle toggle = new Toggle(FontAwesome.PLUS_CIRCLE, FontAwesome.BAN, STYLE_RESTRICTED,
            restrictions.get(opt));
        toggle.addClickHandler(clickEvent -> restrictions.put(opt, toggle.isChecked()));
        table.setWidget(r, 2, toggle);

        FaLabel trash = new FaLabel(FontAwesome.TRASH_O);
        trash.addClickHandler(clickEvent -> {
          restrictions.remove(opt);
          renderer.get().run();
        });
        table.setWidget(r, 3, trash);
        r++;
      }
    });
    renderer.get().run();

    Global.inputWidget(Localized.dictionary().restrictions(), table, new InputCallback() {
      @Override
      public void onAdd() {
        Set<Long> ids = new HashSet<>();

        for (Option opt : configuration.getOptions()) {
          ids.add(opt.getId());
        }
        ids.remove(option.getId());

        for (Option opt : restrictions.keySet()) {
          ids.remove(opt.getId());
        }
        Relation relation = Relation.create(TBL_CONF_OPTIONS,
            Arrays.asList(COL_CODE, COL_OPTION_NAME, COL_GROUP_NAME));

        relation.disableNewRow();
        relation.setFilter(Filter.idIn(ids));

        UnboundSelector inputOption = UnboundSelector.create(relation);
        inputOption.addSelectorHandler(event -> {
          BeeRow beeRow = event.getRelatedRow();

          if (event.isChanged() && beeRow != null) {
            UiHelper.getParentPopup(inputOption).close();

            restrictions.put(new Option(beeRow.getId(),
                Data.getString(TBL_CONF_OPTIONS, beeRow, COL_OPTION_NAME),
                new Dimension(Data.getLong(TBL_CONF_OPTIONS, beeRow, COL_GROUP),
                    Data.getString(TBL_CONF_OPTIONS, beeRow, COL_GROUP_NAME)))
                .setCode(Data.getString(TBL_CONF_OPTIONS, beeRow, COL_CODE)), false);
            renderer.get().run();
          }
        });
        Global.showModalWidget(Localized.dictionary().option(), inputOption, table.getElement());
      }

      @Override
      public void onSuccess() {
        Set<Option> opts = new HashSet<>();
        Map<Option, Boolean> oldRestrictions = new HashMap<>(configuration.getRestrictions(option));

        for (Option opt : oldRestrictions.keySet()) {
          Boolean denied = restrictions.remove(opt);

          if (!Objects.equals(denied, oldRestrictions.get(opt))) {
            if (denied == null) {
              configuration.removeRestriction(option, opt);
            } else {
              configuration.setRestriction(option, opt, denied);
            }
          }
          opts.add(opt);
        }
        for (Option opt : restrictions.keySet()) {
          configuration.setRestriction(option, opt, restrictions.get(opt));
          opts.add(opt);
        }
        if (!BeeUtils.isEmpty(opts)) {
          opts.add(option);
          Map<Long, Map<Long, Boolean>> data = new HashMap<>();

          for (Option opt : opts) {
            data.put(opt.getId(), new HashMap<>());

            for (Map.Entry<Option, Boolean> entry : configuration.getRestrictions(opt).entrySet()) {
              data.get(opt.getId()).put(entry.getKey().getId(), entry.getValue());
            }
          }
          ParameterList args = OrdersKeeper.createSvcArgs(SVC_SET_RESTRICTIONS);
          args.addDataItem(COL_BRANCH, getBranchId());
          args.addDataItem(TBL_CONF_RESTRICTIONS, Codec.beeSerialize(data));
          BeeKeeper.getRpc().makePostRequest(args, defaultResponse);
          refresh();
        }
      }
    }, null, target, EnumSet.of(Action.ADD));
  }

  private Widget makeDnd(String name, int idx, boolean rowMode) {
    Flow widget = new Flow();
    widget.add(new InlineLabel(name));
    int x = (idx + 1) * (rowMode ? 1 : (-1));

    if (!BeeConst.isUndef(idx)) {
      CustomSpan remove = new CustomSpan(STYLE_REMOVE);
      remove.addClickHandler(clickEvent -> {
        List<Dimension> target = rowMode ? configuration.getRowDimensions()
            : configuration.getColDimensions();

        Global.confirmRemove(Localized.dictionary().optionGroups(),
            BeeUtils.bracket(target.get(idx).toString()), () -> {
              removeBundles(configuration.removeBundlesByDimension(target.remove(idx)));
              saveDimensions();
              refresh();
            });
      });
      widget.add(remove);
      DndHelper.makeSource(widget, STYLE_PREFIX, x, null);
    }
    DndHelper.makeTarget(widget, Collections.singletonList(STYLE_PREFIX),
        (rowMode ? STYLE_ROW_HEADER : STYLE_COL_HEADER) + "-over",
        Predicates.not(o -> Objects.equals(o, x)), (ev, index) -> {
          int i = (int) index;
          List<Dimension> source;
          List<Dimension> target = rowMode ? configuration.getRowDimensions()
              : configuration.getColDimensions();

          if (BeeUtils.isNegative(i)) {
            i = Math.abs(i);
            source = configuration.getColDimensions();
          } else {
            source = configuration.getRowDimensions();
          }
          Dimension element = source.remove(--i);

          if (BeeUtils.isIndex(target, idx)) {
            target.add(idx, element);
          } else {
            target.add(element);
          }
          saveDimensions();
          refresh();
        });
    return widget;
  }

  private void removeBundles(Set<Bundle> bundles) {
    if (!BeeUtils.isEmpty(bundles)) {
      List<String> keys = new ArrayList<>();

      for (Bundle bundle : bundles) {
        keys.add(bundle.getKey());
      }
      ParameterList args = OrdersKeeper.createSvcArgs(SVC_DELETE_BUNDLES);
      args.addDataItem(COL_BRANCH, getBranchId());
      args.addDataItem(COL_KEY, Codec.beeSerialize(keys));
      BeeKeeper.getRpc().makePostRequest(args, defaultResponse);
    }
  }

  private void requery() {
    configuration = null;
    refresh();
    Long branchId = getBranchId();

    if (!DataUtils.isId(branchId)) {
      return;
    }
    ParameterList args = OrdersKeeper.createSvcArgs(SVC_GET_CONFIGURATION);
    args.addDataItem(COL_BRANCH, branchId);

    rpcId = BeeKeeper.getRpc().makePostRequest(args, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (!Objects.equals(getRpcId(), rpcId)) {
          return;
        }
        response.notify(getFormView());

        if (!response.hasErrors()) {
          configuration = Configuration.restore(response.getResponseAsString());
          bar.selectTab(0);
        }
      }
    });
  }

  private void refresh() {
    if (container == null) {
      return;
    }
    container.clear();

    if (configuration == null) {
      return;
    }
    bar.setTabEnabled(1, configuration.hasBundles());

    container.add(bar);

    if (bar.getSelectedTab() == 1 && !bar.isTabEnabled(1)) {
      bar.selectTab(0);
    } else {
      container.add(bar.getSelectedTab() == 0 ? renderConfiguration() : renderOptions());
    }
  }

  private Widget renderConfiguration() {
    HtmlTable table = new HtmlTable(STYLE_PREFIX);

    int rowSize = Math.max(configuration.getRowDimensions().size(), 1);
    int colSize = Math.max(configuration.getColDimensions().size(), 1);

    // TABLE CAPTION
    table.getCellFormatter().setRowSpan(0, 0, colSize);
    table.getCellFormatter().setColSpan(0, 0, rowSize);

    table.setText(0, 0, getBranchCaption(), STYLE_HEADER);
    Dictionary dict = Localized.dictionary();

    // ROW CAPTIONS
    int rIdx = colSize;
    int cIdx = 0;

    for (Dimension dimension : configuration.getRowDimensions()) {
      table.setWidget(rIdx, cIdx, makeDnd(dimension.getName(), cIdx, true), STYLE_ROW_HEADER);
      cIdx++;
    }
    if (cIdx == 0) {
      table.setWidget(rIdx, cIdx, makeDnd(dict.rows(), BeeConst.UNDEF, true), STYLE_ROW_HEADER);
      cIdx++;
    }
    CustomSpan rowAdd = new CustomSpan(STYLE_ROW_ADD);
    rowAdd.addClickHandler(new GroupChoice(true));
    table.setWidget(rIdx, cIdx, rowAdd);

    // COL CAPTIONS
    rIdx = 0;

    for (Dimension dimension : configuration.getColDimensions()) {
      table.setWidget(rIdx, rIdx > 0 ? 0 : 1, makeDnd(dimension.getName(), rIdx, false),
          STYLE_COL_HEADER);
      rIdx++;
    }
    if (rIdx == 0) {
      table.setWidget(rIdx, 1, makeDnd(dict.columns(), BeeConst.UNDEF, false), STYLE_COL_HEADER);
      rIdx++;
    }
    cIdx = rowSize;
    CustomSpan colAdd = new CustomSpan(STYLE_COL_ADD);
    colAdd.addClickHandler(new GroupChoice(false));

    Flow cont = new Flow(STYLE_ADD);
    cont.add(table.getWidget(rIdx, cIdx));
    cont.add(new CustomDiv());
    cont.add(colAdd);
    table.setWidget(rIdx, cIdx, cont);

    List<Bundle> rows = configuration.getMetrics(configuration.getRowDimensions());
    List<Bundle> cols = configuration.getMetrics(configuration.getColDimensions());

    // COLS
    for (int i = 0; i < cols.size(); i++) {
      int c = i;
      Bundle bundle = cols.get(i);
      Holder<Integer> x = Holder.of(colSize);

      Configuration.processOptions(bundle, configuration.getColDimensions(),
          (dimension, option) -> {
            table.setText(colSize - x.get(), c + (x.get() < colSize ? 1 : 2),
                option != null ? option.getName() : "", STYLE_COL);
            x.set(x.get() - 1);
            return true;
          });
      Element cell = table.getCellFormatter().ensureElement(colSize, i + rowSize + 1);
      cell.setClassName(STYLE_CELL);
      cell.setInnerText(configuration.getBundlePrice(bundle));

      DomUtils.setDataColumn(cell, i);
    }
    if (!configuration.getColDimensions().isEmpty()) {
      cIdx = cols.size() + 2;
      colAdd = new CustomSpan(STYLE_COL_ADD);
      colAdd.addClickHandler(new OptionChoice(false));
      table.setWidget(0, cIdx, colAdd, STYLE_COL_HEADER);
      table.getCellFormatter().setRowSpan(0, cIdx, colSize + 1);

      for (int i = 0; i < rows.size(); i++) {
        Element cell = table.getCellFormatter().ensureElement(i + colSize + 1, cIdx + rowSize - 1);
        cell.setClassName(STYLE_ROW_DEL);
        DomUtils.setDataIndex(cell, i);
      }
    }
    // ROWS
    rIdx = colSize + 1;

    for (Bundle bundle : rows) {
      int r = rIdx;
      Holder<Integer> i = Holder.of(rowSize);

      Configuration.processOptions(bundle, configuration.getRowDimensions(),
          (dimension, option) -> {
            table.setText(r, rowSize - i.get(), option != null ? option.getName() : "", STYLE_ROW);
            i.set(i.get() - 1);
            return true;
          });
      Element cell = table.getCellFormatter().ensureElement(rIdx, rowSize);
      cell.setClassName(STYLE_CELL);
      cell.setInnerText(configuration.getBundlePrice(bundle));

      DomUtils.setDataIndex(cell, rIdx - colSize - 1);
      rIdx++;
    }
    if (!configuration.getRowDimensions().isEmpty()) {
      rowAdd = new CustomSpan(STYLE_ROW_ADD);
      rowAdd.addClickHandler(new OptionChoice(true));
      table.setWidget(rIdx, 0, rowAdd, STYLE_ROW_HEADER);
      table.getCellFormatter().setColSpan(rIdx, 0, rowSize + 1);

      for (int i = 0; i < cols.size(); i++) {
        Element cell = table.getCellFormatter().ensureElement(rIdx, i + 1);
        cell.setClassName(STYLE_COL_DEL);
        DomUtils.setDataColumn(cell, i);
      }
    }
    // CELLS
    rIdx = colSize + 1;
    cIdx = rowSize + 1;

    for (Bundle rowBundle : rows) {
      for (Bundle colBundle : cols) {
        Element cell = table.getCellFormatter().ensureElement(rIdx, cIdx);
        cell.setClassName(STYLE_CELL);
        cell.setInnerText(configuration.getBundlePrice(Bundle.of(rowBundle,
            colBundle)));

        DomUtils.setDataIndex(cell, rIdx - colSize - 1);
        DomUtils.setDataColumn(cell, cIdx - rowSize - 1);
        cIdx++;
      }
      rIdx++;
      cIdx = rowSize + 1;
    }
    table.addClickHandler(new CellToggle(rows, cols));
    return table;
  }

  private Widget renderOptions() {
    HtmlTable table = new HtmlTable(STYLE_PREFIX);

    List<Dimension> allDimensions = configuration.getAllDimensions();

    // TABLE CAPTION
    int rIdx = 0;
    int cIdx = 0;
    int dimSize = allDimensions.size();

    table.getCellFormatter().setRowSpan(rIdx, cIdx, dimSize);
    table.getCellFormatter().setColSpan(rIdx, cIdx, 2);
    table.setText(rIdx, cIdx, getBranchCaption(), STYLE_HEADER);

    // COL CAPTIONS
    for (Dimension dimension : allDimensions) {
      table.setText(rIdx, cIdx + rIdx > 0 ? 0 : 1, dimension.getName(), STYLE_COL_HEADER);
      rIdx++;
    }
    // COLS
    List<Bundle> cols = new ArrayList<>();
    cIdx = 0;

    for (Bundle bundle : configuration.getMetrics(allDimensions)) {
      if (!BeeUtils.isEmpty(configuration.getBundlePrice(bundle))) {
        cols.add(bundle);
        int c = cIdx;
        Holder<Integer> r = Holder.of(dimSize);

        Configuration.processOptions(bundle, allDimensions, (dimension, option) -> {
          table.setText(dimSize - r.get(), c + (r.get() < dimSize ? 1 : 2),
              option != null ? option.getName() : "", STYLE_COL);
          r.set(r.get() - 1);
          return true;
        });
        cIdx++;
      }
    }
    // ROWS
    List<Option> rows = new ArrayList<>(configuration.getOptions());
    Dimension group = null;

    for (int r = 0; r < rows.size(); r++) {
      Option option = rows.get(r);

      if (!Objects.equals(option.getDimension(), group)) {
        table.getCellFormatter().setColSpan(rIdx, 0, cols.size() + 3);
        table.setText(rIdx, 0, option.getDimension().getName(), STYLE_GROUP);
        rIdx++;
        group = option.getDimension();
      }
      CustomSpan remove = new CustomSpan(STYLE_REMOVE);
      remove.addClickHandler(clickEvent ->
          Global.confirmRemove(null, BeeUtils.bracket(option.toString()), () -> {
            ParameterList args = OrdersKeeper.createSvcArgs(SVC_DELETE_OPTION);
            args.addDataItem(COL_BRANCH, getBranchId());
            args.addDataItem(COL_OPTION, option.getId());
            BeeKeeper.getRpc().makePostRequest(args, defaultResponse);

            configuration.removeOption(option);
            refresh();
          }));
      Flow widget = new Flow();
      widget.add(new InlineLabel(option.getCode()));
      widget.add(remove);
      table.setWidget(rIdx, 0, widget, STYLE_ROW);

      String txt = null;

      if (configuration.hasRestrictions(option)) {
        Label lbl = new InlineLabel(BeeUtils.bracket(configuration.getRestrictions(option).size()));
        lbl.setStyleName(STYLE_RESTRICTED);
        txt = lbl.toString();
      }
      table.setHtml(rIdx, 1, BeeUtils.joinWords(option.getName(), txt), STYLE_ROW, STYLE_CELL);
      table.setText(rIdx, 2, configuration.getOptionPrice(option), STYLE_CELL);
      DomUtils.setDataIndex(table.getCellFormatter().getElement(rIdx, 1), r);
      DomUtils.setDataIndex(table.getCellFormatter().getElement(rIdx, 2), r);

      for (int c = 0; c < cols.size(); c++) {
        Bundle bundle = cols.get(c);
        Element cell = table.getCellFormatter().ensureElement(rIdx, c + 3);
        cell.setClassName(STYLE_CELL);

        if (configuration.hasRelation(option, bundle)) {
          if (configuration.isDefault(option, bundle)) {
            cell.addClassName(STYLE_DEFAULT);
          } else {
            String price = configuration.getRelationPrice(option, bundle);

            if (BeeUtils.isEmpty(price)) {
              cell.addClassName(STYLE_OPTIONAL);
            } else {
              cell.setInnerText(price);
            }
          }
        }
        DomUtils.setDataIndex(cell, r);
        DomUtils.setDataColumn(cell, c);
      }
      rIdx++;
    }
    CustomSpan rowAdd = new CustomSpan(STYLE_ROW_ADD);
    rowAdd.addClickHandler(clickEvent -> {
      HtmlTable input = new HtmlTable();
      Set<Long> excludedGroups = new HashSet<>();
      configuration.getAllDimensions().forEach(dimension -> excludedGroups.add(dimension.getId()));
      Set<Long> excludedOptions = new HashSet<>();
      configuration.getOptions().stream().map(Option::getId).forEach(excludedOptions::add);

      UnboundSelector inputGroup = UnboundSelector.create(TBL_CONF_GROUPS,
          Collections.singletonList(COL_GROUP_NAME));
      inputGroup.getOracle().setAdditionalFilter(Filter.idNotIn(excludedGroups), true);
      inputGroup.setWidth("100%");

      input.setText(0, 0, Localized.dictionary().group());
      input.getCellFormatter().setColSpan(1, 0, 2);
      input.setWidget(1, 0, inputGroup);

      input.setText(2, 0, Localized.dictionary().option());
      UnboundSelector inputOption = UnboundSelector.create(TBL_CONF_OPTIONS,
          Arrays.asList(COL_CODE, COL_OPTION_NAME, COL_GROUP_NAME));

      inputOption.getOracle().setExclusions(excludedOptions);
      inputOption.addSelectorHandler(event -> {
        if (event.isOpened()) {
          if (DataUtils.isId(inputGroup.getRelatedId())) {
            event.getSelector().getOracle()
                .setAdditionalFilter(Filter.equals(COL_GROUP, inputGroup.getRelatedId()), true);
          } else {
            event.getSelector().getOracle()
                .setAdditionalFilter(Filter.exclude(COL_GROUP, excludedGroups), true);
          }
        }
      });
      input.setWidget(3, 0, inputOption);

      input.setText(2, 1, Localized.dictionary().price());
      InputSpinner inputPrice = new InputSpinner();
      inputPrice.setMinValue("0");
      input.setWidget(3, 1, inputPrice);

      Global.inputWidget(null, input, () -> {
        BeeRow beeRow = inputOption.getRelatedRow();

        if (beeRow != null) {
          setOptionPrice(new Option(beeRow.getId(),
                  Data.getString(TBL_CONF_OPTIONS, beeRow, COL_OPTION_NAME),
                  new Dimension(Data.getLong(TBL_CONF_OPTIONS, beeRow, COL_GROUP),
                      Data.getString(TBL_CONF_OPTIONS, beeRow, COL_GROUP_NAME)))
                  .setCode(Data.getString(TBL_CONF_OPTIONS, beeRow, COL_CODE)),
              BeeUtils.isNonNegativeInt(inputPrice.getValue()) ? inputPrice.getValue() : null);
          refresh();

        } else if (DataUtils.isId(inputGroup.getRelatedId())) {
          Global.confirm(inputGroup.getRenderedValue(),
              Icon.QUESTION, Collections.singletonList(Localized.dictionary().selectAll()), () -> {
                Queries.getRowSet(TBL_CONF_OPTIONS, null, Filter.and(Filter.equals(COL_GROUP,
                    inputGroup.getRelatedId()), Filter.idNotIn(excludedOptions)),
                    new Queries.RowSetCallback() {
                      @Override
                      public void onSuccess(BeeRowSet result) {
                        if (DataUtils.isEmpty(result)) {
                          getFormView().notifyWarning(Localized.dictionary().noData());
                        } else {
                          for (BeeRow beeRow : result) {
                            setOptionPrice(new Option(beeRow.getId(),
                                Data.getString(TBL_CONF_OPTIONS, beeRow, COL_OPTION_NAME),
                                new Dimension(inputGroup.getRelatedId(),
                                    Data.getString(TBL_CONF_OPTIONS, beeRow, COL_GROUP_NAME)))
                                .setCode(Data.getString(TBL_CONF_OPTIONS, beeRow, COL_CODE)), null);
                          }
                          refresh();
                        }
                      }
                    });
              });
        }
      });
    });
    table.setWidget(rIdx, 0, rowAdd, STYLE_ROW_HEADER);
    table.getCellFormatter().setColSpan(rIdx, 0, 3);

    table.addClickHandler(event -> {
      TableCellElement cell = DomUtils.getParentCell(EventUtils.getEventTargetElement(event), true);
      int row = DomUtils.getDataIndexInt(cell);
      int col = DomUtils.getDataColumnInt(cell);

      if (BeeConst.isUndef(row)) {
        return;
      }
      Option option = rows.get(row);

      if (BeeConst.isUndef(col)) {
        if (cell.hasClassName(STYLE_ROW)) {
          inputRestrictions(option, cell);
        } else {
          inputPrice(configuration.getOptionPrice(option), false, newPrice -> {
            setOptionPrice(option, newPrice);
            refresh();
          }, null, cell);
        }
        return;
      }
      Bundle bundle = cols.get(col);
      String price = configuration.getRelationPrice(option, bundle);

      BiConsumer<String, String> consumer = (svc, prc) -> {
        ParameterList args = OrdersKeeper.createSvcArgs(svc);
        args.addDataItem(COL_BRANCH, getBranchId());
        args.addDataItem(COL_KEY, bundle.getKey());
        args.addDataItem(COL_OPTION, option.getId());
        args.addNotEmptyData(ClassifierConstants.COL_ITEM_PRICE, prc);
        BeeKeeper.getRpc().makePostRequest(args, defaultResponse);

        switch (svc) {
          case SVC_DELETE_RELATION:
            configuration.removeRelation(option, bundle);
            break;
          case SVC_SET_RELATION:
            configuration.setRelationPrice(option, bundle, prc);
            break;
        }
        refresh();
      };
      if (BeeUtils.isNonNegativeInt(price) || event.isAltKeyDown()) {
        inputPrice(price, false, newPrice -> consumer.accept(SVC_SET_RELATION, newPrice),
            !configuration.hasRelation(option, bundle) ? null : dialog -> {
              dialog.close();
              consumer.accept(SVC_DELETE_RELATION, null);
            }, cell);
      } else if (!configuration.hasRelation(option, bundle)) {
        consumer.accept(SVC_SET_RELATION, Configuration.DEFAULT_PRICE);
      } else if (configuration.isDefault(option, bundle)) {
        consumer.accept(SVC_SET_RELATION, null);
      } else {
        consumer.accept(SVC_DELETE_RELATION, null);
      }
    });
    return table;
  }

  private void saveDimensions() {
    ParameterList args = OrdersKeeper.createSvcArgs(SVC_SAVE_DIMENSIONS);
    args.addDataItem(COL_BRANCH, getBranchId());

    List<Long> rowDimensions = new ArrayList<>();
    List<Long> colDimensions = new ArrayList<>();

    for (Dimension dimension : configuration.getRowDimensions()) {
      rowDimensions.add(dimension.getId());
    }
    for (Dimension dimension : configuration.getColDimensions()) {
      colDimensions.add(dimension.getId());
    }
    args.addDataItem(TBL_CONF_DIMENSIONS,
        Codec.beeSerialize(Pair.of(rowDimensions, colDimensions)));

    BeeKeeper.getRpc().makePostRequest(args, defaultResponse);
  }

  private void setOptionPrice(Option option, String price) {
    ParameterList args = OrdersKeeper.createSvcArgs(SVC_SET_OPTION);
    args.addDataItem(COL_BRANCH, getBranchId());
    args.addDataItem(COL_OPTION, option.getId());
    args.addNotEmptyData(ClassifierConstants.COL_ITEM_PRICE, price);
    BeeKeeper.getRpc().makePostRequest(args, defaultResponse);

    configuration.setOptionPrice(option, price);
  }
}
