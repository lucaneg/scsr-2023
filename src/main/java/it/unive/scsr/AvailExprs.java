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
import java.util.Set;
import java.util.HashSet;

public class AvailExprs implements DataflowElement< DefiniteForwardDataflowDomain< AvailExprs >, AvailExprs > {

	// IMPLEMENTATION NOTE:
	// the code below is outside of the scope of the course. You can uncomment
	// it to get your code to compile. Be aware that the code is written
	// expecting that a field named "expression" of type ValueExpression exists
	// in this class: if you name it differently, change also the code below to
	// make it work by just using the name of your choice instead of
	// "expression". If you don't have a field of type ValueExpression in your
	// solution, then you should make sure that what you are doing is correct :)
    private final ValueExpression expression;

	public AvailExprs() { this( null ); }
    public AvailExprs( ValueExpression expr ) { this.expression = expr; }

	private Collection< Identifier > getInvolvedIdentifiers( ValueExpression expr ) {
		Set< Identifier > res = new HashSet<>();

		if ( expr instanceof Identifier ) {
			res.add( ( Identifier ) expr );
		}
		if ( expr instanceof UnaryExpression ) {
			res.addAll( getInvolvedIdentifiers(
					( ValueExpression ) ( ( UnaryExpression ) expr ).getExpression()
			) );
		}
		if ( expr instanceof BinaryExpression) {
			BinaryExpression binexpr = ( BinaryExpression ) expr;
			res.addAll( getInvolvedIdentifiers( ( ValueExpression ) binexpr.getLeft() ) );
			res.addAll( getInvolvedIdentifiers( ( ValueExpression ) binexpr.getRight() ) );
		}
		if ( expr instanceof TernaryExpression) {
			TernaryExpression terexpr = ( TernaryExpression ) expr;
			res.addAll( getInvolvedIdentifiers( ( ValueExpression ) terexpr.getLeft() ) );
			res.addAll( getInvolvedIdentifiers( ( ValueExpression ) terexpr.getMiddle() ) );
			res.addAll( getInvolvedIdentifiers( ( ValueExpression ) terexpr.getRight() ) );
		}
		return res;
	}
    public Collection< Identifier > getInvolvedIdentifiers() {
        return getInvolvedIdentifiers( expression );
    }

	@Override
	public Collection< AvailExprs > kill( Identifier id,
										  ValueExpression expression,
										  ProgramPoint pp,
										  DefiniteForwardDataflowDomain< AvailExprs > domain
	) throws SemanticException {
		Set< AvailExprs > kill = new HashSet<>();
		for ( AvailExprs aexpr : domain.getDataflowElements() ) {
			if ( aexpr.getInvolvedIdentifiers().contains( id ) )
				kill.add( aexpr );
		}
		return kill;
	}

	@Override
	public Collection< AvailExprs > kill( ValueExpression expression,
									      ProgramPoint pp,
									      DefiniteForwardDataflowDomain< AvailExprs > domain
	) throws SemanticException {
		return new HashSet<>();
	}

	private boolean isRelevant( ValueExpression expr ) {
		return expr instanceof UnaryExpression
			|| expr instanceof BinaryExpression
			|| expr instanceof TernaryExpression;
	}

	@Override
	public Collection< AvailExprs > gen( ValueExpression expression,
										 ProgramPoint pp,
										 DefiniteForwardDataflowDomain< AvailExprs > domain
	) throws SemanticException {
		Collection< AvailExprs > res = new HashSet<>();
		if ( isRelevant( expression ) )
			res.add( new AvailExprs( expression ) );
		return res;
	}

	@Override
	public Collection< AvailExprs > gen( Identifier id,
										 ValueExpression expression,
										 ProgramPoint pp,
										 DefiniteForwardDataflowDomain< AvailExprs > domain
	) throws SemanticException {
		Collection< AvailExprs > res = new HashSet<>();
		if ( isRelevant( expression ) ) {
			AvailExprs aexpr = new AvailExprs( expression );
			if ( !aexpr.getInvolvedIdentifiers().contains( id ) )
				res.add( aexpr );
		}
		return res;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( expression== null ) ? 0 : expression.hashCode() );
		return result;
	}

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		AvailExprs friend = ( AvailExprs ) obj;
		return expression.equals( friend.expression );
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
