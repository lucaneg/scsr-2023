package it.unive.scsr;

import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;

public class StrictUpperBounds extends BaseNonRelationalValueDomain<StrictUpperBounds> {
    @Override
    public StrictUpperBounds lubAux(StrictUpperBounds other) throws SemanticException {
        return null;
    }

    @Override
    public boolean lessOrEqualAux(StrictUpperBounds other) throws SemanticException {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public DomainRepresentation representation() {
        return null;
    }

    @Override
    public StrictUpperBounds top() {
        return null;
    }

    @Override
    public StrictUpperBounds bottom() {
        return null;
    }
}
