package edu.kit.ipd.pronat.loop.filter;

import java.util.List;

import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.pronat.loop.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

/**
 * @author Sebastian Weigelt
 */
public interface ISpecializedCorefExtender {

	public void extendBlocks(Loop loop, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance) throws MissingDataException;

}
