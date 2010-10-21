package com.butent.bee.egg.server;

import com.butent.bee.egg.server.communication.ResponseBuffer;
import com.butent.bee.egg.server.http.RequestInfo;
import com.butent.bee.egg.server.utils.BeeJvm;
import com.butent.bee.egg.server.utils.BeeMX;
import com.butent.bee.egg.server.utils.BeeSystem;
import com.butent.bee.egg.server.utils.Checksum;
import com.butent.bee.egg.server.utils.XmlUtils;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.Codec;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.SubProp;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;

@Stateless
public class Invocation {
  
  public void connectionInfo(RequestInfo reqInfo, ResponseBuffer buff) {
    Assert.notNull(reqInfo);
    buff.addSub(reqInfo.getInfo());
  }

  public void loaderInfo(ResponseBuffer buff) {
    if (BeeJvm.CVF_FAILURE == null) {
      buff.addStringProp(BeeJvm.getLoadedClasses());
    } else {
      buff.add(BeeJvm.CVF_FAILURE);
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

  public void vmInfo(ResponseBuffer buff) {
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
 
}
