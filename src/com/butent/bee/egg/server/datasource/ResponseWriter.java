package com.butent.bee.egg.server.datasource;

import com.butent.bee.egg.server.datasource.base.DataSourceParameters;
import com.butent.bee.egg.server.datasource.base.OutputType;
import com.butent.bee.egg.shared.Assert;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

public class ResponseWriter {
  private static final byte[] UTF_16LE_BOM = new byte[] {(byte) 0xff, (byte) 0xfe};
  
  public static void setServletResponse(String responseMessage,
      DataSourceParameters dataSourceParameters, HttpServletResponse res) throws IOException {
    OutputType type = dataSourceParameters.getOutputType();
    switch (type) {
      case CSV:
        setServletResponseCSV(responseMessage, dataSourceParameters, res);
        break;
      case TSV_EXCEL:
        setServletResponseTSVExcel(responseMessage, dataSourceParameters, res);
        break;
      case HTML:
        setServletResponseHTML(responseMessage, res);
        break;
      case JSONP:
      case JSON:
        setServletResponseJSON(responseMessage, res);
        break;
      default:
        Assert.untouchable("Unhandled output type.");
    }
  }

  private static void setServletResponseCSV(String responseMessage,
      DataSourceParameters dataSourceParameters, HttpServletResponse res) throws IOException {
    res.setContentType("text/csv; charset=UTF-8");
    String outFileName = dataSourceParameters.getOutFileName();
    
    if (!outFileName.toLowerCase().endsWith(".csv")) {
      outFileName = outFileName + ".csv";
    }
    
    res.setHeader("content-disposition", "attachment; filename=" + outFileName);
    writeServletResponse(responseMessage, res);
  }

  private static void setServletResponseHTML(String responseMessage, HttpServletResponse res)
      throws IOException {
    res.setContentType("text/html; charset=UTF-8");
    writeServletResponse(responseMessage, res);
  }
  
  private static void setServletResponseJSON(String responseMessage, HttpServletResponse res)
      throws IOException {
    res.setContentType("text/plain; charset=UTF-8");
    writeServletResponse(responseMessage, res);
  }
  
  private static void setServletResponseTSVExcel(String responseMessage,
      DataSourceParameters dsParams, HttpServletResponse res) throws IOException {
    res.setContentType("text/csv; charset=UTF-16LE");
    String outFileName = dsParams.getOutFileName();
    res.setHeader("Content-Disposition", "attachment; filename=" + outFileName);
    writeServletResponse(responseMessage, res, "UTF-16LE", UTF_16LE_BOM);
  }
  
  private static void writeServletResponse(CharSequence responseMessage, HttpServletResponse res) 
      throws IOException {
    writeServletResponse(responseMessage, res, "UTF-8", null);
  }

  private static void writeServletResponse(CharSequence charSequence, HttpServletResponse res,
      String charset, byte[] byteOrderMark) throws IOException {
    ServletOutputStream outputStream = res.getOutputStream();
    if (byteOrderMark != null) {
      outputStream.write(byteOrderMark);
    }
    outputStream.write(charSequence.toString().getBytes(charset));
  }

  private ResponseWriter() {
  }
}
