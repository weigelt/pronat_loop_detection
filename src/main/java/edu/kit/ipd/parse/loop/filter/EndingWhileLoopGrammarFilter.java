package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.LoopType;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.INode;

public class EndingWhileLoopGrammarFilter extends AbstractWhileLoopGrammarFilter {

	public EndingWhileLoopGrammarFilter() {
		super();
	}

	@Override
	protected Loop constructLoop(Keyphrase keyphrase) throws MissingDataException {
		INode start = keyphrase.getAttachedNodes().get(0);
		INode leftAction = GrammarFilter.findActionNodes(start, true);
		if (leftAction == null) {
			return null;
		} else {
			Loop result = ldne.extract(keyphrase, leftAction, true);
			result.setType(LoopType.DO_UNTIL);
			return result;
		}
	}

}
