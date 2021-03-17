package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
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
		while (currNode != end && !currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			currNode = currNode.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			result.addDependentPhrase(currNode);
		}
		return result;
	}
}
