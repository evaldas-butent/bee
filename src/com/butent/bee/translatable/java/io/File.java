package java.io;

public class File implements Comparable<File> {

  private static class FileSystem {

    private static final int BA_REGULAR = 0x02;
    private static final int BA_DIRECTORY = 0x04;

    private String canonicalize(String path) {
      return path.trim();
    }

    private int compare(File file, File pathname) {
      return file.path.compareTo(pathname.path);
    }

    private int getBooleanAttributes(File file) {
      return file.path.endsWith(File.separator) ? BA_DIRECTORY : BA_REGULAR;
    }

    private String getDefaultParent() {
      return "";
    }

    private char getPathSeparator() {
      return ':';
    }

    private char getSeparator() {
      return '/';
    }

    private int hashCode(File file) {
      return file.path.hashCode();
    }

    private boolean isAbsolute(File file) {
      return file.path.startsWith(File.separator);
    }

    private String normalize(String pathname) {
      return pathname.trim();
    }

    private int prefixLength(String path) {
      if (path == null) {
        return 0;
      }
      return 0;
    }

    private String resolve(File file) {
      return file.path;
    }

    private String resolve(String path, String child) {
      if (path.isEmpty()) {
        return normalize(child);
      } else if (normalize(path).endsWith(File.separator)) {
        return normalize(path) + normalize(child);
      } else {
        return normalize(path) + getSeparator() + normalize(child);
      }
    }
  }

  private static FileSystem fs = new FileSystem();

  public static final char separatorChar = fs.getSeparator();

  public static final String separator = "" + separatorChar;

  public static final char pathSeparatorChar = fs.getPathSeparator();

  public static final String pathSeparator = "" + pathSeparatorChar;

  private String path;

  private transient int prefixLength;

  public File(File parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.path.equals("")) {
        this.path = fs.resolve(fs.getDefaultParent(),  fs.normalize(child));
      } else {
        this.path = fs.resolve(parent.path, fs.normalize(child));
      }
    } else {
      this.path = fs.normalize(child);
    }
    this.prefixLength = fs.prefixLength(this.path);
  }

  public File(String pathname) {
    if (pathname == null) {
      throw new NullPointerException();
    }
    this.path = fs.normalize(pathname);
    this.prefixLength = fs.prefixLength(this.path);
  }

  public File(String parent, String child) {
    if (child == null) {
      throw new NullPointerException();
    }
    if (parent != null) {
      if (parent.equals("")) {
        this.path = fs.resolve(fs.getDefaultParent(), fs.normalize(child));
      } else {
        this.path = fs.resolve(fs.normalize(parent), fs.normalize(child));
      }
    } else {
      this.path = fs.normalize(child);
    }
    this.prefixLength = fs.prefixLength(this.path);
  }

  private File(String pathname, int prefixLength) {
    this.path = pathname;
    this.prefixLength = prefixLength;
  }

  @Override
  public int compareTo(File pathname) {
    return fs.compare(this, pathname);
  }

  @Override
  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof File)) {
      return compareTo((File) obj) == 0;
    }
    return false;
  }

  public File getAbsoluteFile() {
    String absPath = getAbsolutePath();
    return new File(absPath, fs.prefixLength(absPath));
  }

  public String getAbsolutePath() {
    return fs.resolve(this);
  }

  public File getCanonicalFile() {
    String canonPath = getCanonicalPath();
    return new File(canonPath, fs.prefixLength(canonPath));
  }

  public String getCanonicalPath() {
    return fs.canonicalize(fs.resolve(this));
  }

  public String getName() {
    int index = path.lastIndexOf(separatorChar);
    if (index < prefixLength) {
      return path.substring(prefixLength);
    }
    return path.substring(index + 1);
  }

  public String getParent() {
    int index = path.lastIndexOf(separatorChar);
    if (index < prefixLength) {
      if ((prefixLength > 0) && (path.length() > prefixLength)) {
        return path.substring(0, prefixLength);
      }
      return null;
    }
    return path.substring(0, index);
  }

  public File getParentFile() {
    String p = this.getParent();
    if (p == null) {
      return null;
    }
    return new File(p, this.prefixLength);
  }

  public String getPath() {
    return path;
  }

  @Override
  public int hashCode() {
    return fs.hashCode(this);
  }

  public boolean isAbsolute() {
    return fs.isAbsolute(this);
  }

  public boolean isDirectory() {
    return ((fs.getBooleanAttributes(this) & FileSystem.BA_DIRECTORY) != 0);
  }

  public boolean isFile() {
    return ((fs.getBooleanAttributes(this) & FileSystem.BA_REGULAR) != 0);
  }

  @Override
  public String toString() {
    return getPath();
  }

  int getPrefixLength() {
    return prefixLength;
  }
}
