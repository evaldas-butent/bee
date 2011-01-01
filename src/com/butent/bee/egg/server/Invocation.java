package com.butent.bee.egg.server;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.JvmUtils;
import com.butent.bee.egg.server.utils.MxUtils;
import com.butent.bee.egg.server.utils.BeeSystem;
import com.butent.bee.egg.server.utils.Checksum;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;
import com.butent.bee.egg.shared.utils.PropertyUtils;
import com.butent.bee.egg.shared.utils.ExtendedProperty;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

@Stateless
public class Invocation {
  
  public void connectionInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    buff.addExtendedProperties(reqInfo.getInfo());
  }

  public void loaderInfo(ResponseBuffer buff) {
    if (JvmUtils.CVF_FAILURE == null) {
      buff.addProperties(JvmUtils.getLoadedClasses());
    } else {
      buff.add(JvmUtils.CVF_FAILURE);
    }
  }

  public void stringInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    String data = reqInfo.getContent();
    if (BeeUtils.length(data) <= 0) {
      buff.addSevere("Request data not found");
      return;
    }
    
    buff.addBinary(data);
    
    byte[] arr = Codec.toBytes(data); 
    
    buff.addOff("length", data.length());
    buff.addOff("adler32.z", Checksum.adler32(arr));
    buff.addOff("crc32.z", Checksum.crc32(arr));

    buff.addOff("adler32", Codec.adler32(arr));
    buff.addOff("crc16", Codec.crc16(arr));
    buff.addOff("crc32", Codec.crc32(arr));
    buff.addOff("crc32d", Codec.crc32Direct(arr));
  }

  public void systemInfo(ResponseBuffer buff) {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    lst.addAll(BeeSystem.getSysInfo());
    PropertyUtils.appendChildrenToExtended(lst, "Runtime", BeeSystem.getRuntimeInfo());

    lst.addAll(BeeSystem.getPackagesInfo());

    PropertyUtils.appendChildrenToExtended(lst, "Thread Static", BeeSystem.getThreadStaticInfo());

    Thread ct = Thread.currentThread();
    String root = "Current Thread";

    PropertyUtils.appendChildrenToExtended(lst, root, BeeSystem.getThreadInfo(ct));
    PropertyUtils.appendChildrenToExtended(lst, BeeUtils.concat(1, root, "Stack"),
        BeeSystem.getThreadStackInfo(ct));

    lst.addAll(BeeSystem.getThreadGroupInfo(ct.getThreadGroup(), true, true));

    PropertyUtils.appendChildrenToExtended(lst, "[xml] Document Builder Factory",
        XmlUtils.getDomFactoryInfo());
    PropertyUtils.appendChildrenToExtended(lst, "[xml] Document Builder",
        XmlUtils.getDomBuilderInfo());

    PropertyUtils.appendChildrenToExtended(lst, "[xslt] Transformer Factory",
        XmlUtils.getXsltFactoryInfo());
    PropertyUtils.appendChildrenToExtended(lst, "[xslt] Output Keys",
        XmlUtils.getOutputKeysInfo());

    buff.addExtendedProperties(lst);
  }

  public void vmInfo(ResponseBuffer buff) {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.appendChildrenToExtended(lst, "Class Loading", MxUtils.getClassLoadingInfo());
    PropertyUtils.appendChildrenToExtended(lst, "Compilation", MxUtils.getCompilationInfo());

    lst.addAll(MxUtils.getGarbageCollectorInfo());

    lst.addAll(MxUtils.getMemoryInfo());
    lst.addAll(MxUtils.getMemoryManagerInfo());
    lst.addAll(MxUtils.getMemoryPoolInfo());

    PropertyUtils.appendChildrenToExtended(lst, "Operating System", MxUtils.getOperatingSystemInfo());
    lst.addAll(MxUtils.getRuntimeInfo());

    lst.addAll(MxUtils.getThreadsInfo());

    buff.addExtendedProperties(lst);
  }
}
