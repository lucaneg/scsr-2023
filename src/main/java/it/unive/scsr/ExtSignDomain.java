package it.unive.scsr;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
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
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain> {
    private static final ExtSignDomain TOP = new ExtSignDomain(10);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-10);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(5);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-5);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);

    private final Integer sign;

    /**
     * Default constructor creates a <b>safe</b> TOP element
     * */
    public ExtSignDomain(){
        this(10);
    }

    private ExtSignDomain(Integer v){
        this.sign = v;
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
        // just in case
        if (this == other)
            return this;

        if (this == NEGATIVE){ // can only go up to zero-or-negative and top
            if (other == ZERO || other == ZERO_OR_NEGATIVE)
                return ZERO_OR_NEGATIVE;
            return TOP;
        }
        if (this == POSITIVE){ // can only go up to zero-or-positive and top
            if (other == ZERO || other == ZERO_OR_POSITIVE)
                return ZERO_OR_POSITIVE;
            return TOP;
        }

        if (this == ZERO){ // can only go up to zero-or-positive and zero-or-negative, never top
            if (other == POSITIVE || other == ZERO_OR_POSITIVE)
                return ZERO_OR_POSITIVE;
            if(other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                return ZERO_OR_NEGATIVE;
        }

        if (this == ZERO_OR_NEGATIVE){ // can only go up to top or itself
            if (other == POSITIVE)
                return TOP;
            return ZERO_OR_NEGATIVE;
        }
        if(this == ZERO_OR_POSITIVE){ // can only go up to top or itself
            if(other == NEGATIVE)
                return TOP;
            return ZERO_OR_POSITIVE;
        }
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException {
        if (this == ZERO_OR_POSITIVE || this == ZERO_OR_NEGATIVE)
            return false;
        if (this == POSITIVE)
            return other == ZERO_OR_POSITIVE;
        if (this == NEGATIVE)
            return other == ZERO_OR_NEGATIVE;
        if (this == ZERO)
            return other == ZERO_OR_POSITIVE || other == ZERO_OR_NEGATIVE;
        return false;
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        if (constant.getValue() instanceof Integer) {
            int v = (Integer) constant.getValue();
            if (v > 0)
                return POSITIVE;
            else if (v == 0)
                return ZERO;
            else
                return NEGATIVE;
        }
        return TOP;
    }

    private ExtSignDomain getNegation(){
        if (this == POSITIVE)
            return NEGATIVE;
        if (this == NEGATIVE)
            return POSITIVE;
        if (this == ZERO_OR_POSITIVE)
            return ZERO_OR_NEGATIVE;
        if (this == ZERO_OR_NEGATIVE)
            return ZERO_OR_POSITIVE;
        return this;
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if(operator instanceof NumericNegation)
            return arg.getNegation();
        return TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {

        if (operator instanceof AdditionOperator || operator instanceof SubtractionOperator){
            if (operator instanceof  SubtractionOperator)
                right = right.getNegation();

            if (left == right)
                return left;
            if (left == TOP || right == TOP)
                return TOP;

            if (left == POSITIVE){
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return TOP;
                return POSITIVE;
            }
            if (left == NEGATIVE){
                if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return TOP;
                return NEGATIVE;
            }
            if (left == ZERO)
                return right;

            if (left == ZERO_OR_POSITIVE){
                if (right == POSITIVE)
                    return POSITIVE;
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return TOP;
                return ZERO_OR_POSITIVE;
            }
            if (left == ZERO_OR_NEGATIVE){
                if (right == NEGATIVE)
                    return NEGATIVE;
                if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return TOP;
                return ZERO_OR_NEGATIVE;
            }
        }
        if (operator instanceof MultiplicationOperator){
            if (left == ZERO || right == ZERO)
                return ZERO;
            if (left == TOP || right == TOP)
                return TOP;

            if (left == NEGATIVE)
                return right.getNegation();
            if (left == POSITIVE)
                return right;
            if (right == NEGATIVE)
                return left.getNegation();
            if (right == POSITIVE)
                return left;

            //only remain 0+ and 0-
            if (right == left)
                return ZERO_OR_POSITIVE;
            if (right != left)
                return ZERO_OR_NEGATIVE;
        }
        if (operator instanceof DivisionOperator){
            if (right == ZERO)
                return BOTTOM;
            if (left == ZERO)
                return ZERO;
            if (left == TOP || right == TOP)
                return TOP;

            if(left == NEGATIVE){
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return POSITIVE;
                return NEGATIVE;
            }
            if (left == POSITIVE){
                if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return POSITIVE;
                return NEGATIVE;
            }
            if (left == ZERO_OR_NEGATIVE){
                if (right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                    return ZERO_OR_POSITIVE;
                return ZERO_OR_NEGATIVE;
            }
            if (left == ZERO_OR_POSITIVE){
                if (right == POSITIVE || right == ZERO_OR_POSITIVE)
                    return ZERO_OR_POSITIVE;
                return ZERO_OR_NEGATIVE;
            }
        }

        return TOP;
    }

    /*
    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) {
        if (left == TOP || right == TOP)
            return Satisfiability.UNKNOWN;

        if (operator == ComparisonEq.INSTANCE)
            return left.eq(right);
        if (operator == ComparisonGt.INSTANCE)
            return left.gt(right);
        if (operator == ComparisonGe.INSTANCE)
            return left.eq(right).or(left.gt(right));
        if (operator == ComparisonLe.INSTANCE)
            return left.gt(right).negate();
        if (operator == ComparisonLt.INSTANCE)
            return  left.gt(left).or(left.eq(right)).negate();
        if (operator == ComparisonNe.INSTANCE)
            return left.eq(right).negate();

        return Satisfiability.UNKNOWN;
    }

    private Satisfiability eq(ExtSignDomain other){
        if (this == ZERO && other == ZERO)
            return Satisfiability.SATISFIED;
        if ( (this == POSITIVE && (other == NEGATIVE || other == ZERO_OR_NEGATIVE || other == ZERO)) ||
                (this == NEGATIVE && (other == POSITIVE || other == ZERO_OR_POSITIVE || other == ZERO)) )
            return Satisfiability.NOT_SATISFIED;
        return Satisfiability.UNKNOWN;
    }

    private Satisfiability gt(ExtSignDomain other){
        if (this == POSITIVE){
            if (other == ZERO || other == NEGATIVE || other == ZERO_OR_NEGATIVE)
                return Satisfiability.SATISFIED;
            return Satisfiability.UNKNOWN;
        }
        if (this == ZERO_OR_POSITIVE){
            if (other == NEGATIVE)
                return Satisfiability.SATISFIED;
            return Satisfiability.UNKNOWN;
        }
        if (this == ZERO){
            if (other == NEGATIVE)
                return Satisfiability.SATISFIED;
            if (other == POSITIVE || other == ZERO || other == ZERO_OR_POSITIVE)
                return Satisfiability.NOT_SATISFIED;
            return Satisfiability.UNKNOWN;
        }
        // a > b => !(-a <= -b)
        if (this == ZERO_OR_NEGATIVE || this == NEGATIVE)
            return this.getNegation().gt(other.getNegation()).or(this.eq(other)).negate();
        return Satisfiability.UNKNOWN;
    }
    */

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == ExtSignDomain.class && ((ExtSignDomain)obj).sign == this.sign;
    }

    @Override
    public int hashCode() {
        return sign;
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

}
