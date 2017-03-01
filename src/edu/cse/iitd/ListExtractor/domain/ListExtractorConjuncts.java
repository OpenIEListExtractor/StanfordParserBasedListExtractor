/**
 * 
 */
package edu.cse.iitd.ListExtractor.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author swarna
 *
 */
public class ListExtractorConjuncts {
	public int numberOfConjunctStructures;
	public List<ListExtractorConjunctStructure> conjunctStructures = new ArrayList<ListExtractorConjunctStructure>();
	public String sentence;
	
	public ListExtractorConjuncts() {
		
	}
	
	public ListExtractorConjuncts(String sentence) {
		this.sentence = sentence;
	}
	
	public ListExtractorConjuncts(int number) {
		this.numberOfConjunctStructures = number;
	}
	
	public void setNumberOfConjuncts(int numberOfConjunctStructures) {
		this.numberOfConjunctStructures = numberOfConjunctStructures;
	}
	
	public boolean addConjunctStructure(List<List<ListExtractorDependencyNode>> conjuncts, int conjunctiveWordIndex) {
		boolean isConjunctPresent = false;
		for(List<ListExtractorDependencyNode> conjunct : conjuncts) {
			List<Integer> conjunctList = new ArrayList<Integer>();
			for(int i=0; i<conjunct.size(); i++) {
				conjunctList.add(conjunct.get(i).index-1);
			}
			if(isConjunctSame(conjunctList)) {
				isConjunctPresent = true;
				break;
			}
		}
		if(!isConjunctPresent) {
			this.conjunctStructures.add(new ListExtractorConjunctStructure(conjunctiveWordIndex, conjuncts));
		}
		return !isConjunctPresent;
	}
	
	public void addConjunctStructure(ListExtractorConjunctStructure conjunctStructure) {
		this.conjunctStructures.add(conjunctStructure);
	}
	
	private boolean isConjunctSame(List<Integer> conjunctList) {
		for(ListExtractorConjunctStructure conjunctStrcuture : this.conjunctStructures) {
			for(List<Integer> conjunct : conjunctStrcuture.conjuncts) {
				if(isSubset(conjunctList, conjunct) || isSubset(conjunct, conjunctList)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isSubset(List<Integer> list1, List<Integer> list2) {
		Collections.sort(list1);
		Collections.sort(list2);
		
		int i;
		for(i=0; i<Math.min(list1.size(), list2.size()); i++) {
			if(list1.get(i) != list2.get(i)) {
				break;
			}
		}
		
		return i == Math.min(list1.size(), list2.size());
	}
	
	/*public void removeWordsIfInMoreThanOneConjunct() {
		for(ListExtractorConjunctStructure conjunctStrcuture : this.conjunctStructures) {
			Map<Integer, Integer> countOfWords = new HashMap<Integer, Integer>();
			for(List<Integer> conjunct : conjunctStrcuture.conjuncts) {
				for(Integer index : conjunct) {
					if(countOfWords.containsKey(index)) {
						int count = countOfWords.get(index);
						countOfWords.put(index, count+1);
					}
					else {
						countOfWords.put(index, 1);
					}
				}
			}
			
			for(List<Integer> conjunct : conjunctStrcuture.conjuncts) {
				Iterator<Integer> iter = conjunct.iterator();
				while(iter.hasNext()) {
					int index = iter.next();
					if(countOfWords.get(index) > 1) {
						iter.remove();
					}
				}
				if(conjunct.size() == 0) {
					conjunctStrcuture.conjuncts.remove(conjunct);
				}
			}
			
			if(conjunctStrcuture.conjuncts.size() == 0) {
				this.conjunctStructures.remove(conjunctStrcuture);
				(this.numberOfConjunctStructures)--;
			}
		}
	}*/
}
