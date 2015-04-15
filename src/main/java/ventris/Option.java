package ventris;


import java.io.PrintStream;
import java.util.HashMap;

/**
 * Container class to easily store a condition associated with a 
 * particular phoneme translation
 */
public class Option { 
//	// These relationships do not require a character argument
//	static final int DEFAULT = 0;
//	static final int FIRST = 1;
//	static final int NOT_FIRST = 2;
//	static final int LAST = 3;
//	static final int SECOND_TO_LAST = 4;
//	static final int FOLLOWED_BY_VOWEL = 5;
//	static final int FOLLOWED_BY_A = 6;
//	static final int FOLLOWED_BY_E = 7;
//	static final int FOLLOWED_BY_I = 8;
//	static final int FOLLOWED_BY_O = 9;
//	static final int FOLLOWED_BY_U = 10;
//	static final int FOLLOWED_BY_UNVOICEDC = 11;
//	static final int FOLLOWED_BY_CONSONANT = 12;
//	static final int FFOLLOWED_BY_CONSONANT = 13;					// character after next is a consonant
//	static final int FFOLLOWED_BY_VOWEL = 14;							// character after next is a vowel
//	static final int PRECEDED_BY_VOWEL = 15;
//	static final int PRECEDED_BY_A = 16;
//	static final int PRECEDED_BY_E = 17;
//	static final int PRECEDED_BY_I = 18;
//	static final int PRECEDED_BY_O = 19;
//	static final int PRECEDED_BY_U = 20;
//	static final int PRECEDED_BY_CONSONANT = 21;
//	static final int PRECEDED_BY_UNVOICEDC = 22;
//
//	// These relationships take into account a character argument
//	static final int FOLLOWED_BY = -1;
//	static final int PRECEDED_BY = -2;
//	static final int PRECEDED_OR_FOLLOWED_BY = -3;
//	static final int FFOLLOWED_BY = -4;										// character after next is specified char
//	
//	static final HashMap relationsMap = new HashMap<String, Integer>(30);
//       static {
//    	   relationsMap.put("DEFAULT", new Integer(DEFAULT));
//    	   relationsMap.put("FIRST", new Integer(FIRST));
//    	   relationsMap.put("NOT_FIRST", new Integer(NOT_FIRST));
//    	   relationsMap.put("LAST", new Integer(LAST));
//    	   relationsMap.put("SECOND_TO_LAST", new Integer(SECOND_TO_LAST));
//    	   relationsMap.put("FOLLOWED_BY_VOWEL", new Integer(FOLLOWED_BY_VOWEL));
//    	   relationsMap.put("FOLLOWED_BY_A", new Integer(FOLLOWED_BY_A));
//    	   relationsMap.put("FOLLOWED_BY_E", new Integer(FOLLOWED_BY_E));
//    	   relationsMap.put("FOLLOWED_BY_I", new Integer(FOLLOWED_BY_I));
//    	   relationsMap.put("FOLLOWED_BY_O", new Integer(FOLLOWED_BY_O));
//    	   relationsMap.put("FOLLOWED_BY_U", new Integer(FOLLOWED_BY_U));
//    	   relationsMap.put("FOLLOWED_BY_UNVOICEDC", new Integer(FOLLOWED_BY_UNVOICEDC));
//    	   relationsMap.put("FOLLOWED_BY_CONSONANT", new Integer(FOLLOWED_BY_CONSONANT));
//    	   relationsMap.put("FFOLLOWED_BY_CONSONANT", new Integer(FFOLLOWED_BY_CONSONANT));
//    	   relationsMap.put("FFOLLOWED_BY_VOWEL", new Integer(FFOLLOWED_BY_VOWEL));
//    	   relationsMap.put("PRECEDED_BY_VOWEL", new Integer(PRECEDED_BY_VOWEL));
//    	   relationsMap.put("PRECEDED_BY_A", new Integer(PRECEDED_BY_A));
//    	   relationsMap.put("PRECEDED_BY_E", new Integer(PRECEDED_BY_E));
//    	   relationsMap.put("PRECEDED_BY_I", new Integer(PRECEDED_BY_I));
//    	   relationsMap.put("PRECEDED_BY_O", new Integer(PRECEDED_BY_O));
//    	   relationsMap.put("PRECEDED_BY_U", new Integer(PRECEDED_BY_U));
//    	   relationsMap.put("PRECEDED_BY_CONSONANT", new Integer(PRECEDED_BY_CONSONANT));
//    	   relationsMap.put("PRECEDED_BY_UNVOICEDC", new Integer(PRECEDED_BY_UNVOICEDC));
//    	   relationsMap.put("FOLLOWED_BY", new Integer(FOLLOWED_BY));
//    	   relationsMap.put("PRECEDED_BY", new Integer(PRECEDED_BY));
//    	   relationsMap.put("PRECEDED_OR_FOLLOWED_BY", new Integer(PRECEDED_OR_FOLLOWED_BY));
//    	   relationsMap.put("FFOLLOWED_BY", new Integer(FFOLLOWED_BY));
//       }
//	
//	
//	
//	int relation;
//	char c = '\0';
//	String result;
//	boolean removeNext;
//
//	Option() {}
//
//	/**
//	 * Construct an Option object which encapsulates a relationship, an optional 
//	 * character with which the relationship is with respect to, a string 
//	 * representing the phoneme to map to if the relationship holds true, and
//	 * an option for transcribing the next character with the current character as one
//	 * sound.
//	 * 
//	 * @param relation: A predicate for describing if this relationship is true
//	 * @param c: A character for those relationships which require one
//	 * @param result: The correct transcription string if this relationship is true
//	 * @param removeNext: True if this transcription is for both the current 
//	 * 		character and the next character (false otherwise)
//	 */
//	Option (int relation, char c, String result, boolean removeNext) {
//		this.relation = relation;
//		this.c = c;
//		this.result = result;
//		this.removeNext = removeNext;
//	}	
//	/**
//	 * Same as 4 parameter constructor except used for relationships where
//	 * a character parameter is not needed.
//	 */
//	Option (int relation, String result, boolean removeNext) {
//		this.relation = relation;
//		this.result = result;
//		this.removeNext = removeNext;
//	}	
//	
//	/**
//	 * matches returns true if the given prev and next characters satisfy 
//	 * condition this.relation with respect to this.c
//	 
//	boolean matches(char prev, char next) {
//		return matches(this.relation, this.c, prev, next, '\0');
//	}
//	*/
//	
//	
//	/**
//	 * matches returns true if the given prev and next characters satisfy 
//	 * condition this.relation with respect to this.c
//	 */
//	boolean matches(char prev, char next, char nnext) {
//		return matches(this.relation, this.c, prev, next, nnext);
//	}
//	
//	/**
//	 * matches returns true if the given prev and next characters satisfy 
//	 * the given relation with respect to the character c
//	 */
//	static boolean matches(int relation, char c, char prev, char next, char nnext) {
//		switch (relation) {
//		case FIRST:
//			return prev == '\0';
//		case NOT_FIRST:
//			return prev != '\0';
//		case LAST:
//			return next == '\0';
//		case SECOND_TO_LAST:
//			return nnext ==  '\0';
//		case FOLLOWED_BY:
//			return next == c;
//		case PRECEDED_BY:
//			return prev == c;
//		case PRECEDED_OR_FOLLOWED_BY:
//			return (prev == c || next == c);
//		case PRECEDED_BY_A:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_A:
//			return (next == 'a' ||
//					next == 'á' ||
//					next == 'à' ||
//					next == 'â' ||
//					next == 'ä');
//		case PRECEDED_BY_E:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_E:
//			return (next == 'e' ||
//					next == 'é' ||
//					next == 'è' ||
//					next == 'ê' || 
//					next == 'ë');
//		case PRECEDED_BY_I:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_I:
//			return (next == 'i' ||
//					next == 'í' ||
//					next == 'ì' ||
//					next == 'î' ||
//					next == 'ï' ||
//					next == 'Ï');
//		case PRECEDED_BY_O:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_O:
//			return (next == 'o' ||
//					next == 'ó' ||
//					next == 'ò' ||
//					next == 'ô' ||
//					next == 'ö');
//		case PRECEDED_BY_U:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_U:
//			return (next == 'u' ||
//					next == 'ú' ||
//					next == 'ù' ||
//					next == 'û' ||
//					next == 'ü');
//		case PRECEDED_BY_VOWEL:
//			next = prev;   			//drop through to next case
//		case FOLLOWED_BY_VOWEL:
//			return (next == 'a' ||
//					next == 'á' ||
//					next == 'à' ||
//					next == 'â' ||
//					next == 'ä' ||
//					next == 'e' ||
//					next == 'é' ||
//					next == 'è' ||
//					next == 'ê' || 
//					next == 'ë' ||
//					next == 'i' ||
//					next == 'í' ||
//					next == 'ì' ||
//					next == 'î' ||
//					next == 'ï' ||
//					next == 'o' ||
//					next == 'ó' ||
//					next == 'ò' ||
//					next == 'ô' ||
//					next == 'ö' ||
//					next == 'u' ||
//					next == 'ú' ||
//					next == 'ù' ||
//					next == 'û' ||
//					next == 'ü' ||
//					next == 'ə' ||
//					next == 'ɛ' ||
//					next == 'U');
//		case PRECEDED_BY_UNVOICEDC:
//			return (prev == 'c' ||
//					prev == 'f' ||
//					prev == 'h' ||
//					prev == 'k' ||
//					prev == 'p' ||
//					prev == 'q' ||
//					prev == 's' ||
//					prev == 't');
//		case FOLLOWED_BY_UNVOICEDC:
//			return (next == 'c' ||
//					next == 'f' ||
//					next == 'h' ||
//					next == 'k' ||
//					next == 'p' ||
//					next == 'q' ||
//					next == 's' ||
//					next == 't');
//		case PRECEDED_BY_CONSONANT:
//			return prev != '\0' && ! matches(PRECEDED_BY_VOWEL, '\0', prev, '\0', '\0');
//		case FOLLOWED_BY_CONSONANT:
//			return next != '\0' && ! matches(FOLLOWED_BY_VOWEL, '\0', '\0', next, '\0');
//		case FFOLLOWED_BY_VOWEL:
//			return nnext != '\0' && matches(FOLLOWED_BY_VOWEL, '\0', '\0', nnext, '\0');
//		case FFOLLOWED_BY_CONSONANT:
//			return nnext != '\0' && ! matches(FOLLOWED_BY_VOWEL, '\0', '\0', nnext, '\0');			
//		case FFOLLOWED_BY:
//			return matches(FOLLOWED_BY, c, '\0', nnext, '\0');
//		default:
//			return true;
//		}
//	}
}

