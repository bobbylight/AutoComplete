/*
 * 12/22/2008
 *
 * FunctionCompletion.java - A completion representing a function.
 *
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;


/**
 * A completion choice representing a function.<p>
 *
 * In order to use any instance of a {@code ParameterizedCompletion}, you need
 * to call {@link AutoCompletion#setParameterAssistanceEnabled(boolean)} with
 * a value of {@code true} first.  Otherwise, any completion choices will be
 * visible in the completion list, but won't insert anything when selected.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class FunctionCompletion extends VariableCompletion
								implements ParameterizedCompletion {

	/**
	 * Parameters to the function.
	 */
	private List<Parameter> params;

	/**
	 * A description of the return value of this function.
	 */
	private String returnValDesc;

	/**
	 * Used to improve performance of sorting FunctionCompletions.
	 */
	private String compareString;


	/**
	 * Constructor.
	 *
	 * @param provider The parent provider.
	 * @param name The name of this function.
	 * @param returnType The return type of this function.
	 */
	public FunctionCompletion(CompletionProvider provider, String name,
								String returnType) {
		super(provider, name, returnType);
	}


	@Override
	protected void addDefinitionString(StringBuilder sb) {
		sb.append("<html><b>");
		sb.append(getDefinitionString());
		sb.append("</b>");
	}


	/**
	 * Adds HTML describing the parameters to this function to a buffer.
	 *
	 * @param sb The buffer to append to.
	 */
	protected void addParameters(StringBuilder sb) {

		// TODO: Localize me

		int paramCount = getParamCount();
		if (paramCount>0) {
			sb.append("<b>Parameters:</b><br>");
			sb.append("<center><table width='90%'><tr><td>");
			for (int i=0; i<paramCount; i++) {
				Parameter param = getParam(i);
				sb.append("<b>");
				sb.append(param.getName()!=null ? param.getName() :
							param.getType());
				sb.append("</b>&nbsp;");
				String desc = param.getDescription();
				if (desc!=null) {
					sb.append(desc);
				}
				sb.append("<br>");
			}
			sb.append("</td></tr></table></center><br><br>");
		}

		if (returnValDesc!=null) {
			sb.append("<b>Returns:</b><br><center><table width='90%'><tr><td>");
			sb.append(returnValDesc);
			sb.append("</td></tr></table></center><br><br>");
		}

	}


	/**
	 * Overridden to compare methods by their comparison strings.
	 *
	 * @param c2 A <code>Completion</code> to compare to.
	 * @return The sort order.
	 */
	@Override
	public int compareTo(Completion c2) {

		int rc;

		if (c2==this) {
			rc = 0;
		}

		else if (c2 instanceof FunctionCompletion) {
			rc = getCompareString().compareTo(
					((FunctionCompletion)c2).getCompareString());
		}

		else {
			rc = super.compareTo(c2);
		}

		return rc;

	}


	/**
	 * Overridden to match the behavior of {@code compareTo(Completion)}.
	 *
	 * @param other Another object.
	 * @return Whether these two objects are equal.
	 */
	@Override
	public boolean equals(Object other) {
		return other instanceof Completion && compareTo((Completion)other) == 0;
	}

	/**
	 * Returns a string used to compare this method completion to another.
	 *
	 * @return The comparison string.
	 */
	private String getCompareString() {

		/*
		 * This string compares the following parts of methods in this order,
		 * to optimize sort order in completion lists.
		 *
		 * 1. First, by name
		 * 2. Next, by number of parameters.
		 * 3. Finally, by parameter type.
		 */

		if (compareString==null) {
			StringBuilder sb = new StringBuilder(getName());
			// NOTE: This will fail if a method has > 99 parameters (!)
			int paramCount = getParamCount();
			if (paramCount<10) {
				sb.append('0');
			}
			sb.append(paramCount);
			for (int i=0; i<paramCount; i++) {
				String type = getParam(i).getType();
				sb.append(type);
				if (i<paramCount-1) {
					sb.append(',');
				}
			}
			compareString = sb.toString();
		}

		return compareString;

	}


	/**
	 * Returns the "definition string" for this function completion.  For
	 * example, for the C "<code>printf</code>" function, this would return
	 * "<code>int printf(const char *, ...)</code>".
	 *
	 * @return The definition string.
	 */
	@Override
	public String getDefinitionString() {

		StringBuilder sb = new StringBuilder();

		// Add the return type if applicable (C macros like NULL have no type).
		String type = getType();
		if (type!=null) {
			sb.append(type).append(' ');
		}

		// Add the item being described's name.
		sb.append(getName());

		// Add parameters for functions.
		CompletionProvider provider = getProvider();
		char start = provider.getParameterListStart();
		if (start!=0) {
			sb.append(start);
		}
		for (int i=0; i<getParamCount(); i++) {
			Parameter param = getParam(i);
			type = param.getType();
			String name = param.getName();
			if (type!=null) {
				sb.append(type);
				if (name!=null) {
					sb.append(' ');
				}
			}
			if (name!=null) {
				sb.append(name);
			}
			if (i<params.size()-1) {
				sb.append(provider.getParameterListSeparator());
			}
		}
		char end = provider.getParameterListEnd();
		if (end!=0) {
			sb.append(end);
		}

		return sb.toString();

	}


	@Override
	public ParameterizedCompletionInsertionInfo getInsertionInfo(
			JTextComponent tc, boolean replaceTabsWithSpaces) {

		ParameterizedCompletionInsertionInfo info =
			new ParameterizedCompletionInsertionInfo();

		StringBuilder sb = new StringBuilder();
		char paramListStart = getProvider().getParameterListStart();
		if (paramListStart!='\0') {
			sb.append(paramListStart);
		}
		int dot = tc.getCaretPosition() + sb.length();
		int paramCount = getParamCount();

		// Get the range in which the caret can move before we hide
		// this tool tip.
		int minPos = dot;
		Position maxPos = null;
		try {
			maxPos = tc.getDocument().createPosition(dot-sb.length()+1);
		} catch (BadLocationException ble) {
			ble.printStackTrace(); // Never happens
		}
		info.setCaretRange(minPos, maxPos);
		int firstParamLen = 0;

		// Create the text to insert (keep it one completion for
		// performance and simplicity of undo/redo).
		int start = dot;
		for (int i=0; i<paramCount; i++) {
			Parameter param = getParam(i);
			String paramText = getParamText(param);
			if (i==0) {
				firstParamLen = paramText.length();
			}
			sb.append(paramText);
			int end = start + paramText.length();
			info.addReplacementLocation(start, end);
			// Patch for param. list separators with length > 2 -
			// thanks to Matthew Adereth!
			String sep = getProvider().getParameterListSeparator();
			if (i<paramCount-1 && sep!=null) {
				sb.append(sep);
				start = end + sep.length();
			}
		}
		sb.append(getProvider().getParameterListEnd());
		int endOffs = dot + sb.length();
		endOffs -= 1;//getProvider().getParameterListStart().length();
		info.addReplacementLocation(endOffs, endOffs); // offset after function
		info.setDefaultEndOffs(endOffs);

		int selectionEnd = paramCount>0 ? (dot+firstParamLen) : dot;
		info.setInitialSelection(dot, selectionEnd);
		info.setTextToInsert(sb.toString());
		return info;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Parameter getParam(int index) {
		return params.get(index);
	}


	/**
	 * Returns the number of parameters to this function.
	 *
	 * @return The number of parameters to this function.
	 * @see #getParam(int)
	 */
	@Override
	public int getParamCount() {
		return params==null ? 0 : params.size();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getShowParameterToolTip() {
		return true;
	}


	/**
	 * Returns the text to insert for a parameter.
	 *
	 * @param param The parameter.
	 * @return The text.
	 */
	private String getParamText(ParameterizedCompletion.Parameter param) {
		String text = param.getName();
		if (text==null) {
			text = param.getType();
			if (text==null) { // Shouldn't ever happen
				text = "arg";
			}
		}
		return text;
	}


	/**
	 * Returns the description of the return value of this function.
	 *
	 * @return The description, or <code>null</code> if there is none.
	 * @see #setReturnValueDescription(String)
	 */
	public String getReturnValueDescription() {
		return returnValDesc;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		addDefinitionString(sb);
		if (!possiblyAddDescription(sb)) {
			sb.append("<br><br><br>");
		}
		addParameters(sb);
		possiblyAddDefinedIn(sb);
		return sb.toString();
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE",
		justification = "Subclasses could return null")
	public String getToolTipText() {
		String text = getSummary();
		if (text==null) {
			text = getDefinitionString();
		}
		return text;
	}

	@Override
	public int hashCode() {

		int hashCode = super.hashCode();

		for (int i = 0; i < getParamCount(); i++) {
			hashCode ^= getParam(i).hashCode();
		}

		hashCode ^= returnValDesc != null ? returnValDesc.hashCode() : 0;
		hashCode ^= compareString != null ? compareString.hashCode() : 0;

		return hashCode;
	}


	/**
	 * Sets the parameters to this function.
	 *
	 * @param params The parameters.  This should be a list of
	 *        {@link ParameterizedCompletion.Parameter}s.
	 * @see #getParam(int)
	 * @see #getParamCount()
	 */
	public void setParams(List<Parameter> params) {
		if (params!=null) {
			// Deep copy so parsing can re-use its array.
			this.params = new ArrayList<>(params);
		}
	}


	/**
	 * Sets the description of the return value of this function.
	 *
	 * @param desc The description.
	 * @see #getReturnValueDescription()
	 */
	public void setReturnValueDescription(String desc) {
		this.returnValDesc = desc;
	}


}
