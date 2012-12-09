package com.butent.bee.server.modules.mail;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import static com.butent.bee.shared.modules.mail.MailConstants.*;

import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlDelete;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class FolderHandlerBean {

  public static final String DEFAULT_INBOX_FOLDER = "INBOX";
  public static final String DEFAULT_SENT_FOLDER = "Sent Messages";
  public static final String DEFAULT_DRAFTS_FOLDER = "Drafts";
  public static final String DEFAULT_TRASH_FOLDER = "Deleted Messages";

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public void connectFolder(MailFolder folder) {
    syncFolder(folder, null);
  }

  public MailFolder createFolder(Long accountId, Long parentId, String name, Long uidValidity) {
    Assert.state(DataUtils.isId(accountId));
    Assert.notEmpty(name);

    long id = qs.insertData(new SqlInsert(TBL_FOLDERS)
        .addConstant(COL_ACCOUNT, accountId)
        .addConstant(COL_FOLDER_PARENT, parentId)
        .addConstant(COL_FOLDER_NAME, name)
        .addConstant(COL_FOLDER_UID, uidValidity));

    return new MailFolder(id, name, uidValidity);
  }

  public void disconnectFolder(MailFolder folder) {
    syncFolder(folder, MailFolder.DISCONNECTED_MODE);
  }

  public boolean dropFolder(MailFolder folder) {
    Assert.notNull(folder);
    boolean dropped = true;

    for (Iterator<MailFolder> iterator = folder.getSubFolders().iterator(); iterator.hasNext();) {
      if (dropFolder(iterator.next())) {
        iterator.remove();
      } else {
        dropped = false;
      }
    }
    if (folder.isConnected()) {
      if (dropped) {
        qs.updateData(new SqlDelete(TBL_FOLDERS)
            .setWhere(SqlUtils.equal(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), folder.getId())));
      } else {
        qs.updateData(new SqlDelete(TBL_PLACES)
            .setWhere(SqlUtils.equal(TBL_PLACES, COL_FOLDER, folder.getId())));

        disconnectFolder(folder);
      }
    } else {
      dropped = false;
    }
    return dropped;
  }

  public Long getInboxFolder(Long accountId) {
    Assert.state(DataUtils.isId(accountId));

    Long folderId = qs.getLong(new SqlSelect()
        .addFields(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS))
        .addFrom(TBL_FOLDERS)
        .setWhere(SqlUtils.and(SqlUtils.isNull(TBL_FOLDERS, COL_FOLDER_PARENT),
            SqlUtils.equal(TBL_FOLDERS, COL_ACCOUNT, accountId),
            SqlUtils.equal(TBL_FOLDERS, COL_FOLDER_NAME, DEFAULT_INBOX_FOLDER))));

    if (!DataUtils.isId(folderId)) {
      folderId = qs.insertData(new SqlInsert(TBL_FOLDERS)
          .addConstant(COL_ACCOUNT, accountId)
          .addConstant(COL_FOLDER_NAME, DEFAULT_INBOX_FOLDER));
    }
    return folderId;
  }

  public long getLastStoredUID(MailFolder folder) {
    Assert.notNull(folder);

    Long uid = qs.getLong(new SqlSelect()
        .addMax(TBL_PLACES, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(SqlUtils.equal(TBL_PLACES, COL_FOLDER, folder.getId())));

    return BeeUtils.unbox(uid);
  }

  public MailFolder getRootFolder(Long accountId) {
    Assert.state(DataUtils.isId(accountId));

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FOLDERS, COL_FOLDER_PARENT, COL_FOLDER_NAME, COL_FOLDER_UID)
        .addField(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), COL_FOLDER)
        .addFrom(TBL_FOLDERS)
        .setWhere(SqlUtils.equal(TBL_FOLDERS, COL_ACCOUNT, accountId)));

    Multimap<Long, Map<String, String>> folders = HashMultimap.create();

    for (int i = 0; i < data.getNumberOfRows(); i++) {
      folders.put(data.getLong(i, COL_FOLDER_PARENT), data.getRow(i));
    }
    MailFolder rootFolder = new MailFolder(null, null, null);
    createTree(rootFolder, folders);

    return rootFolder;
  }

  public void syncFolder(MailFolder folder, Long uidValidity) {
    Assert.notNull(folder);

    if (!Objects.equal(uidValidity, folder.getUidValidity())) {
      if (Objects.equal(uidValidity, MailFolder.DISCONNECTED_MODE)) {
        qs.updateData(new SqlUpdate(TBL_PLACES)
            .addConstant(COL_MESSAGE_UID, null)
            .setWhere(SqlUtils.equal(TBL_PLACES, COL_FOLDER, folder.getId())));
      } else {
        qs.updateData(new SqlDelete(TBL_PLACES)
            .setWhere(SqlUtils.equal(TBL_PLACES, COL_FOLDER, folder.getId())));
      }
      qs.updateData(new SqlUpdate(TBL_FOLDERS)
          .addConstant(COL_FOLDER_UID, uidValidity)
          .setWhere(SqlUtils.equal(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), folder.getId())));

      folder.setUidValidity(uidValidity);
    }
  }

  private void createTree(MailFolder parent, Multimap<Long, Map<String, String>> folders) {
    for (Map<String, String> row : folders.get(parent.getId())) {
      MailFolder folder = new MailFolder(BeeUtils.toLongOrNull(row.get(COL_FOLDER)),
          row.get(COL_FOLDER_NAME), BeeUtils.toLongOrNull(row.get(COL_FOLDER_UID)));

      createTree(folder, folders);
      parent.addSubFolder(folder);
    }
  }
}
