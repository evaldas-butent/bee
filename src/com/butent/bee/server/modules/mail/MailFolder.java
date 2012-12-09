package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

class MailFolder {

  public static final long DISCONNECTED_MODE = -1;

  private final Long id;
  private final String name;
  private Long uidValidity;

  private final Map<String, MailFolder> childs = Maps.newHashMap();

  public MailFolder(Long id, String name, Long uidValidity) {
    this.id = id;
    this.name = BeeUtils.normalize(name);
    this.uidValidity = uidValidity;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
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
