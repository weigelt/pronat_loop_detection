package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.pronat.loop.data.LoopType;
import edu.kit.ipd.parse.luna.data.MissingDataException;

/**
 * @author Sebastian Weigelt
 */
public class WrappingWhileLoopGrammarFilter extends AbstractWhileLoopGrammarFilter {

	OpeningWhileLoopGrammarFilter owlgf;
	EndingWhileLoopGrammarFilter ewlgf;

	public WrappingWhileLoopGrammarFilter() {
		super();
		owlgf = new OpeningWhileLoopGrammarFilter();
		ewlgf = new EndingWhileLoopGrammarFilter();
	}

	@Override
	protected Loop constructLoop(Keyphrase keyphrase) throws MissingDataException {
		Loop result = owlgf.constructLoop(keyphrase);
		if (result == null) {
			result = ewlgf.constructLoop(keyphrase);
		}
		result.setType(LoopType.DO_WHILE);
		return result;
	}

}
