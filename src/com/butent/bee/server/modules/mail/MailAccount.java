package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailFolder;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.internet.MimeMessage;

public class MailAccount {

  private static final BeeLogger logger = LogUtils.getLogger(MailAccount.class);

  private String error = null;

  private final Protocol storeProtocol;
  private final String storeHost;
  private final Integer storePort;
  private final String storeLogin;
  private final String storePassword;

  private final Protocol transportProtocol = Protocol.SMTP;
  private final String transportHost;
  private final Integer transportPort;

  private final Long accountId;
  private final Long addressId;

  private final Long draftsFolderId;
  private final Long sentFolderId;
  private final Long trashFolderId;

  private MailFolder rootFolder;

  MailAccount(SimpleRow data) {
    if (data == null) {
      error = "Unknown account";
      storeProtocol = null;
      storeHost = null;
      storePort = null;
      storeLogin = null;
      storePassword = null;

      transportHost = null;
      transportPort = null;

      accountId = null;
      addressId = null;

      draftsFolderId = null;
      sentFolderId = null;
      trashFolderId = null;
    } else {
      storeProtocol = NameUtils.getEnumByName(Protocol.class, data.getValue(COL_STORE_STYPE));
      storeHost = data.getValue(COL_STORE_SERVER);
      storePort = data.getInt(COL_STORE_SPORT);
      storeLogin = BeeUtils.notEmpty(data.getValue(COL_STORE_LOGIN),
          data.getValue(CommonsConstants.COL_EMAIL));
      storePassword = data.getValue(COL_STORE_PASSWORD);

      transportHost = data.getValue(COL_TRANSPORT_SERVER);
      transportPort = data.getInt(COL_TRANSPORT_PORT);

      accountId = data.getLong(COL_ACCOUNT);
      addressId = data.getLong(CommonsConstants.COL_ADDRESS);

      draftsFolderId = data.getLong("DraftsFolder");
      sentFolderId = data.getLong("SentFolder");
      trashFolderId = data.getLong("TrashFolder");
    }
  }

  public Long getAccountId() {
    return accountId;
  }

  public Long getAddressId() {
    return addressId;
  }

  public Long getDraftsFolderId() {
    return draftsFolderId;
  }

  public MailFolder getRootFolder() {
    return rootFolder;
  }

  public Long getSentFolderId() {
    return sentFolderId;
  }

  public String getStoreErrorMessage() {
    String err = error;

    if (BeeUtils.isEmpty(err)) {
      if (storeProtocol == null) {
        err = "Unknown store protocol";

      } else if (BeeUtils.isEmpty(storeHost)) {
        err = "Unknown store host";

      } else if (BeeUtils.isEmpty(storeLogin)) {
        err = "Unknown store login";
      }
    }
    return err;
  }

  public String getStoreHost() {
    return storeHost;
  }

  public String getStoreLogin() {
    return storeLogin;
  }

  public String getStorePassword() {
    return storePassword;
  }

  public int getStorePort() {
    return BeeUtils.isPositive(storePort) ? storePort : -1;
  }

  public Protocol getStoreProtocol() {
    return storeProtocol;
  }

  public String getTransportErrorMessage() {
    String err = error;

    if (BeeUtils.isEmpty(err)) {
      if (transportProtocol == null) {
        err = "Unknown transport protocol";

      } else if (BeeUtils.isEmpty(transportHost)) {
        err = "Unknown transport host";
      }
    }
    return err;
  }

  public String getTransportHost() {
    return transportHost;
  }

  public Integer getTransportPort() {
    return BeeUtils.isPositive(transportPort) ? transportPort : -1;
  }

  public Protocol getTransportProtocol() {
    return transportProtocol;
  }

  public Long getTrashFolderId() {
    return trashFolderId;
  }

  public boolean isValidStoreAccount() {
    return BeeUtils.isEmpty(getStoreErrorMessage());
  }

  public boolean isValidTransportAccount() {
    return BeeUtils.isEmpty(getTransportErrorMessage());
  }

  boolean addMessageToRemoteFolder(MimeMessage message, MailFolder localFolder)
      throws MessagingException {
    Assert.state(Objects.equal(localFolder.getAccountId(), getAccountId()),
        BeeUtils.joinWords("Folder", localFolder.getName(), "Doesn't belong to this account"));

    if (getStoreProtocol() == Protocol.POP3 || !localFolder.isConnected()) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(false);
      folder = getRemoteFolder(store, localFolder);
      folder.appendMessages(new MimeMessage[] {message});

    } finally {
      disconnectFromStore(store);
    }
    return true;
  }

  Store connectToStore(boolean debug) throws MessagingException {
    if (!isValidStoreAccount()) {
      throw new MessagingException(getStoreErrorMessage());
    }
    logger.debug("Connecting to store...");
    Session session = Session.getInstance(new Properties(), null);
    session.setDebug(debug);

    Store store = session.getStore(getStoreProtocol().name().toLowerCase());
    store.connect(getStoreHost(), getStorePort(), getStoreLogin(), getStorePassword());
    return store;
  }

  boolean copyMessagesToRemoteFolder(MailFolder source, long[] uids, MailFolder destination)
      throws MessagingException {
    return processMessages(source, uids, destination, false);
  }

  boolean createRemoteFolder(MailFolder parent, String name) throws MessagingException {
    boolean ok = true;

    if (getStoreProtocol() == Protocol.POP3) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(true);
      folder = getRemoteFolder(store, parent);
      Folder newFolder = folder.getFolder(name);

      if (newFolder.exists()) {
        throw new MessagingException("Folder already exists: " + name);
      }
      logger.debug("Creating folder", name);
      ok = newFolder.create(Folder.HOLDS_MESSAGES);

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  boolean deleteMessagesFromRemoteFolder(MailFolder source, long[] uids) throws MessagingException {
    return processMessages(source, uids, null, true);
  }

  void disconnectFromStore(Store store) {
    if (store != null) {
      try {
        logger.debug("Disconnecting from store...");
        store.close();
      } catch (MessagingException e) {
      }
    }
  }

  boolean dropRemoteFolder(MailFolder source) throws MessagingException {
    boolean ok = true;

    if (getStoreProtocol() == Protocol.POP3 || !source.isConnected()) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(true);
      folder = getRemoteFolder(store, source);

      logger.debug("Removing folder", folder.getName());
      ok = folder.delete(true);

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  Folder getRemoteFolder(Store remoteStore, MailFolder localFolder) throws MessagingException {
    Assert.noNulls(remoteStore, localFolder);
    Folder remoteParent;

    if (localFolder.getParent() != null) {
      remoteParent = getRemoteFolder(remoteStore, localFolder.getParent());
    } else {
      remoteParent = remoteStore.getDefaultFolder();
    }
    logger.debug("Looking for remote folder",
        BeeUtils.join(" in ", localFolder.getName(), remoteParent.getName()));

    Folder remote = remoteParent.getFolder(localFolder.getName());

    if (!remote.exists()) {
      throw new MessagingException("Cannot connect to remote folder: " + localFolder.getName());
    }
    return remote;
  }

  boolean moveMessagesToRemoteFolder(MailFolder source, long[] uids, MailFolder destination)
      throws MessagingException {
    return processMessages(source, uids, destination, true);
  }

  boolean renameRemoteFolder(MailFolder source, String name) throws MessagingException {
    boolean ok = true;

    if (getStoreProtocol() == Protocol.POP3 || !source.isConnected()) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(true);
      folder = getRemoteFolder(store, source);
      Folder newFolder = folder.getParent().getFolder(name);

      if (newFolder.exists()) {
        throw new MessagingException("Folder with new name already exists: " + name);
      }
      logger.debug("Renamng folder", folder.getName(), "to", name);
      ok = folder.renameTo(newFolder);

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  boolean setFlag(MailFolder source, long[] uids, Flag flag, boolean on) throws MessagingException {
    if (getStoreProtocol() == Protocol.POP3 || !source.isConnected()) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(true);
      folder = getRemoteFolder(store, source);

      logger.debug("Checking folder", folder.getName(), "UIDValidity with",
          source.getUidValidity());

      if (!Objects.equal(((UIDFolder) folder).getUIDValidity(), source.getUidValidity())) {
        throw new MessagingException("Folder out of sync: " + source.getName());
      }
      logger.debug("Opening folder", folder.getName());
      folder.open(Folder.READ_WRITE);

      logger.debug("Getting messages from folder", folder.getName(), "by UIDs:", uids);
      Message[] msgs = ((UIDFolder) folder).getMessagesByUID(uids);

      logger.debug("Setting flag for selected messages");
      folder.setFlags(msgs, new Flags(flag), on);

    } finally {
      if (folder != null && folder.isOpen()) {
        try {
          logger.debug("Closing folder:", folder.getName());
          folder.close(false);
        } catch (MessagingException e) {
        }
      }
      disconnectFromStore(store);
    }
    return true;
  }

  void setRootFolder(MailFolder folder) {
    this.rootFolder = folder;
  }

  private boolean processMessages(MailFolder source, long[] uids, MailFolder destination,
      boolean move) throws MessagingException {
    if (getStoreProtocol() == Protocol.POP3 || !source.isConnected()) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(true);
      folder = getRemoteFolder(store, source);

      logger.debug("Checking folder", folder.getName(), "UIDValidity with",
          source.getUidValidity());

      if (!Objects.equal(((UIDFolder) folder).getUIDValidity(), source.getUidValidity())) {
        throw new MessagingException("Folder out of sync: " + source.getName());
      }
      logger.debug("Opening folder", folder.getName());
      folder.open(Folder.READ_WRITE);

      logger.debug("Getting messages from folder", folder.getName(), "by UIDs:", uids);
      Message[] msgs = ((UIDFolder) folder).getMessagesByUID(uids);

      if (destination != null) {
        logger.debug("Copying messages to folder:", destination.getName());
        folder.copyMessages(msgs, getRemoteFolder(store, destination));
      }
      if (move) {
        logger.debug("Deleting seleted messages from folder:", folder.getName());
        folder.setFlags(msgs, new Flags(Flag.DELETED), true);
      }
      logger.debug("Closing folder:", folder.getName());
      folder.close(move);

    } finally {
      if (folder != null && folder.isOpen()) {
        try {
          folder.close(false);
        } catch (MessagingException e) {
        }
      }
      disconnectFromStore(store);
    }
    return true;
  }
}
