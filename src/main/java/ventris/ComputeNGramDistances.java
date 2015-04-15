package ventris;

import java.text.ParseException;
import java.util.*;
import java.io.*;

import fig.basic.IOUtils;

public class ComputeNGramDistances
{

  /**
   * @param args
   * @throws IOException 
   * @throws ParseException 
   */
  public static void main(String[] args) throws ParseException, IOException
  {
    if (args.length < 1)
    {
      System.err.println("Usage: ComputeNGramDistances <ngram>+");
      return;
    }
    // TODO : command line arguments to pick a metric
    Metric<NGramCount> metric = new NGramCount.L2Distance(); 
    List<NGramCount> distributions = new ArrayList<NGramCount>();
    for (int i = 0; i < args.length; i++)
    {
      NGramCount currentDistribution = new NGramCount(IOUtils.openIn(args[i]));
      distributions.add(currentDistribution);
    }
    double [][] distances = new double[distributions.size()][distributions.size()];
    for (int i = 0; i < distributions.size(); i++)
    {
      for (int j = 0; j <= i; j++)
      {
        double currentD = metric.d(distributions.get(i), distributions.get(j));
        distances[i][j] = currentD;
        distances[j][i] = currentD;
      }
    }
    System.out.print("\t");
    for (int i = 0; i < args.length; i++)
    {
      System.out.print(args[i] + "\t");
    }
    System.out.println("");
    for (int i = 0; i < args.length; i++)
    {
      System.out.print(args[i] + "\t");
      for (int j = 0; j < args.length; j++)
      {
        System.out.print(distances[i][j] + "\t");
      }
      System.out.println("");
    }
  }

}
