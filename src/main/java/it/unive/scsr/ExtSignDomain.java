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

/**
 * @author Musone Mattia (877962)
 * @version 1.0.0 (10/04/2023)
 * @see BaseNonRelationalValueDomain
 */
public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    /**
     * Sign used in this instance
     *
     * @see Sign
     */
    private final Sign sign;

    /**
     * enum representing the domain of extended sign
     */
    enum Sign {
        TOP, BOTTOM, POS, NEG, ZERO, ZERO_POS, ZERO_NEG
    }

    public ExtSignDomain() {
        this(Sign.TOP);
    }

    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    /**
     * We have to manage the new cases introduced performing the least upper bound operation between this lattice element and the given one
     */
    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this.lessOrEqual(other))
            return other;
        else if (other.lessOrEqual(this))
            return this;
        else if (this.sign == Sign.ZERO || other.sign == Sign.ZERO) {
            if (other.sign == Sign.POS || sign == Sign.POS)
                return new ExtSignDomain(Sign.ZERO_POS);
            else {
                if (other.sign == Sign.NEG || this.sign == Sign.NEG)
                    return new ExtSignDomain(Sign.ZERO_NEG);
            }
        }
        return top();
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return (sign == Sign.NEG && other.sign == Sign.ZERO_NEG) ||
                (sign == Sign.ZERO && (other.sign == Sign.ZERO_NEG || other.sign == Sign.ZERO_POS)) ||
                (sign == Sign.POS && other.sign == Sign.ZERO_POS);
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
        result = prime * result + sign.hashCode();
        return result;
    }

    @Override
    public ExtSignDomain top() {
        return new ExtSignDomain(Sign.TOP);
    }

    @Override
    public boolean isTop() {
        return sign == Sign.TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return new ExtSignDomain(Sign.BOTTOM);
    }

    @Override
    public boolean isBottom() {
        return sign == Sign.BOTTOM;
    }

    /**
     * Override the original method in order to represent the domain of extended signs
     *
     * @throws RuntimeException if there is a case that it was not implemented
     * @see Lattice
     * @see StringRepresentation
     */
    @Override
    public DomainRepresentation representation() throws RuntimeException {
        return switch (this.sign) {
            case TOP -> Lattice.topRepresentation();
            case BOTTOM -> Lattice.bottomRepresentation();
            case POS -> new StringRepresentation("+");
            case NEG -> new StringRepresentation("-");
            case ZERO -> new StringRepresentation("0");
            case ZERO_POS -> new StringRepresentation("0+");
            case ZERO_NEG -> new StringRepresentation("0-");
            default -> throw new RuntimeException("Sign provided is invalid: " + this.sign);
        };
    }

    /**
     * Override the original method in order to eval a non-null constant compatible with the domain of extended signs working with Integers
     *
     * @see Constant
     * @see ProgramPoint
     * @see Integer
     */
    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return new ExtSignDomain(Sign.POS);
            else if (v == 0)
                return new ExtSignDomain(Sign.ZERO);
            else
                return new ExtSignDomain(Sign.NEG);
        }
        return top();
    }

    /**
     * Override the original method in order to eval a unary expression compatible with the domain of extended signs.
     * In this case only the NumericNegation, otherwise top
     *
     * @see UnaryOperator
     * @see ExtSignDomain
     * @see ProgramPoint
     * @see NumericNegation
     */
    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();
        else
            return top();
    }

    /**
     * Override the original method in order to eval a binary expression compatible with the domain of extended signs.
     * For each operation (-, +, /, *), define how to manage each case comparing the left and right sign
     *
     * @see BinaryOperator
     * @see ExtSignDomain
     * @see ProgramPoint
     * @see AdditionOperator
     * @see SubtractionOperator
     * @see MultiplicationOperator
     * @see DivisionOperator
     */
    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator) {
            if (operator instanceof SubtractionOperator)
                right = right.negate();
            if (left.sign == Sign.BOTTOM || right.sign == Sign.BOTTOM)
                return bottom();
            else if ((right.sign == Sign.TOP && left.sign != Sign.ZERO && left.sign != Sign.BOTTOM) ||
                    (left.sign == Sign.TOP && right.sign != Sign.ZERO && right.sign != Sign.BOTTOM))
                return top();
            if (left.sign == Sign.NEG) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.NEG);
                else return top();
            } else if (left.sign == Sign.POS) {
                if (right.sign == Sign.POS || right.sign == Sign.ZERO || right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.POS);
                else return top();
            } else if (left.sign == Sign.ZERO) {
                return new ExtSignDomain(right.sign);
            } else if (left.sign == Sign.ZERO_NEG) {
                if (right.sign == Sign.POS || right.sign == Sign.ZERO_POS)
                    return top();
                else if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.ZERO_NEG);
                else if (right.sign == Sign.NEG)
                    return new ExtSignDomain(Sign.NEG);
            } else if (left.sign == Sign.ZERO_POS) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO_NEG)
                    return top();
                else if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.ZERO_POS);
                else if (right.sign == Sign.POS)
                    return new ExtSignDomain(Sign.POS);
            }
        } else if (operator instanceof MultiplicationOperator) {
            if (left.sign == Sign.BOTTOM || right.sign == Sign.BOTTOM)
                return bottom();
            else if ((right.sign == Sign.TOP && left.sign != Sign.ZERO && left.sign != Sign.BOTTOM) ||
                    (left.sign == Sign.TOP && right.sign != Sign.ZERO && right.sign != Sign.BOTTOM))
                return top();
            else if (left.sign == Sign.NEG)
                return right.negate();
            else if (left.sign == Sign.POS)
                return new ExtSignDomain(right.sign);
            else if (left.sign == Sign.ZERO || right.sign == Sign.ZERO)
                return new ExtSignDomain(Sign.ZERO);
            else if (left.sign == Sign.ZERO_NEG) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.ZERO_POS);
                else return new ExtSignDomain(Sign.ZERO_NEG);
            } else if (left.sign == Sign.ZERO_POS) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.ZERO_NEG);
                else return new ExtSignDomain(Sign.ZERO_POS);
            }
        } else if (operator instanceof DivisionOperator) {
            if (left.sign == Sign.BOTTOM || right.sign == Sign.BOTTOM)
                return bottom();
            else if ((right.sign == Sign.TOP && left.sign != Sign.ZERO && left.sign != Sign.BOTTOM) ||
                    (left.sign == Sign.TOP && right.sign != Sign.ZERO && right.sign != Sign.BOTTOM))
                return new ExtSignDomain(Sign.TOP);
            else if (left.sign == Sign.ZERO) {
                if (right.sign == Sign.ZERO)
                    return new ExtSignDomain(Sign.ZERO);
                else
                    return bottom();
            } else if (left.sign == Sign.NEG) {
                if (right.sign == Sign.NEG || right.sign == Sign.POS)
                    return right.negate();
                else if (right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.POS);
                else if (right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.NEG);
                else if (right.sign == Sign.ZERO)
                    return new ExtSignDomain(Sign.ZERO);
            } else if (left.sign == Sign.POS) {
                if (right.sign == Sign.NEG || right.sign == Sign.POS)
                    return right.negate();
                else if (right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.NEG);
                else if (right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.POS);
                else if (right.sign == Sign.ZERO)
                    return new ExtSignDomain(Sign.ZERO);
            } else if (left.sign == Sign.ZERO_NEG) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.ZERO_POS);
                else if (right.sign == Sign.POS || right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.ZERO_NEG);
                else if (right.sign == Sign.ZERO)
                    return bottom();
            } else if (left.sign == Sign.ZERO_POS) {
                if (right.sign == Sign.NEG || right.sign == Sign.ZERO_NEG)
                    return new ExtSignDomain(Sign.ZERO_NEG);
                else if (right.sign == Sign.POS || right.sign == Sign.ZERO_POS)
                    return new ExtSignDomain(Sign.ZERO_POS);
                else if (right.sign == Sign.ZERO)
                    return bottom();
            }
        }
        return top();
    }

    /**
     * Function that computes the opposite sign of the one of the current context
     *
     * @return the opposite sign
     * @throws RuntimeException if there is a case that it was not implemented
     */
    private ExtSignDomain negate() throws RuntimeException {
        return switch (this.sign) {
            case TOP -> new ExtSignDomain(Sign.TOP);
            case BOTTOM -> new ExtSignDomain(Sign.BOTTOM);
            case POS -> new ExtSignDomain(Sign.NEG);
            case NEG -> new ExtSignDomain(Sign.POS);
            case ZERO -> new ExtSignDomain(Sign.ZERO);
            case ZERO_POS -> new ExtSignDomain(Sign.ZERO_NEG);
            case ZERO_NEG -> new ExtSignDomain(Sign.ZERO_POS);
            default -> throw new RuntimeException("Cannot negate unhandled case: " + this.sign);
        };
    }
}
