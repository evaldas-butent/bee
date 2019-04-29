package com.butent.bee.server.utils;

import static com.butent.bee.shared.modules.service.ServiceConstants.*;

import com.butent.bee.server.Invocation;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;

import org.jsmpp.bean.AlertNotification;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.DataSm;
import org.jsmpp.bean.DeliverSm;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GSMSpecificFeature;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.MessageClass;
import org.jsmpp.bean.MessageMode;
import org.jsmpp.bean.MessageType;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.DataSmResult;
import org.jsmpp.session.MessageReceiverListener;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.session.Session;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public final class SmsUtils {

  private static BeeLogger logger = LogUtils.getLogger(SmsUtils.class);

  private static final char[] BASIC_CHARS = {
      // Basic Character Set
      '@', '£', '$', '¥', 'è', 'é', 'ù', 'ì', 'ò', 'Ç', '\n', 'Ø', 'ø', '\r', 'Å', 'å',
      'Δ', '_', 'Φ', 'Γ', 'Λ', 'Ω', 'Π', 'Ψ', 'Σ', 'Θ', 'Ξ', 'Æ', 'æ', 'ß', 'É',
      ' ', '!', '"', '#', '¤', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', '>', '?',
      '¡', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
      'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ñ', 'Ü', '§',
      '¿', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
      'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ñ', 'ü', 'à',
      // Basic Character Set Extension
      '\f', '^', '{', '}', '\\', '[', '~', ']', '|', '€'
  };

  private static final int MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2 = 134;
  private static final int MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2 = 70;
  private static final int MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT = 154;
  private static final int MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT = 160;

  public static ResponseObject sendSmppMessage(String messageBody, String destinationMsisdn) {
    ParamHolderBean prm = Invocation.locateRemoteBean(ParamHolderBean.class);

    String address = prm.getText(PRM_SMS_REQUEST_SERVICE_ADDRESS);
    String userName = prm.getText(PRM_SMS_REQUEST_SERVICE_USER_NAME);
    String password = prm.getText(PRM_SMS_REQUEST_SERVICE_PASSWORD);
    String sourceMsisdn = prm.getText(PRM_SMS_REQUEST_SERVICE_FROM);

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
    session.setMessageReceiverListener(new MessageReceiverListener() {
      @Override
      public DataSmResult onAcceptDataSm(DataSm dataSm, Session source) {
        return null;
      }

      @Override
      public void onAcceptDeliverSm(DeliverSm deliverSm) {
      }

      @Override
      public void onAcceptAlertNotification(AlertNotification alertNotification) {
      }
    });
    MessageClass messageClass = MessageClass.CLASS1;

    try {
      session.connectAndBind(address, port,
          new BindParameter(BindType.BIND_TRX, userName, password, "cp", TypeOfNumber.UNKNOWN,
              NumberingPlanIndicator.UNKNOWN, null));

      // configure variables according to if message contains non-basic characters
      Alphabet alphabet;
      int maximumSingleMessageSize;
      int maximumMultipartMessageSegmentSize;
      byte[] byteSingleMessage;

      if (isBasicEncodeable(messageBody)) {
        byteSingleMessage = messageBody.getBytes();
        alphabet = Alphabet.ALPHA_DEFAULT;
        maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_7BIT;
        maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_7BIT;
      } else {
        byteSingleMessage = messageBody.getBytes(StandardCharsets.UTF_16BE);
        alphabet = Alphabet.ALPHA_UCS2;
        maximumSingleMessageSize = MAX_SINGLE_MSG_SEGMENT_SIZE_UCS2;
        maximumMultipartMessageSegmentSize = MAX_MULTIPART_MSG_SEGMENT_SIZE_UCS2;
      }

      // check if message needs splitting and set required sending parameters
      byte[][] byteMessagesArray;
      ESMClass esmClass;

      if (messageBody.length() > maximumSingleMessageSize) {
        // split message according to the maximum length of a segment
        byteMessagesArray = splitUnicodeMessage(byteSingleMessage,
            maximumMultipartMessageSegmentSize);
        // set UDHI so PDU will decode the header
        esmClass = new ESMClass(MessageMode.DEFAULT, MessageType.DEFAULT, GSMSpecificFeature.UDHI);
      } else {
        byteMessagesArray = new byte[][] {byteSingleMessage};
        esmClass = new ESMClass();
      }

      logger.info("Sending message to", destinationMsisdn, ":", messageBody);
      logger.info("Message is", messageBody.length(), "characters and will be sent as",
          byteMessagesArray.length, "messages with params:", alphabet, messageClass);

      // submit all messages
      for (byte[] bytes : byteMessagesArray) {
        String messageId = session.submitShortMessage("CMT", TypeOfNumber.UNKNOWN,
            NumberingPlanIndicator.UNKNOWN, sourceMsisdn, TypeOfNumber.UNKNOWN,
            NumberingPlanIndicator.UNKNOWN, destinationMsisdn, esmClass, (byte) 0, (byte) 1, null,
            null, new RegisteredDelivery(SMSCDeliveryReceipt.SUCCESS_FAILURE), (byte) 0,
            new GeneralDataCoding(alphabet, messageClass, false), (byte) 0, bytes);

        logger.info("Message submitted, message_id is", messageId);
      }
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

  public static boolean isBasicEncodeable(String javaString) {
    char[] javaChars = javaString.toCharArray();
    for (char c : javaChars) {
      if (isBasicEncodeable(c)) {
        continue;
      }
      return false;
    }
    return true;
  }

  public static boolean isBasicEncodeable(char javaChar) {
    for (char basicChar : BASIC_CHARS) {
      if (basicChar == javaChar) {
        return true;
      }
    }
    return false;
  }

  private static byte[][] splitUnicodeMessage(byte[] aMessage,
      Integer maximumMultipartMessageSegmentSize) {

    final byte UDHIE_HEADER_LENGTH = 0x05;
    final byte UDHIE_IDENTIFIER_SAR = 0x00;
    final byte UDHIE_SAR_LENGTH = 0x03;

    // determine how many messages have to be sent
    int numberOfSegments = aMessage.length / maximumMultipartMessageSegmentSize;
    int messageLength = aMessage.length;
    if (numberOfSegments > 255) {
      numberOfSegments = 255;
      messageLength = numberOfSegments * maximumMultipartMessageSegmentSize;
    }
    if ((messageLength % maximumMultipartMessageSegmentSize) > 0) {
      numberOfSegments++;
    }

    // prepare array for all of the msg segments
    byte[][] segments = new byte[numberOfSegments][];

    int lengthOfData;

    // generate new reference number
    byte[] referenceNumber = new byte[1];
    new Random().nextBytes(referenceNumber);

    // split the message adding required headers
    for (int i = 0; i < numberOfSegments; i++) {
      if (numberOfSegments - i == 1) {
        lengthOfData = messageLength - i * maximumMultipartMessageSegmentSize;
      } else {
        lengthOfData = maximumMultipartMessageSegmentSize;
      }

      // new array to store the header
      segments[i] = new byte[6 + lengthOfData];

      // UDH header
      // doesn't include itself, its header length
      segments[i][0] = UDHIE_HEADER_LENGTH;
      // SAR identifier
      segments[i][1] = UDHIE_IDENTIFIER_SAR;
      // SAR length
      segments[i][2] = UDHIE_SAR_LENGTH;
      // reference number (same for all messages)
      segments[i][3] = referenceNumber[0];
      // total number of segments
      segments[i][4] = (byte) numberOfSegments;
      // segment number
      segments[i][5] = (byte) (i + 1);

      // copy the data into the array
      System.arraycopy(aMessage, (i * maximumMultipartMessageSegmentSize), segments[i], 6,
          lengthOfData);

    }
    return segments;
  }
}
