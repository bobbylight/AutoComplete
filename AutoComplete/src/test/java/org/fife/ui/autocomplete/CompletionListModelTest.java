/*
 * This library is distributed under a modified BSD license.  See the included
 * LICENSE.md file for details.
 */
package org.fife.ui.autocomplete;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(SwingRunnerExtension.class)
class CompletionListModelTest {


	private static final class RecordingListener implements ListDataListener {

		private final List<ListDataEvent> added = new ArrayList<>();
		private final List<ListDataEvent> removed = new ArrayList<>();

		@Override
		public void intervalAdded(ListDataEvent e) {
			added.add(e);
		}

		@Override
		public void intervalRemoved(ListDataEvent e) {
			removed.add(e);
		}

		@Override
		public void contentsChanged(ListDataEvent e) {
			// Not used by CompletionListModel.
		}
	}


	@Test
	void getSize_emptyByDefault() {
		CompletionListModel model = new CompletionListModel();
		Assertions.assertEquals(0, model.getSize());
	}


	@Test
	void setContents_populatesModel() {
		CompletionListModel model = new CompletionListModel();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion c1 = new BasicCompletion(provider, "a");
		BasicCompletion c2 = new BasicCompletion(provider, "b");

		model.setContents(List.of(c1, c2));

		Assertions.assertEquals(2, model.getSize());
		Assertions.assertSame(c1, model.getElementAt(0));
		Assertions.assertSame(c2, model.getElementAt(1));
	}


	@Test
	void setContents_firesIntervalAdded() {
		CompletionListModel model = new CompletionListModel();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		RecordingListener listener = new RecordingListener();
		model.addListDataListener(listener);

		model.setContents(List.of(
			new BasicCompletion(provider, "a"),
			new BasicCompletion(provider, "b"),
			new BasicCompletion(provider, "c")));

		Assertions.assertEquals(1, listener.added.size());
		Assertions.assertEquals(0, listener.removed.size());
		ListDataEvent event = listener.added.get(0);
		Assertions.assertEquals(0, event.getIndex0());
		Assertions.assertEquals(2, event.getIndex1());
	}


	@Test
	void setContents_emptyCollection_doesNotFireIntervalAdded() {
		CompletionListModel model = new CompletionListModel();
		RecordingListener listener = new RecordingListener();
		model.addListDataListener(listener);

		model.setContents(List.of());

		Assertions.assertTrue(listener.added.isEmpty());
		Assertions.assertEquals(0, model.getSize());
	}


	@Test
	void setContents_replacesPreviousContents() {
		CompletionListModel model = new CompletionListModel();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		BasicCompletion c1 = new BasicCompletion(provider, "a");
		BasicCompletion c2 = new BasicCompletion(provider, "b");

		model.setContents(List.of(c1));
		model.setContents(List.of(c2));

		Assertions.assertEquals(1, model.getSize());
		Assertions.assertSame(c2, model.getElementAt(0));
	}


	@Test
	void setContents_replacingNonEmptyContents_firesRemovedThenAdded() {
		CompletionListModel model = new CompletionListModel();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		model.setContents(List.of(
			new BasicCompletion(provider, "a"),
			new BasicCompletion(provider, "b")));

		RecordingListener listener = new RecordingListener();
		model.addListDataListener(listener);

		model.setContents(List.of(new BasicCompletion(provider, "c")));

		Assertions.assertEquals(1, listener.removed.size());
		ListDataEvent removedEvent = listener.removed.get(0);
		Assertions.assertEquals(0, removedEvent.getIndex0());
		Assertions.assertEquals(1, removedEvent.getIndex1());

		Assertions.assertEquals(1, listener.added.size());
		ListDataEvent addedEvent = listener.added.get(0);
		Assertions.assertEquals(0, addedEvent.getIndex0());
		Assertions.assertEquals(0, addedEvent.getIndex1());
	}


	@Test
	void clear_onEmptyModel_doesNotFireIntervalRemoved() {
		CompletionListModel model = new CompletionListModel();
		RecordingListener listener = new RecordingListener();
		model.addListDataListener(listener);

		model.clear();

		Assertions.assertTrue(listener.removed.isEmpty());
	}


	@Test
	void clear_onNonEmptyModel_firesIntervalRemovedAndEmptiesModel() {
		CompletionListModel model = new CompletionListModel();
		DefaultCompletionProvider provider = new DefaultCompletionProvider();
		model.setContents(List.of(
			new BasicCompletion(provider, "a"),
			new BasicCompletion(provider, "b"),
			new BasicCompletion(provider, "c")));

		RecordingListener listener = new RecordingListener();
		model.addListDataListener(listener);

		model.clear();

		Assertions.assertEquals(0, model.getSize());
		Assertions.assertEquals(1, listener.removed.size());
		ListDataEvent event = listener.removed.get(0);
		Assertions.assertEquals(0, event.getIndex0());
		Assertions.assertEquals(2, event.getIndex1());
	}


}
