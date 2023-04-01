package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.AvailableExpressions;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime + ((expression == null) ? 0 : expression.hashCode());
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

    private static Set<Identifier> getInvolvedIdentifiers_sup(ValueExpression expression) {
        Set<Identifier> result = new HashSet<>();

        if (expression instanceof Identifier)
            result.add((Identifier) expression);

        if (expression instanceof UnaryExpression) {
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((UnaryExpression) expression).getExpression()));
        }

        if (expression instanceof BinaryExpression) {
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((BinaryExpression) expression).getLeft()));
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((BinaryExpression) expression).getRight()));
        }

        if (expression instanceof TernaryExpression) {
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((TernaryExpression) expression).getLeft()));
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((TernaryExpression) expression).getMiddle()));
            result.addAll(getInvolvedIdentifiers_sup((ValueExpression) ((TernaryExpression) expression).getRight()));
        }
        return result;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getInvolvedIdentifiers_sup(this.expression);
    }

    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {

        Set<AvailExprs> result = new HashSet<>();
        AvailExprs availExprs = new AvailExprs(expression);

        if ((expression instanceof Identifier ||
             expression instanceof UnaryExpression ||
             expression instanceof BinaryExpression  ||
             expression instanceof TernaryExpression) && !availExprs.getInvolvedIdentifiers().contains(id)) {
            result.add(availExprs);
        }

        return result;
    }

    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        AvailExprs availExprs = new AvailExprs(expression);

        if ((expression instanceof Identifier ||
                expression instanceof UnaryExpression ||
                expression instanceof BinaryExpression  ||
                expression instanceof TernaryExpression)) {
            result.add(availExprs);
        }

        return result;
    }

    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();

        for (AvailExprs availExprs : domain.getDataflowElements()) {
            if (availExprs.getInvolvedIdentifiers().contains(id))
                result.add(availExprs);
        }

        return result;
    }

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
        return this;
    }

    @Override
    public AvailExprs popScope(ScopeToken scope) throws SemanticException {
        return this;
    }
}
