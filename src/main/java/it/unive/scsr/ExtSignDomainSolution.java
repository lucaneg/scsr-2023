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

public class ExtSignDomainSolution extends BaseNonRelationalValueDomain<ExtSignDomainSolution> {

	private final Sign sign;

	public ExtSignDomainSolution() {
		this(Sign.TOP);
	}

	private ExtSignDomainSolution(Sign sign) {
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
				// add(top, bottom) = bottom
				// add(top, top) = top;
				// add(top, +) = top
				// add(top, 0) = top
				// add(top, -) = top
				// add(top, 0+) = top
				// add(top, 0-) = top
				return other == BOTTOM ? other : this;
			}

			@Override
			Sign mul(Sign other) {
				// mul(top, bottom) = bottom
				// mul(top, top) = top;
				// mul(top, +) = top
				// mul(top, 0) = 0
				// mul(top, -) = top
				// mul(top, 0+) = top
				// mul(top, 0-) = top
				return other == BOTTOM ? other : other == ZERO ? ZERO : TOP;
			}

			@Override
			Sign div(Sign other) {
				// div(top, bottom) = bottom
				// div(top, top) = top;
				// div(top, +) = top
				// div(top, 0) = bottom
				// div(top, -) = top
				// div(top, 0+) = top
				// div(top, 0-) = top
				return other == ZERO || other == BOTTOM ? BOTTOM : TOP;
			}

			@Override
			public String toString() {
				return Lattice.TOP_STRING;
			}
		},

		POS {

			@Override
			Sign minus() {
				return NEG;
			}

			@Override
			Sign add(Sign other) {
				// add(+, bottom) = bottom
				// add(+, top) = top;
				// add(+, +) = +
				// add(+, 0) = +
				// add(+, -) = top
				// add(+, 0+) = +
				// add(+, 0-) = top
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POS || other == POS_OR_ZERO || other == ZERO)
					return this;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(+, bottom) = bottom
				// mul(+, top) = top;
				// mul(+, +) = +
				// mul(+, 0) = 0
				// mul(+, -) = -
				// mul(+, 0+) = 0+
				// mul(+, 0-) = 0-
				return other;
			}

			@Override
			Sign div(Sign other) {
				// div(+, bottom) = bottom
				// div(+, top) = top;
				// div(+, +) = +
				// div(+, 0) = bottom
				// div(+, -) = -
				// div(+, 0+) = +
				// div(+, 0-) = -
				if (other == TOP || other == BOTTOM)
					return other;
				if (other == POS || other == POS_OR_ZERO)
					return POS;
				if (other == NEG || other == NEG_OR_ZERO)
					return NEG;
				return BOTTOM;
			}

			@Override
			public String toString() {
				return "+";
			}
		},

		NEG {

			@Override
			Sign minus() {
				return POS;
			}

			@Override
			Sign add(Sign other) {
				// add(-, bottom) = bottom
				// add(-, top) = top;
				// add(-, +) = top
				// add(-, 0) = -
				// add(-, -) = -
				// add(-, 0+) = top
				// add(-, 0-) = -
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == NEG || other == ZERO || other == NEG_OR_ZERO)
					return NEG;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(-, bottom) = bottom
				// mul(-, top) = top;
				// mul(-, +) = -
				// mul(-, 0) = 0
				// mul(-, -) = +
				// mul(-, 0+) = 0-
				// mul(-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS)
					return this;

				if (other == ZERO)
					return other;

				if (other == NEG)
					return POS;

				if (other == POS_OR_ZERO)
					return NEG_OR_ZERO;
				return POS_OR_ZERO;
			}

			@Override
			Sign div(Sign other) {
				// div(-, bottom) = bottom
				// div(-, top) = top;
				// div(-, +) = -
				// div(-, 0) = bottom
				// div(-, -) = +
				// div(-, 0+) = -
				// div(-, 0-) = +
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return NEG;

				if (other == NEG || other == NEG_OR_ZERO)
					return POS;

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
				// add(0, bottom) = bottom
				// add(0, top) = top;
				// add(0, +) = +
				// add(0, 0) = 0
				// add(0, -) = -
				// add(0, 0+) = 0+
				// add(0, 0-) = 0-
				return other;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0, bottom) = bottom
				// mul(0, top) = 0;
				// mul(0, +) = 0
				// mul(0, 0) = 0
				// mul(0, -) = 0
				// mul(0, 0+) = 0
				// mul(0, 0-) = 0
				return other == BOTTOM ? other : ZERO;
			}

			@Override
			Sign div(Sign other) {
				// div(0, bottom) = bottom
				// div(0, top) = 0;
				// div(0, +) = 0
				// div(0, 0) = bottom
				// div(0, -) = 0
				// div(0, 0+) = 0
				// div(0, 0-) = 0
				return other == ZERO || other == BOTTOM ? BOTTOM : ZERO;
			}

			@Override
			public String toString() {
				return "0";
			}
		},

		POS_OR_ZERO {

			@Override
			Sign minus() {
				return NEG_OR_ZERO;
			}

			@Override
			Sign add(Sign other) {
				// add(0+, bottom) = bottom
				// add(0+, top) = top;
				// add(0+, +) = +
				// add(0+, 0) = 0+
				// add(0+, -) = top
				// add(0+, 0+) = 0+
				// add(0+, 0-) = top
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return other;

				if (other == ZERO)
					return POS_OR_ZERO;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0+, bottom) = bottom
				// mul(0+, top) = top;
				// mul(0+, +) = 0+
				// mul(0+, 0) = 0
				// mul(0+, -) = 0-
				// mul(0+, 0+) = 0+
				// mul(0+, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return POS_OR_ZERO;

				if (other == NEG || other == NEG_OR_ZERO)
					return NEG_OR_ZERO;

				return ZERO;
			}

			@Override
			Sign div(Sign other) {
				// div(0+, bottom) = bottom
				// div(0+, top) = top;
				// div(0+, +) = 0+
				// div(0+, 0) = bottom
				// div(0+, -) = 0-
				// div(0+, 0+) = 0+
				// div(0+, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return POS_OR_ZERO;

				if (other == NEG || other == NEG_OR_ZERO)
					return NEG_OR_ZERO;

				return BOTTOM;
			}

			@Override
			public String toString() {
				return "0+";
			}
		},

		NEG_OR_ZERO {

			@Override
			Sign minus() {
				return POS_OR_ZERO;
			}

			@Override
			Sign add(Sign other) {
				// add(0-, bottom) = bottom
				// add(0-, top) = top;
				// add(0-, +) = top
				// add(0-, 0) = 0-
				// add(0-, -) = -
				// add(0-, 0+) = top
				// add(0-, 0-) = 0-
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == ZERO || other == NEG_OR_ZERO)
					return NEG_OR_ZERO;

				if (other == NEG)
					return other;

				return TOP;
			}

			@Override
			Sign mul(Sign other) {
				// mul(0-, bottom) = bottom
				// mul(0-, top) = top;
				// mul(0-, +) = 0-
				// mul(0-, 0) = 0
				// mul(0-, -) = 0+
				// mul(0-, 0+) = 0-
				// mul(0-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return NEG_OR_ZERO;

				if (other == ZERO)
					return other;

				return POS_OR_ZERO;
			}

			@Override
			Sign div(Sign other) {
				// div(0-, bottom) = bottom
				// div(0-, top) = top;
				// div(0-, +) = 0-
				// div(0-, 0) = bottom
				// div(0-, -) = 0+
				// div(0-, 0+) = 0-
				// div(0-, 0-) = 0+
				if (other == TOP || other == BOTTOM)
					return other;

				if (other == POS || other == POS_OR_ZERO)
					return NEG_OR_ZERO;

				if (other == ZERO)
					return BOTTOM;
				return POS_OR_ZERO;
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
	public ExtSignDomainSolution top() {
		return new ExtSignDomainSolution(Sign.TOP);
	}

	@Override
	public ExtSignDomainSolution bottom() {
		return new ExtSignDomainSolution(Sign.BOTTOM);
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
	public ExtSignDomainSolution evalNonNullConstant(Constant constant, ProgramPoint pp) {
		if (constant.getValue() instanceof Integer) {
			int c = (int) constant.getValue();
			if (c == 0)
				return new ExtSignDomainSolution(Sign.ZERO);
			else if (c > 0)
				return new ExtSignDomainSolution(Sign.POS);
			else
				return new ExtSignDomainSolution(Sign.NEG);
		}
		return top();
	}

	@Override
	public ExtSignDomainSolution evalUnaryExpression(UnaryOperator operator, ExtSignDomainSolution arg,
			ProgramPoint pp) {
		if (operator instanceof NumericNegation)
			return new ExtSignDomainSolution(arg.sign.minus());
		return top();
	}

	@Override
	public ExtSignDomainSolution evalBinaryExpression(BinaryOperator operator, ExtSignDomainSolution left,
			ExtSignDomainSolution right,
			ProgramPoint pp) {
		if (operator instanceof AdditionOperator)
			return new ExtSignDomainSolution(left.sign.add(right.sign));
		if (operator instanceof DivisionOperator)
			return new ExtSignDomainSolution(left.sign.div(right.sign));
		if (operator instanceof MultiplicationOperator)
			return new ExtSignDomainSolution(left.sign.mul(right.sign));
		if (operator instanceof SubtractionOperator)
			return new ExtSignDomainSolution(left.sign.add(right.sign.minus()));
		return top();
	}

	@Override
	public ExtSignDomainSolution lubAux(ExtSignDomainSolution other) throws SemanticException {
		if (lessOrEqual(other))
			return other;
		if (other.lessOrEqual(this))
			return this;

		if (sign == Sign.ZERO) {
			if (other.sign == Sign.POS)
				return new ExtSignDomainSolution(Sign.POS_OR_ZERO);
			else if (other.sign == Sign.NEG)
				return new ExtSignDomainSolution(Sign.NEG_OR_ZERO);
		}

		if (other.sign == Sign.ZERO) {
			if (sign == Sign.POS)
				return new ExtSignDomainSolution(Sign.POS_OR_ZERO);
			else if (sign == Sign.NEG)
				return new ExtSignDomainSolution(Sign.NEG_OR_ZERO);
		}

		return top();
	}

	@Override
	public ExtSignDomainSolution wideningAux(ExtSignDomainSolution other) throws SemanticException {
		return lubAux(other);
	}

	@Override
	public boolean lessOrEqualAux(ExtSignDomainSolution other) throws SemanticException {
		switch (sign) {
		case NEG:
			if (other.sign == Sign.NEG_OR_ZERO)
				return true;
			return false;
		case POS:
			if (other.sign == Sign.POS_OR_ZERO)
				return true;
			return false;
		case ZERO:
			if (other.sign == Sign.POS_OR_ZERO || other.sign == Sign.NEG_OR_ZERO)
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
		ExtSignDomainSolution other = (ExtSignDomainSolution) obj;
		if (sign != other.sign)
			return false;
		return true;
	}

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(sign);
	}
}
