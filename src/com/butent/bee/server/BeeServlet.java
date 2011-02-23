package com.butent.bee.server;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.BeeResource;
import com.butent.bee.shared.BeeService;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.LogUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@SuppressWarnings("serial")
@WebServlet(name = "BeeServlet", urlPatterns = {"/bee/bee"})
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

    ResponseBuffer buff = new ResponseBuffer(sep);
    String sid = null;

    if (isAuthorized(req, reqInfo, resp, buff)) {
      sid = req.getSession(false).getId();

      if (!BeeUtils.same(svc, BeeService.SERVICE_LOGIN)) {
        dispatcher.doService(svc, dsn, reqInfo, buff);
      }
    } else {
      buff.addWarning("Not logged in");
    }

    int respLen = buff.getSize();
    int mc = buff.getMessageCount();
    int pc = buff.getPartCount();

    int cnt = buff.getCount();
    int cc = buff.getColumnCount();

    ContentType ctp = buff.getContentType();
    if (ctp == null) {
      ctp = (cc > 0) ? ContentType.TABLE : CommUtils.defaultResponseContentType;
    }

    resp.setHeader(BeeService.RPC_VAR_SID, sid);
    resp.setHeader(BeeService.RPC_VAR_QID, rid);

    if (!BeeUtils.isEmpty(sep) || !buff.isDefaultSeparator()) {
      resp.setHeader(BeeService.RPC_VAR_SEP, buff.getHexSeparator());
    }

    if (cnt > 0) {
      resp.setIntHeader(BeeService.RPC_VAR_CNT, cnt);
    }
    if (cc > 0) {
      resp.setIntHeader(BeeService.RPC_VAR_COLS, cc);
    }

    if (mc > 0) {
      resp.setIntHeader(BeeService.RPC_VAR_MSG_CNT, mc);
      for (int i = 0; i < mc; i++) {
        resp.setHeader(CommUtils.rpcMessageName(i), buff.getMessage(i).serialize());
      }
    }

    resp.setHeader(BeeService.RPC_VAR_CTP, ctp.transform());

    resp.setHeader("Cache-Control", "no-cache");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");

    String mt = BeeUtils.ifString(buff.getMediaType(), CommUtils.getMediaType(ctp));
    if (!BeeUtils.isEmpty(mt)) {
      resp.setContentType(mt);
    }

    String ce = BeeUtils.ifString(buff.getCharacterEncoding(),
          CommUtils.getCharacterEncoding(ctp));
    if (!BeeUtils.isEmpty(ce)) {
      resp.setCharacterEncoding(ce);
    }

    String s;
    if (respLen > 0) {
      s = CommUtils.prepareContent(ctp, buff.getString());

    } else if (pc > 0) {
      resp.setIntHeader(BeeService.RPC_VAR_PART_CNT, pc);
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

    try {
      PrintWriter out = resp.getWriter();
      out.print(s);
      out.flush();
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
    }
  }

  private boolean isAuthorized(HttpServletRequest req, RequestInfo reqInfo,
      HttpServletResponse resp, ResponseBuffer buff) {

    HttpSession session = req.getSession(false);
    String svc = reqInfo.getService();
    boolean loggedIn = !BeeUtils.isEmpty(session);
    boolean doLogout = loggedIn
        && (BeeUtils.inListSame(svc, BeeService.SERVICE_LOGIN, BeeService.SERVICE_LOGOUT)
        || !BeeUtils.same(session.getId(), reqInfo.getParameter(BeeService.RPC_VAR_SID)));

    if (doLogout) {
      try {
        req.logout();
        session.invalidate();
        loggedIn = false;
        buff.addWarning("Logout successful");

      } catch (ServletException e) {
        buff.addSevere(e.getMessage());
      }
    }

    if (!loggedIn && !BeeUtils.same(svc, BeeService.SERVICE_LOGOUT)) {
      String usr = BeeUtils.ifString(reqInfo.getParameter(BeeService.VAR_LOGIN),
          Config.getProperty("DefaultUser"));
      String pwd = BeeUtils.ifString(reqInfo.getParameter(BeeService.VAR_PASSWORD),
          Config.getProperty("DefaultPassword"));

      if (BeeUtils.allNotEmpty(usr, pwd)) {
        try {
          req.login(usr, pwd);
          usr = dispatcher.doLogin(reqInfo.getDsn());

          if (BeeUtils.isEmpty(usr)) {
            buff.addSevere("User not authorized:", req.getRemoteUser());
            req.logout();
            session = req.getSession(false);

            if (!BeeUtils.isEmpty(session)) {
              session.invalidate();
            }
          } else {
            session = req.getSession(true);
            session.setAttribute(BeeService.VAR_LOGIN, req.getRemoteUser());

            resp.setHeader(BeeService.VAR_USER_SIGN, Codec.encodeBase64(usr));
            loggedIn = true;
            buff.addWarning("Login successful");
          }
        } catch (ServletException e) {
          buff.addSevere(e.getMessage());
        }
      }
    }
    return loggedIn;
  }
}
