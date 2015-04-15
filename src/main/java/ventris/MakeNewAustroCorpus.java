package ventris;
import java.io.*;
import java.util.*;

import fig.basic.IOUtils;

import nuts.lispparser.LispParser;
import nuts.lispparser.ParseException;
import nuts.util.CollUtils.*;
import nuts.util.Arbre;
import nuts.util.Counter;
import nuts.util.Tree;
import static nuts.util.CollUtils.*;
import static nuts.io.IO.*;
import static nuts.util.MathUtils.*;


public class MakeNewAustroCorpus
{

  /**
   * @param args
   * @throws ParseException 
   */
  public static void main(String[] args) throws ParseException
  {
    // read tree
    LispParser lp = new LispParser(IOUtils.openInHard("/Users/bouchard/w/evolvere/data/austro/20100807/processed/All.topo"));
    Tree<String> oriTree = lp.parse();
    System.out.println("Original tree:\n" + Arbre.tree2Arbre(oriTree).deepToString());
    
    // read list of langs
    Set<String> langs = set();
    for (String line : i("/Users/bouchard/w/evolvere/data/austro/allProtoBestLangs-editD.txt"))
      langs.add(line);
    
    // create the flatter tree (only has proto and modern langs)
    System.out.println("Flatter tree:" + Arbre.tree2Arbre(flatter(oriTree).get(0)).deepToString());
    
    System.out.println("List of langs:" + langs);
    
    Arbre<String> transformed =Arbre.tree2Arbre(transform(oriTree, langs));
    System.out.println("Transformed:\n" + transformed.deepToString());
    Arbre<String> removedUn = Arbre.tree2Arbre(removeUnaries(Arbre.arbre2Tree(transformed),null));
    System.out.println("Removed unaries:\n" + removedUn.deepToString());
    System.out.println("Lisp:" + removedUn.deepToLispString());
  }
  
  private static List<Tree<String>> flatter(Tree<String> oriTree)
  {
    List<Tree<String>> flatterChildren = list();
    
    if (oriTree.isLeaf())
    {
      flatterChildren.add(oriTree);
      return flatterChildren;
    }
    
    for (Tree<String> child : oriTree.getChildren())
      flatterChildren.addAll(flatter(child));
    
    if (isProto(oriTree.getLabel()))
    {
      List<Tree<String>> result = list();
      result.add(new Tree<String>(oriTree.getLabel(), flatterChildren));
      return result;
    }
    else return flatterChildren;
  }

  private static Tree<String> transform(Tree<String> t, Set<String> langs)
  {
    List<Tree<String>> newChildren = list();
    for (Tree<String> child : t.getChildren())
    {
      Tree<String> transformedChild = transform(child,langs);
      if (transformedChild != null)
        newChildren.add(transformedChild);
    }
    boolean isInLangs = langs.contains(t.getLabel());
    if (isInLangs || newChildren.size() > 0)
      return new Tree<String>(t.getLabel(), newChildren);
    else
      return null;
  }
  
  
  public static boolean isProto(String str) { return str.contains("Proto"); }
  private static Tree<String> removeUnaries(Tree<String> t, String prevErased)
  {
    if (t.getChildren().size() == 1)
    {
      String erased = t.getLabel();
      if (isProto(erased))
      {
        if (prevErased != null)
          System.err.println("Had to erase proto:" + prevErased);
        prevErased = erased;
      }
      return removeUnaries(t.getChildren().get(0), prevErased);
    }
    List<Tree<String>> newChildren = list();
    for (Tree<String> child : t.getChildren())
      newChildren.add(removeUnaries(child, null));
    String label = t.getLabel();
    if (prevErased != null)
    {
      if (isProto(label))
        System.err.println("Had to erase proto:" + prevErased);
      label = prevErased;
    }
    return new Tree<String>(label, newChildren);
  }

}
