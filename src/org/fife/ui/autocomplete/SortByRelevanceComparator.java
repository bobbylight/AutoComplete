/*
 * 12/17/2010
 *
 * SortByRelevanceComparator.java - Sorts two Completions by relevance before
 * sorting them lexicographically.
 * Copyright (C) 2020 Robert Futrell
 * robert_futrell at users.sourceforge.net
 * http://fifesoft.com/rsyntaxtextarea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA.
 */
package org.fife.ui.autocomplete;

import java.util.Comparator;


/**
 * Compares two <code>Completion</code>s by their relevance before
 * sorting them lexicographically.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SortByRelevanceComparator implements Comparator {


	public int compare(Object o1, Object o2) {
		Completion c1 = (Completion)o1;
		Completion c2 = (Completion)o2;
		int rel1 = c1.getRelevance();
		int rel2 = c2.getRelevance();
		int diff = rel2 - rel1;//rel1 - rel2;
		return diff==0 ? c1.compareTo(c2) : diff;
	}


}