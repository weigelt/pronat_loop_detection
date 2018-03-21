package edu.kit.ipd.parse.loop.filter;

import java.util.List;

import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.Pair;

public interface ISpecializedCorefExtender {

	public void extendBlocks(Loop loop, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance) throws MissingDataException;

}
