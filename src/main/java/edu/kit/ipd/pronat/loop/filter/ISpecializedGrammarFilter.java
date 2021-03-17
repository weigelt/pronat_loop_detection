package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Keyphrase;
import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;

/**
 * @author Sebastian Weigelt
 */
public interface ISpecializedGrammarFilter {

	public Loop filter(Keyphrase keyphrase) throws MissingDataException;
}
