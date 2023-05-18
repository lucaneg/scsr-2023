package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.SetRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.symbolic.value.Identifier;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;
import java.util.stream.Collectors;

public class PentagonElement {
    public static PentagonElement TOP = new PentagonElement(Interval.TOP, Set.of());
    private Interval interval;
    private Set<Identifier> sub;

    public Interval getInterval() {
        return interval;
    }

    public Set<Identifier> getSub() {
        return sub;
    }

    public PentagonElement(Interval interval, Set<Identifier> sub) {
        this.interval = interval;
        this.sub = sub;
    }

    public Boolean isTop(){
        throw new NotImplementedException();
        // return false;
    }

    public DomainRepresentation representation(){
        return new StringRepresentation("Intv: " + intervalString() + ", Sub: " +  subString());
    }

    /**
     * @return interval's domain representation as a string
     * */
    private String intervalString(){
        return getInterval().representation().toString();
    }

    /**
     * @return sub's domain representation as a string
     * */
    private String subString(){
        return new SetRepresentation(getSub().stream().map( i -> new StringRepresentation(i.getName()) ).collect( Collectors.toSet())).toString();
    }

}
