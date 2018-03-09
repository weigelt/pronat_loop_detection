package edu.kit.ipd.parse.loop;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.KeyphraseType;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.Utterance;
import edu.kit.ipd.parse.loop.filter.GrammarFilter;
import edu.kit.ipd.parse.loop.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.INodeType;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

@MetaInfServices(AbstractAgent.class)
public class LoopDetectionAgent extends AbstractAgent {

	private static final String NODE_TYPE_TOKEN = "token";
	private static final String ARC_TYPE_RELATION = "relation";
	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_KEY_PHRASE = "loopKeyPhrase";
	private static final String ARC_TYPE_DEPENDENT_ACTION = "dependentLoopAction";
	private static final String ARC_TYPE_CONDITION = "loopCondition";
	private static final String NODE_TYPE_LOOP = "loop";

	private static final String ATTRIBUTE_NAME_CONDITION = "condition";
	private static final String ATTRIBUTE_NAME_DEPENDENT_PHRASES = "dependentPhrases";
	private static final String ATTRIBUTE_NAME_KEYPHRASE_TYPE = "keyphraseType";
	private static final String ATTRIBUTE_NAME_KEYPHRASE = "keyphrase";
	private static final String ATTRIBUTE_NAME_POSITION = "position";
	private static final String ATTRIBUTE_NAME_TYPE = "type";
	private static final String ATTRIBUTE_NAME_VERFIED_BY_DA = "verfiedByDA";

	private IArcType keyPhraseArcType;
	private IArcType dependentActionArcType;
	private IArcType conditionArcType;
	private INodeType loopNodeType;

	KeyphraseFilter kf;
	GrammarFilter gf;
	Utterance utterance;
	List<Loop> loops;

	@Override
	public void init() {
		kf = new KeyphraseFilter();
		gf = new GrammarFilter();
	}

	@Override
	protected void exec() {
		if (!checkMandatoryPreconditions()) {
			return;
		}

		keyPhraseArcType = createKeyphraseArcType();
		dependentActionArcType = createDependentActionArcType();
		conditionArcType = createConditionArcType();
		loopNodeType = createLoopNodeType();

		ParseGraph graphAsParseGraph = (ParseGraph) graph;
		utterance = new Utterance(graphAsParseGraph);
		List<Keyphrase> keywords = kf.filter(utterance.giveUtteranceAsNodeList());
		try {
			loops = gf.filter(keywords);
		} catch (MissingDataException e) {
			//TODO Logger and return!
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//TODO: add optional filter for coref or eventcoref?
		writeToGraph(loops);
	}

	private void writeToGraph(List<Loop> loops) {
		INodeType tokenType = graph.getNodeType(NODE_TYPE_TOKEN);
		for (Loop loop : loops) {
			Keyphrase currKPtoWrite = loop.getKeyphrase();
			INode currLoopNode = null;
			for (int i = 0; i < currKPtoWrite.getAttachedNodes().size(); i++) {
				INode currKPNode = currKPtoWrite.getAttachedNodes().get(i);
				if (i == 0) {
					if (!currKPNode.getIncomingArcsOfType(keyPhraseArcType).isEmpty()
							&& currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode().getType().equals(loopNodeType)) {
						// we already have a loop node and this is also the source
						// TODO: what if the source node is not the loop node but a token node instead?
						currLoopNode = currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode();
					} else {
						// we don't have a loop node. Thus, we create one!
						currLoopNode = graph.createNode(loopNodeType);
						currLoopNode.setAttributeValue(ATTRIBUTE_NAME_TYPE, loop.getType().name());
						currLoopNode.setAttributeValue(ATTRIBUTE_NAME_KEYPHRASE, currKPtoWrite.getKeyphraseAsString());
						String keyphraseType = convertTypeToString(currKPtoWrite);
						currLoopNode.setAttributeValue(ATTRIBUTE_NAME_TYPE, keyphraseType);
						currLoopNode.setAttributeValue(ATTRIBUTE_NAME_DEPENDENT_PHRASES, loop.getDependentPhrasesAsString());
						currLoopNode.setAttributeValue(ATTRIBUTE_NAME_CONDITION, loop.getConditionAsString());
						IArc newArc = graph.createArc(currLoopNode, currKPNode, keyPhraseArcType);
						// create according arc
						newArc.setAttributeValue(ATTRIBUTE_NAME_VERFIED_BY_DA, false);
						newArc.setAttributeValue(ATTRIBUTE_NAME_TYPE, keyphraseType);
						// create dep action links
						for (int j = 0; j < loop.getDependentActions().size(); j++) {
							IArc currDepArc = graph.createArc(currLoopNode, loop.getDependentActions().get(j), dependentActionArcType);
							currDepArc.setAttributeValue(ATTRIBUTE_NAME_VERFIED_BY_DA, false);
							currDepArc.setAttributeValue(ATTRIBUTE_NAME_POSITION, j);
						}
						//TODO: ADD CONDTION
					}
				} else {
					// i>0
					if (!currKPNode.getIncomingArcsOfType(keyPhraseArcType).isEmpty()) {
						// we already have keyphrase arc
						if (currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode().getType().equals(loopNodeType)) {
							// but it points to a node o.0
							if ((boolean) currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0)
									.getAttributeValue(ATTRIBUTE_NAME_VERFIED_BY_DA)) {
								//TODO: might crash iff "verfiedByDA" is unset
								//but it has been verfied by the da
								cleanUp(currLoopNode);
								// set the new curr con act node
								currLoopNode = currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode();
								// we have to clean up the previously build con node
								//TODO: what if null?
							}
							// we have to clean up!
							cleanUp(currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode());

						} else if (currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode().getType().equals(tokenType)) {
							//and it points to a token node...
							if (!currKPNode.getIncomingArcsOfType(keyPhraseArcType).get(0).getSourceNode()
									.equals(currKPtoWrite.getAttachedNodes().get(i - 1))) {
								// but it's not the right one
								// TODO: what now? Throw an exception?
							}
							// it's the right node... everything's fine!
						}
					} else {
						// that's the good case. We simply add a new arc from the first to the next node of the keyphrase
						// create intermediate arc
						createKeyPhraseArc(currKPtoWrite.getAttachedNodes().get(i - 1), currKPNode, convertTypeToString(currKPtoWrite));
					}
				}
			}
		}
	}

	private void cleanUp(INode sourceNode) {
		if (sourceNode == null) {
			return;
		}
		for (IArc depAction : sourceNode.getOutgoingArcsOfType(dependentActionArcType)) {
			graph.deleteArc(depAction);
		}
		for (IArc keyPhrase : sourceNode.getOutgoingArcsOfType(keyPhraseArcType)) {
			INode nextNode = keyPhrase.getTargetNode();
			graph.deleteArc(keyPhrase);

			while (nextNode.getIncomingArcsOfType(keyPhraseArcType).size() < 1) {
				// we stop when the node has more than one incomming keyphrase arcs,
				// i.e. it has one that comes from a conc Action node and one that comes from a token
				// (this one was deleted in the step before)
				IArc nextArc = nextNode.getOutgoingArcsOfType(keyPhraseArcType).get(0);
				nextNode = nextArc.getTargetNode();
				graph.deleteArc(nextArc);
			}
		}
	}

	private void createKeyPhraseArc(INode from, INode to, String type) {
		IArc newArc = graph.createArc(from, to, keyPhraseArcType);
		// create according arc
		newArc.setAttributeValue("verfiedByDA", false);
		newArc.setAttributeValue("type", type);
	}

	/**
	 * Only for testing
	 *
	 * @return
	 */
	public List<Loop> getLoops() {
		return loops;
	}

	private boolean checkMandatoryPreconditions() {
		if (graph.hasArcType(ARC_TYPE_RELATION) && graph.hasArcType(ARC_TYPE_RELATION_IN_ACTION) && graph.hasNodeType(NODE_TYPE_TOKEN)) {
			return true;
		}
		return false;
	}

	private IArcType createKeyphraseArcType() {
		if (!graph.hasArcType(ARC_TYPE_KEY_PHRASE)) {
			IArcType kpat = graph.createArcType(ARC_TYPE_KEY_PHRASE);
			kpat.addAttributeToType("String", ATTRIBUTE_NAME_VERFIED_BY_DA);
			kpat.addAttributeToType("String", ATTRIBUTE_NAME_TYPE);
			return kpat;
		} else {
			return graph.getArcType(ARC_TYPE_KEY_PHRASE);
		}
	}

	private IArcType createDependentActionArcType() {
		if (!graph.hasArcType(ARC_TYPE_DEPENDENT_ACTION)) {
			IArcType daat = graph.createArcType(ARC_TYPE_DEPENDENT_ACTION);
			daat.addAttributeToType("int", ATTRIBUTE_NAME_POSITION);
			daat.addAttributeToType("String", ATTRIBUTE_NAME_VERFIED_BY_DA);
			return daat;
		} else {
			return graph.getArcType(ARC_TYPE_DEPENDENT_ACTION);
		}
	}

	private IArcType createConditionArcType() {
		if (!graph.hasArcType(ARC_TYPE_CONDITION)) {
			IArcType cat = graph.createArcType(ARC_TYPE_CONDITION);
			cat.addAttributeToType("String", ATTRIBUTE_NAME_VERFIED_BY_DA);
			return cat;
		} else {
			return graph.getArcType(ARC_TYPE_CONDITION);
		}
	}

	private INodeType createLoopNodeType() {
		if (!graph.hasNodeType(NODE_TYPE_LOOP)) {
			INodeType lont = graph.createNodeType(NODE_TYPE_LOOP);
			lont.addAttributeToType("String", ATTRIBUTE_NAME_TYPE);
			lont.addAttributeToType("String", ATTRIBUTE_NAME_KEYPHRASE);
			lont.addAttributeToType("String", ATTRIBUTE_NAME_KEYPHRASE_TYPE);
			lont.addAttributeToType("String", ATTRIBUTE_NAME_DEPENDENT_PHRASES);
			lont.addAttributeToType("String", ATTRIBUTE_NAME_CONDITION);
			return lont;
		} else {
			return graph.getNodeType(NODE_TYPE_LOOP);
		}
	}

	private String convertTypeToString(Keyphrase currKPtoWrite) {
		return currKPtoWrite.getSecondaryType().equals(KeyphraseType.UNSET) ? currKPtoWrite.getPrimaryType().name()
				: currKPtoWrite.getPrimaryType() + "/" + currKPtoWrite.getSecondaryType().name();
	}

}
