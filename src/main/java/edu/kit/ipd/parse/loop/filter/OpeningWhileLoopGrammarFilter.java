package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.LoopType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

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
