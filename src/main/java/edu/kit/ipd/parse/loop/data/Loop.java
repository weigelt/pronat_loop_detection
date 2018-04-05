package edu.kit.ipd.parse.loop.data;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.parse.luna.graph.INode;

public class Loop {
	private Keyphrase keyphrase;
	private List<INode> dependentPhrases;
	private List<INode> dependentActions;
	private Number iterations;
	private LoopType type;

	public Loop() {
		this.dependentPhrases = new ArrayList<>();
		this.dependentActions = new ArrayList<>();
		type = LoopType.UNSET;
	}

	/**
	 * @return the keyphrase
	 */
	public Keyphrase getKeyphrase() {
		return keyphrase;
	}

	/**
	 * @param keyphrase
	 *            the keyphrase to set
	 */
	public void setKeyphrase(Keyphrase keyphrase) {
		this.keyphrase = keyphrase;
	}

	/**
	 * @return the dependentPhrases
	 */
	public List<INode> getDependentPhrases() {
		return dependentPhrases;
	}

	/**
	 * @param dependentPhrases
	 *            the dependentPhrases to set
	 */
	public void setDependentPhrases(List<INode> dependentPhrases) {
		this.dependentPhrases = dependentPhrases;
	}

	/**
	 * @param dependentPhrase
	 *            adds a dependent phrase to the list of dep. phrases
	 */
	public void addDependentPhrase(INode dependentPhrase) {
		dependentPhrases.add(dependentPhrase);
	}

	/**
	 * @return the dependentActions
	 */
	public List<INode> getDependentActions() {
		return dependentActions;
	}

	/**
	 * @param dependentActions
	 *            the dependentActions to set
	 */
	public void setDependentActions(List<INode> dependentActions) {
		this.dependentActions = dependentActions;
	}

	/**
	 * @param dependentAction
	 *            adds a dependent action to the list of dep. phrases
	 */
	public void addDependentAction(INode dependentAction) {
		dependentActions.add(dependentAction);
	}

	@Override
	public String toString() {
		String out = "[keyphrase: " + getKeyphrase().toString() + " dependentNodes: ";
		for (INode iNode : dependentPhrases) {
			out += iNode.getAttributeValue("value") + "(" + iNode.getAttributeValue("position") + "), ";
		}
		out += "]";
		return out;
	}

	public String getDependentPhrasesAsString() {
		String out = "";
		int j = 0, old_j = -1;
		for (int i = 0; i < dependentPhrases.size(); i++) {
			INode nodeToWrite = dependentPhrases.get(i);
			if (old_j != j) {
				out += j + ": ";
				old_j = j;
			}
			out += nodeToWrite.getAttributeValue("value") + " ";
			if (i + 1 < dependentPhrases.size()
					&& (int) nodeToWrite.getAttributeValue("position") < (int) dependentPhrases.get(i + 1).getAttributeValue("position")) {
				j++;
				out = out.trim();
				out += ", ";
			}
		}
		return out;
	}

	public String getConditionAsString() {
		String out = "";
		int j = 0, old_j = -1;
		List<INode> condition = keyphrase.getConditionNodes();
		for (int i = 0; i < condition.size(); i++) {
			INode nodeToWrite = condition.get(i);
			if (old_j != j) {
				out += j + ": ";
				old_j = j;
			}
			out += nodeToWrite.getAttributeValue("value") + " ";
			if (i + 1 < condition.size()
					&& (int) nodeToWrite.getAttributeValue("position") < (int) condition.get(i + 1).getAttributeValue("position")) {
				j++;
				out.trim();
				out += ", ";
			}
		}
		return out;
	}

	public Number getIterations() {
		return iterations;
	}

	public void setIterations(Number iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the type
	 */
	public LoopType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(LoopType type) {
		this.type = type;
	}
}
