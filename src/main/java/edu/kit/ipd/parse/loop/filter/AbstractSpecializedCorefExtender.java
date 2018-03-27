package edu.kit.ipd.parse.loop.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.ipd.parse.loop.data.Loop;
import edu.kit.ipd.parse.loop.data.Utterance;
import edu.kit.ipd.parse.luna.data.MissingDataException;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.INode;
import edu.kit.ipd.parse.luna.graph.Pair;

public abstract class AbstractSpecializedCorefExtender implements ISpecializedCorefExtender {

	protected boolean isLeft;

	public AbstractSpecializedCorefExtender() {
		isLeft = false;
	}

	@Override
	public void extendBlocks(Loop loop, List<Pair<Integer, Integer>> boundaries, int i, Utterance utterance) throws MissingDataException {
		if (!loop.getDependentPhrases().isEmpty()) {
			Set<INode> entities = getEntities(loop.getDependentPhrases());

			int refPosition = getReferencePosition(loop, isLeft);
			int boundary = getBoundary(boundaries, i, isLeft);
			for (INode entity : entities) {
				int result = getMaxPositionFromCorefChain(entity, refPosition, boundary, isLeft);
				if (checkIfExtending(result, refPosition, boundary, isLeft)) {
					refPosition = result;
				}
			}
			refPosition = determineFinalRefPosition(utterance.giveUtteranceAsNodeList().get(refPosition), boundary, isLeft);
			extendDependentPhrase(loop, refPosition, getReferencePosition(loop, isLeft), utterance.giveUtteranceAsNodeList(), isLeft);

		}
	}

	protected int determineFinalRefPosition(INode ref, int boundary, boolean left) throws MissingDataException {
		int result = GrammarFilter.getPositionOfNode(ref);
		INode predicateNode = getPredicateForNode(ref);
		if (predicateNode != null) {
			int position = GrammarFilter.getPositionOfNode(predicateNode);
			if ((left && position > boundary) || (!left && position < boundary)) {
				if ((left && position < result) || (!left && position > result)) {
					result = position;
				}

				if (left) {
					position = determineBegin(predicateNode, boundary, left);
					result = (position < result) ? position : result;
				} else {
					position = determineEnd(predicateNode, boundary, left);
					result = (position > result) ? position : result;
				}
			}
		}
		return result;
	}

	private int determineBegin(INode startingAction, int boundary, boolean left) throws MissingDataException {
		int start = GrammarFilter.getPositionOfNode(startingAction);
		List<? extends IArc> outgoingFirstActionArcs = startingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if ((!left && GrammarFilter.getPositionOfNode(currTargetNode) >= boundary)
						|| (left && GrammarFilter.getPositionOfNode(currTargetNode) <= boundary)) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) < start) {
					start = GrammarFilter.getPositionOfNode(currTargetNode);
				}
			} else {
				continue;
			}
		}
		return start;
	}

	private int determineEnd(INode endingAction, int boundary, boolean left) throws MissingDataException {
		int end = GrammarFilter.getPositionOfNode(endingAction);
		List<? extends IArc> outgoingFirstActionArcs = endingAction.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType);
		for (IArc iArc : outgoingFirstActionArcs) {
			if (iArc.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString()
					.equalsIgnoreCase(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE_TO_PARA)) {
				INode currTargetNode = iArc.getTargetNode();
				if (left && GrammarFilter.getPositionOfNode(currTargetNode) <= boundary) {
					continue;
				}
				if (GrammarFilter.getPositionOfNode(currTargetNode) > end) {
					while (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 0) {
						if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() == 1) {
							if (left && GrammarFilter.getPositionOfNode(currTargetNode
									.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0).getTargetNode()) <= boundary) {
								break;

							}
							currTargetNode = currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
									.getTargetNode();
						} else if (currTargetNode.getOutgoingArcsOfType(GrammarFilter.actionAnalyzerArcType).size() > 1) {
							//TODO what iff the assumption (we only have a INSIDE_CHUNK node) doesn't hold?
						}
					}
					end = GrammarFilter.getPositionOfNode(currTargetNode);
				}
			} else {
				continue;
			}
		}
		return end;
	}

	private INode getPredicateForNode(INode ref) {
		if (ref.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null) {
			INode current = ref;
			while (!current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).isEmpty()
					&& !current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0)
							.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_TYPE).toString().equals("NEXT_ACTION")) {

				current = current.getIncomingArcsOfType(GrammarFilter.actionAnalyzerArcType).get(0).getSourceNode();
			}
			if (current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null && current
					.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString().equals(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)) {
				return current;
			}
		}
		return null;
	}

	protected void extendDependentPhrase(Loop loop, int refPosition, int referencePosition, List<INode> nodes, boolean left) {
		if (left) {
			for (int j = referencePosition - 1; j >= refPosition; j--) {
				extend(loop, nodes, j);
			}
		} else {
			for (int j = referencePosition + 1; j <= refPosition; j++) {
				extend(loop, nodes, j);
			}
		}
		Collections.sort(loop.getDependentPhrases(), new Comparator<INode>() {

			@Override
			public int compare(INode o1, INode o2) {
				try {
					return Integer.compare(GrammarFilter.getPositionOfNode(o1), GrammarFilter.getPositionOfNode(o2));
				} catch (MissingDataException e) {
					return 0;
				}
			}
		});

	}

	private void extend(Loop loop, List<INode> nodes, int j) {
		INode current = nodes.get(j);
		loop.addDependentPhrase(current);
		if (current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE) != null
				&& current.getAttributeValue(GrammarFilter.ATTRIBUTE_NAME_ROLE).toString().equals(GrammarFilter.ATTRIBUTE_VALUE_PREDICATE)
				&& !GrammarFilter.hasIncomingInsideChunkArcs(current)) {
			loop.addDependentAction(current);
		}
	}

	protected int getReferencePosition(Loop loop, boolean left) throws MissingDataException {
		if (left) {
			return GrammarFilter.getPositionOfNode(loop.getDependentPhrases().get(0));

		} else {
			return GrammarFilter.getPositionOfNode(loop.getDependentPhrases().get(loop.getDependentPhrases().size() - 1));
		}
	}

	protected int getBoundary(List<Pair<Integer, Integer>> boundaries, int i, boolean left) {
		int result;
		if (left) {
			result = Integer.MIN_VALUE;
			if (i > 0) {
				result = boundaries.get(i - 1).getRight();
			}
		} else {
			result = Integer.MAX_VALUE;
			if (boundaries.size() > i + 1) {
				result = boundaries.get(i + 1).getLeft();
			}
		}
		return result;
	}

	protected int getMaxPositionFromCorefChain(INode entity, int refPosition, int boundary, boolean left) throws MissingDataException {
		int result = refPosition;
		Set<IArc> arcs;
		if (left) {
			arcs = filterReferentRelations(entity.getOutgoingArcsOfType(CorefExtender.contextRelationArcType));
		} else {
			arcs = filterReferentRelations(entity.getIncomingArcsOfType(CorefExtender.contextRelationArcType));
		}
		if (arcs.isEmpty()) {
			return result;
		} else {
			for (IArc iArc : arcs) {
				INode positionNode;
				if (left) {
					positionNode = iArc.getTargetNode();
				} else {
					positionNode = iArc.getSourceNode();
				}
				int position = getPositionOfReferenceNode(positionNode, left);
				if (checkIfExtending(position, result, boundary, left)) {
					result = position;
					int maxChain = getMaxPositionFromCorefChain(positionNode, result, boundary, left);
					if (checkIfExtending(maxChain, result, boundary, left)) {
						result = maxChain;
					}

				}
			}
		}
		return result;
	}

	protected boolean checkIfExtending(int position, int previous, int boundary, boolean left) {
		if (left) {
			if (position < previous && position > boundary) {
				return true;
			}
		} else {
			if (position > previous && position < boundary) {
				return true;
			}
		}
		return false;
	}

	private int getPositionOfReferenceNode(INode entityNode, boolean left) throws MissingDataException {
		if (left) {
			return GrammarFilter
					.getPositionOfNode(entityNode.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode());
		} else {
			INode current = entityNode;
			while (!current.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).isEmpty()) {
				current = current.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode();
			}
			return GrammarFilter.getPositionOfNode(current);
		}
	}

	private Set<IArc> filterReferentRelations(List<? extends IArc> relations) throws MissingDataException {
		Set<IArc> referentRelations = new HashSet<>();
		for (IArc relation : relations) {
			if (relation.getAttributeValue(CorefExtender.RELATION_TYPE_NAME).equals(CorefExtender.REFERENT_RELATION_TYPE)
					&& (relation.getAttributeValue(CorefExtender.REFERENT_RELATION_ROLE_NAME).equals(CorefExtender.ANAPHORA_NAME_VALUE)
							|| relation.getAttributeValue(CorefExtender.REFERENT_RELATION_ROLE_NAME)
									.equals(CorefExtender.OBJECT_IDENTITY_NAME_VALUE))
					&& checkInstructionNumberBoundary(relation.getTargetNode(), relation.getSourceNode())) {
				if (isMostLikelyReferent(relation, relation.getSourceNode())) {
					referentRelations.add(relation);
				}

			}
		}
		return referentRelations;
	}

	private boolean checkInstructionNumberBoundary(INode entityOne, INode entityTwo) {
		INode refNodeOne, refNodeTwo;
		refNodeOne = entityOne.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode();
		refNodeTwo = entityTwo.getOutgoingArcsOfType(CorefExtender.entityReferenceArcType).get(0).getTargetNode();

		if (refNodeOne != null && refNodeTwo != null) {
			return Math.abs(GrammarFilter.getInstructionNumber(refNodeOne) - GrammarFilter.getInstructionNumber(refNodeTwo)) <= 2;
		}
		return false;
	}

	private boolean isMostLikelyReferent(IArc relation, INode sourceNode) {
		double confidence = (double) relation.getAttributeValue(CorefExtender.CONFIDENCE_NAME);
		for (IArc rel : sourceNode.getOutgoingArcsOfType(CorefExtender.contextRelationArcType)) {
			if (rel.getAttributeValue(CorefExtender.RELATION_TYPE_NAME).equals(CorefExtender.REFERENT_RELATION_TYPE)) {
				if (confidence < (double) rel.getAttributeValue(CorefExtender.CONFIDENCE_NAME)) {
					return false;
				}
			}

		}
		return true;
	}

	protected Set<INode> getEntities(List<INode> dependentPhrases) {
		Set<INode> entities = new HashSet<>();
		for (INode node : dependentPhrases) {
			List<? extends IArc> incoming = node.getIncomingArcsOfType(CorefExtender.entityReferenceArcType);
			if (!incoming.isEmpty() && incoming.get(0).getSourceNode().getType().equals(CorefExtender.entityNodeType)) {
				entities.add(incoming.get(0).getSourceNode());
			}
		}
		return entities;
	}

}
