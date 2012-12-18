package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.modules.mail.proxy.MailProxy;
import com.butent.bee.server.sql.HasConditions;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SearchResult;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.BeeParameter;
import com.butent.bee.shared.modules.ParameterType;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.safety.Whitelist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

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
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

@Stateless
@LocalBean
public class MailModuleBean implements BeeModule {

  private static final BeeLogger logger = LogUtils.getLogger(MailModuleBean.class);

  private static final String TBL_ADDRESSES = CommonsConstants.TBL_EMAILS;
  private static final String COL_EMAIL = CommonsConstants.COL_EMAIL_ADDRESS;
  private static final String COL_LABEL = CommonsConstants.COL_EMAIL_LABEL;

  @EJB
  MailProxy proxy;
  @EJB
  FolderHandlerBean folders;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;
  @Resource
  EJBContext ctx;

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
      response = getMessage(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MESSAGE)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));

    } else if (BeeUtils.same(svc, SVC_GET_ACCOUNTS)) {
      response = getAccounts(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_USER)));

    } else if (BeeUtils.same(svc, SVC_CHECK_MAIL)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder folder = null;

      if (DataUtils.isId(folderId)) {
        folder = folders.findFolder(folders.getInboxFolder(account.getAccountId()), folderId);
      } else {
        SystemFolder sysFolder = NameUtils.getEnumByName(SystemFolder.class,
            reqInfo.getParameter(COL_FOLDER_NAME));

        if (sysFolder != null) {
          switch (sysFolder) {
            case Drafts:
              folder = folders.getDraftsFolder(account);
              break;
            case Inbox:
              folder = folders.getInboxFolder(account.getAccountId());
              break;
            case Sent:
              folder = folders.getSentFolder(account);
              break;
            case Trash:
              folder = folders.getTrashFolder(account);
              break;
          }
        }
      }
      if (folder == null) {
        response = ResponseObject.error("Folder does not exist:", DataUtils.isId(folderId)
            ? BeeUtils.join("=", "ID", folderId)
            : reqInfo.getParameter(COL_FOLDER_NAME));
      } else {
        response = checkMail(account, folder, false);
      }
    } else if (BeeUtils.same(svc, SVC_SEND_MAIL)) {
      response = new ResponseObject();
      boolean save = BeeUtils.toBoolean(reqInfo.getParameter("Save"));
      Long draftId = BeeUtils.toLongOrNull(reqInfo.getParameter("DraftId"));
      Long sender = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SENDER));
      Set<Long> to = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.TO.name()));
      Set<Long> cc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.CC.name()));
      Set<Long> bcc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.BCC.name()));
      String subject = reqInfo.getParameter(COL_SUBJECT);
      String content = reqInfo.getParameter(COL_CONTENT);
      Set<Long> attachments = DataUtils.parseIdSet(reqInfo.getParameter("Attachments"));

      if (draftId != null) {
        qs.updateData(new SqlDelete(TBL_MESSAGES)
            .setWhere(sys.idEquals(TBL_MESSAGES, draftId)));
      }
      if (!save) {
        try {
          sendMail(sender, to, cc, bcc, subject, content, attachments);
          response.addInfo("Laiškas išsiųstas");

        } catch (MessagingException e) {
          save = true;
          logger.error(e);
          response.addError(e);
        }
      }
      if (save) {
        try {
          MailAccount account = getAccount(sender);
          MailFolder folder = folders.getDraftsFolder(account);
          MimeMessage message = buildMessage(null, account.getAddressId(), to, cc, bcc, subject,
              content, attachments);

          if (response.hasErrors() || !account.addMessageToRemoteFolder(message, folder)) {
            storeMail(message, folder.getId(), null);
          }
          response.addInfo("Laiškas išsaugotas juodraščiuose");

        } catch (MessagingException e) {
          logger.error(e);
          response.addError(e);
        }
      }
    } else if (BeeUtils.same(svc, SVC_REMOVE_MESSAGES)) {
      response = removeMessages(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SENDER)),
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
            "DefaultAccount", ParameterType.NUMBER, "Default mail account", false, null),
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

  public ResponseObject sendMail(Long from, String to, String subject, String content) {
    ResponseObject response;
    try {
      response = sendMail(from, storeAddress(new InternetAddress(to)), subject, content);
    } catch (AddressException e) {
      response = ResponseObject.error(e);
    }
    return response;
  }

  public ResponseObject sendMail(Long from, Long to, String subject, String content) {
    try {
      sendMail(from, Sets.newHashSet(to), null, null, subject, content, null);
    } catch (MessagingException e) {
      return ResponseObject.error(e);
    }
    return ResponseObject.info("Mail sent");
  }

  public void sendMail(Long from, Set<Long> to, Set<Long> cc,
      Set<Long> bcc, String subject, String content, Set<Long> attachments)
      throws MessagingException {

    MailAccount account = getAccount(from);

    if (!account.isValidTransportAccount()) {
      throw new MessagingException(account.getTransportErrorMessage());
    }
    Session session = Session.getInstance(new Properties(), null);
    session.setDebug(false);
    Transport transport = null;

    try {
      MimeMessage message = buildMessage(session, account.getAddressId(), to, cc, bcc, subject,
          content, attachments);

      transport = session.getTransport(account.getTransportProtocol().name().toLowerCase());
      transport.connect(account.getTransportHost(), account.getTransportPort(), null, null);
      transport.sendMessage(message, message.getAllRecipients());

      MailFolder folder = folders.getSentFolder(account);

      if (!account.addMessageToRemoteFolder(message, folder)) {
        storeMail(message, folder.getId(), null);
      }
    } finally {
      if (transport != null) {
        try {
          transport.close();
        } catch (MessagingException e) {
        }
      }
    }
  }

  public void storeProxyMail(String mail, String recipient) {
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
    Long folderId = null;

    if (!BeeUtils.isEmpty(recipient)) {
      InternetAddress adr;

      try {
        adr = new InternetAddress(recipient, false);
        adr.validate();
      } catch (AddressException ex) {
        adr = null;
      }
      SqlSelect ss = new SqlSelect()
          .addFields(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS))
          .addFrom(TBL_ACCOUNTS);
      Long accountId;

      if (adr == null) {
        accountId = qs.getLong(ss
            .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_STORE_LOGIN, recipient)));
      } else {
        accountId = qs.getLong(ss
            .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_ACCOUNTS, COL_ADDRESS))
            .setWhere(SqlUtils.equals(TBL_ADDRESSES, COL_EMAIL, recipient)));
      }
      if (DataUtils.isId(accountId)) {
        folderId = folders.getInboxFolder(accountId).getId();
      }
    }
    try {
      storeMail(message, folderId, null);
    } catch (MessagingException e) {
      throw new BeeRuntimeException(e);
    }
  }

  private void addContent(MimeMessage message, String content, Set<Long> attachments)
      throws MessagingException {

    MimeMultipart multi = null;

    if (!BeeUtils.isEmpty(attachments)) {
      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(CommonsConstants.TBL_FILES, CommonsConstants.COL_FILE_REPO,
              CommonsConstants.COL_FILE_NAME, CommonsConstants.COL_FILE_TYPE)
          .addFrom(CommonsConstants.TBL_FILES)
          .setWhere(SqlUtils.inList(CommonsConstants.TBL_FILES,
              sys.getIdName(CommonsConstants.TBL_FILES), attachments.toArray())));

      multi = new MimeMultipart();

      for (SimpleRow file : rs) {
        MimeBodyPart p = new MimeBodyPart();

        try {
          p.attachFile(file.getValue(CommonsConstants.COL_FILE_REPO));
          p.setFileName(MimeUtility.encodeText(file.getValue(CommonsConstants.COL_FILE_NAME)));

        } catch (UnsupportedEncodingException ex) {
          p.setFileName(file.getValue(CommonsConstants.COL_FILE_NAME));
        } catch (IOException ex) {
          logger.error(ex);
          p = null;
        }
        if (p != null) {
          multi.addBodyPart(p);
        }
      }
    }
    if (hasHtml(content)) {
      MimeMultipart mp = new MimeMultipart("alternative");

      MimeBodyPart p = new MimeBodyPart();
      p.setText(stripHtml(content), BeeConst.CHARSET_UTF8);
      mp.addBodyPart(p);

      p = new MimeBodyPart();
      p.setText(content, BeeConst.CHARSET_UTF8, "html");
      mp.addBodyPart(p);

      if (multi != null) {
        p = new MimeBodyPart();
        p.setContent(mp);
        multi.addBodyPart(p, 0);
      } else {
        multi = mp;
      }
    } else if (multi != null) {
      MimeBodyPart p = new MimeBodyPart();
      p.setText(content, BeeConst.CHARSET_UTF8);
      multi.addBodyPart(p, 0);
    }
    if (multi != null) {
      message.setContent(multi);
    } else {
      message.setText(content, BeeConst.CHARSET_UTF8);
    }
    message.saveChanges();
  }

  private MimeMessage buildMessage(Session session, Long from, Set<Long> to, Set<Long> cc,
      Set<Long> bcc, String subject, String content, Set<Long> attachments)
      throws MessagingException {

    MimeMessage message = new MimeMessage(session);

    if (!BeeUtils.isEmpty(to)) {
      message.setRecipients(RecipientType.TO, getAddresses(to));
    }
    if (!BeeUtils.isEmpty(cc)) {
      message.setRecipients(RecipientType.CC, getAddresses(cc));
    }
    if (!BeeUtils.isEmpty(bcc)) {
      message.setRecipients(RecipientType.BCC, getAddresses(bcc));
    }
    Address sender = getAddress(from);
    message.setSender(sender);
    message.setFrom(sender);
    message.setSentDate(TimeUtils.toJava(new DateTime()));
    message.setSubject(subject, BeeConst.CHARSET_UTF8);

    addContent(message, content, attachments);

    return message;
  }

  private int checkFolder(Folder remoteFolder, MailFolder localFolder, boolean recurse)
      throws MessagingException {
    Assert.noNulls(remoteFolder, localFolder);
    int c = 0;
    boolean uidMode = (remoteFolder instanceof UIDFolder);
    Long uidValidity = uidMode ? ((UIDFolder) remoteFolder).getUIDValidity() : null;

    if (localFolder.isConnected()) {
      folders.syncFolder(localFolder, uidValidity);

      if ((remoteFolder.getType() & Folder.HOLDS_MESSAGES) != 0) {
        try {
          remoteFolder.open(Folder.READ_ONLY);
          Message[] newMessages;

          if (uidMode) {
            newMessages = ((UIDFolder) remoteFolder)
                .getMessagesByUID(folders.getLastStoredUID(localFolder) + 1, UIDFolder.LASTUID);
          } else {
            newMessages = remoteFolder.getMessages();
          }
          for (Message message : newMessages) {
            if (storeMail(message, localFolder.getId(),
                uidMode ? ((UIDFolder) remoteFolder).getUID(message) : null)) {

              if (localFolder.getParent() == null) { // INBOX
                // TODO applyRules(message);
              }
              c++;
            }
          }
        } finally {
          if (remoteFolder.isOpen()) {
            try {
              remoteFolder.close(false);
            } catch (MessagingException e) {
            }
          }
        }
      }
    }
    Set<String> visitedFolders = Sets.newHashSet();

    if ((remoteFolder.getType() & Folder.HOLDS_FOLDERS) != 0) {
      for (Folder subFolder : remoteFolder.list()) {
        visitedFolders.add(subFolder.getName());
        MailFolder localSubFolder = folders.getFolder(localFolder, subFolder.getName());

        if (recurse) {
          c += checkFolder(subFolder, localSubFolder, true);
        }
      }
    }
    for (Iterator<MailFolder> iter = localFolder.getSubFolders().iterator(); iter.hasNext();) {
      MailFolder subFolder = iter.next();

      if (!visitedFolders.contains(subFolder.getName()) && folders.dropFolder(subFolder)) {
        iter.remove();
      }
    }
    return c;
  }

  private ResponseObject checkMail(MailAccount account, MailFolder localFolder, boolean recurse) {
    Assert.noNulls(account, localFolder);
    Store store = null;
    int c = 0;

    try {
      store = account.connectToStore(true);
      c = checkFolder(account.getRemoteFolder(store, localFolder), localFolder, recurse);

    } catch (MessagingException e) {
      ctx.setRollbackOnly();
      logger.error(e);
      return ResponseObject.error(e);

    } finally {
      account.disconnectFromStore(store);
    }
    return ResponseObject.response(c);
  }

  private String cleanHtml(String dirtyHtml) {
    if (dirtyHtml != null) {
      return Jsoup.clean(dirtyHtml, Whitelist.relaxed());
    }
    return dirtyHtml;
  }

  private MailAccount getAccount(Long addressId) {
    Assert.state(DataUtils.isId(addressId));

    return new MailAccount(qs.getRow(new SqlSelect()
        .addAllFields(TBL_ACCOUNTS)
        .addField(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS), COL_ACCOUNT)
        .addFields(TBL_ADDRESSES, COL_EMAIL)
        .addFrom(TBL_ACCOUNTS)
        .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_ACCOUNTS, COL_ADDRESS))
        .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_ADDRESS, addressId))));
  }

  private ResponseObject getAccounts(Long user) {
    Assert.notNull(user);

    return ResponseObject.response(qs.getData(new SqlSelect()
        .addFields(TBL_ACCOUNTS, COL_ACCOUNT_DESCRIPTION, COL_ADDRESS, COL_ACCOUNT_DEFAULT,
            "SentFolder", "DraftsFolder", "TrashFolder")
        .addField(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), "InboxFolder")
        .addFrom(TBL_ACCOUNTS)
        .addFromLeft(TBL_FOLDERS,
            SqlUtils.and(sys.joinTables(TBL_ACCOUNTS, TBL_FOLDERS, COL_ACCOUNT),
                SqlUtils.isNull(TBL_FOLDERS, COL_FOLDER_PARENT)))
        .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_USER, user))
        .addOrder(TBL_ACCOUNTS, COL_ACCOUNT_DESCRIPTION)));
  }

  private Address getAddress(Long id) {
    return ArrayUtils.getQuietly(getAddresses(Lists.newArrayList(id)), 0);
  }

  private Address[] getAddresses(Collection<Long> ids) {
    List<Address> addresses = Lists.newArrayList();

    if (!BeeUtils.isEmpty(ids)) {
      SimpleRowSet rs = qs.getData(new SqlSelect()
          .addFields(TBL_ADDRESSES, sys.getIdName(TBL_ADDRESSES), COL_EMAIL, COL_LABEL)
          .addFrom(TBL_ADDRESSES)
          .setWhere(SqlUtils.inList(TBL_ADDRESSES, sys.getIdName(TBL_ADDRESSES), ids.toArray())));

      Assert.state(ids.size() == rs.getNumberOfRows(), "Address count mismatch");

      for (SimpleRow address : rs) {
        try {
          addresses.add(new InternetAddress(address.getValue(COL_EMAIL),
              address.getValue(COL_LABEL), BeeConst.CHARSET_UTF8));
        } catch (UnsupportedEncodingException e) {
        }
      }
    }
    return addresses.toArray(new Address[0]);
  }

  private ResponseObject getMessage(Long id, Long addressId) {
    Assert.notNull(id);
    IsCondition wh = SqlUtils.equals(TBL_RECIPIENTS, COL_MESSAGE, id);

    if (addressId != null) {
      if (addressId != 0) {
        // TODO mark as read
      }
      wh = SqlUtils.and(wh,
          SqlUtils.notEqual(TBL_RECIPIENTS, COL_ADDRESS_TYPE, AddressType.BCC.name()));
    }
    Map<String, SimpleRowSet> packet = Maps.newHashMap();

    packet.put(TBL_RECIPIENTS, qs.getData(new SqlSelect()
        .addFields(TBL_RECIPIENTS, COL_ADDRESS_TYPE, COL_ADDRESS)
        .addFields(TBL_ADDRESSES, COL_EMAIL, COL_LABEL)
        .addFrom(TBL_RECIPIENTS)
        .addFromInner(TBL_ADDRESSES, sys.joinTables(TBL_ADDRESSES, TBL_RECIPIENTS, COL_ADDRESS))
        .setWhere(wh)
        .addOrderDesc(TBL_RECIPIENTS, COL_ADDRESS_TYPE)));

    String[] cols = new String[] {COL_CONTENT, COL_HTML_CONTENT};
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, cols)
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equals(TBL_PARTS, COL_MESSAGE, id)));

    SimpleRowSet newRs = new SimpleRowSet(cols);

    for (String[] row : rs.getRows()) {
      newRs.addRow(new String[] {row[0], cleanHtml(row[1])});
    }
    packet.put(TBL_PARTS, newRs);

    packet.put(TBL_ATTACHMENTS, qs.getData(new SqlSelect()
        .addFields(TBL_ATTACHMENTS, COL_FILE, COL_ATTACHMENT_NAME)
        .addFields(CommonsConstants.TBL_FILES, CommonsConstants.COL_FILE_NAME,
            CommonsConstants.COL_FILE_SIZE)
        .addFrom(TBL_ATTACHMENTS)
        .addFromInner(CommonsConstants.TBL_FILES,
            sys.joinTables(CommonsConstants.TBL_FILES, TBL_ATTACHMENTS, COL_FILE))
        .setWhere(SqlUtils.equals(TBL_ATTACHMENTS, COL_MESSAGE, id))));

    return ResponseObject.response(packet);
  }

  private boolean hasHtml(String content) {
    if (content != null) {
      return !Jsoup.isValid(content, Whitelist.none());
    }
    return false;
  }

  private ResponseObject removeMessages(Long sender, Long recipient, String[] messages,
      boolean purge) {
    Assert.state(!ArrayUtils.isEmpty(messages), "Empty message list");

    SqlUpdate su = null;
    HasConditions wh = SqlUtils.or();

    if (sender != null) {
      String idName = sys.getIdName(TBL_MESSAGES);

      for (String id : messages) {
        wh.add(SqlUtils.equals(TBL_MESSAGES, idName, BeeUtils.toLong(id)));
      }
      su = new SqlUpdate(TBL_MESSAGES)
          .setWhere(SqlUtils.and(wh, SqlUtils.equals(TBL_MESSAGES, COL_SENDER, sender)));

    } else if (recipient != null) {
      for (String id : messages) {
        wh.add(SqlUtils.equals(TBL_RECIPIENTS, COL_MESSAGE, BeeUtils.toLong(id)));
      }
      su = new SqlUpdate(TBL_RECIPIENTS)
          .setWhere(SqlUtils.and(wh, SqlUtils.equals(TBL_RECIPIENTS, COL_ADDRESS, recipient)));
    } else {
      Assert.untouchable("Unknown recipient");
    }
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
        .setWhere(SqlUtils.equals(TBL_ADDRESSES, COL_EMAIL, email)));

    if (id == null) {
      id = qs.insertData(new SqlInsert(TBL_ADDRESSES)
          .addConstant(COL_EMAIL, email)
          .addConstant(COL_LABEL, label));
    }
    return id;
  }

  private boolean storeMail(Message message, Long folderId, Long messageUID)
      throws MessagingException {

    MailEnvelope envelope = new MailEnvelope(message);
    Long messageId = null;
    Long placeId = null;
    Long uid = null;

    SimpleRow data = qs.getRow(new SqlSelect()
        .addField(TBL_MESSAGES, sys.getIdName(TBL_MESSAGES), COL_MESSAGE)
        .addField(TBL_PLACES, sys.getIdName(TBL_PLACES), COL_UNIQUE_ID)
        .addFields(TBL_PLACES, COL_MESSAGE_UID)
        .addFrom(TBL_MESSAGES)
        .addFromLeft(TBL_PLACES,
            SqlUtils.and(sys.joinTables(TBL_MESSAGES, TBL_PLACES, COL_MESSAGE),
                SqlUtils.equals(TBL_PLACES, COL_FOLDER, folderId)))
        .setWhere(SqlUtils.equals(TBL_MESSAGES, COL_UNIQUE_ID, envelope.getUniqueId())));

    if (data != null) {
      messageId = data.getLong(COL_MESSAGE);
      placeId = data.getLong(COL_UNIQUE_ID);
      uid = data.getLong(COL_MESSAGE_UID);
    }
    if (!DataUtils.isId(messageId)) {
      Long sender = storeAddress(envelope.getSender());

      messageId = qs.insertData(new SqlInsert(TBL_MESSAGES)
          .addConstant(COL_UNIQUE_ID, envelope.getUniqueId())
          .addConstant(COL_DATE, envelope.getDate())
          .addConstant(COL_SENDER, sender)
          .addConstant(COL_SUBJECT, envelope.getSubject()));

      qs.insertData(new SqlInsert(TBL_HEADERS)
          .addConstant(COL_MESSAGE, messageId)
          .addConstant(COL_HEADER, envelope.getHeader()));

      Set<Long> allAddresses = Sets.newHashSet();

      for (Entry<AddressType, Address> entry : envelope.getRecipients().entries()) {
        Long adr = storeAddress(entry.getValue());

        if (allAddresses.add(adr)) {
          qs.insertData(new SqlInsert(TBL_RECIPIENTS)
              .addConstant(COL_MESSAGE, messageId)
              .addConstant(COL_ADDRESS, adr)
              .addConstant(COL_ADDRESS_TYPE, entry.getKey().name()));
        }
      }
      try {
        storePart(messageId, message, null);
      } catch (IOException e) {
        throw new MessagingException(e.toString());
      }
    }
    if (!DataUtils.isId(placeId)) {
      qs.insertData(new SqlInsert(TBL_PLACES)
          .addConstant(COL_MESSAGE, messageId)
          .addConstant(COL_FOLDER, folderId)
          .addConstant(COL_MESSAGE_UID, messageUID));

    } else if (!Objects.equal(messageUID, uid)) {
      qs.updateData(new SqlUpdate(TBL_PLACES)
          .addConstant(COL_MESSAGE_UID, messageUID)
          .setWhere(sys.idEquals(TBL_PLACES, placeId)));
    }
    return !DataUtils.isId(placeId);
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
            .addConstant(COL_CONTENT_TYPE, alternative.getB() != null ? "text/html" : "text/plain")
            .addConstant(COL_CONTENT,
                stripHtml(alternative.getA() != null ? alternative.getA() : alternative.getB()))
            .addConstant(COL_HTML_CONTENT, alternative.getB()));
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
      if (!part.isMimeType("text/*")
          || BeeUtils.same(disposition, Part.ATTACHMENT)
          || !BeeUtils.isEmpty(fileName)
          || (alternative != null
              && !part.isMimeType("text/plain") && !part.isMimeType("text/html"))) {

        Long fileId = fs.storeFile(part.getInputStream(), fileName, contentType);

        qs.insertData(new SqlInsert(TBL_ATTACHMENTS)
            .addConstant(COL_MESSAGE, messageId)
            .addConstant(COL_FILE, fileId)
            .addConstant(COL_ATTACHMENT_NAME, fileName));

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
        }
        qs.insertData(new SqlInsert(TBL_PARTS)
            .addConstant(COL_MESSAGE, messageId)
            .addConstant(COL_CONTENT_TYPE, contentType)
            .addConstant(COL_CONTENT, stripHtml(content))
            .addConstant(COL_HTML_CONTENT, htmlContent));
      }
    }
  }

  private String stripHtml(String content) {
    if (hasHtml(content)) {
      return new HtmlToPlainText().getPlainText(Jsoup.parse(content));
    }
    return content;
  }
}
