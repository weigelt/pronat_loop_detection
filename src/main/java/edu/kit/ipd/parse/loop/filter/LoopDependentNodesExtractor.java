package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.stanfordnumbernormalizer.NumberNormalizer;

public class LoopDependentNodesExtractor extends AbstractDependentNodesExtractor {

	// template method
	static Loop extract(Keyphrase keyphrase, INode action, boolean left) throws MissingDataException {

		INode begin = determineBegin(action, keyphrase, left);
		INode end = determineEnd(keyphrase, action, left);
		Number number = extractNumber(keyphrase);
		return constructLoop(keyphrase, begin, end, action, number);
	}

	private static Loop constructLoop(Keyphrase keyphrase, INode start, INode end, INode action, Number number) {
		Loop result = new Loop();
		result.setKeyphrase(keyphrase);
		result.setIterations(number);
		result.addDependentAction(action);
		result.addDependentPhrase(start);
		INode currNode = start;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != end);
		return result;
	}

	private static Number extractNumber(Keyphrase keyphrase) {
		Number number = null;
		String numbers = "";
		if (keyphrase.getAttachedNodes().size() == 1) {
			switch (keyphrase.getAttachedNodes().get(0).getAttributeValue("value").toString().toLowerCase()) {
			case "twice":
				return new Integer(2);

			case "thrice":
				return new Integer(3);

			default:
				break;
			}
		}
		for (INode node : keyphrase.getAttachedNodes()) {

			if (node.getAttributeValue("pos").equals("CD")) {
				numbers += node.getAttributeValue("value");
			}
		}
		if (!numbers.equals("")) {
			number = NumberNormalizer.wordToNumber(numbers);
		}

		return number;

	}
}
