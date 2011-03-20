package com.butent.bee.server.io;

import com.google.common.collect.Lists;

import com.butent.bee.server.Config;
import com.butent.bee.shared.Assert;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.utils.BeeUtils;
import com.butent.bee.shared.utils.ExtendedProperty;
import com.butent.bee.shared.utils.LogUtils;
import com.butent.bee.shared.utils.Property;
import com.butent.bee.shared.utils.PropertyUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.logging.Logger;

public class FileUtils {
  public static final String EXT_CLASS = "class";
  public static final String EXT_JAVA = "java";
  public static final String EXT_PROPERTIES = "properties";

  public static final Filter DIRECTORY_FILTER = new DirectoryFilter();
  public static final Filter FILE_FILTER = new NormalFileFilter();
  public static final Filter INPUT_FILTER = new InputFileFilter();

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private static int defaultBufferSize = 4096;
  private static Charset defaultCharset = UTF_8;
  
  private static Logger logger = Logger.getLogger(FileUtils.class.getName());
  
  public static void closeQuietly(Reader rdr) {
    if (rdr == null) {
      return;
    }

    try {
      rdr.close();
    } catch (IOException ex) {
      LogUtils.warning(logger, ex);
    }
  }

  public static void closeQuietly(Writer wrt) {
    if (wrt == null) {
      return;
    }

    try {
      wrt.close();
    } catch (IOException ex) {
      LogUtils.severe(logger, ex);
    }
  }

  public static String fileToString(File fl) {
    return fileToString(fl, defaultCharset);
  }

  public static String fileToString(File fl, Charset cs) {
    Assert.notNull(fl);
    if (!fl.exists()) {
      LogUtils.warning(logger, fl.getAbsolutePath(), "file not found");
      return null;
    }

    InputStreamReader fr = null;
    StringBuilder sb = new StringBuilder();

    int size = defaultBufferSize;
    char[] arr = new char[size];
    int len;

    try {
      fr = new InputStreamReader(new FileInputStream(fl), cs);
      do {
        len = fr.read(arr, 0, size);
        if (len > 0) {
          sb.append(arr, 0, len);
        }
      } while (len > 0);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fl.getAbsolutePath());
    }

    closeQuietly(fr);
    return sb.toString();
  }

  public static String fileToString(String fileName) {
    return fileToString(fileName, defaultCharset);
  }

  public static String fileToString(String fileName, Charset cs) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());
    return fileToString(fl, cs);
  }

  public static List<File> findFiles(Collection<File> directories, Collection<Filter> filters,
      boolean recurse, boolean all) {
    Assert.notEmpty(directories);
    List<File> files = Lists.newArrayList();
    
    for (File dir : directories) {
      files.addAll(findFiles(dir, filters, recurse));
      if (!all && !files.isEmpty()) {
        break;
      }
    }
    return files;
  }

  public static List<File> findFiles(File dir, Collection<Filter> filters) {
    return findFiles(dir, filters, true);
  }
  
  public static List<File> findFiles(File dir, Collection<Filter> filters, boolean recurse) {
    Assert.notNull(dir);
    
    List<File> found = Lists.newArrayList();
    if (!dir.isDirectory() || !Config.isVisible(dir)) {
      return found;
    }
    
    boolean ok;
    for (File entry : dir.listFiles()) {
      if (!Config.isVisible(entry)) {
        continue;
      }

      ok = true;
      if (filters != null) {
        for (Filter filter : filters) {
          if (!filter.accept(entry) || !filter.accept(dir, entry.getName())) {
            ok = false;
            break;
          }
        }
      }
      if (ok) {
        found.add(entry);
      }

      if (recurse && entry.isDirectory()) {
        found.addAll(findFiles(entry, filters, recurse));
      }
    }
    return found;
  }
  
  public static List<File> findFiles(File dir, Filter... filters) {
    return findFiles(dir, Lists.newArrayList(filters));
  }

  public static String getCanonicalPath(File fl) {
    Assert.notNull(fl);
    String path;

    try {
      path = fl.getCanonicalPath();
    } catch (IOException ex) {
      LogUtils.error(logger, ex);
      path = BeeConst.STRING_EMPTY;
    }
    return path;
  }

  public static List<ExtendedProperty> getCharsets() {
    List<ExtendedProperty> lst = new ArrayList<ExtendedProperty>();
    PropertyUtils.addExtended(lst, "Default Charset", Charset.defaultCharset());

    SortedMap<String, Charset> charsets = Charset.availableCharsets();
    PropertyUtils.addExtended(lst, "Available Charsets", "Cnt", charsets.size());

    int i = 0;
    for (String key : charsets.keySet()) {
      i++;
      Charset cs = charsets.get(key);

      PropertyUtils.addChildren(lst,
          BeeUtils.concat(1, BeeUtils.progress(i, charsets.size()), key),
          "Name", cs.name(),
          "Aliases", BeeUtils.transformCollection(cs.aliases()),
          "Can Encode", cs.canEncode(),
          "Display Name", cs.displayName(),
          "Registered", cs.isRegistered());
    }
    return lst;
  }

  public static List<Property> getFileInfo(File fl) {
    Assert.notNull(fl);

    List<Property> lst = new ArrayList<Property>();
    if (!fl.exists()) {
      PropertyUtils.addProperty(lst, "Exists", false);
      return lst;
    }

    PropertyUtils.addProperties(lst,
        "Can Execute", fl.canExecute(),
        "Can Read", fl.canRead(),
        "Can Write", fl.canWrite(),
        "Absolute Path", fl.getAbsolutePath(),
        "Canonical Path", getCanonicalPath(fl),
        "Free Space", fl.getFreeSpace(),
        "Name", fl.getName(),
        "Parent", fl.getParent(),
        "Path", fl.getPath(),
        "Total Space", fl.getTotalSpace(),
        "Usable Space", fl.getUsableSpace(),
        "Absolute", fl.isAbsolute(),
        "Directory", fl.isDirectory(),
        "File", fl.isFile(),
        "Hidden", fl.isHidden(),
        "Last Modified", new DateTime(fl.lastModified()),
        "Length", fl.length(),
        "URI", fl.toURI());

    return lst;
  }

  public static FileReader getFileReader(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName.trim());

    if (!fl.exists()) {
      LogUtils.warning(logger, fileName, "file not found");
      return null;
    }

    FileReader fr = null;

    try {
      fr = new FileReader(fl);
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fileName);
    }
    return fr;
  }

  public static String[] getFiles(File dir) {
    return getFiles(dir, null);
  }

  public static String[] getFiles(File dir, FilenameFilter filter) {
    Assert.notNull(dir);
    return (filter == null) ? dir.list() : dir.list(filter);
  }

  public static List<Property> getRootsInfo() {
    List<Property> lst = new ArrayList<Property>();

    File[] roots = File.listRoots();
    int n = BeeUtils.length(roots);
    if (n <= 0) {
      PropertyUtils.addProperty(lst, "Roots", BeeUtils.bracket(n));
      return lst;
    }

    for (int i = 0; i < n; i++) {
      PropertyUtils.addProperty(lst, "Root", BeeUtils.progress(i + 1, n));
      lst.addAll(getFileInfo(roots[i]));
    }
    return lst;
  }

  public static boolean isFile(String name) {
    Assert.notEmpty(name);
    return new File(name).exists();
  }

  public static boolean isInputFile(File fl) {
    if (fl == null) {
      return false;
    } else {
      return fl.exists() && fl.isFile() && fl.length() > 0 && fl.canRead();
    }
  }

  public static boolean isInputFile(File parent, String child) {
    Assert.notNull(parent);
    Assert.notEmpty(child);
    File fl = new File(parent, child);

    return isInputFile(fl);
  }

  public static boolean isInputFile(String fileName) {
    Assert.notEmpty(fileName);
    File fl = new File(fileName);

    return isInputFile(fl);
  }

  public static int loadProperties(Properties prp, File fl) {
    return loadProperties(prp, fl, UTF_8);
  }
  
  public static int loadProperties(Properties prp, File fl, Charset cs) {
    Assert.notNull(prp);
    Assert.notNull(fl);
    Assert.notNull(cs);
    if (!fl.exists()) {
      LogUtils.warning(logger, fl.getAbsolutePath(), "file not found");
      return -1;
    }

    int size = prp.size();
    int cnt = 0;
    InputStreamReader fr = null;

    try {
      fr = new InputStreamReader(new FileInputStream(fl), cs);
      prp.load(fr);
      cnt = prp.size() - size;
      LogUtils.info(logger, cnt, "properties loaded from", fl.getAbsolutePath());
    } catch (IOException ex) {
      LogUtils.error(logger, ex, fl.getAbsolutePath());
      cnt = -1;
    }

    closeQuietly(fr);
    return cnt;
  }

  public static Charset normalizeCharset(String name) {
    if (BeeUtils.isEmpty(name)) {
      return defaultCharset;
    }
    Charset cs = null;

    try {
      cs = Charset.forName(name);
    } catch (Exception ex) {
      LogUtils.warning(logger, ex, name);
      cs = null;
    }
    return BeeUtils.nvl(cs, defaultCharset);
  }

  public static Properties readProperties(File fl) {
    Properties prp = new Properties();
    loadProperties(prp, fl);
    return prp;
  }

  public static boolean saveToFile(CharSequence src, String dst) {
    return saveToFile(src, dst, defaultCharset);
  }

  public static boolean saveToFile(CharSequence src, String dst, Charset cs) {
    Assert.notEmpty(src);
    Assert.notEmpty(dst);

    OutputStreamWriter fw = null;
    boolean ok;

    try {
      fw = new OutputStreamWriter(new FileOutputStream(dst), cs);
      fw.append(src);
      ok = true;
    } catch (IOException ex) {
      LogUtils.error(logger, ex, dst);
      ok = false;
    }

    closeQuietly(fw);
    return ok;
  }
  
  public static File toFile(Class<?> clazz) {
    Assert.notNull(clazz);
    return toFile(clazz.getResource(NameUtils.addExtension(clazz.getSimpleName(), EXT_CLASS)));
  }

  public static File toFile(URL url) {
    Assert.notNull(url);
    File file;
    try {
      file = new File(url.toURI());
    } catch (URISyntaxException ex) {
      file = new File(url.getPath());
    }
    return file;
  }
  
  private FileUtils() {
  }
}
