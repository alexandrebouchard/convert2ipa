package ventris;

import java.util.*;
import java.io.*;

import nuts.io.IO;

import fig.basic.IOUtils;
import fig.basic.Option;
import fig.basic.OptionsParser;
import fig.basic.Pair;
import fig.exec.Execution;

/**
 */
public class CognateDetector
{
  private double substCost = 1.0;
  private double addCost = 1.0;
  private double delCost = 1.0;
  
  public CognateDetector()
  {
  }
  
  public CognateDetector(double substCost, double addCost, double delCost)
  {
    this.substCost = substCost;
    this.addCost = addCost;
    this.delCost = delCost;
  }
  
  private Map<Pair<String, String>, Double> cache 
    = new HashMap<Pair<String,String>, Double>();
  /**
   * Warning: do not call concurrently
   * @param s1
   * @param s2
   * @return
   */
  public double cost(String s1, String s2)
  {
    if (s1.length() == 0 && s2.length() == 0)
    {
      return 0.0;
    }
    
    Pair<String, String> key = new Pair<String, String>(s1, s2);
    if (cache.containsKey(key))
    {
      return cache.get(key);
    }
    double result = Double.POSITIVE_INFINITY;
    
    // substitutions
    if (s1.length() > 0 && s2.length() > 0)
    {
      double current = cost(s1.substring(1, s1.length()),
          s2.substring(1, s2.length()));
      if (s1.charAt(0) != s2.charAt(0))
      {
        // the first char is not the same, so substitution must be paid
        current = current + substCost;
      }
      if (current < result)
      {
        result = current;
      }
    }
    
    // addition
    if (s2.length() > 0)
    {
      double current = cost(s1,
          s2.substring(1, s2.length())) + addCost;
      if (current < result)
      {
        result = current;
      }
    }
    
    // deletion
    if (s1.length() > 0)
    {
      double current = cost(s1.substring(1, s1.length()),
          s2) + delCost;
      if (current < result)
      {
        result = current;
      }
    }
    
    cache.put(key, result);
    return result;
  }
  
  @Option(gloss="File")
  public static String tabSeparatedFile = null;
  @Option(gloss="First file")
  public static String file1;
  @Option(gloss="Second file")
  public static String file2;
  @Option(gloss="Cut-off threshold")
  public static double threshold = 0.35;

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    Execution.init(args, "cognateDetector", CognateDetector.class);
    
    if (tabSeparatedFile != null)
    {
      newMain();
    }
    else
    {
      oldMain();
    }
    

    Execution.finish();
  }
  
  public static void oldMain() throws IOException
  {
    PrintWriter pw1 = IOUtils.openOut(file1 + ".filtered"),
    pw2 =  IOUtils.openOut(file2 + ".filtered");
    CognateDetector cd = new CognateDetector();
    int total = 0;
    int kept = 0;
    for (List<String> lines : IO.i(file1, file2))
    {
      String s1 = lines.get(0);
      String s2 = lines.get(1);
      double meanLength = 0.5 * (s1.length() + s2.length());
      double cost = cd.cost(s1, s2);
      if (cost / meanLength < threshold)
      {
        pw1.append(s1 + "\n");
        pw2.append(s2 + "\n");
        kept++;
      }
      total++;
    }
    pw1.close();
    pw2.close();
    System.out.println("" + (double) kept/(double) total + " % kept.");
  }
  
  private static CognateDetector cd = new CognateDetector();
  public static double averageEditD(String s1, String s2)
  {
    if (s1.length() == 0 && s2.length() == 0) return 0.0;
    double meanLength = 0.5 * (s1.length() + s2.length());
    double cost = cd.cost(s1, s2);
    return cost / meanLength;
  }
    
  public static void newMain() throws IOException
  {
    PrintWriter out = IOUtils.openOut(tabSeparatedFile + ".cognated");
    CognateDetector cd = new CognateDetector();
    int total = 0;
    int kept = 0;
    mainLoop:
    for (String line : IO.i(tabSeparatedFile))
    {
      total++;
      String [] fields = line.split("\t");
      int numberOfUnk = 0;
      for (int i = 0; i < fields.length; i++)
      {
        if (fields[i].equals("?"))
        {
          numberOfUnk++;
        }
        for (int j = 0; j < fields.length; j++)
        {
          String s1 = fields[i];
          String s2 = fields[j];
          if (!s1.equals("?") && !s2.equals("?") && (i != j))
          {
            if (averageEditD(s1,s2) > threshold)
            { 
              continue mainLoop;
            }
          }
        }
      }
      if (numberOfUnk < fields.length - 1)
      {
        out.append(line + "\n");
        kept++;
      }
    }
    out.close();
    System.out.println("" + (double) kept/(double) total + " % kept.");
  }

}
