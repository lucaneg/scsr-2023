package it.unive.scsr.pentagons;

import it.unive.lisa.analysis.numeric.Interval;
import it.unive.lisa.symbolic.value.Identifier;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Set;

public class PentagonElement {
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

}
