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

public class ExtSignDomain extends BaseNonRelationalValueDomain <ExtSignDomain> {

	private final ExtSign sign;

	private static final ExtSignDomain TOP =  new ExtSignDomain(ExtSignDomain.ExtSign.TOP);
    private static final ExtSignDomain BOTTOM = new  ExtSignDomain(ExtSignDomain.ExtSign.BOTTOM);
    private static final ExtSignDomain ZERO = new ExtSignDomain(ExtSignDomain.ExtSign.ZERO);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new  ExtSignDomain(ExtSignDomain.ExtSign.ZERO_OR_NEGATIVE);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(ExtSignDomain.ExtSign.ZERO_OR_POSITIVE);
    private static final ExtSignDomain NEGATIVE = new  ExtSignDomain(ExtSignDomain.ExtSign.NEGATIVE);
    private static final ExtSignDomain POSITIVE = new  ExtSignDomain(ExtSignDomain.ExtSign.POSITIVE);
    
	enum ExtSign {
        BOTTOM, ZERO, ZERO_OR_NEGATIVE , ZERO_OR_POSITIVE , NEGATIVE, POSITIVE, TOP;
    }
	
	public ExtSignDomain() {
		this.sign = Extsign.TOP;
	}

	public ExtSignDomain(ExtSign sign) {
		this.sign = sign;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sign == null) ? 0 : sign.hashCode());
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
		ExtSignDomainSolution other = (ExtSignDomainSolution) obj;
		if (sign != other.sign)
			return false;
		return true;
	}

	@Override
    public ExtSignDomain top() {
        return ExtSignDomain.TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return ExtSignDomain.BOTTOM;
    }

	public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            switch (this.sign) {
                case NEGATIVE:
                    if (other.sign == ExtSign.ZERO_OR_NEGATIVE) return ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO) return ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO_OR_POSITIVE) return  ExtSignDomain.TOP;
                    if (other.sign == ExtSign.POSITIVE) return  ExtSignDomain.TOP;
                    break;
                case ZERO_OR_NEGATIVE:
                    if (other.sign == ExtSign.NEGATIVE) return  ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO) return  ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO_OR_POSITIVE) return  ExtSignDomain.TOP;
                    if (other.sign == ExtSign.POSITIVE) return  ExtSignDomain.TOP;
                    break;
                case ZERO:
                    if (other.sign == ExtSign.NEGATIVE) return  ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO_OR_NEGATIVE) return  ExtSignDomain.ZERO_OR_NEGATIVE;
                    if (other.sign == ExtSign.ZERO_OR_POSITIVE) return  ExtSignDomain.ZERO_OR_POSITIVE;
                    if (other.sign == ExtSign.POSITIVE) return  ExtSignDomain.ZERO_OR_POSITIVE;
                    break;
                case ZERO_OR_POSITIVE:
                    if (other.sign == ExtSign.NEGATIVE) return ExtSignDomain.TOP;
                    if (other.sign == ExtSign.ZERO_OR_NEGATIVE) return ExtSignDomain.TOP;
                    if (other.sign == ExtSign.ZERO) return ExtSignDomain.ZERO_OR_POSITIVE;
                    if (other.sign == ExtSign.POSITIVE) return ExtSignDomain.ZERO_OR_POSITIVE;
                    break;
                case POSITIVE:
                    if (other.sign == ExtSign.NEGATIVE) return  ExtSignDomain.TOP;
                    if (other.sign == ExtSign.ZERO_OR_NEGATIVE) return  ExtSignDomain.TOP;
                    if (other.sign == ExtSign.ZERO) return ExtSignDomain.ZERO_OR_POSITIVE;
                    if (other.sign == ExtSign.ZERO_OR_POSITIVE) return ExtSignDomain.ZERO_OR_POSITIVE;
                    break;
            }
        } else {
            throw new SemanticException("Some objects may be null");
        }
        return this.top();
    }

	@Override
    public ExtSignDomain wideningAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            return this.lubAux(other);
        } else {
            throw new SemanticException("Some objects may be null");
        }
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0) return ExtSignDomain.POSITIVE;
            else if (v == 0) return ExtSignDomain.ZERO;
            else return ExtSignDomain.NEGATIVE;
        }
        return top();
    }


    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        /*for javadoc other is not null but we don't know if other.sign is null or not, same thing for this*/
        if (this.sign != null && other.sign != null) {
            if (this.sign == ExtSign.POSITIVE || this.sign == ExtSign.ZERO || this.sign == ExtSign.NEGATIVE) {
                return true;
            } else if (this.sign == ExtSign.ZERO_OR_POSITIVE || this.sign == ExtSign.ZERO_OR_NEGATIVE) {
                if (other.sign == ExtSign.ZERO_OR_NEGATIVE || other.sign == ExtSign.ZERO_OR_POSITIVE) {
                    return true;
                }
            }
            return false;
        } else {
            throw new SemanticException("Some objects may be null");
        }
    }

    private ExtSignDomain negate() {
        if (this.sign == ExtSign.NEGATIVE) {
            return ExtSignDomain.POSITIVE;
        } else if (this.sign == ExtSign.POSITIVE) {
            return ExtSignDomain.NEGATIVE;
        } else if (this.sign == ExtSign.ZERO_OR_NEGATIVE) {
            return ExtSignDomain.ZERO_OR_POSITIVE;
        } else if (this.sign == ExtSign.ZERO_OR_POSITIVE) {
            return ExtSignDomain.ZERO_OR_NEGATIVE;
        } else {
            return this;
        }
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation) {
            return arg.negate();
        } else {
            return top();
        }
    }

	@Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                            return ExtSignDomain.NEGATIVE;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                            return ExtSignDomain.POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO:
                    switch (right.sign) {
                        case NEGATIVE:
                            return  ExtSignDomain.NEGATIVE;
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case POSITIVE:
                            return ExtSignDomain.POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case POSITIVE:
                            return ExtSignDomain.POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.NEGATIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case TOP:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
            }
        } else if (operator instanceof SubtractionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                            return ExtSignDomain.NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                            return ExtSignDomain.POSITIVE;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.POSITIVE;
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case POSITIVE:
                            return ExtSignDomain.NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.POSITIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case POSITIVE:
                            return ExtSignDomain.NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case TOP:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
            }
        } else if (operator instanceof MultiplicationOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.POSITIVE;
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case POSITIVE:
                            return ExtSignDomain.NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.NEGATIVE;
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case POSITIVE:
                            return ExtSignDomain.POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.ZERO;
                    }
                case ZERO_OR_POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case TOP:
                    switch (right.sign) {
                        case NEGATIVE:
                        case ZERO_OR_NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_POSITIVE:
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
            }
        } else if (operator instanceof DivisionOperator) {
            switch (left.sign) {
                case NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.POSITIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                            return ExtSignDomain.NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.NEGATIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                            return ExtSignDomain.POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.ZERO;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.ZERO;
                    }
                case ZERO_OR_POSITIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case ZERO_OR_NEGATIVE:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.ZERO_OR_POSITIVE;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                            return ExtSignDomain.ZERO_OR_NEGATIVE;
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
                case TOP:
                    switch (right.sign) {
                        case NEGATIVE:
                            return ExtSignDomain.TOP;
                        case ZERO_OR_NEGATIVE:
                        case ZERO:
                        case ZERO_OR_POSITIVE:
                            return ExtSignDomain.BOTTOM;
                        case POSITIVE:
                        case TOP:
                            return ExtSignDomain.TOP;
                    }
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
}
