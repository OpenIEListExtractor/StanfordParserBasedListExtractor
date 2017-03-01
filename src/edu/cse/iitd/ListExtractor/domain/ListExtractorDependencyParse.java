/**
 * 
 */
package edu.cse.iitd.ListExtractor.domain;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import edu.stanford.nlp.trees.TypedDependency;

/**
 * @author swarna
 *
 */
public class ListExtractorDependencyParse {
	public String sentence;
	
	public ListExtractorDependencyNode root;
	
	public Map<ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode>, String> dependencies = 
			new HashMap<ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode>, String>();
	
	public ListExtractorDependencyParse(String sentence, ListExtractorDependencyNode root) {
		this.sentence = sentence;
		this.root = root;
	}
	
	public ListExtractorDependencyParse(String sentence, List<TypedDependency> tdl) {
		this.sentence = sentence;
		
		ListExtractorDependencyNode root = null;
		
		for(TypedDependency dependency : tdl) {
			if(dependency.reln().getShortName().equals("root")) {
				String word = dependency.dep().word().toString();
				String posTag = dependency.dep().tag().toString();
				int index = dependency.dep().index();
				
				root = new ListExtractorDependencyNode(word, posTag, index, null);
			}
		}
		
		Map<Integer, ListExtractorDependencyNode> visitedIndices = new HashMap<Integer, ListExtractorDependencyNode>();
		visitedIndices.put(root.index, root);
		
		Queue<ListExtractorDependencyNode> qNodes = new LinkedList<ListExtractorDependencyNode>();
		
		qNodes.add(root);
		
		while(!qNodes.isEmpty()) {
			ListExtractorDependencyNode currNode = qNodes.remove();
			
			for(TypedDependency dependency : tdl) {
				if(dependency.gov().index() == currNode.index) {
					String dependencyLabel = dependency.reln().getShortName();
					String word = dependency.dep().word().toString();
					String posTag = dependency.dep().tag().toString();
					int index = dependency.dep().index();
					
					if(!visitedIndices.containsKey(index)) {
						ListExtractorDependencyNode newNode = new ListExtractorDependencyNode(word, posTag, index, currNode);
						this.addDependency(dependencyLabel, currNode, newNode);
						currNode.children.add(newNode);
						qNodes.add(newNode);
						visitedIndices.put(index, newNode);
					}
					else {
						ListExtractorDependencyNode child = visitedIndices.get(index);
						this.addDependency(dependencyLabel, currNode, child);
						currNode.children.add(child);
						child.parent.add(currNode);
					}
				}
			}
		}
		
		this.root = root;
		
	}
	
	public void addDependency(String dependencyLabel, ListExtractorDependencyNode currNode, ListExtractorDependencyNode childNode) {
		this.dependencies.put(new ListExtractorPairDS<ListExtractorDependencyNode, ListExtractorDependencyNode>(currNode, childNode), dependencyLabel);
	}
}
