package it.unive.scsr;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections4.CollectionUtils;

import it.unive.lisa.analysis.BaseLattice;
import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.MapRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.SubtractionOperator;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;

public class Pentagons extends BaseLattice<Pentagons> implements ValueDomain<Pentagons> {

	private final ValueEnvironment<Interval> intervals;
	private final ValueEnvironment<UpperBounds> upperBounds;

	public Pentagons() {
		this.intervals = new ValueEnvironment<>(new Interval()).top();
		this.upperBounds = new ValueEnvironment<>(new UpperBounds(true)).top();
	}
	
	public Pentagons(ValueEnvironment<Interval> intervals, ValueEnvironment<UpperBounds> upperBounds) {
		this.intervals = intervals;
		this.upperBounds = upperBounds;
	}

	@Override
	public Pentagons assign(Identifier id, ValueExpression expression, ProgramPoint pp) throws SemanticException {
		ValueEnvironment<UpperBounds> newBounds = upperBounds.assign(id, expression, pp);
		
		// we add the semantics for assignments here as we have access to the whole assigment
		if (expression instanceof BinaryExpression) {
			BinaryExpression be = (BinaryExpression) expression;
			BinaryOperator op = be.getOperator();
			if (op instanceof SubtractionOperator && be.getLeft() instanceof Identifier && be.getRight() instanceof Constant) {
				Identifier y = (Identifier) be.getLeft();
				newBounds = newBounds.putState(id, upperBounds.getState(y).add(y));
			}
		}
			
		return new Pentagons(
				intervals.assign(id, expression, pp),
				newBounds);
	}

	@Override
	public Pentagons smallStepSemantics(ValueExpression expression, ProgramPoint pp) throws SemanticException {
		return new Pentagons(
				intervals.smallStepSemantics(expression, pp),
				upperBounds.smallStepSemantics(expression, pp));
	}

	@Override
	public Pentagons assume(ValueExpression expression, ProgramPoint pp) throws SemanticException {
		return new Pentagons(
				intervals.assume(expression, pp),
				upperBounds.assume(expression, pp));
	}

	@Override
	public Pentagons forgetIdentifier(Identifier id) throws SemanticException {
		return new Pentagons(
				intervals.forgetIdentifier(id),
				upperBounds.forgetIdentifier(id));
	}

	@Override
	public Pentagons forgetIdentifiersIf(Predicate<Identifier> test) throws SemanticException {
		return new Pentagons(
				intervals.forgetIdentifiersIf(test),
				upperBounds.forgetIdentifiersIf(test));
	}

	@Override
	public Satisfiability satisfies(ValueExpression expression, ProgramPoint pp) throws SemanticException {
		return intervals.satisfies(expression, pp).glb(upperBounds.satisfies(expression, pp));
	}

	@Override
	public Pentagons pushScope(ScopeToken token) throws SemanticException {
		return this; // we do not care about this for the project
	}

	@Override
	public Pentagons popScope(ScopeToken token) throws SemanticException {
		return this; // we do not care about this for the project
	}

	@Override
	public DomainRepresentation representation() {
		if (isTop())
			return Lattice.topRepresentation();
		if (isBottom())
			return Lattice.bottomRepresentation();
		Map<DomainRepresentation, DomainRepresentation> mapping = new HashMap<>();
		for (Identifier id : CollectionUtils.union(intervals.getKeys(), upperBounds.getKeys()))
			mapping.put(new StringRepresentation(id),
					new StringRepresentation(intervals.getState(id).toString() + ", " +
							upperBounds.getState(id).representation()));
		return new MapRepresentation(mapping);
	}

	@Override
	public Pentagons top() {
		return new Pentagons(intervals.top(), upperBounds.top());
	}

	@Override
	public boolean isTop() {
		return intervals.isTop() && upperBounds.isTop();
	}

	@Override
	public Pentagons bottom() {
		return new Pentagons(intervals.bottom(), upperBounds.bottom());
	}

	@Override
	public boolean isBottom() {
		return intervals.isBottom() || upperBounds.isBottom();
	}

	@Override
	public Pentagons lubAux(Pentagons other) throws SemanticException {
		ValueEnvironment<UpperBounds> newBounds = upperBounds.lub(other.upperBounds);
		for (Entry<Identifier, UpperBounds> entry : upperBounds) {
			Set<Identifier> closure = new HashSet<>();
			for (Identifier bound : entry.getValue())
				if (other.intervals.getState(entry.getKey()).interval.getHigh()
						.compareTo(other.intervals.getState(bound).interval.getLow()) < 0)
					closure.add(bound);
			if (!closure.isEmpty())
				// glb is the union
				newBounds.putState(entry.getKey(),
						newBounds.getState(entry.getKey()).glb(new UpperBounds(closure)));
		}

		for (Entry<Identifier, UpperBounds> entry : other.upperBounds) {
			Set<Identifier> closure = new HashSet<>();
			for (Identifier bound : entry.getValue())
				if (intervals.getState(entry.getKey()).interval.getHigh()
						.compareTo(intervals.getState(bound).interval.getLow()) < 0)
					closure.add(bound);
			if (!closure.isEmpty())
				// glb is the union
				newBounds.putState(entry.getKey(),
						newBounds.getState(entry.getKey()).glb(new UpperBounds(closure)));
		}

		return new Pentagons(intervals.lub(other.intervals), newBounds);
	}

	@Override
	public Pentagons wideningAux(Pentagons other) throws SemanticException {
		return new Pentagons(intervals.widening(other.intervals), upperBounds.widening(other.upperBounds));
	}

	@Override
	public boolean lessOrEqualAux(Pentagons other) throws SemanticException {
		if (!intervals.lessOrEqual(other.intervals))
			return false;
		for (Entry<Identifier, UpperBounds> entry : other.upperBounds)
			for (Identifier bound : entry.getValue())
				if (!(upperBounds.getState(entry.getKey()).contains(bound)
						|| intervals.getState(entry.getKey()).interval.getHigh()
								.compareTo(intervals.getState(bound).interval.getLow()) < 0))
					return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(intervals, upperBounds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pentagons other = (Pentagons) obj;
		return Objects.equals(intervals, other.intervals) && Objects.equals(upperBounds, other.upperBounds);
	}

	@Override
	public String toString() {
		return representation().toString();
	}
}
