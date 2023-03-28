package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private final ValueExpression expression;



    public AvailExprs() {
        this(null);
    }
	public AvailExprs(ValueExpression expression) {
		this.expression = expression;
	}
	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> res = new HashSet<>();

		if(expression instanceof Identifier){
			res.add((Identifier) expression);
		}

		if(expression instanceof UnaryExpression){
			res.addAll(new AvailExprs((ValueExpression)(((UnaryExpression) expression).getExpression())).getInvolvedIdentifiers());
		}

		if(expression instanceof BinaryExpression){
			BinaryExpression bin = (BinaryExpression) expression;
			res.addAll(new AvailExprs((ValueExpression)(bin.getRight())).getInvolvedIdentifiers());
			res.addAll(new AvailExprs((ValueExpression)(bin.getLeft())).getInvolvedIdentifiers());
		}

		if(expression instanceof TernaryExpression){
			TernaryExpression ter = (TernaryExpression) expression;
			res.addAll(new AvailExprs((ValueExpression)(ter.getRight())).getInvolvedIdentifiers());
			res.addAll(new AvailExprs((ValueExpression)(ter.getLeft())).getInvolvedIdentifiers());
			res.addAll(new AvailExprs((ValueExpression)(ter.getMiddle())).getInvolvedIdentifiers());
		}

		return res;
	}

	private static boolean filter(ValueExpression expression) {
		if (expression instanceof Constant) {
			return false;
		}
		else if(expression instanceof Skip) {
			return false;
		}
		else if (expression instanceof Identifier) {
			return false;
		}
		else if (expression instanceof PushAny) {
			return false;
		}
		return true;
	}

	@Override
	public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
		Set<AvailExprs> res = new HashSet<>();

		AvailExprs ae = new AvailExprs(expression);

		if (filter(expression) && !ae.getInvolvedIdentifiers().contains(id)){
			res.add(ae);
		}

		return res;
	}

	@Override
	public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {
		Set<AvailExprs> res = new HashSet<>();
		AvailExprs ae = new AvailExprs(expression);

		if(filter(expression)) {
			res.add(ae);
		}

		return res;
	}
	@Override
	public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {

		Set<AvailExprs> res = new HashSet<>();

		for (AvailExprs ae : domain.getDataflowElements()) {
			Collection<Identifier> idx = ae.getInvolvedIdentifiers();

			if (idx.contains(id)){
				res.add(ae);
			}
		}
		return res;
	}

	@Override
	public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) {
		return new HashSet<>();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		return prime + ((expression == null) ? 0 : expression.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		AvailExprs other = (AvailExprs) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
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
