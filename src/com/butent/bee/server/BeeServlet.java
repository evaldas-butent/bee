package com.butent.bee.server;

import com.google.common.net.HttpHeaders;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.EJB;
import javax.ejb.EJBException;
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

  private static BeeLogger logger = LogUtils.getLogger(BeeServlet.class);

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
    String sessionId = session.getId();

    RequestInfo reqInfo = new RequestInfo(req);

    String meth = reqInfo.getMethod();

    String rid = reqInfo.getId();
    String svc = reqInfo.getService();
    String sep = reqInfo.getSeparator();

    boolean debug = reqInfo.isDebug();

    logger.info("<", rid, meth, svc);
    if (debug) {
      reqInfo.logParams(logger);
      reqInfo.logVars(logger);
      reqInfo.logHeaders(logger);
    }

    if (reqInfo.getContentLen() > 0) {
      logger.info("<", rid, reqInfo.getContentTypeHeader(), reqInfo.getContentType(),
          BeeUtils.bracket(reqInfo.getContentLen()));
      if (debug) {
        logger.info(reqInfo.getContent());
      }
    }

    ResponseObject response = null;
    ResponseBuffer buff = new ResponseBuffer(sep);

    String reqSid = reqInfo.getParameter(Service.RPC_VAR_SID);

    boolean doLogin = false;
    boolean doLogout = false;

    if (BeeUtils.same(svc, Service.LOGIN)) {
      doLogin = session.getAttribute(Service.VAR_USER) == null;
      doLogout = !doLogin;

    } else if (BeeUtils.same(svc, Service.LOGOUT)) {
      doLogout = true;

    } else if (BeeUtils.isEmpty(reqSid)) {
      doLogout = session.getAttribute(Service.VAR_USER) != null;

    } else if (!BeeUtils.same(reqSid, sessionId)) {
      doLogout = true;
      logger.severe("session id:", "request =", reqSid, "current =", sessionId);
    }

    if (doLogin) {
      try {
        response = dispatcher.doLogin(reqInfo);
      } catch (EJBException ex) {
        response = ResponseObject.error(ex);
      }

      if (response.hasErrors()) {
        response.log(logger);
        logout(req, session);

      } else {
        session.setAttribute(Service.VAR_USER, req.getRemoteUser());

        resp.setHeader(Service.RPC_VAR_SID, sessionId);
        resp.setHeader(Service.RPC_VAR_QID, rid);

        logger.info("session id:", sessionId);
      }

    } else if (doLogout) {
      logout(req, session);
      return;

    } else {
      try {
        response = dispatcher.doService(svc, reqInfo, buff);
      } catch (EJBException ex) {
        response = ResponseObject.error(ex);
      }

      resp.setHeader(Service.RPC_VAR_QID, rid);
    }

    resp.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    resp.setHeader(HttpHeaders.PRAGMA, "no-cache");
    resp.setHeader(HttpHeaders.EXPIRES, "Thu, 01 Dec 1994 16:00:00 GMT");

    String s;

    if (response != null) {
      resp.setHeader(Service.RPC_VAR_RESP, "1");

      ContentType ctp = CommUtils.DEFAULT_RESPONSE_CONTENT_TYPE;

      resp.setContentType(CommUtils.getMediaType(ctp));
      resp.setCharacterEncoding(CommUtils.getCharacterEncoding(ctp));
      resp.setHeader(Service.RPC_VAR_CTP, ctp.name());

      s = CommUtils.prepareContent(ctp, Codec.beeSerialize(response));

      logger.info(">", rid, TimeUtils.elapsedSeconds(start), ctp, resp.getContentType(),
          s.length());

    } else {
      int respLen = buff.getSize();
      int mc = buff.getMessageCount();

      int cnt = buff.getCount();
      int cc = buff.getColumnCount();

      ContentType ctp = buff.getContentType();
      if (ctp == null) {
        ctp = (cc > 0) ? ContentType.TABLE : CommUtils.DEFAULT_RESPONSE_CONTENT_TYPE;
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

      resp.setHeader(Service.RPC_VAR_CTP, ctp.name());

      String mt = BeeUtils.notEmpty(buff.getMediaType(), CommUtils.getMediaType(ctp));
      if (!BeeUtils.isEmpty(mt)) {
        resp.setContentType(mt);
      }

      String ce = BeeUtils.notEmpty(buff.getCharacterEncoding(),
          CommUtils.getCharacterEncoding(ctp));
      if (!BeeUtils.isEmpty(ce)) {
        resp.setCharacterEncoding(ce);
      }

      if (respLen > 0) {
        s = CommUtils.prepareContent(ctp, buff.getString());

      } else if (mc > 0) {
        s = "Messages " + BeeUtils.bracket(mc);
      } else {
        s = BeeConst.EMPTY;
      }
      logger.info(">", rid, TimeUtils.elapsedSeconds(start), ctp, resp.getContentType(),
          cnt, cc, mc, s.length());
    }

    try {
      PrintWriter out = resp.getWriter();
      out.print(s);
      out.flush();
    } catch (IOException ex) {
      logger.error(ex);
    }
  }

  private static void logout(HttpServletRequest req, HttpSession session) {
    try {
      req.logout();
      session.invalidate();
      logger.info("logout successful");
    } catch (ServletException ex) {
      logger.warning("logout", ex);
    }
  }
}
