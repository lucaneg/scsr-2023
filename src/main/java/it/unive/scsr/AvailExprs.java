package it.unive.scsr;

import it.unive.lisa.analysis.ScopeToken;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.dataflow.AvailableExpressions;
import it.unive.lisa.analysis.dataflow.DataflowElement;
import it.unive.lisa.analysis.dataflow.DefiniteForwardDataflowDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.*;

import java.util.Collection;
import java.util.HashSet;

public class AvailExprs implements DataflowElement<DefiniteForwardDataflowDomain<AvailExprs>, AvailExprs> {

    private ValueExpression expression;

    // empty constructor
    public AvailExprs() {
        this(null);
    }

    // constructor that builds the AvailExpr instance giving an expression
    public AvailExprs(ValueExpression expression) {
        this.expression = expression;
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



    public Collection<Identifier> getInvolvedIdentifiersAux(ValueExpression expression){
        Collection<Identifier> identifiers = new HashSet<>();
        if(expression != null){
			/* if the expression is not null, we extract the identifiers contained
			in it. Operations are different according to the subtype of ValueExpression*/
            if(expression instanceof Identifier){
                // the expression is an identifier itself, so we just add it to the set
                identifiers.add((Identifier) expression);
            }else{
                if(expression instanceof UnaryExpression){
                    // we have a UnaryExpression, so we extract the ValueExpression apart from the operand and then extract the identifiers
                    SymbolicExpression exp = ((UnaryExpression)expression).getExpression();
                    identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) exp));
                }
                else{
                    if(expression instanceof BinaryExpression){
                        // we have a Binary expression, so we extract the left and right expression apart from the operand and then extract the identifiers
                        SymbolicExpression expLeft = ((BinaryExpression) expression).getLeft();
                        SymbolicExpression expRight = ((BinaryExpression)expression).getRight();
                        identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) expLeft));
                        identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) expRight));
                    }
                    else{
                        if(expression instanceof TernaryExpression){
                            SymbolicExpression expLeft = ((TernaryExpression)expression).getLeft();
                            SymbolicExpression expMiddle = ((TernaryExpression)expression).getMiddle();
                            SymbolicExpression expRight = ((TernaryExpression)expression).getRight();
                            identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) expLeft));
                            identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) expMiddle));
                            identifiers.addAll(getInvolvedIdentifiersAux((ValueExpression) expRight));
                        }
                    }
                }
            }
        }
        return identifiers;
    }

    // this method return the list of the identifier involved in an expression
    @Override
    public Collection<Identifier> getInvolvedIdentifiers() {
        return getInvolvedIdentifiersAux(this.expression);
    }




    private boolean isValidExp(ValueExpression expression){
        if((expression instanceof Identifier) || (expression instanceof Constant) || (expression instanceof Skip) || (expression instanceof PushAny))
            return false;
        return true;
    }


    @Override
    public Collection<AvailExprs> gen(
            Identifier id,
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {

        // an AvailExprs domain element is generated if in the statement there are no assignment on identifiers involved in expression in the same node

        Collection<AvailExprs> c = new HashSet<>();

        if(expression!=null) {
            if (!((getInvolvedIdentifiersAux(expression)).contains(id))) {
                if(isValidExp(expression))
                    c.add(new AvailExprs(expression));
            }
        }
        return c;
    }




    @Override
    public Collection<AvailExprs> gen(
            ValueExpression expression,
            ProgramPoint pp,
            DefiniteForwardDataflowDomain<AvailExprs> domain)
            throws SemanticException {

        Collection<AvailExprs> c = new HashSet<>();

        // if there's no assignment, the expression is generated for sure
        if(expression != null){
            if(isValidExp(expression))
                c.add(new AvailExprs(expression));
        }
        return c;
    }



    @Override
    // an expression is killed if the identifier id is contained in the expression
    public Collection<AvailExprs> kill(Identifier id, ValueExpression expression, ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) {
        Collection<AvailExprs> result = new HashSet<>();
        for (AvailExprs ae : domain.getDataflowElements()) {
            Collection<Identifier> ids = getInvolvedIdentifiersAux(ae.expression);
            if (ids.contains(id))
                result.add(ae);
        }
        return result;
    }

    @Override
    // kill set is empty if there's no assignment
    public Collection<AvailExprs> kill(ValueExpression expression, ProgramPoint pp,
                                       DefiniteForwardDataflowDomain<AvailExprs> domain) {

        return new HashSet<>();
    }























}