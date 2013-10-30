package com.butent.bee.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.Filter;
import com.butent.bee.server.modules.commons.FileStorageBean;
import com.butent.bee.server.utils.ClassUtils;
import com.butent.bee.server.utils.JvmUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Resource;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ResponseObject;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.io.FileNameUtils;
import com.butent.bee.shared.io.Paths;
import com.butent.bee.shared.io.StoredFile;
import com.butent.bee.shared.logging.BeeLogger;
import com.butent.bee.shared.logging.LogUtils;
import com.butent.bee.shared.time.TimeUtils;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 * Manages system requests with <code>rpc_sys</code> tag, for example working with system's files
 * and other resources.
 */

@Stateless
public class SystemServiceBean {

  private static BeeLogger logger = LogUtils.getLogger(SystemServiceBean.class);

  @EJB
  FileStorageBean fs;
  
  public ResponseObject doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);
    ResponseObject response = null;

    if (BeeUtils.same(svc, Service.GET_CLASS_INFO)) {
      classInfo(reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.GET_RESOURCE)) {
      response = getResource(reqInfo, buff);
    } else if (BeeUtils.same(svc, Service.SAVE_RESOURCE)) {
      response = saveResource(reqInfo);

    } else if (BeeUtils.same(svc, Service.GET_DIGEST)) {
      getDigest(reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.GET_FILES)) {
      response = getFiles();
    } else if (BeeUtils.same(svc, Service.GET_FLAGS)) {
      response = getFlags();
      
    } else {
      String msg = BeeUtils.joinWords(svc, "system service not recognized");
      logger.warning(msg);
      buff.addWarning(msg);
    }
    return response;
  }

  private static void classInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String cnm = reqInfo.getParameter(Service.VAR_CLASS_NAME);
    String pck = reqInfo.getParameter(Service.VAR_PACKAGE_LIST);

    if (BeeUtils.isEmpty(cnm)) {
      buff.addSevere("Parameter", Service.VAR_CLASS_NAME, "not found");
      return;
    }

    Set<Class<?>> classes;
    if (BeeUtils.isEmpty(pck)) {
      classes = JvmUtils.findClassWithDefaultPackages(cnm);
    } else {
      classes = JvmUtils.findClass(cnm, pck.split(","));
    }

    if (BeeUtils.isEmpty(classes)) {
      buff.addWarning("Class not found", cnm, pck);
      return;
    }
    int c = classes.size();

    buff.addExtendedPropertiesColumns();
    buff.addExtended(new ExtendedProperty(cnm, pck, BeeUtils.bracket(c)));

    int i = 0;
    if (c > 1) {
      for (Class<?> cls : classes) {
        i++;
        buff.addExtended(new ExtendedProperty(cls.getName(), null, BeeUtils.progress(i, c)));
      }
    }

    i = 0;
    for (Class<?> cls : classes) {
      i++;
      if (c > 1) {
        buff.addExtended(new ExtendedProperty(cls.getName(), null, BeeUtils.progress(i, c)));
      }
      buff.appendExtended(ClassUtils.getClassInfo(cls));
    }
  }

  private static void getDigest(RequestInfo reqInfo, ResponseBuffer buff) {
    String src = reqInfo.getContent();

    if (BeeUtils.length(src) <= 0) {
      buff.addSevere("Source not found");
      return;
    }
    if (src.length() < 100) {
      buff.addMessage(BeeConst.SERVER, src);
    }
    buff.addMessage(BeeConst.SERVER, "Source length", src.length());
    buff.addMessage(BeeConst.SERVER, Codec.md5(src));
  }
  
  private ResponseObject getFiles() {
    List<StoredFile> files = fs.getFiles();
    if (files.isEmpty()) {
      return ResponseObject.warning("file repository is empty");
    } else {
      return ResponseObject.response(files);
    }
  }
  
  private static ResponseObject getFlags() {
    Map<String, String> flags = Maps.newHashMap(); 

    File dir = new File(Config.IMAGE_DIR, Paths.FLAG_DIR);
    File[] files = dir.listFiles();
    
    if (files != null) {
      for (File file : files) {
        String name = file.getName();
        
        String key = BeeUtils.normalize(FileNameUtils.getBaseName(name));
        String ext = BeeUtils.normalize(FileNameUtils.getExtension(name));
        
        if (BeeUtils.anyEmpty(key, ext)) {
          logger.warning("invalid flag file:", file.getPath());
          continue;
        }
        
        byte[] bytes = FileUtils.getBytes(file);
        if (bytes == null) {
          logger.severe("error loading flag:", file.getPath());
          break;
        }
        
        String uri = "data:image/" + ext + ";base64," + Codec.toBase64(bytes);
        flags.put(key, uri);
      }
    }

    logger.info("loaded", flags.size(), "flags");
    return ResponseObject.response(flags);
  }

  private static ResponseObject getResource(RequestInfo reqInfo, ResponseBuffer buff) {
    String mode = reqInfo.getParameter(0);
    if (BeeUtils.same(mode, "cs")) {
      buff.addExtendedProperties(FileUtils.getCharsets());
      return null;
    }
    if (BeeUtils.same(mode, "fs")) {
      buff.addProperties(PropertyUtils.createProperties("Path Separator",
          File.pathSeparator, "Separator", File.separator));
      buff.addProperties(FileUtils.getRootsInfo());
      return null;
    }

    String search = reqInfo.getParameter(1);
    if (BeeUtils.isEmpty(search)) {
      buff.addSevere("resource name ( parameter", CommUtils.rpcParamName(1), ") not specified");
      return null;
    }

    File resFile = null;
    if (FileUtils.isFile(search)) {
      resFile = new File(search);
    } else {

      List<File> roots = null;
      if (BeeUtils.same(mode, "src")) {
        roots = Lists.newArrayList(Config.SOURCE_DIR);
      }

      List<Filter> filters = Lists.newArrayList();
      if (BeeUtils.same(mode, "dir")) {
        filters.add(FileUtils.DIRECTORY_FILTER);
      } else if (BeeUtils.same(mode, "file")) {
        filters.add(FileUtils.FILE_FILTER);
      } else {
        filters.add(FileUtils.INPUT_FILTER);
      }

      String defaultExtension = null;
      if (BeeUtils.same(mode, "src")) {
        defaultExtension = FileUtils.EXT_JAVA;
      } else if (BeeUtils.same(mode, "xml")) {
        defaultExtension = XmlUtils.DEFAULT_XML_EXTENSION;
      }

      List<File> files = FileUtils.findFiles(search, roots, filters, defaultExtension, true, false);
      if (BeeUtils.isEmpty(files)) {
        buff.addWarning("resource", search, "not found");
        return null;
      }

      if (files.size() > 1) {
        Collections.sort(files);

        buff.addColumn(new BeeColumn(ValueType.NUMBER, "Idx"));
        buff.addColumn(new BeeColumn(ValueType.TEXT, "Name"));
        buff.addColumn(new BeeColumn(ValueType.TEXT, "Path"));
        buff.addColumn(new BeeColumn(ValueType.NUMBER, "Size"));
        buff.addColumn(new BeeColumn(ValueType.DATE_TIME, "Modified"));

        long totSize = 0;
        long lastMod = 0;
        long x;
        long y;
        int idx = 0;

        for (File fl : files) {
          x = fl.isFile() ? fl.length() : 0;
          y = fl.lastModified();
          buff.addRow(++idx, fl.getName(), fl.getPath(), x, y);

          if (x > 0) {
            totSize += x;
          }
          if (y != 0) {
            lastMod = Math.max(lastMod, y);
          }
        }
        buff.addRow(0, mode, search, totSize, lastMod);

        buff.addMessage(mode, search, "found", files.size(), "files");
        return null;
      }
      resFile = files.get(0);
    }

    Assert.notNull(resFile);
    String resPath = resFile.getPath();
    if (!resFile.exists()) {
      buff.addWarning("file", resPath, "does not exist");
      return null;
    }
    buff.addMessage(mode, search, "found", resPath);

    if (BeeUtils.inListSame(mode, "get", "src", "xml")) {
      if (!FileUtils.isInputFile(resFile)) {
        buff.addWarning(resPath, "is not readable");
      } else if (!Config.isText(resFile)) {
        buff.addWarning(resPath, "is not a text resource");
      } else {
        Charset cs = FileUtils.normalizeCharset(reqInfo.getParameter(2));
        buff.addMessage("charset", cs);
        String s = FileUtils.fileToString(resFile, cs);

        if (s == null || s.length() == 0) {
          buff.addWarning(resPath, "no content found");

        } else if (BeeUtils.same(mode, "xml")) {
          return ResponseObject.response(s);

        } else {
          return ResponseObject.response(new Resource(resPath, s));
        }
      }
    }

    buff.addProperties(FileUtils.getFileInfo(resFile));

    if (resFile.isDirectory()) {
      String[] arr = FileUtils.getFiles(resFile);
      int n = ArrayUtils.length(arr);

      if (n > 0) {
        buff.addProperties(PropertyUtils.createProperties("file", arr));
      } else {
        buff.addWarning("no files found");
      }
    }
    return null;
  }

  private static ResponseObject saveResource(RequestInfo reqInfo) {
    long start = System.currentTimeMillis();

    String uri = reqInfo.getParameter(Service.RPC_VAR_URI);
    String md5 = reqInfo.getParameter(Service.RPC_VAR_MD5);

    if (BeeUtils.isEmpty(uri)) {
      return ResponseObject.parameterNotFound(Service.SAVE_RESOURCE, Service.RPC_VAR_URI);
    }

    String content = reqInfo.getContent();
    if (BeeUtils.isEmpty(content)) {
      return ResponseObject.error("Content not found");
    }
    
    ResponseObject response = new ResponseObject();
    response.addInfo("uri", uri);

    if (BeeUtils.isEmpty(md5)) {
      response.addWarning("md5 not specified");
    } else {
      response.addInfo("md5", md5);
      String z = Codec.md5(content);
      if (!BeeUtils.same(md5, z)) {
        response.addError("md5 does not match");
        response.addError("received", z);
        return response;
      }
    }

    boolean ok = FileUtils.saveToFile(content, uri);
    if (ok) {
      response.addInfo("saved", content.length(), TimeUtils.elapsedSeconds(start));
    } else {
      response.addError("error saving to", uri);
    }
    
    return response;
  }
}
