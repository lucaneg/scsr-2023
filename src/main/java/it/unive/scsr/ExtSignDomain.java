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
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-10);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-2);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(2);
    private static final ExtSignDomain TOP = new ExtSignDomain(10);

    private final int sign;

    public ExtSignDomain() {
        this(10);
    }

    private ExtSignDomain(int sign) {
        this.sign = sign;
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if ((this.equals(NEGATIVE) && other.equals(ZERO)) || (this.equals(ZERO) && other.equals(NEGATIVE))) {
            return ZERO_OR_NEGATIVE;
        } else if ((this.equals(POSITIVE) && other.equals(ZERO)) || (this.equals(ZERO) && other.equals(POSITIVE))) {
            return ZERO_OR_POSITIVE;
        } else if (this.lessOrEqualAux(other)) {
            return other;
        } else if (other.lessOrEqualAux(this)) {
            return this;
        } else {
            return TOP;
        }
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return (this.equals(NEGATIVE) && other.equals(ZERO_OR_NEGATIVE)) ||
                (this.equals(ZERO) && other.equals(ZERO_OR_NEGATIVE)) ||
                (this.equals(ZERO) && other.equals(ZERO_OR_POSITIVE)) ||
                (this.equals(POSITIVE) && other.equals(ZERO_OR_POSITIVE));
    }

    @Override
    public int hashCode() {
        return sign;
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
        if (this.equals(TOP)) {
            return Lattice.topRepresentation();
        } else if (this.equals(BOTTOM)) {
            return Lattice.bottomRepresentation();
        } else if (this.equals(POSITIVE)) {
            return new StringRepresentation("+");
        } else if (this.equals(NEGATIVE)) {
            return new StringRepresentation("-");
        } else if (this.equals(ZERO)) {
            return new StringRepresentation("0");
        } else if (this.equals(ZERO_OR_POSITIVE)) {
            return new StringRepresentation("0+");
        } else if (this.equals(ZERO_OR_NEGATIVE)){
            return new StringRepresentation("0-");
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return TOP.sign == this.sign;
    }

    @Override
    public boolean isBottom() {
        return BOTTOM.sign == this.sign;
    }


    @Override
    public ExtSignDomain evalNullConstant(ProgramPoint pp) throws SemanticException {
        return super.evalNullConstant(pp);
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return POSITIVE;
            else if (v == 0)
                return ZERO;
            else
                return NEGATIVE;
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (this == NEGATIVE)
            return POSITIVE;
        else if (this == POSITIVE)
            return NEGATIVE;
        else if (this == ZERO_OR_NEGATIVE)
            return ZERO_OR_POSITIVE;
        else if (this == ZERO_OR_POSITIVE)
            return ZERO_OR_NEGATIVE;
        else
            return this;
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        return (operator instanceof NumericNegation) ? arg.negate() : TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp)
            throws SemanticException {
        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator) {
            if (operator instanceof SubtractionOperator) {
                right = right.negate();
            }

            if (left == NEGATIVE) {
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return NEGATIVE;
                else if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return TOP;
            } else if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                else if (right == NEGATIVE) {
                    return NEGATIVE;
                } else if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return TOP;
            } else if (left == ZERO) {
                return right;
            } else if (left == ZERO_OR_POSITIVE) {
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE) {
                    return TOP;
                } else if (right == ZERO || right == ZERO_OR_POSITIVE) {
                    return ZERO_OR_POSITIVE;
                } else if (right == POSITIVE){
                    return POSITIVE;
                }
            } else if (left == POSITIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return POSITIVE;
                else if (right == ZERO_OR_NEGATIVE || right == NEGATIVE)
                    return TOP;
            } else
                return TOP;
        } else if (operator instanceof MultiplicationOperator) {
            if (left == ZERO || right == ZERO) {
                return ZERO;
            } else if (left == POSITIVE) {
                return right;
            } else if (right == POSITIVE) {
                return left;
            } else if (left == NEGATIVE) {
                return right.negate();
            } else if (right == NEGATIVE) {
                return left.negate();
            } else if (left == ZERO_OR_NEGATIVE) {
                return right.negate();
            } else if (left == ZERO_OR_POSITIVE) {
                return right;
            } else {
                return TOP;
            }
        } else if (operator instanceof DivisionOperator) {
            if (right == ZERO) {
                return BOTTOM;
            } else if (left == ZERO) {
                return ZERO;
            } else if (left == TOP || right == TOP) {
                return TOP;
            } else if (left == NEGATIVE) {
                if (right == ZERO_OR_NEGATIVE) {
                    return POSITIVE;
                } else if (right == ZERO_OR_POSITIVE) {
                    return NEGATIVE;
                }
                return right.negate();
            } else if (left == POSITIVE) {
                if (right == ZERO_OR_NEGATIVE) {
                    return NEGATIVE;
                } else if (right == ZERO_OR_POSITIVE) {
                    return POSITIVE;
                }
                return right;
            } else if (left == ZERO_OR_NEGATIVE) {
                if (right == NEGATIVE) {
                    return ZERO_OR_POSITIVE;
                } else if (right == POSITIVE) {
                    return ZERO_OR_NEGATIVE;
                }
                return right.negate();
            } else if (left == ZERO_OR_POSITIVE) {
                if (right == POSITIVE) {
                    return ZERO_OR_POSITIVE;
                } else if (right == NEGATIVE) {
                    return ZERO_OR_NEGATIVE;
                }
                return right;
            }
        }
        return TOP;
    }

}
