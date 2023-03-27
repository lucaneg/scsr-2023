package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AvailExprs implements DataflowElement<
        DefiniteForwardDataflowDomain<
                        AvailExprs>,
        AvailExprs> {

    private final ValueExpression expression;

    public AvailExprs() {
        this(null);
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        ArrayList<ValueExpression> itemForRecursion = new ArrayList<>();

        if(expression instanceof Identifier) {
            Identifier id = (Identifier) expression;
            result.add(id);
        }
        else if(expression instanceof UnaryExpression)
        {
            UnaryExpression ue = (UnaryExpression) expression;
            itemForRecursion.add((ValueExpression) ue.getExpression());
        }
        else if(expression instanceof BinaryExpression)
        {
            BinaryExpression be = (BinaryExpression) expression;
            itemForRecursion.add((ValueExpression) be.getLeft());
            itemForRecursion.add((ValueExpression) be.getRight());
        }
        else if(expression instanceof TernaryExpression)
        {
            TernaryExpression te = (TernaryExpression) expression;
            itemForRecursion.add((ValueExpression) te.getLeft());
            itemForRecursion.add((ValueExpression) te.getMiddle());
            itemForRecursion.add((ValueExpression) te.getRight());
        }

        for (ValueExpression ve :itemForRecursion)
            result.addAll(new AvailExprs(ve).getInvolvedIdentifiers());

        return result;
    }

    private static Boolean isNonCostantExpr(ValueExpression expr)
    {
        return expr instanceof Identifier ||
                expr instanceof UnaryExpression ||
                expr instanceof BinaryExpression ||
                expr instanceof TernaryExpression;
    }

    public Boolean involveIdentifier(Identifier id)
    {
        return this.getInvolvedIdentifiers().contains(id);
    }

    private static Boolean redefineItself(Identifier id, ValueExpression expr)
    {
        AvailExprs availExprs = new AvailExprs(expr);
        return availExprs.involveIdentifier(id);
    }

    @Override
    public Collection<AvailExprs> gen(
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException
    {
        //Assignment
        Set<AvailExprs> result = new HashSet<>();
        if(isNonCostantExpr(expression) && !redefineItself(id, expression))
            result.add(new AvailExprs(expression));
        return result;
    }

    @Override
    public Collection<AvailExprs> gen(
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException
    {
        //Expression without assignment
        Set<AvailExprs> result = new HashSet<>();
        if(isNonCostantExpr(expression))
            result.add(new AvailExprs(expression));
        return result;
    }

    @Override
    public Collection<AvailExprs> kill(
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException
    {
        //must kill all the expression that use id
        return domain.getDataflowElements()
                .stream()
                .filter((AvailExprs ae) -> ae.involveIdentifier(id))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<AvailExprs> kill(
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException
    {
        // if no assignment is performed, no element is killed!
        return new HashSet<>();
    }

    @Override
	public DomainRepresentation representation()
    {
		return new StringRepresentation(expression);
	}

	@Override
	public AvailExprs pushScope(ScopeToken scope) throws SemanticException
    {
		return this;
	}

	@Override
	public AvailExprs popScope(ScopeToken scope) throws SemanticException
    {
		return this;
	}
}
