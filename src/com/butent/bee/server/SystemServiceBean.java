package com.butent.bee.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.butent.bee.server.communication.ResponseBuffer;
import com.butent.bee.server.http.RequestInfo;
import com.butent.bee.server.io.ExtensionFilter;
import com.butent.bee.server.io.FileUtils;
import com.butent.bee.server.io.Filter;
import com.butent.bee.server.io.NameUtils;
import com.butent.bee.server.io.NameUtils.Component;
import com.butent.bee.server.io.WildcardFilter;
import com.butent.bee.server.utils.ClassUtils;
import com.butent.bee.server.utils.JvmUtils;
import com.butent.bee.server.utils.XmlUtils;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.Service;
import com.butent.bee.shared.communication.CommUtils;
import com.butent.bee.shared.communication.ContentType;
import com.butent.bee.shared.data.BeeColumn;
import com.butent.bee.shared.data.value.ValueType;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.Codec;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.PropertyUtils;
import com.butent.bee.shared.utils.Wildcards;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;

@Stateless
public class SystemServiceBean {
  private static Logger logger = Logger.getLogger(SystemServiceBean.class.getName());

  public void doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    if (BeeUtils.same(svc, Service.GET_CLASS_INFO)) {
      classInfo(reqInfo, buff);
    } else if (BeeUtils.same(svc, Service.GET_XML_INFO)) {
      xmlInfo(reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.GET_RESOURCE)) {
      getResource(reqInfo, buff);
    } else if (BeeUtils.same(svc, Service.SAVE_RESOURCE)) {
      saveResource(reqInfo, buff);

    } else if (BeeUtils.same(svc, Service.GET_DIGEST)) {
      getDigest(reqInfo, buff);

    } else {
      String msg = BeeUtils.concat(1, svc, "system service not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }

  private void classInfo(RequestInfo reqInfo, ResponseBuffer buff) {
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

  private void getDigest(RequestInfo reqInfo, ResponseBuffer buff) {
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

  private void getResource(RequestInfo reqInfo, ResponseBuffer buff) {
    String mode = reqInfo.getParameter(0);
    if (BeeUtils.same(mode, "cs")) {
      buff.addExtendedProperties(FileUtils.getCharsets());
      return;
    }
    if (BeeUtils.same(mode, "fs")) {
      buff.addProperties(PropertyUtils.createProperties("Path Separator",
          File.pathSeparator, "Separator", File.separator));
      buff.addProperties(FileUtils.getRootsInfo());
      return;
    }

    String search = reqInfo.getParameter(1);
    if (BeeUtils.isEmpty(search)) {
      buff.addSevere("resource name ( parameter", CommUtils.rpcParamName(1), ") not specified");
      return;
    }

    File resFile = null;
    if (FileUtils.isFile(search)) {
      resFile = new File(search);
    } else {

      String pfx = NameUtils.getPrefix(search);
      String path = NameUtils.getPathNoEndSeparator(search);
      String stem = NameUtils.getBaseName(search);
      String ext = NameUtils.getExtension(search);

      Set<File> roots = Sets.newLinkedHashSet();

      if (!BeeUtils.isEmpty(pfx)) {
        File dir;
        for (int i = 0; i < pfx.length(); i++) {
          switch (pfx.charAt(i)) {
            case 'c':
              dir = Config.CONFIG_DIR;
              break;
            case 's':
              dir = Config.SOURCE_DIR;
              break;
            case 'u':
              dir = Config.USER_DIR;
              break;
            case 'w':
              dir = Config.WAR_DIR;
              break;
            default:
              dir = null;
          }
          if (dir != null) {
            roots.add(dir);
          }
        }
      }
      
      if (roots.isEmpty()) {
        if (BeeUtils.same(mode, "src")) {
          roots.add(Config.SOURCE_DIR);
        } else {
          roots.add(Config.USER_DIR);
          roots.add(Config.CONFIG_DIR);
          roots.add(Config.WAR_DIR);
          roots.add(Config.SOURCE_DIR);
        }
      }

      List<Filter> filters = Lists.newArrayList();

      if (BeeUtils.same(mode, "dir")) {
        filters.add(FileUtils.DIRECTORY_FILTER);
      } else if (BeeUtils.same(mode, "file")) {
        filters.add(FileUtils.FILE_FILTER);
      } else {
        filters.add(FileUtils.INPUT_FILTER);
      }

      if (Wildcards.isFsPattern(path)) {
        filters.add(new WildcardFilter(path, Component.PATH));
      }
      if (Wildcards.isFsPattern(stem)) {
        filters.add(new WildcardFilter(stem, Component.BASE_NAME));
      }
      if (Wildcards.isFsPattern(ext)) {
        filters.add(new WildcardFilter(ext, Component.EXTENSION));
      } else if (BeeUtils.same(mode, "src")) {
        filters.add(new ExtensionFilter(FileUtils.EXT_JAVA));
      }

      List<File> files = FileUtils.findFiles(roots, filters, true, false);
      if (BeeUtils.isEmpty(files)) {
        buff.addWarning("resource", search, "not found");
        return;
      }

      if (files.size() > 1) {
        Collections.sort(files);

        buff.addColumn(new BeeColumn(ValueType.NUMBER, "Idx"));
        buff.addColumn(new BeeColumn(ValueType.TEXT, "Name"));
        buff.addColumn(new BeeColumn(ValueType.TEXT, "Path"));
        buff.addColumn(new BeeColumn(ValueType.NUMBER, "Size"));
        buff.addColumn(new BeeColumn(ValueType.DATETIME, "Modified"));
        
        long totSize = 0;
        long lastMod = 0;
        long x, y;
        int idx = 0;
        
        for (File fl : files) {
          x = fl.isFile() ? fl.length() : 0;
          y = fl.lastModified();
          buff.add(++idx, fl.getName(), fl.getPath(), x, y);
          
          if (x > 0) {
            totSize += x;
          }
          if (y != 0) {
            lastMod = Math.max(lastMod, y);
          }
        }
        buff.add(0, mode, search, totSize, lastMod);

        buff.addMessage(mode, search, "found", files.size(), "files");
        return;
      }
      resFile = files.get(0);
    }

    Assert.notNull(resFile);
    String resPath = resFile.getPath();
    if (!resFile.exists()) {
      buff.addWarning("file", resPath, "does not exist");
      return;
    }
    buff.addMessage(mode, search, "found", resPath);

    if (BeeUtils.inListSame(mode, "get", "src")) {
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

        } else {
          buff.addResource(resPath, s, ContentType.RESOURCE);

          String mt = reqInfo.getParameter(3);
          if (!BeeUtils.isEmpty(mt)) {
            buff.setMediaType(mt);
          }
          String ce = reqInfo.getParameter(4);
          if (!BeeUtils.isEmpty(ce)) {
            buff.setCharacterEncoding(ce);
          }
          return;
        }
      }
    }

    buff.addProperties(FileUtils.getFileInfo(resFile));

    if (resFile.isDirectory()) {
      String[] arr = FileUtils.getFiles(resFile);
      int n = BeeUtils.length(arr);

      if (n > 0) {
        buff.addProperties(PropertyUtils.createProperties("file", arr));
      } else {
        buff.addWarning("no files found");
      }
    }
  }

  private void saveResource(RequestInfo reqInfo, ResponseBuffer buff) {
    long start = System.currentTimeMillis();

    String uri = reqInfo.getParameter(Service.RPC_VAR_URI);
    String md5 = reqInfo.getParameter(Service.RPC_VAR_MD5);

    if (BeeUtils.isEmpty(uri)) {
      buff.addSevere("URI not specified");
      return;
    }

    buff.addMessage("uri", uri);

    String content = reqInfo.getContent();
    if (BeeUtils.isEmpty(content)) {
      buff.addSevere("Content not found");
      return;
    }

    if (BeeUtils.isEmpty(md5)) {
      buff.addWarning("md5 not specified");
    } else {
      buff.addMessage("md5", md5);
      String z = Codec.md5(content);
      if (!BeeUtils.same(md5, z)) {
        buff.addSevere("md5 does not match");
        buff.addSevere("received", z);
        return;
      }
    }

    boolean ok = FileUtils.saveToFile(content, uri);
    if (ok) {
      buff.addMessage("saved", content.length(), BeeUtils.elapsedSeconds(start));
    } else {
      buff.addSevere("error saving to", uri);
    }
  }

  private void xmlInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String pSrc = reqInfo.getParameter(Service.VAR_XML_SOURCE);
    String pXsl = reqInfo.getParameter(Service.VAR_XML_TRANSFORM);
    String pDst = reqInfo.getParameter(Service.VAR_XML_TARGET);
    String ret = reqInfo.getParameter(Service.VAR_XML_RETURN);

    if (BeeUtils.isEmpty(pSrc)) {
      buff.addSevere("Parameter", Service.VAR_XML_SOURCE, "not found");
      return;
    }

    String src = NameUtils.defaultExtension(pSrc, XmlUtils.defaultXmlExtension);
    if (!FileUtils.isInputFile(src)) {
      buff.addSevere(src, "is not a valid input file");
      return;
    }

    buff.addMessage(src);

    if (BeeUtils.isEmpty(pXsl)) {
      if (BeeUtils.context("xml", ret)) {
        String z = FileUtils.fileToString(src);
        if (BeeUtils.isEmpty(z)) {
          buff.addSevere("cannot read file");
        } else {
          buff.addResource(src, z, ContentType.XML);
        }
        return;
      }

      List<ExtendedProperty> lst = XmlUtils.getFileInfo(src);
      if (BeeUtils.isEmpty(lst)) {
        buff.addSevere("cannot get xml info");
      } else {
        buff.addExtendedProperties(lst);
      }
      return;
    }

    String xsl = NameUtils.defaultExtension(pXsl, XmlUtils.defaultXslExtension);
    if (!FileUtils.isInputFile(xsl)) {
      buff.addSevere(xsl, "is not a valid input file");
      return;
    }

    String dst;
    if (BeeUtils.isEmpty(pDst)) {
      dst = null;
    } else {
      dst = NameUtils.defaultExtension(pDst, XmlUtils.defaultXmlExtension);
      if (BeeUtils.inListSame(dst, src, xsl)) {
        buff.addSevere(dst, "is not a valid target");
        return;
      }
    }

    buff.addMessage(xsl);

    String target = null;
    boolean ok = false;

    if (dst == null) {
      if (BeeUtils.context("prop", ret)) {
        List<ExtendedProperty> lst = XmlUtils.xsltToInfo(src, xsl);
        if (BeeUtils.isEmpty(lst)) {
          buff.addSevere("xslt error");
        } else {
          buff.addExtendedProperties(lst);
        }
        return;
      }

      target = XmlUtils.xsltToString(src, xsl);
      ok = !BeeUtils.isEmpty(target);

    } else {
      buff.addMessage(dst);

      ok = XmlUtils.xsltToFile(src, xsl, dst);
      if (ok) {
        if (BeeUtils.context("prop", ret)) {
          List<ExtendedProperty> lst = XmlUtils.getFileInfo(dst);
          if (BeeUtils.isEmpty(lst)) {
            buff.addSevere("cannot get target info");
          } else {
            buff.addExtendedProperties(lst);
          }
          return;
        }

        target = FileUtils.fileToString(dst);
        ok = !BeeUtils.isEmpty(target);
      }
    }

    if (!ok) {
      buff.addSevere("xslt error");
      return;
    }

    String source = null;
    String transf = null;

    if (BeeUtils.context("all", ret) || BeeUtils.context("source", ret)) {
      source = FileUtils.fileToString(src);
      if (BeeUtils.isEmpty(source)) {
        buff.addSevere("cannot read source");
        return;
      }
    }

    if (BeeUtils.context("all", ret) || BeeUtils.context("xsl", ret)) {
      transf = FileUtils.fileToString(xsl);
      if (BeeUtils.isEmpty(transf)) {
        buff.addSevere("cannot read xsl");
        return;
      }
    }

    if (BeeUtils.allEmpty(source, transf)) {
      buff.addResource(dst, target, ContentType.XML);
      return;
    }

    if (!BeeUtils.isEmpty(source)) {
      buff.addPart(src, source, ContentType.XML);
    }
    if (!BeeUtils.isEmpty(transf)) {
      buff.addPart(xsl, transf, ContentType.XML);
    }

    buff.addPart(dst, target, ContentType.XML, dst == null);
  }
}
