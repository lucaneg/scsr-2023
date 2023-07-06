package main.java.it.unive.scsr;

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
import java.util.function.BinaryOperator;


public class StrictUpperBounds extends BaseNonRelationalValueDomain<StrictUpperBounds> {

 public static final StrictUpperBounds TOP = new StrictUpperBounds();

    public static final StrictUpperBounds BOTTOM = new StrictUpperBounds(null);

    private final HashSet<Identifier> identifierSet;

    public StrictUpperBounds() {
        this.identifierSet = new HashSet<>();
    }

    public StrictUpperBounds(HashSet<Identifier> identifierSet) {
        this.identifierSet = identifierSet;
    }

    @Override
    public StrictUpperBounds lubAux(StrictUpperBounds strictUpperBounds) throws SemanticException {
        HashSet<Identifier> lub = (HashSet<Identifier>) this.identifierSet.clone();
        lub.retainAll(strictUpperBounds.identifierSet);
        return new StrictUpperBounds(lub);
    }

    @Override
    public StrictUpperBounds glbAux(StrictUpperBounds strictUpperBounds) {
        HashSet<Identifier> glb = (HashSet<Identifier>) this.identifierSet.clone();
        glb.addAll(strictUpperBounds.identifierSet);
        return new StrictUpperBounds(glb);
    }

    @Override
    public StrictUpperBounds wideningAux(StrictUpperBounds strictUpperBounds) throws SemanticException {
        if(strictUpperBounds.identifierSet.containsAll(this.identifierSet)){
            return strictUpperBounds;
        }else{
            return new StrictUpperBounds();
        }
    }

    @Override
    public boolean lessOrEqualAux(StrictUpperBounds strictUpperBounds) throws SemanticException {
        return this.identifierSet.contains(strictUpperBounds.identifierSet);
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
        if (identifierSet == null) {
            if (other.identifierSet != null)
                return false;
        } else if (!identifierSet.equals(other.identifierSet))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + identifierSet.hashCode();
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
        return identifierSet != null && this.identifierSet.isEmpty();
    }

    @Override
    public boolean isBottom() {
        return this.identifierSet == null;
    }

    @Override
    public StrictUpperBounds evalBinaryExpression(BinaryOperator operator, StrictUpperBounds left, StrictUpperBounds right, ProgramPoint programPoint) {
        return new StrictUpperBounds();
    }

    @Override
    public Satisfiability satisfiesBinaryExpression(BinaryOperator operator, StrictUpperBounds left, StrictUpperBounds right,ProgramPoint ProgramPoint) {
        return Satisfiability.UNKNOWN;
    }


    @Override
    public ValueEnvironment<StrictUpperBounds> assumeBinaryExpression(
            ValueEnvironment<StrictUpperBounds> environment, BinaryOperator operator, ValueExpression left,
            ValueExpression right, ProgramPoint ProgramPoint) throws SemanticException {

        return environment;
    }

    @Override
    public StrictUpperBounds evalUnaryExpression(UnaryOperator operator, StrictUpperBounds arg, ProgramPoint ProgramPoint) {
        return new StrictUpperBounds();
    }

    @Override
    public StrictUpperBounds evalNonNullConstant(Constant constant, ProgramPoint programPoint) {
        if (constant.getValue() instanceof Integer) {
            Integer i = (Integer) constant.getValue();
            return new StrictUpperBounds();
        }

        return top();
    }

}