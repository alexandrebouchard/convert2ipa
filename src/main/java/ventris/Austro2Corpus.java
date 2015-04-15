package ventris;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import nuts.io.IO;
import nuts.io.IteratorWrapper;
import nuts.lang.StringUtils;
import nuts.math.Sampling;
import nuts.tui.FancyTreeRenderer;
import nuts.tui.FancyTreeRenderer.Populator;
import nuts.util.Arbre;
import nuts.util.CollUtils;
import nuts.util.EasyFormat;
import edu.berkeley.nlp.util.Counter;
import edu.berkeley.nlp.util.CounterMap;
import fig.basic.IOUtils;
import fig.basic.Option;
import fig.basic.Pair;
import fig.basic.StrUtils;
import fig.exec.Execution;

public class Austro2Corpus implements Runnable
{
  @Option(required=true) public String dataDir;
  @Option public String langs = null;
  @Option(required=true) public String outputPrefix;
//  @Option public String ipaFile;
  @Option public boolean cleanCognates = true;
  @Option public boolean pickClosestModern = true;
  @Option public String langToReconstruct = "ProtoOceanicBlust";
  @Option public String oceanicRecon = "ProtoOceanicBlust";
  @Option public String centralMalayoPolyRecon = "ProtoCentralEasternMalayoPolynesian";
  @Option public int nInside = 0, nOutside = 0;
  @Option public int minDegree = 2;
  @Option public int nBootstrapSamples = 0;
  @Option public int clipL = Integer.MAX_VALUE; // set back to 10 if PHYLIP is needed
//  @Option public String rootName = "Austronesian";
  @Option public String pmjData = "";
  @Option public String sampa2ipaRulesFile = "";
  
  @Option public Random bootstrapRandom = new Random(1);
  
  public static String unigramStatistics(List <Entry> entries)
  {
    Map<String,Set<Character>> unigramPerLang = new HashMap <String, Set <Character>>();
    for (Entry entry : entries)
    {
      Set<Character> current = unigramPerLang.get(entry.language);
      if (current == null)
      {
        current = new HashSet<Character>();
        unigramPerLang.put(entry.language, current);
      }
      for (char c : entry.ipa.toCharArray())
        current.add(c);
    }
    StringBuilder result = new StringBuilder();
    for (String s : unigramPerLang.keySet())
      result.append(s + ": " + unigramPerLang.get(s).size() + " " + unigramPerLang.get(s) + "\n");
    return result.toString();
  }
  
  private final String [] invArgs;
  public Austro2Corpus(String [] args) { this.invArgs = args; }
  private PrintWriter out;
  public void print(String s)
  {
    IO.so(s);
    out.append(s + "\n");
  }
  
  @Override
  public void run()
  {
    try
    {
      out = IOUtils.openOutHard(outputPrefix + ".info");  // FIRST thing
      print("Inv. args.:" + StrUtils.join(invArgs, " "));
      
      // load data
      List<Entry> entries = allEntries(dataDir);
      entries.addAll(pmjEntries(pmjData));
      Arbre<String> arbre = Arbre.parentMap2Arbre(parentMap, "Austronesian");
      arbre = fixProtoLanguages(arbre);
//      IpaParser p = null;
//      if (cleanCognates)
//      {
//        p = new IpaParser(new File(ipaFile));
//        p.parse();
//      }
      // print the tree
      
//      FancyTreeRenderer ftr = new FancyTreeRenderer(new Populator() {
//        @Override public Object populate() {
//          for (String current : parentMap.keySet())
//            add(current, parentMap.get(current));
//          return "Austronesian";
//        }
//      });
//      IO.so(ftr.toString());
      
      
      // restrict to some languages
      if (langs != null || pickClosestModern)
      {
        if (langs != null && pickClosestModern)
          throw new RuntimeException();
        
        Set <String> languages = null; 
        if (langs != null)
          languages = readLangs(langs);
        else
        {
          languages = new HashSet<String>();
          languages.add(langToReconstruct);
          languages.addAll(bestModernLangRelToAncestor(arbre,entries));
        }
        Set<String> allLangs = new HashSet<String>(languages(entries));
        for (String cl : languages)
          if (!allLangs.contains(cl))
            throw new RuntimeException("Unk lang:" + cl);
        entries = restrict(entries, languages);
        //
        arbre = restrict(arbre, languages);
      }
      
      // topology
      arbre = removeSmallDegreeNodes(arbre, minDegree);
//      IO.so(arbre.deepToString());
      PrintWriter topoOut = IOUtils.openOut(outputPrefix + ".topo");
      topoOut.append(arbre.deepToLispString());
      topoOut.close();
      
      // create cognate file
      List<Entry> filteredEntries = (cleanCognates ? clean(entries) : entries);
      print(unigramStatistics(filteredEntries));
      Map<Pair<String,Integer>,Map<String,Entry>> groups = groupCognates(filteredEntries);
      PrintWriter cogOut = IOUtils.openOut(outputPrefix + ".cog");
      List<String> languages = languages(entries);
      cogOut.append(printHeaderLine(languages));
      for (Pair<String,Integer> key : groups.keySet())
      {
        Map<String,Entry> cognateLine = groups.get(key);
        if (cognateLine.size() > 1)
          cogOut.append(printCognateLine(key.getSecond(), cognateLine, languages) + "\n");
      }
      cogOut.close();
    
      // create phylip file
      Map<Pair<String,Integer>,Map<String,Entry>> phylipGroups = groupCognates(entries);
      PrintWriter phylipOut = IOUtils.openOut(outputPrefix + ".phylip"), 
                  distMtxOut = IOUtils.openOut(outputPrefix + ".mtx");
      printPhylipCognateList(phylipGroups, new ArrayList<String>(languages(entries)), phylipOut);
      printDistMtx(phylipGroups, new ArrayList<String>(languages(entries)), distMtxOut);
      
      out.close();
    } 
    catch (Exception e)
    {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    
  }
  
  private void printDistMtx(
      Map<Pair<String, Integer>, Map<String, Entry>> phylipGroups,
      ArrayList<String> langs, PrintWriter distMtxOut)
  {
    List<Pair<String, Integer>> orderedKeys = new ArrayList<Pair<String,Integer>>(phylipGroups.keySet());
    for (int i = 0; i < nBootstrapSamples; i++)
    {
      // crudly prepare bootstrap sample
      System.out.print("[" + i + "]");
      List<Map<String, Entry>> curSample = new ArrayList<Map<String, Entry>>();
      for (int j = 0; j < orderedKeys.size(); j++)
        curSample.add(phylipGroups.get(orderedKeys.get(bootstrapRandom.nextInt(orderedKeys.size()))));
      //
      distMtxOut.append(rFillWithSpaces("" + langs.size(), 5) + "\n");
      CounterMap<String,String> dists = computeDists(curSample, langs);
      for (String lang : langs)
      {
        distMtxOut.append(fillWithSpaces(cleanedLangName(lang, true), 12));
        for (String lang2 : langs)
          distMtxOut.append(EasyFormat.fmt(dists.getCount(lang,lang2)/((double) langs.size())) + "  ");
        distMtxOut.append("\n");
      }
    }
    System.out.println();
    distMtxOut.close();
  }

  private CounterMap<String, String> computeDists(
      List<Map<String, Entry>> phylipGroups,
      ArrayList<String> langs)
  {
    CounterMap<String, String> result = new CounterMap<String, String>();
    for (int group = 0; group < phylipGroups.size(); group++)
    {
      Set<String> currentCognateSet = phylipGroups.get(group).keySet();
      for (int i = 0; i < langs.size(); i++)
        for (int j = 0; j < langs.size(); j++)
          if (currentCognateSet.contains(langs.get(i)) &&
             !currentCognateSet.contains(langs.get(j)))
          {
            result.incrementCount(langs.get(i),langs.get(j), 1.0);
            result.incrementCount(langs.get(j),langs.get(i), 1.0);
          }
    }
    return result;
  }

  private Collection<String> bestModernLangRelToAncestor(
      Arbre<String> arbre, List<Entry> entries)
  {
    Map<Pair<String,Integer>,Map<String,Entry>> map = groupCognates(entries);
    Counter<String> quality = new Counter<String>();
    for (Arbre<String> node : arbre.nodes())
      if (node.isLeaf())
        quality.setCount(node.getContents(), quality(node.getContents(), map));
    for (String key : quality)
      print(key + "\t" + quality.getCount(key));
    Set<String> desc = Arbre.descMap(arbre).get(Arbre.findFirstNodeWithContents(arbre, langToReconstruct));
    List<String> result = new ArrayList<String>();
    int cI = 0, cO = 0;
    for (String current : quality)
      if (desc.contains(current) && cI++ < nInside)
        result.add(current);
      else if (cO++ < nOutside)
        result.add(current);
    return result;
  }

  private double quality(String lang,
      Map<Pair<String, Integer>, Map<String, Entry>> map)
  {
    double result = 0.0;
    for (Map<String,Entry> values : map.values())
      if (values.containsKey(lang) && values.containsKey(langToReconstruct))
        result++;
    return result;
  }

  /**
   * remove all inside and ouside "subtrees" that do not contain at least
   * one language in the provided languages set
   * i.e. 
   * 1- restrict to lowest common ancestor of the languages
   * 2- remove subtrees 
   * @param arbre
   * @param languages
   * @return
   */
  private Arbre<String> restrict(Arbre<String> arbre, Set<String> languages)
  {
    arbre = Arbre.lowestCommonAncestor(arbre, languages).copy();
    Map<Arbre<String>,Set<String>> descMap = Arbre.descMap(arbre);
    for (Arbre<String> node : arbre.nodes())
      if (!CollUtils.intersects(languages, descMap.get(node)))
          Arbre.removeNode(node);
    return arbre;
  }


  private String printHeaderLine(List<String> languages)
  {
    String result = "#id\t";
    for (String lang : languages)
      result += lang + "\t";
    return result + "\n";
  }

  private Set<String> readLangs(String path)
  {
    Set<String> result = new HashSet<String>();
    for (String line : IO.i(path))
      if (!line.equals(""))
        result.add(cleanedLangName(line.split("\t")[0], true));
    return result;
  }

  public static class Entry
  {
    private final String language, languageDescription, gloss, ipa, comments, cognateCode;
    public Entry(String cognateCode, String comments, String gloss,
        String ipa, String language, String languageD)
    {
      this.languageDescription = languageD.intern();
      this.cognateCode = cognateCode.intern();
      this.comments = comments;
      this.gloss = gloss.toLowerCase().replaceAll(" ","").intern();
      this.ipa = ipa;
      this.language = language.intern();
    }
    public List<Integer> cognateCodes(boolean ignoreUncertain)
    {
      return parseCognateIds(cleanCognateId(cognateCode), ignoreUncertain);
    }
  }
  // clip language names to 10 character for the stupid phylip input format
  public String cleanedLangName(String s, boolean clip)
  {
    return cleanedLangName(s, clip, clipL);
  }
  public static String cleanedLangName(String s, boolean clip, int clipL)
  {
    // remove accents
    s = new DiacriticsRemover().removeDiacritics(s);
    // keep only letters
    String result = "";
    for (char c : s.toCharArray())
      if (("" + c).matches("[A-Za-z]"))
        result += c;
    if (result.length() == 0)
      throw new RuntimeException("orig:" + s);
    // up to length 10 (for phylip)
    if (clip)
      return result.substring(0,Math.min(clipL,result.length())); 
    else return result;
  }
  public static String removeReconstructionFlag(String s)
  { 
    return s.replaceFirst("^[*]","");
  }
  public static String removeToneMarkers(String s)
  {
    return s.replaceAll("[0-9]","");
  }
  public static String cleanOptions(String s)
  {
    s = s.replaceAll("[-]","");
    s = s.replaceAll("[(][^)]*[)]","");
    s = s.replaceAll("\\[[^]]*\\]","");
//    s = s.replaceAll("[/].*","");
//    s = s.replaceAll("`", "");
    return s;
  }
  public static class DiacriticsRemover
  {
    private final Counter<String> ops = new Counter<String>();
    private static final Set<Character> badSet = badSet();
    private static Set<Character> badSet()
    {
      Set<Character> result = new HashSet<Character>();
      for (char c = 0x0300; c <= 0x036F; c++) // all the nonspacing diacritics
        result.add(c);
      char [] spacingDiac = {0x02C8,0x02CC,0x02D0,0x02D1,0x02BC,
          0x02B4,0x02B0,0x02B1,0x02B2,0x02B7,0x02E0,0x02E4,0x02DE,'\'',':'};
      for (char c : spacingDiac) result.add(c);
      return result;
    }
    public String removeDiacritics(String s)
    {
      s = Normalizer.normalize(s, Normalizer.Form.NFKD);
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < s.length(); i++)
      {
        char c = s.charAt(i);
        if (!badSet.contains(c))
          result.append(c);
        else
        {
          String prevChar = "" + ((i-1)>=0?s.charAt(i-1):"# ");
          ops.incrementCount("" + prevChar + c +  "->" + prevChar, 1.0);
        }
      }
      return result.toString();
    }
    @Override
    public String toString()
    {
      return "Diacritics removed: " + ops.toString();
    }
  }
  public static List<String> languages(List<Entry> entries)
  {
    Set<String> result = new HashSet<String>();
    for (Entry entry : entries)
      result.add(entry.language);
    return new ArrayList<String>(result);
  }
  // returns (gloss,cognateId) -> (language->entry)
  public static Map<Pair<String,Integer>,Map<String,Entry>> groupCognates(List<Entry> entries)
  {
    Map<Pair<String,Integer>,Map<String,Entry>> result 
      = new HashMap<Pair<String,Integer>, Map<String,Entry>>();
    for (Entry entry : entries)
      for (int cognateCode : entry.cognateCodes(true))
      {
        Pair<String,Integer> key = Pair.newPair(entry.gloss, cognateCode);
        if (!result.containsKey(key)) 
          result.put(key, new HashMap<String, Entry>());
        result.get(key).put(entry.language, entry);
      }
    return result;
  }
  public static List<Entry> restrict(List<Entry> entries, Set<String> langs)
  {
    List<Entry> result = new ArrayList<Entry>();
    for (Entry entry : entries)
      if (langs.contains(entry.language))
        result.add(entry);
    return result;
  }
  public static final String UNK = "?";
  public static String printCognateLine(int cogCode, Map<String,Entry> entries, List<String> languages)
  {
    StringBuilder result = new StringBuilder();
    List<Entry> list = new ArrayList<Entry>(entries.values());
    if (list.size() > 1)
      if (!list.get(0).gloss.equals(list.get(1).gloss))
        throw new RuntimeException();
    result.append("" + list.get(0).gloss + "(" + cogCode + ") ");
    for (String lang : languages)
    {
      Entry e = entries.get(lang);
      result.append((e==null ? UNK : e.ipa));
      result.append(" ");
    }
    return result.toString();
  }
  /**
   * slit CSV, handling quotes, but not escaped quotes
   * @param string
   * @return
   */
  public static String [] split(String string)
  {
    ArrayList<String> result = new ArrayList<String>();
    int lastIndex = 0;
    boolean inQuote = false;
    for (int i = 0; i < string.length(); i++)
    {
      char c = string.charAt(i);
      if (c == '\"' && (i == 0 || string.charAt(i-1) != '\\')) inQuote = !inQuote;
      if (c == ',' && !inQuote)
      {
        result.add(string.substring(lastIndex, i)
            .replaceFirst("^[\"]", "")
            .replaceFirst("[\"]$",""));
        lastIndex = i + 1;
      }
    }
    result.add(string.substring(lastIndex, string.length()));
    String [] converted = new String[result.size()];
    for (int i = 0; i < result.size(); i++) converted[i] = result.get(i);
    return converted;
  }
  /**
   * can have comma separated cognates
   * @param string
   * @return
   */
  private static List<String> cleanCognateId(String string)
  {
    List<String> result = new ArrayList<String>();
    if (string.equals("")) return result;
    string = string.replaceAll("\"", "").replaceAll("[.]", "").replaceAll(" ","");
    if (string.contains(",")) 
      result.addAll(Arrays.asList(string.split(",")));
    else 
      result.add(string);
    return result;
  }
  private static List<Integer> parseCognateIds(List<String> cleaneds, boolean ignoreUncertain)
  {
    List<Integer> result = new ArrayList<Integer>();
    for (String cleaned : cleaneds)
      try
      {
        if (!ignoreUncertain || !cleaned.contains("?"))
          result.add(Integer.parseInt(cleaned.replaceAll("[?]", "")));
      } catch (NumberFormatException nfe) {}
    return result;
  }
  public static class BadLangName extends RuntimeException
  { public BadLangName() { super("Lang name not found"); } }
  public List<Entry> parse(File file, boolean ignoreLoans) throws IOException
  {
    int lineNumber = 0;
    int numberOfHeaderSeen = 0;
    String currentLanguage = null, currentLangDescr = null;
    final List<Entry> result = new ArrayList<Entry>();
    String langSpecs = "";
    for (String line : IO.i(file)) 
    {
      String [] splitLine = split(line);
      if (line.matches("^id.*$"))
      {
        numberOfHeaderSeen++;
        if (numberOfHeaderSeen > 2) throw new RuntimeException();
        else if (numberOfHeaderSeen == 2)
        {
          String [] splitLangLine = split(langSpecs);
          currentLangDescr = splitLangLine[1];
          if (badLangName(currentLangDescr))
            throw new BadLangName();
          currentLanguage = cleanedLangName(currentLangDescr, true);
          processLanguageHierarchyInfo(splitLangLine, currentLanguage);
        }
        else ;
      }
      else if (numberOfHeaderSeen == 1) langSpecs += line;
      else if (line.equals("")) ;
      else if (line.matches("[0-9]+[,].*"))
      {
        try
        {
          result.add(new Entry(splitLine[6], splitLine[4], 
              splitLine[2], splitLine[3], currentLanguage, currentLangDescr));
        }
        catch (Exception e)
        {
          print("Bad line in " + file.getName() + ": " + line);
        }
      }
      else print("Weird line in " + file.getName() + ":" + line);
      lineNumber++;
    }
    return result;
  }
  // map (lang or family) -> its parent
  public static HashMap<String,String> parentMap = new HashMap<String,String>(),
  certificates = new HashMap<String,String>();
  private String cleanFamilyName(String s)
  {
    s = s.replaceAll("^\\s*", "")
    .replaceAll("\\s*$","");
    s = cleanedLangName(s, false);
    return s;
  }
  private static final String PROTOMTCH = "Proto.*";
  private  Arbre<String> fixProtoLanguages(Arbre<String> a)
  {
    String contents = (findProto(a) == null ? a.getContents() : findProto(a));
    List<Arbre<String>> children = new ArrayList<Arbre<String>>();
    for (Arbre<String> child : a.getChildren())
      if (!child.getContents().matches(PROTOMTCH))
        children.add(fixProtoLanguages(child));
    return new Arbre<String>(contents, children);
  }
  private String findProto(Arbre<String> a)
  {
    if (a.getContents().equals("Oceanic"))
      return oceanicRecon;
    if (a.getContents().equals("CentralMalayoPolynesian"))
      return centralMalayoPolyRecon;
    for (Arbre<String> child : a.getChildren())
      if (child.getContents().matches(PROTOMTCH))
      {
        if (child.getChildren().size() == 0)
          return child.getContents();
        else if (child.getChildren().size() != 1)
          throw new RuntimeException();
        else
          return child.getChildren().get(0).getContents();
      }
    return null;
  }
  private Arbre<String> removeSmallDegreeNodes(Arbre<String> tree, int minDegree)
  {
    tree = tree.root().copy();
    int nRemoved;
    do
    {
      print(tree.deepToString());
      nRemoved = 0;
      for (Arbre<String> node : tree.nodes())
        if (!node.isRoot() && !node.getContents().equals(langToReconstruct) 
            && !node.isLeaf() && node.getChildren().size() < minDegree)
        {
          Arbre.removeNode(node);
          nRemoved++;
        }
    }
    while (nRemoved > 0);
    return tree;
  }
  private void processLanguageHierarchyInfo(String [] line, String lang)
  {
    if (line.length < 7)
    {
      System.err.println("Bad language spec line:" + StrUtils.join(line));
      return;
    }
    String [] hierarchy = line[6].split("[,]");
    List<String> converted = new ArrayList<String>();
    for (String item : hierarchy)
      converted.add(cleanFamilyName(item));//cleanedLangName(item));
    annotate(converted);
    if (!converted.contains(lang))
      converted.add(lang);
    for (int i = 1; i < converted.size(); i++)
    {
      String parent = converted.get(i-1);
      String current = converted.get(i);
      String recordedParent = parentMap.get(current);
      if (recordedParent != null && !recordedParent.equals(parent))
        System.err.println("Conflicting parent info for " + 
            current + ": " + recordedParent + " vs. " + parent + " \n\t" + certificates.get(current) + "\n\t" + converted.toString());
      else
      {
        parentMap.put(current, parent);
        certificates.put(current, converted.toString());
      }
    }
  }
  // simple parent annotation (e.g. several families are called "Northern")
  // also removes (A), etc, they seem to cause problems beyond annotation
  private static void annotate(List<String> converted)
  {
    //
    Iterator<String> path = converted.iterator();
    while (path.hasNext())
      if (path.next().matches("([A-Z]|[A-Z][A-Z])"))
        path.remove();
    for (int i = 1; i < converted.size(); i++)
      if (problematicLables.contains(converted.get(i)))
          converted.set(i,converted.get(i) + "_" + converted.get(i-1));
//    System.out.println(converted.toString());
  }
  public static Set<String> problematicLables = new HashSet<String>(Arrays.asList(
      "West","East", "Central","Western","Nuclear","Northern","Southern","South","Eastern", "North", "Peripheral","Coastal", "Proto"));

  // only digits is bad
  public static boolean badLangName(String s)
  {
    boolean hasLetter = false;
    for (char c : s.toCharArray())
      if (Character.isLetter(c))
        hasLetter = true;
    return !hasLetter;
  }
  
  public static Set<String> bestLangs(int n, Collection<Entry> entries)
  {
    Counter<String> wordPerLang = new Counter<String>();
    for (Entry entry : entries)
      if (entry.cognateCode != null)
        wordPerLang.incrementCount(entry.language, 1.0);
    Set<String> langs = new HashSet<String>();
    int i = 0;
    for (String word : IteratorWrapper.IW(wordPerLang.asPriorityQueue()))
    {
      langs.add(word);
      i++;
      if (i == n) break;
    }
    return langs;
  }
  public List<Entry> allEntries(String dataDir)
  {
    try
    {
    final File directory = new File(dataDir);
    List<Entry> allEntries = new ArrayList<Entry>();
    for (File current : directory.listFiles())
      if (!current.isHidden() && !current.getName().equals("CVS"))
        try { allEntries.addAll(parse(current, true)); }
        catch (BadLangName bln) {}
    return allEntries;
    }
    catch (Exception e) { throw new RuntimeException(e); }
  }
  private List<Pair<String,String>> rules = null;
  private List<Pair<String,String>> getRules()
  {
    if (rules != null) return rules;
    rules = new ArrayList<Pair<String,String>>();
    if (sampa2ipaRulesFile != null && !sampa2ipaRulesFile.equals(""))
      for (String line : IO.i(sampa2ipaRulesFile))
      {
        String [] fields = line.split("\t");
        String lhs = fields[0];
        String rhs = "";
        if (fields.length > 1)
          rhs = fields[1];
        rules.add(Pair.makePair(lhs,rhs));
      }
    return rules;
  }
  private String sampa2Ipa(final String in)
  {
    String res = in;
    for (Pair<String,String> rule : getRules())
      res = res.replaceAll(rule.getFirst(), rule.getSecond());
    if (!res.equals(in))
      System.err.println("Converted X-SAMPA:" + in + "->" + res);
    return res;
  }
  public List<Entry> pmjEntries(String file)
  {
    if (file.equals("")) return Collections.emptyList();
    final String PMJ_NOTH = "PMJNoth",
                 PMJ_PRAG = "PMJPrag";
    List<Entry> result = new ArrayList<Entry>();
    int i = 0;
    for (String line : IO.i(file)) if (i++ > 0)
    {
      String [] fields = split(line);
      if (fields.length < 7)
        throw new RuntimeException(line);
      String nothNotes = fields[3],
             pragNotes = fields[5] + " status=" + fields[6],
             cogCode = fields[2],
             gloss = fields[0],
             noth = fields[1],
             prag = fields[4];
      prag = prag.replaceAll("[,].*","");
      prag = prag.replaceAll("\\[","");
      prag = prag.replaceAll("\\|.*\\]","");
      prag = sampa2Ipa(prag);
      noth = sampa2Ipa(noth);
      result.add(new Entry(cogCode, nothNotes, gloss, noth, PMJ_NOTH, PMJ_NOTH));
      result.add(new Entry(cogCode, pragNotes, gloss, prag, PMJ_PRAG, PMJ_PRAG));
    }
    return result;
  }
  public List<Entry> clean(List<Entry> allEntries)//, Set<Character> characters)
  {
    Counter<Character> invalidChars = new Counter<Character>();
    List<Entry> filteredEntries = new ArrayList<Entry>();
    DiacriticsRemover dr = new DiacriticsRemover();
    int badEntries = 0;
    for (Entry entry : allEntries)
    {
      String current = entry.ipa;
      
      // remove initial star
      current = removeReconstructionFlag(current);
      
      // remove diac
      current = dr.removeDiacritics(current);
      

      
      // remove options
      current = cleanOptions(current);

      
      current = removeToneMarkers(current);
      
      boolean bad = false;
      for (char c : current.toCharArray())
        if (c == ' ' || c == '\t')
        {
          bad = true;
//          invalidChars.incrementCount(c, 1.0);
        }
      if (bad || current.length() == 0)
      {
//        IO.so("Bad:" + current);
        badEntries++;
      }
      else
      {
        // convert SAMPA characters (capitalized)
        current = sampa2Ipa(current);
        
        filteredEntries.add(new Entry(entry.cognateCode,entry.comments,entry.gloss,
            current,entry.language, entry.languageDescription));
      }
    }
    print(dr.toString());
//    IO.so("" + invalidChars);
//    IO.so("Total bad character tokens:" + invalidChars.totalCount());
//    IO.so("Total bad character types :" + invalidChars.size());
    print("Fraction of multi-word items rejected:" + badEntries + " / " + allEntries.size());
    return filteredEntries;
  }

  public static void printPhylipCognateList(Map<Pair<String,Integer>,Map<String,Entry>> group, 
      List<String> languages, PrintWriter out)
  {
    out.append("\t" + languages.size() + "\t" + group.size() + "\n");
    List<Map<String,Entry>> characters = new ArrayList<Map<String,Entry>>(group.values());
    int i = 0;
    for (String lang : languages)
    {
      out.append(fillWithSpaces(lang,10));
      for (Map<String,Entry> character : characters)
          out.append((character.containsKey(lang) ? "1" : "0"));
      out.append("\n");
      i++;
    }
    out.close();
  }
  public static String rFillWithSpaces(String s, int n)
  {
    if (s.length() > n) throw new RuntimeException();
    String res = "";
    int delta = n - s.length();
    for (int i = 0; i < delta; i++)
      res += " ";
    res += s;
    return res;
  }
  public static String fillWithSpaces(String s, int n)
  {
    if (s.length() > n) 
      return s.substring(0,n);
    int delta = n - s.length();
    for (int i = 0; i < delta; i++)
      s += " ";
    return s;
  }

//  public static final String [] Polynesian = {
//    "Tongan", "Niue", "Samoan", cleanedLangName("Rapanui (Easter Island)"),
//    cleanedLangName("Tahitian (Modern)"), "Maori", "Rarotongan", "Marquesan",
//    "Hawaiian"}; 

  /**
   * @param args
   * @throws IOException 
   * @throws ParseException 
   */
  public static void main(String[] args) throws IOException//, ParseException 
  {
    Austro2Corpus a2c = new Austro2Corpus(args);
    Execution.run(args, a2c);
    
//    IpaParser p = new IpaParser(new File("encodings/IPAFeatures"));
//    p.parse();
////    IO.so("" + p.db.characters());
//    
//    
//    List<Entry> allEntries = allEntries();
//    
////    Set<String> restrictedLangs = bestLangs(10,allEntries);
////    List<Entry> restricted = restrict(allEntries, restrictedLangs);
//    
//    
////    List<Entry> filteredEntries = clean(allEntries, p.db.characters());
//    
//    Map<Pair<String,Integer>,Map<String,Entry>> unfilteredGroups = groupCognates(allEntries);
////    Map<Pair<String,Integer>,Map<String,Entry>> groups = groupCognates(filteredEntries);
////    Map<Pair<String,Integer>,Map<String,Entry>> restrictedGroup = groupCognates(restricted);
//    
//    
//    System.out.println(phylipCognateList(unfilteredGroups, new ArrayList<String>(languages(allEntries))));
//    
////    Counter<Integer> sizes = new Counter<Integer>();
////    
////    for (Map<String,Entry> group : groups.values())
////      sizes.incrementCount(group.size(), 1.0);
////    
////    System.out.println(sizes.toString());
//    
//    
//
//    
////    List<String> langs = languages(filteredEntries);
////    
////    for (int i = 2; i < 3; i++)
////    {
////      int n = 0;int t =0;
////      for (Map<String,Entry> cognateLine : groups.values())
////        if (cognateLine.size() > i)
////        {
////          IO.so(printCognateLine(cognateLine, langs));
////          n++;
////          t+=cognateLine.size();
////        }
////      System.out.println("n="+n+",t="+t);
////    }
//    
////    
////    int i = 0;
////    DiacriticsRemover remover = new DiacriticsRemover();
////    for (Entry entry : allEntries)
////      if (!entry.ipa.equals(remover.removeDiacritics(entry.ipa)))
////      {
////        IO.so(entry.ipa);
////        i++;
////      }
////    System.out.println("n:" + i);
////    IO.so(remover.toString());
////    List<String> langs = languages(allEntries);
////    langs = new ArrayList<String>(bestLangs(20, allEntries));
//////    Collections.shuffle(langs);
//////    langs = langs.subList(0,10);
////    
////    allEntries = restrict(allEntries, new HashSet<String>(langs));
////    Counter<Integer> histogram = new Counter<Integer>();
////    Map<Pair<String,Integer>,Map<String,Entry>> db = 
////      groupCognates(allEntries);
////    for (Map<String,Entry> cognateLine : db.values())
////    {
////      histogram.incrementCount(cognateLine.size(), 1.0);
////      if (cognateLine.size() > 1)
////      IO.so(printCognateLine(cognateLine, langs));
////    }
////    System.out.println(histogram);
  }
  
  public static Set<Character> weirdCharacters() throws IOException
  {
    Set<Character> result = new HashSet<Character>();
    for (String line : IO.i("encodings/weirdIPAs"))
      for (int i = 0; i < line.length(); i++)
        result.add(line.charAt(i));
    return result;
  }


}
