package it.unive.scsr;

import it.unive.lisa.analysis.SemanticDomain;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.*;
import it.unive.lisa.symbolic.value.operator.binary.*;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.symbolic.value.operator.unary.StringLength;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;
import it.unive.lisa.util.numeric.IntInterval;
import it.unive.lisa.util.numeric.MathNumber;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;

import java.util.HashSet;


public class StrictUpperBounds extends BaseNonRelationalValueDomain<StrictUpperBounds> {

 public static final StrictUpperBounds TOP = new StrictUpperBounds();

    public static final StrictUpperBounds BOTTOM = new StrictUpperBounds(null);

    private final HashSet<Identifier> y;

    public StrictUpperBounds() {
        this.y = new HashSet<>();
    }

    public StrictUpperBounds(HashSet<Identifier> y) {
        this.y = y;
    }

    @Override
    public StrictUpperBounds lubAux(StrictUpperBounds other) throws SemanticException {
        HashSet<Identifier> lub = (HashSet<Identifier>) this.y.clone();
        lub.retainAll(other.y);
        return new StrictUpperBounds(lub);
    }

    @Override
    public StrictUpperBounds glbAux(StrictUpperBounds other) {
        HashSet<Identifier> glb = (HashSet<Identifier>) this.y.clone();
        glb.addAll(other.y);
        return new StrictUpperBounds(glb);
    }

    @Override
    public StrictUpperBounds wideningAux(StrictUpperBounds other) throws SemanticException {
        if(other.y.containsAll(this.y)){
            return other;
        }else{
            return new StrictUpperBounds();
        }
    }

    @Override
    public boolean lessOrEqualAux(StrictUpperBounds other) throws SemanticException {
        return this.y.contains(other.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StrictUpperBounds other = (StrictUpperBounds) obj;
        if (y == null) {
            if (other.y != null)
                return false;
        } else if (!y.equals(other.y))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + y.hashCode();
        return result;
    }

    @Override
    public DomainRepresentation representation() {
		return new StringRepresentation(y);
    }

    @Override
    public StrictUpperBounds top() {
        return TOP;
    }

    @Override
    public StrictUpperBounds bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return y != null && this.y.isEmpty();
    }

    @Override
    public boolean isBottom() {
        return this.y == null;
    }

    @Override
    public StrictUpperBounds evalBinaryExpression(BinaryOperator operator, StrictUpperBounds left, StrictUpperBounds right, ProgramPoint pp) {
        return new StrictUpperBounds();
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryOperator operator, StrictUpperBounds left, StrictUpperBounds right,
                                                                   ProgramPoint pp) {
        return Satisfiability.UNKNOWN;
    }


    @Override
    public ValueEnvironment<StrictUpperBounds> assumeBinaryExpression(
            ValueEnvironment<StrictUpperBounds> environment, BinaryOperator operator, ValueExpression left,
            ValueExpression right, ProgramPoint pp) throws SemanticException {

        return environment;
    }

    @Override
    public StrictUpperBounds evalUnaryExpression(UnaryOperator operator, StrictUpperBounds arg, ProgramPoint pp) {
        return new StrictUpperBounds();
    }

    @Override
    public StrictUpperBounds evalNonNullConstant(Constant constant, ProgramPoint pp) {
        if (constant.getValue() instanceof Integer) {
            Integer i = (Integer) constant.getValue();
            return new StrictUpperBounds();
        }

        return top();
    }

}
