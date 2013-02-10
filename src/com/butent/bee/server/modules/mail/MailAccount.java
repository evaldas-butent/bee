package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.modules.mail.MailConstants.SystemFolder;
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
import javax.mail.Transport;
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

  private final Protocol transportProtocol;
  private final String transportHost;
  private final Integer transportPort;
  private final String transportLogin;
  private final String transportPassword;

  private final Long accountId;
  private final Long addressId;

  private final Long inboxFolderId;
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

      transportProtocol = null;
      transportHost = null;
      transportPort = null;
      transportLogin = null;
      transportPassword = null;

      accountId = null;
      addressId = null;

      inboxFolderId = null;
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

      transportProtocol = BeeUtils.isTrue(data.getBoolean(COL_TRANSPORT_SSL))
          ? Protocol.SMTPS : Protocol.SMTP;
      transportHost = data.getValue(COL_TRANSPORT_SERVER);
      transportPort = data.getInt(COL_TRANSPORT_PORT);
      transportLogin = BeeUtils.notEmpty(data.getValue(COL_TRANSPORT_LOGIN),
          data.getValue(CommonsConstants.COL_EMAIL));
      transportPassword = data.getValue(COL_TRANSPORT_PASSWORD);

      accountId = data.getLong(COL_ACCOUNT);
      addressId = data.getLong(CommonsConstants.COL_ADDRESS);

      inboxFolderId = data.getLong(SystemFolder.Inbox.name() + COL_FOLDER);
      draftsFolderId = data.getLong(SystemFolder.Drafts.name() + COL_FOLDER);
      sentFolderId = data.getLong(SystemFolder.Sent.name() + COL_FOLDER);
      trashFolderId = data.getLong(SystemFolder.Trash.name() + COL_FOLDER);
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

  public Long getInboxFolderId() {
    return inboxFolderId;
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

  public String getTransportLogin() {
    return transportLogin;
  }

  public String getTransportPassword() {
    return transportPassword;
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

  public boolean isStoredRemotedly(MailFolder folder) {
    Assert.notNull(folder);

    return (getStoreProtocol() == Protocol.IMAP || getStoreProtocol() == Protocol.IMAPS)
        && folder.isConnected();
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

    if (!isStoredRemotedly(localFolder)) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
      folder = getRemoteFolder(store, localFolder);
      folder.appendMessages(new MimeMessage[] {message});

    } finally {
      disconnectFromStore(store);
    }
    return true;
  }

  Store connectToStore() throws MessagingException {
    if (!isValidStoreAccount()) {
      throw new MessagingException(getStoreErrorMessage());
    }
    logger.debug("Connecting to store...");
    Session session = Session.getInstance(new Properties(), null);
    session.setDebug(logger.isDebugEnabled());

    Store store = session.getStore(getStoreProtocol().name().toLowerCase());
    store.connect(getStoreHost(), getStorePort(), getStoreLogin(), getStorePassword());
    return store;
  }

  Transport connectToTransport() throws MessagingException {
    if (!isValidTransportAccount()) {
      throw new MessagingException(getTransportErrorMessage());
    }
    logger.debug("Connecting to transport...");

    String protocol = getTransportProtocol().name().toLowerCase();
    Properties props = new Properties();

    if (!BeeUtils.isEmpty(getTransportPassword())) {
      props.put("mail." + protocol + ".auth", "true");

      if (getTransportProtocol() == Protocol.SMTP) {
        props.put("mail." + protocol + ".starttls.enable", "true");
      }
    }
    Session session = Session.getInstance(props, null);
    session.setDebug(logger.isDebugEnabled());

    Transport transport = session.getTransport(protocol);
    transport.connect(getTransportHost(), getTransportPort(), getTransportLogin(),
        getTransportPassword());
    return transport;
  }

  boolean createRemoteFolder(MailFolder parent, String name) throws MessagingException {
    boolean ok = true;

    if (!isStoredRemotedly(parent)) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
      folder = getRemoteFolder(store, parent, true);
      Folder newFolder = folder.getFolder(name);

      checkNewFolderName(newFolder, name);

      logger.debug("Creating folder", name);
      ok = newFolder.create(Folder.HOLDS_MESSAGES);

      if (ok) {
        newFolder.setSubscribed(true);
      }
    } finally {
      disconnectFromStore(store);
    }
    return ok;
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

    if (!isStoredRemotedly(source)) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
      folder = getRemoteFolder(store, source);

      logger.debug("Removing folder", folder.getName());
      ok = folder.delete(true) || !folder.exists();

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  Folder getRemoteFolder(Store remoteStore, MailFolder localFolder) throws MessagingException {
    return getRemoteFolder(remoteStore, localFolder, false);
  }

  MailFolder getRootFolder() {
    return rootFolder;
  }

  boolean holdsFolders(Folder remoteFolder) throws MessagingException {
    return (remoteFolder.getType() & Folder.HOLDS_FOLDERS) != 0;
  }

  boolean holdsMessages(Folder remoteFolder) throws MessagingException {
    return (remoteFolder.getType() & Folder.HOLDS_MESSAGES) != 0;
  }

  void processMessages(long[] uids, MailFolder source, MailFolder target, boolean move)
      throws MessagingException {

    if (!isStoredRemotedly(source)) {
      return;
    }
    boolean isTarget = (target != null && target.isConnected());

    if (!move && !isTarget) {
      return;
    }
    Store store = null;
    Folder remoteSource = null;

    try {
      store = connectToStore();
      remoteSource = getRemoteFolder(store, source);

      logger.debug("Checking folder", remoteSource.getName(), "UIDValidity with",
          source.getUidValidity());

      if (!Objects.equal(((UIDFolder) remoteSource).getUIDValidity(), source.getUidValidity())) {
        throw new MessagingException("Folder out of sync: " + source.getName());
      }
      logger.debug("Opening folder", remoteSource.getName());
      remoteSource.open(Folder.READ_WRITE);

      logger.debug("Getting messages from folder", remoteSource.getName(), "by UIDs:", uids);
      Message[] msgs = ((UIDFolder) remoteSource).getMessagesByUID(uids);

      if (isTarget) {
        Folder remoteTarget = getRemoteFolder(store, target);

        if (!holdsMessages(remoteTarget)) {
          throw new MessagingException(BeeUtils.joinWords("Folder",
              BeeUtils.bracket(target.getName()), "cannot hold messages"));
        }
        logger.debug("Copying messages to folder:", target.getName());
        remoteSource.copyMessages(msgs, remoteTarget);
      }
      if (move) {
        logger.debug("Deleting seleted messages from folder:", remoteSource.getName());
        remoteSource.setFlags(msgs, new Flags(Flag.DELETED), true);
      }
      logger.debug("Closing folder:", remoteSource.getName());
      remoteSource.close(move);

    } finally {
      if (remoteSource != null && remoteSource.isOpen()) {
        try {
          remoteSource.close(false);
        } catch (MessagingException e) {
        }
      }
      disconnectFromStore(store);
    }
  }

  boolean renameRemoteFolder(MailFolder source, String name) throws MessagingException {
    boolean ok = true;

    if (!isStoredRemotedly(source)) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
      folder = getRemoteFolder(store, source);
      Folder newFolder = folder.getParent().getFolder(name);

      checkNewFolderName(newFolder, name);

      logger.debug("Renamng folder", folder.getName(), "to", name);
      ok = folder.renameTo(newFolder);

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  boolean setFlag(MailFolder source, long[] uids, Flag flag, boolean on) throws MessagingException {
    if (!isStoredRemotedly(source)) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
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

  private void checkNewFolderName(Folder newFolder, String name) throws MessagingException {
    if (name.indexOf(newFolder.getSeparator()) >= 0) {
      throw new MessagingException("Invalid folder name: " + name);
    }
    if (newFolder.exists()) {
      throw new MessagingException("Folder with new name already exists: " + name);
    }
  }

  private Folder getRemoteFolder(Store remoteStore, MailFolder localFolder, boolean createParents)
      throws MessagingException {
    Assert.noNulls(remoteStore, localFolder);

    if (localFolder.getParent() == null) {
      return remoteStore.getDefaultFolder();
    }
    Folder remoteParent = getRemoteFolder(remoteStore, localFolder.getParent(), createParents);

    String name = localFolder.getName();
    logger.debug("Looking for remote folder", BeeUtils.join(" in ", name, remoteParent.getName()));

    Folder remote = remoteParent.getFolder(name);

    if (!remote.exists()) {
      if (createParents) {
        logger.debug("Creating parent folder", name);

        checkNewFolderName(remote, name);

        if (!remote.create(Folder.HOLDS_MESSAGES)) {
          throw new MessagingException("Can't create parent folder: " + name);
        } else {
          remote.setSubscribed(true);
        }
      } else {
        throw new MessagingException("Remote folder does not exist: " + name);
      }
    }
    return remote;
  }
}
