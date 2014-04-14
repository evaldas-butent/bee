package com.butent.bee.client.output;

import com.google.gwt.user.client.Window;

import com.butent.bee.client.grid.HtmlTable;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

public final class Exporter {

  private static final BeeLogger logger = LogUtils.getLogger(Exporter.class);

  private static final String URI_EXCEL = "data:application/vnd.ms-excel;base64,";

  private static final String KEY_SHEET = "{worksheet}";
  private static final String KEY_TABLE = "{table}";

  private static final String TEMPLATE_EXCEL = "<html "
      + "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
      + "xmlns:x=\"urn:schemas-microsoft-com:office:excel\" "
      + "xmlns=\"http://www.w3.org/TR/REC-html40\">"
      + "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"
      + "<xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>"
      + "<x:Name>" + KEY_SHEET + "</x:Name>"
      + "<x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions>"
      + "</x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml>"
      + "</head><body><table>" + KEY_TABLE + "</table></body></html>";

  public static void toExcel(HtmlTable table) {
    if (table == null || table.isEmpty()) {
      logger.warning(NameUtils.getClassName(Exporter.class), "table is empty");
      return;
    }

    String s = TEMPLATE_EXCEL.replace(KEY_SHEET, "sheetb")
        .replace(KEY_TABLE, table.getElement().getInnerHTML());

    String url = URI_EXCEL + Codec.encodeBase64(s);
    Window.open(url, "nnn", null);
  }

  private Exporter() {
  }
}
