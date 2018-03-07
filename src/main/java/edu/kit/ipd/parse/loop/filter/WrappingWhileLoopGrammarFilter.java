package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;

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
		return result;
	}

}
