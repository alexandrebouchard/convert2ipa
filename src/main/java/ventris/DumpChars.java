package ventris;

import java.util.*;
import java.io.*;

import edu.berkeley.nlp.util.Counter;

import nuts.io.IO;

public class DumpChars
{
  public static void main(String [] args) throws IOException
  {
    Counter<Character> counter = new Counter<Character>();
    for (String line : IO.si())
    {
      for (char currentChar : line.toCharArray())
      {
        counter.incrementCount(currentChar, 1.0);
      }
    }
    for (char item : counter)
    {
      IO.so("" + item + " : " + counter.getCount(item));
    }
  }
}
