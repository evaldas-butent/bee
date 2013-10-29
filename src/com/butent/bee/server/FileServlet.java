package com.butent.bee.server;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.HttpUtils;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.ejb.EJB;
import javax.mail.internet.MimeUtility;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages file transfers between client and server sides.
 */

@WebServlet(urlPatterns = "/file")
@SuppressWarnings("serial")
public class FileServlet extends HttpServlet {

  private static final int DEFAULT_BUFFER_SIZE = 10240;

  private static BeeLogger logger = LogUtils.getLogger(FileServlet.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doService(req, resp);
  }

  private static void close(Closeable resource) {
    if (resource != null) {
      try {
        resource.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void doService(HttpServletRequest req, HttpServletResponse resp) {
    Map<String, String> parameters = HttpUtils.getHeaders(req, false);
    parameters.putAll(HttpUtils.getParameters(req, true));

    Long fileId = BeeUtils.toLongOrNull(parameters.get(Service.VAR_FILE_ID));
    String fileName = parameters.get(Service.VAR_FILE_NAME);
    String path = null;
    String mimeType = null;

    if (DataUtils.isId(fileId)) {
      SimpleRow row = qs.getRow(new SqlSelect()
          .addFields(TBL_FILES, COL_FILE_REPO, COL_FILE_NAME, COL_FILE_TYPE)
          .addFrom(TBL_FILES)
          .setWhere(sys.idEquals(TBL_FILES, fileId)));

      if (row != null) {
        path = row.getValue(COL_FILE_REPO);
        fileName = BeeUtils.notEmpty(fileName, row.getValue(COL_FILE_NAME));
        mimeType = row.getValue(COL_FILE_TYPE);
      }
    } else if (!BeeUtils.isEmpty(fileName)) {
      path = Config.getPath(fileName, false);
      fileName = new File(fileName).getName();
    }
    if (path == null) {
      HttpUtils.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          BeeUtils.joinWords("File not found:", fileName));
      return;
    }
    File file = new File(path);

    if (!FileUtils.isInputFile(file)) {
      HttpUtils.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          BeeUtils.joinWords("File was removed:", fileName));
      return;
    }

    if (mimeType == null) {
      mimeType = getServletContext().getMimeType(fileName);
    }
    if (mimeType == null) {
      mimeType = "application/octet-stream";
    }
    try {
      fileName = MimeUtility.encodeText(fileName);
    } catch (UnsupportedEncodingException ex) {
      logger.warning(ex);
    }
    mimeType = BeeUtils.join("; ", mimeType, "name=\"" + fileName + "\"");

    resp.reset();
    resp.setBufferSize(DEFAULT_BUFFER_SIZE);
    resp.setContentType(mimeType);
    resp.setHeader("Content-Length", String.valueOf(file.length()));
    resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

    BufferedInputStream input = null;
    BufferedOutputStream output = null;

    try {
      input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
      output = new BufferedOutputStream(resp.getOutputStream(), DEFAULT_BUFFER_SIZE);

      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
      int length;
      while ((length = input.read(buffer)) > 0) {
        output.write(buffer, 0, length);
      }
    } catch (IOException e) {
      logger.error(e);
    } finally {
      close(output);
      close(input);
    }
  }
}
