package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.ListRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailExprs implements DataflowElement <DefiniteForwardDataflowDomain <AvailExprs>, AvailExprs> {
	
	private final ValueExpression expression;
	
	// Constructor definition
	public AvailExprs() {
		this(null);
	}

	// Constructor Definition
	public AvailExprs(ValueExpression expression) {
		this.expression = expression;
	}

	// Override the method "hashcode" inherited by the default Class "Object"
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}
	
	// Override the method "equals" inherited by the default Class "Object"
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
        else if (obj == null || this.getClass() != obj.getClass()) return false;
        else return Objects.equals(this.expression, ((AvailExprs) obj).expression);
	}
	
	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		// TODO Auto-generated method stub
		return extractOperands(expression);
	}
	
	public static Collection<Identifier> extractOperands(ValueExpression expression) {
		Collection<Identifier> result = new HashSet<>();

		if (expression == null)
			return result;

		if (expression instanceof Identifier)
			result.add((Identifier) expression);

		if (expression instanceof UnaryExpression)
			result.addAll(extractOperands((ValueExpression) ((UnaryExpression) expression).getExpression()));

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			result.addAll(extractOperands((ValueExpression) binary.getLeft()));
			result.addAll(extractOperands((ValueExpression) binary.getRight()));
		}

		if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;
			result.addAll(extractOperands((ValueExpression) ternary.getLeft()));
			result.addAll(extractOperands((ValueExpression) ternary.getMiddle()));
			result.addAll(extractOperands((ValueExpression) ternary.getRight()));
		}

		return result;
	}
	
	// Filters out elements that should not be considered in the analysis, such as:
	// constants, identifiers, skipping statements, ...
	public Boolean filterExprs(ValueExpression expression){
        if (expression instanceof Constant
            || expression instanceof Identifier
            || expression instanceof Skip
            || expression instanceof PushAny) {
        	return false;
        }
		return true;
    }
	
	// "Gen" set for assigning expressions
	@Override
	public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		// we generate a new element tracking this definition
		Collection<AvailExprs> result = new HashSet<>();
		AvailExprs ae = new AvailExprs(expression);
		if (!ae.getInvolvedIdentifiers().contains(id) && filterExprs(expression)){
            result.add(ae);
        }
		return result;
	}

	// "Gen" set for non-assigning expressions
	@Override
	public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		// if no assignment is performed, still new expressions (r-values) can be generated
		Collection<AvailExprs> result = new HashSet<>();
		AvailExprs ae = new AvailExprs(expression);
		if(filterExprs(expression)){
            result.add(ae);
		}
		return result;
	}

	// "Kill" set for assigning expressions
	@Override
	public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		Set<AvailExprs> killed = new HashSet<>();
		for (AvailExprs ae : domain.getDataflowElements())
			if (ae.getInvolvedIdentifiers().contains(id))
				killed.add(ae);
		return killed;
	}

	// "Kill" set for non-assigning expressions
	@Override
	public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		// if no assignment is performed, no element is killed!
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
		return new StringRepresentation(expression);
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
