package com.butent.bee.client.output;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.FormElement;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.Timer;

import com.butent.bee.client.BeeKeeper;
import com.butent.bee.client.Global;
import com.butent.bee.client.data.Queries;
import com.butent.bee.client.dialog.StringCallback;
import com.butent.bee.client.grid.CellContext;
import com.butent.bee.client.presenter.GridPresenter;
import com.butent.bee.client.screen.BodyPanel;
import com.butent.bee.client.view.grid.CellGrid;
import com.butent.bee.client.view.grid.ColumnInfo;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.css.Colors;
import com.butent.bee.shared.css.CssUnit;
import com.butent.bee.shared.css.values.VerticalAlign;
import com.butent.bee.shared.data.BeeRowSet;
import com.butent.bee.shared.data.IsRow;
import com.butent.bee.shared.data.cache.CachingPolicy;
import com.butent.bee.shared.data.filter.Filter;
import com.butent.bee.shared.data.view.Order;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XFont;
import com.butent.bee.shared.export.XRow;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.export.XWorkbook;
import com.butent.bee.shared.html.Keywords;
import com.butent.bee.shared.i18n.Localized;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.ui.Action;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Exporter {

  private static final BeeLogger logger = LogUtils.getLogger(Exporter.class);

  private static final String EXPORT_URL = "export";

  private static final int SUBMIT_TIMEOUT = 60_000;

  private static final double GRID_CAPTION_HEIGHT_FACTOR = 1.5;
  private static final double GRID_HEADER_HEIGHT_FACTOR = 1.2;

  public static void confirm(String fileName, StringCallback callback) {
    Assert.notNull(callback);

    int width = BeeUtils.resize(BeeUtils.trim(fileName).length(), 20, 100, 300, 600);

    Global.inputString(Localized.getConstants().exportToMsExcel(),
        Localized.getConstants().fileName(), callback, fileName, 200, null,
        width, CssUnit.PX, BeeConst.UNDEF,
        Action.EXPORT.getCaption(), Action.CANCEL.getCaption(), null);
  }

  public static void export(GridPresenter presenter, String caption, final String fileName) {
    Assert.notNull(presenter);
    Assert.isTrue(validateFileName(fileName));

    CellGrid grid = presenter.getGridView().getGrid();

    final int rowCount = grid.getRowCount();
    if (rowCount <= 0) {
      String message = "grid is empty";
      logger.warning(message);
      BeeKeeper.getScreen().notifyWarning(message);
      return;
    }

    final List<ColumnInfo> columns = new ArrayList<>();

    List<ColumnInfo> gridColumns = grid.getColumns();
    for (ColumnInfo column : gridColumns) {
      if (column.isExportable()) {
        columns.add(column);
      }
    }

    int columnCount = columns.size();
    if (columnCount <= 0) {
      String message = "No exportable columns found";
      logger.warning(message);
      BeeKeeper.getScreen().notifyWarning(message);
      return;
    }

    final XSheet sheet = new XSheet();
    int rowIndex = 0;

    XRow row;
    XCell cell;
    XFont font;
    XStyle style;

    Integer styleRef;

    if (!BeeUtils.isEmpty(caption)) {
      font = XFont.bold();
      font.setFactor(GRID_CAPTION_HEIGHT_FACTOR);

      style = XStyle.center();
      style.setFontRef(sheet.registeFont(font));

      cell = new XCell(0, caption, sheet.registerStyle(style));
      if (columnCount > 1) {
        cell.setColSpan(columnCount);
      }

      rowIndex++;
      row = new XRow(rowIndex++);
      row.setHeightFactor(GRID_CAPTION_HEIGHT_FACTOR);
      
      row.add(cell);
      sheet.add(row);
    }

    Filter filter = presenter.getDataProvider().getUserFilter();
    if (filter != null) {
      String label = presenter.getFilterLabel();
      if (BeeUtils.isEmpty(label)) {
        label = filter.toString();
      }

      style = XStyle.center();

      cell = new XCell(0, label, sheet.registerStyle(style));
      if (columnCount > 1) {
        cell.setColSpan(columnCount);
      }

      row = new XRow(rowIndex++);
      row.add(cell);
      sheet.add(row);
    }

    if (rowIndex > 0) {
      rowIndex++;
    }

    if (grid.hasHeaders()) {
      row = new XRow(rowIndex++);
      row.setHeightFactor(GRID_HEADER_HEIGHT_FACTOR);

      style = XStyle.center();
      style.setVerticalAlign(VerticalAlign.MIDDLE);
      style.setColor(Colors.LIGHTGRAY);
      style.setFontRef(sheet.registeFont(XFont.bold()));

      styleRef = sheet.registerStyle(style);

      for (int i = 0; i < columnCount; i++) {
        row.add(new XCell(i, columns.get(i).getCaption(), styleRef));
      }
      sheet.add(row);
    }

    final Map<Integer, Integer> bodyStyles = new HashMap<>();
    for (int i = 0; i < columnCount; i++) {
      styleRef = columns.get(i).getColumn().initExport(sheet);

      if (styleRef != null) {
        bodyStyles.put(i, styleRef);
      }
    }

    final CellContext context = new CellContext(grid);

    int dataSize = grid.getDataSize();
    String viewName = presenter.getViewName();

    final Double heightFactor = getRowHeightFactor(grid);

    if (dataSize >= rowCount || BeeUtils.isEmpty(viewName)) {
      for (int i = 0; i < dataSize; i++) {
        IsRow dataRow = BeeUtils.getQuietly(grid.getRowData(), i);
        if (dataRow != null) {
          exportRow(context, dataRow, columns, rowIndex++, heightFactor, sheet, bodyStyles);
        }
      }

      sheet.autoSizeAll();
      export(sheet, fileName);

    } else {
      int stepSize = Math.min(rowCount, 100);
      int numberOfSteps = rowCount / stepSize;
      if (rowCount % stepSize > 0) {
        numberOfSteps++;
      }

      final Holder<Integer> chunks = Holder.of(numberOfSteps);

      final int firstRowIndex = rowIndex;

      filter = presenter.getDataProvider().getFilter();
      Order order = presenter.getDataProvider().getOrder();

      for (int offset = 0; offset < rowCount; offset += stepSize) {
        Queries.getRowSet(viewName, null, filter, order, offset, stepSize, CachingPolicy.NONE,
            new Queries.RowSetCallback() {
              @Override
              public void onSuccess(BeeRowSet result) {
                String respOffset = result.getTableProperty(Service.VAR_VIEW_OFFSET);
                Integer remaining = chunks.get();
                logger.debug(remaining, respOffset);

                if (BeeUtils.isDigit(respOffset)) {
                  int index = BeeUtils.toInt(respOffset) + firstRowIndex;
                  for (IsRow dataRow : result) {
                    exportRow(context, dataRow, columns, index++, heightFactor, sheet, bodyStyles);
                  }
                }

                chunks.set(remaining - 1);
                if (remaining <= 1) {
                  if (rowCount < 1000) {
                    sheet.autoSizeAll();
                  }
                  export(sheet, fileName);
                }
              }
            });
      }
    }
  }

  public static void export(XSheet sheet, String fileName) {
    if (sheet == null || sheet.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "sheet is empty");

    } else {
      XWorkbook wb = new XWorkbook();
      wb.add(sheet);
      export(wb, fileName);
    }
  }
  
  private static Double getRowHeightFactor(CellGrid grid) {
    int height = grid.getBodyCellHeight();
    if (height >= 30) {
      return height / 20d;
    } else {
      return null;
    }
  }

  public static void export(XWorkbook wb, String fileName) {
    if (wb == null || wb.isEmpty()) {
      logger.severe(NameUtils.getClassName(Exporter.class), "workbook is empty");

    } else if (BeeUtils.isEmpty(fileName)) {
      logger.severe(NameUtils.getClassName(Exporter.class), "file name not specified");

    } else if (!validateFileName(fileName)) {
      logger.severe(NameUtils.getClassName(Exporter.class), "invalid file name", fileName);

    } else {
      String frameName = NameUtils.createUniqueName("frame");

      final IFrameElement frame = Document.get().createIFrameElement();

      frame.setName(frameName);
      frame.setSrc(Keywords.URL_ABOUT_BLANK);

      BodyPanel.conceal(frame);

      final FormElement form = Document.get().createFormElement();

      form.setAcceptCharset(BeeConst.CHARSET_UTF8);
      form.setMethod(Keywords.METHOD_POST);
      form.setAction(GWT.getHostPageBaseURL() + EXPORT_URL);
      form.setTarget(frameName);

      form.appendChild(createFormParameter(Service.RPC_VAR_SVC, Service.EXPORT_WORKBOOK));
      form.appendChild(createFormParameter(Service.VAR_FILE_NAME, sanitizeFileName(fileName)));
      form.appendChild(createFormParameter(Service.VAR_DATA, wb.serialize()));

      BodyPanel.conceal(form);
      form.submit();

      Timer timer = new Timer() {
        @Override
        public void run() {
          form.removeFromParent();
          frame.removeFromParent();
        }
      };

      timer.schedule(SUBMIT_TIMEOUT);
    }
  }

  public static void maybeExport(final XSheet sheet, String fileName) {
    StringCallback callback = new StringCallback() {
      @Override
      public void onSuccess(String value) {
        export(sheet, BeeUtils.trim(value));
      }

      @Override
      public boolean validate(String value) {
        return validateFileName(value);
      }
    };

    confirm(fileName, callback);
  }

  public static boolean validateFileName(String input) {
    return !BeeUtils.isEmpty(sanitizeFileName(input));
  }

  private static InputElement createFormParameter(String name, String value) {
    InputElement inputElement = Document.get().createHiddenInputElement();

    inputElement.setName(name);
    inputElement.setValue(value);

    return inputElement;
  }

  private static void exportRow(CellContext context, IsRow dataRow, List<ColumnInfo> columns,
      int rowIndex, Double heightFactor, XSheet sheet, Map<Integer, Integer> styles) {

    context.setRow(dataRow);

    XRow row = new XRow(rowIndex);
    if (BeeUtils.isPositive(heightFactor)) {
      row.setHeightFactor(heightFactor);
    }

    int columnCount = columns.size();
    for (int j = 0; j < columnCount; j++) {
      context.setColumnIndex(j);

      ColumnInfo columnInfo = columns.get(j);

      Integer styleRef = null;
      if (columnInfo.getDynStyles() != null) {
        styleRef = columnInfo.getDynStyles().getExportStyleRef(dataRow, sheet);
      }
      if (styleRef == null) {
        styleRef = styles.get(j);
      }

      XCell cell = columnInfo.getColumn().export(context, styleRef, sheet);
      if (cell != null) {
        row.add(cell);
      }
    }

    sheet.add(row);
  }

  private static String sanitizeFileName(String input) {
    return FileNameUtils.sanitize(input, BeeConst.STRING_POINT);
  }

  private Exporter() {
  }
}
