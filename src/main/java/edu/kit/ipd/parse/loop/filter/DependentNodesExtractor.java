package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class DependentNodesExtractor extends AbstractDependentNodesExtractor {

	// template method
	static Loop extract(Keyphrase keyphrase, INode startingAction, INode endingAction, boolean left) throws MissingDataException {

		INode begin = determineBegin(startingAction, keyphrase, left);
		INode end = determineEnd(keyphrase, endingAction, left);
		return constructLoop(keyphrase, begin, end, startingAction, endingAction);
	}

	private static Loop constructLoop(Keyphrase keyphrase, INode start, INode end, INode startingAction, INode endingAction) {
		Loop result = new Loop();
		result.setKeyphrase(keyphrase);
		result.addDependentAction(startingAction);
		result.addDependentAction(endingAction);
		result.addDependentPhrase(start);
		INode currNode = start;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != end);
		return result;
	}
}
