package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Sends client side HTTP service requests to server, extends Java standard <code>HttpServlet</code>
 * functionality.
 */

@SuppressWarnings("serial")
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
      reqInfo.logVars(logger);
      reqInfo.logHeaders(logger);
    }

    if (reqInfo.getContentLen() > 0) {
      LogUtils.info(logger, rid, "content", reqInfo.getContentTypeHeader(),
          reqInfo.getContentType(), BeeUtils.bracket(reqInfo.getContentLen()));
      if (debug) {
        LogUtils.info(logger, reqInfo.getContent());
      }
    }

    HttpSession session = req.getSession(false);
    boolean loggedIn = (session != null);

    String reqSid = reqInfo.getParameter(Service.RPC_VAR_SID);
    String currentSid = loggedIn ? session.getId() : null;
    String respSid = null;

    ResponseObject response = null;
    ResponseBuffer buff = new ResponseBuffer(sep);

    if (BeeUtils.same(svc, Service.LOGOUT)) {
      if (loggedIn) {
        response = logout(req, session);
      } else {
        response = ResponseObject.error("Not logged in");
      }
      respSid = BeeConst.NULL;
    
    } else if (BeeUtils.same(svc, Service.LOGIN)) {
      if (loggedIn) {
        response = logout(req, session);
      } else {
        response = new ResponseObject();
      }

      String usr = reqInfo.getParameter(Service.VAR_LOGIN);
      String pwd = reqInfo.getParameter(Service.VAR_PASSWORD);

      try {
        req.login(usr, pwd);
        ResponseObject ro = dispatcher.doLogin(reqInfo.getLocale());

        if (ro.hasErrors()) {
          for (String error : ro.getErrors()) {
            response.addError(error);
          }
          req.logout();
          session = req.getSession(false);
          if (session != null) {
            session.invalidate();
          }
        } else {
          session = req.getSession(true);
          session.setAttribute(Service.VAR_LOGIN, req.getRemoteUser());
          respSid = session.getId();
          LogUtils.infoNow(logger, "session id:", respSid);

          if (ro.hasWarnings()) {
            for (String warning : ro.getWarnings()) {
              response.addWarning(warning);
            }
          }
          if (ro.hasNotifications()) {
            for (String note : ro.getNotifications()) {
              response.addInfo(note);
            }
          }
          response.setResponse(ro.getResponse());
        }
      } catch (ServletException e) {
        response.addError(e);
      }

    } else if (!BeeUtils.same(reqSid, currentSid)) {
      LogUtils.warning(logger, "session id:", "request =", reqSid, "current =", currentSid);
      if (loggedIn) {
        response = logout(req, session);
      } else {
        response = ResponseObject.error("request:", reqSid, "current:", currentSid);
      }
      respSid = BeeConst.NULL;
      
    } else {
      respSid = currentSid;
      response = dispatcher.doService(svc, dsn, reqInfo, buff);
    }

    resp.setHeader(Service.RPC_VAR_SID, respSid);
    resp.setHeader(Service.RPC_VAR_QID, rid);

    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");

    String s;

    if (response != null) {
      resp.setHeader(Service.RPC_VAR_RESP, "1");

      ContentType ctp = CommUtils.defaultResponseContentType;

      resp.setContentType(CommUtils.getMediaType(ctp));
      resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));
      resp.setHeader(Service.RPC_VAR_CTP, ctp.transform());

      s = CommUtils.prepareContent(ctp, Codec.beeSerialize(response));

      LogUtils.infoNow(logger, BeeUtils.elapsedSeconds(start), rid, "response",
          ctp, resp.getContentType(), s.length());

    } else {
      int respLen = buff.getSize();
      int mc = buff.getMessageCount();
      int pc = buff.getPartCount();

      int cnt = buff.getCount();
      int cc = buff.getColumnCount();

      ContentType ctp = buff.getContentType();
      if (ctp == null) {
        ctp = (cc > 0) ? ContentType.TABLE : CommUtils.defaultResponseContentType;
      }

      if (!BeeUtils.isEmpty(sep) || !buff.isDefaultSeparator()) {
        resp.setHeader(Service.RPC_VAR_SEP, buff.getHexSeparator());
      }

      if (cnt > 0) {
        resp.setIntHeader(Service.RPC_VAR_CNT, cnt);
      }
      if (cc > 0) {
        resp.setIntHeader(Service.RPC_VAR_COLS, cc);
      }

      if (mc > 0) {
        resp.setIntHeader(Service.RPC_VAR_MSG_CNT, mc);
        for (int i = 0; i < mc; i++) {
          resp.setHeader(CommUtils.rpcMessageName(i), buff.getMessage(i).serialize());
        }
      }

      resp.setHeader(Service.RPC_VAR_CTP, ctp.transform());

      String mt = BeeUtils.ifString(buff.getMediaType(), CommUtils.getMediaType(ctp));
      if (!BeeUtils.isEmpty(mt)) {
        resp.setContentType(mt);
      }

      String ce = BeeUtils.ifString(buff.getCharacterEncoding(),
          CommUtils.getCharacterEncoding(ctp));
      if (!BeeUtils.isEmpty(ce)) {
        resp.setCharacterEncoding(ce);
      }

      if (respLen > 0) {
        s = CommUtils.prepareContent(ctp, buff.getString());

      } else if (pc > 0) {
        resp.setIntHeader(Service.RPC_VAR_PART_CNT, pc);
        StringBuilder sb = new StringBuilder();
        int pn = 0;

        for (BeeResource br : buff.getParts()) {
          String part = br.serialize();
          sb.append(part);
          resp.setIntHeader(CommUtils.rpcPartName(pn++), part.length());
        }
        s = sb.toString();

      } else if (mc > 0) {
        s = "Messages " + BeeUtils.bracket(mc);
      } else {
        s = BeeConst.EMPTY;
      }
      LogUtils.infoNow(logger, BeeUtils.elapsedSeconds(start), rid, "response",
          ctp, resp.getContentType(), cnt, cc, mc, pc, s.length());
    }

    try {
      PrintWriter out = resp.getWriter();
      out.print(s);
      out.flush();
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
    }
  }

  private ResponseObject logout(HttpServletRequest req, HttpSession session) {
    ResponseObject response = new ResponseObject();

    try {
      req.logout();
      session.invalidate();
      response.addInfo("Logout successful");
    } catch (ServletException e) {
      response.addError(e);
    }

    return response;
  }
}
