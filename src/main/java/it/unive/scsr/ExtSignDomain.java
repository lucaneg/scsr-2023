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
        BOTTOM, MINUS, ZERO_MINUS, ZERO, ZERO_PLUS, PLUS, TOP;
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
            if (v > 0) return new ExtSignDomain(Sign.PLUS);
            else if (v == 0) return new ExtSignDomain(Sign.ZERO);
            else return new ExtSignDomain(Sign.MINUS);
        }
        return top();
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this.sign != null && other.sign != null) {
            switch (this.sign) {
                case MINUS:
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO_MINUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.TOP);
                    break;
                case ZERO:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    break;
                case ZERO_PLUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    break;
                case PLUS:
                    if (other.sign == Sign.MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (other.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (other.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
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
            if (this.sign == Sign.PLUS || this.sign == Sign.ZERO || this.sign == Sign.MINUS) {
                return true;
            } else if (this.sign == Sign.ZERO_PLUS || this.sign == Sign.ZERO_MINUS) {
                if (other.sign == Sign.ZERO_MINUS || other.sign == Sign.ZERO_PLUS) {
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
        if (this.sign == Sign.MINUS) {
            return new ExtSignDomain(Sign.PLUS);
        } else if (this.sign == Sign.PLUS) {
            return new ExtSignDomain(Sign.MINUS);
        } else if (this.sign == Sign.ZERO_MINUS) {
            return new ExtSignDomain(Sign.ZERO_PLUS);
        } else if (this.sign == Sign.ZERO_PLUS) {
            return new ExtSignDomain(Sign.ZERO_MINUS);
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
                case MINUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.TOP || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.TOP);
                case ZERO_MINUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.TOP || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.TOP);
                case PLUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_PLUS || right.sign == Sign.PLUS)
                        return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_PLUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else if (oper instanceof SubtractionOperator) {
            switch (left.sign) {
                case MINUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_MINUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_PLUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.TOP || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.TOP);
                case PLUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.TOP || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.TOP);
                case TOP:
                    return new ExtSignDomain(Sign.TOP);
            }
        } else if (oper instanceof MultiplicationOperator) {
            switch (left.sign) {
                case MINUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case PLUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO_MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO_PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    return new ExtSignDomain(Sign.ZERO);
                case ZERO_PLUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_MINUS:
                    if (right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS)
                        return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    if (right.sign == Sign.TOP || right.sign == Sign.MINUS || right.sign == Sign.ZERO_MINUS || right.sign == Sign.PLUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO) return new ExtSignDomain(Sign.ZERO);
            }
        } else if (oper instanceof DivisionOperator) {
            switch (left.sign) {
                case MINUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case PLUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.MINUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS || right.sign == Sign.TOP) return new ExtSignDomain(Sign.ZERO);
                case ZERO_MINUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case ZERO_PLUS:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.ZERO_MINUS);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS) return new ExtSignDomain(Sign.ZERO_PLUS);
                    if (right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);
                case TOP:
                    if (right.sign == Sign.MINUS) return new ExtSignDomain(Sign.TOP);
                    if (right.sign == Sign.ZERO || right.sign == Sign.ZERO_MINUS || right.sign == Sign.ZERO_PLUS)
                        return new ExtSignDomain(Sign.BOTTOM);
                    if (right.sign == Sign.PLUS || right.sign == Sign.TOP) return new ExtSignDomain(Sign.TOP);

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
