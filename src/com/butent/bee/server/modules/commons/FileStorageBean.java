package com.butent.bee.server.modules.commons;

import com.google.common.collect.Lists;

import static com.butent.bee.shared.modules.commons.CommonsConstants.*;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogLevel;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;

@Singleton
@Lock(LockType.READ)
public class FileStorageBean {

  private static final BeeLogger logger = LogUtils.getLogger(FileStorageBean.class);
  private static BeeLogger messyLogger = LogUtils.getLogger(QueryServiceBean.class);

  private static final int BUFFER_SIZE = 8192;

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  private final Object lock = new Object();
  private File repositoryDir;

  public boolean deletePhoto(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return false;
    }

    File file = new File(getPhotoDir(), BeeUtils.trim(fileName));
    return file.exists() && file.delete();
  }

  public StoredFile getFile(Long fileId) throws IOException {
    Assert.notNull(fileId);

    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_FILES, COL_FILE_REPO, COL_FILE_NAME, COL_FILE_SIZE, COL_FILE_TYPE)
        .addFrom(TBL_FILES)
        .setWhere(sys.idEquals(TBL_FILES, fileId)));

    if (row == null) {
      throw new IOException("File not found: id =" + fileId);
    }
    StoredFile storedFile = new StoredFile(fileId, row.getValue(COL_FILE_NAME),
        row.getLong(COL_FILE_SIZE), row.getValue(COL_FILE_TYPE));

    String repo = row.getValue(COL_FILE_REPO);

    if (BeeUtils.isEmpty(repo)) {
      SqlSelect query = new SqlSelect().setLimit(10)
          .addFields(TBL_FILE_PARTS, COL_FILE_PART)
          .addFrom(TBL_FILE_PARTS)
          .setWhere(SqlUtils.equals(TBL_FILE_PARTS, COL_FILE_FILE, fileId))
          .addOrder(TBL_FILE_PARTS, sys.getIdName(TBL_FILE_PARTS));

      File tmp = File.createTempFile("bee_", null);
      tmp.deleteOnExit();
      OutputStream out = new FileOutputStream(tmp);
      SimpleRowSet rs = qs.getData(query);

      while (rs.getNumberOfRows() > 0) {
        for (SimpleRow r : rs) {
          byte[] buffer = Codec.fromBase64(r.getValue(COL_FILE_PART));
          out.write(buffer, 0, buffer.length);
        }
        if (rs.getNumberOfRows() < query.getLimit()) {
          break;
        }
        rs = qs.getData(query.setOffset(query.getOffset() + query.getLimit()));
      }
      out.flush();
      out.close();

      ZipInputStream in = new ZipInputStream(new FileInputStream(tmp));

      if (in.getNextEntry() != null) {
        File res = File.createTempFile("bee_", null);
        res.deleteOnExit();
        repo = res.getAbsolutePath();

        out = new FileOutputStream(res);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        try {
          while ((bytesRead = in.read(buffer)) > 0) {
            out.write(buffer, 0, bytesRead);
          }
          in.closeEntry();
          out.flush();
        } finally {
          out.close();
          in.close();
        }
      }
      tmp.delete();
      storedFile.setTemporary(true);
    }
    storedFile.setPath(repo);

    return storedFile;
  }

  public List<StoredFile> getFiles() {
    List<StoredFile> files = Lists.newArrayList();

    String idName = sys.getIdName(TBL_FILES);
    String versionName = sys.getVersionName(TBL_FILES);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FILES, idName, versionName, COL_FILE_NAME, COL_FILE_SIZE, COL_FILE_TYPE)
        .addFrom(TBL_FILES)
        .addOrder(TBL_FILES, versionName));

    for (SimpleRow row : data) {
      StoredFile sf = new StoredFile(row.getLong(idName),
          row.getValue(COL_FILE_NAME), row.getLong(COL_FILE_SIZE), row.getValue(COL_FILE_TYPE));

      sf.setFileDate(DateTime.restore(row.getValue(versionName)));

      sf.setIcon(ExtensionIcons.getIcon(sf.getName()));
      files.add(sf);
    }
    return files;
  }

  public boolean photoExists(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return false;
    }

    File file = new File(getPhotoDir(), BeeUtils.trim(fileName));
    return file.exists();
  }

  public void setRepositoryDir(File repositoryDir) {
    this.repositoryDir = repositoryDir;
  }

  public Long storeFile(InputStream is, String fileName, String mimeType) throws IOException {
    String name = BeeUtils.notEmpty(fileName, "unknown");
    boolean storeAsFile = repositoryDir != null;
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new BeeRuntimeException(e);
    }
    InputStream in = new DigestInputStream(is, md);
    OutputStream out;
    File tmp;

    if (storeAsFile) {
      JustDate dt = new JustDate();
      tmp = new File(repositoryDir,
          BeeUtils.join(File.separator, dt.getYear(), dt.getMonth(), dt.getDom()));
      tmp.mkdirs();
      tmp = new File(tmp, "bee_" + BeeUtils.randomString(30) + ".tmp");
      out = new FileOutputStream(tmp);
    } else {
      tmp = File.createTempFile("bee_", null);
      out = new ZipOutputStream(new FileOutputStream(tmp));
      ((ZipOutputStream) out).putNextEntry(new ZipEntry(name));
    }
    tmp.deleteOnExit();

    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;
    long size = 0;

    try {
      while ((bytesRead = in.read(buffer)) > 0) {
        out.write(buffer, 0, bytesRead);
        size += bytesRead;
      }
      if (!storeAsFile) {
        ((ZipOutputStream) out).closeEntry();
      }
      out.flush();
    } finally {
      out.close();
      in.close();
    }
    Long id = null;

    synchronized (lock) {
      String hash = Codec.toHex(md.digest());
      String idName = sys.getIdName(TBL_FILES);

      SimpleRow data = qs.getRow(new SqlSelect()
          .addFields(TBL_FILES, COL_FILE_REPO, idName)
          .addFrom(TBL_FILES)
          .setWhere(SqlUtils.equals(TBL_FILES, COL_FILE_HASH, hash)));

      boolean exists = data != null;

      if (exists) {
        id = data.getLong(idName);
        exists = BeeUtils.isEmpty(data.getValue(COL_FILE_REPO));

        if (!exists) {
          exists = new File(data.getValue(COL_FILE_REPO)).exists();
        }
      }
      if (!exists) {
        String repo = null;

        if (storeAsFile) {
          File target = new File(tmp.getParentFile(), hash);
          repo = target.getPath();

          if (target.exists()) {
            logger.warning("File already existed:", repo);
            target.delete();
          }
          if (!tmp.renameTo(target)) {
            throw new BeeRuntimeException(BeeUtils.joinWords("Error renaming file:",
                tmp.getPath(), "to:", repo));
          }
        }
        if (DataUtils.isId(id)) {
          qs.updateData(new SqlUpdate(TBL_FILES)
              .addConstant(COL_FILE_REPO, repo)
              .setWhere(SqlUtils.equals(TBL_FILES, idName, id)));
        } else {
          id = qs.insertData(new SqlInsert(TBL_FILES)
              .addConstant(COL_FILE_HASH, hash)
              .addConstant(COL_FILE_REPO, repo)
              .addConstant(COL_FILE_NAME, name)
              .addConstant(COL_FILE_SIZE, size)
              .addConstant(COL_FILE_TYPE, mimeType));
        }
        if (!storeAsFile) {
          boolean isDebugEnabled = messyLogger.isDebugEnabled();

          if (isDebugEnabled) {
            messyLogger.setLevel(LogLevel.INFO);
          }
          in = new FileInputStream(tmp);
          buffer = new byte[BUFFER_SIZE - BUFFER_SIZE % 3];
          size = BeeUtils.toLong(Math.floor(Math.pow(2, 20) / buffer.length));
          int c = 0;
          StringBuilder sb = new StringBuilder();

          try {
            while ((bytesRead = in.read(buffer)) > 0) {
              c++;
              sb.append(Codec.toBase64(buffer));

              if (c == size) {
                qs.insertData(new SqlInsert(TBL_FILE_PARTS)
                    .addConstant(COL_FILE_FILE, id)
                    .addConstant(COL_FILE_PART, sb.toString()));
                c = 0;
                sb = new StringBuilder();
              }
            }
            if (c > 0) {
              qs.insertData(new SqlInsert(TBL_FILE_PARTS)
                  .addConstant(COL_FILE_FILE, id)
                  .addConstant(COL_FILE_PART, sb.toString()));
            }
          } finally {
            in.close();
          }
          if (isDebugEnabled) {
            messyLogger.setLevel(LogLevel.DEBUG);
          }
        }
      }
      if (tmp.exists()) {
        tmp.delete();
      }
    }
    return id;
  }

  public boolean storePhoto(InputStream is, String fileName) {
    File dir = getPhotoDir();
    if (!dir.exists() && !dir.mkdirs()) {
      logger.severe("cannot create", dir.getPath());
      return false;
    }

    File file = new File(dir, BeeUtils.trim(fileName));

    byte[] buffer = new byte[BUFFER_SIZE];
    int bytesRead;

    OutputStream out = null;
    boolean ok;

    try {
      out = new FileOutputStream(file);
      while ((bytesRead = is.read(buffer)) > 0) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush();
      ok = true;

    } catch (IOException ex) {
      logger.severe(ex);
      ok = false;
    }

    FileUtils.closeQuietly(out);
    if (!ok && file.exists()) {
      file.delete();
    }
    return ok;
  }

  private static File getPhotoDir() {
    return new File(Config.IMAGE_DIR, Paths.PHOTO_DIR);
  }

  @PostConstruct
  private void init() {
    String repo = Config.getProperty("RepositoryDir");

    if (!BeeUtils.isEmpty(repo)) {
      File repository = new File(repo);

      if (FileUtils.isDirectory(repository)) {
        setRepositoryDir(repository);
      } else {
        logger.warning("Wrong repository directory:", repo);
      }
    }
  }
}
