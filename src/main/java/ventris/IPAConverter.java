package ventris;

import java.io.*;
import java.util.*;

import fig.basic.*;
import fig.exec.*;
import static fig.basic.LogInfo.*;

/**
 * A text to IPA converter replacement for Text2IPA.
 * Handles a larger class of rules and has a easier syntax.
 * Example rule file:

vow := [aeoiu]  # Define vowels

á -> a          # Replace á with a
$vow/f/ -> ff   # Replace f with ff after vowels
/sc/[ei] -> ʃ   # Replace sc with ʃ before e or i
/s/_ ->         # Drop final s
a/./ -> e%      # Insert e before any character which was originally preceded by a
 */
public class IPAConverter implements Runnable {
  @fig.basic.Option(required=true) public String rulesPath;
  @fig.basic.Option public boolean dumpRules = false;
  @fig.basic.Option public String inputPath = "/dev/stdin";
  @fig.basic.Option public String outputPath = "/dev/stdout";
  @fig.basic.Option(gloss="Character that is added to beginning and end of words when rules are applied")
    public String boundaryChar = "_";
  @fig.basic.Option(gloss="Used in the replacement string to refer to the search string")
    public static String searchRefChar = "%";
  @fig.basic.Option(gloss="Matches any character")
    public static String wildcardChar = ".";

  private static Map<String,String> macros = new HashMap<String, String>();
  
	public IPAConverter(String rulesPath)
  {
    this.rulesPath = rulesPath;
    loadRules();
  }
  private IPAConverter()
  {
  }

  public static void main(String[] args) {
    Execution.startMainTrack = false;
    Execution.run(args, new IPAConverter());
  }

  // A macro has the format:
  //   name := value
  public Pair<String,String> parseMacro(String s) {
    String[] tokens = s.split("\\s*:=\\s*");
    if(tokens.length == 2)
      return new Pair(tokens[0], tokens[1]);
    return null;
  }

  public static String substituteMacro(String s) {
    if(s.length() >= 2 && s.charAt(0) == '$') {
      s = s.substring(1);
      String t = macros.get(s);
      if(t == null)
        throw Exceptions.bad("Unknown variable: '%s'", s);
      return t;
    }
    return s;
  }
  
  private List<Rule> rules;
  private void loadRules()
  {
    // Load rules (and macros)
    rules = new ArrayList<Rule>();
    for(String line : IOUtils.readProgramLinesHard(rulesPath)) {
      if(line.equals("")) continue;
      Pair<String,String> p = parseMacro(line);
      if(p != null)
        macros.put(p.getFirst(), p.getSecond());
      else
        rules.add(new Rule(line));
    }
    rules.add(new Rule("/"+boundaryChar+"/ ->")); // Remove boundary characters
    rules.add(new Rule("/"+wildcardChar+"/ -> " + searchRefChar)); // Identity rule
    if(dumpRules) {
      track("Rules", true);
      for(Rule rule : rules) logs(rule);
      end_track();
    }
  }

  public void run() {
    loadRules();
    // Convert the input
    PrintWriter out = IOUtils.openOutHard(outputPath);
    BufferedReader in = IOUtils.openInHard(inputPath);
    String line;
    try {
      while((line = in.readLine()) != null) {
        String[] tokens = line.split("\\s+");
        for(int i = 0; i < tokens.length; i++)
          tokens[i] = convert(tokens[i]);
        out.println(StrUtils.join(tokens, " "));
        out.flush();
      }
      out.close();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }


  final static String vowels = "aáàâäeéèêëiíìîïoóòôöuúùûü";
  static boolean isVowel(char c) { return vowels.indexOf(c) != -1; }
  // First vowel marks the start of a syllable for us
  boolean isFirstVowel(String word, int i) {
    if(i-1 >= 0 && isVowel(word.charAt(i-1))) return false; // Nope, if we're not the first vowel
    return isVowel(word.charAt(i));
  }

  // A syllable is defined as a contiguous group of vowels
  int findNumSyllables(String word) {
    int n = 0;
    for(int i = 0; i < word.length(); i++)
      if(isFirstVowel(word, i)) n++;
    return n;
  }

  public String convert(String word) {
    word = boundaryChar + word.toLowerCase() + boundaryChar;
    int numSyllabes = findNumSyllables(word);

    // Walk along the word;
    // For each position, try applying any of the rules
    StringBuilder buf = new StringBuilder();
    int syllable = 0;
    for(int i = 0; i < word.length();) {
      Rule bestRule = null;
      int bestNumMatched = 0;
      for(Rule rule : rules) {
        if(!rule.syllableMatches(numSyllabes, syllable)) continue;
        if(!rule.globalPropertiesMatches(word)) continue;
        int numMatched = rule.matches(word, i);
        if(numMatched > bestNumMatched) {
          bestRule = rule;
          bestNumMatched = numMatched;
        }
      }

      if(bestNumMatched == 0)
        throw Exceptions.bad("Couldn't find rule to apply at '%s' (shouldn't happen)", buf.substring(i));
      buf.append(bestRule.replace.replace(word.substring(i, i+bestNumMatched)));
      for(int n = 0; n < bestNumMatched; n++) {
        if(isFirstVowel(word, i)) syllable++;
        i++;
      }
    }
    return buf.toString();
  }
}

////////////////////////////////////////////////////////////

class Matcher {
  // If there are no choices, that matches everything
  public String[] choices; // Match one of the following
  public boolean negate;

  public Matcher(String s) {
    s = IPAConverter.substituteMacro(s.trim());
    // Formats for s:
    //   ^<char> 
    //   <char> 
    //   [<char><char><char>]
    //   {<str>,<str>,<str>}
    if(s.length() > 0 && s.charAt(0) == '^') { // Negate
      negate = true;
      s = s.substring(1);
    }

    if(s.length() == 0)
      choices = new String[0];
    else if(s.length() > 2 && s.charAt(0) == '[' && s.charAt(s.length()-1) == ']') {
      choices = s.substring(1, s.length()-1).split("");
      choices = ListUtils.subArray(choices, 1, choices.length); // Stupid Java makes first element ""
    }
    else if(s.length() > 2 && s.charAt(0) == '{' && s.charAt(s.length()-1) == '}')
      choices = s.substring(1, s.length()-1).split(",");
    else
      choices = new String[] { s };
  }

  public boolean matchesStart(String str) {
    boolean match = false;
    if(choices.length == 0) match = true;
    for(String choice : choices) {
      if(str.startsWith(choice)) match = true;
      if(choice.equals(IPAConverter.wildcardChar) && str.length() >= 1) match = true;
      if(match) break;
    }
    return match ^ negate;
  }
  public boolean matchesEnd(String str) {
    boolean match = false;
    if(choices.length == 0) match = true;
    for(String choice : choices) {
      if(str.endsWith(choice)) match = true;
      if(choice.equals(IPAConverter.wildcardChar) && str.length() >= 1) match = true;
      if(match) break;
    }
    return match ^ negate;
  }

  public String toString() { return (negate?"^":"")+"{"+StrUtils.join(choices, ",")+"}"; }
}

////////////////////////////////////////////////////////////

class Replacer {
  // Replace $ with current word
  private String replacement;
  public Replacer(String replacement) {
    this.replacement = IPAConverter.substituteMacro(replacement);
  }
  public String replace(String orig) {
    return replacement.replaceAll(IPAConverter.searchRefChar, orig);
  }
  public String toString() { return replacement; }
}

////////////////////////////////////////////////////////////

class Rule {
  final int UNDEF = Integer.MAX_VALUE;
  // search -> replace / left _ right
  public Matcher search;
  public Replacer replace;
  public Matcher left, right;
  public int matchSyllable = UNDEF; // Apply this rule only on this syllable
  public int endsWithVowel = UNDEF;
  public boolean matchSyllableNegate = false;

  public Rule(Matcher search, Replacer replace, Matcher left, Matcher right) {
    this.search = search;
    this.replace = replace;
    this.left = left;
    this.right = right;
  }

  // Format: <left matcher>/<search>/<right matcher> -> <replace> [when syllable=3[,endsWithVowel=true|false]]
  public Rule(String s) {
    String[] tokens;

    tokens = s.split("\\s+when\\s+");
    if(tokens.length == 2) {
      for(String entry : tokens[1].split("\\s+")) {
        entry = IPAConverter.substituteMacro(entry);
        for (String clause : entry.split(","))
        {
          String[] keyValue = clause.split("=");
          if(keyValue[0].equals("syllable")) {
            this.matchSyllable = Integer.parseInt(keyValue[1]);
            this.matchSyllableNegate = false;
          }
          else if(keyValue[0].equals("syllable!")) {
            this.matchSyllable = Integer.parseInt(keyValue[1]);
            this.matchSyllableNegate = true;
          }
          else if(keyValue[0].equals("endsWithVowel")) {
            if (keyValue[1].equals("true")) this.endsWithVowel = 1;
            else if (keyValue[1].equals("false")) this.endsWithVowel = 0;
            else throw new RuntimeException("Invalid value for endsInVowels");
          }
          else throw new RuntimeException("Invalid ``when'' condition:" + entry);
        }
      }
    }

    tokens = tokens[0].split("\\s*->\\s*");
    if(tokens.length == 1) tokens = new String[] { tokens[0], "" };
    if(tokens.length != 2) throw Exceptions.bad("Rule should have form ... -> ..., but got '%s' instead", s);
    this.replace = new Replacer(tokens[1]);

    tokens = (" "+tokens[0]+" ").split("/");
    if(tokens.length == 1) tokens = new String[] { "", tokens[0], "" };
    if(tokens.length != 3) throw Exceptions.bad("Left-hand side of rule should be <left>/<search>/<right>, but got '%s' instead", s);
    this.left = new Matcher(tokens[0]);
    this.search = new Matcher(tokens[1]);
    this.right = new Matcher(tokens[2]);
  }

  public boolean syllableMatches(int numSyllabes, int syllable) {
    // Can't use this unless we match the syllable
    if(matchSyllable == UNDEF) return true;
    if(numSyllabes == 0) return false;
    int tmpMatchSyllable = matchSyllable;
    if(tmpMatchSyllable < 0) tmpMatchSyllable += numSyllabes;
    return (tmpMatchSyllable == syllable) ^ matchSyllableNegate;
  }
  
  public boolean globalPropertiesMatches(String word)
  {
    if (endsWithVowel == UNDEF) return true;
    // need to look at index (word.length() - 2) because boundary was appended
    // NOTE: asserts boundarysymbol is 1 char long
    return !((endsWithVowel == 1) ^ 
          (IPAConverter.isVowel(word.charAt(word.length() - 2))));  
  }

  // Return the number of characters matched
  public int matches(String word, int i) {
    if(!left.matchesEnd(word.substring(0, i))) return 0; // Check left context matches
    for(String choice : search.choices) { // For each choice of replacement string
      // Specialized hack since we don't support full-blown regular expressions
      if(choice.equals(IPAConverter.wildcardChar) && word.length()-i >= 1)
        return choice.length();
      // Check choice matches and right context matches
      if(word.substring(i).startsWith(choice) && right.matchesStart(word.substring(i+choice.length())))
        return choice.length();
    }
    return 0;
  }

  public String toString() {
    return String.format("%s / %s / %s -> %s%s", left, search, right, replace,
      this.matchSyllable == UNDEF ? "" :
      String.format(" when syllable%s%d", this.matchSyllableNegate?"!=":"=", this.matchSyllable));
  }
}
