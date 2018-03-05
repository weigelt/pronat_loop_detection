package edu.kit.ipd.parse.loop.data;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IArcType;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.ParseGraph;

public class Utterance {

	private ParseGraph fullGraph;
	private ParseGraph utteranceAsGraph;
	private List<INode> utteranceAsNodeList;

	public Utterance(ParseGraph fullGraph) {
		this.fullGraph = fullGraph;
		this.utteranceAsNodeList = new ArrayList<>();
		createUtterance();
	}

	//TODO: create parsing for utteranceGraph
	private void createUtterance() {
		IArcType nextType = fullGraph.getArcType("relation");
		utteranceAsGraph = new ParseGraph();
		INode currNode = fullGraph.getFirstUtteranceNode();
		IArc nextArc = null;
		do {
			utteranceAsNodeList.add(currNode);
			List<? extends IArc> nArcs = currNode.getOutgoingArcsOfType(nextType);
			if (nArcs.isEmpty()) {
				currNode = null;
			} else {
				nextArc = nArcs.get(0);
				currNode = nextArc.getTargetNode();
			}
		} while (currNode != null);
	}

	public List<INode> giveUtteranceAsNodeList() {
		return utteranceAsNodeList;
	}
}
