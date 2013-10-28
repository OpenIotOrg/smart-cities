package edu.kit.cm.ivu.smartmeetings.ui;

import java.util.ArrayList;
import java.util.List;

import edu.kit.cm.ivu.smartmeetings.logic.interfaces.Worker;
import edu.kit.cm.ivu.smartmeetings.ui.util.DepthItem;

/**
 * Abstract implementation of a {@link Worker} that retrieves a tree hierarchy
 * by calling {@link #getTopElements()} and {@link #getChildren(T)} and flattens
 * the tree to a list of {@link DepthItem}s.
 * 
 * @author Kirill Rakhman
 */
public abstract class ListFlatteningWorker<I, T> implements
		Worker<I, List<DepthItem<T>>> {

	@Override
	final public List<DepthItem<T>> doWork(final I... input) {

		final ArrayList<DepthItem<T>> list = new ArrayList<DepthItem<T>>();
		getList(list, null, 0);
		return list;
	}

	private void getList(final List<DepthItem<T>> list, final T node,
			final int depth) {

		List<T> currentLevel;
		if (depth == 0) {
			currentLevel = getTopElements();
		} else {
			currentLevel = getChildren(node);
		}

		if (currentLevel != null && !currentLevel.isEmpty()) {
			for (final T iRoomProperty : currentLevel) {
				list.add(new DepthItem<T>(iRoomProperty, depth));
				getList(list, iRoomProperty, depth + 1);
			}
		}
	}

	protected abstract List<T> getTopElements();

	protected abstract List<T> getChildren(T node);
}
