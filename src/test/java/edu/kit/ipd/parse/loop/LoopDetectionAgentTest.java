package edu.kit.ipd.parse.loop;

import java.util.Arrays;
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

	@Ignore("copy pasted")
	@Test
	public void separatingTest() {
		ppd = new PrePipelineData();
		//@formatter:off
		String input = "the robot grabs a cup meanwhile Jack goes to the fridge";
		//@formatter:on
		ppd.setMainHypothesis(StringToHypothesis.stringToMainHypothesis(input, true));

		IGraph graph = executePreviousStages(ppd);
		loopDetectAgent.setGraph(graph);
		loopDetectAgent.exec();
		List<Loop> loops = loopDetectAgent.getLoops();
		Assert.assertEquals(1, loops.size());
		Loop loop = loops.get(0);
		String[] expected = new String[] { "meanwhile" };
		int i = 0;
		for (INode node : loop.getKeyphrase().getAttachedNodes()) {
			Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
			i++;
		}
		int[] expectedSpanBefore = new int[] { 0, 4 };
		int[] expectedSpanAfter = new int[] { 6, 9 };
		int lastBefore = 0;
		int index = 0;
		Assert.assertEquals("Before span does not start where expected", expectedSpanBefore[0],
				loop.getDependentPhrases().get(0).getAttributeValue("position"));
		for (INode node : loop.getDependentPhrases()) {
			int nodePosition = (int) node.getAttributeValue("position");
			boolean isInsideSpan = expectedSpanBefore[0] <= nodePosition && nodePosition <= expectedSpanBefore[1];
			if (lastBefore == 0 && isInsideSpan == false) {
				Assert.assertEquals("Before span does not end where expected", expectedSpanBefore[1],
						loop.getDependentPhrases().get(index - 1).getAttributeValue("position"));
				lastBefore = index - 1;
				Assert.assertEquals("After span does start not where expected", expectedSpanAfter[0], nodePosition);
			}
			isInsideSpan = isInsideSpan || expectedSpanAfter[0] <= nodePosition && nodePosition <= expectedSpanAfter[1];
			Assert.assertTrue("Dependent Node at position " + nodePosition + " is not inside expected spans: "
					+ Arrays.toString(expectedSpanBefore) + ", " + Arrays.toString(expectedSpanAfter), isInsideSpan);
			index++;
		}
		Assert.assertEquals("After span does not end where expected", expectedSpanAfter[1],
				loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1).getAttributeValue("position"));

	}

	@Test
	public void loopTest() {
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
	public void loopTimesTest() {
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
