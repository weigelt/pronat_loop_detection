package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class LoopDependentNodesExtractor extends AbstractDependentNodesExtractor {

	// template method
	Loop extract(Keyphrase keyphrase, INode action, boolean left) throws MissingDataException {

		INode begin = determineBegin(action, keyphrase, left);
		INode end = determineEnd(keyphrase, action, left);
		return constructLoop(keyphrase, begin, end, action);
	}

	protected Loop constructLoop(Keyphrase keyphrase, INode start, INode end, INode action) {
		//TODO: neu!
		Loop result = new Loop();
		result.setKeyphrase(keyphrase);
		result.addDependentAction(action);
		result.addDependentPhrase(start);
		INode currNode = start;
		do {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		} while (currNode != end);
		return result;
	}
}
