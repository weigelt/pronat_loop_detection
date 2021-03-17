package edu.kit.ipd.pronat.loop.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public class StatementExtractor {

	private static final List<String> NOUN_BLOCK = Arrays.asList("NP", "[CC]");
	private static final List<String> VERB_PHRASE = Arrays.asList("VP", "[RB]");
	private static final List<String> ADJP_PHRASE = Arrays.asList("ADJP");

	public static boolean extract(Keyphrase keyphrase) {
		INode endOfKP = keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1);
		if (endOfKP.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			return false;
		}
		List<INode> conditionalNodes = new ArrayList<>();
		INode nextNode = endOfKP.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();

		nextNode = getEndNodeOfChunk(nextNode, NOUN_BLOCK, conditionalNodes);
		nextNode = getEndNodeOfChunk(nextNode, VERB_PHRASE, conditionalNodes);
		nextNode = getEndNodeOfChunk(nextNode, ADJP_PHRASE, conditionalNodes);

		if (nextNode == null) {
			return false;
		}

		keyphrase.setConditionNodes(conditionalNodes);
		return true;
	}

	private static INode getEndNodeOfChunk(INode start, List<String> chunktypes, List<INode> conditionalNodes) {
		if (start == null) {
			return null;
		}
		if (!start.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals(chunktypes.get(0))) {
			return null;
		} else {
			conditionalNodes.add(start);
		}
		INode nextNode = start;
		while (!nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			nextNode = nextNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			boolean isOfType = false;
			for (String chunktype : chunktypes) {
				String posTag = posTag(chunktype);
				if (posTag != null && nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POS).equals(posTag)
						|| nextNode.getAttributeValue(GrammarFilter.ATTRIBUTE_CHUNK_NAME).equals(chunktype)) {
					isOfType = true;
				}
			}
			if (isOfType) {
				conditionalNodes.add(nextNode);
			} else {
				break;
			}
		}
		return nextNode;
	}

	private static String posTag(String input) {
		if (input.startsWith("[") && input.endsWith("]")) {
			String result = input.replace("[", "");
			result = result.replace("]", "");
			result.trim();
			if (result.equals(result.toUpperCase())) {
				return result;
			}
		}
		return null;
	}
}
