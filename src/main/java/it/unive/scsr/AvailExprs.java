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

/**
 * Available Expressions Analysis: this is a Forward and Definite Analysis
 */
public class AvailExprs
        implements DataflowElement<
        //type of dataflow domain
        DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private ValueExpression expression;

    public AvailExprs() {
        this(null);
    }

    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
    }

    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        // analyze expression to get identifiers used
        return getIdentifiersFromExpression(expression);
    }


    /**
     * Return identifiers involved in an expression, if any.
     * <p>
     * ValueExpression is abstract class extended by different kinds of expressions.
     * See <a href="https://lisa-analyzer.github.io/structure/symbolic.png">Symbolic Expression Structure</a>
     * <p>
     * In particular:
     * - Identifier: single identifier
     * - UnaryExpression: only one expression
     * - BinaryExpression: left and right expressions
     * - TernaryExpression: left, middle, right expressions
     * All other kind of ValueExpression doesn't contain identifiers.
     * <p>
     * Note: recursive analysis of expression is needed to find identifiers in left/middle/right part of expressions
     */
    private static Collection<Identifier> getIdentifiersFromExpression(SymbolicExpression expression) {
        HashSet<Identifier> identifiers = new HashSet<>();

        if (expression instanceof Identifier) {
            identifiers.add((Identifier) expression); // single identifier
        } else if (expression instanceof UnaryExpression) {
            // In UnaryExpression need to check single expression
            UnaryExpression unaryExpr = (UnaryExpression) expression;
            identifiers.addAll(getIdentifiersFromExpression(unaryExpr.getExpression()));
        } else if (expression instanceof BinaryExpression) {
            // In BinaryExpression need to check left and right expressions
            BinaryExpression binaryExpr = (BinaryExpression) expression;
            identifiers.addAll(getIdentifiersFromExpression(binaryExpr.getLeft()));
            identifiers.addAll(getIdentifiersFromExpression(binaryExpr.getRight()));
        } else if (expression instanceof TernaryExpression) {
            // In TernaryExpression need to check left, middle and right expressions
            TernaryExpression ternaryExpr = (TernaryExpression) expression;
            identifiers.addAll(getIdentifiersFromExpression(ternaryExpr.getLeft()));
            identifiers.addAll(getIdentifiersFromExpression(ternaryExpr.getMiddle()));
            identifiers.addAll(getIdentifiersFromExpression(ternaryExpr.getRight()));
        }
        // anything else doesn't contain identifiers

        return identifiers;
    }


    /**
     * Check if an expression can be used in the analysis.
     * Note that constants, skips and top values must be ignored in the analysis.
     */
    private static boolean canBeUsed(ValueExpression expression) {
        if (expression instanceof Identifier
                || expression instanceof Constant   // constant value expression
                || expression instanceof Skip       // expression that does nothing
                || expression instanceof PushAny)   // expression to push value to stack
            return false;
        return true;
    }

    /**
     * Gen set for assignment statement (i.e. id = expression)
     * Expression evaluated without subsequently redefining its operands.
     */
    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        HashSet<AvailExprs> generated = new HashSet<>();
        AvailExprs ae = new AvailExprs(expression);
        Collection<Identifier> involvedIdentifiers = ae.getInvolvedIdentifiers();

        if (!involvedIdentifiers.contains(id) && canBeUsed(expression)) {
            generated.add(ae);
        }

        return generated;
    }

    /**
     * Gen set for non-assignment statement.
     * Just an expression evaluation.
     */
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        HashSet<AvailExprs> generated = new HashSet<>();
        AvailExprs ae = new AvailExprs(expression);

        if (canBeUsed(expression)) {
            generated.add(ae);
        }

        return generated;
    }

    /**
     * Kill set for assignment statement.
     * Expressions whose operands are redefined in block without re-evaluating the expression afterwards
     */
    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> killed = new HashSet<>();

        for (AvailExprs ae : domain.getDataflowElements()) { // check all existing expressions
            Collection<Identifier> identifiersInvolved = ae.getInvolvedIdentifiers();

            if (identifiersInvolved.contains(id)) {
                killed.add(ae); // add expression to kill set
            }

        }
        return killed;
    }

    /**
     * Kill set for non-assignment statement.
     * All expressions are still available since there are no assignments (kill set is empty)
     */
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        // No assignment is performed, no expressions are killed, so empty set
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
