package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.ListRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

// the type of dataflow domain that we want to use with this analysis is the definite one
public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    /**
     * The expression available
     */
    private final ValueExpression expression;

    /**
     * The place in the program where the expression becomes available
     */
    private final CodeLocation definition;
    public AvailExprs() {
        this(null, null);
    }
    public AvailExprs(ValueExpression expression, CodeLocation definition) {
        this.expression = expression;
        this.definition = definition;
    }

    /**
     * Hash code computed as done in ReachingDefinitions.java
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        result = prime * result + ((expression == null) ? 0 : expression.hashCode());
        return result;
    }

    /**
     * equals computed as done in ReachingDefinitions.java
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AvailExprs other = (AvailExprs) obj;
        if (definition == null) {
            if (other.definition != null)
                return false;
        } else if (!definition.equals(other.definition))
            return false;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }

    /**
     * Getting the involved identifiers is the fundamental operation of the analysis. It is used to compute the gen and kill sets. In particular, we need to pay attention to the fact that the expression may be a
     * binary or ternary expression, and in that case we need to recursively call the method on the left, middle and right operands. In all other types of expressions, we don't store anything in the set. The list of subclasses (found on file LisA for dummies) of
     * ValueExpression are:
     * - BinaryExpression --> we need to get the involved identifiers from the left and right operands
     * - Constant (subclasses: NullConstant) --> we don't have any identifier
     * - Identifier (subclasses: HeapLocation, MemoryPointer, OutOfScopeIdentifier, Variable) --> we add simply the identifier (Base case of recursion)
     * - Skip --> for jumps instructions we have no identifier
     * - TernaryExpression --> we need to get the involved identifiers from the left, middle and right operands
     * - UnaryExpression --> we need to get the involved identifiers from the expression without the operator
     * - TypeConversion
     */
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        Set<Identifier> result = new HashSet<>();

        if (expression instanceof Identifier)
            result.add((Identifier) expression);
        else if (expression instanceof UnaryExpression)
            result.addAll(new AvailExprs((ValueExpression) ((UnaryExpression) expression).getExpression(), definition).getInvolvedIdentifiers());
        else if (expression instanceof BinaryExpression) {
            BinaryExpression bin_expr = (BinaryExpression) expression;
            result.addAll(new AvailExprs((ValueExpression) bin_expr.getLeft(), definition).getInvolvedIdentifiers());
            result.addAll(new AvailExprs((ValueExpression) bin_expr.getRight(), definition).getInvolvedIdentifiers());
        }
        else if (expression instanceof TernaryExpression) {
            TernaryExpression ter_expr = (TernaryExpression) expression;
            result.addAll(new AvailExprs((ValueExpression) ter_expr.getLeft(), definition).getInvolvedIdentifiers());
            result.addAll(new AvailExprs((ValueExpression) ter_expr.getMiddle(), definition).getInvolvedIdentifiers());
            result.addAll(new AvailExprs((ValueExpression) ter_expr.getRight(), definition).getInvolvedIdentifiers());
        }

        return result;

    }

    /**
     *
     * This method is used to compute the gen set when there is an assignment to an identifier. id = expression.
     * In order to provide the available expression that comes from this instruction we need to consider all the expression that has no identifiers inside equal to id. To do this we will use the getInvolvedIdentifiers().
     * One last thing to notice is that the only expressions that we want to consider are the ones that involve some operations. (so no constant, jump instructions or single variables)
     */
    @Override
    public Collection<AvailExprs> gen(Identifier id,
                                      ValueExpression expression,
                                      ProgramPoint pp,
                                      DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> result = new HashSet<>();

        AvailExprs expr = new AvailExprs(expression, pp.getLocation());
        //short-circuit evaluation -> if we have no useful expression we don't even compute the set of involved identifiers
        if ((expression instanceof BinaryExpression || expression instanceof TernaryExpression || expression instanceof UnaryExpression) && !expr.getInvolvedIdentifiers().contains(id))
            result.add(expr);
        return result;
    }

    /**
     * This method is used to compute the gen set when there is no assignment to an identifier.
     * We are only considering situation in which we are evaluating an expression. In this case it's much simpler, we just need to check if the expression is of the right type and then we add it to the gen set.
     */
    @Override
    public Collection<AvailExprs> gen(ValueExpression expression,
                                      ProgramPoint pp,
                                      DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {

        Collection<AvailExprs> result = new HashSet<>();
        if (expression instanceof BinaryExpression || expression instanceof TernaryExpression || expression instanceof UnaryExpression)
            result.add(new AvailExprs(expression, pp.getLocation()));
        return result;
    }

    /**
     * This method is used to compute the kill set when there is an assignment to an identifier. id = expression.
     * In this case we have to take in account all the available expressions that reach this point (stored in the domain) and check if some of them uses the identifier that is being assigned.
     * If this is the case we need to remove the expression from the available expressions set, by inserting it in the kill set.
     */
    @Override
    public Collection<AvailExprs> kill(Identifier id,
                                       ValueExpression expression,
                                       ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {

        Collection<AvailExprs> result = new HashSet<>();
        for (AvailExprs expr : domain.getDataflowElements()) {
            Collection<Identifier> expression_identifiers = expr.getInvolvedIdentifiers();
            if (expression_identifiers.contains(id))
                result.add(expr);
        }
        return result;
    }

    /**
     * This method is used to compute the kill set when there is no assignment to an identifier.
     * This is the simplest scenario. Since there are no assignment all the available expressions are still available. The kill set is therefore empty.
     */
    @Override
    public Collection<AvailExprs> kill(ValueExpression expression,
                                       ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {

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
        /*if we want to show the location of the expression we can use the following code
        return new ListRepresentation(
                new StringRepresentation(expression),
                new StringRepresentation(definition));*/
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
