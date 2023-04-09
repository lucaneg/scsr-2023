package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

//I'M ASSUMING TO WORKING ONLY WITH INTEGER

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    // IMPLEMENTATION NOTE:
    // you can follow the same logic of Signs to implement representation().
    // note that this is not mandatory: you can have any other logic instead
    // of constant fields, and you can change the logic in representation()
    // accordingly. the only constraint is that the strings used to represent
    // the elements stay the same (0, +, -, 0+, 0-)

    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-100);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-10);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(10);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain TOP = new ExtSignDomain(100);

    // this is just needed to distinguish the elements
    private final int sign;

    public ExtSignDomain() {
        this(100);
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
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if((other == NEGATIVE && this == ZERO) ||
                (other == ZERO && this == NEGATIVE) ||
                (other == ZERO_OR_NEGATIVE && (this == NEGATIVE || this == ZERO)) ||
                (this == ZERO_OR_NEGATIVE && (other == NEGATIVE || other == ZERO)))
            return ZERO_OR_NEGATIVE;

        if((other == POSITIVE && this == ZERO) ||
                (other == ZERO && this == POSITIVE) ||
                (other == ZERO_OR_POSITIVE && (this == POSITIVE || this == ZERO)) ||
                (this == ZERO_OR_POSITIVE && (other == POSITIVE || other == ZERO)))
            return ZERO_OR_POSITIVE;

        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if(other.sign == ZERO_OR_NEGATIVE.sign || other.sign == ZERO_OR_POSITIVE.sign){
            if(this.sign == NEGATIVE.sign || this.sign == POSITIVE.sign || this.sign == ZERO.sign)
                return true;
        }
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
            return new StringRepresentation("0 +");
        return new StringRepresentation("0 -");
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof Integer){
            int value = (int) constant.getValue();
            if (value > 0)
                return POSITIVE;
            else if (value == 0)
                return ZERO;
            else
                return NEGATIVE;
        }
        return top();
    }

    private ExtSignDomain negate() {
        if (this == NEGATIVE)
            return POSITIVE;
        if (this == POSITIVE)
            return NEGATIVE;
        if (this == ZERO_OR_POSITIVE)
            return ZERO_OR_NEGATIVE;
        if (this == ZERO_OR_NEGATIVE)
            return ZERO_OR_POSITIVE;
        return this;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof AdditionOperator)
            return left.lubAux(right);

        if(operator instanceof SubtractionOperator){
            if (left == NEGATIVE){
                if (right == ZERO || right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return NEGATIVE;
                return top();
            }
            if (left == POSITIVE){
                if (right == ZERO || right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return POSITIVE;
                return top();
            }
            if(left == ZERO)
                return right;

            if(left == ZERO_OR_POSITIVE){
                if(right == ZERO || right == ZERO_OR_NEGATIVE || right == NEGATIVE)
                    return ZERO_OR_POSITIVE;
                return top();
            }
            if (left == ZERO_OR_NEGATIVE){
                if(right == ZERO || right == ZERO_OR_POSITIVE || right == POSITIVE)
                    return ZERO_OR_NEGATIVE;
                return top();
            }
        }
        if(operator instanceof MultiplicationOperator){
            if(left == NEGATIVE)
                return right.negate();

            if(left == POSITIVE)
                return right;

            if(left == ZERO)
                return ZERO;

            if(left == ZERO_OR_POSITIVE){
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                return ZERO_OR_POSITIVE;
            }
            if(left == ZERO_OR_NEGATIVE) {
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_POSITIVE;
                return ZERO_OR_NEGATIVE;
            }
        }
        if(operator instanceof DivisionOperator){
            if(left == NEGATIVE)
                return right.negate();

            if(left == POSITIVE)
                return right;

            if(left == ZERO)
                return ZERO;

            if(left == ZERO_OR_POSITIVE){
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_NEGATIVE;
                return ZERO_OR_POSITIVE;
            }
            if(left == ZERO_OR_NEGATIVE) {
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_POSITIVE;
                return ZERO_OR_NEGATIVE;
            }
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
    public ExtSignDomain variable(Identifier id, ProgramPoint pp) throws SemanticException {
        return super.variable(id, pp);
    }

}
