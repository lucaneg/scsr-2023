package it.unive.scsr;

import org.junit.Test;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.LiSAConfiguration.GraphType;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.imp.IMPFrontend;
import it.unive.lisa.imp.ParsingException;
import it.unive.lisa.program.Program;

public class ExtSignDomainExample {

	@Test
	public void testAvailableExpressions() throws ParsingException, AnalysisException {
		// we parse the program to get the CFG representation of the code in it
		Program program = IMPFrontend.processFile("inputs/ext-sign.imp");

		// we build a new configuration for the analysis
		LiSAConfiguration conf = new LiSAConfiguration();

		// we specify where we want files to be generated
		conf.workdir = "outputs/ext-sign-example";

		// we specify the visual format of the analysis results
		conf.analysisGraphs = GraphType.HTML;

		// we specify the analysis that we want to execute
		conf.abstractState = new SimpleAbstractState<>(
				// heap domain
				new MonolithicHeap(),
				// value domain
				// NOTE: the following line won't compile unless you 
				// extend the right class in ExtSignDomain 
				new ValueEnvironment<>(new ExtSignDomain()),
				// type domain
				new TypeEnvironment<>(new InferredTypes()));

		// we instantiate LiSA with our configuration
		LiSA lisa = new LiSA(conf);

		// finally, we tell LiSA to analyze the program
		lisa.run(program);
	}
}
