package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;

public abstract class AbstractWhileLoopGrammarFilter implements ISpecializedGrammarFilter {

	protected LoopDependentNodesExtractor ldne;

	public AbstractWhileLoopGrammarFilter() {
		ldne = new LoopDependentNodesExtractor();
	}

	@Override
	public Loop filter(Keyphrase keyphrase) throws MissingDataException {
		if (StatementExtractor.extract(keyphrase)) {
			return constructLoop(keyphrase);
		} else {
			return null;
		}
	}

	protected abstract Loop constructLoop(Keyphrase keyphrase) throws MissingDataException;
}