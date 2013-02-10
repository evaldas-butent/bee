package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.AddressType;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.utils.BeeUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.UIDFolder;
import javax.mail.internet.AddressException;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParseException;

@Stateless
@LocalBean
public class MailStorageBean {

  private static final BeeLogger logger = LogUtils.getLogger(MailStorageBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  FileStorageBean fs;

  public void connectFolder(MailFolder folder) {
    validateFolder(folder, null);
  }

  public MailFolder createFolder(MailAccount account, MailFolder parent, String name) {
    Assert.notNull(parent);

    for (MailFolder subFolder : parent.getSubFolders()) {
      if (BeeUtils.same(subFolder.getName(), name)) {
        return subFolder;
      }
    }
    MailFolder folder = createFolder(parent.getAccountId(), parent, name, null);

    if (!account.isStoredRemotedly(parent)) {
      disconnectFolder(folder);
    }
    return folder;
  }

  public void disconnectFolder(MailFolder folder) {
    Assert.notNull(folder);

    if (folder.isConnected()) {
      folder.disconnect();

      qs.updateData(new SqlUpdate(TBL_PLACES)
          .addConstant(COL_MESSAGE_UID, null)
          .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));

      qs.updateData(new SqlUpdate(TBL_FOLDERS)
          .addConstant(COL_FOLDER_UID, folder.getUidValidity())
          .setWhere(sys.idEquals(TBL_FOLDERS, folder.getId())));
    }
  }

  public void dropFolder(MailFolder folder) {
    Assert.notNull(folder);

    qs.updateData(new SqlDelete(TBL_FOLDERS)
        .setWhere(sys.idEquals(TBL_FOLDERS, folder.getId())));
  }

  public MailFolder findFolder(MailAccount account, Long folderId) {
    return getRootFolder(account).findFolder(folderId);
  }

  public MailAccount getAccount(Long addressId) {
    Assert.state(DataUtils.isId(addressId));
    return getAccount(null, addressId);
  }

  public MailFolder getDraftsFolder(MailAccount account) {
    return getSysFolder(account, account.getDraftsFolderId(), SystemFolder.Drafts);
  }

  public MailFolder getInboxFolder(MailAccount account) {
    return getSysFolder(account, account.getInboxFolderId(), SystemFolder.Inbox);
  }

  public MailFolder getRootFolder(MailAccount account) {
    Assert.notNull(account);

    if (account.getRootFolder() == null) {
      account.setRootFolder(getRootFolder(account.getAccountId()));
    }
    return account.getRootFolder();
  }

  public MailFolder getRootFolder(Long accountId) {
    Assert.state(DataUtils.isId(accountId));

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FOLDERS, COL_FOLDER_PARENT, COL_FOLDER_NAME, COL_FOLDER_UID)
        .addField(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), COL_FOLDER)
        .addFrom(TBL_FOLDERS)
        .setWhere(SqlUtils.equals(TBL_FOLDERS, COL_ACCOUNT, accountId))
        .addOrder(TBL_FOLDERS, COL_FOLDER_PARENT, COL_FOLDER_NAME));

    Multimap<Long, SimpleRow> folders = LinkedListMultimap.create();

    for (SimpleRow row : data) {
      folders.put(row.getLong(COL_FOLDER_PARENT), row);
    }
    MailFolder root = new MailFolder(accountId, null, null, null, null);
    createTree(root, folders);

    if (BeeUtils.isEmpty(root.getSubFolders())) {
      MailAccount account = getAccount(accountId, null);
      account.setRootFolder(root);

      for (SystemFolder sysFolder : SystemFolder.values()) {
        createSysFolder(account, sysFolder);
      }
    }
    return root;
  }

  public MailFolder getSentFolder(MailAccount account) {
    return getSysFolder(account, account.getSentFolderId(), SystemFolder.Sent);
  }

  public MailFolder getTrashFolder(MailAccount account) {
    return getSysFolder(account, account.getTrashFolderId(), SystemFolder.Trash);
  }

  public void renameFolder(MailFolder folder, String name) {
    qs.updateData(new SqlUpdate(TBL_FOLDERS)
        .addConstant(COL_FOLDER_NAME, name)
        .setWhere(sys.idEquals(TBL_FOLDERS, folder.getId())));
  }

  public Long storeAddress(Address address) throws AddressException {
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
        .addFields(CommonsConstants.TBL_EMAILS, sys.getIdName(CommonsConstants.TBL_EMAILS))
        .addFrom(CommonsConstants.TBL_EMAILS)
        .setWhere(SqlUtils.equals(CommonsConstants.TBL_EMAILS, CommonsConstants.COL_EMAIL_ADDRESS,
            email)));

    if (id == null) {
      id = qs.insertData(new SqlInsert(CommonsConstants.TBL_EMAILS)
          .addConstant(CommonsConstants.COL_EMAIL_ADDRESS, email)
          .addConstant(CommonsConstants.COL_EMAIL_LABEL, label));
    }
    return id;
  }

  public boolean storeMail(Message message, Long folderId, Long messageUID)
      throws MessagingException {

    MailEnvelope envelope = new MailEnvelope(message);
    Long messageId = null;
    Long placeId = null;

    SimpleRow data = qs.getRow(new SqlSelect()
        .addField(TBL_MESSAGES, sys.getIdName(TBL_MESSAGES), COL_MESSAGE)
        .addMax(TBL_PLACES, sys.getIdName(TBL_PLACES), COL_UNIQUE_ID)
        .addFrom(TBL_MESSAGES)
        .addFromLeft(TBL_PLACES,
            SqlUtils.and(sys.joinTables(TBL_MESSAGES, TBL_PLACES, COL_MESSAGE),
                SqlUtils.equals(TBL_PLACES, COL_FOLDER, folderId, COL_MESSAGE_UID, messageUID)))
        .setWhere(SqlUtils.equals(TBL_MESSAGES, COL_UNIQUE_ID, envelope.getUniqueId()))
        .addGroup(TBL_MESSAGES, sys.getIdName(TBL_MESSAGES)));

    if (data != null) {
      messageId = data.getLong(COL_MESSAGE);
      placeId = data.getLong(COL_UNIQUE_ID);
    }
    if (!DataUtils.isId(messageId)) {
      Long sender = storeAddress(envelope.getSender());

      messageId = qs.insertData(new SqlInsert(TBL_MESSAGES)
          .addConstant(COL_UNIQUE_ID, envelope.getUniqueId())
          .addConstant(COL_DATE, envelope.getDate())
          .addConstant(COL_SENDER, sender)
          .addConstant(COL_SUBJECT, envelope.getSubject()));

      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      try {
        message.writeTo(bos);
        bos.close();

        qs.insertData(new SqlInsert(TBL_RAW_CONTENTS).addConstant(COL_MESSAGE, messageId)
            .addConstant(COL_RAW_CONTENT, bos.toString(BeeConst.CHARSET_UTF8)));

      } catch (IOException e) {
        throw new MessagingException(e.toString());
      }
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
        try {
          storePart(messageId, message, null);
        } catch (MessagingException ex) {
          storePart(messageId, new MimeMessage(null, new ByteArrayInputStream(bos.toByteArray())),
              null);
        }
      } catch (IOException e) {
        throw new MessagingException(e.toString());
      }
    }
    if (!DataUtils.isId(placeId)) {
      qs.insertData(new SqlInsert(TBL_PLACES)
          .addConstant(COL_MESSAGE, messageId)
          .addConstant(COL_FOLDER, folderId)
          .addConstant(COL_FLAGS, envelope.getFlagMask())
          .addConstant(COL_MESSAGE_UID, messageUID));
    }
    return !DataUtils.isId(placeId);
  }

  public long syncFolder(MailFolder localFolder, Folder remoteFolder, boolean sync)
      throws MessagingException {
    Assert.noNulls(localFolder, remoteFolder);

    long lastUid;

    if (sync) {
      SimpleRowSet data = qs.getData(new SqlSelect()
          .addFields(TBL_PLACES, COL_FLAGS, COL_MESSAGE_UID)
          .addField(TBL_PLACES, sys.getIdName(TBL_PLACES), COL_UNIQUE_ID)
          .addFrom(TBL_PLACES)
          .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, localFolder.getId()))
          .addOrderDesc(TBL_PLACES, COL_MESSAGE_UID)
          .setLimit(100));

      lastUid = BeeUtils.unbox(data.getLong(0, COL_MESSAGE_UID));

      if (data.getNumberOfRows() > 0) {
        Set<Long> syncedMsgs = Sets.newHashSet();

        Message[] msgs = ((UIDFolder) remoteFolder).getMessagesByUID(BeeUtils
            .unbox(data.getLong(data.getNumberOfRows() - 1, COL_MESSAGE_UID)), lastUid);

        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        remoteFolder.fetch(msgs, fp);

        for (Message message : msgs) {
          long uid = ((UIDFolder) remoteFolder).getUID(message);
          SimpleRow row = data.getRow(data.getKeyIndex(COL_MESSAGE_UID, BeeUtils.toString(uid)));

          if (row != null) {
            Integer flags = MailEnvelope.getFlagMask(message);
            Long id = row.getLong(COL_UNIQUE_ID);

            if (BeeUtils.unbox(row.getInt(COL_FLAGS)) != BeeUtils.unbox(flags)) {
              qs.updateData(new SqlUpdate(TBL_PLACES)
                  .addConstant(COL_FLAGS, flags)
                  .setWhere(sys.idEquals(TBL_PLACES, id)));
            }
            syncedMsgs.add(id);
          } else {
            storeMail(message, localFolder.getId(), uid);
          }
        }
        List<Long> deletedMsgs = Lists.newArrayList();

        for (int i = 0; i < data.getNumberOfRows(); i++) {
          Long id = data.getLong(i, COL_UNIQUE_ID);

          if (!syncedMsgs.contains(id)) {
            deletedMsgs.add(id);
          }
        }
        if (!deletedMsgs.isEmpty()) {
          qs.updateData(new SqlDelete(TBL_PLACES)
              .setWhere(SqlUtils.inList(TBL_PLACES, sys.getIdName(TBL_PLACES), deletedMsgs)));
        }
      }
    } else {
      lastUid = BeeUtils.unbox(qs.getLong(new SqlSelect()
          .addMax(TBL_PLACES, COL_MESSAGE_UID)
          .addFrom(TBL_PLACES)
          .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, localFolder.getId()))));
    }
    return lastUid;
  }

  public void validateFolder(MailFolder folder, Long uidValidity) {
    Assert.notNull(folder);

    if (!Objects.equal(uidValidity, folder.getUidValidity())) {
      qs.updateData(new SqlDelete(TBL_PLACES)
          .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));

      qs.updateData(new SqlUpdate(TBL_FOLDERS)
          .addConstant(COL_FOLDER_UID, uidValidity)
          .setWhere(sys.idEquals(TBL_FOLDERS, folder.getId())));

      folder.setUidValidity(uidValidity);
    }
  }

  private MailFolder createFolder(Long accountId, MailFolder parent, String name, Long folderUID) {
    Assert.state(DataUtils.isId(accountId));
    Assert.notEmpty(name);

    long id = qs.insertData(new SqlInsert(TBL_FOLDERS)
        .addConstant(COL_ACCOUNT, accountId)
        .addConstant(COL_FOLDER_PARENT, parent == null ? null : parent.getId())
        .addConstant(COL_FOLDER_NAME, name)
        .addConstant(COL_FOLDER_UID, folderUID));

    MailFolder folder = new MailFolder(accountId, parent, id, name, folderUID);

    if (parent != null) {
      parent.addSubFolder(folder);
    }
    return folder;
  }

  private MailFolder createSysFolder(MailAccount account, SystemFolder sysFolder) {
    MailFolder root = getRootFolder(account);
    MailFolder folder = createFolder(account, root, sysFolder.getFullName());

    if (sysFolder == SystemFolder.Inbox && !folder.isConnected()) {
      connectFolder(folder);
    }
    qs.updateData(new SqlUpdate(TBL_ACCOUNTS)
        .addConstant(sysFolder.name() + COL_FOLDER, folder.getId())
        .setWhere(sys.idEquals(TBL_ACCOUNTS, root.getAccountId())));

    return folder;
  }

  private void createTree(MailFolder parent, Multimap<Long, SimpleRow> folders) {
    for (SimpleRow row : folders.get(parent.getId())) {
      MailFolder folder = new MailFolder(parent.getAccountId(), parent, row.getLong(COL_FOLDER),
          row.getValue(COL_FOLDER_NAME), row.getLong(COL_FOLDER_UID));

      createTree(folder, folders);
      parent.addSubFolder(folder);
    }
  }

  private MailAccount getAccount(Long accountId, Long addressId) {
    return new MailAccount(qs.getRow(new SqlSelect()
        .addAllFields(TBL_ACCOUNTS)
        .addField(TBL_ACCOUNTS, sys.getIdName(TBL_ACCOUNTS), COL_ACCOUNT)
        .addFields(CommonsConstants.TBL_EMAILS, CommonsConstants.COL_EMAIL_ADDRESS)
        .addFrom(TBL_ACCOUNTS)
        .addFromInner(CommonsConstants.TBL_EMAILS,
            sys.joinTables(CommonsConstants.TBL_EMAILS, TBL_ACCOUNTS, COL_ADDRESS))
        .setWhere(DataUtils.isId(addressId)
            ? SqlUtils.equals(TBL_ACCOUNTS, COL_ADDRESS, addressId)
            : sys.idEquals(TBL_ACCOUNTS, accountId))));
  }

  private MailFolder getSysFolder(MailAccount account, Long folderId, SystemFolder sysFolder) {
    MailFolder folder = null;

    if (DataUtils.isId(folderId)) {
      folder = findFolder(account, folderId);
    }
    if (folder == null) {
      folder = createSysFolder(account, sysFolder);
    }
    return folder;
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
            .addConstant(COL_CONTENT, HtmlUtils.stripHtml(alternative.getA() != null
                ? alternative.getA() : alternative.getB()))
            .addConstant(COL_HTML_CONTENT, alternative.getB()));
      }
    } else if (part.isMimeType("message/rfc822")) {
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
            .addConstant(COL_CONTENT, HtmlUtils.stripHtml(content))
            .addConstant(COL_HTML_CONTENT, htmlContent));
      }
    }
  }
}
