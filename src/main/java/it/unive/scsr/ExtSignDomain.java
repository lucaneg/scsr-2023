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

import javax.swing.border.EtchedBorder;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-10);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-1);

    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(1);

    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain TOP = new ExtSignDomain(10);

    private final int sign;

    public ExtSignDomain(int sign) {
        this.sign = sign;
    }

    public ExtSignDomain() {
        this(10);
    }


    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this == ZERO_OR_POSITIVE || this == ZERO_OR_NEGATIVE) {
            return false;
        }
        if (this == POSITIVE) {
            return other == ZERO_OR_POSITIVE;
        }
        if (this == NEGATIVE) {
            return other == ZERO_OR_NEGATIVE;
        }
        if (this == ZERO) {
            return other == ZERO_OR_POSITIVE || other == ZERO_OR_NEGATIVE;
        }
        return false;
    }


    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this == NEGATIVE) {
            if (other == ZERO || other == ZERO_OR_NEGATIVE) {
                return ZERO_OR_NEGATIVE;
            }
            return TOP;
        }
        if (this == POSITIVE) {
            if (other == ZERO || other == ZERO_OR_POSITIVE) {
                return ZERO_OR_POSITIVE;
            }
            return TOP;
        }
        if (this == ZERO) {
            if (other == POSITIVE || other == ZERO_OR_POSITIVE) {
                return ZERO_OR_POSITIVE;
            }
            if (other == NEGATIVE || other == ZERO_OR_NEGATIVE) {
                return ZERO_OR_NEGATIVE;
            }
        }
        if (this == ZERO_OR_NEGATIVE) {
            if (other == POSITIVE) {
                return TOP;
            }
            return ZERO_OR_NEGATIVE;
        }
        if (this == ZERO_OR_POSITIVE) {
            if (other == NEGATIVE) {
                return TOP;
            }
            return ZERO_OR_POSITIVE;
        }
        return TOP;
    }

    private ExtSignDomain negate() {
        if (this == NEGATIVE)
            return POSITIVE;
        else if (this == POSITIVE)
            return NEGATIVE;
        else
            return this;
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

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();

        return TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator || operator instanceof  SubtractionOperator) {

            if (operator instanceof SubtractionOperator)
                right = right.negate();

            if (left == NEGATIVE) {
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return left;
            } else if (left == POSITIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return left;
            } else if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return left;
                if (right == NEGATIVE)
                    return right;
            } else if (left == ZERO_OR_POSITIVE) {
                if (right == ZERO || right == ZERO_OR_POSITIVE)
                    return left;
                if (right == NEGATIVE)
                    return right;
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
            if (right == ZERO || right == ZERO_OR_NEGATIVE || right == ZERO_OR_POSITIVE)
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


        return super.evalBinaryExpression(operator, left, right, pp);
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sign;
        return result;
    }

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

    @Override
    public ExtSignDomain top() {
        return this.TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return this.BOTTOM;
    }
}
