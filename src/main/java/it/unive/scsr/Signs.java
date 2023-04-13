package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class Signs
		 instances of this class are lattice elements such that
		 - their state (fields) hold the information contained into a single
		 element
		 - they provide logic for the evaluation of expressions
		extends BaseNonRelationalValueDomain
				 java requires this type parameter to have this class
				 as type in fieldsmethods
				Signs {

	 as this is a finite lattice, we can optimize by having constant elements
	 for each of them
	private static final Signs BOTTOM = new Signs(-10);
	private static final Signs NEGATIVE = new Signs(-1);
	private static final Signs ZERO = new Signs(0);
	private static final Signs POSITIVE = new Signs(1);
	private static final Signs TOP = new Signs(10);

	 this is just needed to distinguish the elements
	private final int sign;

	public Signs() {
		this(10);
	}

	public Signs(int sign) {
		this.sign = sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime  result + sign;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signs other = (Signs) obj;
		if (sign != other.sign)
			return false;
		return true;
	}

	@Override
	public Signs top() {
		 the top element of the lattice
		 if this method does not return a constant value,
		 you must override the isTop() method!
		return TOP;
	}

	@Override
	public Signs bottom() {
		 the bottom element of the lattice
		 if this method does not return a constant value,
		 you must override the isBottom() method!
		return BOTTOM;
	}

	@Override
	public Signs lubAux(Signs other) throws SemanticException {
		 this and other are always incomparable when we reach here
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(Signs other) throws SemanticException {
		 this and other are always incomparable when we reach here
		return false;
	}

	@Override
	public DomainRepresentation representation() {
		if (this == TOP)
			return Lattice.topRepresentation();
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		if (this == POSITIVE)
			return new StringRepresentation(+);
		if (this == NEGATIVE)
			return new StringRepresentation(-);
		return new StringRepresentation(0);
	}
	
	 logic for evaluating expressions below
	
	@Override
	public Signs evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			int v = (Integer) constant.getValue();
			if (v  0)
				return POSITIVE;
			else if (v == 0)
				return ZERO;
			else
				return NEGATIVE;
		}
		return top();
	}

	private Signs negate() {
		if (this == NEGATIVE)
			return POSITIVE;
		else if (this == POSITIVE)
			return NEGATIVE;
		else
			return this;
	}

	@Override
	public Signs evalUnaryExpression(UnaryOperator operator, Signs arg, ProgramPoint pp) throws SemanticException {
		if (operator instanceof NumericNegation)
			return arg.negate();

		return TOP;
	}

	@Override
	public Signs evalBinaryExpression(BinaryOperator operator, Signs left, Signs right, ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof AdditionOperator) {
			if (left == NEGATIVE) {
				if (right == ZERO  right == NEGATIVE)
					return left;
				else
					return TOP;
			} else if (left == POSITIVE) {
				if (right == ZERO  right == POSITIVE)
					return left;
				else
					return TOP;
			} else if (left == ZERO) {
				return right;
			} else
				return TOP;
		} else if (operator instanceof SubtractionOperator) {
			if (left == NEGATIVE) {
				if (right == ZERO  right == POSITIVE)
					return left;
				else
					return TOP;
			} else if (left == POSITIVE) {
				if (right == ZERO  right == NEGATIVE)
					return left;
				else
					return TOP;
			} else if (left == ZERO) {
				return right;
			} else
				return TOP;
		} else if (operator instanceof MultiplicationOperator) {
			if (left == NEGATIVE) {
				return right.negate();
			} else if (left == POSITIVE) {
				return right;
			} else if (left == ZERO) {
				return ZERO;
			} else
				return TOP;
		} else if (operator instanceof DivisionOperator) {
			if (right == ZERO)
				return BOTTOM;

			if (left == NEGATIVE) {
				return right.negate();
			} else if (left == POSITIVE) {
				return right;
			} else if (left == ZERO) {
				return ZERO;
			} else
				return TOP;
		}

		return TOP;
	}
}