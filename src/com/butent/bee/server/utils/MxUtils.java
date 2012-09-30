package com.butent.bee.server.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.utils.ArrayUtils;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.MonitorInfo;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Enables to monitor server memory usage and other performance related parameters.
 */

public class MxUtils {
  public static List<Property> getClassLoadingInfo() {
    List<Property> lst = new ArrayList<Property>();

    ClassLoadingMXBean mxb = ManagementFactory.getClassLoadingMXBean();

    PropertyUtils.addProperties(lst, "Loaded Class Count", mxb.getLoadedClassCount(),
        "Total Loaded Class Count", mxb.getTotalLoadedClassCount(),
        "Unloaded Class Count", mxb.getUnloadedClassCount(),
        "Is Verbose", mxb.isVerbose());
    return lst;
  }

  public static List<Property> getCompilationInfo() {
    List<Property> lst = new ArrayList<Property>();

    CompilationMXBean mxb = ManagementFactory.getCompilationMXBean();

    PropertyUtils.addProperties(lst, "Name", mxb.getName(),
        "Total Compilation Time", mxb.getTotalCompilationTime(),
        "Is Compilation Time Monitoring Supported", mxb.isCompilationTimeMonitoringSupported());
    return lst;
  }

  public static List<ExtendedProperty> getGarbageCollectorInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    List<GarbageCollectorMXBean> mxbs = ManagementFactory.getGarbageCollectorMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropertyUtils.addExtended(lst, "Garbage Collectors", BeeUtils.bracket(mxbs.size()));

      for (GarbageCollectorMXBean b : mxbs) {
        String nm = b.getName();

        PropertyUtils.addChildren(lst, nm, "Collection Count", b.getCollectionCount(),
            "Collection Time", b.getCollectionTime(),
            "Memory Pool Names", transformMemoryPoolNames(b.getMemoryPoolNames()),
            "Is Valid", b.isValid());
      }
    }
    return lst;
  }

  public static List<ExtendedProperty> getMemoryInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    MemoryMXBean mxb = ManagementFactory.getMemoryMXBean();
    String nm = "Memory";

    PropertyUtils.addProperties(lst, true,
        nm, "Object Pending Finalization Count", mxb.getObjectPendingFinalizationCount(),
        nm, "Is Verbose", mxb.isVerbose());

    PropertyUtils.appendChildrenToExtended(lst, "Heap Memory Usage",
        getMemoryUsageInfo(mxb.getHeapMemoryUsage()));
    PropertyUtils.appendChildrenToExtended(lst, "Non Heap Memory Usage",
        getMemoryUsageInfo(mxb.getNonHeapMemoryUsage()));

    return lst;
  }

  public static List<ExtendedProperty> getMemoryManagerInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    List<MemoryManagerMXBean> mxbs = ManagementFactory.getMemoryManagerMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropertyUtils.addExtended(lst, "Memory Managers", BeeUtils.bracket(mxbs.size()));

      for (MemoryManagerMXBean b : mxbs) {
        String nm = b.getName();

        PropertyUtils.addChildren(lst, nm,
            "Memory Pool Names", transformMemoryPoolNames(b.getMemoryPoolNames()),
            "Is Valid", b.isValid());
      }
    }
    return lst;
  }

  public static List<ExtendedProperty> getMemoryPoolInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    List<MemoryPoolMXBean> mxbs = ManagementFactory.getMemoryPoolMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropertyUtils.addExtended(lst, "Memory Pools", BeeUtils.bracket(mxbs.size()));

      for (MemoryPoolMXBean b : mxbs) {
        String root = b.getName();

        PropertyUtils.addChildren(lst, root, "Type", b.getType(),
            "Memory Manager Names", transformMemoryManagerNames(b.getMemoryManagerNames()),
            "Is Collection Usage Threshold Supported", b.isCollectionUsageThresholdSupported(),
            "Is Usage Threshold Supported", b.isUsageThresholdSupported(),
            "Is Valid", b.isValid());

        if (b.isCollectionUsageThresholdSupported()) {
          PropertyUtils.addChildren(lst, root,
              "Collection Usage Threshold", b.getCollectionUsageThreshold(),
              "Collection Usage Threshold Count", b.getCollectionUsageThresholdCount(),
              "Is Collection Usage Threshold Exceeded", b.isCollectionUsageThresholdExceeded());
        }

        if (b.isUsageThresholdSupported()) {
          PropertyUtils.addChildren(lst, root,
              "Usage Threshold", b.getUsageThreshold(),
              "Usage Threshold Count", b.getUsageThresholdCount(),
              "Is Usage Threshold Exceeded", b.isUsageThresholdExceeded());
        }

        PropertyUtils.appendChildrenToExtended(lst, "Collection Usage",
            getMemoryUsageInfo(b.getCollectionUsage()));
        PropertyUtils.appendChildrenToExtended(lst, "Peak Usage",
            getMemoryUsageInfo(b.getPeakUsage()));
        PropertyUtils.appendChildrenToExtended(lst, "Usage", getMemoryUsageInfo(b.getUsage()));
      }
    }

    return lst;
  }

  public static List<Property> getOperatingSystemInfo() {
    List<Property> lst = new ArrayList<Property>();

    OperatingSystemMXBean mxb = ManagementFactory.getOperatingSystemMXBean();

    PropertyUtils.addProperties(lst, "Name", mxb.getName(),
        "Version", mxb.getVersion(),
        "Arch", mxb.getArch(),
        "Available Processors", mxb.getAvailableProcessors(),
        "System Load Average", mxb.getSystemLoadAverage());
    return lst;
  }

  public static List<ExtendedProperty> getRuntimeInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    RuntimeMXBean mxb = ManagementFactory.getRuntimeMXBean();
    String root = "Runtime";

    PropertyUtils.addChildren(lst, root, "Boot Class Path", mxb.getBootClassPath(),
        "Class Path", mxb.getClassPath(), "Library Path", mxb.getLibraryPath(),
        "Management Spec Version", mxb.getManagementSpecVersion(), "Name", mxb.getName(),
        "Spec Name", mxb.getSpecName(), "Spec Vendor", mxb.getSpecVendor(),
        "Spec Version", mxb.getSpecVersion(), "Start Time", new Date(mxb.getStartTime()),
        "Uptime", mxb.getUptime(), "Vm Name", mxb.getVmName(), "Vm Vendor", mxb.getVmVendor(),
        "Vm Version", mxb.getVmVersion(),
        "Is Boot Class Path Supported", mxb.isBootClassPathSupported());

    List<String> args = mxb.getInputArguments();
    if (!BeeUtils.isEmpty(args)) {
      PropertyUtils.addExtended(lst, root, "Input Arguments", BeeUtils.bracket(args.size()));

      for (String s : args) {
        PropertyUtils.addExtended(lst, root, "Input Argument", s);
      }
    }

    Map<String, String> prp = mxb.getSystemProperties();
    if (!BeeUtils.isEmpty(prp)) {
      PropertyUtils.addExtended(lst, root, "System Properties", BeeUtils.bracket(prp.size()));

      for (Map.Entry<String, String> el : prp.entrySet()) {
        PropertyUtils.addExtended(lst, "System Property", el.getKey(), el.getValue());
      }
    }
    return lst;
  }

  public static List<ExtendedProperty> getThreadInfo(ThreadInfo ti, String msg) {
    Assert.notNull(ti);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    String root = BeeUtils.joinWords("Thread Id", ti.getThreadId(), msg);

    PropertyUtils.addChildren(lst, root, "Blocked Count", ti.getBlockedCount(),
        "Blocked Time", ti.getBlockedTime(), "Lock Info", transformLockInfo(ti.getLockInfo()),
        "Lock Name", ti.getLockName(), "Lock Owner Id", ti.getLockOwnerId(),
        "Lock Owner Name", ti.getLockOwnerName(), "Thread Name", ti.getThreadName(),
        "Thread State", ti.getThreadState(), "Waited Count", ti.getWaitedCount(),
        "Waited Time", ti.getWaitedTime(), "Is In Native", ti.isInNative(),
        "Is Suspended", ti.isSuspended());

    MonitorInfo[] monitors = ti.getLockedMonitors();
    if (monitors != null) {
      if (monitors.length > 1) {
        PropertyUtils.addExtended(lst, root, "Locked Monitors", BeeUtils.bracket(monitors.length));
      }

      for (MonitorInfo inf : monitors) {
        PropertyUtils.addExtended(lst, root, "Locked Monitor", transformMonitorInfo(inf));
      }
    }

    LockInfo[] lckArr = ti.getLockedSynchronizers();
    if (lckArr != null) {
      if (lckArr.length > 1) {
        PropertyUtils.addExtended(lst, root, "Locked Synchronizers",
            BeeUtils.bracket(lckArr.length));
      }

      for (LockInfo inf : lckArr) {
        PropertyUtils.addExtended(lst, root, "Locked Synchronizer", transformLockInfo(inf));
      }
    }

    StackTraceElement[] stack = ti.getStackTrace();
    if (stack != null) {
      if (stack.length > 1) {
        PropertyUtils.addExtended(lst, root, "Stack Trace", BeeUtils.bracket(stack.length));
      }

      for (int i = 0; i < stack.length; i++) {
        PropertyUtils.addExtended(lst, root, "Stack Trace Element " + i,
            SystemInfo.transformStackTraceElement(stack[i]));
      }
    }

    return lst;
  }

  public static List<ExtendedProperty> getThreadsInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
    String root = "Threads";

    PropertyUtils.addChildren(lst, root,
        "Current Thread Cpu Time", mxb.getCurrentThreadCpuTime(),
        "Current Thread User Time", mxb.getCurrentThreadUserTime(),
        "Daemon Thread Count", mxb.getDaemonThreadCount(),
        "Peak Thread Count", mxb.getPeakThreadCount(), "Thread Count", mxb.getThreadCount(),
        "Total Started Thread Count", mxb.getTotalStartedThreadCount(),
        "Is Current Thread Cpu Time Supported", mxb.isCurrentThreadCpuTimeSupported(),
        "Is Object Monitor Usage Supported", mxb.isObjectMonitorUsageSupported(),
        "Is Synchronizer Usage Supported", mxb.isSynchronizerUsageSupported(),
        "Is Thread Contention Monitoring Enabled", mxb.isThreadContentionMonitoringEnabled(),
        "Is Thread Contention Monitoring Supported", mxb.isThreadContentionMonitoringSupported(),
        "Is Thread Cpu Time Enabled", mxb.isThreadCpuTimeEnabled(),
        "Is Thread Cpu Time Supported", mxb.isThreadCpuTimeSupported());

    long[] idArr = mxb.findDeadlockedThreads();
    if (idArr != null) {
      for (int i = 0; i < idArr.length; i++) {
        PropertyUtils.addExtended(lst, root, "Deadlocked Thread", idArr[i]);
      }
    }

    idArr = mxb.findMonitorDeadlockedThreads();
    if (idArr != null) {
      for (int i = 0; i < idArr.length; i++) {
        PropertyUtils.addExtended(lst, root, "Monitor Deadlocked Thread", idArr[i]);
      }
    }

    ThreadInfo[] tiArr = mxb.dumpAllThreads(true, true);
    if (tiArr != null) {
      if (mxb.isThreadCpuTimeSupported() && mxb.isThreadCpuTimeEnabled()) {
        ThreadInfo ti;
        for (int i = 0; i < tiArr.length; i++) {
          ti = tiArr[i];
          long id = ti.getThreadId();

          PropertyUtils.addChildren(lst,
              BeeUtils.joinWords("Thread Id", id, BeeUtils.progress(i + 1, tiArr.length)),
              "Thread Cpu Time", mxb.getThreadCpuTime(id),
              "Thread User Time", mxb.getThreadUserTime(id));
        }
      }

      ThreadInfo ti;
      for (int i = 0; i < tiArr.length; i++) {
        ti = tiArr[i];
        lst.addAll(getThreadInfo(ti, BeeUtils.progress(i + 1, tiArr.length)));
      }
    }

    return lst;
  }

  private static List<Property> getMemoryUsageInfo(MemoryUsage mu) {
    if (mu == null) {
      return null;
    }
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst, "Committed", mu.getCommitted(), "Init", mu.getInit(),
        "Max", mu.getMax(), "Used", mu.getUsed());
    return lst;
  }

  private static String transformLockInfo(LockInfo inf) {
    if (inf == null) {
      return null;
    } else {
      return inf.toString();
    }
  }

  private static String transformMemoryManagerNames(String[] names) {
    if (names == null) {
      return null;
    } else {
      return ArrayUtils.transform(names);
    }
  }

  private static String transformMemoryPoolNames(String[] names) {
    if (names == null) {
      return null;
    } else {
      return ArrayUtils.transform(names);
    }
  }

  private static String transformMonitorInfo(MonitorInfo inf) {
    if (inf == null) {
      return null;
    } else {
      return inf.toString();
    }
  }

  private MxUtils() {
  }
}
