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

    private enum ExtSign {
        BOTTOM, NEGATIVE, POSITIVE, ZERO, ZERO_OR_NEGATIVE, ZERO_OR_POSITIVE, TOP
    }

    private final ExtSign sign;

    /**
     * Safe constructor with TOP
     */
    public ExtSignDomain() {
        // current state to TOP
        this.sign = ExtSign.TOP;
    }

    private ExtSignDomain(ExtSign sign) {
        this.sign = sign;
    }


    /**
     * Performs the least upper bound operation between this lattice element and the given one
     */
    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this.lessOrEqual(other)) {
            return other;
        } else if (other.lessOrEqual(this)) {
            return this;
        } else if (this.isZero()) { // this.sign == ExtSign.ZERO
            if (other.isPositive()) { // this.sign == ExtSign.POSITIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_POSITIVE); // return ZERO_OR_POSITIVE
            } else if (other.isNegative()) { // other.sign == ExtSign.NEGATIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_NEGATIVE); // return ZERO_OR_NEGATIVE
            }
        } else if (other.isZero()) { // other.sign == ExtSign.ZERO
            if (this.isPositive()) { // this.sign == ExtSign.POSITIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_POSITIVE); // return ZERO_OR_POSITIVE
            } else if (this.isNegative()) { // this.sign == ExtSign.NEGATIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_NEGATIVE); // return ZERO_OR_NEGATIVE
            }
        }
        return top();
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return switch (this.sign) {
            case NEGATIVE -> other.isZeroOrNegative(); // other.sign == ExtSign.ZERO_OR_NEGATIVE;
            case POSITIVE -> other.isZeroOrPositive(); // other.sign == ExtSign.ZERO_OR_POSITIVE;
            // other.sign == ExtSign.ZERO_OR_POSITIVE || other.sign == ExtSign.ZERO_OR_NEGATIVE;
            case ZERO -> other.isZeroOrPositive() || other.isZeroOrNegative();
            default -> false;
        };
    }

    @Override
    public ExtSignDomain top() {
        // the top element of the lattice
        // if this method does not return a constant value,
        // you must override the isTop() method!
        return new ExtSignDomain(ExtSign.TOP);
    }

    @Override
    public ExtSignDomain bottom() {
        // the bottom element of the lattice
        // if this method does not return a constant value,
        // you must override the isBottom() method!
        return new ExtSignDomain(ExtSign.BOTTOM);
    }

    @Override
    public boolean isTop() {
        return this.sign == ExtSign.TOP;
    }

    @Override
    public boolean isBottom() {
        return this.sign == ExtSign.BOTTOM;
    }

    public boolean isNegative() {
        return this.sign == ExtSign.NEGATIVE;
    }

    public boolean isZero() {
        return this.sign == ExtSign.ZERO;
    }

    public boolean isPositive() {
        return this.sign == ExtSign.POSITIVE;
    }

    public boolean isZeroOrNegative() {
        return this.sign == ExtSign.ZERO_OR_NEGATIVE;
    }

    public boolean isZeroOrPositive() {
        return this.sign == ExtSign.ZERO_OR_POSITIVE;
    }

    /**
     * Return the negation of the current sign
     */
    private ExtSignDomain getNegation() {
        return switch (this.sign) {
            case POSITIVE -> new ExtSignDomain(ExtSign.NEGATIVE);
            case NEGATIVE -> new ExtSignDomain(ExtSign.POSITIVE);
            case ZERO_OR_NEGATIVE -> new ExtSignDomain(ExtSign.ZERO_OR_POSITIVE);
            case ZERO_OR_POSITIVE -> new ExtSignDomain(ExtSign.ZERO_OR_NEGATIVE);
            default -> this; // No negate for ZERO, TOP, BOTTOM values
        };
    }

    /**
     * Eval a non-null constant compatible with the domain of extended signs
     */
    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int value = (Integer) constant.getValue();
            if (value > 0)
                return new ExtSignDomain(ExtSign.POSITIVE);
            else if (value == 0)
                return new ExtSignDomain(ExtSign.ZERO);
            else // value < 0
                return new ExtSignDomain(ExtSign.NEGATIVE);
        }
        return top();
    }

    /**
     * Eval a unary expression compatible with the domain of extended signs
     */
    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation) {
            return arg.getNegation();
        }
        return top();
    }

    /**
     * Eval a binary expression compatible with the domain of extended signs
     * For each operator (+, -, *, /), define how to handle left and right signs
     */
    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator) {
            if (operator instanceof SubtractionOperator) { // left - right = left + (-right)
                right = right.getNegation();
            }

            if (left.sign == right.sign) return left; // keep same sign

            if (left.isBottom() || right.isBottom()) return new ExtSignDomain(ExtSign.BOTTOM);  // return BOTTOM

            if (left.isNegative() && (right.isZero() || right.isZeroOrNegative())) return left; // return NEGATIVE
            if (right.isNegative() && (left.isZero() || left.isZeroOrNegative())) return right; // return NEGATIVE

            if (left.isPositive() && (right.isZero() || right.isZeroOrPositive())) return left; // return POSITIVE
            if (right.isPositive() && (left.isZero() || left.isZeroOrPositive())) return right; // return POSITIVE

            if (left.isZero() && right.isZeroOrPositive()) return right; // return ZERO_OR_POSITIVE
            if (left.isZeroOrPositive() && right.isZero()) return left;  // return ZERO_OR_POSITIVE

            if (left.isZero() && right.isZeroOrNegative()) return right; // return ZERO_OR_NEGATIVE
            if (left.isZeroOrNegative() && right.isZero()) return left;  // return ZERO_OR_NEGATIVE

            // otherwise return TOP

        } else if (operator instanceof MultiplicationOperator) {

            if (left.isBottom() || right.isBottom()) return bottom(); // return BOTTOM

            if (left.isNegative()) return right.getNegation(); // NEGATIVE * right =  right negate
            if (right.isNegative()) return left.getNegation(); // left * NEGATIVE = left negate

            if (left.isPositive()) return right; // POSITIVE * right = right
            if (right.isPositive()) return left; // left * POSITIVE = left

            if (left.isZero()) return left;   // ZERO * right = ZERO
            if (right.isZero()) return right; // left * ZERO = ZERO

            if ((left.isZeroOrNegative() && right.isZeroOrNegative()) ||    // ZERO_OR_NEGATIVE * ZERO_OR_NEGATIVE = ZERO_OR_POSITIVE
                    (left.isZeroOrPositive() && right.isZeroOrPositive()))  // ZERO_OR_POSITIVE * ZERO_OR_POSITIVE = ZERO_OR_POSITIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_POSITIVE);

            if ((left.isZeroOrNegative() && right.isZeroOrPositive()) ||    // ZERO_OR_NEGATIVE * ZERO_OR_POSITIVE = ZERO_OR_NEGATIVE
                    (left.isZeroOrPositive() && right.isZeroOrNegative()))  // ZERO_OR_POSITIVE * ZERO_OR_NEGATIVE = ZERO_OR_NEGATIVE
                return new ExtSignDomain(ExtSign.ZERO_OR_NEGATIVE);

            // otherwise return TOP

        } else if (operator instanceof DivisionOperator) {

            if (left.isBottom() || right.isBottom()) return bottom(); // return BOTTOM
            if (right.isZero()) return bottom(); // BOTTOM (division by zero!)

            if (left.isZero()) return left; // ZERO / right = ZERO (excluded previously excluded cases)

            if (right.isNegative() || right.isZeroOrNegative())
                // left / NEGATIVE = left negate, and
                // left / ZERO_OR_NEGATIVE = left  negate
                return left.getNegation();

            if (right.isPositive() || right.isZeroOrPositive())
                // left / NEGATIVE = left negate, and
                // left / ZERO_OR_NEGATIVE = left  negate
                return left;

            // otherwise return TOP

        }
        return top();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.sign.ordinal();
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


    // IMPLEMENTATION NOTE:
    // you can follow the same logic of Signs to implement representation().
    // note that this is not mandatory: you can have any other logic instead
    // of constant fields, and you can change the logic in representation()
    // accordingly. the only constraint is that the strings used to represent
    // the elements stay the same (0, +, -, 0+, 0-)

    @Override
    public DomainRepresentation representation() {
        return switch (this.sign) {
            case BOTTOM -> Lattice.bottomRepresentation();
            case NEGATIVE -> new StringRepresentation("-");
            case POSITIVE -> new StringRepresentation("+");
            case ZERO -> new StringRepresentation("0");
            case ZERO_OR_NEGATIVE -> new StringRepresentation("0-");
            case ZERO_OR_POSITIVE -> new StringRepresentation("0+");
            case TOP -> Lattice.topRepresentation();
        };
    }
}