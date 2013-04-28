package com.butent.bee.client.view.search;

import com.google.common.collect.HashMultimap;
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
import com.butent.bee.client.widget.BeeImage;
import com.butent.bee.client.widget.CustomDiv;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.value.BooleanValue;
import com.butent.bee.shared.data.value.TextValue;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Filters {

  private static class Item implements Comparable<Item> {

    private final long id;

    private boolean initial;
    private int order;

    private String name;
    private final String filter;

    private Item(long id, boolean initial, int order, String name, String filter) {
      this.id = id;
      this.initial = initial;
      this.order = order;
      this.name = name;
      this.filter = filter;
    }

    @Override
    public int compareTo(Item o) {
      int result = BeeUtils.compare(o.isInitial(), isInitial());
      return (result == BeeConst.COMPARE_EQUAL) ? BeeUtils.compare(getOrder(), o.getOrder())
          : result;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Item) ? getId() == ((Item) obj).getId() : false;
    }

    @Override
    public int hashCode() {
      return Longs.hashCode(getId());
    }

    private String getFilter() {
      return filter;
    }

    private long getId() {
      return id;
    }

    private String getName() {
      return name;
    }

    private int getOrder() {
      return order;
    }

    private boolean isInitial() {
      return initial;
    }

    private boolean isNew() {
      return !DataUtils.isId(getId());
    }

    private void setName(String name) {
      this.name = name;
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(Filters.class);

  private static final String TBL_FILTERS = "Filters";

  private static final String COL_USER = "User";
  private static final String COL_KEY = "Key";
  private static final String COL_INITIAL = "Initial";
  private static final String COL_ORDER = "Order";
  private static final String COL_NAME = "Name";
  private static final String COL_FILTER = "Filter";

  private static final String STYLE_PREFIX = "bee-Filters-";
  private static final String STYLE_DIALOG = STYLE_PREFIX + "dialog";

  private static final String STYLE_TABLE = STYLE_PREFIX + "table";
  private static final String STYLE_ROW = STYLE_PREFIX + "row";
  private static final String STYLE_ROW_INITIAL = STYLE_ROW + "-initial";
  private static final String STYLE_ROW_ACTIVE = STYLE_ROW + "-active";
  private static final String STYLE_ROW_NEW = STYLE_ROW + "-new";

  private static final String STYLE_INITIAL = STYLE_PREFIX + "initial";
  private static final String STYLE_NON_INITIAL = STYLE_PREFIX + "non-initial";
  private static final String STYLE_NAME = STYLE_PREFIX + "name";
  private static final String STYLE_EDIT = STYLE_PREFIX + "edit";
  private static final String STYLE_ADD = STYLE_PREFIX + "add";
  private static final String STYLE_DELETE = STYLE_PREFIX + "delete";

  private static final String STYLE_SUFFIX_COL = "-col";
  private static final String STYLE_SUFFIX_CELL = "-cell";

  private final Multimap<String, Item> itemsByKey = HashMultimap.create();

  private int maxNameLength = BeeConst.UNDEF;

  public Filters() {
    super();
  }

  public void handle(final String key, String name, String filter, Element relativeTo,
      final Consumer<String> callback) {

    Assert.notEmpty(key);
    Assert.notNull(callback);

    final List<Item> items = getItems(key);

    int activeItemIndex = BeeConst.UNDEF;
    if (!BeeUtils.isEmpty(name) && !BeeUtils.isEmpty(filter)) {
      if (!items.isEmpty()) {
        for (int i = 0; i < items.size(); i++) {
          if (filter.equals(items.get(i).getFilter())) {
            activeItemIndex = i;
            break;
          }
        }
      }

      if (BeeConst.isUndef(activeItemIndex)) {
        items.add(new Item(DataUtils.NEW_ROW_ID, false, BeeConst.UNDEF, normalizeName(name),
            filter.trim()));
        activeItemIndex = items.size() - 1;
      }
    }

    if (items.isEmpty()) {
      return;
    }

    final DialogBox dialog = DialogBox.create("Filtrai", STYLE_DIALOG);

    final HtmlTable table = new HtmlTable();
    table.addStyleName(STYLE_TABLE);

    int r = 0;

    for (Item item : items) {
      int c = 0;

      final long id = item.getId();

      CustomDiv initial = new CustomDiv();
      initial.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
        }
      });

      createCell(table, r, c, initial, item.isInitial() ? STYLE_INITIAL : STYLE_NON_INITIAL);
      c++;

      final CustomDiv label = new CustomDiv();
      label.setHTML(item.getName());

      if (!item.isNew()) {
        label.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            dialog.close();
            callback.accept(getItem(items, id).getFilter());
          }
        });
      }

      createCell(table, r, c, label, STYLE_NAME);
      c++;

      BeeImage edit = new BeeImage(Global.getImages().silverEdit());
      edit.addStyleName(STYLE_EDIT);
      edit.setTitle("keisti pavadinimą");

      edit.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          final Item editItem = getItem(items, id);
          final String oldName = normalizeName(editItem.getName());

          Global.inputString("Pakeisti pavadinimą", null, new StringCallback() {
            @Override
            public void onSuccess(String value) {
              String newName = normalizeName(value);
              if (!BeeUtils.isEmpty(newName) && !newName.equals(oldName)) {
                editItem.setName(newName);
                label.setHTML(newName);
                
                if (!editItem.isNew()) {
                  Queries.update(TBL_FILTERS, id, COL_NAME, new TextValue(newName));
                }
              }
            }
          }, oldName, getMaxNameLength());
        }
      });

      createCell(table, r, c, edit, STYLE_EDIT);
      c++;

      if (item.isNew()) {
        BeeImage add = new BeeImage(Global.getImages().silverPlus());
        add.setTitle("išsaugoti");

        add.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            dialog.close();
            insert(key, items, id);
          }
        });

        createCell(table, r, c, add, STYLE_ADD);

      } else {
        BeeImage delete = new BeeImage(Global.getImages().silverMinus());
        delete.setTitle("pašalinti");

        delete.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            remove(key, items, id, dialog, table);
          }
        });

        createCell(table, r, c, delete, STYLE_DELETE);
      }

      c++;

      table.getRowFormatter().addStyleName(r, STYLE_ROW);

      if (item.isInitial()) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_INITIAL);
      }
      if (r == activeItemIndex) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_ACTIVE);
      }
      if (item.isNew()) {
        table.getRowFormatter().addStyleName(r, STYLE_ROW_NEW);
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

  public void load() {
    Queries.getRowSet(TBL_FILTERS, null, BeeKeeper.getUser().getFilter(COL_USER),
        new Order(COL_KEY, true), new Queries.RowSetCallback() {
          @Override
          public void onSuccess(BeeRowSet result) {
            itemsByKey.clear();

            if (!DataUtils.isEmpty(result)) {
              int keyIndex = result.getColumnIndex(COL_KEY);
              int initIndex = result.getColumnIndex(COL_INITIAL);
              int orderIndex = result.getColumnIndex(COL_ORDER);
              int nameIndex = result.getColumnIndex(COL_NAME);
              int filterIndex = result.getColumnIndex(COL_FILTER);

              for (BeeRow row : result.getRows()) {
                String key = BeeUtils.trim(row.getString(keyIndex));

                boolean initial = BeeUtils.isTrue(row.getBoolean(initIndex));
                int order = BeeUtils.unbox(row.getInteger(orderIndex));

                String name = BeeUtils.trim(row.getString(nameIndex));
                String filter = BeeUtils.trim(row.getString(filterIndex));

                itemsByKey.put(key, new Item(row.getId(), initial, order, name, filter));
              }
            }

            logger.info("loaded", itemsByKey.size(), "filters");
          }
        });
  }

  private void createCell(HtmlTable table, int row, int col, Widget widget, String styleName) {
    widget.addStyleName(styleName);
    table.setWidget(row, col, widget, styleName + STYLE_SUFFIX_CELL);

    if (row == 0) {
      table.getColumnFormatter().addStyleName(col, styleName + STYLE_SUFFIX_COL);
    }
  }
  
  private Item getItem(Collection<Item> items, long id) {
    for (Item item : items) {
      if (item.getId() == id) {
        return item;
      }
    }
    return null;
  }

  private List<Item> getItems(String key) {
    List<Item> result = Lists.newArrayList();

    if (itemsByKey.containsKey(key)) {
      result.addAll(itemsByKey.get(key));
      if (result.size() > 1) {
        Collections.sort(result);
      }
    }

    return result;
  }

  private int getMaxNameLength() {
    if (maxNameLength <= 0) {
      maxNameLength = Data.getColumnPrecision(TBL_FILTERS, COL_NAME);
    }
    return maxNameLength;
  }
  
  private void insert(final String key, List<Item> items, long id) {
    Item item = null;
    int order = 0;

    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).getId() == id) {
        item = items.get(i);
        if (!BeeConst.isUndef(item.getOrder())) {
          order = item.getOrder();
          break;
        }

      } else {
        order = Math.max(order, items.get(i).getOrder() + 1);
      }
    }

    Assert.notNull(item);

    final boolean initial = item.isInitial();
    final int itemOrder = order;
    final String name = normalizeName(item.getName());
    final String filter = item.getFilter();

    List<BeeColumn> columns = Data.getColumns(TBL_FILTERS,
        Lists.newArrayList(COL_USER, COL_KEY, COL_ORDER, COL_NAME, COL_FILTER));
    List<String> values = Queries.asList(BeeKeeper.getUser().getUserId(), key, order, name, filter);

    if (initial) {
      columns.add(Data.getColumn(TBL_FILTERS, COL_INITIAL));
      values.add(BooleanValue.pack(initial));
    }

    Queries.insert(TBL_FILTERS, columns, values, null, new RowCallback() {
      @Override
      public void onSuccess(BeeRow result) {
        if (result != null) {
          itemsByKey.put(key, new Item(result.getId(), initial, itemOrder, name, filter));
        }
      }
    });
  }

  private String normalizeName(String name) {
    return BeeUtils.left(BeeUtils.trim(name), getMaxNameLength());
  }

  private void remove(final String key, final List<Item> items, final long id,
      final DialogBox dialog, final HtmlTable table) {

    final Item item = getItem(items, id);
    Assert.notNull(item);
    
    Global.confirmDelete("Filtro pašalinimas", Icon.WARNING,
        Lists.newArrayList("Pašalinti filtrą", BeeUtils.joinWords(item.getName(), "?")),
        new ConfirmationCallback() {
          @Override
          public void onConfirm() {
            Queries.deleteRow(TBL_FILTERS, id);
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
