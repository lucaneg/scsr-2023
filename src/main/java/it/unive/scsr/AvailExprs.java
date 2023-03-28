package it.unive.scsr;
import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {
    private final ValueExpression expression;
    private final Identifier id;

    public AvailExprs() {
        this(null, null);
    }

    public AvailExprs(Identifier id, ValueExpression expression) {
        this.id = id;
        this.expression = expression;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();
        result.add(id);
        return result;
    }

    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp,
                                      DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        return new HashSet<>();
    }

    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp,
                                      DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        try {
            Float.parseFloat(expression.toString());
        } catch (Exception e) {
            String expr = expression.toString();
            if (expr.contains("+")
                    || expr.contains("-")
                    || expr.contains("*")
                    || expr.contains("/")
                    || expr.contains("%")) {
                if (!expr.contains("<")
                        && !expr.contains("<=")
                        && !expr.contains(">")
                        && !expr.contains(">=")
                        && !expr.contains("==")
                        && !expr.contains("!=")) {
                    AvailExprs ae = new AvailExprs(null, expression);
                    result.add(ae);
                }
            }
        }
        return result;
    }

    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        return new HashSet<>();
    }

    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> killed = new HashSet<>();
        for (AvailExprs avexp : domain.getDataflowElements()) {
            String strexp = avexp.expression.toString();
            String idstr = id.toString();
            if (strexp.contains(" " + idstr + " ")
                    || strexp.startsWith(idstr + " ")
                    || strexp.endsWith(" " + idstr)) {
                killed.add(avexp);
            }
        }
        return killed;
    }

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
