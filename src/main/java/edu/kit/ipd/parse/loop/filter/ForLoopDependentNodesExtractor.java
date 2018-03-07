package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.stanfordnumbernormalizer.NumberNormalizer;

public class ForLoopDependentNodesExtractor extends LoopDependentNodesExtractor {

	@Override
	protected Loop constructLoop(Keyphrase keyphrase, INode start, INode end, INode action) {
		Loop result = new Loop();
		result.setKeyphrase(keyphrase);
		result.setIterations(extractNumber(keyphrase));
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
