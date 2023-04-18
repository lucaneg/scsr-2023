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

    private static final ExtSignDomain TOP = new ExtSignDomain(10);
    private static final ExtSignDomain BOT = new ExtSignDomain(-10);
    private static final ExtSignDomain POS = new ExtSignDomain(5);
    private static final ExtSignDomain NEG = new ExtSignDomain(-5);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain ZERO_POS = new ExtSignDomain(1);
    private static final ExtSignDomain ZERO_NEG = new ExtSignDomain(-1);

    private final int sign;

    public ExtSignDomain() {
        this(10);
    }

    private ExtSignDomain(int e) {
        this.sign = e;
    }

    @Override
    public ExtSignDomain top() {
        return TOP;
    }

    @Override
    public ExtSignDomain bottom() {
        return BOT;
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this == ZERO_POS || this == ZERO_NEG) {
            return false;
        }
        if (this == POS) {
            return other == ZERO_POS;
        }
        if (this == NEG) {
            return other == ZERO_NEG;
        }
        if (this == ZERO) {
            return other == ZERO_POS || other == ZERO_NEG;
        }
        return false;
    }


    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this == NEG) {
            if (other == ZERO || other == ZERO_NEG) {
                return ZERO_NEG;
            }
            return TOP;
        }
        if (this == POS) {
            if (other == ZERO || other == ZERO_POS) {
                return ZERO_POS;
            }
            return TOP;
        }

        if (this == ZERO) {
            if (other == POS || other == ZERO_POS) {
                return ZERO_POS;
            }
            if (other == NEG || other == ZERO_NEG) {
                return ZERO_NEG;
            }
        }

        if (this == ZERO_NEG) {
            if (other == POS) {
                return TOP;
            }
            return ZERO_NEG;
        }
        if (this == ZERO_POS) {
            if (other == NEG) {
                return TOP;
            }
            return ZERO_POS;
        }
        return TOP;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ExtSignDomain other = (ExtSignDomain) obj;
        return sign == other.sign;
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
        if (this == TOP) {
            return Lattice.topRepresentation();
        }
        if (this == BOT) {
            return Lattice.bottomRepresentation();
        }
        if (this == POS) {
            return new StringRepresentation("+");
        }
        if (this == NEG) {
            return new StringRepresentation("-");
        }
        if (this == ZERO) {
            return new StringRepresentation("0");
        }
        if (this == ZERO_POS) {
            return new StringRepresentation("0+");
        }
        return new StringRepresentation("0-");
    }

    private ExtSignDomain negate() {
        if (this == NEG)
            return POS;
        if (this == POS)
            return NEG;
        if (this == ZERO_NEG)
            return ZERO_POS;
        if (this == ZERO_POS)
            return ZERO_NEG;
        return this;
    }


    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int e = (Integer) constant.getValue();
            if (e > 0) {
                return POS;
            } else if (e == 0) {
                return ZERO;
            } else
                return NEG;
        }
        return TOP;
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();
        return TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            return evalAddition(left, right);
        } else if (operator instanceof SubtractionOperator) {
            return evalSubtraction(left, right);
        } else if (operator instanceof MultiplicationOperator) {
            return evalMultiplication(left, right);
        } else if (operator instanceof DivisionOperator) {
            return evalDivision(left, right);
        }
        return super.evalBinaryExpression(operator, left, right, pp);
    }


    private ExtSignDomain evalAddition(ExtSignDomain left, ExtSignDomain right) {
        if (left == NEG) {
            if (right == ZERO || right == NEG || right == ZERO_NEG)
                return left;
        }
        if (left == POS) {
            if (right == ZERO || right == POS || right == ZERO_POS)
                return left;
        }
        if (left == ZERO_NEG) {
            if (right == ZERO || right == ZERO_NEG)
                return left;
            if (right == NEG)
                return right;
        }
        if (left == ZERO_POS) {
            if (right == ZERO || right == ZERO_POS)
                return left;
            if (right == POS)
                return right;
        }
        if (left == ZERO) {
            if (right == ZERO || right == POS || right == ZERO_POS || right == ZERO_NEG || right == NEG)
                return right;
        }
        return TOP;
    }

    private ExtSignDomain evalSubtraction(ExtSignDomain left, ExtSignDomain right) {
        return evalAddition(left, right.negate());
    }

    private ExtSignDomain evalMultiplication(ExtSignDomain left, ExtSignDomain right) {
        if (left == ZERO || right == ZERO)
            return ZERO;
        if (left == NEG)
            return right.negate();
        if (left == POS) {
            return right;
        }
        if (left == ZERO_NEG || left == ZERO_POS) {
            if (right == POS)
                return left;
            if (right == NEG)
                return left.negate();
            if (right == ZERO_POS)
                return left;
            if (right == ZERO_NEG)
                return left.negate();
        }
        return TOP;
    }

    private ExtSignDomain evalDivision(ExtSignDomain left, ExtSignDomain right) {
        if (right == ZERO || right == ZERO_NEG || right == ZERO_POS)
            return BOT;
        return evalMultiplication(left, right);
    }
}