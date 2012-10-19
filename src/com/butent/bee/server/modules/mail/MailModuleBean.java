package com.butent.bee.server.modules.mail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.commons.CommonsConstants.TBL_FILES;
import static com.butent.bee.shared.modules.mail.MailConstants.COL_ADDRESS;
import static com.butent.bee.shared.modules.mail.MailConstants.COL_FILE;
import static com.butent.bee.shared.modules.mail.MailConstants.COL_MESSAGE;
import static com.butent.bee.shared.modules.mail.MailConstants.MAIL_METHOD;
import static com.butent.bee.shared.modules.mail.MailConstants.MAIL_MODULE;
import static com.butent.bee.shared.modules.mail.MailConstants.SVC_GET_MESSAGE;
import static com.butent.bee.shared.modules.mail.MailConstants.SVC_RESTART_PROXY;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_ADDRESSES;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_ATTACHMENTS;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_HEADERS;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_MESSAGES;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_PARTS;
import static com.butent.bee.shared.modules.mail.MailConstants.TBL_RECIPIENTS;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.modules.mail.proxy.MailProxy;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.BeeUtils;

import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

@Stateless
@LocalBean
public class MailModuleBean implements BeeModule {

  private static final BeeLogger logger = LogUtils.getLogger(MailModuleBean.class);

  @EJB
  MailProxy proxy;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;

  private final Session session = null;

  @Override
  public Collection<String> dependsOn() {
    return null;
  }

  @Override
  public List<SearchResult> doSearch(String query) {
    return null;
  }

  @Override
  public ResponseObject doService(RequestInfo reqInfo) {
    ResponseObject response = null;
    String svc = reqInfo.getParameter(MAIL_METHOD);

    if (BeeUtils.same(svc, SVC_RESTART_PROXY)) {
      response = proxy.initServer();
      response.log(logger);

    } else if (BeeUtils.same(svc, SVC_GET_MESSAGE)) {
      response = getMessage(BeeUtils.toLong(reqInfo.getParameter("id")));

    } else {
      String msg = BeeUtils.joinWords("Mail service not recognized:", svc);
      logger.warning(msg);
      response = ResponseObject.error(msg);
    }
    return response;
  }

  @Override
  public Collection<BeeParameter> getDefaultParameters() {
    List<BeeParameter> params = Lists.newArrayList(
        new BeeParameter(MAIL_MODULE,
            "POP3Server", ParameterType.TEXT, "POP3 server name", false, null),
        new BeeParameter(MAIL_MODULE,
            "POP3ServerPort", ParameterType.NUMBER, "POP3 server port number", false, null),
        new BeeParameter(MAIL_MODULE,
            "POP3BindPort", ParameterType.NUMBER, "POP3 proxy port number to listen on", false,
            null),
        new BeeParameter(MAIL_MODULE,
            "SMTPServer", ParameterType.TEXT, "SMTP server name", false, null),
        new BeeParameter(MAIL_MODULE,
            "SMTPServerPort", ParameterType.NUMBER, "SMTP server port number", false, null),
        new BeeParameter(MAIL_MODULE,
            "SMTPBindPort", ParameterType.NUMBER, "SMTP proxy port number to listen on", false,
            null));

    return params;
  }

  @Override
  public String getName() {
    return MAIL_MODULE;
  }

  @Override
  public String getResourcePath() {
    return getName();
  }

  @Override
  public void init() {
    proxy.initServer();
  }

  public ResponseObject sendMail(String to, String subject, String body) {
    ResponseObject response;

    if (session == null) {
      String msg = "Mail session not available";
      logger.severe(msg);
      response = ResponseObject.error(msg);
    } else {
      MimeMessage message = new MimeMessage(session);

      try {
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, BeeConst.CHARSET_UTF8);
        message.setText(body, BeeConst.CHARSET_UTF8);

        Transport.send(message);
        response = ResponseObject.response("Mail sent");

      } catch (MessagingException ex) {
        response = ResponseObject.error(ex);
      }
    }
    return response;
  }

  public void storeMail(String mail, String pop3User) {
    Assert.notNull(mail);
    logger.debug("GOT", BeeUtils.isEmpty(pop3User) ? Protocol.SMTP : Protocol.POP3, "mail:");
    logger.debug(mail);

    Long pop3Address = null;

    if (!BeeUtils.isEmpty(pop3User)) {
      try {
        pop3Address = storeAddress(new InternetAddress(pop3User, false));
      } catch (AddressException ex) {
        logger.error(ex);
        return;
      }
    }
    MimeMessage message = null;

    try {
      message = new MimeMessage(null,
          new ByteArrayInputStream(mail.getBytes(BeeConst.CHARSET_UTF8)));
    } catch (UnsupportedEncodingException e) {
      logger.error(e);
    } catch (MessagingException e) {
      logger.error(e);
    }
    if (message == null) {
      return;
    }
    MailEnvelope envelope = null;

    try {
      envelope = new MailEnvelope(message);
    } catch (MessagingException e) {
      logger.error(e);
      return;
    }
    boolean userExists = (pop3Address == null);

    Long id = qs.getLong(new SqlSelect()
        .addFields(TBL_MESSAGES, sys.getIdName(TBL_MESSAGES))
        .addFrom(TBL_MESSAGES)
        .setWhere(SqlUtils.equal(TBL_MESSAGES, "UniqueId", envelope.getMessageId())));

    if (id == null) {
      Long sender = storeAddress(envelope.getSender());
      Long header = qs.insertData(new SqlInsert(TBL_HEADERS)
          .addConstant("Header", envelope.getHeader()));

      id = qs.insertData(new SqlInsert(TBL_MESSAGES)
          .addConstant("Header", header)
          .addConstant("UniqueId", envelope.getMessageId())
          .addConstant("Date", envelope.getDate())
          .addConstant("Sender", sender)
          .addConstant("Subject", envelope.getSubject()));

      for (Entry<RecipientType, Address> entry : envelope.getRecipients().entries()) {
        Long adr = storeAddress(entry.getValue());
        userExists = userExists || (adr == pop3Address);

        qs.insertData(new SqlInsert(TBL_RECIPIENTS)
            .addConstant(COL_MESSAGE, id)
            .addConstant(COL_ADDRESS, adr)
            .addConstant("Type", entry.getKey().toString()));
      }
      try {
        storePart(id, message, null);
      } catch (MessagingException e) {
        logger.error(e);
        return;
      } catch (IOException e) {
        logger.error(e);
        return;
      }
    } else if (!userExists) {
      userExists = qs.sqlExists(TBL_RECIPIENTS,
          SqlUtils.and(SqlUtils.equal(TBL_RECIPIENTS, COL_MESSAGE, id),
              SqlUtils.equal(TBL_RECIPIENTS, COL_ADDRESS, pop3Address)));
    }
    if (!userExists) {
      qs.insertData(new SqlInsert(TBL_RECIPIENTS)
          .addConstant(COL_MESSAGE, id)
          .addConstant(COL_ADDRESS, pop3Address)
          .addConstant("Type", RecipientType.BCC.toString()));
    }
  }

  private ResponseObject getMessage(Long id) {
    Assert.notNull(id);
    Map<String, SimpleRowSet> packet = Maps.newHashMap();

    packet.put(TBL_PARTS, qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, "Content", "HtmlContent")
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equal(TBL_PARTS, COL_MESSAGE, id))));

    packet.put(TBL_RECIPIENTS, qs.getData(new SqlSelect()
        .addFields(TBL_RECIPIENTS, "Type")
        .addFields(TBL_ADDRESSES, "Label", "Email")
        .addFrom(TBL_RECIPIENTS)
        .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_RECIPIENTS, COL_ADDRESS))
        .setWhere(SqlUtils.equal(TBL_RECIPIENTS, COL_MESSAGE, id))
        .addOrderDesc(TBL_RECIPIENTS, "Type")));

    packet.put(TBL_ATTACHMENTS, qs.getData(new SqlSelect()
        .addFields(TBL_ATTACHMENTS, "FileName")
        .addFields(TBL_FILES, "Hash", "Name", "Size", "Mime")
        .addFrom(TBL_ATTACHMENTS)
        .addFromInner(TBL_FILES, sys.joinTables(TBL_FILES, TBL_ATTACHMENTS, COL_FILE))
        .setWhere(SqlUtils.equal(TBL_ATTACHMENTS, COL_MESSAGE, id))));

    return ResponseObject.response(packet);
  }

  private Long storeAddress(Address address) {
    Assert.notNull(address);
    String email;
    String label = null;

    if (address instanceof InternetAddress) {
      try {
        ((InternetAddress) address).validate();
      } catch (AddressException e) {
        throw new BeeRuntimeException(e);
      }
      label = ((InternetAddress) address).getPersonal();
      email = BeeUtils.normalize(((InternetAddress) address).getAddress());
    } else {
      email = BeeUtils.normalize(address.toString());
    }
    Assert.notEmpty(email);

    Long id = qs.getLong(new SqlSelect()
        .addFields(TBL_ADDRESSES, sys.getIdName(TBL_ADDRESSES))
        .addFrom(TBL_ADDRESSES)
        .setWhere(SqlUtils.equal(TBL_ADDRESSES, "Email", email)));

    if (id == null) {
      id = qs.insertData(new SqlInsert(TBL_ADDRESSES)
          .addConstant("Email", email)
          .addConstant("Label", label));
    }
    return id;
  }

  private void storePart(Long messageId, Part part, Pair<String, String> alternative)
      throws MessagingException, IOException {

    if (part.isMimeType("multipart/*")) {
      Multipart multiPart = (Multipart) part.getContent();
      boolean hasAlternative = (alternative == null && part.isMimeType("multipart/alternative"));

      if (hasAlternative) {
        alternative = Pair.of(null, null);
      }
      for (int i = 0; i < multiPart.getCount(); i++) {
        storePart(messageId, multiPart.getBodyPart(i), alternative);
      }
      if (hasAlternative) {
        qs.insertData(new SqlInsert(TBL_PARTS)
            .addConstant(COL_MESSAGE, messageId)
            .addConstant("ContentType", alternative.getB() != null ? "text/html" : "text/plain")
            .addConstant("Content",
                alternative.getA() != null ? alternative.getA() : stripHtml(alternative.getB()))
            .addConstant("HtmlContent", alternative.getB()));
      }
    } else if (part.isMimeType("message/*")) {
      storePart(messageId, (Message) part.getContent(), alternative);
    } else {
      String contentType = part.getContentType();

      try {
        contentType = new ContentType(contentType).getBaseType();
      } catch (ParseException e) {
        logger.warning(e);
      }
      String disposition = part.getDisposition();
      String fileName = part.getFileName();

      if (!BeeUtils.isEmpty(fileName)) {
        try {
          fileName = MimeUtility.decodeText(part.getFileName());
        } catch (UnsupportedEncodingException ex) {
        }
      }
      if (!(part.isMimeType("text/*"))
          || BeeUtils.same(disposition, Part.ATTACHMENT)
          || !BeeUtils.isEmpty(fileName)
          || (alternative != null
              && !part.isMimeType("text/plain") && !part.isMimeType("text/html"))) {

        Long fileId = fs.storeFile(part.getInputStream(), fileName, contentType);

        qs.insertData(new SqlInsert(TBL_ATTACHMENTS)
            .addConstant(COL_MESSAGE, messageId)
            .addConstant(COL_FILE, fileId)
            .addConstant("FileName", fileName));

      } else if (alternative != null) {
        if (part.isMimeType("text/plain") && alternative.getA() == null) {
          alternative.setA((String) part.getContent());
        } else if (part.isMimeType("text/html") && alternative.getB() == null) {
          alternative.setB((String) part.getContent());
        }
      } else {
        String htmlContent = null;
        String content = (String) part.getContent();

        if (part.isMimeType("text/html")) {
          htmlContent = content;
          content = stripHtml(htmlContent);
        }
        qs.insertData(new SqlInsert(TBL_PARTS)
            .addConstant(COL_MESSAGE, messageId)
            .addConstant("ContentType", contentType)
            .addConstant("Content", content)
            .addConstant("HtmlContent", htmlContent));
      }
    }
  }

  private String stripHtml(String content) {
    return Jsoup.parse(content).text();
  }
}
