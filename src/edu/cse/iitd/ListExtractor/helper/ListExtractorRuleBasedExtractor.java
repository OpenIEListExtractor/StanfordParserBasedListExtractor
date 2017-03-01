/**
 * 
 */
package edu.cse.iitd.ListExtractor.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.cse.iitd.ListExtractor.comparators.ListExtractorComparatorDependencyIndex;
import edu.cse.iitd.ListExtractor.domain.ListExtractorConjuncts;
import edu.cse.iitd.ListExtractor.domain.ListExtractorDependencyNode;
import edu.cse.iitd.ListExtractor.domain.ListExtractorDependencyParse;
import edu.cse.iitd.ListExtractor.domain.ListExtractorPairDS;

/**
 * @author swarna
 *
 */
public class ListExtractorRuleBasedExtractor {
	
	public static List<String> getSimpleSentences(ListExtractorDependencyParse dependencyParse, ListExtractorConjuncts conjunctStructures) {
		ListExtractorDependencyNode root = dependencyParse.root;
		
		int prevEndIndex = -1, currStartIndex = 1000;
		List<String> simpleSentences = new ArrayList<String>();
		
		List<ListExtractorDependencyNode> conjunctiveWords = getConjunctiveWordsInOrder(dependencyParse);
		List<ListExtractorDependencyNode> oneConjunctListHeadsInOrder = getOneConjunctListHeadInOrder(dependencyParse);
		
		conjunctStructures.setNumberOfConjuncts(conjunctiveWords.size());
		
		int i=0;
		for(ListExtractorDependencyNode oneConjunctListHead : oneConjunctListHeadsInOrder) {
			int conjunctiveWordindex = conjunctiveWords.get(i).index;
			i++;
			List<ListExtractorDependencyNode> listHeads = getListHeads(oneConjunctListHead, dependencyParse);
			if(listHeads.size() < 2) {
				(conjunctStructures.numberOfConjunctStructures)--;
				continue;
			}
			List<List<ListExtractorDependencyNode>> conjuncts = getConjuncts(listHeads, dependencyParse);
			List<String> conjunctStrings = getConjunctStrings(conjuncts);
			
			if(conjunctStructures.addConjunctStructure(conjuncts, conjunctiveWordindex)) {
				int firstConjunctStartIndex = getFirstConjunctStartIndex(listHeads, conjuncts);
				
				currStartIndex = firstConjunctStartIndex;
				
				getSimpleSentences(simpleSentences, prevEndIndex, currStartIndex, conjunctStrings, root);
				
				int lastConjunctEndIndex = getLastConjunctEndIndex(listHeads, conjuncts);
				prevEndIndex = lastConjunctEndIndex;
			}
			else {
				(conjunctStructures.numberOfConjunctStructures)--;
			}
		}
		
		//conjunctStructures.removeWordsIfInMoreThanOneConjunct();
		
		getSimpleSentences(simpleSentences, prevEndIndex, 1000, null, root);
		
		return simpleSentences;
	}
	
	private static void getSimpleSentences(List<String> simpleSentences, int prevEndIndex, int currStartIndex, List<String> conjunctStrings, ListExtractorDependencyNode root) {
		List<ListExtractorDependencyNode> commonList = new ArrayList<ListExtractorDependencyNode>();
		
		Queue<ListExtractorDependencyNode> qNodes = new LinkedList<ListExtractorDependencyNode>();
		qNodes.add(root);
		
		Map<ListExtractorDependencyNode, Boolean> visitedNodes = new HashMap<ListExtractorDependencyNode, Boolean>();
		visitedNodes.put(root, true);
		
		while(!qNodes.isEmpty()) {
			ListExtractorDependencyNode currNode = qNodes.remove();
			
			if(currNode.index > prevEndIndex && currNode.index < currStartIndex) commonList.add(currNode);
			
			for(ListExtractorDependencyNode child : currNode.children) {
				if(!visitedNodes.containsKey(child)) {
					visitedNodes.put(child, true);
					qNodes.add(child);
				}
			}
		}
		
		String commonString = sortExpansionCreateString(commonList);
		
		if(simpleSentences.size() == 0) {
			if(conjunctStrings == null) return;
			for(String conjunctString : conjunctStrings) {
				simpleSentences.add(commonString + " " + conjunctString);
			}
		}
		else if(conjunctStrings == null) {
			List<String> tempSimpleSentences = new ArrayList<String>(simpleSentences);
			simpleSentences.clear();
			
			for(String simpleSentence : tempSimpleSentences) {
				simpleSentences.add(simpleSentence + " " + commonString);
			}
		}
		else {
			List<String> tempSimpleSentences = new ArrayList<String>(simpleSentences);
			simpleSentences.clear();
			
			for(String simpleSentence : tempSimpleSentences) {
				for(String conjunctString : conjunctStrings) {
					simpleSentences.add(simpleSentence + " " + commonString + " " + conjunctString);
				}
			}
		}
	}
	
	private static int getFirstConjunctStartIndex(List<ListExtractorDependencyNode> listHeads, List<List<ListExtractorDependencyNode>> expandedLists) {
		int firstConjunctStartIndex = -1;
		
		int i = 0, save = 0;
		for(ListExtractorDependencyNode listHead : listHeads) {
			if(firstConjunctStartIndex == -1 || (listHead.index < firstConjunctStartIndex)) {
				firstConjunctStartIndex = listHead.index;
				save = i;
			}
			i++;
		}
		
		for(ListExtractorDependencyNode node : expandedLists.get(save)) {
			if(node.index < firstConjunctStartIndex) {
				firstConjunctStartIndex = node.index;
			}
		}
		
		return firstConjunctStartIndex;
	}
	
	private static int getLastConjunctEndIndex(List<ListExtractorDependencyNode> listHeads, List<List<ListExtractorDependencyNode>> expandedLists) {
		int lastConjunctEndIndex = -1;
		
		int i = 0, save = 0;
		for(ListExtractorDependencyNode listHead : listHeads) {
			if(lastConjunctEndIndex == -1 || (listHead.index > lastConjunctEndIndex)) {
				lastConjunctEndIndex = listHead.index;
				save = i;
			}
			i++;
		}
		
		for(ListExtractorDependencyNode node : expandedLists.get(save)) {
			if(node.index > lastConjunctEndIndex) {
				lastConjunctEndIndex = node.index;
			}
		}
		
		return lastConjunctEndIndex;
	}
	
	private static List<String> getConjunctStrings(List<List<ListExtractorDependencyNode>> conjuncts) {
		List<String> conjunctStrings = new ArrayList<String>();
		
		for(List<ListExtractorDependencyNode> conjunct : conjuncts) {
			String conjunctString = sortExpansionCreateString(conjunct);
			conjunctStrings.add(conjunctString);
		}
		
		return conjunctStrings;
	}
	
	private static List<ListExtractorDependencyNode> getConjunctiveWordsInOrder(ListExtractorDependencyParse dependencyParse) {
		List<ListExtractorDependencyNode> conjunctiveWords = new ArrayList<ListExtractorDependencyNode>();
		
		for(ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode> dependency : dependencyParse.dependencies.keySet()) {
			if(dependencyParse.dependencies.get(dependency).equals("cc")) {
				conjunctiveWords.add(dependency.getSecond());
			}
		}
		
		Collections.sort(conjunctiveWords, new ListExtractorComparatorDependencyIndex());
		
		return conjunctiveWords;
	}
	
	private static List<ListExtractorDependencyNode> getOneConjunctListHeadInOrder(ListExtractorDependencyParse dependencyParse) {
		List<ListExtractorDependencyNode> oneConjunctiveListHeads = new ArrayList<ListExtractorDependencyNode>();
		
		for(ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode> dependency : dependencyParse.dependencies.keySet()) {
			if(dependencyParse.dependencies.get(dependency).equals("cc")) {
				oneConjunctiveListHeads.add(dependency.getFirst());
			}
		}
		
		Collections.sort(oneConjunctiveListHeads, new ListExtractorComparatorDependencyIndex());
		
		return oneConjunctiveListHeads;
	}
	
	private static List<ListExtractorDependencyNode> getListHeads(ListExtractorDependencyNode currNode, ListExtractorDependencyParse dependencyParse) {
		List<ListExtractorDependencyNode> listHeads = new ArrayList<ListExtractorDependencyNode>();
		
		listHeads.add(currNode);
		
		for(ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode> dependency : dependencyParse.dependencies.keySet()) {
			if(dependency.getFirst().equals(currNode) && 
					(dependencyParse.dependencies.get(dependency).equals("conj"))) {
				listHeads.add(dependency.getSecond());
			}
		}
		
		return listHeads;
	}
	
	private static List<List<ListExtractorDependencyNode>> getConjuncts(List<ListExtractorDependencyNode> listHeads, ListExtractorDependencyParse dependencyParse) {
		List<List<ListExtractorDependencyNode>> conjuncts = new ArrayList<List<ListExtractorDependencyNode>>();
		
		for(ListExtractorDependencyNode listHead : listHeads) {
			// expand the subtree, except the child connected by "conj"
			List<ListExtractorDependencyNode> expandedList = getExpandedList(listHead, dependencyParse, conjuncts);
			conjuncts.add(expandedList);
		}
		
		return conjuncts;
	}
	
	private static List<ListExtractorDependencyNode> getExpandedList(ListExtractorDependencyNode listHead, ListExtractorDependencyParse dependencyParse, List<List<ListExtractorDependencyNode>> conjuncts) {
		List<ListExtractorDependencyNode> expandedList = new ArrayList<ListExtractorDependencyNode>();
		
		
		Queue<ListExtractorDependencyNode> qNodes = new LinkedList<ListExtractorDependencyNode>();
		qNodes.add(listHead);
		
		Map<ListExtractorDependencyNode, Boolean> visitedNodes = new HashMap<ListExtractorDependencyNode,Boolean>();
		visitedNodes.put(listHead, true);
		
		while(!qNodes.isEmpty()) {
			ListExtractorDependencyNode currNode = qNodes.remove();
			expandedList.add(currNode);
			
			for(ListExtractorDependencyNode child : currNode.children) {
				String dependencyLabel = dependencyParse.dependencies.get(new ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode>(currNode, child));
				if(!dependencyLabel.equals("cc") && !dependencyLabel.equals("conj") && !dependencyLabel.equals("prep")) {
					if(!visitedNodes.containsKey(child) && !isIncludedInOtherConjuncts(child, conjuncts)) {
						visitedNodes.put(child, true);
						qNodes.add(child);
					}
				}
			}
		}
		
		return expandedList;
	}
	
	private static boolean isIncludedInOtherConjuncts(ListExtractorDependencyNode node, List<List<ListExtractorDependencyNode>> conjuncts) {
		for(List<ListExtractorDependencyNode> conjunct : conjuncts) {
			if(conjunct.contains(node)) return true;
		}
		return false;
	}
	
	private static String sortExpansionCreateString(List<ListExtractorDependencyNode> expandedList) {
		Collections.sort(expandedList, new ListExtractorComparatorDependencyIndex());
		StringBuilder sb = new StringBuilder("");
		for (ListExtractorDependencyNode expansion : expandedList) {
			sb.append(expansion.word + " ");
        }
		return sb.toString().trim();
	}
}
