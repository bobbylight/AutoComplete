/*
 * 12/23/2008
 *
 * ProceduralLanguageCellRenderer.java - Cell renderer for procedural
 * languages.
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
 * A cell renderer for {@link ProceduralLanguageCompletionProvider}.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class ProceduralLanguageCellRenderer extends DefaultListCellRenderer {

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
	public ProceduralLanguageCellRenderer() {
		font = new Font("Monospaced", Font.PLAIN, 12);
		altBG = new Color(248,248,248);
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
	 * @return The font.
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
//		System.out.println(index);
		setFont(font); // Overrides super's setFont(list.getFont()).

		if (value instanceof FunctionCompletion) {
			FunctionCompletion fc = (FunctionCompletion)value;
			StringBuffer sb = new StringBuffer("<html><b><em>" + fc.getName() + "</em></b>");
			sb.append('(');
			int paramCount = fc.getParamCount();
			for (int i=0; i<paramCount; i++) {
				FunctionCompletion.Parameter param = fc.getParam(i);
				String type = param.getType();
				if (type!=null) {
					if (!selected) {
						sb.append("<font color='#aa0077'>");
					}
					sb.append(type);
					if (!selected) {
						sb.append("</font>");
					}
					sb.append(' ');
				}
				String name = param.getName();
				if (name!=null) {
					sb.append(name);
				}
				if (i<paramCount-1) {
					sb.append(", ");
				}
			}
			sb.append(") : ");
			if (!selected) {
				sb.append("<font color='#a0a0ff'>");
			}
			sb.append(fc.getType());
			if (!selected) {
				sb.append("</font>");
			}
			setText(sb.toString());
		}

		else if (value instanceof VariableCompletion) {
			VariableCompletion vc = (VariableCompletion)value;
			StringBuffer sb = new StringBuffer("<html><b><em>" + vc.getName() + "</em></b>");
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

		if (!selected && (index&1)==0 && altBG!=null) {
			setBackground(altBG);
		}

//		if (index==238) {
//			Thread.dumpStack();
//		}
//setIcon(varIcon);
		return this;

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
	 * @param font The font to use.
	 * @see #getDisplayFont()
	 */
	public void setDisplayFont(Font font) {
		this.font = font;
	}


}