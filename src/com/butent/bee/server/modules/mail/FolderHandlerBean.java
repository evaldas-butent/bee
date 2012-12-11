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
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.utils.BeeUtils;

import java.util.Iterator;

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
            .setWhere(sys.idEquals(TBL_FOLDERS, folder.getId())));
      } else {
        qs.updateData(new SqlDelete(TBL_PLACES)
            .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));

        disconnectFolder(folder);
      }
    } else {
      dropped = false;
    }
    return dropped;
  }

  public MailFolder getDraftsFolder(Long accountId) {
    return getFolder(getInboxFolder(accountId), DEFAULT_DRAFTS_FOLDER);
  }

  public MailFolder getFolder(MailFolder parent, String name) {
    Assert.notNull(parent);

    for (MailFolder subFolder : parent.getSubFolders()) {
      if (BeeUtils.same(subFolder.getName(), name)) {
        return subFolder;
      }
    }
    return createFolder(parent.getAccountId(), parent, name, null);
  }

  public MailFolder getInboxFolder(Long accountId) {
    Assert.state(DataUtils.isId(accountId));

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FOLDERS, COL_FOLDER_PARENT, COL_FOLDER_NAME, COL_FOLDER_UID)
        .addField(TBL_FOLDERS, sys.getIdName(TBL_FOLDERS), COL_FOLDER)
        .addFrom(TBL_FOLDERS)
        .setWhere(SqlUtils.equals(TBL_FOLDERS, COL_ACCOUNT, accountId)));

    Multimap<Long, SimpleRow> folders = HashMultimap.create();
    MailFolder inbox = null;

    for (SimpleRow row : data) {
      Long parent = row.getLong(COL_FOLDER_PARENT);

      if (parent == null) {
        Assert.isNull(inbox, "Account allows only single INBOX folder");

        inbox = new MailFolder(accountId, null, row.getLong(COL_FOLDER),
            row.getValue(COL_FOLDER_NAME), row.getLong(COL_FOLDER_UID));
      } else {
        folders.put(parent, row);
      }
    }
    if (inbox == null) {
      inbox = createFolder(accountId, null, DEFAULT_INBOX_FOLDER, null);
    } else {
      createTree(inbox, folders);
    }
    return inbox;
  }

  public long getLastStoredUID(MailFolder folder) {
    Assert.notNull(folder);

    Long uid = qs.getLong(new SqlSelect()
        .addMax(TBL_PLACES, COL_MESSAGE_UID)
        .addFrom(TBL_PLACES)
        .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));

    return BeeUtils.unbox(uid);
  }

  public MailFolder getSentFolder(Long accountId) {
    return getFolder(getInboxFolder(accountId), DEFAULT_SENT_FOLDER);
  }

  public MailFolder getTrashFolder(Long accountId) {
    return getFolder(getInboxFolder(accountId), DEFAULT_TRASH_FOLDER);
  }

  public void syncFolder(MailFolder folder, Long uidValidity) {
    Assert.notNull(folder);

    if (!Objects.equal(uidValidity, folder.getUidValidity())) {
      if (Objects.equal(uidValidity, MailFolder.DISCONNECTED_MODE)) {
        qs.updateData(new SqlUpdate(TBL_PLACES)
            .addConstant(COL_MESSAGE_UID, null)
            .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));
      } else {
        qs.updateData(new SqlDelete(TBL_PLACES)
            .setWhere(SqlUtils.equals(TBL_PLACES, COL_FOLDER, folder.getId())));
      }
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

    return new MailFolder(accountId, parent, id, name, folderUID);
  }

  private void createTree(MailFolder parent, Multimap<Long, SimpleRow> folders) {
    for (SimpleRow row : folders.get(parent.getId())) {
      MailFolder folder = new MailFolder(parent.getAccountId(), parent, row.getLong(COL_FOLDER),
          row.getValue(COL_FOLDER_NAME), row.getLong(COL_FOLDER_UID));

      createTree(folder, folders);
      parent.addSubFolder(folder);
    }
  }
}
