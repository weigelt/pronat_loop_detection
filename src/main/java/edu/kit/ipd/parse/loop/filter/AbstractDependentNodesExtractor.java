package edu.kit.ipd.parse.loop.filter;

import java.util.List;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;

public abstract class AbstractDependentNodesExtractor {

	protected static INode determineBegin(INode startingAction, Keyphrase keyphrase, boolean left) throws MissingDataException {
		INode depNodeBegin = startingAction;
		int start = GrammarFilter.getPositionOfNode(depNodeBegin);
		List<? extends IArc> outgoingFirstActionArcs = startingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (!left && GrammarFilter.getPositionOfNode(currTargetNode) <= GrammarFilter
						.getPositionOfNode(keyphrase.getAttachedNodes().get(keyphrase.getAttachedNodes().size() - 1))) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) < start) {
					start = GrammarFilter.getPositionOfNode(currTargetNode);
					depNodeBegin = currTargetNode;
				}
			} else {
				continue;
			}
		}
		return depNodeBegin;
	}

	protected static INode determineEnd(Keyphrase keyphrase, INode endingAction, boolean left) {
		INode depNodeEnd = endingAction;
		int end = (int) endingAction.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION);
		List<? extends IArc> outgoingFirstActionArcs = endingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (left && (int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION) >= (int) keyphrase
						.getAttachedNodes().get(0).getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION)) {
					continue;
				}
				if ((int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION) > end) {
					while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
							currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
									.getTargetNode();
						} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
							//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
						}
					}
					end = (int) currTargetNode.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_POSITION);
					depNodeEnd = currTargetNode;
				}
			} else {
				continue;
			}
		}
		return depNodeEnd;
	}
}
