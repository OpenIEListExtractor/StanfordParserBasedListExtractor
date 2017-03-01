/**
 * 
 */
package edu.cse.iitd.ListExtractor.comparators;

import java.util.Comparator;
import java.util.List;

/**
 * @author swarna
 *
 */
public class ListExtractorComparatorIntegerList implements Comparator<List<Integer>>{
	@Override
    public int compare(List<Integer> list1, List<Integer> list2) {
	    return list1.get(0) - list2.get(0);
    }
}
