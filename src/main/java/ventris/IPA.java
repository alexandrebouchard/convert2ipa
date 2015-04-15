package ventris;

import java.text.ParseException;
import java.util.*;
import java.io.*;

import nuts.io.IO;

/**
 * 
 * Gives access to IPA characters in UTF and natural classes.
 *
 */
public class IPA
{
  // Hack: you will need to change those to get the stuff to work
  public static final String IPA_CONSONANTS_FILE = "/home/eecs/bouchard/data/ipa_consonants";
  public static final String IPA_VOWELS_FILE = "/home/eecs/bouchard/data/ipa_consonants";
  
  private static List<Character> consonants = null;
  /**
   * All the consonants in the IPA alphabet
   * 
   * We might want to change that at some point to add the language as an argument
   * and return only the IPA character for that language.
   * @return
   * @throws IOException
   * @throws ParseException
   */
  public static List<Character> getConsonants() throws IOException, ParseException
  {
    if (consonants != null)
    {
      return consonants;
    }
    consonants = new ArrayList<Character>();
    for (String line : IO.i(IPA_CONSONANTS_FILE)) // iterate the lines of the file
    {
      String [] fields = line.split("\\s+");
      if (fields.length != 4)
      {
        throw new ParseException(line, 0);
      }
      consonants.add(fields[3].charAt(0));
    }
    return consonants;
  }
}
