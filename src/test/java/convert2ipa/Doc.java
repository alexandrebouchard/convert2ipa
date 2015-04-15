package convert2ipa;


import tutorialj.Tutorial;

public class Doc
{
  /**
   * 
   * Summary 
   * -------
   * 
   * convert2ipa is a tool that was used for data pre-processing for the following paper:
   * 
   * ```
   * Alexandre Bouchard-Côté, Percy Liang, Thomas Griffiths, and Dan Klein. (2007) 
   * A Probabilistic Approach to Diachronic Phonology. 
   * Proceedings of the 2007 Conference on Empirical Methods on Natural Language Processing (EMNLP07). 12:887-896
   * ```
   * 
   * Note that this package is purely for documentation purpose, and is not maintained.
   * 
   * See at the bottom of this file for some basic documentation on using this to convert 
   * text in orthographic form into IPA using a set of rules.
   * 
   * (Over)-simple rules for Italian, Latin, Portuguese, and Spanish are available
   * in ``rules/[language].rules``. Note that these may have varying degrees of accuracy.
   * 
   * Installation
   * ------------
   * 
   * To install the package:
   * 
   * - Install the latest version of java and gradle
   * - Check out the source ``git clone git@github.com:alexandrebouchard/convert2ipa.git``
   * - Compile using ``gradle installApp``
   * 
   *   
   * Basic usage
   * -----------
   * 
   * Type:
   * 
   * ```
   * build/install/convert2ipa/bin/main -rulesPath rules/[language].rules -input [inputFile] -output [outputFile]
   * ```
   * 
   * Make sure to use UTF-8 encodings for all files involved.
   * 
   */
  @Tutorial(startTutorial = "README.md", showSource = false)
  public void installInstructions() {}
  
  
}
