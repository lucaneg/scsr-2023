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

	// IMPLEMENTATION NOTE:
	// you can follow the same logic of Signs to implement representation().
	// note that this is not mandatory: you can have any other logic instead
	// of constant fields, and you can change the logic in representation()
	// accordingly. the only constraint is that the strings used to represent
	// the elements stay the same (0, +, -, 0+, 0-)

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
            Sign add(Sign other) {
                return this;
            }
            @Override
            Sign minus() {
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
            Sign add(Sign other) {
                if(other == BOTTOM){
                    return other;
                }
                else{
                    return this;
                }
            }
            @Override
            Sign minus() {
                return this;
            }

            @Override
            Sign mul(Sign other) {
                if(other== ZERO){
                    return other;
                }
                else {
                    if(other == BOTTOM){
                        return other;
                    }
                    else {
                        return this;
                    }
                }
            }

            @Override
            Sign div(Sign other) {

                if(other==ZERO || other==BOTTOM){
                    return BOTTOM;
                }
                else {
                    return this;
                }
            }

            @Override
            public String toString() {
                return Lattice.TOP_STRING;
            }
        },

        POS {

            @Override
            Sign add(Sign other) {

                if (other == TOP || other == BOTTOM)
                    return other;
                else if (other == ZERO || other == POS || other == POS_OR_ZERO)
                    return this;
                else {
                    return TOP;
                }
            }
            @Override
            Sign minus() {
                return NEG;
            }

            @Override
            Sign mul(Sign other) {
                return other;
            }

            @Override
            Sign div(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return POS;
                }
                else if (other == NEG || other == NEG_OR_ZERO) {
                    return NEG;
                }
                else {
                    return BOTTOM;
                }
            }

            @Override
            public String toString() {
                return "+";
            }
        },
        NEG {

            @Override
            Sign add(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == NEG || other == ZERO || other == NEG_OR_ZERO) {
                    return NEG;
                }
                else{
                    return TOP;
                }
            }
            @Override
            Sign minus() {
                return POS;
            }

            @Override
            Sign mul(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS) {
                    return this;
                }
                else if (other == ZERO) {
                    return other;
                }
                else if (other == NEG) {
                    return POS;
                }
                else if (other == POS_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else {
                    return POS_OR_ZERO;
                }
            }

            @Override
            Sign div(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return NEG;
                }
                else if (other == NEG || other == NEG_OR_ZERO) {
                    return POS;
                }
                else {
                    return BOTTOM;
                }
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
            Sign minus() {
                return ZERO;
            }
            @Override
            Sign mul(Sign other) {

                if(other==BOTTOM){
                    return other;
                }
                else {
                    return this;
                }

            }
            @Override
            Sign div(Sign other) {

                if(other==BOTTOM || other==ZERO){
                    return BOTTOM;
                }
                else {
                    return this;
                }
            }

            @Override
            public String toString() {
                return "0";
            }
        },

        POS_OR_ZERO {

            @Override
            Sign add(Sign other) {
                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return other;
                }
                else if (other == ZERO) {
                    return POS_OR_ZERO;
                }
                else {
                    return TOP;
                }
            }
            @Override
            Sign minus() {
                return NEG_OR_ZERO;
            }

            @Override
            Sign mul(Sign other) {

                if (other == TOP || other == BOTTOM){
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return POS_OR_ZERO;
                }
                else if (other == NEG || other == NEG_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else {
                    return ZERO;
                }
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return POS_OR_ZERO;
                }
                else if (other == NEG || other == NEG_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else {
                    return BOTTOM;
                }
            }

            @Override
            public String toString() {
                return "0+";
            }
        },

        NEG_OR_ZERO {

            @Override
            Sign add(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == ZERO || other == NEG_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else if (other == NEG) {
                    return other;
                }
                else {
                    return TOP;
                }
            }
            @Override
            Sign minus() {
                return POS_OR_ZERO;
            }

            @Override
            Sign mul(Sign other) {

                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else if (other == ZERO) {
                    return other;
                }
                else {
                    return POS_OR_ZERO;
                }
            }

            @Override
            Sign div(Sign other) {
                if (other == TOP || other == BOTTOM) {
                    return other;
                }
                else if (other == POS || other == POS_OR_ZERO) {
                    return NEG_OR_ZERO;
                }
                else if (other == ZERO) {
                    return BOTTOM;
                }
                else {
                    return POS_OR_ZERO;
                }
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
    public ExtSignDomain evalNonNullConstant(Constant c, ProgramPoint pp) {
        if (c.getValue() instanceof Integer) {

            int x = (int) c.getValue();

            if (x == 0) {
                return new ExtSignDomain(Sign.ZERO);
            }
            else if (x < 0) {
                return new ExtSignDomain(Sign.NEG);
            }
            else {
                return new ExtSignDomain(Sign.POS);
            }
        }
        return top();
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator op, ExtSignDomain other, ProgramPoint pp) {
        if (op instanceof NumericNegation) {
            return new ExtSignDomain(other.sign.minus());
        }
        else {
            return top();
        }
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator op, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
        if (op instanceof AdditionOperator){
            return new ExtSignDomain(left.sign.add(right.sign));
        }
        else if (op instanceof SubtractionOperator){
            return new ExtSignDomain(left.sign.add(right.sign.minus()));
        }
        else if (op instanceof Multiplication) {
            return new ExtSignDomain(left.sign.mul(right.sign));
        }
        else if (op instanceof DivisionOperator) {
            return new ExtSignDomain(left.sign.div(right.sign));
        }
        else{
            return top();
        }
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (lessOrEqual(other)) {
            return other;
        }
        else if (other.lessOrEqual(this)) {
            return this;
        }

        if (sign == Sign.ZERO) {
            if (other.sign == Sign.POS) {
                return new ExtSignDomain(Sign.POS_OR_ZERO);
            }
            else if (other.sign == Sign.NEG) {
                return new ExtSignDomain(Sign.NEG_OR_ZERO);
            }
        }

        if (other.sign == Sign.ZERO) {
            if (sign == Sign.POS) {
                return new ExtSignDomain(Sign.POS_OR_ZERO);
            }
            else if (sign == Sign.NEG) {
                return new ExtSignDomain(Sign.NEG_OR_ZERO);
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
            case NEG:
                if (other.sign == Sign.NEG_OR_ZERO) {
                    return true;
                }
                else {
                    return false;
                }
            case POS:
                if (other.sign == Sign.POS_OR_ZERO) {
                    return true;
                }
                else {
                    return false;
                }
            case ZERO:
                if (other.sign == Sign.POS_OR_ZERO || other.sign == Sign.NEG_OR_ZERO) {
                    return true;
                }
                else{
                    return false;
                }
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 + ((sign == null) ? 0 : sign.hashCode());
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