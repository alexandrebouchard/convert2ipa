package ventris;

import java.io.IOException;
import nuts.io.IO;
import java.io.*;
import java.util.*;
import java.lang.reflect.Array;


/**
 * A text to IPA converter.
 * 
 * Usage: Text2IPAConverter (converter)
 * currently converter can only be "identity", more to come...
 */
public class Text2IPA
{
//  public static String dataPath = "/home/eecs/bouchard/data/ventrisRules/";
//  
//	/**
//	 * The interface you must implement to convert stuff text to IPA
//	 *
//	 */
//	public static interface Text2IPAConverter
//	{
//		public String convert(String s);
//	}
//	public static void main(String [] args) throws IOException
//	{
//		if (args.length < 1)
//		{
//			System.err.println("Usage: Text2IPAConverter <converter> <rule path>");
//			return;
//		}
//		String lang = args[0].toLowerCase();
//    if(args.length >= 2) dataPath = args[1];
//		System.err.println("Using language: " + lang);
//		System.err.println("Loading from: " + dataPath);
//
//		final boolean TEST = false;															// show test cases
//		
//		//PrintStream out = new PrintStream(System.out, true, "UTF-16"); //////////////////////////
//    PrintStream out = new PrintStream(System.out, true, "UTF-8");
//
//		Text2IPAConverter converter = null; 
//		// TODO : load the right module depending on the language...
//		// convert line by line
//		if (lang.equals("identity"))
//		{
//			converter = new IdentityConverter();
//		}
//		else if (lang.equals("fr"))
//		{
//			converter = FrenchConverter.getInstance(); 
//			if (TEST) {
//				((FrenchConverter) converter).test(); 	
//			}
//		}
//		else if (lang.equals("de"))
//		{
//			converter = GermanConverter.getInstance(); 
//			if (TEST) {
//				((GermanConverter) converter).test(); 	
//			}
//		}
//		else if (lang.equals("it"))
//		{
//			converter = ItalianConverter.getInstance(); 
//			if (TEST) {
//				((ItalianConverter) converter).test(); 	
//			}
//		}
//		else if (lang.equals("pt"))
//		{
//			converter = PortugueseConverter.getInstance(); 
//			if (TEST) {
//				((PortugueseConverter) converter).test(); 	
//			}
//		}
//		else if (lang.equals("es"))
//		{
//			converter = SpanishConverter.getInstance(); 
//			if (TEST) {
//				((SpanishConverter) converter).test(); 									
//			}
//		}
//    else if (lang.equals("la"))
//    {
//      converter = LatinConverter.getInstance(); 
//      if (TEST) {
//        ((LatinConverter) converter).test();                  
//      }
//    }
//		else
//		{
//			System.out.println("Unknown converter.");
//			return;
//		}
//		for (String str : IO.si())
//		{
//			//IO.so(converter.convert(str)); ///////////////
//			out.println(converter.convert(str));
//		}
//    out.close();
//	}
//
//	/**
//	 * The trivial and uninteresting converter
//	 *
//	 */
//	public static class IdentityConverter implements Text2IPAConverter
//	{
//		public String convert(String s)
//		{
//			return s;
//		}
//	}
//
//	
//	
//	
//	
//	
//
//
//
//	/**
//	 * LangConverter is a generic converter used only to abstract away the HashMap phones
//	 * property and the convert method, which operates the same for all languages. The only
//	 * difference in subclasses will be the entries put into phones.
//	 * 
//	 * The purpose of this class is to be extended.
//	 */
//	protected static abstract class LangConverter implements Text2IPAConverter  {
//		// phones maps each character in the language to either a String with its unique 
//		// phonemic transcription, or else an array of Option objects, each containing
//		// a relationship predicate and corresponding phonemic transcription 
//		protected static HashMap<Character, Object> phones;
//		
//		/**
//		 * convert takes in a String and returns a string in which all words 
//		 * are transcribed into phonemes for the particular language of the class.
//		 */
//		public String convert(String s) 
//		{
//			String converted = new String();
//			int len = s.length();
//
//			for (int pos = 0; pos < len; pos++) {				
//				char c = s.charAt(pos);
//				char prev = '\0'; 
//				char next = '\0'; 
//				char nnext = '\0';
//				
//				/*
//				try {
//					PrintStream out = new PrintStream(System.out, true, "UTF-16");			////////////////////////////////////////////////////
//					out.println("cur char: " + c);					//////////////////////////////////////////////////////////////////////////////
//				}
//				catch (Exception e) {
//					;
//				}
//				*/
//				
//				
//				if (c == ' ') {
//					converted = converted.concat(" ");
//					continue;
//				}
//
//				Object o = phones.get(c);
//				if(o == null) { 											 //ignore punctuation, numerals
//					continue;
//				}
//
//				if (pos > 0) {
//					prev = s.charAt(pos-1);
//					if( ! Character.isLetter(prev)) {						//ignore punctuation, numerals
//						prev = '\0';
//					}
//				}
//				
//				if (pos < len - 1) 
//				{
//					next = s.charAt(pos+1);
//					if( ! Character.isLetter(next)) {						//ignore punctuation, numerals
//						next = '\0';
//					}
//					if (pos < len - 2) 
//					{
//						nnext = s.charAt(pos+2);
//						if( ! Character.isLetter(nnext)) {						//ignore punctuation, numerals
//							nnext = '\0';
//						}
//					}
//				}
//
//				if (o.getClass() == String.class) {						// Only one phoneme option to transcribe to
//					converted = converted.concat((String) o);
//					
//					/*
//					try {
//						PrintStream out = new PrintStream(System.out, true, "UTF-16");			////////////////////////////////////////////////////
//						out.println("added: " + (String) o);							////////////////////////////////////////////////////////////
//					}
//					catch (Exception e) {
//						;
//					}*/
//					
//					
//				}
//				else {  													// array of possible Options
//					Option[] options = ((Option[]) o);
//					for(int i = 0; i < options.length; i++)  {
//						if (options[i] != null && options[i].matches(prev, next, nnext))  {
//							converted = converted.concat(options[i].result);
//							
//							/*
//							try {
//								PrintStream out = new PrintStream(System.out, true, "UTF-16");			////////////////////////////////////////////////////
//								out.println("index: " + Integer.toString(i));
//								out.println("added: " + options[i].result);							////////////////////////////////////////////////////////////
//							}
//							catch (Exception e) {
//								;
//							}
//							*/
//							
//							
//							if (options[i].removeNext) {
//								pos++;
//							}
//							break;
//						}	
//					}
//				}
//			}
//			return converted;
//		}
//		
//		//take the string to transcribe along with it's correct transcription, compare transcription and print corrections if needed
//		public void test(PrintStream out, String s, String c) {              
//			try {			
//				String t = this.convert(s).trim();
//				out.println("\nText:\t\t\t" + s + "\nTranscription:\t" + t);
//				if (!t.equals(c)) {
//					out.println("Should be:\t\t" + c);
//				}  
//			}
//			catch (Exception e) 
//			{
//				System.out.println("error: " + e.getMessage());
//			}
//			
//		}
//		
//		
//		
//		public static Object parseElement(String elt) {
//			
//			// check if elt represents a boolean
//			if (elt.equals("true")) {
//				return new Boolean(true);
//			}
//			if (elt.equals("false")) {
//				return new Boolean(false);
//			}
//			
//			// check if elt represents a relation
//			Integer relation = (Integer) Option.relationsMap.get(elt);
//			if (relation != null) {
//				return relation;
//			}
//			
//			// check if elt represents a string
//			if (elt.charAt(0) == '\"') {
//				return elt.substring(1, elt.length() - 1);				// get rid of quotations
//			}
//			
//			// check if elt represents a char
//			if (elt.charAt(0) == '\'') {
//				return elt.charAt(1);											// extract char from quotations
//			}
//				
//			System.err.println("Error parseElement could not parse: " + elt);
//			return null;
//		}
//		
//		
//		
//		
//		
//		
//		public static HashMap getDict(String fileName) {
//			
//			final boolean DEBUG = false;															// show debug info
//			
//			
//			HashMap dict = new HashMap<Character, Object>(50);
//			String line;
//			try {                            
//				//PrintStream out = new PrintStream(System.out, true, "UTF-16"); 
//        PrintStream out = new PrintStream(System.out, true, "UTF-8"); 
//				//fileName = System.getProperty("user.dir") + "\\src\\ventris\\" + fileName;					// assumes dict files are in the src\ventris dir
//				fileName = new File(dataPath, fileName).toString();
//        FileInputStream fstream = new FileInputStream(fileName);  
//				InputStreamReader in = new InputStreamReader(fstream);
//				try {
//				in = new InputStreamReader(fstream, "UTF8");
//				}
//				catch (UnsupportedEncodingException e) {
//					out.println("unsupported encoding exception");
//				}
//				if (DEBUG) {
//					out.println("Running getDict with file: " + fileName);
//					out.println("isr encoding: " + in.getEncoding());
//		    	}
//				
//				BufferedReader br = new BufferedReader(in);
//				// Read lines while there are still some left to read
//				
//				while (br.ready()) {
//			    	line = br.readLine();
//			    	if (DEBUG) {
//			    		out.println("CURLINE: " + line); 
//			    	}
//			    	String[] tokens = line.trim().split("\\s+");								// split line on whitespace
//			    	if (line.equals("") || Array.getLength(tokens) < 1) {											// ignore empty lines
//			    		continue;
//			    	}
//			    	Character key = new Character(tokens[0].charAt(0));
//			    	int numEntries = Integer.parseInt(tokens[1]);
//			    	if (numEntries < 2) {																// single char mapping
//			    		String translation = tokens[2].substring(1, tokens[2].length() - 1);
//			    		dict.put(key, translation);
//			    	} 
//			    	else {																						// multiple Option mapping
//			    		Option[] optList = new Option[numEntries];
//			    		int curEntry = 0;
//				    	while(br.ready() && curEntry < numEntries) {
//				    		line = br.readLine();
//				    		if (DEBUG) {
//				    			out.println("curline: " + line);
//					    	}
//				    		int commentIndex = line.indexOf('/');								
//				    		if (commentIndex == 0) {												// whole line is comment
//				    			continue;
//				    		}
//				    		if (commentIndex > 0) {													// remove comments
//				    			line = line.substring(0, commentIndex);
//				    		}
//				    		String[] entryTokens = line.trim().split("\\s+");				// split line on whitespace
//				    		int numParams = Array.getLength(entryTokens) - 1;
//				    		
//				    		if (entryTokens[0].equals("Option")) {
//				    			switch (numParams) {
//				    			case 3:
//				    				optList[curEntry] = new Option((Integer) parseElement(entryTokens[1]), 
//				    																(String) parseElement(entryTokens[2]),
//				    																(Boolean) parseElement(entryTokens[3]) );
//				    				break;
//				    			case 4:
//				    				optList[curEntry] = new Option((Integer) parseElement(entryTokens[1]), 
//				    																(Character) parseElement(entryTokens[2]), 
//				    																(String) parseElement(entryTokens[3]),
//				    																(Boolean) parseElement(entryTokens[4]) );
//				    				break;
//				    			default:
//				    				out.println("Error parsing option parameters : " + line);
//				    			}
//				    		}
//				    		else if (entryTokens[0].equals("Complex")) {
//				    			switch (numParams) {
//				    			case 4:
//				    				optList[curEntry] = new ComplexOption((Integer) parseElement(entryTokens[1]), 
//				    																(Integer) parseElement(entryTokens[2]),
//				    																(String) parseElement(entryTokens[3]),
//				    																(Boolean) parseElement(entryTokens[4]));
//				    				break;
//				    			case 5:
//				    				if (entryTokens[2].charAt(0) == '\'')	{														// if second parameter is a character
//				    					optList[curEntry] = new ComplexOption((Integer) parseElement(entryTokens[1]), 
//				    																(Character) parseElement(entryTokens[2]),  
//				    																(Integer) parseElement(entryTokens[3]),   
//				    																(String) parseElement(entryTokens[4]), 
//				    																(Boolean) parseElement(entryTokens[5]) );
//				    				} 
//				    				else {																										//if second parameter is an int
//				    					optList[curEntry] = new ComplexOption((Integer) parseElement(entryTokens[1]), 
//																					(Integer) parseElement(entryTokens[2]),  
//																					(Character) parseElement(entryTokens[3]),    
//																					(String) parseElement(entryTokens[4]), 
//																					(Boolean) parseElement(entryTokens[5]) );
//				    				}
//				    				break;
//				    			case 6:
//				    				optList[curEntry] = new ComplexOption((Integer) parseElement(entryTokens[1]), 
//				    																(Character) parseElement(entryTokens[2]), 
//				    																(Integer) parseElement(entryTokens[3]),
//				    																(Character) parseElement(entryTokens[4]),
//				    																(String) parseElement(entryTokens[5]),
//				    																(Boolean) parseElement(entryTokens[6]) );
//				    				break;
//				    			default:
//				    				out.println("Error parsing option parameters : " + line);
//				    			}
//				    		}
//				    		else {
//				    			out.println("Error reading line : " + line);
//				    		}
//				    		
//				    		curEntry++;
//				    	}
//				    	dict.put(key, optList);
//			    	}
//				}
//			} catch (Exception e)	{
//				try {
//					//PrintStream out = new PrintStream(System.out, true, "UTF-16"); 
//          PrintStream out = new PrintStream(System.out, true, "UTF-8"); 
//					out.println("Error reading file: " + fileName);
//					out.println("Exception: " + e.getMessage());
//					e.printStackTrace(out);
//				} catch(IOException ioe) {
//					;
//				}
//			}
//			
//			return dict;
//		}
//		
//	}
//
//
//
//
//
//	
//	
//	
//	
//	
//	
//	
//	
//	
//
//	
//
//
//
//
//
//
//	//TODO:
//	/**
//	 * Text2IPAConverter for French
//	 * 
//	 * Note: use FrenchConverter.getInstance() instead of a constructor.
//	 * Simplification: This does not apply changes in sound because of adjacent words. Each word is pronounced as if it were isolated.
//	 */
//	public static class FrenchConverter extends LangConverter
//	{
//		private static FrenchConverter _instance;
//
//		public static FrenchConverter getInstance()
//		{
//			if (_instance == null)
//			{
//				_instance = new FrenchConverter();
//			}
//			return _instance;
//		}
//
//		private FrenchConverter() 
//		{  
//			
//			phones = getDict("FrenchDict.txt");
//
//			//Note: this does not handle proper noun pronunciation exceptions
//			
//			
//		}
//
//
//
//		public String convert(String s)
//		{
//			s = " " + s.toLowerCase() + " ";
//			s = s.replaceAll("[!`~@#$%^&()_:;\"\'<>,.?/0-9]", "");  //remove punctuation & numbers - needed to recognize last characters in words for some preprocessing
//			
//			// to save dictionary memory, where necessary, replace vowels with accent grave or circumflex with unaccented counterparts. These accents do not affect pronunciation.
//			s = s.replaceAll("à", "a");
//			s = s.replaceAll("ì", "i");
//			s = s.replaceAll("î", "i");
//			s = s.replaceAll("ò", "o");
//			s = s.replaceAll("ù", "u");
//			s = s.replaceAll("û", "u");
//			s = s.replaceAll("ÿ", "y");
//
//			//	preprocess special sequences of more than 3 characters here
//			s = s.replaceAll("auld", "o");
//			s = s.replaceAll("ault", "o");
//			s = s.replaceAll("oell", "wal");
//			s = s.replaceAll("emment", "amɑ̃"); 
//			s = s.replaceAll("ouill", "uJ"); 
//			s = s.replaceAll("euse ", "øz ");
//			s = s.replaceAll("eut ", "ø ");
//			s = s.replaceAll("oeufs ", "ø ");
//			s = s.replaceAll("oeu ", "ø ");
//			s = s.replaceAll("ueu ", "ø ");
//			
//			s = s.replaceAll("ail ", "AJ ");
//			s = s.replaceAll("eil ", "ɛJ ");
//			s = s.replaceAll("oeil ", "œJ ");
//			s = s.replaceAll("ueil ", "œJ ");
//			s = s.replaceAll("euil ", "œJ ");
//			s = s.replaceAll("oil ", "J ");
//			s = s.replaceAll("uil ", "J ");
//			s = s.replaceAll("oue", "uɛ");
//			
//			s = s.replaceAll("ill", "L");	 	// char L is placeholder for 'ill'
//			
//			//	pars of suffixes where t sounds like s
//			s = s.replaceAll("tion", "SJon");					
//			s = s.replaceAll("tien", "SJen");		
//			s = s.replaceAll("tiel", "SJel");		
//			s = s.replaceAll("tieux", "SJeux");	
//			s = s.replaceAll("tiable", "SJable");		
//			
//			// exceptions - monosyllabic words ending in 'es'
//			s = s.replaceAll(" les ", " lɛ ");
//			s = s.replaceAll(" mes ", " mɛ ");
//			s = s.replaceAll(" tes ", " tɛ ");
//			s = s.replaceAll(" des ", " dɛ ");
//			s = s.replaceAll(" ses ", " sɛ ");
//			s = s.replaceAll(" ces ", " sɛ ");
//			
//			
//			
//			s =  super.convert(s);
//			//postprocess special sequnces here
//			s = s.replaceAll("iɛ", "i"); //could not eliminate ɛ next to i before without eliminating ɛ where needed.
//			
//			return s;
//		}
//		
//		
//		
//		
//		/**
//		 * Print example transcriptions for french converter
//		 * @@throws IOException 
//		 */
//		 void test() throws IOException {
//			//PrintStream out = new PrintStream(System.out, true, "UTF-16");
//       PrintStream out = new PrintStream(System.out, true, "UTF-8");
//			out.println("TESTING FRENCH");
//
//			//	 Note: mistakes 'a' in  prira and trepa for 'ɑ' because these words are verbs. (terminal 'as' in verbs = 'a' while in non-verbs = ɑ
//			// This cannot be helped without storing all verbs in a lookup table. For statistical purposes only, 
//			// this could be fixed by changing a certain percentage of ending 'ɑ' to 'a'.
//			String s1 = "J'oublierai les douleurs passées. Non! Tu ne prieras pas! Qui nous lie nous liera juqu'au trépas.";
//			String c1 = "ʒublire lɛ dulœr pɑse nõ ty nə prira pɑ ki nu li nu lira ʒysko trepa";  
//			test(out, s1, c1);
//			String s2 = "Nous avons vu cirque de soleil dans le château.";
//			String c2 = "nu avõ vy sirkə də sɔlɛj dɑ̃ lə ʃɑto";
//			test(out, s2, c2);
//			String s3 = "Parlez vous français";
//			String c3 = "parle vu frɑ̃sɛ";
//			test(out, s3, c3);
//			String s4 = "émocion occasion  boulanger toujours ouest oui jeûne";
//			String c4 = "emosjõ  ɔkɑzjõ bulɑ̃ʒe tuʒur  uɛst ui ʒønə"; 
//			test(out, s4, c4);
//			String s5 = "Rends moi la vie. Où la fauve agonie des feuilles.";			// simplification: terminal es should be 'ə' for plural nouns and adjectives and verb endings but not  for monosyllables (such as 'les') where it should be 'ɛ'
//			String c5 = "rɑ̃ mua la vi u la fov agɔni dɛ fœjə"; 
//			test(out, s5, c5);
//			//		test vowels
//			String s6 = "si su sous pré ceux sot près soeur sort patte pâte ce"; 	// note: terminal ə is optional (almost silent in french)
//			String c6 = "si sy su pre sø so prɛ sœr sɔr patə pɑtə sə";  
//			test(out, s6, c6);
//			String s7 = "Foucault fille nuit jonc acclamer avoir vieux"; 
//			String c7 = "fuko fijə nui ʒõ aklame avuar vjø";      					// nuit = nui (nɥi), ɥ is allophone for u??
//			test(out, s7, c7);
//			
//			//		test nasal vowels
//			String s8 = "enfant ennoblir remmener examen"; 
//			String c8 = "ɑ̃fɑ̃ ɑ̃nɔblir rɑ̃məne ɛgzamɛ̃̃"; 		//  ə optional
//			test(out, s8, c8);
//			String s9 = "candeur chant flambeau serment temps bien chien vin timbre lynx thym"; 
//			String c9 = "kɑ̃dœr ʃɑ̃ flɑ̃bo sɛrmɑ̃ tɑ̃ bjɛ̃ ʃjɛ̃ vɛ̃ tɛ̃brə lɛ̃ks tɛ̃";  			// final ə optional
//			test(out, s9, c9);
//			String s10 = "bon fontaine rompre parfum"; 
//			String c10 = "bõ fõtɛnə rõprə parfœ̃";  								
//			test(out, s10, c10);
//			
//			//test consonants
//			String s11 = "bouche absence parc jonc comme cette garçon"; 
//			String c11 =  "buʃə apsɑ̃sə park ʒõ kɔmə sɛtə garsõ"; 						
//			test(out, s11, c11);
//			String s12 = "froid pied addition soif sang goûter rouge baguette"; 
//			String c12 = "frua pje adisjõ suaf sɑ̃ gute ruʒə bagɛtə";  
//			test(out, s12, c12);
//			
//			String s13 = "les mes tes ces"; 
//			String c13 = "lɛ mɛ tɛ sɛ";  
//			test(out, s13, c13);
//			String s14 ="ignoble beaucoup coquin fleur alors jasmine faux "; 
//			String c14 = "iɲɔblə boku kɔkɛ̃ flœr alɔr ʒasmɛ̃ fo ";  
//			test(out, s14, c14);
//			
//			//"œ̃"
//			
//		} 
//	}
//
//	
//	
//	
//	
//	
//	
//	
//	
//
//	
//	
//	
//
//	
//	
//	
//
//
//	/**
//	 * Text2IPAConverter for German
//	 * 
//	 * Note: use GermanConverter.getInstance() instead of a constructor.
//	 */
//	public static class GermanConverter extends LangConverter
//	{
//		private static GermanConverter _instance;
//
//		public static GermanConverter getInstance()
//		{
//			if (_instance == null)
//			{
//				_instance = new GermanConverter();
//			}
//			return _instance;
//		}
//
//		private GermanConverter() 
//		{  
//			phones = getDict("GermanDict.txt");
//			
//		}
//
//		public String convert(String s)
//		{
//			s = s.toLowerCase();
//			
//			//			preprocess special sequences of more than 3 characters here 
//			s = s.replaceAll("x", "ks");																			// must preprocess x to count as two consonants before evaluating precendent vowels
//			s = s.replaceAll("ucht", "Uxt");
//			s = s.replaceAll("ücht", "Yçt");
//			s = s.replaceAll("beein", "bəaen");				
//			s = s.replaceAll("geein", "gəaen");	
//			s = s.replaceAll("bee", "bəɛ");				
//			s = s.replaceAll("gee", "gəe");	
//			s = s.replaceAll("äuch", "äuç");
//			s = s.replaceAll("euch", "euç");
//			s = s.replaceAll("schen", "sçən");
//			s = s.replaceAll("tion", "tsjon");
//			s = s.replaceAll("tient", "tsjɛnt");
//			s = s.replaceAll("chst", "xst");
//			s = s.replaceAll("ichs", "içs");
//			s = s.replaceAll("chs", "xs");
//			return super.convert(s);
//		}
//		
//		/**
//		 * Print example transcriptions for german converter
//		 * @@throws IOException 
//		 */
//		 void test() throws IOException {
//			PrintStream out = new PrintStream(System.out, true, "UTF-16");
//			out.println("TESTING GERMAN");
//
//			String s1 = "geblieben ihnen seel nett eine Gelegenheit";
//			String c1 = "gəblibən inən seIl nɛt aenə gəlegənhaet";
//			test(out, s1, c1);
//			String s2 = "nächst trällern vetter";
//			String c2 = "nɛçst trɛlɐn fɛtər";
//			test(out, s2, c2);
//			String s3 = "lohn boot noch schuh Busch für Blüte";
//			String c3 = "lon bot nɔx ʃu bUʃ fyr blytə";
//			test(out, s3, c3);
//			String s4 = "müssen Hülle Glück böse gewöhnlich Hölle völlig";
//			String c4 = "mYsən hYlə glYk bøzə gəvønkIIç hœlə fœlIç";
//			test(out, s4, c4);
//			String s5 = "Saal wahr wallen allein zusammenfassen";
//			String c5 = "zal var valən alaen tsuzamənfasən";
//			test(out, s5, c5);
//			String s6 = "ich Sprache fluch Flüchtling gross Schlösser Fuß";
//			String c6 = "Iç ʃpraxə flux flYçtlIŋ gros ʃlœsər fus";
//			test(out, s6, c6);
//			String s7 = "vorbei inoffiziel Labsal";
//			String c7 = "forbae Inɔfitsjɛl lapzal";
//			test(out, s7, c7);
//			String s8 = "zerstören heilig hoffnungslos unruhig";
//			String c8 = "tsɛrʃtørən haelIç hɔfnUŋslos UnruIç";
//			test(out, s8, c8);
//			String s9 = "jetzt Majestät irre füllen Sonne Hölle";
//			String c9 = "jɛtst majɛstɛt Irə fYlən zɔnə hœlə";
//			test(out, s9, c9);
//			String s10 = "Bach doch auch Milch Häuschen lachst Reichs Bachs Herrlichster lehren ernst";
//			String c10 = "bax dɔx aox mIlç hɔøsçən laxst raeçs baxs hɛrlIçstər lerən ɛrnst";
//			test(out, s10, c10);
//			String s11 = "mir zittern leiden Monde Königs";
//			String c11 = "mir tsItərn laedən mɔndə kønIçs";
//			test(out, s11, c11);
//			String s12 = "sauber böse gipsen fast Lebensreise";
//			String c12 = "zaobər bøzə gIpsən fast lebənsraezə";
//			test(out, s12, c12);
//			String s13 = "rasch stellen Gespräch beste";
//			String c13 = "raʃ ʃtɛlən gəʃprɛç bɛstə";
//			test(out, s13, c13);
//			String s14 = "Hauch Hoheit lebhaft sehen Gnade knoten lang";
//			String c14 = "haox hohaet lephaft zeən gnadə knotən laŋ";
//			test(out, s14, c14);
//			String s15 = "Frankreich phrase quelle theater patient tschechisch muß";
//			String c15 = "fraŋkraeç frazə kvɛlə teatər patsjɛnt tʃɛçIʃ mUs";
//			test(out, s15, c15);
//			// note: all 'ər' or 'ɛr' could also be transcribed as'ɐ'
//		} 
//
//	}
//	
//	
//	
//	
//	
//	
//	
//	
//
//	
//	
//	
//	/**
//	 * Text2IPAConverter for Italian
//	 * 
//	 * Note: use ItalianConverter.getInstance() instead of a constructor.
//	 */
//	public static class ItalianConverter extends LangConverter
//	{
//		private static ItalianConverter _instance;
//
//		public static ItalianConverter getInstance()
//		{
//			if (_instance == null)
//			{
//				_instance = new ItalianConverter();
//			}
//			return _instance;
//		}
//
//		private ItalianConverter() 
//		{  			
//			phones = getDict("ItalianDict.txt");
//		}
//
//		public String convert(String s)
//		{
//			s = s.toLowerCase();
//			//			preprocess special sequences of more than 3 characters here 
//			s = s.replaceAll("gli", "ʎi");																																// if i is followed by vowel, remove it later
//			//	  		preprocess doubles which should be treated as singles
//			s = s.replaceAll("bb", "b");
//			s = s.replaceAll("dd", "d");
//			s = s.replaceAll("ff", "f");
//			s = s.replaceAll("gg", "g");
//			s = s.replaceAll("ll", "l");
//			s = s.replaceAll("mm", "m");
//			s = s.replaceAll("nn", "n");
//			s = s.replaceAll("pp", "p");
//			s = s.replaceAll("qq", "q");
//			s = s.replaceAll("ss", "s");
//			s = s.replaceAll("tt", "t");
//			s = s.replaceAll("vv", "v");
//			s = s.replaceAll("zz", "z");
//			return super.convert(s);		
//		}
//		
//		/**
//		 * Print example transcriptions for italian converter
//		 * @@throws IOException 
//		 */
//		 void test() throws IOException {
//			PrintStream out = new PrintStream(System.out, true, "UTF-16");
//			out.println("TESTING ITALIAN");
//			String s1 = "sorella, squarcio, falso, assistere, tesoro, sgelo, fantasma";
//			String c1 = "soɾɛla skuarʧo falso asisteɾe tezɔɾo zʤɛlo fantazma";
//			test(out, s1, c1);
//			String s2 = "crudo puro bene bere posta posto";
//			String c2 = "krudo puɾo bɛne beɾe pɔsta posto";
//			test(out, s2, c2);
//			String s3 = "figlio vogliono bagno possono fiore piano mestiere chiuso Maria Mario Lucia tragedia";
//			String c3 = "fiʎo vɔʎono baɲo pɔsono fjoɾe pjano mestjɛɾe kjuzo maɾia maɾjo luʧia traʤɛdja";
//			test(out, s3, c3);
//			String s4 = "idea teatro boa soave eroe poeta";
//			String c4 = "idɛa teatro bɔa soave eɾɔe poɛta";
//			test(out, s4, c4);
//			String s5 = "pesce angoscia";
//			String c5 = "peʃe aŋgɔʃa";
//			test(out, s5, c5);
//		} 
//
//	}
//
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	/**
//	 * Text2IPAConverter for Portuguese
//	 * 
//	 * Note: use PortugueseConverter.getInstance() instead of a constructor.
//	 */
//	public static class PortugueseConverter extends LangConverter
//	{
//		private static PortugueseConverter _instance;
//
//		public static PortugueseConverter getInstance()
//		{
//			if (_instance == null)
//			{
//				_instance = new PortugueseConverter();
//			}
//			return _instance;
//		}
//
//		private PortugueseConverter() 
//		{  
//			phones = getDict("PortugueseDict.txt");
//		}
//
//		public String convert(String s)
//		{
//			s = s.toLowerCase();
//			return super.convert(s);
//		}
//		
//		/**
//		 * Print example transcriptions for Portuguese converter
//		 * @@throws IOException 
//		 */
//		 void test() throws IOException {
//			PrintStream out = new PrintStream(System.out, true, "UTF-16");
//			out.println("TESTING PORTUGUESE");
//			String s1 = "basquetebol rocha faca gelo guerra história jovens";
//			String c1 = "baʃketebɔl roʃɐ fakɐ ʒelu guerɐ iʃtɔɾiɐ ʒovẽs";
//			test(out, s1, c1);
//			String s2 = "milhões tamanho quente qualidade rir carro";
//			String c2 = "miʎõeʃ tamaɲu kẽtɯ kualidad riɾ karu";
//			test(out, s2, c2);
//			String s3 = "sete pássaro mesa gatos doisnavios";
//			String c3 = "setɯ pasaɾu mezɐ gatoʃ doiʒnavioʃ";
//			test(out, s3, c3);
//			String s4 = "peixe próximo exame táxi fazer insensatez cabeça";
//			String c4 = "pɐiʃɯ prɔsimu ezame taksi fazer ĩsẽsatez kabesɐ";
//			test(out, s4, c4);
//			String s5 = "O vento norte e o sol descutiam qual dos dois era o mais forte.";
//			String c5 = "u vẽtu nɔɾtɯ i u sɔl deʃkutiɐ̃u kual duʒ doiz ɛɾɔ u maiʃ fɔɾtɯ";
//			test(out, s5, c5);
//			String s6 = "quando sucedeu passar um viajante envolto numa capa.";
//			String c6 = "kuɐ̃du sucɯdeu pɐsaɾ ũ viɐjɐ̃tɯ ɯ̃volt numɐ capɐ";
//			test(out, s6, c6);
//			String s7 = "Ao vê-lo põem-se de acordo em como aquele que primeiro conseguisse obrigar o viajante a tirar a capa seria considerado.";
//			String c7 = "au velu põiɐ̃is diɐ koɾdu ɐ̃i komu ɐkel kɯ pɾimɐiɾu kõsgis ɔbɾigaɾ u viɐʒɐ̃tɯ ɐ tiɾaɾ ɐ kapɐ sɾiɐ kõsidɯɾadu";
//			test(out, s7, c7);
//			
//			// oral vowels
//			String s8 = "vi vê sé vá só sou mudo pagar pegar";
//			String c8 = "vi ve sɛ va sɔ so mudu pɐgaɾ pɯgaɾ";
//			test(out, s8, c8);
//			//	nasal vowels
//			String s9 = "vim entro antro som mundo";
//			String c9 = "vĩ ẽtɾu ɐ̃tɾu sõ mũdu";
//			test(out, s9, c9);
//			//	diphthongs
//			String s10 = "anéis sai sei mói moita anuis viu meu véu mau cem anões muita mão cinqüenta";
//			String c10 = "ɐnɛiʃ sai sɐi mɔi moitɐ ɐnuiʃ viu meu vɛu mau sɐ̃i ɐnõiʃ mũitɐ mãu sĩkuẽtɐ";
//			test(out, s10, c10);
//		} 
//	}
//	
//	
//	
//  /**
//   * Text2IPAConverter for Latin
//   * 
//   * Note: use LatinConverter.getInstance() instead of a constructor.
//   */
//  public static class LatinConverter extends LangConverter
//  {
//    private static LatinConverter _instance;
//
//    public static LatinConverter getInstance()
//    {
//      if (_instance == null)
//      {
//        _instance = new LatinConverter();
//      }
//      return _instance;
//    }
//
//    private LatinConverter() 
//    {  
//      phones = getDict("LatinDict.txt");
//    }
//
//    public String convert(String s)
//    {
//      s = s.toLowerCase();
//      return super.convert(s);
//    }
//    
//    /**
//     * Print example transcriptions for Portuguese converter
//     * @@throws IOException 
//     */
//     void test() throws IOException {
//    } 
//  }
//	
//	
//	/**
//	 * Text2IPAConverter for Spanish
//	 * 
//	 * Note: use SpanishConverter.getInstance() instead of a constructor.
//	 */
//	public static class SpanishConverter extends LangConverter
//	{
//		private static SpanishConverter _instance;
//
//		public static SpanishConverter getInstance()
//		{
//			if (_instance == null)
//			{
//				_instance = new SpanishConverter();
//			}
//			return _instance;
//		}
//
//		private SpanishConverter() 
//		{  
//			phones = getDict("SpanishDict.txt");
//		}
//
//		public String convert(String s)
//		{
//			s = s.toLowerCase();
//			return super.convert(s);
//		}
//		
//		/**
//		 * Print example transcriptions for spanish converter
//		 * @@throws IOException 
//		 */
//		 void test() throws IOException {
//			PrintStream out = new PrintStream(System.out, true, "UTF-16");
//			out.println("TESTING SPANISH");
//			String s1 = "Quiero comer algunas tortillas con chorizo, queso, y guayaba.";
//			String c1 = "kyeɾo komeɾ algunas toɾtiʎas kon ʧoɾiso keso i gwaʝaba";
//			test(out, s1, c1);
//			String s2 = "La corrupción del gobierno en el oeste resultó en una guerra universal; tiene mucha vergüenza.";
//			String c2 = "la korupsyon del gobyeɾno en el oeste ɾesulto en una gwera unibeɾsal tyene muʧa beɾguensa";
//			test(out, s2, c2);
//			String s3 = "Quién es el ingeniero escondido con la chaqueta anaranjada? ";
//			String c3 = "kyen es el iŋxenyeɾo eskondido kon la ʧaketa anaɾanxada";
//			test(out, s3, c3);
//			String s4 = "El reloj ha estado lleno de agua desde que fui mañana a descansar en la piscina de color violeta.";
//			String c4 = "el ɾelox a estado ʎeno de agwa desde ke fui maɲana a deskansaɾ en la pisina de koloɾ byoleta";
//			test(out, s4, c4);
//			String s5 = "Ayúdame en inglés. Mostrame la acción correcta de la guía para cuando estás ausente.";
//			String c5 = "aʝudame en iŋgles mostɾame la aksyon korekta de la guia paɾa kwando estas ausente";
//			test(out, s5, c5);
//		} 
//	}
//	
//	
//	
//	
//	


}







