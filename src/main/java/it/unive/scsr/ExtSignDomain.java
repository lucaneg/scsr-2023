package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

	private final Sign sign;

	public ExtSignDomain() {
		this(Sign.TOP);
	}

	private ExtSignDomain(Sign sign) {
		this.sign = sign;
	}

	enum Sign {

		BOTTOM {

			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				return this;
			}

			@Override
			Sign mul(Sign other) {
				return this;
			}

			@Override
			Sign div(Sign other) {
				return this;
			}

			@Override
			public String toString() {
				return Lattice.BOTTOM_STRING;
			}
		},

		TOP {

			@Override
			Sign minus() {
				return this;
			}

			@Override
			Sign add(Sign other) {
				return other == BOTTOM ? other : this;
			}

			@Override
			Sign mul(Sign other) {
				return other == BOTTOM ? other : other == ZERO ? ZERO : TOP;
			}

			@Override
			Sign div(Sign other) {
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return Lattice.TOP_STRING;
			}
		},

		POSITIVE {

			@Override
			Sign minus() {
				return NEGATIVE;
			}

			@Override
			Sign add(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POSITIVE || other == ZERO_OR_POS || other == ZERO)
					return this;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				return other;
			}

			@Override
			Sign div(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POSITIVE || other == ZERO_OR_POS)
					return POSITIVE;
				if (other == NEGATIVE || other == ZERO_OR_NEG)
					return NEGATIVE;
				return BOTTOM;
			}

			@Override
			public String toString() {
				return "+";
			}
		},

		NEGATIVE {

			@Override
			Sign minus() {
				return POSITIVE;
			}

			@Override
			Sign add(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == NEGATIVE || other == ZERO || other == ZERO_OR_NEG)
					return NEGATIVE;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE)
					return this;

				if (other == ZERO)
					return other;

				if (other == NEGATIVE)
					return POSITIVE;

				if (other == ZERO_OR_POS)
					return ZERO_OR_NEG;
				return ZERO_OR_POS;
			}

			@Override
			Sign div(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return NEGATIVE;

				if (other == NEGATIVE || other == ZERO_OR_NEG)
					return POSITIVE;

				return BOTTOM;
			}

			@Override
			public String toString() {
				return "-";
			}
		},

		ZERO {

			@Override
			Sign minus() {
				return ZERO;
			}

			@Override
			Sign add(Sign other) {
				return other;
			}

			@Override
			Sign mul(Sign other) {
				return other == BOTTOM ? other : ZERO;
			}

			@Override
			Sign div(Sign other) {
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			public String toString() {
				return "0";
			}
		},

		ZERO_OR_POS {

			@Override
			Sign minus() {
				return ZERO_OR_NEG;
			}

			@Override
			Sign add(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return other;

				if (other == ZERO)
					return ZERO_OR_POS;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return ZERO_OR_POS;

				if (other == NEGATIVE || other == ZERO_OR_NEG)
					return ZERO_OR_NEG;

				return ZERO;
			}

			@Override
			Sign div(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return ZERO_OR_POS;

				if (other == NEGATIVE || other == ZERO_OR_NEG)
					return ZERO_OR_NEG;

				return BOTTOM;
			}

			@Override
			public String toString() {
				return "0+";
			}
		},

		ZERO_OR_NEG {

			@Override
			Sign minus() {
				return ZERO_OR_POS;
			}

			@Override
			Sign add(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == ZERO || other == ZERO_OR_NEG)
					return ZERO_OR_NEG;

				if (other == NEGATIVE)
					return other;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return ZERO_OR_NEG;

				if (other == ZERO)
					return other;

				return ZERO_OR_POS;
			}

			@Override
			Sign div(Sign other) {
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POSITIVE || other == ZERO_OR_POS)
					return ZERO_OR_NEG;

				if (other == ZERO)
					return BOTTOM;
				return ZERO_OR_POS;
			}

			@Override
			public String toString() {
				return "0-";
			}
		};

		abstract Sign minus();

		abstract Sign add(Sign other);

		abstract Sign mul(Sign other);

		abstract Sign div(Sign other);

		@Override
		public abstract String toString();
	}

	@Override
	public ExtSignDomain top() {
		return new ExtSignDomain(Sign.TOP);
	}

	@Override
	public ExtSignDomain bottom() {
		return new ExtSignDomain(Sign.BOTTOM);
	}

	@Override
	public boolean isTop() {
		return this.sign == Sign.TOP;
	}

	@Override
	public boolean isBottom() {
		return this.sign == Sign.BOTTOM;
	}

	@Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			int c = (int) constant.getValue();
			if (c == 0)
				return new ExtSignDomain(Sign.ZERO);
			else if (c > 0)
				return new ExtSignDomain(Sign.POSITIVE);
			else
				return new ExtSignDomain(Sign.NEGATIVE);
		}
		return top();
	}

	@Override
	public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
		if (operator instanceof NumericNegation)
			return new ExtSignDomain(arg.sign.minus());
		return top();
	}

	@Override
	public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
		if (operator instanceof AdditionOperator)
			return new ExtSignDomain(left.sign.add(right.sign));
        if (operator instanceof SubtractionOperator)    
			return new ExtSignDomain(left.sign.add(right.sign.minus()));
		if (operator instanceof Multiplication)
			return new ExtSignDomain(left.sign.mul(right.sign));
        if (operator instanceof DivisionOperator)
			return new ExtSignDomain(left.sign.div(right.sign));
		return top();
	}

	@Override
	public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
		if (lessOrEqual(other))
			return other;
		if (other.lessOrEqual(this))
			return this;

		if (sign == Sign.ZERO) {
			if (other.sign == Sign.POSITIVE)
				return new ExtSignDomain(Sign.ZERO_OR_POS);
			else if (other.sign == Sign.NEGATIVE)
				return new ExtSignDomain(Sign.ZERO_OR_NEG);
		}

		if (other.sign == Sign.ZERO) {
			if (sign == Sign.POSITIVE)
				return new ExtSignDomain(Sign.ZERO_OR_POS);
			else if (sign == Sign.NEGATIVE)
				return new ExtSignDomain(Sign.ZERO_OR_NEG);
		}

		return top();
	}

	@Override
	public ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
		switch (sign) {
        case ZERO:
			if (other.sign == Sign.ZERO_OR_POS || other.sign == Sign.ZERO_OR_NEG)
				return true;
			return false;

		case POSITIVE:
			if (other.sign == Sign.ZERO_OR_POS)
				return true;
			return false;

        case NEGATIVE:
			if (other.sign == Sign.ZERO_OR_NEG)
				return true;
			return false;
            
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sign == null) ? 0 : sign.hashCode());
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
        ExtSignDomain other = (ExtSignDomain) obj;
		if (sign != other.sign)
			return false;
		return true;
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}
}
