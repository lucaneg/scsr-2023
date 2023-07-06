package test.java.it.unive;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;
import org.junit.Test;

public class PentagonsTest {

    @Test
    public void testPentagons() throws ParsingException, AnalysisException {

        LiSAConfiguration lisaConfig = new LiSAConfiguration();

        Program prog = IMPFrontend.processFile("inputs/pentagons.imp");
        
        lisaConfig.workdir = "outputs/pentagons";

        lisaConfig.analysisGraphs = LiSAConfiguration.GraphType.HTML;

      
        lisaConfig.abstractState = new SimpleAbstractState<>(
                
                new MonolithicHeap(),
                
                new PentagonsDomain(),
            
                new TypeEnvironment<>(new InferredTypes()));

        LiSA lisa = new LiSA(lisaConfig);

        lisa.run(prog);
    }

}