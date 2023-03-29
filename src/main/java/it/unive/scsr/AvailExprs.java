package it.unive.scsr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.ValueExpression;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>,AvailExprs> {

    private final Identifier variable;
    private final ValueExpression expression;

    public AvailExprs() {
        this(null, null);
    }

    public AvailExprs(Identifier variable, ValueExpression expression) {
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
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
        AvailExprs other = (AvailExprs) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
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
    public Collection<AvailExprs> gen(
            Identifier variable,
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        try {
            Float.parseFloat(expression.toString());
        } catch (Exception e) {
            String expr_str = expression.toString();
            if (expr_str.contains("+")
                    || expr_str.contains("-")
                    || expr_str.contains("*")
                    || expr_str.contains("/")
                    || expr_str.contains("%")) {
                if (!expr_str.contains("<")
                        && !expr_str.contains("<=")
                        && !expr_str.contains(">")
                        && !expr_str.contains(">=")
                        && !expr_str.contains("==")
                        && !expr_str.contains("!=")) {
                    AvailExprs AE = new AvailExprs(null, expression);
                    result.add(AE);
                }
            }
        }
        return result;
    }

    @Override
    public Collection<AvailExprs> gen(
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {
        // if no assignment is performed, no element is generated!
        return new HashSet<>();
    }

    @Override
    public Collection<AvailExprs> kill(
            Identifier variable,
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {
        Set<AvailExprs> killed = new HashSet<>();
        for (AvailExprs AE : domain.getDataflowElements()) {
            String str_exp = AE.expression.toString();
            String var_str = variable.toString();
            if (str_exp.contains(" " + var_str + " ")
                    || str_exp.startsWith(var_str + " ")
                    || str_exp.endsWith(" " + var_str)) {
                killed.add(AE);
            }
        }
        return killed;
    }

    @Override
    public Collection<AvailExprs> kill(
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {
        // if no assignment is performed, no element is killed!
        return new HashSet<>();
    }



    @Override
    public DomainRepresentation representation() {
                return new StringRepresentation(expression);

    }

    

    @Override
    public AvailExprs pushScope(ScopeToken token) throws SemanticException {
        return this;
    }

    @Override
    public AvailExprs popScope(ScopeToken token) throws SemanticException {
        return this;
    }
}
