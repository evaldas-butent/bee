package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import static com.butent.bee.shared.modules.classifiers.ClassifierConstants.*;
import static com.butent.bee.shared.modules.trade.TradeConstants.*;
import static com.butent.bee.shared.modules.trade.acts.TradeActConstants.*;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.communication.ParameterList;
import com.butent.bee.client.communication.ResponseCallback;
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.layout.Simple;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.client.widget.Toggle;
import com.butent.bee.shared.BiConsumer;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Latch;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.font.FontAwesome;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.modules.trade.acts.TradeActKind;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import elemental.html.File;

public class TradeActItemsGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private static final class ImportEntry {
    private final String article;
    private final String name;

    private final Double quantity;

    private BeeRow item;

    private ImportEntry(String article, String name, Double quantity) {
      this.article = article;
      this.name = name;
      this.quantity = quantity;
    }
  }

  private static final BeeLogger logger = LogUtils.getLogger(TradeActItemsGrid.class);

  private static final String STYLE_IMPORT_PREFIX = TradeActKeeper.STYLE_PREFIX + "import-items-";

  private static final String STYLE_IMPORT_DIALOG = STYLE_IMPORT_PREFIX + "dialog";
  private static final String STYLE_IMPORT_SAVE = STYLE_IMPORT_PREFIX + "save";
  private static final String STYLE_IMPORT_CLOSE = STYLE_IMPORT_PREFIX + "close";

  private static final String STYLE_IMPORT_WRAPPER = STYLE_IMPORT_PREFIX + "wrapper";
  private static final String STYLE_IMPORT_TABLE = STYLE_IMPORT_PREFIX + "table";

  private static final String STYLE_IMPORT_HEADER_ROW = STYLE_IMPORT_PREFIX + "header";
  private static final String STYLE_IMPORT_ITEM_ROW = STYLE_IMPORT_PREFIX + "item";
  private static final String STYLE_IMPORT_SELECTED_ROW = STYLE_IMPORT_PREFIX + "selected";
  private static final String STYLE_IMPORT_DUPLICATE_ROW = STYLE_IMPORT_PREFIX + "duplicate";
  private static final String STYLE_IMPORT_NO_STOCK_ROW = STYLE_IMPORT_PREFIX + "no-stock";

  private static final String STYLE_IMPORT_TOGGLE_PREFIX = STYLE_IMPORT_PREFIX + "toggle-";
  private static final String STYLE_IMPORT_TOGGLE_WIDGET = STYLE_IMPORT_PREFIX + "toggle";

  private static final String STYLE_IMPORT_ARTICLE_PREFIX = STYLE_IMPORT_PREFIX + "article-";
  private static final String STYLE_IMPORT_NAME_PREFIX = STYLE_IMPORT_PREFIX + "name-";
  private static final String STYLE_IMPORT_QTY_PREFIX = STYLE_IMPORT_PREFIX + "qty-";

  private static final String STYLE_IMPORT_STOCK_PREFIX = STYLE_IMPORT_PREFIX + "stock-";
  private static final String STYLE_IMPORT_FROM_PREFIX = STYLE_IMPORT_PREFIX + "from-";

  private static final String STYLE_IMPORT_ID_PREFIX = STYLE_IMPORT_PREFIX + "id-";
  private static final String STYLE_IMPORT_ITEM_NAME_PREFIX = STYLE_IMPORT_PREFIX + "item-name-";

  private static final String STYLE_HEADER_CELL_SUFFIX = "label";
  private static final String STYLE_CELL_SUFFIX = "cell";

  private TradeActItemPicker picker;
  private FileCollector collector;

  TradeActItemsGrid() {
  }

  @Override
  public void afterCreatePresenter(GridPresenter presenter) {
    Button command = new Button(Localized.getConstants().actionImport());
    command.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        ensureCollector().clickInput();
      }
    });

    presenter.getHeader().addCommandItem(command);

    super.afterCreatePresenter(presenter);
  }

  @Override
  public boolean beforeAddRow(GridPresenter presenter, boolean copy) {
    IsRow parentRow = UiHelper.getFormRow(presenter.getMainView());
    if (parentRow != null) {
      ensurePicker().show(parentRow, presenter.getMainView().getElement());
    }

    return false;
  }

  @Override
  public GridInterceptor getInstance() {
    return new TradeActItemsGrid();
  }

  @Override
  public void onSelection(SelectionEvent<BeeRowSet> event) {
    addItems(event.getSelectedItem());
  }

  private void addItems(final BeeRowSet rowSet) {
    if (!DataUtils.isEmpty(rowSet) && VIEW_ITEMS.equals(rowSet.getViewName())) {
      getGridView().ensureRelId(new IdCallback() {
        @Override
        public void onSuccess(Long result) {
          IsRow parentRow = UiHelper.getFormRow(getGridView());

          if (DataUtils.idEquals(parentRow, result)) {
            ItemPrice itemPrice = TradeActKeeper.getItemPrice(VIEW_TRADE_ACTS, parentRow);

            DateTime date = Data.getDateTime(VIEW_TRADE_ACTS, parentRow, COL_TA_DATE);
            Long currency = Data.getLong(VIEW_TRADE_ACTS, parentRow, COL_TA_CURRENCY);

            addItems(parentRow, date, currency, itemPrice, getDefaultDiscount(), rowSet);
          }
        }
      });
    }
  }

  private void addItems(IsRow parentRow, DateTime date, Long currency, ItemPrice itemPrice,
      Double discount, BeeRowSet items) {

    List<String> colNames = Lists.newArrayList(COL_TRADE_ACT, COL_TA_ITEM,
        COL_TRADE_ITEM_QUANTITY, COL_TRADE_ITEM_PRICE, COL_TRADE_DISCOUNT);
    BeeRowSet rowSet = new BeeRowSet(getViewName(), Data.getColumns(getViewName(), colNames));

    int actIndex = rowSet.getColumnIndex(COL_TRADE_ACT);
    int itemIndex = rowSet.getColumnIndex(COL_TA_ITEM);
    int qtyIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_QUANTITY);
    int priceIndex = rowSet.getColumnIndex(COL_TRADE_ITEM_PRICE);
    int discountIndex = rowSet.getColumnIndex(COL_TRADE_DISCOUNT);

    for (BeeRow item : items) {
      Double qty = BeeUtils.toDoubleOrNull(item.getProperty(PRP_QUANTITY));

      if (BeeUtils.isDouble(qty)) {
        BeeRow row = DataUtils.createEmptyRow(rowSet.getNumberOfColumns());

        row.setValue(actIndex, parentRow.getId());
        row.setValue(itemIndex, item.getId());

        row.setValue(qtyIndex, qty);

        if (itemPrice != null) {
          Double price = item.getDouble(items.getColumnIndex(itemPrice.getPriceColumn()));

          if (BeeUtils.isDouble(price)) {
            if (DataUtils.isId(currency)) {
              Long ic = item.getLong(items.getColumnIndex(itemPrice.getCurrencyColumn()));
              if (DataUtils.isId(ic) && !currency.equals(ic)) {
                price = Money.exchange(ic, currency, price, date);
              }
            }

            row.setValue(priceIndex, Data.round(getViewName(), COL_TRADE_ITEM_PRICE, price));
          }
        }

        if (BeeUtils.nonZero(discount)) {
          row.setValue(discountIndex, discount);
        }

        rowSet.addRow(row);
      }
    }

    if (!rowSet.isEmpty()) {
      Queries.insertRows(rowSet);
    }
  }

  private FileCollector ensureCollector() {
    if (collector == null) {
      collector = FileCollector.headless(new Consumer<Collection<? extends FileInfo>>() {
        @Override
        public void accept(Collection<? extends FileInfo> input) {
          List<FileInfo> fileInfos = FileUtils.validateFileSize(input, 100_000L, getGridView());

          if (!BeeUtils.isEmpty(fileInfos)) {
            List<String> fileNames = new ArrayList<>();
            for (FileInfo fileInfo : fileInfos) {
              fileNames.add(fileInfo.getName());
            }

            final String importCaption = BeeUtils.joinItems(fileNames);

            readImport(fileInfos, new Consumer<List<String>>() {
              @Override
              public void accept(final List<String> lines) {
                if (lines.isEmpty()) {
                  getGridView().notifyWarning(importCaption, Localized.getConstants().noData());

                } else {
                  Global.getParameter(PRM_IMPORT_TA_ITEM_RX, new Consumer<String>() {
                    @Override
                    public void accept(String pattern) {
                      final List<ImportEntry> importEntries =
                          parseImport(lines, BeeUtils.notEmpty(pattern, RX_IMPORT_ACT_ITEM));

                      if (importEntries.isEmpty()) {
                        getGridView().notifyWarning(importCaption,
                            Localized.getConstants().nothingFound());

                      } else {
                        queryImportItems(importEntries,
                            new BiConsumer<List<ImportEntry>, Map<Long, String>>() {
                              @Override
                              public void accept(List<ImportEntry> t, Map<Long, String> u) {
                                showImport(importCaption, t, u);
                              }
                            });
                      }
                    }
                  });
                }
              }
            });
          }
        }
      });

      collector.setAccept(CommUtils.MEDIA_TYPE_TEXT_PLAIN);
      getGridView().add(collector);
    }
    return collector;
  }

  private void queryImportItems(final List<ImportEntry> input,
      final BiConsumer<List<ImportEntry>, Map<Long, String>> consumer) {

    ParameterList params = TradeActKeeper.createArgs(SVC_GET_ITEMS_FOR_SELECTION);

    IsRow parentRow = UiHelper.getFormRow(getGridView());
    TradeActKind kind = TradeActKeeper.getKind(VIEW_TRADE_ACTS, parentRow);
    if (kind != null) {
      params.addDataItem(COL_TA_KIND, kind.ordinal());
    }

    Set<String> articles = new HashSet<>();
    for (ImportEntry entry : input) {
      articles.add(entry.article);
    }

    Filter filter = Filter.anyString(COL_ITEM_ARTICLE, articles);
    params.addDataItem(Service.VAR_VIEW_WHERE, filter.serialize());

    BeeKeeper.getRpc().makeRequest(params, new ResponseCallback() {
      @Override
      public void onResponse(ResponseObject response) {
        if (response.hasResponse(BeeRowSet.class)) {
          BeeRowSet items = BeeRowSet.restore(response.getResponseAsString());
          int index = items.getColumnIndex(COL_ITEM_ARTICLE);

          List<ImportEntry> output = new ArrayList<>();
          for (ImportEntry ie : input) {
            ImportEntry entry = new ImportEntry(ie.article, ie.name, ie.quantity);
            entry.item = items.findRow(index, entry.article);

            output.add(entry);
          }

          consumer.accept(output,
              TradeActKeeper.getWarehouses(TradeActKeeper.extractWarehouses(items)));

        } else {
          Map<Long, String> whs = Collections.emptyMap();
          consumer.accept(input, whs);
        }
      }
    });
  }

  private void showImport(String caption, final List<ImportEntry> entries,
      Map<Long, String> warehouses) {

    List<Long> warehouseIds = new ArrayList<>();
    if (!BeeUtils.isEmpty(warehouses)) {
      warehouseIds.addAll(warehouses.keySet());
    }

    Long warehouseFrom = TradeActKeeper.getWarehouseFrom(VIEW_TRADE_ACTS,
        UiHelper.getFormRow(getGridView()));

    final HtmlTable table = new HtmlTable(STYLE_IMPORT_TABLE);

    int r = 0;
    int c = 0;

    String pfx;
    boolean duplicate;

    table.setText(r, c++, Localized.getConstants().actionImport(),
        STYLE_IMPORT_TOGGLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.setText(r, c++, Localized.getConstants().article(),
        STYLE_IMPORT_ARTICLE_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().name(),
        STYLE_IMPORT_NAME_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().quantity(),
        STYLE_IMPORT_QTY_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    for (Long w : warehouseIds) {
      pfx = w.equals(warehouseFrom) ? STYLE_IMPORT_FROM_PREFIX : STYLE_IMPORT_STOCK_PREFIX;
      table.setText(r, c++, warehouses.get(w), pfx + STYLE_HEADER_CELL_SUFFIX);
    }

    table.setText(r, c++, Localized.getLabel(Data.getColumn(VIEW_ITEMS, COL_ITEM_NAME)),
        STYLE_IMPORT_ID_PREFIX + STYLE_HEADER_CELL_SUFFIX);
    table.setText(r, c++, Localized.getConstants().captionId(),
        STYLE_IMPORT_ID_PREFIX + STYLE_HEADER_CELL_SUFFIX);

    table.getRowFormatter().addStyleName(r, STYLE_IMPORT_HEADER_ROW);

    int nameIndex = Data.getColumnIndex(VIEW_ITEMS, COL_ITEM_NAME);
    int qtyScale = Data.getColumnScale(getViewName(), COL_TRADE_ITEM_QUANTITY);

    Set<Long> itemIds = new HashSet<>();

    if (!BeeUtils.isEmpty(getGridView().getRowData())) {
      int itemIndex = getDataIndex(COL_TA_ITEM);
      for (IsRow row : getGridView().getRowData()) {
        itemIds.add(row.getLong(itemIndex));
      }
    }

    boolean hasItems = false;

    r++;
    for (ImportEntry entry : entries) {
      c = 0;

      duplicate = entry.item != null && itemIds.contains(entry.item.getId());

      if (entry.item != null && !duplicate) {
        Toggle toggle = new Toggle(FontAwesome.SQUARE_O, FontAwesome.CHECK_SQUARE_O,
            STYLE_IMPORT_TOGGLE_WIDGET, true);

        toggle.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            Element rowElement = table.getEventRowElement(event, false);

            if (rowElement != null && event.getSource() instanceof Toggle) {
              if (((Toggle) event.getSource()).isChecked()) {
                rowElement.addClassName(STYLE_IMPORT_SELECTED_ROW);
              } else {
                rowElement.removeClassName(STYLE_IMPORT_SELECTED_ROW);
              }
            }
          }
        });

        table.setWidget(r, c, toggle, STYLE_IMPORT_TOGGLE_PREFIX + STYLE_CELL_SUFFIX);

        itemIds.add(entry.item.getId());
        hasItems = true;
      }
      c++;

      table.setText(r, c++, entry.article, STYLE_IMPORT_ARTICLE_PREFIX + STYLE_CELL_SUFFIX);
      table.setText(r, c++, entry.name, STYLE_IMPORT_NAME_PREFIX + STYLE_CELL_SUFFIX);

      table.setText(r, c++, BeeUtils.toString(entry.quantity, qtyScale),
          STYLE_IMPORT_QTY_PREFIX + STYLE_CELL_SUFFIX);

      if (entry.item != null) {
        Double qtyFrom = null;

        if (!warehouseIds.isEmpty() && !BeeUtils.isEmpty(entry.item.getProperties())) {
          for (Map.Entry<String, String> property : entry.item.getProperties().entrySet()) {
            if (BeeUtils.isPrefix(property.getKey(), PRP_WAREHOUSE_PREFIX)) {
              Long w = BeeUtils.toLongOrNull(BeeUtils.removePrefix(property.getKey(),
                  PRP_WAREHOUSE_PREFIX));

              if (DataUtils.isId(w) && warehouseIds.contains(w)) {
                if (w.equals(warehouseFrom)) {
                  pfx = STYLE_IMPORT_FROM_PREFIX;
                  qtyFrom = BeeUtils.toDoubleOrNull(property.getValue());
                } else {
                  pfx = STYLE_IMPORT_STOCK_PREFIX;
                }

                table.setText(r, c + warehouseIds.indexOf(w), property.getValue(),
                    pfx + STYLE_CELL_SUFFIX);
              }
            }
          }
        }
        c += warehouseIds.size();

        table.setText(r, c++, entry.item.getString(nameIndex),
            STYLE_IMPORT_ITEM_NAME_PREFIX + STYLE_CELL_SUFFIX);
        table.setText(r, c++, BeeUtils.toString(entry.item.getId()),
            STYLE_IMPORT_ID_PREFIX + STYLE_CELL_SUFFIX);

        if (!duplicate && DataUtils.isId(warehouseFrom)
            && (!BeeUtils.isPositive(qtyFrom) || BeeUtils.isMore(entry.quantity, qtyFrom))) {
          table.getRowFormatter().addStyleName(r, STYLE_IMPORT_NO_STOCK_ROW);
        }
      }

      table.getRowFormatter().addStyleName(r, STYLE_IMPORT_ITEM_ROW);

      if (duplicate) {
        table.getRowFormatter().addStyleName(r, STYLE_IMPORT_DUPLICATE_ROW);
        table.getRow(r).setTitle(Localized.getMessages().valueExists(entry.article));

      } else if (entry.item != null) {
        table.getRowFormatter().addStyleName(r, STYLE_IMPORT_SELECTED_ROW);
      }

      r++;
    }

    final DialogBox dialog = DialogBox.withoutCloseBox(caption, STYLE_IMPORT_DIALOG);

    if (hasItems) {
      Image save = new Image(Global.getImages().silverSave());
      save.addStyleName(STYLE_IMPORT_SAVE);

      save.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialog.close();

          BeeRowSet selection = new BeeRowSet(VIEW_ITEMS, Data.getColumns(VIEW_ITEMS));
          for (int i = 0; i < entries.size(); i++) {
            if (table.getRow(i + 1).hasClassName(STYLE_IMPORT_SELECTED_ROW)) {
              ImportEntry entry = entries.get(i);

              if (entry.item != null) {
                BeeRow row = DataUtils.cloneRow(entry.item);
                row.setProperty(PRP_QUANTITY, BeeUtils.toString(entry.quantity));

                selection.addRow(row);
              }
            }
          }

          if (!selection.isEmpty()) {
            addItems(selection);
          }
        }
      });

      dialog.addAction(Action.SAVE, save);
    }

    Image close = new Image(Global.getImages().silverClose());
    close.addStyleName(STYLE_IMPORT_CLOSE);

    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.close();
      }
    });

    dialog.addAction(Action.CLOSE, close);

    Simple wrapper = new Simple(table);
    wrapper.addStyleName(STYLE_IMPORT_WRAPPER);

    dialog.setWidget(wrapper);
    dialog.center();
  }

  private static List<ImportEntry> parseImport(List<String> lines, String pattern) {
    List<ImportEntry> result = new ArrayList<>();

    RegExp rx;
    try {
      rx = RegExp.compile(pattern);
    } catch (RuntimeException ex) {
      logger.severe(pattern, ex);
      BeeKeeper.getScreen().notifySevere("cannot compile pattern", pattern);

      rx = null;
    }

    if (!BeeUtils.isEmpty(lines) && rx != null) {
      for (String line : lines) {
        MatchResult matchResult = rx.exec(line);

        if (matchResult != null && matchResult.getGroupCount() == 4) {
          String article = BeeUtils.trim(matchResult.getGroup(1));
          String name = BeeUtils.trim(matchResult.getGroup(2));
          Double qty = BeeUtils.toDoubleOrNull(matchResult.getGroup(3));

          if (!BeeUtils.isEmpty(article) && BeeUtils.isPositive(qty)) {
            result.add(new ImportEntry(article, name, qty));
          } else {
            logger.warning("cannot parse", line);
            logger.warning("article", article, "name", name, "qty", matchResult.getGroup(3));
          }

        } else {
          logger.warning(line, "does not match");
        }
      }
    }

    logger.debug(pattern);
    logger.debug("matched", result.size(), "of", lines.size());

    return result;
  }

  private void readImport(Collection<? extends FileInfo> input,
      final Consumer<List<String>> consumer) {

    final List<File> files = new ArrayList<>();

    for (FileInfo fileInfo : input) {
      File file = (fileInfo instanceof NewFileInfo) ? ((NewFileInfo) fileInfo).getFile() : null;
      if (file != null) {
        files.add(file);
      }
    }

    if (files.isEmpty()) {
      getGridView().notifyWarning(Localized.getConstants().noData());

    } else {
      final List<String> lines = new ArrayList<>();

      final Latch latch = new Latch(files.size());
      for (File file : files) {
        FileUtils.readLines(file, new Consumer<List<String>>() {
          @Override
          public void accept(List<String> content) {
            if (!content.isEmpty()) {
              lines.addAll(content);
            }

            latch.decrement();
            if (latch.isOpen()) {
              consumer.accept(lines);
            }
          }
        });
      }
    }
  }

  private TradeActItemPicker ensurePicker() {
    if (picker == null) {
      picker = new TradeActItemPicker();
      picker.addSelectionHandler(this);
    }

    return picker;
  }

  private Double getDefaultDiscount() {
    IsRow row = getGridView().getActiveRow();
    if (row == null) {
      row = BeeUtils.getLast(getGridView().getRowData());
    }

    return (row == null) ? null : row.getDouble(getDataIndex(COL_TRADE_DISCOUNT));
  }
}
