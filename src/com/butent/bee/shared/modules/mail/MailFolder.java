package com.butent.bee.shared.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeSerializable;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.util.Collection;
import java.util.Map;

public class MailFolder implements BeeSerializable {

  private static final long DISCONNECTED_MODE = -1;

  public static MailFolder restore(String s) {
    MailFolder folder = new MailFolder();
    folder.deserialize(s);
    Assert.state(DataUtils.isId(folder.getAccountId()));
    return folder;
  }

  private enum Serial {
    ACCOUNT, ID, NAME, UID, CHILDS
  }

  private long accountId;
  private MailFolder parent;
  private Long id;
  private String name;
  private Long uidValidity;

  private final Map<String, MailFolder> childs = Maps.newLinkedHashMap();

  public MailFolder(long accountId, MailFolder parent, Long id, String name, Long uidValidity) {
    Assert.state(DataUtils.isId(accountId));

    this.accountId = accountId;
    this.parent = parent;
    this.id = id;
    this.name = name;
    this.uidValidity = uidValidity;
  }

  private MailFolder() {
  }

  public void addSubFolder(MailFolder subFolder) {
    childs.put(BeeUtils.normalize(subFolder.getName()), subFolder);
  }

  @Override
  public void deserialize(String s) {
    String[] arr = Codec.beeDeserializeCollection(s);
    Serial[] members = Serial.values();
    Assert.lengthEquals(arr, members.length);

    for (int i = 0; i < members.length; i++) {
      Serial member = members[i];
      String value = arr[i];

      switch (member) {
        case ACCOUNT:
          accountId = BeeUtils.toLong(value);
          break;
        case CHILDS:
          String[] data = Codec.beeDeserializeCollection(value);

          if (!ArrayUtils.isEmpty(data)) {
            for (int j = 0; j < data.length; j += 2) {
              MailFolder child = restore(data[j + 1]);
              child.parent = this;
              childs.put(data[j], child);
            }
          }
          break;
        case ID:
          id = BeeUtils.toLongOrNull(value);
          break;
        case NAME:
          name = value;
          break;
        case UID:
          uidValidity = BeeUtils.toLongOrNull(value);
          break;
      }
    }
  }

  public void disconnect() {
    setUidValidity(DISCONNECTED_MODE);
  }

  public MailFolder findFolder(Long folderId) {
    if (Objects.equal(getId(), folderId)) {
      return this;
    }
    for (MailFolder sub : getSubFolders()) {
      MailFolder subFolder = sub.findFolder(folderId);

      if (subFolder != null) {
        return subFolder;
      }
    }
    return null;
  }

  public long getAccountId() {
    return accountId;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public MailFolder getParent() {
    return parent;
  }

  public Collection<MailFolder> getSubFolders() {
    return childs.values();
  }

  public Long getUidValidity() {
    return uidValidity;
  }

  public boolean isConnected() {
    return !Objects.equal(uidValidity, DISCONNECTED_MODE);
  }

  public MailFolder removeSubFolder(String subFolderName) {
    return childs.remove(BeeUtils.normalize(subFolderName));
  }

  @Override
  public String serialize() {
    Serial[] members = Serial.values();
    Object[] arr = new Object[members.length];
    int i = 0;

    for (Serial member : members) {
      switch (member) {
        case ACCOUNT:
          arr[i++] = accountId;
          break;
        case CHILDS:
          arr[i++] = childs;
          break;
        case ID:
          arr[i++] = id;
          break;
        case NAME:
          arr[i++] = name;
          break;
        case UID:
          arr[i++] = uidValidity;
          break;
      }
    }
    return Codec.beeSerialize(arr);
  }

  public void setUidValidity(Long uidValidity) {
    this.uidValidity = uidValidity;
  }
}
