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
import java.util.Set;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {
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
        Collection<Identifier> identifiersCollection = new HashSet<Identifier>();
        if (expression != null) {
            if (expression instanceof Identifier) {
                identifiersCollection.add((Identifier) expression);
            }
            else if (expression instanceof UnaryExpression) {
                //unary expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((UnaryExpression) expression).getExpression()));
            }
            else if (expression instanceof BinaryExpression) {
                //left expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getLeft()));
                //right expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((BinaryExpression) expression).getRight()));
            }
            else if (expression instanceof TernaryExpression) {
                //left expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getLeft()));
                //middle expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getMiddle()));
                //right expression
                identifiersCollection.addAll(getInvolvedIdentifiers((ValueExpression) ((TernaryExpression) expression).getRight()));
            }
        }
        return identifiersCollection;
    }

    @Override
    public Collection<AvailExprs> gen(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> collection = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        Collection<Identifier> involvedIdentifiers = ae.getInvolvedIdentifiers();
        //check if the variable on the left side is not also contained in the right one
        if (!involvedIdentifiers.contains(id)) {
            //check the expression
            if (!(expression instanceof Constant)) {
                if (!(expression instanceof Identifier)) {
                    if (!(expression instanceof Skip)) {
                        collection.add(ae);
                    }
                }
            }
        }
        return collection;
    }

    @Override
    public Collection<AvailExprs> gen(ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> collection = new HashSet<AvailExprs>();
        AvailExprs ae = new AvailExprs(expression);
        //check the type of the expression
        if (!(expression instanceof Constant)) {
            if (!(expression instanceof Identifier)) {
                if (!(expression instanceof Skip)) {
                    collection.add(ae);
                }
            }
        }
        return collection;
    }

    @Override
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp, DefiniteForwardDataflowDomain<AvailExprs> domain) throws SemanticException {
        Collection<AvailExprs> collection = new HashSet<AvailExprs>();
        Set<AvailExprs> dataflowElements = domain.getDataflowElements();
        for (AvailExprs ae : dataflowElements) {
            //get all the identifiers
            Collection<Identifier> identifiers = ae.getInvolvedIdentifiers();
            //here I check if the variable on the left side is contained on the right side too, if there is I add it to the collection
            if (identifiers.contains(id)) {
                collection.add(ae);
            }
        }
        return collection;
    }

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
}
