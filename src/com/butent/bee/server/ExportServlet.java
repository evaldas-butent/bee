package com.butent.bee.server;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;

import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.value.Value;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.export.XCell;
import com.butent.bee.shared.export.XRow;
import com.butent.bee.shared.export.XSheet;
import com.butent.bee.shared.export.XStyle;
import com.butent.bee.shared.export.XWorkbook;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/export")
@SuppressWarnings("serial")
public class ExportServlet extends LoginServlet {

  private static BeeLogger logger = LogUtils.getLogger(ExportServlet.class);

  private static final String EXT_WORKBOOK = "xlsx";

  private static CellStyle convertStyle(Workbook wb, XStyle input) {
    if (input == null) {
      return null;
    }

    CellStyle cellStyle = wb.createCellStyle();

    if (input.hasFont()) {
      Font font = wb.createFont();

      if (!BeeUtils.isEmpty(input.getFontName())) {
        font.setFontName(input.getFontName());
      }
      if (input.getFontHeight() > 0) {
        font.setFontHeightInPoints((short) input.getFontHeight());
      }

      if (input.getFontWeight() != null) {
        switch (input.getFontWeight()) {
          case BOLD:
          case BOLDER:
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            break;

          case NORMAL:
            font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
            break;

          default:
            logger.warning("font weight", input.getFontWeight(), "not converted");
        }
      }

      if (input.getFontStyle() != null) {
        switch (input.getFontStyle()) {
          case ITALIC:
          case OBLIQUE:
            font.setItalic(true);
            break;
          case NORMAL:
            font.setItalic(false);
            break;
        }
      }

      cellStyle.setFont(font);
    }

    if (!BeeUtils.isEmpty(input.getFormat())) {
      cellStyle.setDataFormat(wb.createDataFormat().getFormat(input.getFormat()));
    }

    if (input.getTextAlign() != null) {
      switch (input.getTextAlign()) {
        case CENTER:
          cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
          break;
        case END:
        case RIGHT:
          cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
          break;
        case JUSTIFY:
          cellStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
          break;
        case LEFT:
        case START:
          cellStyle.setAlignment(CellStyle.ALIGN_LEFT);
          break;
        case MATCH_PARENT:
          cellStyle.setAlignment(CellStyle.ALIGN_GENERAL);
          break;
        case START_END:
          cellStyle.setAlignment(CellStyle.ALIGN_FILL);
          break;
      }
    }
    
    if (input.getVerticalAlign() != null) {
      switch (input.getVerticalAlign()) {
        case BOTTOM:
          cellStyle.setVerticalAlignment(CellStyle.VERTICAL_BOTTOM);
          break;
        case CENTRAL:
        case MIDDLE:
          cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
          break;
        case TOP:
          cellStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
          break;
        default:
          logger.warning("vertical align", input.getVerticalAlign(), "not converted");
      }
    }

    return cellStyle;
  }

  private static Workbook createWorkbook(XWorkbook inputBook) {
    Workbook wb = new XSSFWorkbook();

    for (XSheet inputSheet : inputBook.getSheets()) {
      Sheet sheet;
      if (BeeUtils.isEmpty(inputSheet.getName())) {
        sheet = wb.createSheet();
      } else {
        sheet = wb.createSheet(inputSheet.getName());
      }

      for (XRow inputRow : inputSheet.getRows()) {
        Row row = sheet.createRow(inputRow.getIndex());

        if (inputRow.getHeight() > 0) {
          row.setHeightInPoints(inputRow.getHeight());
        }

        XStyle rowStyle = getStyle(inputSheet, inputRow.getStyleRef());
        if (rowStyle != null) {
          row.setRowStyle(convertStyle(wb, rowStyle));
        }
        
        for (XCell inputCell : inputRow.getCells()) {
          Cell cell = row.createCell(inputCell.getIndex());
          
          Value value = inputCell.getValue();
          if (value != null && !value.isNull()) {
            ValueType type = value.getType();
            
            switch (type) {
              case BOOLEAN:
                cell.setCellValue(value.getBoolean());
                break;
              case DATE:
                cell.setCellValue(value.getDate().getJava());
                break;
              case DATE_TIME:
                cell.setCellValue(value.getDateTime().getJava());
                break;
              case DECIMAL:
              case INTEGER:
              case LONG:
              case NUMBER:
                cell.setCellValue(value.getDouble());
                break;
              case TEXT:
              case TIME_OF_DAY:
                cell.setCellValue(value.getString());
                break;
              default:
                logger.warning("cell type", type, "not supported");
            }
          
          } else if (!BeeUtils.isEmpty(inputCell.getFormula())) {
            cell.setCellFormula(inputCell.getFormula());
          }

          XStyle cellStyle = getStyle(inputSheet, inputCell.getStyleRef());
          if (cellStyle != null) {
            cell.setCellStyle(convertStyle(wb, cellStyle));
          }
          
          if (inputCell.getColSpan() > 1) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(),
                cell.getColumnIndex(), cell.getColumnIndex() + inputCell.getColSpan() - 1));
          }
        }
      }
    }

    return wb;
  }
  
  private static XStyle getStyle(XSheet sheet, Integer index) {
    if (index == null || index < 0) {
      return null;
    } else if (index < sheet.getStyles().size()) {
      return sheet.getStyle(index);
    } else {
      logger.warning("invalid style index", index);
      return null;
    }
  }

  @Override
  protected void doService(HttpServletRequest req, HttpServletResponse resp) {
    long start = System.currentTimeMillis();

    RequestInfo reqInfo = new RequestInfo(req);
    String svc = reqInfo.getService();

    if (Service.EXPORT_WORKBOOK.equals(svc)) {
      String fileName = reqInfo.getParameter(Service.VAR_FILE_NAME);
      if (BeeUtils.isEmpty(fileName)) {
        HttpUtils.badRequest(resp, svc, "parameter not found:", Service.VAR_FILE_NAME);
        return;
      }

      String data = reqInfo.getParameter(Service.VAR_DATA);
      if (BeeUtils.isEmpty(data)) {
        HttpUtils.badRequest(resp, svc, "parameter not found:", Service.VAR_DATA);
        return;
      }

      XWorkbook input = XWorkbook.restore(data);
      if (input == null || input.isEmpty()) {
        HttpUtils.badRequest(resp, svc, "workbook is empty");
        return;
      }

      Workbook workbook = createWorkbook(input);

      resp.reset();
      resp.setContentType(MediaType.MICROSOFT_EXCEL.toString());

      String name = Codec.rfc5987(FileNameUtils.defaultExtension(fileName, EXT_WORKBOOK));
      resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=" + name);

      logger.info(">", svc, fileName, name, TimeUtils.elapsedSeconds(start));

      try {
        OutputStream output = resp.getOutputStream();
        workbook.write(output);
        output.flush();

      } catch (IOException ex) {
        logger.error(ex);
      }

    } else {
      HttpUtils.badRequest(resp, NameUtils.getName(this), "service not recognized", svc);
    }
  }
}
