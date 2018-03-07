package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class ForLoopGrammarFilter implements ISpecializedGrammarFilter {

	@Override
	public Loop filter(Keyphrase keyphrase) throws MissingDataException {

		INode start = keyphrase.getAttachedNodes().get(0);
		INode leftAction = GrammarFilter.findActionNodes(start, true);

		Loop result = null;
		if (leftAction != null) {
			result = LoopDependentNodesExtractor.extract(keyphrase, leftAction, true);
		} else {
		}
		return result;
	}

}
