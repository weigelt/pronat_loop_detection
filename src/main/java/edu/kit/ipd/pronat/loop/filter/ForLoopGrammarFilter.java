package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public class ForLoopGrammarFilter implements ISpecializedGrammarFilter {

	LoopDependentNodesExtractor ldne;

	public ForLoopGrammarFilter() {
		ldne = new ForLoopDependentNodesExtractor();
	}

	@Override
	public Loop filter(Keyphrase keyphrase) throws MissingDataException {

		INode start = keyphrase.getAttachedNodes().get(0);
		INode leftAction = GrammarFilter.findActionNodes(start, true);

		Loop result = null;
		if (leftAction != null) {
			result = ldne.extract(keyphrase, leftAction, true);
		} else {
		}
		return result;
	}

}
