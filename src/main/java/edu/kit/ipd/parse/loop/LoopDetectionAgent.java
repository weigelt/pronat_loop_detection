package edu.kit.ipd.parse.loop;

import java.util.List;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.parse.loop.data.Keyphrase;
import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.Utterance;
import edu.kit.ipd.parse.loop.filter.GrammarFilter;
import edu.kit.ipd.parse.loop.filter.KeyphraseFilter;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArcType;
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
			kpat.addAttributeToType("String", "verfiedByDA");
			kpat.addAttributeToType("String", "type");
			return kpat;
		} else {
			return graph.getArcType(ARC_TYPE_KEY_PHRASE);
		}
	}

	private IArcType createDependentActionArcType() {
		if (!graph.hasArcType(ARC_TYPE_DEPENDENT_ACTION)) {
			IArcType daat = graph.createArcType(ARC_TYPE_DEPENDENT_ACTION);
			daat.addAttributeToType("int", "position");
			daat.addAttributeToType("String", "verfiedByDA");
			return daat;
		} else {
			return graph.getArcType(ARC_TYPE_DEPENDENT_ACTION);
		}
	}

	private IArcType createConditionArcType() {
		if (!graph.hasArcType(ARC_TYPE_CONDITION)) {
			IArcType cat = graph.createArcType(ARC_TYPE_CONDITION);
			cat.addAttributeToType("String", "verfiedByDA");
			return cat;
		} else {
			return graph.getArcType(ARC_TYPE_CONDITION);
		}
	}

	private INodeType createLoopNodeType() {
		if (!graph.hasNodeType(NODE_TYPE_LOOP)) {
			INodeType lont = graph.createNodeType(NODE_TYPE_LOOP);
			lont.addAttributeToType("String", "type");
			lont.addAttributeToType("String", "keyphrase");
			lont.addAttributeToType("String", "keyphraseType");
			lont.addAttributeToType("String", "dependentPhrases");
			return lont;
		} else {
			return graph.getNodeType(NODE_TYPE_LOOP);
		}
	}

}
