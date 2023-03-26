package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.AvailableExpressions;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.*;
import java.util.stream.Collectors;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {
	private ValueExpression expression;

	private static Collection<Identifier> getIdentifierOperands(ValueExpression expression) {
		Collection<Identifier> identifiers = new HashSet<>();

		if (expression instanceof Identifier) {
			identifiers.add((Identifier) expression);
		} else if (expression instanceof UnaryExpression) {
			identifiers.addAll(getIdentifierOperands((ValueExpression) ((UnaryExpression) expression).getExpression()));
		} else if (expression instanceof BinaryExpression) {
			BinaryExpression binaryExpression = (BinaryExpression) expression;
			identifiers.addAll(getIdentifierOperands((ValueExpression) binaryExpression.getLeft()));
			identifiers.addAll(getIdentifierOperands((ValueExpression) binaryExpression.getRight()));
		} else if (expression instanceof TernaryExpression) {
			TernaryExpression ternaryExpression = (TernaryExpression) expression;
			identifiers.addAll(getIdentifierOperands((ValueExpression) ternaryExpression.getLeft()));
			identifiers.addAll(getIdentifierOperands((ValueExpression) ternaryExpression.getMiddle()));
			identifiers.addAll(getIdentifierOperands((ValueExpression) ternaryExpression.getRight()));
		}

		return identifiers;
	}

	public AvailExprs() {}

	public AvailExprs(ValueExpression expression) {
		this.expression = expression;
	}

	public ValueExpression getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		return 31 + ((getExpression() == null) ? 0 : getExpression().hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		final AvailExprs other = (AvailExprs) obj;
		return Objects.equals(getExpression(), other.getExpression());
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getIdentifierOperands(getExpression());
	}

	@Override
	public Collection<AvailExprs> gen(Identifier id,
									  ValueExpression expression,
									  ProgramPoint pp,
									  DefiniteForwardDataflowDomain<AvailExprs> domain) {
		final AvailExprs availExprs = new AvailExprs(expression);
		return !availExprs.getInvolvedIdentifiers().contains(id) && filter(expression) ?
				new HashSet<>(List.of(availExprs)) :
				new HashSet<>();
	}

	@Override
	public Collection<AvailExprs> gen(ValueExpression expression,
									  ProgramPoint pp,
									  DefiniteForwardDataflowDomain<AvailExprs> domain) {
		return filter(expression) ?
				new HashSet<>(List.of(new AvailExprs(expression))) :
				new HashSet<>();
	}

	private static boolean filter(ValueExpression expression) {
		return !((expression instanceof Identifier) ||
				(expression instanceof Constant) ||
				(expression instanceof Skip) ||
				(expression instanceof PushAny));
	}

	@Override
	public Collection<AvailExprs> kill(Identifier id,
									   ValueExpression expression,
									   ProgramPoint pp,
									   DefiniteForwardDataflowDomain<AvailExprs> domain) {
		return domain.getDataflowElements().stream()
				.filter(availExprs -> getIdentifierOperands(availExprs.getExpression()).contains(id))
				.collect(Collectors.toSet());
	}

	@Override
	public Collection<AvailExprs> kill(ValueExpression expression,
									   ProgramPoint pp,
									   DefiniteForwardDataflowDomain<AvailExprs> domain) {
		return Collections.emptyList();
	}

	/*
	 IMPLEMENTATION NOTE:
	 the code below is outside of the scope of the course. You can uncomment
	 it to get your code to compile. Be aware that the code is written
	 expecting that a field named "expression" of type ValueExpression exists
	 in this class: if you name it differently, change also the code below to
	 make it work by just using the name of your choice instead of
	 "expression". If you don't have a field of type ValueExpression in your
	 solution, then you should make sure that what you are doing is correct :)
	*/

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(getExpression());
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
