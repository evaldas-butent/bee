package com.butent.bee.server.modules.commons;

import static com.butent.bee.shared.modules.commons.CommonsConstants.TBL_FILES;

import com.butent.bee.server.Config;
import com.butent.bee.server.data.QueryServiceBean;
import com.butent.bee.server.data.SystemBean;
import com.butent.bee.server.sql.SqlInsert;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
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
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class FileStorageBean {

  private static final BeeLogger logger = LogUtils.getLogger(FileStorageBean.class);

  @EJB
  QueryServiceBean qs;
  @EJB
  SystemBean sys;

  public Long storeFile(InputStream is, String fileName, String mimeType) throws IOException {
    MessageDigest md = null;

    try {
      md = MessageDigest.getInstance("SHA");
    } catch (NoSuchAlgorithmException e) {
      throw new BeeRuntimeException(e);
    }
    JustDate dt = new JustDate();
    File tmp = new File(Config.REPOSITORY_DIR, BeeUtils.join(File.separator,
        dt.getYear(), dt.getMonth(), dt.getDom(), "tmp_" + BeeUtils.randomString(10)));
    tmp.deleteOnExit();
    tmp.getParentFile().mkdirs();
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

    Long id = null;
    File target = null;

    Map<String, String> data = qs.getRow(new SqlSelect()
        .addFields(TBL_FILES, "Repository", sys.getIdName(TBL_FILES))
        .addFrom(TBL_FILES)
        .setWhere(SqlUtils.equal(TBL_FILES, "Hash", hash)));

    if (data != null) {
      id = BeeUtils.toLong(data.get(sys.getIdName(TBL_FILES)));
      target = new File(data.get("Repository"));
      target.getParentFile().mkdirs();
    } else {
      target = new File(tmp.getParent(), hash);
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
          .addConstant("Hash", hash)
          .addConstant("Repository", target.getPath())
          .addConstant("Name", fileName)
          .addConstant("Size", target.length())
          .addConstant("Mime", mimeType));
    }
    return id;
  }
}
