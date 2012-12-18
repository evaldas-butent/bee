package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

public class MailFolder {

  public static final long DISCONNECTED_MODE = -1;

  private final long accountId;
  private final MailFolder parent;
  private final long id;
  private final String name;
  private Long uidValidity;

  private final Map<String, MailFolder> childs = Maps.newHashMap();

  MailFolder(long accountId, MailFolder parent, long id, String name, Long uidValidity) {
    Assert.state(DataUtils.isId(accountId));
    Assert.state(DataUtils.isId(id));
    Assert.notEmpty(name);

    this.accountId = accountId;
    this.parent = parent;
    this.id = id;
    this.name = name;
    this.uidValidity = uidValidity;
  }

  public long getAccountId() {
    return accountId;
  }

  public long getId() {
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

  void addSubFolder(MailFolder subFolder) {
    childs.put(BeeUtils.normalize(subFolder.getName()), subFolder);
  }

  MailFolder removeSubFolder(String subFolderName) {
    return childs.remove(BeeUtils.normalize(subFolderName));
  }

  void setUidValidity(Long uidValidity) {
    this.uidValidity = uidValidity;
  }
}
