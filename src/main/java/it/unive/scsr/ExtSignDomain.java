package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;

import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

import java.util.Objects;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private final Sign sign;

    public ExtSignDomain() {
        this(Sign.TOP);
    }
    public ExtSignDomain(Sign sign) {
        this.sign = sign;
    }

    enum Sign {
        BOTTOM, NEGATIVE, ZERO_OR_NEGATIVE, ZERO, ZERO_OR_POSITIVE, POSITIVE, TOP;
    }

    @Override
    public ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        if (this.sign != null && other.sign != null) {
            return this.lubAux(other);
        } else {
            throw new SemanticException("Some objects are null");
        }
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0) return new ExtSignDomain(Sign.POSITIVE);
            else if (v == 0) return new ExtSignDomain(Sign.ZERO);
            else return new ExtSignDomain(Sign.NEGATIVE);
        }
        return top();
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this.sign != null && other.sign != null) {
            switch (this.sign) {
                case NEGATIVE:
                    if (other.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO_OR_NEGATIVE:
                    if (other.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO:
                    if (other.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (other.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (other.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    break;
                case ZERO_OR_POSITIVE:
                    if (other.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (other.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    break;
                case POSITIVE:
                    if (other.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (other.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    break;
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else { throw new SemanticException("Some objects are null");}
        return this.top();
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this.sign != null && other.sign != null) {
            if (this.sign == Sign.POSITIVE || this.sign == Sign.ZERO || this.sign == Sign.NEGATIVE) {
                return true;
            } else if (this.sign == Sign.ZERO_OR_POSITIVE || this.sign == Sign.ZERO_OR_NEGATIVE) {
                if (other.sign == Sign.ZERO_OR_NEGATIVE || other.sign == Sign.ZERO_OR_POSITIVE) {
                    return true;
                }
            }
            return false;
        }else {
            throw new SemanticException("Some objects are null");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        ExtSignDomain other = (ExtSignDomain) obj;
        return this.sign == other.sign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sign);
    }

    @Override
    public DomainRepresentation representation() {

        return new StringRepresentation(this.sign);
    }

    @Override
    public ExtSignDomain top() {
        return new ExtSignDomain(Sign.TOP);
    }

    @Override
    public ExtSignDomain bottom() {
        return new ExtSignDomain(Sign.BOTTOM);
    }

    private ExtSignDomain negate() {
        if (this.sign == Sign.NEGATIVE) {
            return new ExtSignDomain(Sign.POSITIVE);
        } else if (this.sign == Sign.POSITIVE) {
            return new ExtSignDomain(Sign.NEGATIVE);
        } else if (this.sign == Sign.ZERO_OR_NEGATIVE) {
            return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
        } else if (this.sign == Sign.ZERO_OR_POSITIVE) {
            return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
        } else {
            return this;
        }
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator oper, ExtSignDomain esd, ProgramPoint pp) throws SemanticException {
        if (oper instanceof NumericNegation) {
            return esd.negate();
        } else {
            return top();
        }
    }

    public ExtSignDomain evalBinaryExpression(BinaryOperator oper, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (oper instanceof AdditionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.TOP || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_NEGATIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.TOP || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.TOP);
                case POSITIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_POSITIVE || right.sign == Sign.POSITIVE)
                        return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_POSITIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else if (oper instanceof SubtractionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_NEGATIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_POSITIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.TOP || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.TOP);
                case POSITIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.TOP || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.TOP);
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else if (oper instanceof MultiplicationOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case POSITIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO_OR_NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO_OR_POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    return new ExtSignDomain(Sign.ZERO);
                case ZERO_OR_POSITIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_NEGATIVE:
                    if (right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    if (right.sign == Sign.TOP || right.sign == Sign.NEGATIVE || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.POSITIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
            }
        } else if (oper instanceof DivisionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case POSITIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.NEGATIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE || right.sign == Sign.TOP) return new ExtSignDomain(Sign.ZERO);
                case ZERO_OR_NEGATIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_OR_POSITIVE:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.ZERO_OR_NEGATIVE);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE) return new ExtSignDomain(Sign.ZERO_OR_POSITIVE);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    if (right.sign == Sign.NEGATIVE) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_OR_NEGATIVE || right.sign == Sign.ZERO_OR_POSITIVE)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.POSITIVE || right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);

            }
        }
        return this.top();
    }

        // IMPLEMENTATION NOTE:
	// you can follow the same logic of Signs to implement representation().
	// note that this is not mandatory: you can have any other logic instead
	// of constant fields, and you can change the logic in representation()
	// accordingly. the only constraint is that the strings used to represent
	// the elements stay the same (0, +, -, 0+, 0-)
}
