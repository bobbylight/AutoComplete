/*
 * 12/23/2008
 *
 * CompletionCellRenderer.java - Cell renderer that can render the standard
 * completion types like Eclipse or NetBeans does.
 * Copyright (C) 2008 Robert Futrell
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;


/**
 * A cell renderer that adds some pizazz when rendering the standard
 * {@link Completion} types, like Eclipse and NetBeans do.  Specifically,
 * this renderer handles:  
 * 
 * <ul>
 *    <li>{@link FunctionCompletion}s</li>
 *    <li>{@link VariableCompletion}s</li>
 *    <li>{@link MarkupTagCompletion}s</li>
 *    <li>{@link ShorthandCompletion}s</li>
 * </ul>
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class CompletionCellRenderer extends DefaultListCellRenderer {

	/**
	 * The alternating background color.
	 */
	private Color altBG;

	/**
	 * The font to use when rendering items.
	 */
	private Font font;


	/**
	 * Constructor.
	 */
	public CompletionCellRenderer() {
		setDisplayFont(new Font("Monospaced", Font.PLAIN, 12));
		setAlternateBackground(new Color(240,240,240));
	}


	/**
	 * Returns the background color to use on alternating lines.
	 *
	 * @return The altnernate background color.  If this is <code>null</code>,
	 *         alternating colors are not used.
	 * @see #setAlternateBackground(Color)
	 */
	public Color getAlternateBackground() {
		return altBG;
	}


	/**
	 * Returns the font used when rendering completions.
	 *
	 * @return The font.  If this is <code>null</code>, then the default list
	 *         font is used.
	 * @see #setDisplayFont(Font)
	 */
	public Font getDisplayFont() {
		return font;
	}


	/**
	 * Returns the renderer.
	 *
	 * @param list The list of choices being rendered.
	 * @param value The {@link Completion} being rendered.
	 * @param index The index into <code>list</code> being rendered.
	 * @param selected Whether the item is selected.
	 * @param hasFocus Whether the item has focus.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
						int index, boolean selected, boolean hasFocus) {

		super.getListCellRendererComponent(list,value,index,selected,hasFocus);
		setFont(font); // Overrides super's setFont(list.getFont()).

		if (value instanceof FunctionCompletion) {
			FunctionCompletion fc = (FunctionCompletion)value;
			prepareForFunctionCompletion(list, fc, index, selected, hasFocus);
		}
		else if (value instanceof VariableCompletion) {
			VariableCompletion vc = (VariableCompletion)value;
			prepareForVariableCompletion(list, vc, index, selected, hasFocus);
		}
		else if (value instanceof MarkupTagCompletion) {
			MarkupTagCompletion mtc = (MarkupTagCompletion)value;
			prepareForMarkupTagCompletion(list, mtc, index, selected, hasFocus);
		}
		else {
			Completion c = (Completion)value;
			prepareForOtherCompletion(list, c, index, selected, hasFocus);
		}

		if (!selected && (index&1)==0 && altBG!=null) {
			setBackground(altBG);
		}

		return this;

	}


	/**
	 * Prepares this renderer to display a function completion. 
	 *
	 * @param list The list of choices being rendered.
	 * @param fc The completion to render.
	 * @param index The index into <code>list</code> being rendered.
	 * @param selected Whether the item is selected.
	 * @param hasFocus Whether the item has focus.
	 */
	protected void prepareForFunctionCompletion(JList list,
		FunctionCompletion fc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(fc.getName());
		sb.append("</em></b>");

		sb.append(fc.getProvider().getParameterListStart());
		int paramCount = fc.getParamCount();
		for (int i=0; i<paramCount; i++) {
			FunctionCompletion.Parameter param = fc.getParam(i);
			String type = param.getType();
			String name = param.getName();
			if (type!=null) {
				if (!selected) {
					sb.append("<font color='#aa0077'>");
				}
				sb.append(type);
				if (!selected) {
					sb.append("</font>");
				}
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<paramCount-1) {
				sb.append(fc.getProvider().getParameterListSeparator());
			}
		}
		sb.append(fc.getProvider().getParameterListEnd());
		sb.append(" : ");
		if (!selected) {
			sb.append("<font color='#a0a0ff'>");
		}
		sb.append(fc.getType());
		if (!selected) {
			sb.append("</font>");
		}

		setText(sb.toString());

	}


	/**
	 * Prepares this renderer to display a markup tag completion.
	 *
	 * @param list The list of choices being rendered.
	 * @param mc The completion to render.
	 * @param index The index into <code>list</code> being rendered.
	 * @param selected Whether the item is selected.
	 * @param hasFocus Whether the item has focus.
	 */
	protected void prepareForMarkupTagCompletion(JList list,
		MarkupTagCompletion mc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(mc.getName());
		sb.append("</em></b>");

		setText(sb.toString());

	}


	/**
	 * Prepares this renderer to display a completion not specifically handled
	 * elsewhere.
	 *
	 * @param list The list of choices being rendered.
	 * @param c The completion to render.
	 * @param index The index into <code>list</code> being rendered.
	 * @param selected Whether the item is selected.
	 * @param hasFocus Whether the item has focus.
	 */
	protected void prepareForOtherCompletion(JList list,
		Completion c, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(c.getInputText());
		sb.append("</em></b>");

		setText(sb.toString());

	}


	/**
	 * Prepares this renderer to display a variable completion.
	 *
	 * @param list The list of choices being rendered.
	 * @param vc The completion to render.
	 * @param index The index into <code>list</code> being rendered.
	 * @param selected Whether the item is selected.
	 * @param hasFocus Whether the item has focus.
	 */
	protected void prepareForVariableCompletion(JList list,
		VariableCompletion vc, int index, boolean selected, boolean hasFocus) {

		StringBuffer sb = new StringBuffer("<html><b><em>");
		sb.append(vc.getName());
		sb.append("</em></b>");

		if (vc.getType()!=null) {
			sb.append(" : ");
			if (!selected) {
				sb.append("<font color='#a0a0ff'>");
			}
			sb.append(vc.getType());
			if (!selected) {
				sb.append("</font>");
			}
		}

		setText(sb.toString());

	}


	/**
	 * Sets the background color to use on alternating lines.
	 *
	 * @param altBG The new alternate background color.  If this is
	 *        <code>null</code>, alternating lines will not use different
	 *        background colors.
	 * @see #getAlternateBackground()
	 */
	public void setAlternateBackground(Color altBG) {
		this.altBG = altBG;
	}


	/**
	 * Sets the font to use when rendering completion items.
	 *
	 * @param font The font to use.  If this is <code>null</code>, then
	 *        the default list font is used.
	 * @see #getDisplayFont()
	 */
	public void setDisplayFont(Font font) {
		this.font = font;
	}


}