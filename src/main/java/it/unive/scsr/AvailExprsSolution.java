package it.unive.scsr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.PushAny;
import it.unive.lisa.symbolic.value.Skip;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailExprsSolution
		// instances of this class are dataflow elements such that:
		// - their state (fields) hold the information contained into a single
		// element
		// - they provide gen and kill functions that are specific to the
		// analysis that we are executing
		implements DataflowElement<
				// the type of dataflow domain that we want to use with this
				// analysis
				DefiniteForwardDataflowDomain<
						// java requires this type parameter to have this class
						// as type in fields/methods
						AvailExprsSolution>,
				// java requires this type parameter to have this class
				// as type in fields/methods
				AvailExprsSolution> {

	/**
	 * The expression being tracked
	 */
	private final ValueExpression expression;

	public AvailExprsSolution() {
		this(null);
	}

	private AvailExprsSolution(ValueExpression expression) {
		this.expression = expression;
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
		AvailExprsSolution other = (AvailExprsSolution) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getVariablesIn(expression);
	}

	private static Collection<Identifier> getVariablesIn(ValueExpression expression) {
		Collection<Identifier> result = new HashSet<>();

		if (expression == null)
			return result;

		if (expression instanceof Identifier)
			result.add((Identifier) expression);

		if (expression instanceof UnaryExpression)
			result.addAll(getVariablesIn((ValueExpression) ((UnaryExpression) expression).getExpression()));

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;
			result.addAll(getVariablesIn((ValueExpression) binary.getLeft()));
			result.addAll(getVariablesIn((ValueExpression) binary.getRight()));
		}

		if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;
			result.addAll(getVariablesIn((ValueExpression) ternary.getLeft()));
			result.addAll(getVariablesIn((ValueExpression) ternary.getMiddle()));
			result.addAll(getVariablesIn((ValueExpression) ternary.getRight()));
		}

		return result;
	}

	@Override
	public Collection<AvailExprsSolution> gen(
			Identifier id,
			ValueExpression expression,
			ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailExprsSolution> domain) {
		// we generate a new element tracking this expression
		Collection<AvailExprsSolution> result = new HashSet<>();
		AvailExprsSolution ae = new AvailExprsSolution(expression);
		// we don't add an expression if one of its variables is being redefined
		if (!ae.getInvolvedIdentifiers().contains(id) && filter(expression))
			result.add(ae);
		return result;
	}

	@Override
	public Collection<AvailExprsSolution> gen(
			ValueExpression expression,
			ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailExprsSolution> domain) {
		// we generate a new element tracking this expression
		Collection<AvailExprsSolution> result = new HashSet<>();
		AvailExprsSolution ae = new AvailExprsSolution(expression);
		if (filter(expression))
			result.add(ae);
		return result;
	}

	private static boolean filter(ValueExpression expression) {
		// optional: these expressions are not really interesting
		if (expression instanceof Identifier)
			// variables are always available
			return false;
		if (expression instanceof Constant)
			// constants do not need to be computed
			return false;
		// the following are lisa internal expressions that we don't care about
		if (expression instanceof Skip)
			return false;
		if (expression instanceof PushAny)
			return false;
		return true;
	}

	@Override
	public Collection<AvailExprsSolution> kill(
			Identifier id,
			ValueExpression expression,
			ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailExprsSolution> domain) {
		// we kill all of the elements that refer to expressions using the
		// variable being assinged
		Collection<AvailExprsSolution> result = new HashSet<>();

		for (AvailExprsSolution ae : domain.getDataflowElements()) {
			Collection<Identifier> ids = getVariablesIn(ae.expression);

			if (ids.contains(id))
				result.add(ae);
		}

		return result;
	}

	@Override
	public Collection<AvailExprsSolution> kill(
			ValueExpression expression,
			ProgramPoint pp,
			DefiniteForwardDataflowDomain<AvailExprsSolution> domain) {
		// no variable is being redefined, so nothing to kill here
		return Collections.emptyList();
	}

	/*
	 * Out of the scope of the course: this is needed to build structured
	 * representations
	 */

	@Override
	public DomainRepresentation representation() {
		return new StringRepresentation(expression);
	}

	/* Out of the scope of the course: these are needed to handle calls */

	@Override
	public AvailExprsSolution pushScope(ScopeToken scope) throws SemanticException {
		return new AvailExprsSolution((ValueExpression) expression.pushScope(scope));
	}

	@Override
	public AvailExprsSolution popScope(ScopeToken scope) throws SemanticException {
		return new AvailExprsSolution((ValueExpression) expression.popScope(scope));
	}
}
