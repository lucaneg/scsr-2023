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

public class ExtSignDomain extends BaseNonRelationalValueDomain<ExtSignDomain>
{

    // IMPLEMENTATION NOTE:
    // you can follow the same logic of Signs to implement representation().
    // note that this is not mandatory: you can have any other logic instead
    // of constant fields, and you can change the logic in representation()
    // accordingly. the only constraint is that the strings used to represent
    // the elements stay the same (0, +, -, 0+, 0-)

    private static final ExtSignDomain TOP = new ExtSignDomain(100);
    private static final ExtSignDomain POSITIVE = new ExtSignDomain(10);
    private static final ExtSignDomain ZERO_OR_POSITIVE = new ExtSignDomain(1);
    private static final ExtSignDomain ZERO = new ExtSignDomain(0);
    private static final ExtSignDomain ZERO_OR_NEGATIVE = new ExtSignDomain(-1);
    private static final ExtSignDomain NEGATIVE = new ExtSignDomain(-10);
    private static final ExtSignDomain BOTTOM = new ExtSignDomain(-100);

    private final int code;

    public ExtSignDomain()
    {
        this(TOP.code);
    }

    @Override
    public ExtSignDomain evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
        Object val = constant.getValue();
        if(val instanceof Integer)
        {
            Integer i = (Integer)val;
            if(i > 0) return POSITIVE;
            if(i < 0) return NEGATIVE;
            return ZERO;
        }
        return TOP;
    }

    public ExtSignDomain(int code)
    {
        this.code = code;
    }

    @Override
    public ExtSignDomain lubAux(ExtSignDomain other) throws SemanticException
    {
        if(this == NEGATIVE)
        {
            if(other == ZERO) return ZERO_OR_NEGATIVE;
            if(other == POSITIVE) return TOP;
            if(other == ZERO_OR_NEGATIVE) return ZERO_OR_NEGATIVE;
            if(other == ZERO_OR_POSITIVE) return TOP;
        }
        else if(this == ZERO)
        {
            if(other == NEGATIVE) return ZERO_OR_NEGATIVE;
            if(other == POSITIVE) return ZERO_OR_POSITIVE;
            if(other == ZERO_OR_NEGATIVE) return ZERO_OR_NEGATIVE;
            if(other == ZERO_OR_POSITIVE) return ZERO_OR_POSITIVE;
        }
        else if(this == POSITIVE)
        {
            if(other == ZERO) return ZERO_OR_POSITIVE;
            if(other == NEGATIVE) return TOP;
            if(other == ZERO_OR_NEGATIVE) return TOP;
            if(other == ZERO_OR_POSITIVE) return ZERO_OR_POSITIVE;
        }
        else if(this == ZERO_OR_POSITIVE)
        {
            if(other == POSITIVE) return ZERO_OR_POSITIVE;
            if(other == ZERO) return ZERO_OR_POSITIVE;
            if(other == NEGATIVE) return TOP;
            if(other == ZERO_OR_NEGATIVE) return TOP;
        }
        else if(this == ZERO_OR_NEGATIVE)
        {
            if(other == ZERO_OR_POSITIVE) return TOP;
            if(other == POSITIVE) return TOP;
            if(other == ZERO) return ZERO_OR_NEGATIVE;
            if(other == NEGATIVE) return ZERO_OR_NEGATIVE;
        }
        return TOP;
    }

    @Override
    public boolean lessOrEqualAux(ExtSignDomain other) throws SemanticException
    {
        if(this == NEGATIVE)
            return other == ZERO_OR_NEGATIVE;
        if(this == POSITIVE)
            return other == ZERO_OR_POSITIVE;
        if(this == ZERO)
            return other == ZERO_OR_POSITIVE || other == ZERO_OR_NEGATIVE;
        return false;
    }

    @Override
    public ExtSignDomain evalUnaryExpression(UnaryOperator operator, ExtSignDomain arg, ProgramPoint pp) throws SemanticException {
        if(operator instanceof NumericNegation)
            return arg.opposite();
        return TOP;
    }

    @Override
    public ExtSignDomain evalBinaryExpression(BinaryOperator operator, ExtSignDomain left, ExtSignDomain right, ProgramPoint pp) throws SemanticException {
        if (operator instanceof AdditionOperator)
            return sum(left, right);
        if (operator instanceof SubtractionOperator)
            return sum(left, right.opposite());
        if (operator instanceof MultiplicationOperator)
            return multipl(left, right);
        if (operator instanceof DivisionOperator)
            return divis(left, right);
        return TOP;
    }

    private ExtSignDomain opposite()
    {
        if(this == TOP) return TOP;
        if(this == ZERO_OR_POSITIVE) return ZERO_OR_NEGATIVE;
        if(this == ZERO_OR_NEGATIVE) return ZERO_OR_POSITIVE;
        if(this == ZERO) return ZERO;
        if(this == POSITIVE) return NEGATIVE;
        if(this == NEGATIVE) return POSITIVE;
        if(this == BOTTOM) return BOTTOM;

        return TOP;
    }

    private static ExtSignDomain sum(ExtSignDomain left, ExtSignDomain right)
    {
        if(left == TOP || right == TOP) return TOP;

        if(left == ZERO) return right;
        if(right == ZERO) return left;

        if(left == right) return left;

        if(left == NEGATIVE)
        {
            if(right == ZERO_OR_NEGATIVE)
                return NEGATIVE;
            else
                return TOP;
        }

        if(left == POSITIVE)
        {
            if(right == ZERO_OR_POSITIVE)
                return POSITIVE;
            else
                return TOP;
        }

        if(left == ZERO_OR_NEGATIVE)
        {
            if(right == NEGATIVE)
                return NEGATIVE;
            else
                return TOP;
        }


        if(left == ZERO_OR_POSITIVE)
        {
            if(right == POSITIVE)
                return POSITIVE;
            else
                return TOP;
        }

        return TOP;
    }

    private static ExtSignDomain multipl(ExtSignDomain left, ExtSignDomain right)
    {
        if(left == ZERO || right == ZERO) return ZERO;

        if(left == TOP || right == TOP) return TOP;

        if(left == NEGATIVE) return right.opposite();
        if(right == NEGATIVE) return left.opposite();

        if(left == POSITIVE) return right;
        if(right == POSITIVE) return left;

        if(left == ZERO_OR_NEGATIVE)
        {
            if(right == ZERO_OR_NEGATIVE)
                return ZERO_OR_POSITIVE;
            else
                return ZERO_OR_NEGATIVE;
        }

        if(left == ZERO_OR_POSITIVE)
        {
            if(right == ZERO_OR_POSITIVE)
                return ZERO_OR_POSITIVE;
            else
                return ZERO_OR_NEGATIVE;
        }

        return TOP;
    }

    private static ExtSignDomain divis(ExtSignDomain left, ExtSignDomain right)
    {
        if(right == ZERO) return BOTTOM;

        if(left == ZERO) return ZERO;

        if(left == TOP || right == TOP) return TOP;

        if(left == NEGATIVE)
        {
            if(right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                return POSITIVE;
            else
                return NEGATIVE;
        }

        if(left == POSITIVE)
        {
            if(right == POSITIVE || right == ZERO_OR_POSITIVE)
                return POSITIVE;
            else
                return NEGATIVE;
        }

        if(left == ZERO_OR_NEGATIVE)
        {
            if(right == NEGATIVE || right == ZERO_OR_NEGATIVE)
                return ZERO_OR_POSITIVE;
            else
                return ZERO_OR_NEGATIVE;
        }

        if(left == ZERO_OR_POSITIVE)
        {
            if(right == POSITIVE || right == ZERO_OR_POSITIVE)
                return ZERO_OR_POSITIVE;
            else
                return ZERO_OR_NEGATIVE;
        }

        return TOP;
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
    public boolean isTop() {
        return this.code == TOP.code;
    }

    @Override
    public boolean isBottom() {
        return this.code == BOTTOM.code;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * code;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null)
            if(obj instanceof ExtSignDomain)
                return ((ExtSignDomain)obj).code == this.code;
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
            return new StringRepresentation("0+");
        return new StringRepresentation("0-");
    }
}
