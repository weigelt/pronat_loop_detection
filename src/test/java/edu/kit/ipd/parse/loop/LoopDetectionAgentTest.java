package edu.kit.ipd.parse.loop;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.parse.actionRecognizer.ActionRecognizer;
import edu.kit.ipd.parse.graphBuilder.GraphBuilder;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.data.PrePipelineData;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.pipeline.PipelineStageException;
import edu.kit.ipd.parse.luna.tools.StringToHypothesis;
import edu.kit.ipd.parse.ner.NERTagger;
import edu.kit.ipd.parse.shallownlp.ShallowNLP;
import edu.kit.ipd.parse.srlabeler.SRLabeler;

public class LoopDetectionAgentTest {

	private static ShallowNLP snlp;
	private static GraphBuilder graphBuilder;
	private static ActionRecogMock actionRecog;
	private static SRLabeler srLabeler;
	private static NERTagger ner;
	private static LoopDetectionAgent loopDetectAgent;
	private PrePipelineData ppd;
	static ParseGraph pg;
	static INodeType nodeType;

	@BeforeClass
	public static void setUp() {
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
		loopDetectAgent = new LoopDetectionAgent();
		loopDetectAgent.init();
	}

	@Test
	public void wrappingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the fridge while the fridge and the dishwasher are white";
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
		int[] expectedSpanDependentNodes = new int[] { 0, 2 };

		Assert.assertEquals(expectedSpanDependentNodes[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpanDependentNodes[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

		int[] expectedSpanCondition = new int[] { 4, 10 };
		Assert.assertEquals(expectedSpanCondition[0], loop.getKeyphrase().getConditionNodes().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpanCondition[1], loop.getKeyphrase().getConditionNodes()
				.get(loop.getKeyphrase().getConditionNodes().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void forLoopTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the dog jumps and the horse looks twice";
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
		int[] expectedSpan = new int[] { 4, 6 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void forLoopTimesTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the dog jumps and the horse looks two times";
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
		int[] expectedSpan = new int[] { 4, 6 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void whileOpeningTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the fridge and while the fridge is open take out the beverages";
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
		int[] expectedSpan = new int[] { 9, 12 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void whileEndingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the fridge and take out the beverages until the fridge is blue";
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
		int[] expectedSpan = new int[] { 4, 7 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Ignore("TODO")
	@Test
	public void noLoopTest() { // loop steckt implizit in der Bedeutung von "to empty"
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the dishwasher empty the dishwasher";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		int[] expectedSpan = new int[] { 0, 0 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Ignore("TODO")
	@Test
	public void endingTest2() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "open the dishwasher then take each piece of dishware out successively";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "successively" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 3, 9 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("TODO")
	@Test
	public void endingTest3() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "I want you to empty the dishwasher you need to open it first then grab the dishes out one after another";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "one", "after", "another" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 13, 17 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("one after another not in span")
	@Test
	public void whileEndingTest2() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "I want you to empty the dishwasher you need to open it first then grab the dishes out one after another until it is empty";
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
		String[] expectedCondition = new String[] { "it", "is", "empty" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 13, 20 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("NPB VP NEG ADJP")
	@Test
	public void whileEndingTest3() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "go to the dishwasher and open it as long as the dishwasher is not empty take a dish out of it and store it";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "as", "long", "as" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		String[] expectedCondition = new String[] { "the", "dishwasher", "is", "not", "empty" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 15, 23 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("TODO")
	@Test
	public void whileEndingTest4() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "take a piece of dishware out of the dishwasher if it is not empty then take the others out one by one till the dishwasher is empty";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		// 	IGraph graph = executePreviousStages(ppd);
		// 	loopDetectAgent.setGraph(graph);
		// 	loopDetectAgent.exec();
		// 	List<Loop> loops = loopDetectAgent.getLoops();
		// 	Assert.assertEquals(1, loops.size());
		// 	Loop loop = loops.get(0);
		// 	String[] expected = new String[] { "till", "one", "by", "one"}; // hier unklar
		// 	int i = 0;
		// 	for (INode node : loop.getKeyphrase().getAttachedNodes()) {
		// 		Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
		// 		i++;
		// 	}
		// 	String[] expectedCondition = new String[] { "the", "dishwasher", "is", "empty" }; // hier unklar
		// 	i = 0;
		// 	for (INode node : loop.getKeyphrase().getConditionNodes()) {
		// 		Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
		// 		i++;
		// 	}
		// 	//Assert.assertEquals(new Integer(2), loop.getIterations());
		// 	int[] expectedSpan = new int[] { 0, 8 };
		// 	Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		// 	Assert.assertEquals(expectedSpan[1],
		// 			loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("TODO")
	@Test
	public void whileEndingTest5() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "empty the dishwasher which is at the left side of the kitchen therefore you have to take out each peace of dishware till it is empty the dishwasher needs to be opened before that";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "till" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		String[] expectedCondition = new String[] { "it", "is", "empty" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 12, 21 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("TODO")
	@Test
	public void repeatEndingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "move to the dishwasher open it and grab one piece of dishware store it if the dishwasher is not empty repeat";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "repeat" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		String[] expectedCondition = new String[] { "if", "the", "dishwasher", "is", "not", "empty" };
		i = 0;
		for (INode node : loop.getKeyphrase().getConditionNodes()) {
			Assert.assertEquals(expectedCondition[i], node.getAttributeValue("value").toString());
			i++;
		}
		//Assert.assertEquals(new Integer(2), loop.getIterations());
		int[] expectedSpan = new int[] { 7, 13 };
		Assert.assertEquals(expectedSpan[0], loop.getDependentPhrases().get(0).getAttributeValue("position"));
		Assert.assertEquals(expectedSpan[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));
	}

	@Ignore("WIP")
	@Test
	public void beachday0002() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "When the scene starts the Girl moves into the foreground to the center of the scene "
				+ "At the same time the Frog foottaps twice "
				+ "After that the girl waves "
				+ "The Kangaroo claps twice and wags its tail simultaneously "
				+ "All the animals turn to face the Girl "
				+ "The Frog and the Bunny hop at the same time the Kangaroo nods "
				+ "The Kangaroo moves toward the Girl "
				+ "After that the Bunny moves toward the Girl "
				+ "Then the Frog moves toward the Girl "
				+ "The Girl turns to face the LightHouse "
				+ "All the animals turn to face the LightHouse "
				+ "The Girl and all the animals move toward the Beachterrain";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		System.out.println(loops);

	}

	private IGraph executePreviousStages(PrePipelineData ppd) {
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
		} catch (MissingDataException e) {
			e.printStackTrace();
		}
		actionRecog.exec();
		return actionRecog.getGraph();
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
