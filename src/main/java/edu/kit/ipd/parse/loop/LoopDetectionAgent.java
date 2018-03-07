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

	private static final String ARC_TYPE_RELATION = "relation";
	private static final String ARC_TYPE_RELATION_IN_ACTION = "relationInAction";
	private static final String ARC_TYPE_KEY_PHRASE = "loopKeyPhrase";
	private static final String ARC_TYPE_DEPENDENT_ACTION = "dependentLoopAction";
	private static final String NODE_TYPE_CONCURRENT_ACTION = "loop";

	private IArcType keyPhraseType;
	private IArcType dependentActionType;
	private INodeType concurrentActionType;

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

		keyPhraseType = createKeyphraseArcType();
		dependentActionType = createDependentActionArcType();
		concurrentActionType = createConcurrentActionNodeType();

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
		//writeToGraph(conActions);
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
		if (graph.hasArcType("relation") && graph.hasArcType("relationInAction")) {
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

	private INodeType createConcurrentActionNodeType() {
		if (!graph.hasNodeType(NODE_TYPE_CONCURRENT_ACTION)) {
			INodeType cant = graph.createNodeType(NODE_TYPE_CONCURRENT_ACTION);
			cant.addAttributeToType("String", "keyphrase");
			cant.addAttributeToType("String", "type");
			cant.addAttributeToType("String", "dependentPhrases");
			return cant;
		} else {
			return graph.getNodeType(NODE_TYPE_CONCURRENT_ACTION);
		}
	}

}
