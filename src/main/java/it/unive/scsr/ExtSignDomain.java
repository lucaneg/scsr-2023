package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.Constant;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private final Sign sign;

    public ExtSignDomain() {
        this.sign = Sign.TOP;
    }

    private ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    enum Sign {
        TOP {
            @Override
            Sign add(Sign other) {
                return other == BOT ? other : this;
            }

            @Override
            Sign sub() {
                return this;
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

        ZERO_POS {
            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return other;
                if (other == ZERO)
                    return ZERO_POS;
                return TOP;
            }

            @Override
            Sign sub() {
                return ZERO_NEG;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return ZERO_POS;
                if (other == NEG || other == ZERO_NEG)
                    return ZERO_NEG;
                return ZERO;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return ZERO_POS;
                if (other == NEG || other == ZERO_NEG)
                    return ZERO_NEG;
                return BOT;
            }

            @Override
            public String toString() {
                return "0+";
            }
        },

        ZERO_NEG {
            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == ZERO || other == ZERO_NEG)
                    return ZERO_NEG;
                if (other == NEG)
                    return other;
                return TOP;
            }

            @Override
            Sign sub() {
                return ZERO_POS;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return ZERO_NEG;
                if (other == ZERO)
                    return other;
                return ZERO_POS;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return ZERO_NEG;
                if (other == ZERO)
                    return BOT;
                return ZERO_POS;
            }

            @Override
            public String toString() {
                return "0-";
            }
        },

        POS {
            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS || other == ZERO)
                    return this;
                return TOP;
            }

            @Override
            Sign sub() {
                return NEG;
            }

            @Override
            Sign mul(Sign other) {
                return other;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO_POS)
                    return POS;
                if (other == NEG || other == ZERO_NEG)
                    return NEG;
                return BOT;
            }

            @Override
            public String toString() {
                return "+";
            }
        },

        NEG {
            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == NEG || other == ZERO || other == ZERO_NEG)
                    return NEG;
                return TOP;
            }

            @Override
            Sign sub() {
                return POS;
            }

            @Override
            Sign mul(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS)
                    return this;
                if (other == ZERO)
                    return other;
                if (other == NEG)
                    return POS;
                if (other == ZERO_POS)
                    return ZERO_NEG;
                return ZERO_POS;
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOT)
                    return other;
                if (other == POS || other == ZERO)
                    return NEG;
                if (other == NEG || other == ZERO_NEG)
                    return POS;
                return BOT;
            }

            @Override
            public String toString() {
                return "-";
            }
        },

        ZERO {
            @Override
            Sign add(Sign other) {
                return other;
            }

            @Override
            Sign sub() {
                return ZERO;
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

        BOT {
            @Override
            Sign add(Sign other) {
                return this;
            }

            @Override
            Sign sub() {
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
        };

        abstract Sign add(Sign other);

        abstract Sign sub();

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
            return k == 0 ? new ExtSignDomain(Sign.ZERO) : k > 0 ? new ExtSignDomain(Sign.POS) : new ExtSignDomain(Sign.NEG);
        }
        return top();
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) {
        if (operator instanceof NumericNegation) {
            return new ExtSignDomain(arg.sign.sub());
        }
        return top();
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right,
            ProgramPoint pp) {
        if (operator instanceof AdditionOperator) {
            return new ExtSignDomain(left.sign.add(right.sign));
        }
        if (operator instanceof SubtractionOperator) {
            return new ExtSignDomain(left.sign.add(right.sign.sub()));
        }
        if (operator instanceof MultiplicationOperator) {
            return new ExtSignDomain(left.sign.mul(right.sign));
        }
        if (operator instanceof DivisionOperator) {
            return new ExtSignDomain(left.sign.div(right.sign));
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
            if (other.sign == Sign.POS) {
                return new ExtSignDomain(Sign.ZERO_POS);
            }
            if (other.sign == Sign.NEG) {
                return new ExtSignDomain(Sign.ZERO_NEG);
            }
        }
        if (other.sign == Sign.ZERO) {
            if (sign == Sign.POS) {
                return new ExtSignDomain(Sign.ZERO_POS);
            }
            if (sign == Sign.NEG) {
                return new ExtSignDomain(Sign.ZERO_NEG);
            }
        }
        return top();
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        switch (this.sign) {
            case POS:
                return other.sign == Sign.ZERO_POS;
            case NEG:
                return other.sign == Sign.ZERO_NEG;
            case ZERO:
                return other.sign == Sign.ZERO_POS || other.sign == Sign.ZERO_NEG;
            default:
                return false;
        }
    }

    @Override
    public DomainRepresentation representation() {
        switch (this.sign) {
            case TOP:
                return Lattice.topRepresentation();
            case ZERO_POS:
                return new StringRepresentation("0+");
            case ZERO_NEG:
                return new StringRepresentation("0-");
            case POS:
                return new StringRepresentation("+");
            case NEG:
                return new StringRepresentation("-");
            case ZERO:
                return new StringRepresentation("0");
            default: // this is case BOT
                return Lattice.bottomRepresentation();
        }
    }

    @Override
    public ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sign == null) ? 0 : sign.hashCode());
        return result;
    }
}

