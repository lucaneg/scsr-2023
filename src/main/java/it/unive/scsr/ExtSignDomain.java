package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ExtSignDomain extends BaseNonRelationalValueDomain<
        // java requires this type parameter to have this class
        // as type in fields/methods
        ExtSignDomain> {

	// IMPLEMENTATION NOTE:
	// you can follow the same logic of Signs to implement representation().
	// note that this is not mandatory: you can have any other logic instead
	// of constant fields, and you can change the logic in representation()
	// accordingly. the only constraint is that the strings used to represent
	// the elements stay the same (0, +, -, 0+, 0-)

    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-100);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-10);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(10);
    private static final ExtSignDomain TOP = new ExtSignDomain(100);

    // this is just needed to distinguish the elements
    private final int sign;

    public ExtSignDomain() {
        this(10);
    }

    public ExtSignDomain(int sign) {
        this.sign = sign;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sign;
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
        if((this == NEGATIVE || this == ZERO) && other == ZERO_OR_NEGATIVE)
            return true;
        if((this == POSITIVE || this == ZERO) && other == ZERO_OR_POSITIVE)
            return true;
        return false;
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
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if(value > 0)
                return POSITIVE;
            else if(value < 0)
                return NEGATIVE;
            else
                return ZERO;
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
                    return left;
                else
                    return TOP;
            } else if (left == POSITIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return left;
                else
                    return TOP;
            } else if (left == ZERO) {
                    return right;
            } else if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return left;
                else if(right==NEGATIVE)
                    return right;
                else
                    return TOP;
            } else if (left == ZERO_OR_POSITIVE) {
                if (right == ZERO || right == ZERO_OR_POSITIVE)
                    return left;
                else if(right==POSITIVE)
                    return right;
                else
                    return TOP;
            } else
                return TOP;
        } else if (operator instanceof SubtractionOperator) {
            if (left == NEGATIVE) {
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return left;
                else
                    return TOP;
            } else if (left == POSITIVE) {
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return left;
                else
                    return TOP;
            } else if (left == ZERO) {
                return right.negate();
            } else if (left == ZERO_OR_NEGATIVE) {
                if (right == ZERO || right == ZERO_OR_POSITIVE)
                    return left;
                else if (right == POSITIVE)
                    return right.negate();
                else
                    return TOP;
            } else if (left == ZERO_OR_POSITIVE) {
                if (right == ZERO || right == ZERO_OR_NEGATIVE)
                    return left;
                else if (right == NEGATIVE)
                    return right.negate();
                else
                    return TOP;
            } else
                return TOP;
        } else if (operator instanceof MultiplicationOperator) {
            if (left == ZERO || right == ZERO) {
                return ZERO;
            }else if (left == NEGATIVE) {
                return right.negate();
            } else if (left == POSITIVE) {
                return right;
            } else  if (left == ZERO_OR_NEGATIVE || left == ZERO_OR_POSITIVE){
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
            } else if (left == POSITIVE) {
                return right;
            } else if (left == ZERO) {
                return ZERO;
            }else if (left == ZERO_OR_NEGATIVE || left == ZERO_OR_POSITIVE){
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
