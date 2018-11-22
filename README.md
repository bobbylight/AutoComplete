AutoComplete is a code completion library for Swing JTextComponents, with enhanced functionality available when used with its sister project [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea).

Features include a completion choices list that updates as the user types, a "documentation" companion window for displaying documentation about the currently selected completion choice, and parameter assistance (e.g. tabbing through function/method parameters, with tool tip assistance for each argument and a possible list of valid variable completions for each).

AutoComplete is available under a [modified BSD license](https://github.com/bobbylight/AutoComplete/blob/master/AutoComplete/src/main/dist/AutoComplete.License.txt).

Available in the [Maven Central repository](http://search.maven.org/#search%7Cga%7C1%7Cautocomplete%20jar) (`com.fifesoft:autocomplete:XXX`).
SNAPSHOT builds of the in-development, unreleased version are hosted on [Sonatype](https://oss.sonatype.org/content/repositories/snapshots/com/fifesoft/autocomplete/).

# Example Usage

The example below shows how to add code completion for simple keywords to RSyntaxTextArea.

```java
import java.awt.*;
import javax.swing.*;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class AutoCompleteDemo extends JFrame {

   public AutoCompleteDemo() {

      JPanel contentPane = new JPanel(new BorderLayout());
      RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
      textArea.setCodeFoldingEnabled(true);
      contentPane.add(new RTextScrollPane(textArea));

      // A CompletionProvider is what knows of all possible completions, and
      // analyzes the contents of the text area at the caret position to
      // determine what completion choices should be presented. Most instances
      // of CompletionProvider (such as DefaultCompletionProvider) are designed
      // so that they can be shared among multiple text components.
      CompletionProvider provider = createCompletionProvider();

      // An AutoCompletion acts as a "middle-man" between a text component
      // and a CompletionProvider. It manages any options associated with
      // the auto-completion (the popup trigger key, whether to display a
      // documentation window along with completion choices, etc.). Unlike
      // CompletionProviders, instances of AutoCompletion cannot be shared
      // among multiple text components.
      AutoCompletion ac = new AutoCompletion(provider);
      ac.install(textArea);

      setContentPane(contentPane);
      setTitle("AutoComplete Demo");
      setDefaultCloseOperation(EXIT_ON_CLOSE);
      pack();
      setLocationRelativeTo(null);

   }

   /**
    * Create a simple provider that adds some Java-related completions.
    */
   private CompletionProvider createCompletionProvider() {

      // A DefaultCompletionProvider is the simplest concrete implementation
      // of CompletionProvider. This provider has no understanding of
      // language semantics. It simply checks the text entered up to the
      // caret position for a match against known completions. This is all
      // that is needed in the majority of cases.
      DefaultCompletionProvider provider = new DefaultCompletionProvider();

      // Add completions for all Java keywords. A BasicCompletion is just
      // a straightforward word completion.
      provider.addCompletion(new BasicCompletion(provider, "abstract"));
      provider.addCompletion(new BasicCompletion(provider, "assert"));
      provider.addCompletion(new BasicCompletion(provider, "break"));
      provider.addCompletion(new BasicCompletion(provider, "case"));
      // ... etc ...
      provider.addCompletion(new BasicCompletion(provider, "transient"));
      provider.addCompletion(new BasicCompletion(provider, "try"));
      provider.addCompletion(new BasicCompletion(provider, "void"));
      provider.addCompletion(new BasicCompletion(provider, "volatile"));
      provider.addCompletion(new BasicCompletion(provider, "while"));

      // Add a couple of "shorthand" completions. These completions don't
      // require the input text to be the same thing as the replacement text.
      provider.addCompletion(new ShorthandCompletion(provider, "sysout",
            "System.out.println(", "System.out.println("));
      provider.addCompletion(new ShorthandCompletion(provider, "syserr",
            "System.err.println(", "System.err.println("));

      return provider;

   }

   public static void main(String[] args) {
      // Instantiate GUI on the EDT.
      SwingUtilities.invokeLater(() -> {
        try {
           String laf = UIManager.getSystemLookAndFeelClassName();
           UIManager.setLookAndFeel(laf);
        } catch (Exception e) { /* Never happens */ }
        new AutoCompleteDemo().setVisible(true);
      });
   }

}
```
# Sister Projects

* [RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea) provides syntax highlighting, code folding, and many other features out-of-the-box.
* [RSTALanguageSupport](https://github.com/bobbylight/RSTALanguageSupport) - Code completion for RSTA for the following languages: Java, JavaScript, HTML, PHP, JSP, Perl, C, Unix Shell.  Built on both RSTA and AutoComplete.
* [SpellChecker](https://github.com/bobbylight/SpellChecker) - Adds squiggle-underline spell checking to RSyntaxTextArea.
* [RSTAUI](https://github.com/bobbylight/RSTAUI) - Common dialogs needed by text editing applications: Find, Replace, Go to Line, File Properties.

# Getting Help

* Add an [issue on GitHub](https://github.com/bobbylight/AutoComplete/issues)
