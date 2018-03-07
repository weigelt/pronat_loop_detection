package edu.kit.ipd.parse.loop.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class GrammarFilter {

	ISpecializedGrammarFilter spg;
	ParseGraph pgStub;
	static final String WORD_AND = "and";
	static final String ATTRIBUTE_VALUE_PREDICATE = "PREDICATE";
	static final String ATTRIBUTE_VALUE_PREDICATE_TO_PARA = "PREDICATE_TO_PARA";
	static final String ATTRIBUTE_NAME_TYPE = "type";
	static final String ATTRIBUTE_NAME_ROLE = "role";
	static final String ATTRIBUTE_NAME_VALUE = "value";
	static final String ATTRIBUTE_NAME_POSITION = "position";
	static final String ATTRIBUTE_CHUNK_NAME = "chunkName";
	static final String ARC_TYPE_RELATION = "relation";
	static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	static IArcType actionAnalyzerArcType;
	static IArcType nextArcType;

	public GrammarFilter() {
		pgStub = new ParseGraph();
		nextArcType = pgStub.createArcType(ARC_TYPE_RELATION);
		//		nextArcType.addAttributeToType("String", "value");
		actionAnalyzerArcType = pgStub.createArcType(ARC_TYPE_RELATION_IN_ACTION);
	}

	public List<Loop> filter(List<Keyphrase> keyphrases) throws MissingDataException {
		List<Loop> conActions = new ArrayList<>();
		for (Keyphrase keyphrase : keyphrases) {
			switch (keyphrase.getPrimaryType()) {
			case WRAPPING:
				//spg = new WrappingGrammarFilter();
				break;
			case LOOP:
				spg = new ForLoopGrammarFilter();
				break;
			case OPENING:
				//spg = new OpeningGrammarFilter();
				break;
			case ENDING:
				//spg = new EndingGrammarFilter();
				break;
			default:
				break;
			}
			Loop result = spg.filter(keyphrase);
			//			if (result == null) {
			//				switch (keyphrase.getSecondaryType()) {
			//				case WRAPPING:
			//					spg = new WrappingGrammarFilter();
			//					break;
			//				case SEPARATING:
			//					spg = new SeparatingGrammarFilter();
			//					break;
			//				case OPENING:
			//					spg = new OpeningGrammarFilter();
			//					break;
			//				case ENDING:
			//					spg = new EndingGrammarFilter();
			//					break;
			//				default:
			//					break;
			//				}
			//				result = spg.filter(keyphrase);
			//			}
			if (result != null) {
				conActions.add(result);
			}
		}
		return conActions;
	}

	static int getPositionOfNode(INode node) throws MissingDataException {
		//		if (!node.getType().containsAttribute("int", ATTRIBUTE_NAME_POSITION)) {
		//			throw new MissingDataException("Node has no position attribute");
		//		}
		return (int) node.getAttributeValue(ATTRIBUTE_NAME_POSITION);
	}

	static INode findActionNodes(INode start, boolean left) {
		while (left ? !start.getIncomingArcsOfType(GrammarFilter.nextArcType).isEmpty()
				: !start.getOutgoingArcsOfType(GrammarFilter.nextArcType).isEmpty()) {
			start = left ? start.getIncomingArcsOfType(GrammarFilter.nextArcType).get(0).getSourceNode()
					: start.getOutgoingArcsOfType(GrammarFilter.nextArcType).get(0).getTargetNode();
			if (start.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null
					&& start.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString()
							.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)) {
				return start;
			}
		}
		return null;
	}
}
