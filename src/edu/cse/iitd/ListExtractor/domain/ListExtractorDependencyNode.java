/**
 * 
 */
package edu.cse.iitd.ListExtractor.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author swarna
 *
 */
public class ListExtractorDependencyNode {
	//public String dependencyLabel;
	public String word;
	public String posTag;
	
	public int index;
	
	public List<ListExtractorDependencyNode> children = new ArrayList<ListExtractorDependencyNode>();
	public List<ListExtractorDependencyNode> parent = new ArrayList<ListExtractorDependencyNode>();
	
	public ListExtractorDependencyNode(String word, String posTag, int index, ListExtractorDependencyNode parent) {
		//this.dependencyLabel = dependencyLabel;
		this.word = word;
		this.posTag = posTag;
		
		this.index = index;
		
		if(parent != null) {
			this.parent.add(parent);
		}
	}
	
	public String toString() {
		return "(" + "_" + word + "_" + posTag + ")";
	}
}
