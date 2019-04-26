package com.butent.bee.server.utils;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;

import java.util.Date;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public final class SmsUtils {

  public static ResponseObject sendSmppMessage(String msg, String to) {
    ParamHolderBean prm = Invocation.locateRemoteBean(ParamHolderBean.class);

    String address = prm.getText(PRM_SMS_REQUEST_SERVICE_ADDRESS);
    String userName = prm.getText(PRM_SMS_REQUEST_SERVICE_USER_NAME);
    String password = prm.getText(PRM_SMS_REQUEST_SERVICE_PASSWORD);
    String source = prm.getText(PRM_SMS_REQUEST_SERVICE_FROM);

    if (BeeUtils.isEmpty(address)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_ADDRESS + " is empty");
    }
    if (BeeUtils.isEmpty(userName)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_USER_NAME + " is empty");
    }
    if (BeeUtils.isEmpty(password)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_PASSWORD + " is empty");
    }
    String[] split = address.split(":", 2);
    address = split[0];
    int port = BeeUtils.toInt(ArrayUtils.getQuietly(split, 1));

    SMPPSession session = new SMPPSession();

    try {
      session.connectAndBind(address, port,
          new BindParameter(BindType.BIND_TX, userName, password, "cp", TypeOfNumber.UNKNOWN,
              NumberingPlanIndicator.UNKNOWN, null));

      session.submitShortMessage("CMT",
          TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, source,
          TypeOfNumber.INTERNATIONAL, NumberingPlanIndicator.UNKNOWN, to,
          new ESMClass(), (byte) 0, (byte) 1, new AbsoluteTimeFormatter().format(new Date()), null,
          new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0,
          new GeneralDataCoding(Alphabet.ALPHA_8_BIT, MessageClass.CLASS1, false), (byte) 0,
          msg.getBytes());

    } catch (Exception e) {
      return ResponseObject.error(e);

    } finally {
      if (session != null) {
        session.unbindAndClose();
      }
    }
    return ResponseObject.emptyResponse();
  }

  public static ResponseObject sendRestMessage(String msg, String to) {
    ParamHolderBean prm = Invocation.locateRemoteBean(ParamHolderBean.class);

    String address = prm.getText(PRM_SMS_REQUEST_SERVICE_ADDRESS);
    String userName = prm.getText(PRM_SMS_REQUEST_SERVICE_USER_NAME);
    String password = prm.getText(PRM_SMS_REQUEST_SERVICE_PASSWORD);
    String source = prm.getText(PRM_SMS_REQUEST_SERVICE_FROM);

    if (BeeUtils.isEmpty(address)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_ADDRESS + " is empty");
    }
    if (BeeUtils.isEmpty(userName)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_USER_NAME + " is empty");
    }
    if (BeeUtils.isEmpty(password)) {
      return ResponseObject.error(PRM_SMS_REQUEST_SERVICE_PASSWORD + " is empty");
    }
    Client client = ClientBuilder.newClient();
    UriBuilder uriBuilder = UriBuilder.fromPath(address);

    uriBuilder.queryParam("username", userName);
    uriBuilder.queryParam("password", password);
    uriBuilder.queryParam("message", msg);
    uriBuilder.queryParam("from", source);
    uriBuilder.queryParam("to", to);
    WebTarget webtarget = client.target(uriBuilder);

    javax.ws.rs.client.Invocation.Builder builder = webtarget.request(MediaType.TEXT_PLAIN_TYPE)
        .acceptEncoding(BeeConst.CHARSET_UTF8);

    Response response = builder.get();
    String smsResponseMessage = response.readEntity(String.class);

    if (response.getStatus() == 200 && !BeeUtils.isEmpty(smsResponseMessage)
        && BeeUtils.containsSame(smsResponseMessage, "OK")) {
      return ResponseObject.emptyResponse();
    }
    return ResponseObject.error(smsResponseMessage);
  }
}
