import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class Formatter {
  
  public static void main(String... args) {
    new Formatter().build("d:\\bee\\eclipse\\gwt.prefs", "d:\\bee\\eclipse\\builtin.xml", "d:\\bee\\eclipse\\beejs.xml");
  }
  
  private void build(String prefsIn, String formatterIn, String formatterOut) {
    if (!new File(prefsIn).exists()) {
      System.err.println(prefsIn);
      return;
    }
    if (!new File(formatterIn).exists()) {
      System.err.println(formatterIn);
      return;
    }
    
    Map<String, String> prefs = readPrefs(prefsIn);
    if (prefs == null || prefs.size() <= 0) {
      System.err.println("no prefs");
      return;
    }
    
    BufferedReader reader = null;
    PrintWriter writer = null;
    
    try {
      reader = new BufferedReader(new FileReader(formatterIn));
      writer = new PrintWriter(new FileWriter(formatterOut));
      
      int cntUpd = 0;
      int cntIn = 0;
      int cntOut = 0;
      int cntNotPref = 0;
      int cntNotFound = 0;
      int cntSame = 0;

      int p1;
      int p2;
      
      String line;
      while ((line = reader.readLine()) != null) {
        cntIn++;

        String key = getAttribute(line, "id");
        String value = getAttribute(line, "value");
        if (key == null || value == null) {
          System.out.println("not pref: " + line);
          System.out.println();
          writer.println(line);
          cntOut++;
          cntNotPref++;
          continue;
        }
        
        String pv = prefs.get(key);
        if (pv == null) {
          System.out.println();
          System.out.println("not found: " + key);
          System.out.println();
          writer.println(line);
          cntOut++;
          cntNotFound++;
          continue;
        }
        
        if (value.equals(pv)) {
          writer.println(line);
          cntOut++;
          cntSame++;
          continue;
        }
        
        System.out.println(key + " " + value + " " + pv);
        
        p2 = line.lastIndexOf('"');
        p1 = line.lastIndexOf('"', p2 - 1);
        
        writer.println(line.substring(0, p1 + 1) + pv + line.substring(p2));
        cntOut++;
        cntUpd++;
      }
      
      System.out.println("prefs " + prefs.size());
      System.out.println("formatter in " + cntIn);
      System.out.println("not a pref " + cntNotPref);
      System.out.println("pref not found " + cntNotFound);
      System.out.println("value same " + cntSame);
      System.out.println("updated " + cntUpd);
      System.out.println("total " + (cntNotPref + cntNotFound + cntSame + cntUpd));
      System.out.println("formatter out " + cntOut);

    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
      if (writer != null) {
        writer.close();
      }
    }
  }

  private String getAttribute(String line, String name) {
    int p0 = line.indexOf(" " + name.trim() + "=");
    if (p0 <= 0) {
      return null;
    }
    int p1 = line.indexOf('"', p0);
    if (p1 <= 0) {
      return null;
    }
    int p2 = line.indexOf('"', p1 + 1);
    if (p2 <= 0) {
      return null;
    }
    return line.substring(p1 + 1, p2);
  }
  
  private Map<String, String> readPrefs(String fileName) {
    Map<String, String> prefs = new HashMap<String, String>();

    BufferedReader reader = null;
    
    try {
      reader = new BufferedReader(new FileReader(fileName));
      String line;
      int p;
      while ((line = reader.readLine()) != null) {
        p = line.indexOf('=');
        if (p <= 0) {
          continue;
        }
        
        String key = line.substring(0, p);
        String value = line.substring(p + 1);
        if ("eclipse.preferences.version".equals(key)) {
          continue;
        }
        
        prefs.put(key, value);
      }
    
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }
    return prefs;
  }
}
