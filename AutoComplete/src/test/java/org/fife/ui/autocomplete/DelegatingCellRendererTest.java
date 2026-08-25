/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class DelegatingCellRendererTest {


	@Test
	void getSetFallbackCellRenderer_roundTrips() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		Assertions.assertNull(renderer.getFallbackCellRenderer());

		DefaultListCellRenderer fallback = new DefaultListCellRenderer();
		renderer.setFallbackCellRenderer(fallback);
		Assertions.assertSame(fallback, renderer.getFallbackCellRenderer());
	}


	@Test
	void getListCellRendererComponent_providerHasRenderer_delegatesToProviderRenderer() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();

		DefaultListCellRenderer providerRenderer = new DefaultListCellRenderer();
		provider.setListCellRenderer(providerRenderer);

		BasicCompletion completion = new BasicCompletion(provider, "foo");
		JList<Object> list = new JList<>();

		Component result = renderer.getListCellRendererComponent(list, completion, 0, false, false);

		Assertions.assertSame(providerRenderer, result);
	}


	@Test
	void getListCellRendererComponent_noProviderRendererOrFallback_usesSuperDefault() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		JList<Object> list = new JList<>();

		Component result = renderer.getListCellRendererComponent(list, completion, 0, false, false);

		Assertions.assertSame(renderer, result);
	}


	@Test
	void getListCellRendererComponent_noProviderRenderer_withFallback_usesFallback() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		DefaultListCellRenderer fallback = new DefaultListCellRenderer();
		renderer.setFallbackCellRenderer(fallback);

		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion completion = new BasicCompletion(provider, "foo");
		JList<Object> list = new JList<>();

		Component result = renderer.getListCellRendererComponent(list, completion, 0, false, false);

		Assertions.assertSame(fallback, result);
	}


	@Test
	void updateUI_fallbackIsJComponentAndNotThis_delegatesUpdateUI() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		boolean[] updateUiCalled = {false};
		DefaultListCellRenderer fallback = new DefaultListCellRenderer() {
			@Override
			public void updateUI() {
				super.updateUI();
				updateUiCalled[0] = true;
			}
		};
		renderer.setFallbackCellRenderer(fallback);

		renderer.updateUI();

		Assertions.assertTrue(updateUiCalled[0]);
	}


	@Test
	void updateUI_fallbackIsThis_doesNotRecurse() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		renderer.setFallbackCellRenderer(renderer);
		Assertions.assertDoesNotThrow(renderer::updateUI);
	}


	@Test
	void updateUI_fallbackIsNotJComponent_doesNotThrow() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		ListCellRenderer<Object> nonComponentFallback =
			(list, value, index, isSelected, cellHasFocus) -> null;
		renderer.setFallbackCellRenderer(nonComponentFallback);
		Assertions.assertDoesNotThrow(renderer::updateUI);
	}


	@Test
	void updateUI_noFallback_doesNotThrow() {
		DelegatingCellRenderer renderer = new DelegatingCellRenderer();
		Assertions.assertDoesNotThrow(renderer::updateUI);
	}


}
