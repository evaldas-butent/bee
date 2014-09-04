package com.butent.bee.client.modules.trade.acts;

import com.google.common.collect.Lists;
import com.google.common.net.MediaType;
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
import com.butent.bee.client.composite.FileCollector;
import com.butent.bee.client.data.Data;
import com.butent.bee.client.data.IdCallback;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.DialogBox;
import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.client.i18n.Money;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.ui.UiHelper;
import com.butent.bee.client.utils.FileUtils;
import com.butent.bee.client.utils.NewFileInfo;
import com.butent.bee.client.view.grid.interceptor.AbstractGridInterceptor;
import com.butent.bee.client.view.grid.interceptor.GridInterceptor;
import com.butent.bee.client.widget.Button;
import com.butent.bee.client.widget.Image;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Consumer;
import com.butent.bee.shared.Latch;
import com.butent.bee.shared.data.BeeRow;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.classifiers.ItemPrice;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import elemental.html.File;

public class TradeActItemsGrid extends AbstractGridInterceptor implements
    SelectionHandler<BeeRowSet> {

  private static final BeeLogger logger = LogUtils.getLogger(TradeActItemsGrid.class);

  private static final String STYLE_IMPORT_PREFIX = TradeActKeeper.STYLE_PREFIX + "import-items-";

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
                      final Map<String, Double> quantityByCode =
                          parseImport(lines, BeeUtils.notEmpty(pattern, RX_IMPORT_ACT_ITEM));

                      if (quantityByCode.isEmpty()) {
                        getGridView().notifyWarning(importCaption,
                            Localized.getConstants().nothingFound());

                      } else {
                        Filter filter = Filter.and(Filter.isNull(COL_ITEM_IS_SERVICE),
                            Filter.anyString(COL_ITEM_ARTICLE, quantityByCode.keySet()));

                        Queries.getRowSet(VIEW_ITEMS, null, filter, new Queries.RowSetCallback() {
                          @Override
                          public void onSuccess(BeeRowSet items) {
                            showImportData(importCaption, quantityByCode, items);
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

      collector.setAccept(MediaType.PLAIN_TEXT_UTF_8);
      getGridView().add(collector);
    }
    return collector;
  }

  private void showImportData(String caption, final Map<String, Double> quantities,
      final BeeRowSet items) {

    final int codeIndex;
    int nameIndex;

    if (DataUtils.isEmpty(items)) {
      codeIndex = BeeConst.UNDEF;
      nameIndex = BeeConst.UNDEF;
    } else {
      codeIndex = items.getColumnIndex(COL_ITEM_ARTICLE);
      nameIndex = items.getColumnIndex(COL_ITEM_NAME);
    }

    HtmlTable table = new HtmlTable(STYLE_IMPORT_PREFIX + "table");

    int r = 0;
    int c;

    BeeRow item;

    for (Map.Entry<String, Double> entry : quantities.entrySet()) {
      String code = entry.getKey();
      item = BeeConst.isUndef(codeIndex) ? null : findItem(items, codeIndex, code);

      c = 0;

      if (item != null) {
        table.setText(r, c, item.getString(nameIndex), STYLE_IMPORT_PREFIX + "name");
      }

      table.setText(r, c++, code, STYLE_IMPORT_PREFIX + "code");
      table.setText(r, c++, BeeUtils.toString(entry.getValue()), STYLE_IMPORT_PREFIX + "qty");

      r++;
    }

    final DialogBox dialog = DialogBox.withoutCloseBox(caption, STYLE_IMPORT_PREFIX + "dialog");

    if (!DataUtils.isEmpty(items)) {
      Image save = new Image(Global.getImages().silverSave());
      save.addStyleName(STYLE_IMPORT_PREFIX + "save");

      save.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          dialog.close();

          BeeRowSet selection = new BeeRowSet(items.getViewName(), items.getColumns());
          for (Map.Entry<String, Double> entry : quantities.entrySet()) {
            BeeRow ir = findItem(items, codeIndex, entry.getKey());

            if (ir != null) {
              BeeRow row = DataUtils.cloneRow(ir);
              row.setProperty(PRP_QUANTITY, BeeUtils.toString(entry.getValue()));

              selection.addRow(row);
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
    close.addStyleName(STYLE_IMPORT_PREFIX + "close");

    close.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        dialog.close();
      }
    });

    dialog.addAction(Action.CLOSE, close);

    dialog.setWidget(table);
    dialog.center();
  }

  private static BeeRow findItem(BeeRowSet items, int columnIndex, String code) {
    for (BeeRow row : items) {
      if (code.equals(row.getString(columnIndex))) {
        return row;
      }
    }
    return null;
  }

  private static Map<String, Double> parseImport(List<String> lines, String pattern) {
    Map<String, Double> result = new LinkedHashMap<>();

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

        if (matchResult != null && matchResult.getGroupCount() == 3) {
          String code = BeeUtils.trim(matchResult.getGroup(1));
          Double qty = BeeUtils.toDoubleOrNull(matchResult.getGroup(2));

          if (!BeeUtils.isEmpty(code) && BeeUtils.isPositive(qty)) {
            if (result.containsKey(code)) {
              Double value = result.get(code);

              logger.warning(line);
              logger.warning("duplicate code", code, "qty", value, "+", qty);

              result.put(code, value + qty);

            } else {
              result.put(code, qty);
            }

          } else {
            logger.warning(line);
            logger.warning("code", code, "qty", matchResult.getGroup(2));
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
