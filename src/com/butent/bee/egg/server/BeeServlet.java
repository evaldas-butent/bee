package com.butent.bee.egg.server;

import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;

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

@SuppressWarnings("serial")
@WebServlet(name = "BeeServlet", urlPatterns = {"/egg/bee"})
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
    long start = System.currentTimeMillis();

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

    boolean debug = reqInfo.isDebug();

    LogUtils.infoNow(logger, rid, "request", meth, svc, dsn, opt);
    if (debug) {
      reqInfo.logParams(logger);
      reqInfo.logFields(logger);
      reqInfo.logHeaders(logger);
    }

    if (reqInfo.getContentLen() > 0) {
      LogUtils.info(logger, rid, "content", reqInfo.getContentType(),
          reqInfo.getDataType(), BeeUtils.bracket(reqInfo.getContentLen()));
      if (debug) {
        LogUtils.info(logger, reqInfo.getContent());
      }
    }

    ResponseBuffer buff = new ResponseBuffer(sep);

    dispatcher.doService(svc, dsn, reqInfo, buff);

    int respLen = buff.getSize();
    int mc = buff.getMessageCount();

    if (respLen > 0 || mc > 0) {
      int cnt = buff.getCount();
      int cc = buff.getColumnCount();

      BeeService.DATA_TYPE dtp = buff.getDataType();
      if (dtp == null) {
        dtp = (cc > 0) ? BeeService.DATA_TYPE.TABLE
            : BeeService.DEFAULT_RESPONSE_DATA_TYPE;
      }

      resp.setHeader(BeeService.RPC_FIELD_SID, sid);
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

      resp.setHeader(BeeService.RPC_FIELD_DTP, BeeService.transform(dtp));

      String ct = BeeService.getContentType(dtp);
      if (!BeeUtils.isEmpty(ct)) {
        resp.setContentType(ct);
      }

      String ce = BeeService.getCharacterEncoding(dtp);
      if (!BeeUtils.isEmpty(ce)) {
        resp.setCharacterEncoding(ce);
      }

      String s;
      if (respLen > 0) {
        s = buff.getString();
      } else if (mc > 0) {
        s = "Messages " + BeeUtils.bracket(mc);
      } else {
        s = BeeConst.EMPTY;
      }

      LogUtils.infoNow(logger, BeeUtils.elapsedSeconds(start), rid, "response",
          dtp, cc, cnt, respLen, mc);

      try {
        ServletOutputStream out = resp.getOutputStream();
        out.print(s);
        out.flush();
      } catch (IOException ex) {
        logger.severe(ex.getMessage());
      }
    }
  }

}
