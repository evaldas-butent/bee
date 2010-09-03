package com.butent.bee.egg.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

@SuppressWarnings("serial")
@WebServlet(name = "BeeServlet", urlPatterns = { "/egg/bee" })
public class BeeServlet extends HttpServlet {
  private static Logger logger = Logger.getLogger(BeeServlet.class.getName());

  @EJB
  DispatcherBean dispatcher;

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

  private void doService(HttpServletRequest req, HttpServletResponse resp) {
    HttpSession session = req.getSession();
    String sid = session.getId();

    RequestInfo reqInfo = new RequestInfo(req);

    String meth, rid, svc, dsn, sep, opt;

    meth = reqInfo.getMethod();

    rid = reqInfo.getId();
    svc = reqInfo.getService();
    dsn = reqInfo.getDsn();
    sep = reqInfo.getSeparator();
    opt = reqInfo.getOptions();

    LogUtils.infoNow(logger, rid, "received request", meth, svc, dsn, sep, opt);
    LogUtils
        .info(logger, rid, "received headers", reqInfo.getHeadersAsString());

    if (reqInfo.getContentLen() > 0)
      LogUtils
          .info(logger, rid, "received content", reqInfo.getContentType(),
              BeeUtils.bracket(reqInfo.getContentLen()),
              BeeUtils.bracket(reqInfo.getContent().length()),
              reqInfo.getContent());

    ResponseBuffer buff = new ResponseBuffer(sep);

    dispatcher.doService(svc, dsn, reqInfo, buff);

    int respLen = buff.getSize();

    if (respLen > 0) {
      int cnt = buff.getCount();
      int cc = buff.getColumnCount();
      int mc = buff.getMessageCount();

      LogUtils.infoNow(logger, rid, "sending response", meth, svc, dsn, cc,
          cnt, respLen, mc);

      resp.setHeader(BeeService.RPC_FIELD_QID, rid);
      if (!BeeUtils.isEmpty(sep) || !buff.isDefaultSeparator()) {
        resp.setHeader(BeeService.RPC_FIELD_SEP, buff.getHexSeparator());
      }

      if (cnt > 0) {
        resp.setIntHeader(BeeService.RPC_FIELD_CNT, cnt);
      }
      if (cc > 0) {
        resp.setIntHeader(BeeService.RPC_FIELD_COLS, cc);
      }

      if (mc > 0) {
        resp.setIntHeader(BeeService.RPC_FIELD_MSG_CNT, mc);
        for (int i = 0; i < mc; i++) {
          resp.setHeader(BeeService.rpcMessageName(i), buff.getMessage(i));
        }
      }

      resp.setContentType("text/plain");
      resp.setCharacterEncoding("utf-8");

      // resp.setContentLength(respLen);

      try {
        ServletOutputStream out = resp.getOutputStream();
        out.print(buff.getString());
        out.flush();
      } catch (IOException ex) {
        logger.severe(ex.getMessage());
      }
    }
  }

}
