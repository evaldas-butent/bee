package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.modules.commons.CommonsConstants;
import com.butent.bee.shared.modules.mail.MailConstants.Protocol;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.NameUtils;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

public class MailAccount {

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

    if (getStoreProtocol() == Protocol.POP3) {
      return false;
    }
    Store store = null;
    Folder folder = null;

    try {
      store = connectToStore(false);
      folder = getRemoteFolder(store, localFolder);

      if (folder == null) {
        throw new MessagingException("Cannot connect to remote folder: " + localFolder.getName());
      }
      folder.appendMessages(new MimeMessage[] {message});

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

  Store connectToStore(boolean debug) throws MessagingException {
    if (!isValidStoreAccount()) {
      throw new MessagingException(getStoreErrorMessage());
    }
    Session session = Session.getInstance(new Properties(), null);
    session.setDebug(debug);

    Store store = session.getStore(getStoreProtocol().name().toLowerCase());
    store.connect(getStoreHost(), getStorePort(), getStoreLogin(), getStorePassword());
    return store;
  }

  void disconnectFromStore(Store store) {
    if (store != null) {
      try {
        store.close();
      } catch (MessagingException e) {
      }
    }
  }

  Folder getRemoteFolder(Store remoteStore, MailFolder localFolder) throws MessagingException {
    Assert.noNulls(remoteStore, localFolder);
    Folder remoteParent;

    if (localFolder.getParent() != null) {
      remoteParent = getRemoteFolder(remoteStore, localFolder.getParent());
    } else {
      remoteParent = remoteStore.getDefaultFolder();
    }
    Assert.notNull(remoteParent);
    Folder remote = remoteParent.getFolder(localFolder.getName());

    if (!remote.exists() && !remote.create(Folder.HOLDS_FOLDERS & Folder.HOLDS_MESSAGES)) {
      remote = null;
    }
    return remote;
  }
}
