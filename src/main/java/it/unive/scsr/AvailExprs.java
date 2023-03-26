package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.ListRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

public class AvailExprs implements DataflowElement<
        DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private final ValueExpression expression;

    public AvailExprs() {
        this(null);
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }


    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> set = new HashSet<>();

        if (expression instanceof Identifier){
            set.add((Identifier) expression);
        }

        if (expression instanceof UnaryExpression){
            UnaryExpression ue = (UnaryExpression) expression;
            AvailExprs unary = new AvailExprs((ValueExpression) ue.getExpression());

            set.addAll(unary.getInvolvedIdentifiers());
        }

        if (expression instanceof BinaryExpression){
            BinaryExpression be = (BinaryExpression) expression;
            AvailExprs left = new AvailExprs((ValueExpression) be.getLeft());
            AvailExprs right = new AvailExprs((ValueExpression) be.getRight());

            set.addAll(left.getInvolvedIdentifiers());
            set.addAll(right.getInvolvedIdentifiers());
        }

        if(expression instanceof TernaryExpression){
            TernaryExpression te = (TernaryExpression) expression;
            AvailExprs left = new AvailExprs((ValueExpression) te.getLeft());
            AvailExprs middle = new AvailExprs((ValueExpression) te.getMiddle());
            AvailExprs right = new AvailExprs((ValueExpression) te.getRight());

            set.addAll(left.getInvolvedIdentifiers());
            set.addAll(middle.getInvolvedIdentifiers());
            set.addAll(right.getInvolvedIdentifiers());
        }

        return set;
    }

    /** Filters constants, identifiers, skips (e.g. break, continue) and top values, that are not to be considered in the analysis*/
    public Boolean filterExpression(ValueExpression expression){
        return !(expression instanceof Constant
                || expression instanceof Identifier
                || expression instanceof Skip
                || expression instanceof PushAny);
    }

    /**
     * Gen set of an assignment: expressions evaluated in the block without subsequently redefining its operands
     * */
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
        Set<AvailExprs> set = new HashSet<>();
        AvailExprs ae = new AvailExprs(expression);

        if (!ae.getInvolvedIdentifiers().contains(id) && filterExpression(expression)){
            set.add(ae);
        }

        return set;
    }

    /**
     * Gen set of a non-assignment: expressions evaluated in the block
     * */
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {
        Set<AvailExprs> set = new HashSet<>();
        AvailExprs ae = new AvailExprs(expression);
        if(filterExpression(expression))
            set.add(ae);

        return set;
    }

    /**
     * Kill set of an assignment: expressions whose operands are redefined in the block without reevaluating the expression afterwards
     * */
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {
        // for each available expression, if it contains the newly assigned identifier, add it to the kill set
        return domain.getDataflowElements().stream()
                .filter(ae -> ae.getInvolvedIdentifiers().contains(id))
                .collect(Collectors.toSet());
    }

    /**
     * Kill set of a non-assignment: the empty set
     * */
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {
        return new HashSet<>();
    }


    @Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailExprs pushScope(ScopeToken scope) {
		return this;
	}

	@Override
	public AvailExprs popScope(ScopeToken scope) {
		return this;
	}

    @Override
    public int hashCode() {
        return 31 + (expression == null ? 0 : expression.hashCode());
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (that == null || getClass() != that.getClass())
            return false;

        AvailExprs other = (AvailExprs) that;
        return Objects.equals(expression, other.expression);
    }
}
