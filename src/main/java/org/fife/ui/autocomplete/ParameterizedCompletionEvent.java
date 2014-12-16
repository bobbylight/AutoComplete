package org.fife.ui.autocomplete;


/**
 * An event fired by an instance of {@link AutoCompletion}.  
 * This can be used by applications that wish to be notified of the auto-complete of {@link ParameterizedCompletion} 
 * @author Ricardo JL Rufino (ricardo@criativasoft.com.br)
 */
public class ParameterizedCompletionEvent extends AutoCompletionEvent{
    
    private ParameterizedCompletionContext context;
    
    private int paramIndex;
    private String choice; 

    public ParameterizedCompletionEvent(ParameterizedCompletionContext context, Type type) {
        super(context.getAutoCompletion(), type);
        this.context = context;
    }
    
    public void setParamIndex( int paramIndex ) {
        this.paramIndex = paramIndex;
    }
    
    public void setChoice( String choice ) {
        this.choice = choice;
    }
    
    /**
     * Get the execution context of {@link ParameterizedCompletion}
     * You can use {@link ParameterizedCompletionContext#getParameterValues()}, to get the values of the parameters entered by the user.
     * @return The ParameterizedCompletionContext
     */
    public ParameterizedCompletionContext getContext() {
        return context;
    }
    
    public ParameterizedCompletion getCompletion() {
        return context.getParameterizedCompletion();
    }
    
    /**
     * Get the index of the parameter being edited. <br/>
     * This only works for the event: {@link AutoCompletionEvent.Type#PARAMETER_COMPLETION_SELECT}
     * @return
     */
    public int getParamIndex() {
        return paramIndex;
    }
    
    /**
     * Gets the element that was selected in the parameter autocomplete list. <br/>
     * This only works for the event: {@link AutoCompletionEvent.Type#PARAMETER_COMPLETION_SELECT}
     * @return
     */
    public String getChoice() {
        return choice;
    }

}
