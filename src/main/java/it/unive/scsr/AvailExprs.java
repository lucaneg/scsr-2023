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
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private final ValueExpression valueExpression;
    private final Identifier variable;


    public AvailExprs() {
        valueExpression = null;
        variable = null;
    }

    public AvailExprs(ValueExpression valueExpression, Identifier variable) {
        this.valueExpression = valueExpression;
        this.variable = variable;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        result.add(variable);
        return result;
    }

    //assign expression
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        if (expression.toString().contains(id.toString()))
            result.add(new AvailExprs(expression, id));
        return result;
    }

    //NON assign expression
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        if (expression.toString().contains("+") || expression.toString().contains("-") || expression.toString().contains("*") ||
                expression.toString().contains("/") && !expression.toString().contains(">") && !expression.toString().contains(">=")
            && !expression.toString().contains("<") && !expression.toString().contains("<=") && !expression.toString().contains("!="))
            result.add(new AvailExprs(expression, null));
        return result;
    }

    //assign expression
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        for (AvailExprs availExprs : domain.getDataflowElements())
            if(availExprs.toString().contains(id.toString()))
                result.add(availExprs);
        return result;
    }

    //NON assign expression
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        return new HashSet<>();
    }



    // IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment
	// it to get your code to compile. Be aware that the code is written
	// expecting that a field named "expression" of type ValueExpression exists
	// in this class: if you name it differently, change also the code below to
	// make it work by just using the name of your choice instead of
	// "expression". If you don't have a field of type ValueExpression in your
	// solution, then you should make sure that what you are doing is correct :)

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(valueExpression);
	}

	@Override
	public AvailExprs pushScope(ScopeToken scope) throws SemanticException {
		return this;
	}

	@Override
	public AvailExprs popScope(ScopeToken scope) throws SemanticException {
		return this;
	}
}
