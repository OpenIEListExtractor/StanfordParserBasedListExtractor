/**
 * 
 */
package edu.cse.iitd.ListExtractor.comparators;

import java.util.Comparator;

import edu.cse.iitd.ListExtractor.domain.ListExtractorDependencyNode;

/**
 * @author swarna
 *
 */
public class ListExtractorComparatorDependencyIndex implements Comparator<ListExtractorDependencyNode>{
	@Override
    public int compare(ListExtractorDependencyNode o1, ListExtractorDependencyNode o2) {
	    return o1.index - o2.index;
    }
}
