/**
 * 
 */
package edu.cse.iitd.ListExtractor.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author swarna
 *
 */
public class ListExtractorConjunctStructure {
	public int conjunctiveWordIndex;
	public int numberOfConjuncts;
	public List<List<Integer>> conjuncts = new ArrayList<List<Integer>>();
	
	public ListExtractorConjunctStructure(int conjunctiveWordIndex) {
		this.conjunctiveWordIndex = conjunctiveWordIndex;
	}
	
	public void setNumberOfConjuncts(int numberOfConjuncts) {
		this.numberOfConjuncts = numberOfConjuncts;
	}
	
	public ListExtractorConjunctStructure(int conjunctiveWordIndex, List<List<ListExtractorDependencyNode>> conjuncts) {
		this.conjunctiveWordIndex = conjunctiveWordIndex-1;
		this.numberOfConjuncts = conjuncts.size();
		
		for(List<ListExtractorDependencyNode> conjunct : conjuncts) {
			List<Integer> conjunctList = new ArrayList<Integer>();
			for(int i=0; i<conjunct.size(); i++) {
				if(i!=conjunct.size()-1 || (!conjunct.get(i).word.equals(",") && !conjunct.get(i).word.equals("."))) {
					conjunctList.add(conjunct.get(i).index-1);
				}
			}
			this.conjuncts.add(conjunctList);
		}
	}
}
