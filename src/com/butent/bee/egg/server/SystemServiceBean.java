package com.butent.bee.egg.server;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.ClassUtils;
import com.butent.bee.egg.server.utils.JvmUtils;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeConst;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.communication.CommUtils;
import com.butent.bee.egg.shared.communication.ContentType;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.PropertyUtils;
import com.butent.bee.egg.shared.utils.ExtendedProperty;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
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

    if (BeeUtils.same(svc, BeeService.SERVICE_CLASS_INFO)) {
      classInfo(reqInfo, buff);
    } else if (BeeUtils.same(svc, BeeService.SERVICE_XML_INFO)) {
      xmlInfo(reqInfo, buff);

    } else if (BeeUtils.same(svc, BeeService.SERVICE_GET_RESOURCE)) {
      getResource(reqInfo, buff);
    } else if (BeeUtils.same(svc, BeeService.SERVICE_SAVE_RESOURCE)) {
      saveResource(reqInfo, buff);

    } else if (BeeUtils.same(svc, BeeService.SERVICE_GET_DIGEST)) {
      getDigest(reqInfo, buff);

    } else {
      String msg = BeeUtils.concat(1, svc, "system service not recognized");
      LogUtils.warning(logger, msg);
      buff.addWarning(msg);
    }
  }

  private void classInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String cnm = reqInfo.getParameter(BeeService.VAR_CLASS_NAME);
    String pck = reqInfo.getParameter(BeeService.VAR_PACKAGE_LIST);

    if (BeeUtils.isEmpty(cnm)) {
      buff.addSevere("Parameter", BeeService.VAR_CLASS_NAME, "not found");
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
    if (reqInfo.parameterEquals(0, "cs")) {
      buff.addExtendedProperties(FileUtils.getCharsets());
      return;
    }
    if (reqInfo.parameterEquals(0, "fs")) {
      buff.addProperties(PropertyUtils.createProperties("Path Separator",
          File.pathSeparator, "Separator", File.separator));
      buff.addProperties(FileUtils.getRootsInfo());
      return;
    }

    String name = reqInfo.getParameter(1);
    if (BeeUtils.isEmpty(name)) {
      buff.addSevere("resource name ( parameter", CommUtils.rpcParamName(1), ") not specified");
      return;
    }

    String path;

    if (FileUtils.isFile(name)) {
      path = name;
    } else {
      URL url = getClass().getResource(name);
      if (url == null) {
        buff.addWarning("resource", name, "not found");
        return;
      }

      buff.addMessage(url);
      path = url.getPath();
    }

    File fl = new File(path);

    if (!fl.exists()) {
      buff.addWarning("file", path, "does not exist");
      return;
    }

    if (reqInfo.parameterEquals(0, "get")) {
      if (!FileUtils.isInputFile(fl)) {
        buff.addWarning("file", path, "is not a valid resource");
      } else {
        Charset cs = FileUtils.normalizeCharset(reqInfo.getParameter(2));
        buff.addMessage("charset", cs);
        String s = FileUtils.fileToString(fl, cs);

        if (s == null || s.length() == 0) {
          buff.addWarning("file", path, "no content found");

        } else {
          buff.addResource(fl.getAbsolutePath(), s, ContentType.RESOURCE);

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

    buff.addProperties(FileUtils.getFileInfo(fl));

    if (fl.isDirectory()) {
      String[] arr = FileUtils.getFiles(fl);
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

    String uri = reqInfo.getParameter(BeeService.RPC_VAR_URI);
    String md5 = reqInfo.getParameter(BeeService.RPC_VAR_MD5);

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

    boolean ok = FileUtils.toFile(content, uri);
    if (ok) {
      buff.addMessage("saved", content.length(), BeeUtils.elapsedSeconds(start));
    } else {
      buff.addSevere("error saving to", uri);
    }
  }

  private void xmlInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String pSrc = reqInfo.getParameter(BeeService.VAR_XML_SOURCE);
    String pXsl = reqInfo.getParameter(BeeService.VAR_XML_TRANSFORM);
    String pDst = reqInfo.getParameter(BeeService.VAR_XML_TARGET);
    String ret = reqInfo.getParameter(BeeService.VAR_XML_RETURN);

    if (BeeUtils.isEmpty(pSrc)) {
      buff.addSevere("Parameter", BeeService.VAR_XML_SOURCE, "not found");
      return;
    }

    String src = FileUtils.defaultExtension(pSrc, XmlUtils.defaultXmlExtension);
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

    String xsl = FileUtils.defaultExtension(pXsl, XmlUtils.defaultXslExtension);
    if (!FileUtils.isInputFile(xsl)) {
      buff.addSevere(xsl, "is not a valid input file");
      return;
    }

    String dst;
    if (BeeUtils.isEmpty(pDst)) {
      dst = null;
    } else {
      dst = FileUtils.defaultExtension(pDst, XmlUtils.defaultXmlExtension);
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
