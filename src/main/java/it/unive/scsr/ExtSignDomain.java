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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private final Sign sign;

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    private ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    enum Sign {
        BOT {
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
                return other == BOT ? other : this;
            }

            @Override
            Sign mul(Sign other) {
                return other == BOT ? other : other == ZERO ? ZERO : TOP;
            }

            @Override
            Sign div(Sign other) {
                return other == ZERO || other == BOT ? BOT : TOP;
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
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE || other == ZERO)
                    return this;
                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                return other;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return POSITIVE;
                if (other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                    return NEGATIVE;
                return BOT;
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
                if (other == TOP || other == BOT)
                    return other;
                if (other == NEGATIVE || other == ZERO || other == ZERO_OR_NEGATIVE)
                    return NEGATIVE;
                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE)
                    return this;
                if (other == ZERO)
                    return other;
                if (other == NEGATIVE)
                    return POSITIVE;
                if (other == ZERO_OR_POSITIVE)
                    return ZERO_OR_NEGATIVE;
                return ZERO_OR_POSITIVE;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return NEGATIVE;
                if (other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                    return POSITIVE;
                return BOT;
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
                return other == BOT ? other : ZERO;
            }

            @Override
            Sign div(Sign other) {
                return other == ZERO || other == BOT ? BOT : ZERO;
            }

            @Override
            public String toString() {
                return "0";
            }
        },

        ZERO_OR_POSITIVE {
            @Override
            Sign minus() {
                return ZERO_OR_NEGATIVE;
            }

            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return other;
                if (other == ZERO)
                    return ZERO_OR_POSITIVE;
                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return ZERO_OR_POSITIVE;
                if (other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                return ZERO;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return ZERO_OR_POSITIVE;
                if (other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                return BOT;
            }

            @Override
            public String toString() {
                return "0+";
            }
        },

        ZERO_OR_NEGATIVE {
            @Override
            Sign minus() {
                return ZERO_OR_POSITIVE;
            }

            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == ZERO || other == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                if (other == NEGATIVE)
                    return other;
                return TOP;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return ZERO_OR_NEGATIVE;
                if (other == ZERO)
                    return other;
                return ZERO_OR_POSITIVE;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                    return ZERO_OR_NEGATIVE;
                if (other == ZERO)
                    return BOT;
                return ZERO_OR_POSITIVE;
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
        return new ExtSignDomain(Sign.BOT);
    }

    @Override
    public boolean isTop() {
        return this.sign == Sign.TOP;
    }

    @Override
    public boolean isBottom() {
        return this.sign == Sign.BOT;
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            int k = (int) constant.getValue();
            if (k == 0) {
                return new ExtSignDomain(Sign.ZERO);
            }
            else {
                if (k > 0) {
                    return new ExtSignDomain(Sign.POSITIVE);
                }
                else {
                    return new ExtSignDomain(Sign.NEGATIVE);
                }
            }
        }
        return top();
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
        if (operator instanceof NumericNegation) {
            return new ExtSignDomain(arg.sign.minus());
        }
        return top();
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
        if (operator instanceof AdditionOperator) {
            return new ExtSignDomain(left.sign.add(right.sign));
        }
        if (operator instanceof DivisionOperator) {
            return new ExtSignDomain(left.sign.div(right.sign));
        }
        if (operator instanceof MultiplicationOperator) {
            return new ExtSignDomain(left.sign.mul(right.sign));
        }
        if (operator instanceof SubtractionOperator) {
            return new ExtSignDomain(left.sign.add(right.sign.minus()));
        }
        return top();
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (lessOrEqual(other)) {
            return other;
        }
        if (other.lessOrEqual(this)) {
            return this;
        }
        if (sign == Sign.ZERO) {
            if (other.sign == Sign.POSITIVE) {
                return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
            }
            else {
                if (other.sign == Sign.NEGATIVE) {
                    return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                }
            }
        }
        if (other.sign == Sign.ZERO) {
            if (sign == Sign.POSITIVE) {
                return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
            }
            else {
                if (sign == Sign.NEGATIVE) {
                    return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                }
            }
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
            case NEGATIVE:
                return other.sign == Sign.ZERO_OR_NEGATIVE;
            case POSITIVE:
                return other.sign == Sign.ZERO_OR_POSITIVE;
            case ZERO:
                return other.sign == Sign.ZERO_OR_POSITIVE || other.sign == Sign.ZERO_OR_NEGATIVE;
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
        return sign == other.sign;
    }

    @Override
	public DomainRepresentation representation() {
		if (this.sign == Sign.TOP)
			return Lattice.topRepresentation();
		if (this.sign == Sign.BOT)
			return Lattice.bottomRepresentation();
		if (this.sign == Sign.POSITIVE)
			return new StringRepresentation("+");
		if (this.sign == Sign.NEGATIVE)
			return new StringRepresentation("-");
		if (this.sign == Sign.ZERO)
			return new StringRepresentation("0");
		if (this.sign == Sign.ZERO_OR_POSITIVE)
			return new StringRepresentation("0+");
		return new StringRepresentation("0-");
	}
}
