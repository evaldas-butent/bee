package com.butent.bee.client.view.search;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Longs;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.data.RowCallback;
import com.butent.bee.client.dialog.ConfirmationCallback;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.dialog.Icon;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.LocaleUtils;
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.filter.FilterDescription;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Filters {

  private static class Item implements Comparable<Item> {

    private final long id;

    private final FilterDescription filterDescription;

    private final boolean predefined;

    private Item(long id, FilterDescription filterDescription, boolean predefined) {
      this.id = id;
      this.filterDescription = filterDescription;
      this.predefined = predefined;
    }

    @Override
    public int compareTo(Item o) {
      return filterDescription.compareTo(o.filterDescription);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Item) ? getId() == ((Item) obj).getId() : false;
    }

    @Override
    public int hashCode() {
      return Longs.hashCode(getId());
    }

    private long getId() {
      return id;
    }

    private String getLabel() {
      return filterDescription.getLabel();
    }

    private String getName() {
      return filterDescription.getName();
    }

    private Integer getOrdinal() {
      return filterDescription.getOrdinal();
    }

    private String getValue() {
      return filterDescription.getValue();
    }

    private Map<String, String> getValues() {
      return filterDescription.getValues();
    }

    private boolean isEditable() {
      return filterDescription.isEditable();
    }

    private boolean isInitial() {
      return filterDescription.isInitial();
    }

    private boolean isPredefined() {
      return predefined;
    }

    private boolean isRemovable() {
      return filterDescription.isRemovable();
    }

    private void setLabel(String label) {
      filterDescription.setLabel(label);
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Filters.class);

  private static final String COL_NAME = "Name";
  private static final String COL_LABEL = "Label";
  private static final String COL_INITIAL = "Initial";
  private static final String COL_EDITABLE = "Editable";
  private static final String COL_REMOVABLE = "Removable";
  private static final String COL_PREDEFINED = "Predefined";
  private static final String COL_VALUE = "Value";

  private static final String STYLE_PREFIX = "bee-Filters-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_ROW = STYLE_PREFIX + "row";
  private static final String STYLE_ROW_INITIAL = STYLE_ROW + "-initial";
  private static final String STYLE_ROW_ACTIVE = STYLE_ROW + "-active";
  private static final String STYLE_ROW_PREDEFINED = STYLE_ROW + "-predefined";

  private static final String STYLE_INITIAL = STYLE_PREFIX + "initial";
  private static final String STYLE_NON_INITIAL = STYLE_PREFIX + "non-initial";
  private static final String STYLE_LABEL = STYLE_PREFIX + "label";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";
  private static final String STYLE_DELETE = STYLE_PREFIX + "delete";

  private static final String STYLE_SUFFIX_COL = "-col";
  private static final String STYLE_SUFFIX_CELL = "-cell";

  private final Multimap<String, Item> itemsByKey = ArrayListMultimap.create();

  private int maxLabelLength = BeeConst.UNDEF;

  private int keyColumnIndex = BeeConst.UNDEF;

  private int nameColumnIndex;
  private int labelColumnIndex;

  private int initialColumnIndex;
  private int ordinalColumnIndex;

  private int editableColumnIndex;
  private int removableColumnIndex;
  private int predefinedColumnIndex;

  private int valueColumnIndex;

  public Filters() {
    super();
  }

  public void addCustomFilter(String key, String label, Map<String, String> values) {
    Assert.notEmpty(key);
    Assert.notEmpty(label);
    Assert.notEmpty(values);

    FilterDescription filterDescription =
        FilterDescription.userDefined(normalizeLabel(label), values);

    int ordinal = -1;
    if (itemsByKey.containsKey(key)) {
      for (Item item : itemsByKey.get(key)) {
        if (item.getOrdinal() != null) {
          ordinal = Math.max(ordinal, item.getOrdinal());
        }
      }
    }
    filterDescription.setOrdinal(ordinal + 1);
    
    insert(key, filterDescription, false);
  }

  public boolean contains(String key, Map<String, String> values) {
    if (BeeUtils.isEmpty(key) || BeeUtils.isEmpty(values)) {
      return false;
    }

    if (itemsByKey.containsKey(key)) {
      for (Item item : itemsByKey.get(key)) {
        if (item.getValues().equals(values)) {
          return true;
        }
      }
    }
    return false;
  }

  public void ensurePredefinedFilters(String key, List<FilterDescription> filters) {
    if (!BeeUtils.isEmpty(key) && !BeeUtils.isEmpty(filters) && !itemsByKey.containsKey(key)) {
      List<FilterDescription> predefinedFilters = Lists.newArrayList(filters);
      if (predefinedFilters.size() > 1) {
        Collections.sort(predefinedFilters);
      }

      for (int i = 0; i < predefinedFilters.size(); i++) {
        FilterDescription filterDescription = predefinedFilters.get(i).copy();
        filterDescription.setOrdinal(i);
        
        itemsByKey.put(key, new Item(DataUtils.NEW_ROW_ID, filterDescription, true));

        insert(key, filterDescription, true);
      }
    }
  }

  public List<Map<String, String>> getInitialValues(String key) {
    List<Map<String, String>> initialValues = Lists.newArrayList();

    if (itemsByKey.containsKey(key)) {
      for (Item item : itemsByKey.get(key)) {
        if (item.isInitial()) {
          initialValues.add(item.getValues());
        }
      }
    }

    return initialValues;
  }

  public void handle(final String key, Element relativeTo, final Consumer<String> callback) {
    Assert.notEmpty(key);
    Assert.notNull(callback);

    final List<Item> items = getItems(key);

    int activeItemIndex = BeeConst.UNDEF;

    if (items.isEmpty()) {
      return;
    }

    final DialogBox dialog = DialogBox.create("Filtrai", STYLE_DIALOG);

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_TABLE);

    int r = 0;

    for (Item item : items) {
      int c = 0;

      final String name = item.getName();

      CustomDiv initial = new CustomDiv();
      initial.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
        }
      });

      createCell(table, r, c, initial, item.isInitial() ? STYLE_INITIAL : STYLE_NON_INITIAL);
      c++;

      final CustomDiv labelWidget = new CustomDiv();
      labelWidget.setHTML(LocaleUtils.maybeLocalize(item.getLabel()));

      labelWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialog.close();
          callback.accept(getItem(items, name).getValue());
        }
      });

      createCell(table, r, c, labelWidget, STYLE_LABEL);
      c++;

      if (item.isEditable()) {
        BeeImage edit = new BeeImage(Global.getImages().silverEdit());
        edit.addStyleName(STYLE_EDIT);
        edit.setTitle("keisti pavadinimą");

        edit.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            final Item editItem = getItem(items, name);
            final String oldLabel = normalizeLabel(LocaleUtils.maybeLocalize(editItem.getLabel()));

            Global.inputString("Pakeisti pavadinimą", null, new StringCallback() {
              @Override
              public void onSuccess(String newValue) {
                String newLabel = normalizeLabel(newValue);
                if (!BeeUtils.isEmpty(newLabel) && !newLabel.equals(oldLabel)) {
                  editItem.setLabel(newLabel);
                  labelWidget.setHTML(newLabel);

                  Queries.update(CommonsConstants.TBL_FILTERS, editItem.getId(), COL_LABEL,
                      new TextValue(newLabel));
                }
              }
            }, oldLabel, getMaxLabelLength());
          }
        });

        createCell(table, r, c, edit, STYLE_EDIT);
      }

      c++;

      if (item.isRemovable()) {
        BeeImage delete = new BeeImage(Global.getImages().silverMinus());
        delete.setTitle("pašalinti");

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            remove(key, items, name, dialog, table);
          }
        });

        createCell(table, r, c, delete, STYLE_DELETE);
      }

      c++;

      table.getRowFormatter().addStyleName(r, STYLE_ROW);

      if (item.isInitial()) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_INITIAL);
      }
      if (item.isPredefined()) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_PREDEFINED);
      }

      if (r == activeItemIndex) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_ACTIVE);
      }

      r++;
    }

    dialog.setWidget(table);

    dialog.setAnimationEnabled(true);
    dialog.setHideOnEscape(true);

    if (relativeTo == null) {
      dialog.center();
    } else {
      dialog.showRelativeTo(relativeTo);
    }
  }

  public void load(String serialized) {
    Assert.notEmpty(serialized);

    BeeRowSet rowSet = BeeRowSet.restore(serialized);

    keyColumnIndex = rowSet.getColumnIndex(CommonsConstants.COL_FILTER_KEY);

    nameColumnIndex = rowSet.getColumnIndex(COL_NAME);
    labelColumnIndex = rowSet.getColumnIndex(COL_LABEL);

    initialColumnIndex = rowSet.getColumnIndex(COL_INITIAL);
    ordinalColumnIndex = rowSet.getColumnIndex(CommonsConstants.COL_FILTER_ORDINAL);

    editableColumnIndex = rowSet.getColumnIndex(COL_EDITABLE);
    removableColumnIndex = rowSet.getColumnIndex(COL_REMOVABLE);
    predefinedColumnIndex = rowSet.getColumnIndex(COL_PREDEFINED);

    valueColumnIndex = rowSet.getColumnIndex(COL_VALUE);

    itemsByKey.clear();

    for (BeeRow row : rowSet.getRows()) {
      String key = BeeUtils.trim(row.getString(keyColumnIndex));
      Item item = createItem(row);

      itemsByKey.put(key, item);
    }

    logger.info("filters", itemsByKey.size());
  }

  private void createCell(HtmlTable table, int row, int col, Widget widget, String styleName) {
    widget.addStyleName(styleName);
    table.setWidget(row, col, widget, styleName + STYLE_SUFFIX_CELL);

    if (row == 0) {
      table.getColumnFormatter().addStyleName(col, styleName + STYLE_SUFFIX_COL);
    }
  }

  private Item createItem(BeeRow row) {
    if (row == null || BeeConst.isUndef(keyColumnIndex)) {
      return null;
    }

    String name = BeeUtils.trim(row.getString(nameColumnIndex));
    String label = BeeUtils.trim(row.getString(labelColumnIndex));

    Boolean initial = row.getBoolean(initialColumnIndex);
    Integer ordinal = row.getInteger(ordinalColumnIndex);

    Boolean editable = row.getBoolean(editableColumnIndex);
    Boolean removable = row.getBoolean(removableColumnIndex);
    boolean predefined = BeeUtils.isTrue(row.getBoolean(predefinedColumnIndex));

    String value = BeeUtils.trim(row.getString(valueColumnIndex));

    return new Item(row.getId(),
        new FilterDescription(name, label, value, initial, ordinal, editable, removable),
        predefined);
  }

  private Item getItem(Collection<Item> items, String name) {
    for (Item item : items) {
      if (item.getName().equals(name)) {
        return item;
      }
    }
    return null;
  }

  private List<Item> getItems(String key) {
    List<Item> result = Lists.newArrayList();

    if (itemsByKey.containsKey(key)) {
      result.addAll(itemsByKey.get(key));
    }

    return result;
  }

  private int getMaxLabelLength() {
    if (maxLabelLength <= 0) {
      maxLabelLength = Data.getColumnPrecision(CommonsConstants.TBL_FILTERS, COL_LABEL);
    }
    return maxLabelLength;
  }

  private void insert(final String key, FilterDescription filterDescription, boolean predefined) {
    List<BeeColumn> columns =
        Data.getColumns(CommonsConstants.TBL_FILTERS,
            Lists.newArrayList(CommonsConstants.COL_FILTER_USER, CommonsConstants.COL_FILTER_KEY,
                COL_NAME, COL_LABEL, COL_VALUE));
    List<String> values = Queries.asList(BeeKeeper.getUser().getUserId(), key,
        filterDescription.getName(), filterDescription.getLabel(), filterDescription.getValue());

    if (filterDescription.isInitial()) {
      columns.add(Data.getColumn(CommonsConstants.TBL_FILTERS, COL_INITIAL));
      values.add(BooleanValue.pack(filterDescription.isInitial()));
    }
    if (filterDescription.getOrdinal() != null) {
      columns.add(Data.getColumn(CommonsConstants.TBL_FILTERS,
          CommonsConstants.COL_FILTER_ORDINAL));
      values.add(filterDescription.getOrdinal().toString());
    }

    if (filterDescription.isEditable()) {
      columns.add(Data.getColumn(CommonsConstants.TBL_FILTERS, COL_EDITABLE));
      values.add(BooleanValue.pack(filterDescription.isEditable()));
    }
    if (filterDescription.isRemovable()) {
      columns.add(Data.getColumn(CommonsConstants.TBL_FILTERS, COL_REMOVABLE));
      values.add(BooleanValue.pack(filterDescription.isRemovable()));
    }

    if (predefined) {
      columns.add(Data.getColumn(CommonsConstants.TBL_FILTERS, COL_PREDEFINED));
      values.add(BooleanValue.pack(predefined));
    }

    Queries.insert(CommonsConstants.TBL_FILTERS, columns, values, null, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        itemsByKey.put(key, createItem(result));
      }
    });
  }

  private String normalizeLabel(String label) {
    return BeeUtils.left(BeeUtils.trim(label), getMaxLabelLength());
  }

  private void remove(final String key, final List<Item> items, String name,
      final DialogBox dialog, final HtmlTable table) {

    final Item item = getItem(items, name);
    Assert.notNull(item);

    Global.confirmDelete("Filtro pašalinimas", Icon.WARNING,
        Lists.newArrayList("Pašalinti filtrą", BeeUtils.joinWords(item.getName(), "?")),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            Queries.deleteRow(CommonsConstants.TBL_FILTERS, item.getId());
            itemsByKey.remove(key, item);

            if (items.size() > 1) {
              int index = items.indexOf(item);
              items.remove(index);
              table.removeRow(index);
            } else {
              dialog.close();
            }
          }
        });
  }
}
