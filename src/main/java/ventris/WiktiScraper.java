package ventris;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nuts.io.IO;
import fig.basic.IOUtils;

public class WiktiScraper
{
  private static Pattern translationPattern
  = Pattern.compile(".*[*](\\w+)\\s*[:]\\s*\\[\\[(\\w+)\\]\\].*");
  private List<Map<String, String>> translations 
    = new ArrayList<Map<String,String>>();
  public void process(String path) throws IOException
  {
    Map<String, String> currentWordBlock = new HashMap<String, String>();
    for (String line : IO.i(path))
    {
      if (line.matches(".*[=][=]Translations[=][=].*"))
      {
        if (currentWordBlock.size() > 0)
        {
          translations.add(currentWordBlock);
        }
        // a new word block was found
        currentWordBlock = new HashMap<String, String>();
        continue;
      }
      if (line.matches(".*[=][=][=].*[=][=][=].*"))
      {
        if (currentWordBlock.size() > 0)
        {
          translations.add(currentWordBlock);
        }       
        // not added to the big list intentionally, forget these
        currentWordBlock = new HashMap<String, String>();
      }
      if (line.matches(".*[*]\\w+\\s*[:]\\s*\\[\\[\\w+\\]\\].*"))
      {
        // a translation was found
        Matcher m = translationPattern.matcher(line);
        m.find();
        String langName = m.group(1);
        String translation = m.group(2);
        currentWordBlock.put(langName, translation);
      }
    }
  } 


  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length == 0)
    {
      System.err.println("First arg is the path to the xml wiktionary file." +
          "The next arguments are language names to extract.");
      return;
    }
    String wikiPath = args[0];
    List<String> langs = new ArrayList<String>();
    for (int i = 1; i < args.length; i++)
    {
      langs.add(args[i]);
    }
    PrintWriter out = IOUtils.openOut(wikiPath + "." + langs);
    WiktiScraper scraper = new WiktiScraper();
    scraper.process(wikiPath);
    for (Map<String, String> wordBlock : scraper.translations)
    {
      int numLangs = 0;
      for (String lang : langs)
      {
        if (wordBlock.keySet().contains(lang))
        {
          numLangs++;
        }
      }
      if (numLangs < 2)
      {
        continue;
      }
      for (int i = 0; i < langs.size(); i++)
      {
        String word = wordBlock.get(langs.get(i));
        if (word != null)
        {
          out.append(word);
        }
        else
        {
          out.append("?");
        }
        if (i != langs.size() - 1)
        {
          out.append("\t");
        }
      }
      out.append("\n");
    }
    out.close();
  }

}
