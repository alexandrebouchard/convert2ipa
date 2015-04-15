package ventris;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nuts.io.BufferedReaderIterator;
import nuts.io.IO;
import nuts.lang.ArrayUtils;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;
import fig.basic.CharEncUtils;

public class NGramCount
{
  private CounterMap<String, String> counts = new CounterMap<String, String>();
  private int order = 3;
  private static final String WORD_BOUNDARY = " ";
  private static final String SEGMENT_BOUNDARY = "";
  private static final String SENTENCE_PREFIX = "<W>";
  private boolean normalized = false;
  
  public NGramCount(int order)
  {
    this.order = order;
  }
  
  public NGramCount(BufferedReader saved) throws ParseException
  {
    for (String line : BufferedReaderIterator.iterate(saved))
    {
      String [] fields = line.split("\\s*[|][|][|]\\s*");
      if (fields.length != 3)
      {
        throw new ParseException(line, 0);
      }
      String prefix = fields[0];
      String argument = fields[1];
      double value = Double.parseDouble(fields[2]);
      counts.incrementCount(prefix, argument, value);
    }
  }
  
  public void countNGrams(List<String> segments)
  {
    assert !normalized;
    for (int i = 0; i < order; i++)
    {
      segments.add(0, SENTENCE_PREFIX);
    }
    for (int i = order - 1; i < segments.size(); i++)
    {
      StringBuilder prefix = new StringBuilder();
      prefix.append("");
      for (int j = i - order + 1; j < i; j++)
      {
        prefix.append(segments.get(j) + SEGMENT_BOUNDARY);
      }
      String argument = segments.get(i);
      counts.incrementCount(prefix.toString(), argument, 1.0);
    }
  }
  
  public void normalize()
  {
    normalized = true;
    counts.normalize();
  }
  
  public double getConditionalPr(String argument, String conditionedPrefix)
  {
    if (!normalized)
    {
      normalize();
    }
    return counts.getCount(conditionedPrefix, argument);
  }
  
  public void print(PrintWriter out)
  {
    normalize();
    List<String> keys = new ArrayList<String>();
    keys.addAll(counts.keySet());
    Collections.sort(keys);
    for (String prefix : keys)
    {
      Counter<String> currentDist = counts.getCounter(prefix);
      for (String argument : currentDist)
      {
        out.println(prefix + " ||| " + argument + " ||| " + currentDist.getCount(argument));
      }
    }
    out.flush();
  }
  
  /**
   * @param args
   * @throws IOException 
   */
  public static void main(String[] args) throws IOException
  {
    if (args.length < 1)
    {
      System.err.println("Usage: NGramCount <order>");
      return;
    }
    int order = Integer.parseInt(args[0]);
    NGramCount nGramCount = new NGramCount(order);
    for (String line : IO.si())
    {
      String [] words = line.split(WORD_BOUNDARY);
      for (String word : words)
      {
        String [] segments = new String[word.length()];
        for (int i = 0; i < word.length(); i++)
        {
          segments[i] = word.substring(i, i + 1);
        }
        nGramCount.countNGrams(ArrayUtils.stringArray2List(segments));
      }
    }
    nGramCount.normalize();
    PrintWriter stdout = CharEncUtils.getWriter(System.out);
    nGramCount.print(stdout);
    stdout.close();
  }

  public static class L2Distance implements Metric<NGramCount>
  {
    public double d(NGramCount n1, NGramCount n2)
    {
      double total = 0.0;
      Set<String> prefixes = new HashSet<String>();
      prefixes.addAll(n1.counts.keySet());
      prefixes.addAll(n2.counts.keySet());
      for (String prefix : prefixes)
      {
        Counter<String> c1 = n1.counts.getCounter(prefix);
        Counter<String> c2 = n2.counts.getCounter(prefix);
        if (c1 != null && c2 != null)
        {
          Set<String> arguments = new HashSet<String>();
          arguments.addAll(c1.keySet());
          arguments.addAll(c2.keySet());
          for (String argument : arguments)
          {
            double delta = c1.getCount(argument) - c2.getCount(argument);
            total = total + delta*delta;
          }
        }
        else if (c1 != null)
        {
          for (String argument : c1.keySet())
          {
            double delta = c1.getCount(argument) - 0;
            total = total + delta*delta;
          }
        }
        else if (c2 != null)
        {
          for (String argument : c2.keySet())
          {          
            double delta = c2.getCount(argument) - 0;
            total = total + delta*delta;     
          }
        }
      }
      return total;
    }
  }
}
