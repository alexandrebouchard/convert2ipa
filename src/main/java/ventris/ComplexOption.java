package ventris;

/**
 * Similar to Option, ComplexOption encapsulates an additional relationship 
 * and optional character parameter. A ComplexOption is satisfied only if 
 * BOTH relation is satisfied (with respect to char c) 
 * AND relation2 is satisfied (with respect to char c2)
 */
public class ComplexOption //extends Option {
{
//	int relation2;
//	char c2;
//
//	ComplexOption (int relation, char c, int relation2, char c2, String result, boolean removeNext) {
//		this.relation = relation;
//		this.c = c;
//		this.relation2 = relation2;
//		this.c2 = c2;
//		this.result = result;
//		this.removeNext = removeNext;
//	}
//
//	ComplexOption (int relation, char c, int relation2, String result, boolean removeNext) {
//		this.relation = relation;
//		this.c = c;
//		this.relation2 = relation2;
//		this.result = result;
//		this.removeNext = removeNext;
//	}
//
//	ComplexOption (int relation, int relation2, char c2, String result, boolean removeNext) {
//		this.relation = relation;
//		this.relation2 = relation2;
//		this.c2 = c2;
//		this.result = result;
//		this.removeNext = removeNext;
//	}
//
//	ComplexOption (int relation, int relation2, String result, boolean removeNext) {
//		this.relation = relation;
//		this.relation2 = relation2;
//		this.result = result;
//		this.removeNext = removeNext;
//	}
//
//	/**
//	 * matches returns true if the given prev, next, and nnext characters satisfy 
//	 * conditions relation with respect to this.c and relation2 with respect to this.c2
//	 */
//	boolean matches(char prev, char next, char nnext)  {
//		return (super.matches(this.relation, this.c, prev, next, nnext) &&
//				super.matches(this.relation2, this.c2, prev, next, nnext));
//	}	
}

