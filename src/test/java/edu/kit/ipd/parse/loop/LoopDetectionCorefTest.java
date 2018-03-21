package edu.kit.ipd.parse.loop;

import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.actionRecognizer.ActionRecognizer;
import edu.kit.ipd.parse.contextanalyzer.ContextAnalyzer;
import edu.kit.ipd.parse.corefanalyzer.CorefAnalyzer;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.ontology_connection.Domain;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;

public class LoopDetectionCorefTest {

	private static ShallowNLP snlp;
	private static GraphBuilder graphBuilder;
	private static ActionRecogMock actionRecog;
	private static SRLabeler srLabeler;
	private static NERTagger ner;
	private static ContextAnalyzer context;
	private static CorefAnalyzer coref;
	private PrePipelineData ppd;
	private static Properties props;
	private static LoopDetectionAgent loopDetectAgent;

	@BeforeClass
	public static void setUp() {
		props = ConfigManager.getConfiguration(LoopDetectionAgent.class);
		props.setProperty("COREF", "true");
		props = ConfigManager.getConfiguration(Domain.class);
		props.setProperty("ONTOLOGY_PATH", "/ontology.owl");
		props.setProperty("SYSTEM", "System");
		props.setProperty("METHOD", "Method");
		props.setProperty("PARAMETER", "Parameter");
		props.setProperty("DATATYPE", "DataType");
		props.setProperty("VALUE", "Value");
		props.setProperty("STATE", "State");
		props.setProperty("OBJECT", "Object");
		props.setProperty("SYSTEM_HAS_METHOD", "hasMethod");
		props.setProperty("STATE_ASSOCIATED_STATE", "associatedState");
		props.setProperty("STATE_ASSOCIATED_OBJECT", "associatedObject");
		props.setProperty("STATE_CHANGING_METHOD", "changingMethod");
		props.setProperty("METHOD_CHANGES_STATE", "changesStateTo");
		props.setProperty("METHOD_HAS_PARAMETER", "hasParameter");
		props.setProperty("OBJECT_HAS_STATE", "hasState");
		props.setProperty("OBJECT_SUB_OBJECT", "subObject");
		props.setProperty("OBJECT_SUPER_OBJECT", "superObject");
		props.setProperty("PARAMETER_OF_DATA_TYPE", "ofDataType");
		props.setProperty("DATATYPE_HAS_VALUE", "hasValue");
		props.setProperty("PRIMITIVE_TYPES", "String,int,double,float,short,char,boolean,long");
		graphBuilder = new GraphBuilder();
		graphBuilder.init();
		srLabeler = new SRLabeler();
		srLabeler.init();
		snlp = new ShallowNLP();
		snlp.init();
		ner = new NERTagger();
		ner.init();
		actionRecog = new ActionRecogMock();
		actionRecog.init();
		context = new ContextAnalyzer();
		context.init();
		coref = new CorefAnalyzer();
		coref.init();
		loopDetectAgent = new LoopDetectionAgent();
		loopDetectAgent.init();
	}

	@Test
	public void wrappingCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "go to the fridge and open it while the fridge and the dishwasher are white";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "while" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpanDependentNodes = new int[] { 0, 6 };

		Assert.assertEquals(expectedSpanDependentNodes[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpanDependentNodes[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

		int[] expectedSpanCondition = new int[] { 8, 14 };
		Assert.assertEquals(expectedSpanCondition[0], loop.getKeyphrase().getConditionNodes().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpanCondition[1], loop.getKeyphrase().getConditionNodes()
				.get(loop.getKeyphrase().getConditionNodes().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void forLoopCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "take the shirts and fold them twice";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "twice" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpan = new int[] { 0, 5 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void forLoopTimesCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "take the shirts and fold them two times";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "two", "times" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 0, 5 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void whileOpeningCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the fridge and while the fridge is open take out the beverages and put them on the table";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "and", "while" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		String[] expectedCondition = new String[] { "the", "fridge", "is", "open" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 9, 18 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void whileEndingCorefTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "go to the fridge and take out the beverages from it until the fridge is blue";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "until" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		String[] expectedCondition = new String[] { "the", "fridge", "is", "blue" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 0, 10 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	private IGraph executePreviousStages(PrePipelineData ppd) {
		IGraph result = null;
		try {
			snlp.exec(ppd);
			srLabeler.exec(ppd);
			ner.exec(ppd);
			graphBuilder.exec(ppd);
		} catch (PipelineStageException e) {
			e.printStackTrace();
		}
		try {
			actionRecog.setGraph(ppd.getGraph());
			actionRecog.exec();
			context.setGraph(actionRecog.getGraph());
			context.exec();
			coref.setGraph(context.getGraph());
			coref.exec();
			result = coref.getGraph();
		} catch (MissingDataException e) {
			e.printStackTrace();
		}

		return result;
	}

	private static class ActionRecogMock extends ActionRecognizer {

		public ActionRecogMock() {
			super();
		}

		@Override
		public void exec() {
			super.exec();
		}
	}

}
