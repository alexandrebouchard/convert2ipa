package ventris;

import java.util.*;
import java.io.*;

import fig.basic.IOUtils;

public class SwadeshScraper
{
  public static void main(String [] args) throws IOException
  {
    // read and linearized
    BufferedReader in = IOUtils.openIn(args[0]);
    StringBuffer string = new StringBuffer();
    String line = null;
    while ((line = in.readLine()) != null)
    {
      string.append(line);
    }
    // split into rows
    String [] rows = string.toString().split("[<]TR[>]");
    int i = 0;
    for (String row : rows)
    {
      int j = 0;
      // split into columns
      if (i > 1) 
      {
        for (String column : row.split("<TD>"))
        {
          if (j == 5 || j == 6)
            System.out.print(column.replaceAll("[<][^>]*[>]", "").split(",")[0] + "\t");
          j++;
        }
        System.out.print("\n");
      }
      i++;
    }
  }
}
