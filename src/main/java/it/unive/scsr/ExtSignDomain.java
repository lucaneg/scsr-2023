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
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

	private static final ExtSignDomain BOTTOM = new ExtSignDomain(-10);
	private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-5);
	private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
	private static final ExtSignDomain ZERO = new ExtSignDomain(0);
	private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
	private static final ExtSignDomain POSITIVE = new ExtSignDomain(1);
	private static final ExtSignDomain TOP = new ExtSignDomain(10);

	private final int sign;

	public ExtSignDomain() {
		this(10);
	}

	public ExtSignDomain(int sign) {
		this.sign = sign;
	}

	@Override
	public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
		if (sign == ZERO.sign) {
			if (other.sign == POSITIVE.sign)
				return ZERO_OR_POSITIVE;
			if (other.sign == NEGATIVE.sign)
				return ZERO_OR_NEGATIVE;
		}
		if (other.sign == ZERO.sign) {
			if (sign == POSITIVE.sign)
				return ZERO_OR_POSITIVE;
			if (sign == NEGATIVE.sign)
				return ZERO_OR_NEGATIVE;
		}
		return TOP;
	}

	@Override
	public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		if (this == ZERO)
			return other == ZERO || other == ZERO_OR_NEGATIVE || other == ZERO_OR_POSITIVE;
		if (this == POSITIVE)
			return other == ZERO_OR_POSITIVE;
		if (this == NEGATIVE)
			return other == ZERO_OR_NEGATIVE;
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass())
			return false;
		if (this == obj)
			return true;
		ExtSignDomain other = (ExtSignDomain) obj;
		return sign == other.sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + sign;
		return result;
	}

	@Override
	public ExtSignDomain top() {
		return TOP;
	}

	@Override
	public ExtSignDomain bottom() {
		return BOTTOM;
	}

	// IMPLEMENTATION NOTE:
	// you can follow the same logic of Signs to implement representation().
	// note that this is not mandatory: you can have any other logic instead
	// of constant fields, and you can change the logic in representation()
	// accordingly. the only constraint is that the strings used to represent
	// the elements stay the same (0, +, -, 0+, 0-)

	@Override
	public DomainRepresentation representation() {
		if (this == TOP)
			return Lattice.topRepresentation();
		if (this == BOTTOM)
			return Lattice.bottomRepresentation();
		if (this == POSITIVE)
			return new StringRepresentation("+");
		if (this == NEGATIVE)
			return new StringRepresentation("-");
		if (this == ZERO)
			return new StringRepresentation("0");
		if (this == ZERO_OR_POSITIVE)
			return new StringRepresentation("0+");
		return new StringRepresentation("0-");
	}

	private ExtSignDomain negate() {
		if (this == NEGATIVE)
			return POSITIVE;
		if (this == POSITIVE)
			return NEGATIVE;
		if (this == ZERO_OR_NEGATIVE)
			return ZERO_OR_POSITIVE;
		if (this == ZERO_OR_POSITIVE)
			return ZERO_OR_NEGATIVE;
		return this;
	}

	@Override
	public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		if (constant.getValue() instanceof Integer) {
			int v = (Integer) constant.getValue();
			if (v > 0) {
				return POSITIVE;
			}
			if (v == 0) {
				return ZERO;
			}
			return NEGATIVE;
		}
		return top();
	}

	@Override
	public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp)
			throws SemanticException {
		if (operator instanceof NumericNegation)
			return arg.negate();
		return TOP;
	}

	private ExtSignDomain evalAddition(ExtSignDomain left, ExtSignDomain right) {
		if (left == NEGATIVE) {
			if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
				return left;
		}
		if (left == POSITIVE) {
			if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
				return left;
		}
		if (left == ZERO_OR_NEGATIVE) {
			if (right == ZERO || right == ZERO_OR_NEGATIVE)
				return left;
			if (right == NEGATIVE)
				return right;
		}
		if (left == ZERO_OR_POSITIVE) {
			if (right == ZERO || right == ZERO_OR_POSITIVE)
				return left;
			if (right == POSITIVE)
				return right;
		}
		if (left == ZERO) {
			if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE || right == ZERO_OR_NEGATIVE
					|| right == NEGATIVE)
				return right;
		}
		return TOP;
	}

	private ExtSignDomain evalSubtraction(ExtSignDomain left, ExtSignDomain right) {
		return evalAddition(left, right.negate());
	}

	private ExtSignDomain evalMultiplication(ExtSignDomain left, ExtSignDomain right) {
		if (left == ZERO || right == ZERO)
			return ZERO;
		if (left == NEGATIVE)
			return right.negate();
		if (left == POSITIVE) {
			return right;
		}
		if (left == ZERO_OR_NEGATIVE || left == ZERO_OR_POSITIVE) {
			if (right == POSITIVE)
				return left;
			if (right == NEGATIVE)
				return left.negate();
			if (right == ZERO_OR_POSITIVE)
				return left;
			if (right == ZERO_OR_NEGATIVE)
				return left.negate();
		}
		return TOP;
	}

	private ExtSignDomain evalDivision(ExtSignDomain left, ExtSignDomain right) {
		if (right == ZERO || right == ZERO_OR_NEGATIVE || right == ZERO_OR_POSITIVE)
			return BOTTOM;
		return evalMultiplication(left, right);
	}

	@Override
	public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
			ProgramPoint pp) throws SemanticException {
		if (operator instanceof AdditionOperator) {
			return evalAddition(left, right);
		} else if (operator instanceof SubtractionOperator) {
			return evalSubtraction(left, right);
		} else if (operator instanceof MultiplicationOperator) {
			return evalMultiplication(left, right);
		} else if (operator instanceof DivisionOperator) {
			return evalDivision(left, right);
		}
		return super.evalBinaryExpression(operator, left, right, pp);
	}

	@Override
	public ExtSignDomain evalTernaryExpression(TernaryOperator operator, ExtSignDomain left, ExtSignDomain middle,
			ExtSignDomain right, ProgramPoint pp) throws SemanticException {
		return super.evalTernaryExpression(operator, left, middle, right, pp);
	}
}
