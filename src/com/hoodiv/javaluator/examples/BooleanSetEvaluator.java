package com.hoodiv.javaluator.examples;

import com.hoodiv.javaluator.BracketPair;
import com.hoodiv.javaluator.Parameters;
import com.hoodiv.javaluator.Operator;
import com.hoodiv.javaluator.AbstractEvaluator;
import com.hoodiv.javaluator.Constant;
import com.hoodiv.javaluator.EvaluationContext;
import com.hoodiv.javaluator.Token;
import java.util.BitSet;
import java.util.Iterator;


/** An example of how to implement an evaluator from scratch, working on something more complex 
 * than doubles.
 * <br>This evaluator computes expressions that use boolean sets.
 * <br>A boolean set is a vector of booleans. A true is represented by a one, a false, by a zero.
 * For instance "01" means {false, true}
 * <br>The evaluator uses the BitSet java class to represent these vectors.
 * <br>It supports the logical OR (+), AND (*) and NEGATE (-) operators.
 */
public class BooleanSetEvaluator extends AbstractEvaluator<BitSet> {
	/** The negate unary operator.*/
	public final static Operator NEGATE = new Operator("-", 1, Operator.Associativity.RIGHT, 3);
	/** The logical AND operator.*/
	private static final Operator AND = new Operator("*", 2, Operator.Associativity.LEFT, 2);
	/** The logical OR operator.*/
	public final static Operator OR = new Operator("+", 2, Operator.Associativity.LEFT, 1);
	/** The true constant. */
	public final static Constant TRUE = new Constant("true");
	/** The false constant. */
	public final static Constant FALSE = new Constant("false");
	
	public static class BitSetEvaluationContext implements EvaluationContext {
		/** The bitset's length. */
		private int bitSetLength;
		public BitSetEvaluationContext(int bitSetLength) {
			super();
			this.bitSetLength = bitSetLength;
		}
		public int getBitSetLength() {
			return bitSetLength;
		}
	}

	/** The evaluator's parameters.*/
	private static final Parameters PARAMETERS;
	static {
		// Create the evaluator's parameters
		PARAMETERS = new Parameters();
		// Add the supported operators
		PARAMETERS.add(AND);
		PARAMETERS.add(OR);
		PARAMETERS.add(NEGATE);
		PARAMETERS.add(TRUE);
		PARAMETERS.add(FALSE);
		// Add the default parenthesis pair
		PARAMETERS.addExpressionBracket(BracketPair.PARENTHESES);
	}

	/** Constructor.
	 */
	public BooleanSetEvaluator() {
		super(PARAMETERS);
	}

	@Override
	protected BitSet toValue(Token literalTok, EvaluationContext evaluationContext) {
                String literal = literalTok.getString();
		int length =((BitSetEvaluationContext)evaluationContext).getBitSetLength(); 
		// A literal is composed of 0 and 1 characters. If not, it is an illegal argument
		if (literal.length()!=length) throw evaluationContext.getError(literal+" must have a length of "+length,literalTok);
		BitSet result = new BitSet(length);
		for (int i = 0; i < length; i++) {
			if (literal.charAt(i)=='1') {
				result.set(i);
			} else if (literal.charAt(i)!='0') {
				throw evaluationContext.getError(literal+" contains the wrong character "+literal.charAt(i),literalTok);
			}
		}
		return result;
	}

	@Override
	protected BitSet evaluate(Operator operator, Iterator<BitSet> operands, EvaluationContext evaluationContext) {
		// Implementation of supported operators
		BitSet o1 = operands.next();
		if (operator == NEGATE) {
			int length = ((BitSetEvaluationContext)evaluationContext).getBitSetLength();
			o1.flip(0, length);
		} else {
			BitSet o2 = operands.next();
			if (operator == OR) {
				o1.or(o2);
			} else if (operator == AND) {
				o1.and(o2);
			} else {
				o1 = super.evaluate(operator, operands, evaluationContext);
			}
		}
		return o1;
	}
	
	@Override
	protected BitSet evaluate(Constant constant, EvaluationContext evaluationContext) {
		// Implementation of supported constants
		int length = ((BitSetEvaluationContext)evaluationContext).getBitSetLength();
		BitSet result;
		if (constant==FALSE) {
			result = new BitSet(length);
		} else if (constant==TRUE) {
			result = new BitSet(length);
			result.flip(0, length);
		} else {
			result = super.evaluate(constant, evaluationContext);
		}
		return result;
	}
	
	/** A simple program using this evaluator. */
	public static void main(String[] args) {
		BooleanSetEvaluator evaluator = new BooleanSetEvaluator();
		BitSetEvaluationContext context = new BitSetEvaluationContext(4);
		doIt(evaluator, "0011 * ( 1010", context);
		doIt(evaluator, "true * 1100", context);
		doIt(evaluator, "-false", context);
	}

	private static void doIt(BooleanSetEvaluator evaluator, String expression, BitSetEvaluationContext context) {
		// Evaluate the expression
		BitSet result = evaluator.evaluate(expression, context);
		// Display the result
		System.out.println (expression+" = "+toBinaryString(result));
	}

	/** Converts a bitSet to its binary representation.
	 * @param bitSet A bit set
	 * @return a String composed of 0 and 1. 1 indicates that the corresponding bit is set.
	 */
	public static String toBinaryString(BitSet bitSet) {
		// Converts the result to a String
		StringBuilder builder = new StringBuilder(bitSet.length());
		for (int i = 0; i < bitSet.length(); i++) {
			builder.append(bitSet.get(i)?'1':'0');
		}
		String res = builder.toString();
		return res;
	}
}
