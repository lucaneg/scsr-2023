package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.PossibleForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.ListRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

public class ReachingDefinitions
		// instances of this class are dataflow elements such that:
		// - their state (fields) hold the information contained into a single
		// element
		// - they provide gen and kill functions that are specific to the
		// analysis that we are executing
		implements DataflowElement<
				// the type of dataflow domain that we want to use with this
				// analysis
				PossibleForwardDataflowDomain<
						// java requires this type parameter to have this class
						// as type in fields/methods
						ReachingDefinitions>,
				// java requires this type parameter to have this class
				// as type in fields/methods
				ReachingDefinitions> {

	/**
	 * The variable being defined
	 */
	private final Identifier variable;

	/**
	 * The place in the program where the variable is defined
	 */
	private final CodeLocation definition;

	public ReachingDefinitions() {
		this(null, null);
	}

	public ReachingDefinitions(Identifier variable, CodeLocation definition) {
		this.variable = variable;
		this.definition = definition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((definition == null) ? 0 : definition.hashCode());
		result = prime * result + ((variable == null) ? 0 : variable.hashCode());
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
		ReachingDefinitions other = (ReachingDefinitions) obj;
		if (definition == null) {
			if (other.definition != null)
				return false;
		} else if (!definition.equals(other.definition))
			return false;
		if (variable == null) {
			if (other.variable != null)
				return false;
		} else if (!variable.equals(other.variable))
			return false;
		return true;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		Set<Identifier> result = new HashSet<>();
		result.add(variable);
		return result;
	}

	@Override
	public Collection<ReachingDefinitions> gen(
			Identifier id,
			ValueExpression expression,
			ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain)
			throws SemanticException {
		// we generate a new element tracking this definition
		Set<ReachingDefinitions> result = new HashSet<>();
		ReachingDefinitions rd = new ReachingDefinitions(id, pp.getLocation());
		result.add(rd);
		return result;
	}

	@Override
	public Collection<ReachingDefinitions> gen(
			ValueExpression expression,
			ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain)
			throws SemanticException {
		// if no assignment is performed, no element is generated!
		return new HashSet<>();
	}

	@Override
	public Collection<ReachingDefinitions> kill(
			Identifier id,
			ValueExpression expression,
			ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain)
			throws SemanticException {
		// we kill all of the elements that refer to the variable being
		// assigned, as we are redefining the variable
		Set<ReachingDefinitions> killed = new HashSet<>();
		for (ReachingDefinitions rd : domain.getDataflowElements())
			// we could use `rd.variable.equals(id)` as elements of this class
			// refer to one variable at a time
			if (rd.getInvolvedIdentifiers().contains(id))
				killed.add(rd);
		return killed;
	}

	@Override
	public Collection<ReachingDefinitions> kill(
			ValueExpression expression,
			ProgramPoint pp,
			PossibleForwardDataflowDomain<ReachingDefinitions> domain)
			throws SemanticException {
		// if no assignment is performed, no element is killed!
		return new HashSet<>();
	}

	/*
	 * Out of the scope of the course: this is needed to build structured
	 * representations
	 */

	@Override
	public DomainRepresentation representation() {
		return new ListRepresentation(
				new StringRepresentation(variable),
				new StringRepresentation(definition));
	}

	/* Out of the scope of the course: these are needed to handle calls */

	@Override
	public ReachingDefinitions pushScope(ScopeToken token) throws SemanticException {
		return this;
	}

	@Override
	public ReachingDefinitions popScope(ScopeToken token) throws SemanticException {
		return this;
	}
}
