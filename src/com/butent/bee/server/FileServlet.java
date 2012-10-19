package com.butent.bee.server;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Manages file transfers between client and server sides.
 */

@SuppressWarnings("serial")
public class FileServlet extends HttpServlet {
  private static BeeLogger logger = LogUtils.getLogger(FileServlet.class);

  private static final int DEFAULT_BUFFER_SIZE = 10240;

  @EJB
  QueryServiceBean qs;

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

  private void close(Closeable resource) {
    if (resource != null) {
      try {
        resource.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void doError(HttpServletResponse resp, String err) {
    try {
      logger.severe(err);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private void doService(HttpServletRequest req, HttpServletResponse resp) {
    Pair<String, String> data = null;

    String requestedData = req.getPathInfo();

    if (requestedData != null) {
      requestedData = requestedData.substring(1);
    }
    if (BeeUtils.isEmpty(requestedData)) {
      doError(resp, "No request data provided");
      return;
    } else {
      try {
        data = Pair.restore(Codec.decodeBase64(requestedData));
      } catch (Exception e) {
        doError(resp, e.getMessage());
        return;
      }
    }
    String path = null;
    String hash = data.getB();
    String fileName = BeeUtils.notEmpty(data.getA(), hash);
    String mimeType = null;

    if (!BeeUtils.isEmpty(hash)) {
      Map<String, String> row = qs.getRow(new SqlSelect()
          .addFields(CommonsConstants.TBL_FILES, "Repository", "Name", "Mime")
          .addFrom(CommonsConstants.TBL_FILES)
          .setWhere(SqlUtils.equal(CommonsConstants.TBL_FILES, "Hash", hash)));

      if (row != null) {
        path = row.get("Repository");
        fileName = BeeUtils.notEmpty(fileName, row.get("Name"));
        mimeType = row.get("Mime");
      }
    } else if (!BeeUtils.isEmpty(fileName)) {
      path = Config.getPath(fileName, false);
      fileName = new File(fileName).getName();
    }
    if (path == null) {
      doError(resp, BeeUtils.joinWords("File not found:", fileName));
      return;
    }
    File file = new File(path);

    if (!FileUtils.isInputFile(file)) {
      doError(resp, BeeUtils.joinWords("File was removed:", fileName));
      return;
    }
    if (mimeType == null) {
      mimeType = getServletContext().getMimeType(fileName);
    }
    if (mimeType == null) {
      mimeType = "application/octet-stream";
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
