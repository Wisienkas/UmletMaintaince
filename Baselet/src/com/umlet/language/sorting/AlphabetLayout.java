package com.umlet.language.sorting;

import java.util.Comparator;
import java.util.List;

import com.umlet.language.SortableElement;

public class AlphabetLayout extends Layout {

	@Override
	public void layout(List<SortableElement> elements) {
		super.simpleLayout(new AlphabetSorter(), elements);
	}

	private class AlphabetSorter implements Comparator<SortableElement> {

		@Override
		public int compare(SortableElement e1, SortableElement e2) {
			return e1.getName().compareTo(e2.getName());
		}
	}
}
