package com.butent.bee.server.utils;

import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SystemInfo {
  private static class PackageComparator implements Comparator<Package> {
    public int compare(Package p1, Package p2) {
      return p1.getName().compareTo(p2.getName());
    }
  }

  private static PackageComparator packageComparator = null;

  public static long freeMemory() {
    return Runtime.getRuntime().freeMemory();
  }

  public static List<Property> getPackageInfo(Package p, boolean withName) {
    Assert.notNull(p);
    List<Property> lst = new ArrayList<Property>();

    if (withName) {
      PropertyUtils.addProperty(lst, "Name", p.getName());
    }
    PropertyUtils.addProperties(lst, "Implementation Title",
        p.getImplementationTitle(), "Implementation Vendor",
        p.getImplementationVendor(), "Implementation Version",
        p.getImplementationVersion(), "Specification Title",
        p.getSpecificationTitle(), "SpecificationVendor",
        p.getSpecificationVendor(), "Specification Version",
        p.getSpecificationVersion(), "Is Sealed",
        p.isSealed() ? Boolean.toString(true) : BeeConst.STRING_EMPTY);

    Annotation[] arr = p.getDeclaredAnnotations();
    if (!BeeUtils.isEmpty(arr)) {
      for (Annotation ann : arr) {
        PropertyUtils.addProperty(lst, "Declared Annotation", ClassUtils.transformAnnotation(ann));
      }
    }

    arr = p.getAnnotations();
    if (!BeeUtils.isEmpty(arr)) {
      for (Annotation ann : arr) {
        PropertyUtils.addProperty(lst, "Annotation",  ClassUtils.transformAnnotation(ann));
      }
    }

    return lst;
  }

  public static List<ExtendedProperty> getPackagesInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    Package[] pArr = Package.getPackages();

    if (!BeeUtils.isEmpty(pArr)) {
      if (pArr.length > 1) {
        PropertyUtils.addExtended(lst, "Packages", BeeUtils.bracket(pArr.length));
        Arrays.sort(pArr, ensurePackageComparator());
      }

      Package p;
      String nm;

      for (int i = 0; i < pArr.length; i++) {
        p = pArr[i];
        nm = p.getName();

        PropertyUtils.addExtended(lst, nm, BeeUtils.progress(i + 1, pArr.length));
        PropertyUtils.appendChildrenToExtended(lst, nm, getPackageInfo(p, false));
      }
    }

    return lst;
  }

  public static List<Property> getRuntimeInfo() {
    Runtime rt = Runtime.getRuntime();
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst, "Available Processors", rt.availableProcessors(),
        "Free Memory", rt.freeMemory(), "Max Memory", rt.maxMemory(),
        "Total Memory", rt.totalMemory());

    return lst;
  }

  public static List<ExtendedProperty> getSysInfo() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.addExtended(lst, "Current Time Millis", System.currentTimeMillis());
    PropertyUtils.addExtended(lst, "Nano Time", System.nanoTime());

    int i, c;

    Map<String, String> env = System.getenv();
    if (env != null) {
      i = 0;
      c = env.size();

      for (Map.Entry<String, String> it : env.entrySet()) {
        i++;
        PropertyUtils.addExtended(lst, "env " + BeeUtils.progress(i, c), it.getKey(),
            it.getValue());
      }
    }

    Properties prp = System.getProperties();

    if (prp != null) {
      i = 0;
      c = prp.size();
      Object key;

      for (Enumeration<?> names = prp.propertyNames(); names.hasMoreElements();) {
        key = names.nextElement();
        i++;
        PropertyUtils.addExtended(lst, "property " + BeeUtils.progress(i, c), key,
            prp.getProperty(key.toString()));
      }
    }

    return lst;
  }

  public static List<ExtendedProperty> getThreadGroupInfo(ThreadGroup tg,
      boolean recurse, boolean stack) {
    Assert.notNull(tg);
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();

    PropertyUtils.addChildren(lst, "Thread Group",
        "Name", tg.getName(), "Parent", transformThreadGroup(tg.getParent()),
        "Max Priority", tg.getMaxPriority(), "Is Daemon", tg.isDaemon(),
        "Is Destroyed", tg.isDestroyed());

    int tc = tg.activeCount();
    int gc = tg.activeGroupCount();

    PropertyUtils.addProperties(lst, false, "Active Count", tc, "Active Group Count",
        gc);

    int n;

    if (tc > 0) {
      Thread[] tArr = new Thread[tc];
      n = tg.enumerate(tArr, recurse);

      if (n > 0) {
        for (int i = 0; i < n; i++) {
          PropertyUtils.addExtended(lst, "Thread", BeeUtils.progress(i + 1, n),
              transformThread(tArr[i]));
          if (stack) {
            PropertyUtils.appendChildrenToExtended(lst, "Stack Trace",
                getThreadStackInfo(tArr[i]));
          }
        }
      }
    }

    if (gc > 0) {
      ThreadGroup[] gArr = new ThreadGroup[gc];
      n = tg.enumerate(gArr, recurse);

      if (n > 0) {
        for (int i = 0; i < n; i++) {
          PropertyUtils.addExtended(lst, "Thread Group", BeeUtils.progress(i + 1, n),
              transformThreadGroup(gArr[i]));
        }
      }
    }

    return lst;
  }

  public static List<Property> getThreadInfo(Thread t) {
    Assert.notNull(t);
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst, "Id", t.getId(), "Name", t.getName(), "State",
        t.getState(), "Thread Group", transformThreadGroup(t.getThreadGroup()),
        "Uncaught Exception Handler", t.getUncaughtExceptionHandler(),
        "Is Alive", t.isAlive(), "Is Daemon", t.isDaemon(), "Is Interrupted", t.isInterrupted());

    return lst;
  }

  public static List<Property> getThreadStackInfo(Thread t) {
    Assert.notNull(t);
    List<Property> lst = new ArrayList<Property>();

    StackTraceElement[] arr = t.getStackTrace();

    if (!BeeUtils.isEmpty(arr)) {
      for (int i = 0; i < arr.length; i++) {
        PropertyUtils.addProperty(lst, BeeUtils.bracket(i),
            transformStackTraceElement(arr[i]));
      }
    }

    return lst;
  }

  public static List<Property> getThreadStaticInfo() {
    List<Property> lst = new ArrayList<Property>();

    PropertyUtils.addProperties(lst, "MAX_PRIORITY", Thread.MAX_PRIORITY,
        "MIN_PRIORITY", Thread.MIN_PRIORITY, "NORM_PRIORITY",
        Thread.NORM_PRIORITY, "Default Uncaught Exception Handler",
        Thread.getDefaultUncaughtExceptionHandler());

    return lst;
  }

  public static String transformStackTraceElement(StackTraceElement ste) {
    if (ste == null) {
      return null;
    } else {
      return ste.toString();
    }
  }

  public static String transformThread(Thread t) {
    if (t == null) {
      return null;
    } else {
      return BeeUtils.transformOptions("Id", t.getId(), "Name", t.getName());
    }
  }

  public static String transformThreadGroup(ThreadGroup tg) {
    if (tg == null) {
      return null;
    } else {
      return tg.getName();
    }
  }

  private static PackageComparator ensurePackageComparator() {
    if (packageComparator == null) {
      packageComparator = new PackageComparator();
    }
    return packageComparator;
  }
}