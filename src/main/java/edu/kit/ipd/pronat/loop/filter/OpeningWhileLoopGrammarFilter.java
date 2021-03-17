package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.pronat.loop.data.LoopType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

/**
 * @author Sebastian Weigelt
 */
public class OpeningWhileLoopGrammarFilter extends AbstractWhileLoopGrammarFilter {

	public OpeningWhileLoopGrammarFilter() {
		super();
	}

	@Override
	protected Loop constructLoop(Keyphrase keyphrase) throws MissingDataException {
		INode start = keyphrase.getConditionNodes().get(keyphrase.getConditionNodes().size() - 1);
		INode rightAction = GrammarFilter.findActionNodes(start, false);
		if (rightAction == null) {
			return null;
		} else {
			Loop result = ldne.extract(keyphrase, rightAction, false);
			result.setType(LoopType.DO_WHILE);
			return result;
		}
	}

}
