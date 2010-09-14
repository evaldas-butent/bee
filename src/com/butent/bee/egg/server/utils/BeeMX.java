package com.butent.bee.egg.server.utils;

import com.butent.bee.egg.shared.Assert;
import com.butent.bee.egg.shared.utils.BeeUtils;
import com.butent.bee.egg.shared.utils.PropUtils;
import com.butent.bee.egg.shared.utils.StringProp;
import com.butent.bee.egg.shared.utils.SubProp;

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

public class BeeMX {
  public static List<StringProp> getClassLoadingInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    ClassLoadingMXBean mxb = ManagementFactory.getClassLoadingMXBean();

    PropUtils.addString(lst, "Loaded Class Count", mxb.getLoadedClassCount(),
        "Total Loaded Class Count", mxb.getTotalLoadedClassCount(),
        "Unloaded Class Count", mxb.getUnloadedClassCount(), "Is Verbose",
        mxb.isVerbose());

    return lst;
  }

  public static List<StringProp> getCompilationInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    CompilationMXBean mxb = ManagementFactory.getCompilationMXBean();

    PropUtils.addString(lst, "Name", mxb.getName(), "Total Compilation Time",
        mxb.getTotalCompilationTime(),
        "Is Compilation Time Monitoring Supported",
        mxb.isCompilationTimeMonitoringSupported());

    return lst;
  }

  public static List<SubProp> getGarbageCollectorInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    List<GarbageCollectorMXBean> mxbs = ManagementFactory.getGarbageCollectorMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropUtils.addSub(lst, "Garbage Collectors", BeeUtils.bracket(mxbs.size()));

      for (GarbageCollectorMXBean b : mxbs) {
        String nm = b.getName();

        PropUtils.addRoot(lst, nm, "Collection Count", b.getCollectionCount(),
            "Collection Time", b.getCollectionTime(), "Memory Pool Names",
            transformMemoryPoolNames(b.getMemoryPoolNames()), "Is Valid",
            b.isValid());
      }
    }

    return lst;
  }

  public static List<SubProp> getMemoryInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    MemoryMXBean mxb = ManagementFactory.getMemoryMXBean();
    String nm = "Memory";

    PropUtils.addPropSub(lst, true, nm, "Object Pending Finalization Count",
        mxb.getObjectPendingFinalizationCount(), nm, "Is Verbose",
        mxb.isVerbose());

    PropUtils.appendString(lst, "Heap Memory Usage",
        getMemoryUsageInfo(mxb.getHeapMemoryUsage()));
    PropUtils.appendString(lst, "Non Heap Memory Usage",
        getMemoryUsageInfo(mxb.getNonHeapMemoryUsage()));

    return lst;
  }

  public static List<SubProp> getMemoryManagerInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    List<MemoryManagerMXBean> mxbs = ManagementFactory.getMemoryManagerMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropUtils.addSub(lst, "Memory Managers", BeeUtils.bracket(mxbs.size()));

      for (MemoryManagerMXBean b : mxbs) {
        String nm = b.getName();

        PropUtils.addRoot(lst, nm, "Memory Pool Names",
            transformMemoryPoolNames(b.getMemoryPoolNames()), "Is Valid",
            b.isValid());
      }
    }

    return lst;
  }

  public static List<SubProp> getMemoryPoolInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    List<MemoryPoolMXBean> mxbs = ManagementFactory.getMemoryPoolMXBeans();

    if (!BeeUtils.isEmpty(mxbs)) {
      PropUtils.addSub(lst, "Memory Pools", BeeUtils.bracket(mxbs.size()));

      for (MemoryPoolMXBean b : mxbs) {
        String root = b.getName();

        PropUtils.addRoot(lst, root, "Type", b.getType(),
            "Memory Manager Names",
            transformMemoryManagerNames(b.getMemoryManagerNames()),
            "Is Collection Usage Threshold Supported",
            b.isCollectionUsageThresholdSupported(),
            "Is Usage Threshold Supported", b.isUsageThresholdSupported(),
            "Is Valid", b.isValid());

        if (b.isCollectionUsageThresholdSupported()) {
          PropUtils.addRoot(lst, root, "Collection Usage Threshold",
              b.getCollectionUsageThreshold(),
              "Collection Usage Threshold Count",
              b.getCollectionUsageThresholdCount(),
              "Is Collection Usage Threshold Exceeded",
              b.isCollectionUsageThresholdExceeded());
        }

        if (b.isUsageThresholdSupported()) {
          PropUtils.addRoot(lst, root, "Usage Threshold",
              b.getUsageThreshold(), "Usage Threshold Count",
              b.getUsageThresholdCount(), "Is Usage Threshold Exceeded",
              b.isUsageThresholdExceeded());
        }

        PropUtils.appendString(lst, "Collection Usage",
            getMemoryUsageInfo(b.getCollectionUsage()));
        PropUtils.appendString(lst, "Peak Usage",
            getMemoryUsageInfo(b.getPeakUsage()));
        PropUtils.appendString(lst, "Usage", getMemoryUsageInfo(b.getUsage()));
      }
    }

    return lst;
  }

  public static List<StringProp> getOperatingSystemInfo() {
    List<StringProp> lst = new ArrayList<StringProp>();

    OperatingSystemMXBean mxb = ManagementFactory.getOperatingSystemMXBean();

    PropUtils.addString(lst, "Name", mxb.getName(), "Version",
        mxb.getVersion(), "Arch", mxb.getArch(), "Available Processors",
        mxb.getAvailableProcessors(), "System Load Average",
        mxb.getSystemLoadAverage());

    return lst;
  }

  public static List<SubProp> getRuntimeInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    RuntimeMXBean mxb = ManagementFactory.getRuntimeMXBean();
    String root = "Runtime";

    PropUtils.addRoot(lst, root, "Boot Class Path", mxb.getBootClassPath(),
        "Class Path", mxb.getClassPath(), "Library Path", mxb.getLibraryPath(),
        "Management Spec Version", mxb.getManagementSpecVersion(), "Name",
        mxb.getName(), "Spec Name", mxb.getSpecName(), "Spec Vendor",
        mxb.getSpecVendor(), "Spec Version", mxb.getSpecVersion(),
        "Start Time", new Date(mxb.getStartTime()), "Uptime", mxb.getUptime(),
        "Vm Name", mxb.getVmName(), "Vm Vendor", mxb.getVmVendor(),
        "Vm Version", mxb.getVmVersion(), "Is Boot Class Path Supported",
        mxb.isBootClassPathSupported());

    List<String> args = mxb.getInputArguments();
    if (!BeeUtils.isEmpty(args)) {
      PropUtils.addSub(lst, root, "Input Arguments",
          BeeUtils.bracket(args.size()));

      for (String s : args) {
        PropUtils.addSub(lst, root, "Input Argument", s);
      }
    }

    Map<String, String> prp = mxb.getSystemProperties();
    if (!BeeUtils.isEmpty(prp)) {
      PropUtils.addSub(lst, root, "System Properties",
          BeeUtils.bracket(prp.size()));

      for (Map.Entry<String, String> el : prp.entrySet()) {
        PropUtils.addSub(lst, "System Property", el.getKey(), el.getValue());
      }
    }

    return lst;
  }

  public static List<SubProp> getThreadInfo(ThreadInfo ti, String msg) {
    Assert.notNull(ti);
    List<SubProp> lst = new ArrayList<SubProp>();

    String root = BeeUtils.concat(1, "Thread Id", ti.getThreadId(), msg);

    PropUtils.addRoot(lst, root, "Blocked Count", ti.getBlockedCount(),
        "Blocked Time", ti.getBlockedTime(), "Lock Info",
        transformLockInfo(ti.getLockInfo()), "Lock Name", ti.getLockName(),
        "Lock Owner Id", ti.getLockOwnerId(), "Lock Owner Name",
        ti.getLockOwnerName(), "Thread Name", ti.getThreadName(),
        "Thread State", ti.getThreadState(), "Waited Count",
        ti.getWaitedCount(), "Waited Time", ti.getWaitedTime(), "Is In Native",
        ti.isInNative(), "Is Suspended", ti.isSuspended());

    MonitorInfo[] monitors = ti.getLockedMonitors();
    if (!BeeUtils.isEmpty(monitors)) {
      if (monitors.length > 1) {
        PropUtils.addSub(lst, root, "Locked Monitors",
            BeeUtils.bracket(monitors.length));
      }

      for (MonitorInfo inf : monitors) {
        PropUtils.addSub(lst, root, "Locked Monitor", transformMonitorInfo(inf));
      }
    }

    LockInfo[] lckArr = ti.getLockedSynchronizers();
    if (!BeeUtils.isEmpty(lckArr)) {
      if (lckArr.length > 1) {
        PropUtils.addSub(lst, root, "Locked Synchronizers",
            BeeUtils.bracket(lckArr.length));
      }

      for (LockInfo inf : lckArr) {
        PropUtils.addSub(lst, root, "Locked Synchronizer",
            transformLockInfo(inf));
      }
    }

    StackTraceElement[] stack = ti.getStackTrace();
    if (!BeeUtils.isEmpty(stack)) {
      if (stack.length > 1) {
        PropUtils.addSub(lst, root, "Stack Trace",
            BeeUtils.bracket(stack.length));
      }

      for (int i = 0; i < stack.length; i++) {
        PropUtils.addSub(lst, root, "Stack Trace Element " + i,
            BeeSystem.transformStackTraceElement(stack[i]));
      }
    }

    return lst;
  }

  public static List<SubProp> getThreadsInfo() {
    List<SubProp> lst = new ArrayList<SubProp>();

    ThreadMXBean mxb = ManagementFactory.getThreadMXBean();
    String root = "Threads";

    PropUtils.addRoot(lst, root, "Current Thread Cpu Time",
        mxb.getCurrentThreadCpuTime(), "Current Thread User Time",
        mxb.getCurrentThreadUserTime(), "Daemon Thread Count",
        mxb.getDaemonThreadCount(), "Peak Thread Count",
        mxb.getPeakThreadCount(), "Thread Count", mxb.getThreadCount(),
        "Total Started Thread Count", mxb.getTotalStartedThreadCount(),
        "Is Current Thread Cpu Time Supported",
        mxb.isCurrentThreadCpuTimeSupported(),
        "Is Object Monitor Usage Supported",
        mxb.isObjectMonitorUsageSupported(), "Is Synchronizer Usage Supported",
        mxb.isSynchronizerUsageSupported(),
        "Is Thread Contention Monitoring Enabled",
        mxb.isThreadContentionMonitoringEnabled(),
        "Is Thread Contention Monitoring Supported",
        mxb.isThreadContentionMonitoringSupported(),
        "Is Thread Cpu Time Enabled", mxb.isThreadCpuTimeEnabled(),
        "Is Thread Cpu Time Supported", mxb.isThreadCpuTimeSupported());

    long[] idArr = mxb.findDeadlockedThreads();
    if (!BeeUtils.isEmpty(idArr)) {
      for (int i = 0; i < idArr.length; i++) {
        PropUtils.addSub(lst, root, "Deadlocked Thread", idArr[i]);
      }
    }

    idArr = mxb.findMonitorDeadlockedThreads();
    if (!BeeUtils.isEmpty(idArr)) {
      for (int i = 0; i < idArr.length; i++) {
        PropUtils.addSub(lst, root, "Monitor Deadlocked Thread", idArr[i]);
      }
    }

    ThreadInfo[] tiArr = mxb.dumpAllThreads(true, true);
    if (!BeeUtils.isEmpty(tiArr)) {
      if (mxb.isThreadCpuTimeSupported() && mxb.isThreadCpuTimeEnabled()) {
        ThreadInfo ti;
        for (int i = 0; i < tiArr.length; i++) {
          ti = tiArr[i];
          long id = ti.getThreadId();

          PropUtils.addRoot(
              lst,
              BeeUtils.concat(1, "Thread Id", id,
                  BeeUtils.progress(i + 1, tiArr.length)), "Thread Cpu Time",
              mxb.getThreadCpuTime(id), "Thread User Time",
              mxb.getThreadUserTime(id));
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

  private static List<StringProp> getMemoryUsageInfo(MemoryUsage mu) {
    if (mu == null) {
      return null;
    }
    List<StringProp> lst = new ArrayList<StringProp>();

    PropUtils.addString(lst, "Committed", mu.getCommitted(), "Init",
        mu.getInit(), "Max", mu.getMax(), "Used", mu.getUsed());

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
      return BeeUtils.transformArray(names);
    }
  }

  private static String transformMemoryPoolNames(String[] names) {
    if (names == null) {
      return null;
    } else {
      return BeeUtils.transformArray(names);
    }
  }

  private static String transformMonitorInfo(MonitorInfo inf) {
    if (inf == null) {
      return null;
    } else {
      return inf.toString();
    }
  }

}
