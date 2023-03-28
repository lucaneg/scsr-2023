package it.unive.scsr;

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
import java.util.Collection;
import java.util.HashSet;


public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs>  {

	private final ValueExpression expression;

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

	private static boolean filter(ValueExpression expression) {
		return !(expression instanceof Identifier || expression instanceof Constant || expression instanceof Skip || expression instanceof PushAny);
	}

	public AvailExprs() {
		this(null);
	}

	public AvailExprs(ValueExpression expression) {
		this.expression=expression;
	}

	@Override
	public Collection<Identifier> getInvolvedIdentifiers() {
		return getIdentifiers(expression);
	}

	public Collection<Identifier> getIdentifiers(ValueExpression expression) {
		Collection<Identifier> result = new HashSet<>();

		if (expression == null)
			return result;

		if (expression instanceof Identifier)
			result.add((Identifier) expression);

		if (expression instanceof UnaryExpression){
			UnaryExpression unaryExpr = (UnaryExpression) expression;
            AvailExprs unary = new AvailExprs((ValueExpression) unaryExpr.getExpression());
			result.addAll(unary.getInvolvedIdentifiers());
		}

		if (expression instanceof BinaryExpression) {
			BinaryExpression binaryExpr = (BinaryExpression) expression;
			AvailExprs left = new AvailExprs((ValueExpression) binaryExpr.getLeft());
            AvailExprs right = new AvailExprs((ValueExpression) binaryExpr.getRight());
			result.addAll(left.getInvolvedIdentifiers());
            result.addAll(right.getInvolvedIdentifiers());

		}

		if (expression instanceof TernaryExpression) {
            TernaryExpression ternaryExpr = (TernaryExpression) expression;
            AvailExprs left = new AvailExprs((ValueExpression) ternaryExpr.getLeft());
            AvailExprs middle = new AvailExprs((ValueExpression) ternaryExpr.getMiddle());
            AvailExprs right = new AvailExprs((ValueExpression) ternaryExpr.getRight());
            result.addAll(left.getInvolvedIdentifiers());
            result.addAll(middle.getInvolvedIdentifiers());
            result.addAll(right.getInvolvedIdentifiers());
		}
		return result;
	}

	@Override
	public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
		Collection<AvailExprs> result = new HashSet<>();
		AvailExprs avEexpr = new AvailExprs(expression);
		if (filter(expression))
			result.add(avEexpr);
		return result;
	}

	@Override
	public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
		Collection<AvailExprs> result = new HashSet<>();
		AvailExprs avEexpr = new AvailExprs(expression);
		if (!avEexpr.getInvolvedIdentifiers().contains(id) && filter(expression))
			result.add(avEexpr);
		return result;
	}

	@Override
	public Collection<AvailExprs> kill(Identifier id, ValueExpression expression,ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
		Collection<AvailExprs> result = new HashSet<>();
		for (AvailExprs avEexpr : domain.getDataflowElements()) {
			Collection<Identifier> identifiers = getIdentifiers(avEexpr.expression);
			if (identifiers.contains(id))
				result.add(avEexpr);
		}
		return result;
	}

	@Override
	public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain){
		return new HashSet<>();
	}

}
