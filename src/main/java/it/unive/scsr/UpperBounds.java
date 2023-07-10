package it.unive.scsr;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.SetRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonEq;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonGt;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLe;
import it.unive.lisa.symbolic.value.operator.binary.ComparisonLt;

public class UpperBounds extends BaseNonRelationalValueDomain<UpperBounds> implements Iterable<Identifier> {

	private static final UpperBounds TOP = new UpperBounds(true);
	private static final UpperBounds BOTTOM = new UpperBounds(false);

	private final boolean isTop;
	private final Set<Identifier> bounds;

	public UpperBounds(boolean isTop) {
		this.bounds = null;
		this.isTop = isTop;
	}

	public UpperBounds(Set<Identifier> bounds) {
		this.bounds = bounds;
		this.isTop = false;
	}

	@Override
	public DomainRepresentation representation() {
		if (isTop())
			return new StringRepresentation("{}");
		if (isBottom())
			return Lattice.bottomRepresentation();
		return new SetRepresentation(bounds, StringRepresentation::new);
	}

	@Override
	public UpperBounds top() {
		return TOP;
	}

	@Override
	public UpperBounds bottom() {
		return BOTTOM;
	}

	@Override
	public UpperBounds lubAux(UpperBounds other) throws SemanticException {
		Set<Identifier> lub = new HashSet<>(bounds);
		lub.retainAll(other.bounds);
		return new UpperBounds(lub);
	}
	
	@Override
	public UpperBounds glbAux(UpperBounds other) throws SemanticException {
		Set<Identifier> lub = new HashSet<>(bounds);
		lub.addAll(other.bounds);
		return new UpperBounds(lub);
	}

	@Override
	public boolean lessOrEqualAux(UpperBounds other) throws SemanticException {
		return bounds.containsAll(other.bounds);
	}

	@Override
	public UpperBounds wideningAux(UpperBounds other) throws SemanticException {
		return other.bounds.containsAll(bounds) ? other : TOP;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UpperBounds other = (UpperBounds) obj;
		return Objects.equals(bounds, other.bounds) && isTop == other.isTop;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bounds, isTop);
	}

	@Override
	public ValueEnvironment<UpperBounds> assumeBinaryExpression(
			ValueEnvironment<UpperBounds> environment,
			BinaryOperator operator,
			ValueExpression left,
			ValueExpression right,
			ProgramPoint pp)
			throws SemanticException {
		if (!(left instanceof Identifier && right instanceof Identifier))
			return super.assumeBinaryExpression(environment, operator, left, right, pp);

		Identifier x = (Identifier) left;
		Identifier y = (Identifier) right;

		// glb is the union!
		
		if (operator instanceof ComparisonEq) {
			// x == y
			UpperBounds set = environment.getState(x).glb(environment.getState(y));
			return environment.putState(x, set).putState(y, set);
		}

		if (operator instanceof ComparisonLt) {
			// x < y
			UpperBounds set = environment.getState(x).glb(environment.getState(y))
					.glb(new UpperBounds(Collections.singleton(y)));
			return environment.putState(x, set);
		}

		if (operator instanceof ComparisonLe) {
			// x <= y
			UpperBounds set = environment.getState(x).glb(environment.getState(y));
			return environment.putState(x, set);
		}

		if (operator instanceof ComparisonGt) {
			// x > y ---> y < x
			UpperBounds set = environment.getState(x).glb(environment.getState(y))
					.glb(new UpperBounds(Collections.singleton(x)));
			return environment.putState(y, set);
		}

		if (operator instanceof ComparisonGe) {
			// x >= y --- > y <= x
			UpperBounds set = environment.getState(x).glb(environment.getState(y));
			return environment.putState(y, set);
		}

		return super.assumeBinaryExpression(environment, operator, left, right, pp);
	}

	@Override
	public Iterator<Identifier> iterator() {
		if (bounds == null)
			return Collections.emptyIterator();
		return bounds.iterator();
	}
	
	public boolean contains(Identifier id) {
		return bounds != null && bounds.contains(id);
	}
	
	public UpperBounds add(Identifier id) {
		Set<Identifier> res = new HashSet<>();
		if (!isTop() && !isBottom())
			res.addAll(bounds);
		res.add(id);
		return new UpperBounds(res);
	}
}
