package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class LoopGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public Loop filter(Keyphrase keyphrase) throws MissingDataException {

		INode[] leftActions = new INode[3];
		leftActions[0] = keyphrase.getAttachedNodes().get(0);
		boolean leftAnd = GrammarFilter.findActionNodes(leftActions, true);
		INode firstLeftAction = leftActions[1];

		Loop result = null;
		if (firstLeftAction != null) {
			result = DependentNodesExtractor.extract(keyphrase, firstLeftAction, firstLeftAction, true);
		} else {
		}
		return result;
	}

}
