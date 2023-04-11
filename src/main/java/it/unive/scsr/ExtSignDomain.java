package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.operator.AdditionOperator;
import it.unive.lisa.symbolic.value.operator.DivisionOperator;
import it.unive.lisa.symbolic.value.operator.MultiplicationOperator;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;


public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {

    private final int value;

    public static ExtSignDomain top = new ExtSignDomain(10);
    public static ExtSignDomain bottom = new ExtSignDomain(-10);
    public static ExtSignDomain positive = new ExtSignDomain(1);
    public static ExtSignDomain negative = new ExtSignDomain(-1);
    public static ExtSignDomain zero = new ExtSignDomain(0);


    public ExtSignDomain(int value) {
        this.value = value;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + value;
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

        return value == other.value;

    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException {
        if (this == other) return this;
        else return top();
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            Integer i = (Integer) constant.getValue();
            if (i == 0)
                return zero;
            else if (i > 0)
                return positive;
            else return negative;
        }
        return super.evalNonNullConstant(constant, pp);
    }

        private ExtSignDomain negate() {
            if (this == negative)
                return positive;
            else if (this == positive)
                return negative;
            else
                return this;
        }


    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if (operator instanceof NumericNegation)
            return arg.negate();

        return super.evalUnaryExpression(operator, arg, pp);
    }
    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator) {
            if (left == negative) {
                if (right == zero || right == negative)
                    return left;
                else
                    return right;
            } else if (left == positive) {
                if (right == zero || right == positive)
                    return left;
                else
                    return right;
            } else if (left == zero) {
                return right;
            } else
                return top;
        } else if (operator instanceof SubtractionOperator) {
            if (left == negative) {
                if (right == zero || right == positive)
                    return left;
                else
                    return top;
            } else if (left == positive) {
                if (right == zero || right == negative)
                    return left;
                else
                    return top;
            } else if (left == zero) {
                return right;
            } else
                return top;
        } else if (operator instanceof MultiplicationOperator) {
            if (left == negative) {
                return right.negate();
            } else if (left == positive) {
                return right;
            } else if (left == zero) {
                return zero;
            } else
                return top;
        } else if (operator instanceof DivisionOperator) {
            if (right == zero)
                return bottom;

            if (left == negative) {
                return right.negate();
            } else if (left == positive) {
                return right;
            } else if (left == zero) {
                return zero;
            } else
                return top;
        }


        return super.evalBinaryExpression(operator, left, right, pp);
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        return this == other;
    }

    @Override
    public DomainRepresentation representation() {

        if (this == top)
            return Lattice.topRepresentation();
        if (this == bottom)
            return Lattice.bottomRepresentation();
        if (this == positive)
            return new StringRepresentation("+");
        if (this == negative)
            return new StringRepresentation("-");
        return new StringRepresentation("0");

    }


    @Override
    public ExtSignDomain top() {
        return top;
    }

    @Override
    public ExtSignDomain bottom() {
        return bottom;
    }

}
