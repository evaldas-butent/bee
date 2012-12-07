package com.butent.bee.server.modules.mail;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Collection;
import java.util.Map;

class MailFolder {

  public static MailFolder create(SimpleRowSet data) {
    Multimap<Long, Map<String, String>> folders = HashMultimap.create();

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      folders.put(data.getLong(i, COL_FOLDER_PARENT), data.getRow(i));
    }
    MailFolder rootFolder = new MailFolder(null, null, null);
    createTree(rootFolder, folders);

    return rootFolder;
  }

  private static void createTree(MailFolder parent, Multimap<Long, Map<String, String>> folders) {
    for (Map<String, String> row : folders.get(parent.getId())) {
      MailFolder folder = new MailFolder(BeeUtils.toLongOrNull(row.get(COL_UNIQUE_ID)),
          row.get(COL_FOLDER_NAME), BeeUtils.toLongOrNull(row.get(COL_FOLDER_UID)));

      createTree(folder, folders);
      parent.addSubFolder(folder);
    }
  }

  private final Long id;
  private final String name;
  private final Long uidValidity;

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

  MailFolder removeSubFolder(String subFolderName) {
    return childs.remove(BeeUtils.normalize(subFolderName));
  }

  private void addSubFolder(MailFolder subFolder) {
    childs.put(BeeUtils.normalize(subFolder.getName()), subFolder);
  }
}
