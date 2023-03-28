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
        this.expression = null;
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return this.getInvolvedIdentifiers(this.expression);
    }

    public Collection<Identifier> getInvolvedIdentifiers(ValueExpression expression) {
        Collection<Identifier> col = new HashSet<Identifier>();
        if (expression != null) {
            if (expression instanceof Identifier) {
                col.add((Identifier) expression);
            }
            else if (expression instanceof UnaryExpression) {
                Collection<Identifier> unarye = getInvolvedIdentifiers((ValueExpression) ((UnaryExpression) expression).getExpression());
                col.addAll(unarye);
            }
            else if (expression instanceof BinaryExpression) {
                Collection<Identifier> left = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getLeft());
                col.addAll(left);
                Collection<Identifier> right = getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getRight());
                col.addAll(right);
            }
            else if (expression instanceof TernaryExpression) {
                Collection<Identifier> left = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getLeft());
                col.addAll(left);
                Collection<Identifier> middle = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getMiddle());
                col.addAll(middle);
                Collection<Identifier> right = getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getRight());
                col.addAll(right);
            }
        }
        return col;
    }

    //This function gen is used when there is an assignment where the variable on the left side is not contained on the right side
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> col = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        Collection<Identifier> ide = ae.getInvolvedIdentifiers();
        //check if the variable on the left side is not contained on the right side
        if (!ide.contains(id)) {
            //check the expression
            if (!(expression instanceof Constant)) {
                if (!(expression instanceof Identifier)) {
                    if (!(expression instanceof Skip)) {
                        col.add(ae);
                    }
                }
            }
        }
        return col;
    }

    //This function gen is used when there isn't an assignment, there is an expression
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> col = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        //check the expression
        if (!(expression instanceof Constant)) {
            if (!(expression instanceof Identifier)) {
                if (!(expression instanceof Skip)) {
                    col.add(ae);
                }
            }
        }
        return col;
    }

    //This function kill is used when there is an assignment where the variable on the left side is contained on the right side
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> col = new HashSet<AvailExprs>();
        Set<AvailExprs> setae = domain.getDataflowElements();
        for (AvailExprs ae : setae) {
            Collection<Identifier> ide = ae.getInvolvedIdentifiers();
            //check if the variable on the left side is contained on the right side
            if (ide.contains(id)) {
                col.add(ae);
            }
        }
        return col;
    }

    //This function kill is used when there isn't an assignment, there is an expression
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        return new HashSet<>();
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
