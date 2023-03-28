/**
 * @author Musone Mattia (877962)
 * @version 1.0.0 (27/03/2023)
 */

package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * We use DefiniteForwardDataflowDomain because the confluence operator of the Available expression is the intersection
 * in[B] = ∩ out[P], over the predecessors P of B
 *
 * @see DefiniteForwardDataflowDomain
 * */
public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private final ValueExpression expression;

    public AvailExprs() {
        this(null);
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }

    /**
     * Recursive function that extract identifiers from an expression
     * Due to documentation a ValueExpression is the symbolic expressions defined on constant values and identifiers can be:
     * <ul>
     *     <li>BinaryExpression</li>
     *     <li>Constant</li>
     *     <li>Identifier</li>
     *     <li>Skip</li>
     *     <li>TernaryExpression</li>
     *     <li>TypeConversion</li>
     *     <li>UnaryExpression</li>
     * </ul>
     * So let's verify what type of SymbolicExpression is and find out all the identifiers involved in those expressions
     *
     * @param expression The expression to evaluate
     * @return a collections of identifiers found in the expression
     * @see ValueExpression
     * @see UnaryExpression
     * @see BinaryExpression
     * @see TernaryExpression
     * @see Constant
     * @see Skip
     * @see Identifier
     * */
    private Collection<Identifier> getIdentifiersFromExpression(SymbolicExpression expression) {
        Collection<Identifier> set = new HashSet<>();
        if (expression instanceof UnaryExpression) {
            var un = (UnaryExpression) expression;
            set.addAll(getIdentifiersFromExpression(un.getExpression()));
        } else if (expression instanceof BinaryExpression) {
            var bin = (BinaryExpression) expression;
            set.addAll(getIdentifiersFromExpression(bin.getLeft()));
            set.addAll(getIdentifiersFromExpression(bin.getRight()));
        } else if (expression instanceof TernaryExpression) {
            var ter = (TernaryExpression) expression;
            set.addAll(getIdentifiersFromExpression(ter.getLeft()));
            set.addAll(getIdentifiersFromExpression(ter.getMiddle()));
            set.addAll(getIdentifiersFromExpression(ter.getRight()));
        } else if (expression instanceof Identifier) {
            var id = (Identifier) expression;
            set.add(id);
        } else if (expression instanceof Constant) {

        } else if (expression instanceof Skip) {

        }
        return set;
    }

    /**
     * Check if the given expression is useful for gen/kill functions
     *
     * @param expression the expression to evaluate
     * @return true if the expression is a UnaryExpression or BinaryExpression or TernaryExpression, false otherwise
     * @see ValueExpression
     * @see UnaryExpression
     * @see BinaryExpression
     * @see TernaryExpression
     * */
    private boolean isUsefulExpression(ValueExpression expression) {
        return expression instanceof UnaryExpression || expression instanceof BinaryExpression || expression instanceof TernaryExpression;
    }

    /**
     * Return the involved identifiers in this analysis
     *
     * @return The collection of identifiers
     * @see Identifier
     * */
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getIdentifiersFromExpression(expression);
    }

    /**
     * gen function when an assignment is taking place
     *
     * We know that the gen works like:
     * gen[B] = {expressions evaluated in B without subsequently redefining its operands}
     * we check if the operations is redefined by checking if there are the id that we are considering in the previous expression using the getIdentifiersFromExpressionfunction
     * we only add to our set the expressions, so we remove unecessary stuff
     *
     * @param id the identifier of the statement (left value)
     * @param expression the expression of the statement (right value)
     * @param pp representing an instruction that is happening in one of the CFG under analysis
     * @param domain the domain of the statement of the analysis
     * @return A collection of generated AvailExprs that could be empty if none are generated
     * @see Identifier
     * @see ValueExpression
     * @see ProgramPoint
     * @see DefiniteForwardDataflowDomain
     * @see AvailExprs
     * */
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        if (isUsefulExpression(expression) && !getIdentifiersFromExpression(expression).contains(id))
            result.add(new AvailExprs(expression));
        return result;
    }

    /**
     * gen function when no assignment is taking place
     *
     * We know that the gen works like:
     * gen[B] = {expressions evaluated in B without subsequently redefining its operands}
     * we check if the operations is redefined by checking if there are the id that we are considering in the previous expression
     * we only add to our set the expressions, so we remove unecessary stuff
     *
     * @param expression the expression of the statement (right value)
     * @param pp representing an instruction that is happening in one of the CFG under analysis
     * @param domain the domain of the statement of the analysis
     * @return A collection of generated AvailExprs that could be empty if none are generated
     * @see ValueExpression
     * @see ProgramPoint
     * @see DefiniteForwardDataflowDomain
     * @see AvailExprs
     * */
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> result = new HashSet<>();
        if (isUsefulExpression(expression))
            result.add(new AvailExprs(expression));
        return result;
    }

    /**
     * kill function when an assignment is taking place
     * We know that the kill works like:
     * kill[B] = {expressions whose operands are redefined in B without reevaluating the expression afterwards}
     *
     * @param id the identifier of the statement (left value)
     * @param expression the expression of the statement (right value)
     * @param pp representing an instruction that is happening in one of the CFG under analysis
     * @param domain the domain of the statement of the analysis
     * @return A collection of killed AvailExprs that could be empty if none are killed
     * @see ValueExpression
     * @see ProgramPoint
     * @see DefiniteForwardDataflowDomain
     * @see AvailExprs
     * */
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Set<AvailExprs> killed = new HashSet<>();
        for (AvailExprs rd : domain.getDataflowElements()) {
            if (rd.getInvolvedIdentifiers().contains(id))
                killed.add(rd);
        }
        return killed;
    }

    /**
     * kill function when no assignment is taking place
     * We know that the kill works like:
     * kill[B] = {expressions whose operands are redefined in B without reevaluating the expression afterwards}
     *
     * @param expression the expression of the statement (right value)
     * @param pp representing an instruction that is happening in one of the CFG under analysis
     * @param domain the domain of the statement of the analysis
     * @return an empty set, because there is no kill in the expression itself, it must be assigned to kill the previous ones
     * @see ValueExpression
     * @see ProgramPoint
     * @see DefiniteForwardDataflowDomain
     * @see AvailExprs
     * */
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
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
}
