package ventris;

import java.io.IOException;

import nuts.io.IO;
import edu.berkeley.nlp.util.Counter;

public class TrimRareWords
{
  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length < 1)
    {
      System.err.println("Usage: TrimRareWords <freq>");
      return;
    }
    int freq = Integer.parseInt(args[0]);
    Counter<String> counter = new Counter<String>();
    for (String line : IO.si())
    {
      String [] words = line.split("\\s+");
      for (String word : words)
      {
        counter.incrementCount(word, 1.0);
      }
    }
    for (String key : counter.keySet())
    {
      if (counter.getCount(key) > freq)
      {
        for (int i = 0; i < counter.getCount(key); i++)
        {
          IO.so(key);
        }
      }
    }
  }
}