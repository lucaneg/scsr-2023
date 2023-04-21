package it.unive.scsr;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

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
import it.unive.lisa.outputs.compare.JsonReportComparer;
import it.unive.lisa.outputs.json.JsonReport;
import it.unive.lisa.program.Program;

public class ExtSignDomainTaskEvaluation {

	@Test
	public void testExtendedSigns() throws ParsingException, AnalysisException {
		Program program = IMPFrontend.processFile("inputs/ext-sign-eval.imp");

		LiSAConfiguration conf = new LiSAConfiguration();
		conf.jsonOutput = true;
		conf.serializeResults = true;
		conf.workdir = "outputs/ext-sign";
		conf.abstractState = new SimpleAbstractState<>(
				new MonolithicHeap(),
				new ValueEnvironment<>(new ExtSignDomain()),
				new TypeEnvironment<>(new InferredTypes()));

		LiSA lisa = new LiSA(conf);
		lisa.run(program);

		Path expectedPath = Paths.get("expected", "ext-sign");
		Path actualPath = Paths.get("outputs", "ext-sign");

		File expFile = Paths.get(expectedPath.toString(), "report.json").toFile();
		File actFile = Paths.get(actualPath.toString(), "report.json").toFile();
		try {
			JsonReport expected = JsonReport.read(new FileReader(expFile));
			JsonReport actual = JsonReport.read(new FileReader(actFile));
			assertTrue("Results are different",
					JsonReportComparer.compare(expected, actual, expectedPath.toFile(), actualPath.toFile()));
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
			fail("Unable to find report file");
		} catch (IOException e) {
			e.printStackTrace(System.err);
			fail("Unable to compare reports");
		}
	}
}
