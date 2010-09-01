package com.butent.bee.egg.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;

import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.http.ResponseBuffer;
import com.butent.bee.egg.server.lang.StringUtils;
import com.butent.bee.egg.server.utils.BeeClass;
import com.butent.bee.egg.server.utils.BeeJvm;
import com.butent.bee.egg.server.utils.BeeMX;
import com.butent.bee.egg.server.utils.BeeSystem;
import com.butent.bee.egg.server.utils.FileUtils;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.BeeService;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

@Stateless
public class SystemServiceBean {
  private static Logger logger = Logger.getLogger(SystemServiceBean.class
      .getName());

  public void doService(String svc, RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notEmpty(svc);
    Assert.notNull(buff);

    if (svc.equals(BeeService.SERVICE_TEST_CONNECTION))
      connectionInfo(reqInfo, buff);
    else if (svc.equals(BeeService.SERVICE_SERVER_INFO))
      systemInfo(buff);
    else if (svc.equals(BeeService.SERVICE_VM_INFO))
      vmInfo(buff);
    else if (svc.equals(BeeService.SERVICE_LOADER_INFO))
      loaderInfo(buff);
    else if (svc.equals(BeeService.SERVICE_CLASS_INFO))
      classInfo(reqInfo, buff);
    else if (svc.equals(BeeService.SERVICE_XML_INFO))
      xmlInfo(reqInfo, buff);
    else {
      String msg = BeeUtils.concat(1, svc, "system service not recognized");
      logger.warning(msg);
      buff.add(msg);
    }
  }

  private void connectionInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    buff.addSub(reqInfo.getInfo());
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

  private void loaderInfo(ResponseBuffer buff) {
    if (BeeJvm.CVF_FAILURE == null)
      buff.addStringProp(BeeJvm.getLoadedClasses());
    else
      buff.add(BeeJvm.CVF_FAILURE);
  }

  private void classInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String xml = reqInfo.getContent();
    if (BeeUtils.isEmpty(xml)) {
      buff.add("Request data not found");
      return;
    }

    Map<String, String> fields = XmlUtils.getText(xml);
    if (BeeUtils.isEmpty(fields)) {
      buff.addLine("No elements with text found in", xml);
      return;
    }

    String cnm = fields.get(BeeService.FIELD_CLASS_NAME);
    String pck = fields.get(BeeService.FIELD_PACKAGE_LIST);

    if (BeeUtils.isEmpty(cnm)) {
      buff.addLine("Parameter", BeeService.FIELD_CLASS_NAME, "not found in",
          xml);
      return;
    }

    Set<Class<?>> classes;
    if (BeeUtils.isEmpty(pck))
      classes = BeeJvm.findClassWithDefaultPackages(cnm);
    else
      classes = BeeJvm.findClass(cnm, StringUtils.split(pck, " ,;"));

    if (BeeUtils.isEmpty(classes)) {
      buff.addLine("Class not found", cnm, pck);
      return;
    }
    int c = classes.size();

    buff.addSubColumns();
    buff.addPropSub(new SubProp(cnm, pck, BeeUtils.bracket(c)));

    int i = 0;
    if (c > 1)
      for (Class<?> cls : classes) {
        i++;
        buff.addPropSub(new SubProp(cls.getName(), null, BeeUtils
            .progress(i, c)));
      }

    i = 0;
    for (Class<?> cls : classes) {
      i++;
      if (c > 1)
        buff.addPropSub(new SubProp(cls.getName(), null, BeeUtils
            .progress(i, c)));
      buff.appendSub(BeeClass.getClassInfo(cls));
    }
  }

  private void xmlInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String reqData = reqInfo.getContent();
    if (BeeUtils.isEmpty(reqData)) {
      buff.add("Request data not found");
      return;
    }

    String fileName = XmlUtils.getText(reqData, BeeService.FIELD_XML_FILE);
    if (BeeUtils.isEmpty(fileName)) {
      buff.addLine("Parameter", BeeService.FIELD_XML_FILE, "not found");
      return;
    }

    if (!FileUtils.isInputFile(fileName)) {
      buff.addLine(fileName, "is not a valid input file");
      return;
    }

    List<SubProp> lst = XmlUtils.getFileInfo(fileName);

    if (BeeUtils.isEmpty(lst))
      buff.addLine(fileName, "cannot get xml info");
    else
      buff.addSub(lst);
  }

}
