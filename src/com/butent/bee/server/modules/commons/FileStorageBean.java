package com.butent.bee.server.modules.commons;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.io.FileNameUtils;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class FileStorageBean {

  private static final BeeLogger logger = LogUtils.getLogger(FileStorageBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  private final Object lock = new Object();

  public List<StoredFile> getFiles() {
    List<StoredFile> files = Lists.newArrayList();

    String idName = sys.getIdName(TBL_FILES);
    String versionName = sys.getVersionName(TBL_FILES);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FILES, idName, versionName, COL_FILE_NAME, COL_FILE_SIZE, COL_FILE_TYPE)
        .addFrom(TBL_FILES)
        .addOrder(TBL_FILES, versionName));

    for (Map<String, String> row : data) {
      StoredFile sf = new StoredFile(BeeUtils.toLong(row.get(idName)),
          row.get(COL_FILE_NAME), BeeUtils.toLong(row.get(COL_FILE_SIZE)), row.get(COL_FILE_TYPE));

      sf.setFileDate(DateTime.restore(row.get(versionName)));

      sf.setIcon(FileNameUtils.getExtensionIcon(sf.getName()));
      files.add(sf);
    }
    return files;
  }

  public Long storeFile(InputStream is, String fileName, String mimeType) throws IOException {
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new BeeRuntimeException(e);
    }
    JustDate dt = new JustDate();
    File tmp = new File(Config.REPOSITORY_DIR,
        BeeUtils.join(File.separator, dt.getYear(), dt.getMonth(), dt.getDom()));
    tmp.mkdirs();
    tmp = new File(tmp, "bee_" + BeeUtils.randomString(30) + ".tmp");
    tmp.deleteOnExit();
    OutputStream out = new DigestOutputStream(new FileOutputStream(tmp), md);

    byte buffer[] = new byte[8 * 1024];
    int bytesRead;

    try {
      while ((bytesRead = is.read(buffer)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
    } finally {
      out.close();
    }
    String hash = Codec.toHex(md.digest());

    String idName = sys.getIdName(TBL_FILES);
    Long id = null;
    File target = new File(tmp.getParentFile(), hash);

    synchronized (lock) {
      Map<String, String> data = qs.getRow(new SqlSelect()
          .addFields(TBL_FILES, COL_FILE_REPO, idName)
          .addFrom(TBL_FILES)
          .setWhere(SqlUtils.equal(TBL_FILES, COL_FILE_HASH, hash)));

      if (data != null) {
        id = BeeUtils.toLong(data.get(idName));
        File oldTarget = new File(data.get(COL_FILE_REPO));

        if (oldTarget.exists()) {
          target = oldTarget;
        } else {
          qs.updateData(new SqlUpdate(TBL_FILES)
              .addConstant(COL_FILE_REPO, target.getPath())
              .setWhere(SqlUtils.equal(TBL_FILES, idName, id)));
        }
      }
      if (target.exists()) {
        tmp.delete();

        if (id == null) {
          logger.warning("File already exists:", target.getPath());
        }
      } else if (!tmp.renameTo(target)) {
        tmp.delete();
        throw new BeeRuntimeException(BeeUtils.joinWords("Error renaming file:",
            tmp.getPath(), "to:", target.getPath()));
      }
      if (id == null) {
        id = qs.insertData(new SqlInsert(TBL_FILES)
            .addConstant(COL_FILE_HASH, hash)
            .addConstant(COL_FILE_REPO, target.getPath())
            .addConstant(COL_FILE_NAME, fileName)
            .addConstant(COL_FILE_SIZE, target.length())
            .addConstant(COL_FILE_TYPE, mimeType));
      }
    }
    return id;
  }
}
