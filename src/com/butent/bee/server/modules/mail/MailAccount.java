package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  private final boolean storeSSL;

  private final Protocol transportProtocol = Protocol.SMTP;
  private final String transportHost;
  private final Integer transportPort;
  private final String transportLogin;
  private final String transportPassword;
  private final boolean transportSSL;

  private final Long accountId;
  private final Long addressId;

  private final Map<SystemFolder, Long> sysFolders = Maps.newHashMap();

  private MailFolder rootFolder;

  MailAccount(SimpleRow data) {
    if (data == null) {
      error = "Unknown account";
      storeProtocol = null;
      storeHost = null;
      storePort = null;
      storeLogin = null;
      storePassword = null;
      storeSSL = false;

      transportHost = null;
      transportPort = null;
      transportLogin = null;
      transportPassword = null;
      transportSSL = false;

      accountId = null;
      addressId = null;
    } else {
      storeProtocol = NameUtils.getEnumByName(Protocol.class, data.getValue(COL_STORE_STYPE));
      storeHost = data.getValue(COL_STORE_SERVER);
      storePort = data.getInt(COL_STORE_SPORT);
      storeLogin = BeeUtils.notEmpty(data.getValue(COL_STORE_LOGIN),
          data.getValue(CommonsConstants.COL_EMAIL));
      storePassword = BeeUtils.isEmpty(data.getValue(COL_STORE_PASSWORD)) ? null :
          Codec.decodeBase64(data.getValue(COL_STORE_PASSWORD));
      storeSSL = BeeUtils.isTrue(data.getBoolean(COL_STORE_SSL));

      transportHost = data.getValue(COL_TRANSPORT_SERVER);
      transportPort = data.getInt(COL_TRANSPORT_PORT);
      transportLogin = BeeUtils.notEmpty(data.getValue(COL_TRANSPORT_LOGIN),
          data.getValue(CommonsConstants.COL_EMAIL));
      transportPassword = BeeUtils.isEmpty(data.getValue(COL_TRANSPORT_PASSWORD)) ? null :
          Codec.decodeBase64(data.getValue(COL_TRANSPORT_PASSWORD));
      transportSSL = BeeUtils.isTrue(data.getBoolean(COL_TRANSPORT_SSL));

      accountId = data.getLong(COL_ACCOUNT);
      addressId = data.getLong(CommonsConstants.COL_ADDRESS);

      for (SystemFolder sysFolder : SystemFolder.values()) {
        sysFolders.put(sysFolder, data.getLong(sysFolder.name() + COL_FOLDER));
      }
    }
  }

  public Long getAccountId() {
    return accountId;
  }

  public Long getAddressId() {
    return addressId;
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

  public Long getSysFolderId(SystemFolder sysFolder) {
    return sysFolders.get(sysFolder);
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

  public boolean isStoredRemotedly(MailFolder folder) {
    Assert.notNull(folder);
    return (getStoreProtocol() == Protocol.IMAP) && folder.isConnected();
  }

  public boolean isStoreSSL() {
    return storeSSL;
  }

  public boolean isTransportSSL() {
    return transportSSL;
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

    String protocol = getStoreProtocol().name().toLowerCase();
    Properties props = new Properties();

    if (isStoreSSL()) {
      props.put("mail." + protocol + ".ssl.enable", "true");
    }
    Session session = Session.getInstance(props, null);
    session.setDebug(logger.isDebugEnabled());

    Store store = session.getStore(protocol);
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

      if (isTransportSSL()) {
        props.put("mail." + protocol + ".ssl.enable", "true");
      } else {
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

  boolean createRemoteFolder(MailFolder parent, String name, boolean acceptExisting)
      throws MessagingException {
    boolean ok = true;

    if (!isStoredRemotedly(parent)) {
      return ok;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore();
      folder = getRemoteFolder(store, parent);
      Folder newFolder = folder.getFolder(name);

      if (checkNewFolderName(newFolder, name, acceptExisting)) {
        logger.debug("Creating folder:", name);
        ok = newFolder.create(Folder.HOLDS_MESSAGES);
      }
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

      logger.debug("Removing folder:", folder.getName());
      ok = folder.delete(true) || !folder.exists();

    } finally {
      disconnectFromStore(store);
    }
    return ok;
  }

  Folder getRemoteFolder(Store remoteStore, MailFolder localFolder) throws MessagingException {
    Assert.noNulls(remoteStore, localFolder);

    if (localFolder.getParent() == null) {
      logger.debug("Looking for root folder");
      return remoteStore.getDefaultFolder();
    }
    Folder remoteParent = getRemoteFolder(remoteStore, localFolder.getParent());

    String name = localFolder.getName();
    logger.debug("Looking for remote folder", BeeUtils.join(" in ", name, remoteParent.getName()));

    Folder remote = remoteParent.getFolder(name);

    if (!remote.exists()) {
      throw new MessagingException("Remote folder does not exist: " + name);
    }
    return remote;
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
      logger.debug("Opening folder:", remoteSource.getName());
      remoteSource.open(Folder.READ_WRITE);

      List<Message> messages = getMessageReferences(remoteSource, uids);

      if (isTarget) {
        Folder remoteTarget = getRemoteFolder(store, target);

        if (!holdsMessages(remoteTarget)) {
          throw new MessagingException(BeeUtils.joinWords("Folder",
              BeeUtils.bracket(target.getName()), "cannot hold messages"));
        }
        logger.debug("Copying messages to folder:", target.getName());
        remoteSource.copyMessages(messages.toArray(new Message[0]), remoteTarget);
      }
      if (move) {
        for (Iterator<Message> iterator = messages.iterator(); iterator.hasNext();) {
          Message message = iterator.next();

          if (message.isExpunged()) {
            iterator.remove();
          }
        }
        if (!BeeUtils.isEmpty(messages)) {
          logger.debug("Deleting selected messages from folder:", remoteSource.getName());
          remoteSource.setFlags(messages.toArray(new Message[0]), new Flags(Flag.DELETED), true);
        } else {
          logger.debug("All messages already expunged, no delete required");
        }
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

      checkNewFolderName(newFolder, name, false);

      logger.debug("Renamng folder:", folder.getName(), "->", name);
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
      logger.debug("Opening folder:", folder.getName());
      folder.open(Folder.READ_WRITE);

      List<Message> messages = getMessageReferences(folder, uids);

      logger.debug(on ? "Setting" : "Clearing", "flag for selected messages");
      folder.setFlags(messages.toArray(new Message[0]), new Flags(flag), on);

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

  void setSysFolderId(SystemFolder sysFolder, Long id) {
    sysFolders.put(sysFolder, id);
  }

  private boolean checkNewFolderName(Folder newFolder, String name, boolean acceptExisting)
      throws MessagingException {
    if (name.indexOf(newFolder.getSeparator()) >= 0) {
      throw new MessagingException("Invalid folder name: " + name);
    }
    logger.debug("Checking, if folder exists:", name);

    if (newFolder.exists()) {
      if (acceptExisting) {
        return false;
      }
      throw new MessagingException("Folder with new name already exists: " + name);
    }
    return true;
  }

  private List<Message> getMessageReferences(Folder remoteSource, long[] uids)
      throws MessagingException {

    logger.debug("Getting messages from folder", remoteSource.getName(), "by UIDs:", uids);
    Message[] msgs = ((UIDFolder) remoteSource).getMessagesByUID(uids);

    for (Message message : msgs) {
      if (message == null) {
        throw new MessagingException("Not all messages where returned by UIDs. " +
            "Folder resynchronization required.");
      }
    }
    return Lists.newArrayList(msgs);
  }
}
