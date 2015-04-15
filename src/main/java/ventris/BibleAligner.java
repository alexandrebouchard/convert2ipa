package ventris;

import java.util.*;
import java.io.*;

import nuts.io.IO;
import nuts.lang.StringUtils;

import fig.basic.IOUtils;
import fig.basic.Option;
import fig.basic.OptionsParser;
import fig.exec.Execution;

public class BibleAligner
{
  @Option(gloss="Files", required=true)
  public ArrayList<String> files;
  
  public void align() throws IOException
  {
    Map<String, String> map1 = parseBible(files.get(0));
    Map<String, String> map2 = parseBible(files.get(1));
    PrintWriter out1 = IOUtils.openOut(files.get(0) + ".biblealigned");
    PrintWriter out2 = IOUtils.openOut(files.get(1) + ".biblealigned");
    
    // intersection
    for (String key : map1.keySet())
    {
      if (map2.containsKey(key))
      {
        String line1 = map1.get(key);
        String line2 = map2.get(key);
        out1.println(line1);
        out2.println(line2);
      }
    }
    
    out1.close();
    out2.close();
  }
  
  public Map<String, String> parseBible(String file) throws IOException
  {
    Map<String, String> result = new HashMap<String, String>();
    for (String line : IO.i(file))
    {
      if (line.matches("[<]seg id[=][\"][^\"]*[\"][^>]*[>].*"))
      {
        String id = StringUtils.selectRegex("[<]seg id[=][\"]([^\"]*)[\"][^>]*[>].*", line).get(0);
        String contents = StringUtils.selectRegex("[<]seg id[=][\"][^\"]*[\"][^>]*[>](.*)", line).get(0);
        // remove punctuation too
        contents = contents.replaceAll("\\p{Punct}", "");
        result.put(id, contents);
      }
    }
    return result;
  }

  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    BibleAligner aligner = new BibleAligner();
    OptionsParser.register("biblealigner", aligner);
    Execution.init(args);
    
    aligner.align();
    
    Execution.finish();
  }

}
