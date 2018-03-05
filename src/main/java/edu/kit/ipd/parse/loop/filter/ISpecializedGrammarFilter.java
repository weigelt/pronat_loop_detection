package edu.kit.ipd.parse.loop.filter;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;

public interface ISpecializedGrammarFilter {

	public Loop filter(Keyphrase keyphrase) throws MissingDataException;
}
