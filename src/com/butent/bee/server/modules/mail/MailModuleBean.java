package com.butent.bee.server.modules.mail;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.modules.BeeModule;
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
import com.butent.bee.shared.modules.mail.MailConstants.MessageFlag;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

@Stateless
@LocalBean
public class MailModuleBean implements BeeModule {

  private static final BeeLogger logger = LogUtils.getLogger(MailModuleBean.class);

  @EJB
  MailProxy proxy;
  @EJB
  MailStorageBean mail;
  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
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

    } else if (BeeUtils.same(svc, SVC_GET_ACCOUNTS)) {
      response = getAccounts(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_USER)));

    } else if (BeeUtils.same(svc, SVC_GET_MESSAGE)) {
      response = getMessage(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_MESSAGE)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)),
          BeeUtils.toLongOrNull(reqInfo.getParameter(COL_PLACE)),
          BeeUtils.toBoolean(reqInfo.getParameter("showBcc")),
          BeeUtils.toBoolean(reqInfo.getParameter("markAsRead")));

    } else if (BeeUtils.same(svc, SVC_FLAG_MESSAGE)) {
      try {
        response = ResponseObject.response(setMessageFlag(getAccount(BeeUtils
            .toLongOrNull(reqInfo.getParameter(COL_ADDRESS))),
            BeeUtils.toLongOrNull(reqInfo.getParameter(COL_PLACE)), MessageFlag.FLAGGED,
            BeeUtils.toBoolean(reqInfo.getParameter("toggle"))));
      } catch (MessagingException e) {
        response = ResponseObject.error(e);
      }

    } else if (BeeUtils.same(svc, SVC_COPY_MESSAGES)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));

      response = processMessages(account,
          mail.findFolder(account, BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER_PARENT))),
          mail.findFolder(account, BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER))),
          Codec.beeDeserializeCollection(reqInfo.getParameter(COL_PLACE)),
          BeeUtils.toBoolean(reqInfo.getParameter("move")));

    } else if (BeeUtils.same(svc, SVC_REMOVE_MESSAGES)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));

      response = processMessages(account,
          mail.findFolder(account, BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER))),
          BeeUtils.toBoolean(reqInfo.getParameter("Purge")) ? null : mail.getTrashFolder(account),
          Codec.beeDeserializeCollection(reqInfo.getParameter(COL_PLACE)), true);

    } else if (BeeUtils.same(svc, SVC_GET_FOLDERS)) {
      Object resp = mail.getRootFolder(BeeUtils.toLong(reqInfo.getParameter(COL_ACCOUNT)));
      Long addressId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS));

      if (DataUtils.isId(addressId)) {
        MailAccount account = getAccount(addressId);
        resp = ImmutableMap.of(SystemFolder.Inbox, resp,
            SystemFolder.Sent, account.getSentFolderId(),
            SystemFolder.Drafts, account.getDraftsFolderId(),
            SystemFolder.Trash, account.getTrashFolderId());
      }
      response = ResponseObject.response(resp);

    } else if (BeeUtils.same(svc, SVC_CREATE_FOLDER)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder parent = mail.getInboxFolder(account);

      if (folderId != null) {
        parent = parent.findFolder(folderId);
      }
      if (parent == null) {
        response = ResponseObject.error("Folder does not exist: ID =", folderId);
      } else {
        String name = reqInfo.getParameter(COL_FOLDER_NAME);

        try {
          if (account.createRemoteFolder(parent, name)) {
            mail.createFolder(account, parent, name);
            response = ResponseObject.info("Folder created:", name);
          } else {
            response = ResponseObject.error("Cannot create folder:", name);
          }
        } catch (MessagingException e) {
          response = ResponseObject.error(e);
        }
      }
    } else if (BeeUtils.same(svc, SVC_DISCONNECT_FOLDER)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder folder = mail.findFolder(account, folderId);

      if (folder == null) {
        response = ResponseObject.error("Folder does not exist: ID =", folderId);
      } else {
        mail.disconnectFolder(folder);
        response = ResponseObject.info("Folder disconnected: " + folder.getName());
      }
    } else if (BeeUtils.same(svc, SVC_RENAME_FOLDER)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder folder = mail.findFolder(account, folderId);
      String name = reqInfo.getParameter(COL_FOLDER_NAME);

      if (folder == null) {
        response = ResponseObject.error("Folder does not exist: ID =", folderId);
      } else {
        try {
          if (account.renameRemoteFolder(folder, name)) {
            mail.renameFolder(folder, name);
            response = ResponseObject.info("Folder renamed: " + folder.getName() + "->" + name);
          } else {
            response = ResponseObject.error("Cannot rename folder", folder.getName(), "to", name);
          }
        } catch (MessagingException e) {
          response = ResponseObject.error(e);
        }
      }
    } else if (BeeUtils.same(svc, SVC_DROP_FOLDER)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder folder = mail.findFolder(account, folderId);

      if (folder == null) {
        response = ResponseObject.error("Folder does not exist: ID =", folderId);
      } else {
        try {
          if (account.dropRemoteFolder(folder)) {
            mail.dropFolder(folder);
            response = ResponseObject.info("Folder dropped: " + folder.getName());
          } else {
            response = ResponseObject.error("Cannot drop folder", folder.getName());
          }
        } catch (MessagingException e) {
          response = ResponseObject.error(e);
        }
      }
    } else if (BeeUtils.same(svc, SVC_CHECK_MAIL)) {
      MailAccount account = getAccount(BeeUtils.toLongOrNull(reqInfo.getParameter(COL_ADDRESS)));
      Long folderId = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_FOLDER));
      MailFolder folder = mail.findFolder(account, folderId);

      if (folder == null) {
        response = ResponseObject.error("Folder does not exist: ID =", folderId);
      } else {
        response = checkMail(account, folder, true);
      }
    } else if (BeeUtils.same(svc, SVC_SEND_MAIL)) {
      response = new ResponseObject();
      boolean save = BeeUtils.toBoolean(reqInfo.getParameter("Save"));
      String draftId = reqInfo.getParameter("DraftId");
      Long sender = BeeUtils.toLongOrNull(reqInfo.getParameter(COL_SENDER));
      Set<Long> to = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.TO.name()));
      Set<Long> cc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.CC.name()));
      Set<Long> bcc = DataUtils.parseIdSet(reqInfo.getParameter(AddressType.BCC.name()));
      String subject = reqInfo.getParameter(COL_SUBJECT);
      String content = reqInfo.getParameter(COL_CONTENT);
      Set<Long> attachments = DataUtils.parseIdSet(reqInfo.getParameter("Attachments"));

      MailAccount account = null;

      if (draftId != null) {
        account = getAccount(sender);
        processMessages(account, mail.getDraftsFolder(account), null, new String[] {draftId}, true);
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
          if (account == null) {
            account = getAccount(sender);
          }
          MailFolder folder = mail.getDraftsFolder(account);
          MimeMessage message = buildMessage(null, account.getAddressId(), to, cc, bcc, subject,
              content, attachments);

          if (!account.addMessageToRemoteFolder(message, folder)) {
            mail.storeMail(message, folder.getId(), null);
          }
          response.addInfo("Laiškas išsaugotas juodraščiuose");

        } catch (MessagingException e) {
          logger.error(e);
          response.addError(e);
        }
      }
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
      response = sendMail(from, mail.storeAddress(new InternetAddress(to)), subject, content);
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

      MailFolder folder = mail.getSentFolder(account);

      if (!account.addMessageToRemoteFolder(message, folder)) {
        mail.storeMail(message, folder.getId(), null);
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

  public void storeProxyMail(String content, String recipient) {
    Assert.notNull(content);
    logger.debug("GOT", BeeUtils.isEmpty(recipient) ? Protocol.SMTP : Protocol.POP3, "mail:");
    logger.debug(content);

    MimeMessage message = null;

    try {
      message = new MimeMessage(null,
          new ByteArrayInputStream(content.getBytes(BeeConst.CHARSET_UTF8)));
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
            .addFromInner(CommonsConstants.TBL_EMAILS,
                sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_ACCOUNTS, COL_ADDRESS))
            .setWhere(SqlUtils.equals(CommonsConstants.TBL_EMAILS,
                CommonsConstants.COL_EMAIL_ADDRESS, recipient)));
      }
      if (DataUtils.isId(accountId)) {
        folderId = mail.getRootFolder(accountId).getId();
      }
    }
    if (DataUtils.isId(folderId)) {
      try {
        mail.storeMail(message, folderId, null);
      } catch (MessagingException e) {
        throw new BeeRuntimeException(e);
      }
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
    if (HtmlUtils.hasHtml(content)) {
      MimeMultipart mp = new MimeMultipart("alternative");

      MimeBodyPart p = new MimeBodyPart();
      p.setText(HtmlUtils.stripHtml(content), BeeConst.CHARSET_UTF8);
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

  private int checkFolder(MailAccount account, Folder remoteFolder, MailFolder localFolder,
      boolean sync) throws MessagingException {
    Assert.noNulls(remoteFolder, localFolder);

    int c = 0;

    if (localFolder.isConnected() && account.holdsMessages(remoteFolder)) {
      boolean uidMode = (remoteFolder instanceof UIDFolder);
      Long uidValidity = uidMode ? ((UIDFolder) remoteFolder).getUIDValidity() : null;

      mail.validateFolder(localFolder, uidValidity);

      try {
        remoteFolder.open(Folder.READ_ONLY);
        Message[] newMessages;

        if (uidMode) {
          long lastUid = mail.syncFolder(localFolder, remoteFolder, sync);
          newMessages = ((UIDFolder) remoteFolder).getMessagesByUID(lastUid + 1,
              UIDFolder.LASTUID);
        } else {
          newMessages = remoteFolder.getMessages();
        }
        for (Message message : newMessages) {
          if (mail.storeMail(message, localFolder.getId(),
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
    Set<String> visitedFolders = Sets.newHashSet();

    if (account.holdsFolders(remoteFolder)) {
      for (Folder subFolder : remoteFolder.list()) {
        visitedFolders.add(subFolder.getName());
        MailFolder localSubFolder = mail.createFolder(account, localFolder, subFolder.getName());

        if (localSubFolder.isConnected() && !subFolder.isSubscribed()) {
          subFolder.setSubscribed(true);
        }
        c += checkFolder(account, subFolder, localSubFolder, false);
      }
    }
    for (Iterator<MailFolder> iter = localFolder.getSubFolders().iterator(); iter.hasNext();) {
      MailFolder subFolder = iter.next();

      if (!visitedFolders.contains(subFolder.getName()) && subFolder.isConnected()) {
        mail.dropFolder(subFolder);
        iter.remove();
      }
    }
    return c;
  }

  private ResponseObject checkMail(MailAccount account, MailFolder localFolder, boolean sync) {
    Assert.noNulls(account, localFolder);
    Store store = null;
    int c = 0;

    if (localFolder.isConnected()) {
      try {
        store = account.connectToStore();
        c = checkFolder(account, account.getRemoteFolder(store, localFolder), localFolder, sync);

      } catch (MessagingException e) {
        ctx.setRollbackOnly();
        logger.error(e);
        return ResponseObject.error(e);

      } finally {
        account.disconnectFromStore(store);
      }
    }
    return ResponseObject.response(c);
  }

  private MailAccount getAccount(Long addressId) {
    Assert.state(DataUtils.isId(addressId));

    return new MailAccount(qs.getRow(new SqlSelect()
        .addAllFields(TBL_ACCOUNTS)
        .addField(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS), COL_ACCOUNT)
        .addFields(CommonsConstants.TBL_EMAILS, CommonsConstants.COL_EMAIL_ADDRESS)
        .addFrom(TBL_ACCOUNTS)
        .addFromInner(CommonsConstants.TBL_EMAILS,
            sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_ACCOUNTS, COL_ADDRESS))
        .setWhere(SqlUtils.equals(TBL_ACCOUNTS, COL_ADDRESS, addressId))));
  }

  private ResponseObject getAccounts(Long user) {
    Assert.notNull(user);

    return ResponseObject.response(qs.getData(new SqlSelect()
        .addField(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS), COL_ACCOUNT)
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
          .addFields(CommonsConstants.TBL_EMAILS,
              CommonsConstants.COL_EMAIL_ADDRESS, CommonsConstants.COL_EMAIL_LABEL)
          .addFrom(CommonsConstants.TBL_EMAILS)
          .setWhere(SqlUtils.inList(CommonsConstants.TBL_EMAILS,
              sys.getIdName(CommonsConstants.TBL_EMAILS), ids.toArray())));

      Assert.state(ids.size() == rs.getNumberOfRows(), "Address count mismatch");

      for (SimpleRow address : rs) {
        try {
          addresses.add(new InternetAddress(address.getValue(CommonsConstants.COL_EMAIL_ADDRESS),
              address.getValue(CommonsConstants.COL_EMAIL_LABEL), BeeConst.CHARSET_UTF8));
        } catch (UnsupportedEncodingException e) {
        }
      }
    }
    return addresses.toArray(new Address[0]);
  }

  private ResponseObject getMessage(Long id, Long addressId, Long placeId, boolean showBcc,
      boolean markAsRead) {
    Assert.notNull(id);

    Map<String, SimpleRowSet> packet = Maps.newHashMap();
    IsCondition wh = SqlUtils.equals(TBL_RECIPIENTS, COL_MESSAGE, id);

    if (!showBcc) {
      wh = SqlUtils.and(wh,
          SqlUtils.notEqual(TBL_RECIPIENTS, COL_ADDRESS_TYPE, AddressType.BCC.name()));
    }
    packet.put(TBL_RECIPIENTS, qs.getData(new SqlSelect()
        .addFields(TBL_RECIPIENTS, COL_ADDRESS_TYPE, COL_ADDRESS)
        .addFields(CommonsConstants.TBL_EMAILS,
            CommonsConstants.COL_EMAIL_ADDRESS, CommonsConstants.COL_EMAIL_LABEL)
        .addFrom(TBL_RECIPIENTS)
        .addFromInner(CommonsConstants.TBL_EMAILS,
            sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_RECIPIENTS, COL_ADDRESS))
        .setWhere(wh)
        .addOrderDesc(TBL_RECIPIENTS, COL_ADDRESS_TYPE)));

    String[] cols = new String[] {COL_CONTENT, COL_HTML_CONTENT};
    SimpleRowSet rs = qs.getData(new SqlSelect()
        .addFields(TBL_PARTS, cols)
        .addFrom(TBL_PARTS)
        .setWhere(SqlUtils.equals(TBL_PARTS, COL_MESSAGE, id)));

    SimpleRowSet newRs = new SimpleRowSet(cols);

    for (String[] row : rs.getRows()) {
      newRs.addRow(new String[] {row[0], HtmlUtils.cleanHtml(row[1])});
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

    ResponseObject response = ResponseObject.response(packet);

    if (markAsRead) {
      try {
        setMessageFlag(getAccount(addressId), placeId, MessageFlag.SEEN, true);
      } catch (MessagingException e) {
        response.addError(e);
      }
    }
    return response;
  }

  private ResponseObject processMessages(MailAccount account, MailFolder source, MailFolder target,
      String[] places, boolean move) {
    Assert.state(!ArrayUtils.isEmpty(places), "Empty message list");

    HasConditions wh = SqlUtils.or();

    for (String id : places) {
      wh.add(sys.idEquals(TBL_PLACES, BeeUtils.toLong(id)));
    }
    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_PLACES, COL_MESSAGE, COL_FLAGS, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(wh));

    List<Long> lst = Lists.newArrayList();

    for (SimpleRow row : data) {
      Long id = row.getLong(COL_MESSAGE_UID);

      if (id != null) {
        lst.add(id);
      }
    }
    long[] uids = new long[lst.size()];

    for (int i = 0; i < lst.size(); i++) {
      uids[i] = lst.get(i);
    }
    ResponseObject response = null;
    boolean delete = move;

    try {
      account.processMessages(uids, source, target, move);

      if (target != null) {
        if (target.isConnected() && account.getStoreProtocol() != Protocol.POP3) {
          if (!source.isConnected()) {
            SimpleRowSet msgs = qs.getData(new SqlSelect()
                .addFields(TBL_HEADERS, COL_HEADER, COL_RAW_CONTENT)
                .addFrom(TBL_HEADERS)
                .addFromInner(TBL_PLACES,
                    SqlUtils.joinUsing(TBL_HEADERS, TBL_PLACES, COL_MESSAGE))
                .setWhere(wh));

            for (SimpleRow msg : msgs) {
              StringBuilder message = new StringBuilder(msg.getValue(COL_HEADER))
                  .append("\r\n")
                  .append(msg.getValue(COL_RAW_CONTENT));

              try {
                account.addMessageToRemoteFolder(new MimeMessage(null,
                    new ByteArrayInputStream(message.toString().getBytes(BeeConst.CHARSET_UTF8))),
                    target);
              } catch (UnsupportedEncodingException e) {
                throw new MessagingException(e.getMessage());
              }
            }
          }
          if (!delete) {
            response = ResponseObject.response(uids.length);
          }
        } else {
          if (move) {
            response = qs.updateDataWithResponse(new SqlUpdate(TBL_PLACES)
                .addConstant(COL_FOLDER, target.getId())
                .addConstant(COL_MESSAGE_UID, null)
                .setWhere(wh));
          } else {
            for (SimpleRow row : data) {
              qs.insertData(new SqlInsert(TBL_PLACES)
                  .addConstant(COL_FOLDER, target.getId())
                  .addConstant(COL_MESSAGE, row.getLong(COL_MESSAGE))
                  .addConstant(COL_FLAGS, row.getInt(COL_FLAGS)));
            }
            response = ResponseObject.response(data.getNumberOfRows());
          }
          delete = false;
        }
      }
      if (delete) {
        response = qs.updateDataWithResponse(new SqlDelete(TBL_PLACES).setWhere(wh));
      }
    } catch (MessagingException e) {
      ctx.setRollbackOnly();
      logger.error(e);
      response = ResponseObject.error(e);
    }
    return response;
  }

  private int setMessageFlag(MailAccount account, Long placeId, MessageFlag flag, boolean on)
      throws MessagingException {
    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_PLACES, COL_FOLDER, COL_FLAGS, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(sys.idEquals(TBL_PLACES, placeId)));

    Assert.notNull(row);
    int value = BeeUtils.unbox(row.getInt(COL_FLAGS));
    MailFolder folder = mail.findFolder(account, row.getLong(COL_FOLDER));

    account.setFlag(folder, new long[] {BeeUtils.unbox(row.getLong(COL_MESSAGE_UID))},
        MailEnvelope.getFlag(flag), on);

    if (on) {
      value = value | flag.getMask();
    } else {
      value = value & ~flag.getMask();
    }
    qs.updateData(new SqlUpdate(TBL_PLACES)
        .addConstant(COL_FLAGS, value)
        .setWhere(sys.idEquals(TBL_PLACES, placeId)));

    return value;
  }
}
