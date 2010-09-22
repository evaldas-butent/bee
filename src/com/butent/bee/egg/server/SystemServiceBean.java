package com.butent.bee.egg.server;

import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.utils.BeeClass;
import com.butent.bee.egg.server.utils.BeeJvm;
import com.butent.bee.egg.server.utils.BeeMX;
import com.butent.bee.egg.server.utils.BeeSystem;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.LogUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
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

    if (BeeService.equals(svc, BeeService.SERVICE_TEST_CONNECTION)) {
      connectionInfo(reqInfo, buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_SERVER_INFO)) {
      systemInfo(buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_VM_INFO)) {
      vmInfo(buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_LOADER_INFO)) {
      loaderInfo(buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_CLASS_INFO)) {
      classInfo(reqInfo, buff);
    } else if (BeeService.equals(svc, BeeService.SERVICE_XML_INFO)) {
      xmlInfo(reqInfo, buff);

    } else if (BeeService.equals(svc, BeeService.SERVICE_GET_RESOURCE)) {
      getResource(reqInfo, buff);

    } else {
      String msg = BeeUtils.concat(1, svc, "system service not recognized");
      LogUtils.warning(logger, msg);
      buff.add(msg);
    }
  }

  private void classInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String cnm = reqInfo.getParameter(BeeService.FIELD_CLASS_NAME);
    String pck = reqInfo.getParameter(BeeService.FIELD_PACKAGE_LIST);

    if (BeeUtils.isEmpty(cnm)) {
      buff.addSevere("Parameter", BeeService.FIELD_CLASS_NAME, "not found");
      return;
    }

    Set<Class<?>> classes;
    if (BeeUtils.isEmpty(pck)) {
      classes = BeeJvm.findClassWithDefaultPackages(cnm);
    } else {
      classes = BeeJvm.findClass(cnm, pck.split(","));
    }

    if (BeeUtils.isEmpty(classes)) {
      buff.addWarning("Class not found", cnm, pck);
      return;
    }
    int c = classes.size();

    buff.addSubColumns();
    buff.addPropSub(new SubProp(cnm, pck, BeeUtils.bracket(c)));

    int i = 0;
    if (c > 1) {
      for (Class<?> cls : classes) {
        i++;
        buff.addPropSub(new SubProp(cls.getName(), null,
            BeeUtils.progress(i, c)));
      }
    }

    i = 0;
    for (Class<?> cls : classes) {
      i++;
      if (c > 1) {
        buff.addPropSub(new SubProp(cls.getName(), null,
            BeeUtils.progress(i, c)));
      }
      buff.appendSub(BeeClass.getClassInfo(cls));
    }
  }

  private void connectionInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    buff.addSub(reqInfo.getInfo());
  }

  private void getResource(RequestInfo reqInfo, ResponseBuffer buff) {
    if (reqInfo.parameterEquals(0, "cs")) {
      buff.addSub(FileUtils.getCharsets());
      return;
    }
    if (reqInfo.parameterEquals(0, "fs")) {
      buff.addStringProp(PropUtils.createStringProp("Path Separator",
          File.pathSeparator, "Separator", File.separator));
      buff.addStringProp(FileUtils.getRootsInfo());
      return;
    }

    String name = reqInfo.getParameter(1);
    if (BeeUtils.isEmpty(name)) {
      buff.addSevere("resource name (parameter " + BeeService.rpcParamName(1)
          + ") not specified");
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
          buff.addResource(fl.getAbsolutePath(), s,
              BeeService.DATA_TYPE.RESOURCE);

          String ct = reqInfo.getParameter(3);
          if (!BeeUtils.isEmpty(ct)) {
            buff.setContentType(ct);
          }
          String ce = reqInfo.getParameter(4);
          if (!BeeUtils.isEmpty(ce)) {
            buff.setCharacterEncoding(ce);
          }

          return;
        }
      }
    }

    buff.addStringProp(FileUtils.getFileInfo(fl));

    if (fl.isDirectory()) {
      String arr[] = FileUtils.getFiles(fl);
      int n = BeeUtils.length(arr);

      if (n > 0) {
        buff.addStringProp(PropUtils.arrayToString("file", arr));
      } else {
        buff.addWarning("no files found");
      }
    }
  }

  private void loaderInfo(ResponseBuffer buff) {
    if (BeeJvm.CVF_FAILURE == null) {
      buff.addStringProp(BeeJvm.getLoadedClasses());
    } else {
      buff.add(BeeJvm.CVF_FAILURE);
    }
  }

  private void systemInfo(ResponseBuffer buff) {
    List<SubProp> lst = new ArrayList<SubProp>();

    lst.addAll(BeeSystem.getSysInfo());
    PropUtils.appendString(lst, "Runtime", BeeSystem.getRuntimeInfo());

    lst.addAll(BeeSystem.getPackagesInfo());

    PropUtils.appendString(lst, "Thread Static",
        BeeSystem.getThreadStaticInfo());

    Thread ct = Thread.currentThread();
    String root = "Current Thread";

    PropUtils.appendString(lst, root, BeeSystem.getThreadInfo(ct));
    PropUtils.appendString(lst, BeeUtils.concat(1, root, "Stack"),
        BeeSystem.getThreadStackInfo(ct));

    lst.addAll(BeeSystem.getThreadGroupInfo(ct.getThreadGroup(), true, true));

    PropUtils.appendString(lst, "[xml] Document Builder Factory",
        XmlUtils.getDomFactoryInfo());
    PropUtils.appendString(lst, "[xml] Document Builder",
        XmlUtils.getDomBuilderInfo());

    PropUtils.appendString(lst, "[xslt] Transformer Factory",
        XmlUtils.getXsltFactoryInfo());
    PropUtils.appendString(lst, "[xslt] Output Keys",
        XmlUtils.getOutputKeysInfo());

    buff.addSub(lst);
  }

  private void vmInfo(ResponseBuffer buff) {
    List<SubProp> lst = new ArrayList<SubProp>();

    PropUtils.appendString(lst, "Class Loading", BeeMX.getClassLoadingInfo());
    PropUtils.appendString(lst, "Compilation", BeeMX.getCompilationInfo());

    lst.addAll(BeeMX.getGarbageCollectorInfo());

    lst.addAll(BeeMX.getMemoryInfo());
    lst.addAll(BeeMX.getMemoryManagerInfo());
    lst.addAll(BeeMX.getMemoryPoolInfo());

    PropUtils.appendString(lst, "Operating System",
        BeeMX.getOperatingSystemInfo());
    lst.addAll(BeeMX.getRuntimeInfo());

    lst.addAll(BeeMX.getThreadsInfo());

    buff.addSub(lst);
  }

  private void xmlInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String src = reqInfo.getParameter(BeeService.FIELD_XML_SOURCE);
    String xsl = reqInfo.getParameter(BeeService.FIELD_XML_TRANSFORM);
    String dst = reqInfo.getParameter(BeeService.FIELD_XML_TARGET);
    String ret = reqInfo.getParameter(BeeService.FIELD_XML_RETURN);

    if (BeeUtils.isEmpty(src)) {
      buff.addSevere("Parameter", BeeService.FIELD_XML_SOURCE, "not found");
      return;
    }

    if (!FileUtils.isInputFile(src)) {
      buff.addSevere(src, "is not a valid input file");
      return;
    }

    if (BeeUtils.isEmpty(xsl)) {
      List<SubProp> lst = XmlUtils.getFileInfo(src);
      if (BeeUtils.isEmpty(lst)) {
        buff.addSevere(src, "cannot get xml info");
      } else {
        buff.addSub(lst);
      }
      return;
    }

    if (!FileUtils.isInputFile(xsl)) {
      buff.addSevere(xsl, "is not a valid input file");
      return;
    }
    if (BeeUtils.isEmpty(dst)) {
      buff.addSevere("xslt target not specified");
      return;
    }

    boolean ok = XmlUtils.xslTransform(src, xsl, dst);
    if (!ok) {
      buff.addSevere("xslt error");
      return;
    }

    if (BeeUtils.same(ret, "properties")) {
      List<SubProp> lst = XmlUtils.getFileInfo(dst);
      if (BeeUtils.isEmpty(lst)) {
        buff.addSevere(dst, "cannot get xml info");
      } else {
        buff.addSub(lst);
      }
    } else {
      String z = FileUtils.fileToString(dst);
      buff.addResource(dst, z, BeeService.DATA_TYPE.RESOURCE);
    }
  }

}
