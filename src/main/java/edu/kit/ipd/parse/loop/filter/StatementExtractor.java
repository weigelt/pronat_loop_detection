package edu.kit.ipd.parse.loop.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.INode;

public class StatementExtractor {

	public static boolean extract(Keyphrase keyphrase) {
		//TODO: refactor me, every moment in my life is pure pain!
		INode endOfKP = keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1);
		if (endOfKP.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			return false;
		}
		INode nextNode = endOfKP.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
		List<INode> conditionalNodes = new ArrayList<>();
		if (!nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("NP")) {
			return false;
		} else {
			conditionalNodes.add(nextNode);
		}
		while (!nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			nextNode = nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("NP")
					|| nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("CONJ")) {
				conditionalNodes.add(nextNode);
			} else {
				break;
			}
		}

		if (!nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("VP")) {
			return false;
		} else {
			conditionalNodes.add(nextNode);
		}

		while (!nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			nextNode = nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("VP")) {
				conditionalNodes.add(nextNode);
			} else {
				break;
			}
		}

		if (!nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("ADJP")) {
			return false;
		} else {
			conditionalNodes.add(nextNode);
		}

		while (!nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			nextNode = nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals("ADJP")) {
				conditionalNodes.add(nextNode);
			} else {
				break;
			}
		}
		keyphrase.setConditionNodes(conditionalNodes);
		return true;
	}
}
