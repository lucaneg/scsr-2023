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
import java.util.Objects;
import java.util.Set;
public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs>{
    private final ValueExpression expression;

    public AvailExprs() {
        this(null);
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (o == null) return false;
        else if (this.getClass() != o.getClass()) return false;
        else return Objects.equals(this.expression, ((AvailExprs) o).expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.expression);
    }

    @Override
    public String toString() {
        return this.representation().toString();
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return this.getInvolvedIdentifiers(this.expression);
    }

    public Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
        Collection<Identifier> res = new HashSet<Identifier>();
        if (expression != null) {
            if (expression instanceof Identifier) {
                res.add((Identifier) expression);
            }
            else if (expression instanceof UnaryExpression) {
                Collection<Identifier> ci = getInvolvedIdentifiers((ValueExpression) ((UnaryExpression) expression).getExpression());
                res.addAll(ci);
            }
            else if (expression instanceof BinaryExpression) {
                Collection<Identifier> cl = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getLeft());
                res.addAll(cl);
                Collection<Identifier> cr = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getRight());
                res.addAll(cr);
            }
            else if (expression instanceof TernaryExpression) {
                Collection<Identifier> cl = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getLeft());
                res.addAll(cl);
                Collection<Identifier> cm = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getMiddle());
                res.addAll(cm);
                Collection<Identifier> cr = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getRight());
                res.addAll(cr);
            }
        }
        return res;
    }

    //This function gen is used when there is an assignment where the variable on the left side is not contained on the right side
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> res = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        Collection<Identifier> ci = ae.getInvolvedIdentifiers();
        //check if the variable on the left side is not contained on the right side
        if (!ci.contains(id)) {
            //check the expression
            if (!(expression instanceof Constant)) {
                if (!(expression instanceof Identifier)) {
                    if (!(expression instanceof Skip)) {
                        res.add(ae);
                    }
                }
            }
        }
        return res;
    }

    //This function gen is used when there isn't an assignment, there is an expression
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> res = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        //check the expression
        if (!(expression instanceof Constant)) {
            if (!(expression instanceof Identifier)) {
                if (!(expression instanceof Skip)) {
                    res.add(ae);
                }
            }
        }
        return res;
    }

    //This function kill is used when there is an assignment where the variable on the left side is contained on the right side
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> res = new HashSet<AvailExprs>();
        Set<AvailExprs> df = domain.getDataflowElements();
        for (AvailExprs ae : df) {
            Collection<Identifier> ci = ae.getInvolvedIdentifiers();
            //check if the variable on the left side is contained on the right side
            if (ci.contains(id)) {
                res.add(ae);
            }
        }
        return res;
    }

    //This function kill is used when there isn't an assignment, there is an expression
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
		return new StringRepresentation(expression);
	}

	@Override
	public AvailExprs pushScope(ScopeToken scope) throws SemanticException {
		return new AvailExprs((ValueExpression) expression.pushScope(scope));
	}

    @Override
    public AvailExprs popScope(ScopeToken scope) throws SemanticException {
		return new AvailExprs((ValueExpression) expression.popScope(scope));
	}
}
