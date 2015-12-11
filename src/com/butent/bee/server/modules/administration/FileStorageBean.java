package com.butent.bee.server.modules.administration;

import static com.butent.bee.shared.modules.administration.AdministrationConstants.*;
import static com.butent.bee.shared.modules.documents.DocumentConstants.*;

import com.butent.bee.server.Config;
import com.butent.bee.server.Invocation;
import com.butent.bee.server.concurrency.ConcurrencyBean;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.modules.ParamHolderBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUpdate;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.server.utils.HtmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.Holder;
import com.butent.bee.shared.Pair;
import com.butent.bee.shared.data.DataUtils;
import com.butent.bee.shared.data.SimpleRowSet;
import com.butent.bee.shared.data.SimpleRowSet.SimpleRow;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.io.FileInfo;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.DateTime;
import com.butent.bee.shared.time.JustDate;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.lowagie.text.DocumentException;

import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private static final int BUFFER_SIZE = 8192;

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;
  @EJB
  ConcurrencyBean cb;
  @EJB
  ParamHolderBean prm;

  private File repositoryDir;

  public String createPdf(String content, String... styleSheets) {
    StringBuilder sb = new StringBuilder();

    for (String name : new String[] {PRM_PRINT_HEADER, PRM_PRINT_FOOTER}) {
      Long id = prm.getRelation(name);

      if (DataUtils.isId(id)) {
        String nameContent = qs.getValue(new SqlSelect()
            .addFields(TBL_EDITOR_TEMPLATES, COL_EDITOR_TEMPLATE_CONTENT)
            .addFrom(TBL_EDITOR_TEMPLATES)
            .setWhere(sys.idEquals(TBL_EDITOR_TEMPLATES, id)));

        if (!BeeUtils.isEmpty(nameContent)) {
          sb.append("<div style=\"position:running(").append(name).append(")\">")
              .append(nameContent)
              .append("</div>");
        }
      }
    }
    String parsed = HtmlUtils.cleanXml(sb.append(content).toString());

    Map<Long, String> files = HtmlUtils.getFileReferences(parsed);
    List<FileInfo> tmpFiles = new ArrayList<>();
    String path = null;

    try {
      for (Long fileId : files.keySet()) {
        FileInfo fileInfo = getFile(fileId);
        tmpFiles.add(fileInfo);
        parsed = parsed.replace(files.get(fileId), fileInfo.getFile().toURI().toString());
      }
      StringBuilder html = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
          .append("<!DOCTYPE html [<!ENTITY nbsp \"&#160;\">]>")
          .append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>")
          .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");

      List<String> styles = Arrays.asList("print");

      if (styleSheets != null) {
        styles.addAll(Arrays.asList(styleSheets));
      }
      for (String style : styles) {
        html.append("<link rel=\"stylesheet\" href=\"")
            .append(new File(Config.WAR_DIR, Paths.getStyleSheetPath(style)).toURI().toString())
            .append("\" />");
      }
      sb = new StringBuilder();

      for (Pair<String, String> pair : Arrays.asList(Pair.of(PRM_PRINT_MARGINS, "margin"),
          Pair.of(PRM_PRINT_SIZE, "size"))) {
        String prop = prm.getText(pair.getA());

        if (!BeeUtils.isEmpty(prop)) {
          sb.append(pair.getB()).append(":").append(prop).append(";");
        }
      }
      if (BeeUtils.isPositive(sb.length())) {
        html.append("<style>")
            .append("@page {").append(sb.toString()).append("}")
            .append("</style>");
      }
      html.append("</head><body>" + parsed + "</body></html>");

      ITextRenderer renderer = new ITextRenderer();
      renderer.setDocumentFromString(html.toString());
      renderer.layout();

      File tmp = File.createTempFile("bee_", ".pdf");
      tmp.deleteOnExit();
      FileOutputStream os = new FileOutputStream(tmp);

      renderer.createPDF(os);
      os.close();

      path = tmp.getPath();

    } catch (IOException | DocumentException e) {
      logger.error(e);

    } finally {
      for (FileInfo fileInfo : tmpFiles) {
        fileInfo.close();
      }
    }
    return path;
  }

  public static boolean deletePhoto(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return false;
    }

    File file = new File(getPhotoDir(), BeeUtils.trim(fileName));
    return file.exists() && file.delete();
  }

  public FileInfo getFile(Long fileId) throws IOException {
    Assert.notNull(fileId);

    SimpleRow row = qs.getRow(new SqlSelect()
        .addFields(TBL_FILES, COL_FILE_REPO, COL_FILE_NAME, COL_FILE_SIZE, COL_FILE_TYPE)
        .addFrom(TBL_FILES)
        .setWhere(sys.idEquals(TBL_FILES, fileId)));

    if (row == null) {
      throw new IOException("File not found: id =" + fileId);
    }
    FileInfo storedFile = new FileInfo(fileId, row.getValue(COL_FILE_NAME),
        row.getLong(COL_FILE_SIZE), row.getValue(COL_FILE_TYPE));

    String repo = row.getValue(COL_FILE_REPO);

    if (BeeUtils.isEmpty(repo)) {
      SqlSelect query = new SqlSelect().setLimit(10)
          .addFields(TBL_FILE_PARTS, COL_FILE_PART)
          .addFrom(TBL_FILE_PARTS)
          .setWhere(SqlUtils.equals(TBL_FILE_PARTS, COL_FILE, fileId))
          .addOrder(TBL_FILE_PARTS, sys.getIdName(TBL_FILE_PARTS));

      File tmp = File.createTempFile("bee_", null);
      tmp.deleteOnExit();
      OutputStream out = new FileOutputStream(tmp);
      List<byte[]> rs = qs.getBytesColumn(query);

      while (rs.size() > 0) {
        for (byte[] bytes : rs) {
          if (bytes != null) {
            out.write(bytes, 0, bytes.length);
          }
        }
        if (rs.size() < query.getLimit()) {
          break;
        }
        rs = qs.getBytesColumn(query.setOffset(query.getOffset() + query.getLimit()));
      }
      out.flush();
      out.close();

      ZipInputStream in = new ZipInputStream(new FileInputStream(tmp));

      if (in.getNextEntry() != null) {
        File res = File.createTempFile("bee_", null);
        res.deleteOnExit();
        repo = res.getAbsolutePath();

        try {
          Files.copy(in, res.toPath(), StandardCopyOption.REPLACE_EXISTING);
          in.closeEntry();
        } finally {
          in.close();
        }
      }
      tmp.delete();
      storedFile.setTemporary(true);
    }
    storedFile.setPath(repo);

    return storedFile;
  }

  public List<FileInfo> getFiles(List<Long> fileIds) {
    List<FileInfo> files = new ArrayList<>();

    String idName = sys.getIdName(TBL_FILES);
    String versionName = sys.getVersionName(TBL_FILES);

    SimpleRowSet data = qs.getData(new SqlSelect()
        .addFields(TBL_FILES, idName, versionName, COL_FILE_NAME, COL_FILE_SIZE, COL_FILE_TYPE)
        .addFrom(TBL_FILES)
        .setWhere(sys.idInList(TBL_FILES, fileIds))
        .addOrder(TBL_FILES, versionName));

    for (SimpleRow row : data) {
      FileInfo sf = new FileInfo(row.getLong(idName),
          row.getValue(COL_FILE_NAME), row.getLong(COL_FILE_SIZE), row.getValue(COL_FILE_TYPE));

      sf.setFileDate(DateTime.restore(row.getValue(versionName)));

      sf.setIcon(ExtensionIcons.getIcon(sf.getName()));
      files.add(sf);
    }
    return files;
  }

  public static boolean photoExists(String fileName) {
    if (BeeUtils.isEmpty(fileName)) {
      return false;
    }

    File file = new File(getPhotoDir(), BeeUtils.trim(fileName));
    return file.exists();
  }

  public Long storeFile(InputStream is, String fileName, String mimeType) throws IOException {
    boolean storeAsFile = Objects.nonNull(repositoryDir);
    String name = sys.clampValue(TBL_FILES, COL_FILE_NAME, BeeUtils.notEmpty(fileName, "unknown"));

    String type = !BeeUtils.isEmpty(mimeType) ? mimeType
        : BeeUtils.notEmpty(URLConnection.guessContentTypeFromStream(is),
        URLConnection.guessContentTypeFromName(name));

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

    Holder<Long> size = Holder.of(0L);

    try {
      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead = in.read(buffer);

      while (bytesRead > 0) {
        out.write(buffer, 0, bytesRead);
        size.set(size.get() + bytesRead);
        bytesRead = in.read(buffer);
      }
      if (!storeAsFile) {
        ((ZipOutputStream) out).closeEntry();
      }
      out.flush();
    } finally {
      out.close();
      in.close();
    }
    String idName = sys.getIdName(TBL_FILES);
    String hash = Codec.toHex(md.digest());
    Holder<Long> id = Holder.absent();
    Holder<Boolean> exists = Holder.of(false);

    cb.synchronizedCall(new Runnable() {
      @Override
      public void run() {
        QueryServiceBean queryBean = Invocation.locateRemoteBean(QueryServiceBean.class);

        id.set(queryBean.getLong(new SqlSelect()
            .addFields(TBL_FILES, idName)
            .addFrom(TBL_FILES)
            .setWhere(SqlUtils.equals(TBL_FILES, COL_FILE_HASH, hash))));

        if (id.isNull()) {
          id.set(queryBean.insertData(new SqlInsert(TBL_FILES)
              .addConstant(COL_FILE_HASH, hash)
              .addConstant(COL_FILE_NAME, name)
              .addConstant(COL_FILE_SIZE, size.get())
              .addConstant(COL_FILE_TYPE, sys.clampValue(TBL_FILES, COL_FILE_TYPE, type))));
        } else {
          exists.set(true);
        }
      }
    });
    if (!exists.get() && !storeAsFile) {
      in = new FileInputStream(tmp);

      try {
        byte[] buffer = new byte[0x100000];
        int bytesRead = in.read(buffer);

        while (bytesRead > 0) {
          long recId = qs.insertData(new SqlInsert(TBL_FILE_PARTS)
              .addConstant(COL_FILE, id.get()));
          qs.updateBlob(TBL_FILE_PARTS, recId, COL_FILE_PART, new ByteArrayInputStream(buffer));
          bytesRead = in.read(buffer);
        }
      } catch (SQLException e) {
        throw new RuntimeException(e);
      } finally {
        in.close();
      }
    }
    if (storeAsFile) {
      String repo = qs.getValue(new SqlSelect()
          .addFields(TBL_FILES, COL_FILE_REPO)
          .addFrom(TBL_FILES)
          .setWhere(SqlUtils.equals(TBL_FILES, idName, id.get())));

      if (BeeUtils.isEmpty(repo) || !FileUtils.isFile(repo)) {
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
        qs.updateData(new SqlUpdate(TBL_FILES)
            .addConstant(COL_FILE_REPO, repo)
            .setWhere(SqlUtils.equals(TBL_FILES, idName, id.get())));
      }
    }
    if (tmp.exists()) {
      tmp.delete();
    }
    return id.get();
  }

  public static boolean storePhoto(InputStream is, String fileName) {
    File dir = getPhotoDir();
    if (!dir.exists() && !dir.mkdirs()) {
      logger.severe("cannot create", dir.getPath());
      return false;
    }

    File file = new File(dir, BeeUtils.trim(fileName));
    boolean ok = true;

    try {
      Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

    } catch (IOException ex) {
      logger.severe(ex);
      ok = false;
    }
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
        repositoryDir = repository;
      } else {
        logger.warning("Wrong repository directory:", repo);
      }
    }
  }
}
