AutoComplete Readme
-------------------
Please contact me if you are using AutoComplete in your project!  I like to
know when people are finding it useful.  Please send mail to:
robert -at- fifesoft dot com.


* About AutoComplete

  AutoComplete is a Swing library that facilitates adding auto-completion
  (aka "code completion, "Intellisense") to any JTextComponent.  Special
  integration is added for RSyntaxTextArea (a programmer's text editor, see
  http://fifesoft.com/rsyntaxtextarea/ for more information), since this feature
  is commonly needed when editing source code.

* Example Usage

  See the AutoComplete example(s) on the RSyntaxTextArea examples page here:
  
     http://fifesoft.com/rsyntaxtextarea/examples/index.php
  
  They provide a good example of basic usage of the library, showing how to
  auto-complete a simple, fixed set of words (function names).
  
  Also see the AutoCompleteDemo project, which lives here in SVN:
  
     http://svn.fifesoft.com/svn/RSyntaxTextArea/

  It provides an example of loading completions from an XML file.  It
  demonstrates a code editor with completion support for the C Standard library,
  and demos the parameter assistance feature.

* Compiling

  If you wish to compile AutoComplete from source, the easiest way to do so
  is via the included Ant build script.  The default target builds the jar.
  
  This project depends on its sister RSyntaxTextArea project.  It is recommended
  that you check the two projects out side by side.  Then, to build:
  
     cd RSyntaxTextArea
     ant
     cd ../AutoComplete
     ant

* License

  AutoComplete is licensed under a modified BSD license.  Please see the
  included AutoComplete.License.txt file.

* Feedback

  I hope you find AutoComplete useful.  Bug reports, feature requests, and
  just general questions are always welcome.  Ways you can submit feedback:

    * http://forum.fifesoft.com (preferred)
         Has a forum for AutoComplete and related projects, where you can
         ask questions and get feedback quickly.

    * https://github.com/bobbylight/AutoComplete
         Here you can submit bug reports or enhancement requests, peruse the
         Wiki, etc.

* Other Links

    * http://fifesoft.com/autocomplete
         Project home page, which contains general information and example
         source code.

    * http://fifesoft.com/rsyntaxtextarea
         The source code editor you're (probably) already using if you're
         using this AutoComplete library.

    * http://javadoc.fifesoft.com/rsyntaxtextarea/
    * http://javadoc.fifesoft.com/autocomplete/
         API documentation for the package.  Note that this *will* change as
         the library matures.

* Thanks

  The left and right arrow icons in the Help "tooltip" window are from the
  "Silk" icon set, under the Creative Commons 3.0 License.  This is a wonderful
  icon set, found here:
  
     http://famfamfam.com/lab/icons/silk/

  The "bullet_black.png" icon is an edited version of "bullet_black.png" in the
  Silk icon set noted above.  It can be considered to be distributed under the
  same Creative Commons 3.0 License.
  
  "osx_sizegrip.png" is a reproduction of the size grip used on native OS X
  windows.  It is distributed under the same modified BSD license as the meat
  of the AutoComplete library.

* Translators

  Arabic:                 Mawaheb, Linostar
  Chinese:                Terrance, peter_barnes, Sunquan, sonyichi, zvest
  Chinese (Traditional):  kin Por Fok, liou xiao
  Dutch:                  Roel, Sebastiaan, lovepuppy
  French:                 Pat, PivWan
  German:                 Domenic, bikerpete
  Hungarian:              Zityi, flatron
  Indonesian:             azis, Sonny
  Italian:                Luca, stepagweb
  Japanese:               Josh, izumi, tomoM
  Korean:                 Changkyoon, sbrownii
  Polish:                 Chris, Maciej Mrug
  Portuguese (Brazil):    Pat, Marcos Parmeggiani, Leandro
  Russian:                Nadiya, Vladimir
  Spanish:                Leonardo, phrodo, daloporhecho
  Turkish:                Cahit, Burak
  