package com.butent.bee.server;

import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "FileServlet", urlPatterns = {"/bee/file/*"})
public class FileServlet extends HttpServlet {
  private static Logger logger = Logger.getLogger(FileServlet.class.getName());

  private static final int DEFAULT_BUFFER_SIZE = 10240;

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

  private void doService(HttpServletRequest req, HttpServletResponse resp) {

    String requestedFile = req.getPathInfo();

    if (requestedFile == null) {
      LogUtils.warning(logger, "No file name provided");
      try {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No file name provided");
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }

    requestedFile = Codec.decodeBase64(requestedFile.substring(1));

    String path = Config.getPath(requestedFile);
    if (path == null) {
      try {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, requestedFile);
      } catch (IOException e) {
        e.printStackTrace();
      }
      return;
    }

    File file = new File(path);

    String contentType = getServletContext().getMimeType(file.getName());

    if (contentType == null) {
      contentType = "application/octet-stream";
    }

    resp.reset();
    resp.setBufferSize(DEFAULT_BUFFER_SIZE);
    resp.setContentType(contentType);
    resp.setHeader("Content-Length", String.valueOf(file.length()));
    resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

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
      e.printStackTrace();
    } finally {
      close(output);
      close(input);
    }
  }
}
