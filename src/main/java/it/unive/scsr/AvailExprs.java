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

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

	private ValueExpression expression;
	private Identifier id;

	public AvailExprs() {
		this(null);
	}

	public AvailExprs(ValueExpression expression) {
		this.expression = expression;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getIdentifierOperands(expression);
	}

	private static Collection<Identifier> getIdentifierOperands(ValueExpression expression) {
		Collection<Identifier> res = new HashSet<>();

		if (expression instanceof Identifier) {
			Identifier i = (Identifier) expression;
			res.add(i);
		} else if (expression instanceof UnaryExpression) {
			UnaryExpression u = (UnaryExpression) expression;
			res.addAll(getIdentifierOperands((ValueExpression) u.getExpression()));
		} else if (expression instanceof BinaryExpression) {
			BinaryExpression b = (BinaryExpression) expression;
			res.addAll(getIdentifierOperands((ValueExpression) b.getLeft()));
			res.addAll(getIdentifierOperands((ValueExpression) b.getRight()));
		} else if (expression instanceof TernaryExpression) {
			TernaryExpression t = (TernaryExpression) expression;
			res.addAll(getIdentifierOperands((ValueExpression) t.getLeft()));
			res.addAll(getIdentifierOperands((ValueExpression) t.getMiddle()));
			res.addAll(getIdentifierOperands((ValueExpression) t.getRight()));
		}
		return res;
	}

	public ValueExpression getExpression() {
		return expression;
	}

	@Override
	public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
									  DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		Collection<AvailExprs> res = new HashSet<>();
		AvailExprs a = new AvailExprs(expression);
		if (!a.getInvolvedIdentifiers().contains(id) && filterExpression(expression))
			res.add(a);
		return res;
	}

	@Override
	public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		Collection<AvailExprs> result = new HashSet<>();
		AvailExprs a = new AvailExprs(expression);
		if (filterExpression(expression))
			result.add(a);
		return result;
	}

	@Override
	public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		Collection<AvailExprs> result = new HashSet<>();

		for (AvailExprs a : domain.getDataflowElements()) {
			Collection<Identifier> i = getIdentifierOperands(a.expression);

			if (i.contains(id))
				result.add(a);
		}

		return result;
	}

	@Override
	public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
		return new HashSet<>();
	}

	private static boolean filterExpression(ValueExpression expression) {
		if (expression instanceof Identifier)
			return false;
		if (expression instanceof Constant)
			return false;
		if (expression instanceof Skip)
			return false;
		if (expression instanceof PushAny)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
