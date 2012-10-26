package com.butent.bee.server.modules.mail;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.modules.mail.proxy.MailProxy;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
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
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import org.jsoup.Jsoup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBContext;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
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

  private static final String DEFAULT_MAIL_FOLDER = "INBOX";

  @EJB
  MailProxy proxy;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;
  @Resource
  EJBContext ctx;

  public ResponseObject checkMail(Long addressId) {
    Map<String, String> data = null;

    if (addressId != null) {
      data = qs.getRow(new SqlSelect()
          .addFields(TBL_ACCOUNTS, "Login", "Password", "StoreType", "StoreServer", "StorePort")
          .addFields(TBL_ADDRESSES, "Email")
          .addFrom(TBL_ACCOUNTS)
          .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_ACCOUNTS, COL_ADDRESS))
          .setWhere(SqlUtils.equal(TBL_ACCOUNTS, COL_ADDRESS, addressId)));
    }
    if (data == null) {
      return ResponseObject.error("Unknown user account:", addressId);
    }
    int c = 0;

    try {
      c = checkMail(NameUtils.getEnumByName(Protocol.class,
          data.get("StoreType")), data.get("StoreServer"),
          BeeUtils.toIntOrNull(data.get("StorePort")),
          BeeUtils.notEmpty(data.get("Login"), data.get("Email")),
          data.get("Password"), addressId);

    } catch (MessagingException e) {
      ctx.setRollbackOnly();
      logger.error(e);
      return ResponseObject.error(e);
    }
    return ResponseObject.response(c);
  }

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
      response = getMessage(BeeUtils.toLongOrNull(reqInfo.getParameter("Message")),
          BeeUtils.toLongOrNull(reqInfo.getParameter("AccountAddress")));

    } else if (BeeUtils.same(svc, SVC_GET_ACCOUNTS)) {
      response = getAccounts(BeeUtils.toLongOrNull(reqInfo.getParameter("User")));

    } else if (BeeUtils.same(svc, SVC_CHECK_MAIL)) {
      response = checkMail(BeeUtils.toLongOrNull(reqInfo.getParameter("AccountAddress")));

    } else if (BeeUtils.same(svc, SVC_REMOVE_MESSAGES)) {
      response = removeMessages(BeeUtils.toLongOrNull(reqInfo.getParameter("Sender")),
          BeeUtils.toLongOrNull(reqInfo.getParameter("Recipient")),
          Codec.beeDeserializeCollection(reqInfo.getParameter("Messages")),
          BeeUtils.toBoolean(reqInfo.getParameter("Purge")));

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
    Session session = Session.getInstance(new Properties(), null);

    if (session == null) {
      String msg = "Mail session not available";
      logger.severe(msg);
      response = ResponseObject.error(msg);
    } else {
      MimeMessage message = new MimeMessage(session);

      try {
        message.setRecipient(RecipientType.TO, new InternetAddress(to));
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

  public void storeMail(String mail, String recipient) {
    Assert.notNull(mail);
    logger.debug("GOT", BeeUtils.isEmpty(recipient) ? Protocol.SMTP : Protocol.POP3, "mail:");
    logger.debug(mail);

    MimeMessage message = null;

    try {
      message = new MimeMessage(null,
          new ByteArrayInputStream(mail.getBytes(BeeConst.CHARSET_UTF8)));
    } catch (MessagingException e) {
      throw new BeeRuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new BeeRuntimeException(e);
    }
    Long addressId = null;

    if (!BeeUtils.isEmpty(recipient)) {
      InternetAddress adr;

      try {
        adr = new InternetAddress(recipient, false);
        adr.validate();
      } catch (AddressException ex) {
        adr = null;
      }
      try {
        if (adr == null) {
          addressId = qs.getLong(new SqlSelect()
              .addFields(MailConstants.TBL_ACCOUNTS, COL_ADDRESS)
              .addFrom(TBL_ACCOUNTS)
              .setWhere(SqlUtils.equal(TBL_ACCOUNTS, "Login", recipient)));
        } else {
          addressId = storeAddress(adr);
        }
      } catch (Exception ex) {
        addressId = null;
      }
    }
    try {
      storeMail(message, addressId);
    } catch (MessagingException e) {
      throw new BeeRuntimeException(e);
    }
  }

  private int checkMail(Protocol type, String host, Integer port, String user, String password,
      Long addressId) throws MessagingException {
    Assert.notNull(type);
    Assert.notEmpty(host);
    Assert.notEmpty(user);
    Assert.notEmpty(password);

    Store store = null;
    Folder folder = null;
    int c = 0;
    Session session = Session.getInstance(new Properties(), null);

    try {
      store = session.getStore(type.name().toLowerCase());
      store.connect(host, (BeeUtils.isNonNegative(port) ? port : -1), user, password);
      folder = store.getDefaultFolder();

      if (folder == null) {
        throw new MessagingException("No default folder");
      }
      folder = folder.getFolder(DEFAULT_MAIL_FOLDER);

      if (folder == null) {
        throw new MessagingException(BeeUtils.joinWords("Folder not found:", DEFAULT_MAIL_FOLDER));
      }
      folder.open(Folder.READ_ONLY);
      int totalMessages = folder.getMessageCount();

      if (totalMessages > 0) {
        Message[] msgs = folder.getMessages();

        for (int i = 0; i < totalMessages; i++) {
          if (storeMail(msgs[i], addressId)) {
            c++;
          }
        }
      }
    } finally {
      if (folder != null) {
        folder.close(false);
      }
      if (store != null) {
        store.close();
      }
    }
    return c;
  }

  private ResponseObject getAccounts(Long user) {
    Assert.notNull(user);

    return ResponseObject.response(qs.getData(new SqlSelect()
        .addFields(TBL_ACCOUNTS, "Description", COL_ADDRESS, "Main")
        .addFrom(TBL_ACCOUNTS)
        .setWhere(SqlUtils.equal(TBL_ACCOUNTS, "User", user))
        .addOrder(TBL_ACCOUNTS, "Description")));
  }

  private ResponseObject getMessage(Long id, Long addressId) {
    Assert.notNull(id);
    IsCondition wh = SqlUtils.equal(TBL_RECIPIENTS, COL_MESSAGE, id);

    if (addressId != null) {
      if (addressId != 0) {
        qs.updateData(new SqlUpdate(TBL_RECIPIENTS)
            .addConstant("Unread", null)
            .setWhere(SqlUtils.and(wh, SqlUtils.equal(TBL_RECIPIENTS, COL_ADDRESS, addressId))));
      }
      wh = SqlUtils.and(wh, SqlUtils.notEqual(TBL_RECIPIENTS, "Type", AddressType.BCC.name()));
    }
    Map<String, SimpleRowSet> packet = Maps.newHashMap();

    packet.put(TBL_RECIPIENTS, qs.getData(new SqlSelect()
        .addFields(TBL_RECIPIENTS, "Type")
        .addFields(TBL_ADDRESSES, "Label", "Email")
        .addFrom(TBL_RECIPIENTS)
        .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_RECIPIENTS, COL_ADDRESS))
        .setWhere(wh)
        .addOrderDesc(TBL_RECIPIENTS, "Type")));

    packet.put(TBL_PARTS, qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, "Content", "HtmlContent")
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equal(TBL_PARTS, COL_MESSAGE, id))));

    packet.put(TBL_ATTACHMENTS, qs.getData(new SqlSelect()
        .addFields(TBL_ATTACHMENTS, "FileName")
        .addFields(CommonsConstants.TBL_FILES, "Hash", "Name", "Size", "Mime")
        .addFrom(TBL_ATTACHMENTS)
        .addFromInner(CommonsConstants.TBL_FILES,
            sys.joinTables(CommonsConstants.TBL_FILES, TBL_ATTACHMENTS, COL_FILE))
        .setWhere(SqlUtils.equal(TBL_ATTACHMENTS, COL_MESSAGE, id))));

    return ResponseObject.response(packet);
  }

  private ResponseObject removeMessages(Long sender, Long recipient, String[] messages,
      boolean purge) {
    Assert.state(!ArrayUtils.isEmpty(messages), "Empty message list");

    SqlUpdate su = null;
    HasConditions wh = SqlUtils.or();

    if (sender != null) {
      String idName = sys.getIdName(TBL_MESSAGES);

      for (String id : messages) {
        wh.add(SqlUtils.equal(TBL_MESSAGES, idName, BeeUtils.toLong(id)));
      }
      su = new SqlUpdate(TBL_MESSAGES)
          .setWhere(SqlUtils.and(wh, SqlUtils.equal(TBL_MESSAGES, "Sender", sender)));

    } else if (recipient != null) {
      for (String id : messages) {
        wh.add(SqlUtils.equal(TBL_RECIPIENTS, COL_MESSAGE, BeeUtils.toLong(id)));
      }
      su = new SqlUpdate(TBL_RECIPIENTS)
          .setWhere(SqlUtils.and(wh, SqlUtils.equal(TBL_RECIPIENTS, COL_ADDRESS, recipient)));
    } else {
      Assert.untouchable("Unknown recipient");
    }
    su.addConstant("Status", purge ? STATUS_PURGED : STATUS_DELETED);

    return qs.updateDataWithResponse(su);
  }

  private Long storeAddress(Address address) throws AddressException {
    Assert.notNull(address);
    String email;
    String label = null;

    if (address instanceof InternetAddress) {
      ((InternetAddress) address).validate();

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

  private boolean storeMail(Message message, Long addressId) throws MessagingException {
    boolean stored = false;

    if (message == null) {
      return stored;
    }
    MailEnvelope envelope = null;
    envelope = new MailEnvelope(message);
    boolean userExists = (addressId == null);

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
          .addConstant("Status", STATUS_NEUTRAL)
          .addConstant("Subject", envelope.getSubject()));

      for (Entry<AddressType, Address> entry : envelope.getRecipients().entries()) {
        Long adr = storeAddress(entry.getValue());
        userExists = userExists || (adr == addressId);

        qs.insertData(new SqlInsert(TBL_RECIPIENTS)
            .addConstant(COL_MESSAGE, id)
            .addConstant(COL_ADDRESS, adr)
            .addConstant("Type", entry.getKey().name())
            .addConstant("Status", STATUS_NEUTRAL)
            .addConstant("Unread", true));
      }
      try {
        storePart(id, message, null);
      } catch (IOException e) {
        throw new MessagingException(e.toString());
      }
      stored = true;

    } else if (!userExists) {
      userExists = qs.sqlExists(TBL_RECIPIENTS,
          SqlUtils.and(SqlUtils.equal(TBL_RECIPIENTS, COL_MESSAGE, id),
              SqlUtils.equal(TBL_RECIPIENTS, COL_ADDRESS, addressId)));
    }
    if (!userExists) {
      qs.insertData(new SqlInsert(TBL_RECIPIENTS)
          .addConstant(COL_MESSAGE, id)
          .addConstant(COL_ADDRESS, addressId)
          .addConstant("Type", AddressType.BCC.name())
          .addConstant("Status", STATUS_NEUTRAL)
          .addConstant("Unread", true));
    }
    return stored;
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
