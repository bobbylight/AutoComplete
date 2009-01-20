package org.fife.ui.autocomplete;


/**
 * A completion option that takes parameters, such as a function or method.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public interface ParameterizedCompletion extends Completion {


	/**
	 * Returns the "definition string" for this completion.  For example,
	 * for the C "<code>printf</code>" function, this would return
	 * "<code>int printf(const char *, ...)</code>".
	 * 
	 * @return The definition string.
	 */
	public String getDefinitionString();


	/**
	 * Returns the specified {@link Parameter}.
	 *
	 * @param index The index of the parameter to retrieve.
	 * @return The parameter.
	 * @see #getParamCount()
	 */
	public Parameter getParam(int index);


	/**
	 * Returns the number of parameters this completion takes.
	 *
	 * @return The number of parameters this completion takes.
	 * @see #getParam(int)
	 */
	public int getParamCount();


	/**
	 * A parameter passed to a parameterized {@link Completion}.
	 *
	 * @author Robert Futrell
	 * @version 1.0
	 */
	public static class Parameter {

		private String name;
		private String type;
		private String desc;

		public Parameter(String type, String name) {
			this.name = name;
			this.type = type;
		}

		public String getDescription() {
			return desc;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public void setDescription(String desc) {
			this.desc = desc;
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (type!=null) {
				sb.append(type);
			}
			if (name!=null) {
				if (type!=null) {
					sb.append(' ');
				}
				sb.append(name);
			}
			return sb.toString();
		}

	}


}