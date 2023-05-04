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

    public ExtSignDomain(int sign) {
        this.sign = sign;
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if((this == NEGATIVE && other == ZERO) || (this == ZERO && other == NEGATIVE))
            return ZERO_OR_NEGATIVE;
        if((this == POSITIVE && other == ZERO)||(this == ZERO && other == POSITIVE))
            return ZERO_OR_POSITIVE;
        if(this.lessOrEqualAux(other))
            return other;
        if(other.lessOrEqualAux(this))
            return this;
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if(this == NEGATIVE && other == ZERO_OR_NEGATIVE)
            return true;
        if(this == ZERO && other == ZERO_OR_NEGATIVE)
            return true;
        if(this == POSITIVE && other == ZERO_OR_POSITIVE)
            return true;
        if(this == ZERO && other == ZERO_OR_POSITIVE)
            return true;
        return false;
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

        return other.sign == sign;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + sign;
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
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    private ExtSignDomain negate() {
        if (this == ZERO_OR_POSITIVE)
            return ZERO_OR_NEGATIVE;
        if (this == POSITIVE)
            return NEGATIVE;
        if (this == ZERO_OR_NEGATIVE)
            return ZERO_OR_POSITIVE;
        if (this == NEGATIVE)
            return POSITIVE;
        return this;
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            int value = (int) constant.getValue();
            if (value == 0)
                return ZERO;
            if (value > 0)
                return POSITIVE;
            else
                return NEGATIVE;
        }
        return top();
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if(operator instanceof NumericNegation)
            return arg.negate();
        return TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            if (left == NEGATIVE) {
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return NEGATIVE;
                else
                    return TOP;
            }
            if (left == POSITIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return POSITIVE;
                else
                    return TOP;
            }
            if (left == ZERO) {
                return right;
            }
            if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                else if(right==NEGATIVE)
                    return NEGATIVE;
                else
                    return TOP;
            }
            if (left == ZERO_OR_POSITIVE) {
                if (right == ZERO || right == ZERO_OR_POSITIVE)
                    return ZERO_OR_POSITIVE;
                else if(right==POSITIVE)
                    return POSITIVE;
                else
                    return TOP;
            }
            return TOP;
        } else if (operator instanceof SubtractionOperator) {
            if (left == NEGATIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return NEGATIVE;
                else
                    return TOP;
            }
            if (left == POSITIVE) {
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return POSITIVE;
                else
                    return TOP;
            }
            if (left == ZERO) {
                return right.negate();
            }
            if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_POSITIVE)
                    return ZERO_OR_NEGATIVE;
                else if (right == POSITIVE)
                    return NEGATIVE;
                else
                    return TOP;
            }
            if (left == ZERO_OR_POSITIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_POSITIVE;
                else if (right == NEGATIVE)
                    return POSITIVE;
                else
                    return TOP;
            } else
                return TOP;
        } else if (operator instanceof MultiplicationOperator) {
            if (left == ZERO || right == ZERO) {
                return ZERO;
            }
            if (left == NEGATIVE) {
                return right.negate();
            }
            if (left == POSITIVE) {
                return right;
            }
            if (left == ZERO_OR_NEGATIVE || left == ZERO_OR_POSITIVE){
                if(right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return left.negate();
                else if(right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return left;
                else
                    return TOP;
            }  else
                return TOP;
        } else if (operator instanceof DivisionOperator) {
            if (right == ZERO)
                return BOTTOM;

            if (left == NEGATIVE) {
                return right.negate();
            }
            if (left == POSITIVE) {
                return right;
            }
            if (left == ZERO) {
                return ZERO;
            }
            if (left == ZERO_OR_NEGATIVE || left == ZERO_OR_POSITIVE){
                if(right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return left.negate();
                else if(right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return left;
                else
                    return TOP;
            }  else
                return TOP;
        }

        return TOP;
    }

}
