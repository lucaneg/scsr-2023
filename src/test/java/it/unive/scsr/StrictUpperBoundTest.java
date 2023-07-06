
package test.java.it.unive.scsr;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;
import org.junit.Test;

public class StrictUpperBoundTest {

    @Test
    public void testDomainAnalysis() throws ParsingException, AnalysisException {
     
        Program parsedProgram = IMPFrontend.processFile("inputs/strict-upper-bound.imp");

     
        LiSAConfiguration analysisConf = new LiSAConfiguration();

     
        analysisConf.workdir = "outputs/sub";

     
        analysisConf.analysisGraphs = LiSAConfiguration.GraphType.HTML;

        analysisConf.abstractState = new SimpleAbstractState<>(
                new MonolithicHeap(),
                new ValueEnvironment<>(new StrictUpperBounds()),
                new TypeEnvironment<>(new InferredTypes()));

        LiSA lisaAnalyzer = new LiSA(analysisConf);

        lisaAnalyzer.run(parsedProgram);
    }

}