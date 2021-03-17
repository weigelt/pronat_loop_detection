package edu.kit.ipd.pronat.loop.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.pronat.loop.data.Loop;
import edu.kit.ipd.pronat.loop.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.Pair;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

/**
 * @author Sebastian Weigelt
 */
public class CorefExtender {

	ISpecializedCorefExtender spcex;
	private final ParseGraph pgStub = new ParseGraph();
	static IArcType entityReferenceArcType;
	static IArcType contextRelationArcType;
	static INodeType entityNodeType;

	private static final String REFERENCE = "reference";
	private static final String ENTITY_NODE_TYPE = "contextEntity";
	private static final String RELATION_ARC_TYPE = "contextRelation";
	static final String REFERENT_RELATION_TYPE = "referentRelation";
	static final String RELATION_TYPE_NAME = "typeOfRelation";
	static final String CONFIDENCE_NAME = "confidence";
	static final String REFERENT_RELATION_ROLE_NAME = "name";
	static final String ANAPHORA_NAME_VALUE = "anaphoraReferent";
	static final Object OBJECT_IDENTITY_NAME_VALUE = "objectIdentityReferent";

	public CorefExtender() {
		entityReferenceArcType = pgStub.createArcType(REFERENCE);
		contextRelationArcType = pgStub.createArcType(RELATION_ARC_TYPE);
		entityNodeType = pgStub.createNodeType(ENTITY_NODE_TYPE);
	}

	public void extendBlocks(List<Loop> loops, Utterance utterance) throws MissingDataException {
		List<Pair<Integer, Integer>> boundaries = getBoundaries(loops);
		for (int i = 0; i < loops.size(); i++) {
			Loop loop = loops.get(i);
			switch (loop.getKeyphrase().getPrimaryType()) {
			case WRAPPING:
				spcex = new WrappingCorefExtender();
				break;
			case OPENING:
				spcex = new OpeningCorefExtender();
				break;
			case ENDING:
			case LOOP:
				spcex = new EndingCorefExtender();
				break;
			default:
				break;
			}
			if (spcex != null) {
				spcex.extendBlocks(loop, boundaries, i, utterance);
			}
		}
	}

	private List<Pair<Integer, Integer>> getBoundaries(List<Loop> loops) throws MissingDataException {
		List<Pair<Integer, Integer>> result = new ArrayList<>(loops.size());
		for (Loop loop : loops) {
			List<INode> nodes = loop.getKeyphrase().getAttachedNodes();
			if (!nodes.isEmpty()) {
				int begin = GrammarFilter.getPositionOfNode(nodes.get(0));
				int end = GrammarFilter.getPositionOfNode(nodes.get(nodes.size() - 1));
				if (!loop.getKeyphrase().getConditionNodes().isEmpty()) {
					end = GrammarFilter.getPositionOfNode(
							loop.getKeyphrase().getConditionNodes().get(loop.getKeyphrase().getConditionNodes().size() - 1));
				}
				result.add(new Pair<Integer, Integer>(begin, end));
			}

		}
		return result;
	}
}
