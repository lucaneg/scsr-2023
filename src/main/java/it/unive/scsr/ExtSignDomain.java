package it.unive.scsr;
import java.util.Objects;
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
    private static final ExtSignDomain TOP = new ExtSignDomain(Sign.TOP);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(Sign.BOTTOM);

    private enum Sign {
        BOTTOM, NEGATIVE, POSITIVE, ZERO, ZERO_OR_NEGATIVE, ZERO_OR_POSITIVE, TOP
    }

    // IMPLEMENTATION NOTE:
    // you can follow the same logic of Signs to implement representation().
    // note that this is not mandatory: you can have any other logic instead
    // of constant fields, and you can change the logic in representation()
    // accordingly. the only constraint is that the strings used to represent
    // the elements stay the same (0, +, -, 0+, 0-)

    @Override
    public DomainRepresentation representation() {
        if (this.sign == Sign.TOP)
            return Lattice.topRepresentation();
        if (this.sign == Sign.BOTTOM)
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

    public ExtSignDomain() {
        this.sign = Sign.TOP;
    }

    public ExtSignDomain(Sign value) {
        this.sign = value;
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        switch (this.sign){
            case ZERO:
                switch(other.sign){
                    case NEGATIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    case POSITIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    case ZERO_OR_NEGATIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    case ZERO_OR_POSITIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    default: return top();
                }
            case NEGATIVE:
                switch (other.sign){
                    case ZERO:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    case ZERO_OR_NEGATIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    default: return top();
                }
            case POSITIVE:
                switch (other.sign){
                    case ZERO:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    case ZERO_OR_POSITIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    default: return top();
                }
            case ZERO_OR_NEGATIVE:
                switch (other.sign){
                    case ZERO:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    case NEGATIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    default: return top();
                }
            case ZERO_OR_POSITIVE:
                switch (other.sign){
                    case ZERO:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    case POSITIVE:
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    default: return top();
                }
            default: return top();
        }
    }

    @Override
    public ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        switch(this.sign){
            case NEGATIVE:
                switch(other.sign) {
                    case ZERO_OR_NEGATIVE:
                        return true;
                    case NEGATIVE:
                        return true;
                    default:
                        return false;
                }
            case POSITIVE:
                switch(other.sign) {
                    case ZERO_OR_POSITIVE:
                        return true;
                    case POSITIVE:
                        return true;
                    default:
                        return false;
                }
            case ZERO:
                switch(other.sign) {
                    case ZERO_OR_POSITIVE:
                        return true;
                    case ZERO_OR_NEGATIVE:
                        return true;
                    case ZERO:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExtSignDomain)) return false;
        ExtSignDomain that = (ExtSignDomain) obj;
        return sign == that.sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sign);
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }//gay

    @Override
    public ExtSignDomain bottom() {
        return BOTTOM;
    }

    private ExtSignDomain negate() {
        if (sign == Sign.NEGATIVE)
            return new ExtSignDomain(Sign.POSITIVE);
        else if (sign == Sign.POSITIVE)
            return new ExtSignDomain(Sign.NEGATIVE);
        else if (sign == Sign.ZERO_OR_POSITIVE)
            return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
        else if (sign == Sign.ZERO_OR_NEGATIVE)
            return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
        else
            return this;
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();
        return top();
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if(constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if (value > 0)
                return new ExtSignDomain(Sign.POSITIVE);
            else if (value == 0)
                return new ExtSignDomain(Sign.ZERO);
            else
                return new ExtSignDomain(Sign.NEGATIVE);
        }
        return top();
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if(operator instanceof AdditionOperator) {
            switch (left.sign) {
                case ZERO:
                    return right;
                case POSITIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.POSITIVE);
                        default: return top();
                    }
                case ZERO_OR_POSITIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        default: return top();
                    }
                case NEGATIVE:
                    switch(right.sign) {
                        case NEGATIVE: return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO:return new ExtSignDomain(Sign.NEGATIVE);
                        default: return top();
                    }
                case ZERO_OR_NEGATIVE:
                    switch(right.sign) {
                        case NEGATIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        default: return top();
                    }
                default: return top();
            }
        }
        if(operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case ZERO:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO);
                        default: return top();
                    }
                case POSITIVE:
                    switch(right.sign) {
                        case ZERO:return new ExtSignDomain(Sign.POSITIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        default: return top();
                    }
                case ZERO_OR_POSITIVE:
                    switch(right.sign) {
                        case NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        default: return top();
                    }
                case NEGATIVE:
                    switch(right.sign) {
                        case ZERO:return new ExtSignDomain(Sign.NEGATIVE);
                        case POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        default: return top();
                    }
                case ZERO_OR_NEGATIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        default: return top();
                    }
                default: return top();
            }
        }
        if(operator instanceof MultiplicationOperator) {
            switch (left.sign) {
                case ZERO:
                    return new ExtSignDomain(Sign.ZERO);
                case POSITIVE:
                    switch(right.sign) {
                        case NEGATIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case POSITIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO);
                        default: return top();
                    }
                case ZERO_OR_POSITIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO);
                        default: return top();
                    }
                case NEGATIVE:
                    switch(right.sign) {
                        case NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO);
                        case POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        default: return top();
                    }
                case ZERO_OR_NEGATIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:return new ExtSignDomain(Sign.ZERO);
                        default: return top();
                    }
                default: return top();
            }
        }
        if(operator instanceof DivisionOperator) {
            switch (left.sign) {
                case ZERO:
                    switch(right.sign) {
                        case ZERO:bottom();
                        default:return new ExtSignDomain(Sign.ZERO);
                    }
                case POSITIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:bottom();
                        case NEGATIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        default:return top();
                    }
                case ZERO_OR_POSITIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO:bottom();
                        default: return top();
                    }
                case NEGATIVE:
                    switch(right.sign) {
                        case NEGATIVE:return new ExtSignDomain(Sign.POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:bottom();
                        case POSITIVE:return new ExtSignDomain(Sign.NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        default: return top();
                    }
                case ZERO_OR_NEGATIVE:
                    switch(right.sign) {
                        case POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case ZERO_OR_POSITIVE:return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                        case NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO_OR_NEGATIVE:return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                        case ZERO:bottom();
                        default: return top();
                    }
                default: return top();
            }
        }
        return this.top();
    }
}