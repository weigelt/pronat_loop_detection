package edu.kit.ipd.pronat.loop.filter;

import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;

/**
 * @author Sebastian Weigelt
 */
public class WrappingCorefExtender extends AbstractSpecializedCorefExtender {

	@Override
	protected int getReferencePosition(Loop loop, boolean left) throws MissingDataException {
		if (GrammarFilter.getPositionOfNode(loop.getDependentActions().get(0)) < GrammarFilter
				.getPositionOfNode(loop.getKeyphrase().getAttachedNodes().get(0))) {
			isLeft = true;
			return GrammarFilter.getPositionOfNode(loop.getDependentPhrases().get(0));

		} else {
			isLeft = false;
			return GrammarFilter.getPositionOfNode(loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1));
		}
	}

}
