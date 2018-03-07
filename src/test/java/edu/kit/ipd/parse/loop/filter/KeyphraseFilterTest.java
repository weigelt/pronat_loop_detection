package edu.kit.ipd.parse.loop.filter;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class KeyphraseFilterTest {

	static ParseGraph pg;
	static INodeType nodeType;

	@BeforeClass
	public static void setUp() {
		pg = new ParseGraph();
		nodeType = pg.createNodeType("test");
		nodeType.addAttributeToType("String", "value");
		nodeType.addAttributeToType("String", "pos");
	}

	@Test
	public void testCardinalNumber() {
		String input = "the dog barks twice";
		String inPos = "DT NN VBZ RB";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		String[] splittedPOS = inPos.split(" ");
		for (int i = 0; i < splitted.length; i++) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", splitted[i]);
			currNode.setAttributeValue("pos", splittedPOS[i]);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "twice" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}

	@Test
	public void testCardinalNumberTimes() {
		String input = "the dog barks two times";
		String inPos = "DT NN VBZ CD NNS";
		List<INode> inputNodeList = new ArrayList<>();
		String[] splitted = input.split(" ");
		String[] splittedPOS = inPos.split(" ");
		for (int i = 0; i < splitted.length; i++) {
			INode currNode = pg.createNode(nodeType);
			currNode.setAttributeValue("value", splitted[i]);
			currNode.setAttributeValue("pos", splittedPOS[i]);
			inputNodeList.add(currNode);
		}
		String[] expected = new String[] { "two", "times" };
		List<Keyphrase> result = new KeyphraseFilter().filter(inputNodeList);
		for (Keyphrase keyphrase : result) {
			int i = 0;
			for (INode node : keyphrase.getAttachedNodes()) {
				Assert.assertEquals(expected[i], node.getAttributeValue("value").toString());
				i++;
			}
		}
	}

}
