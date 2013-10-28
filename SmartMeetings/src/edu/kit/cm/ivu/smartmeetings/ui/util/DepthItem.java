package edu.kit.cm.ivu.smartmeetings.ui.util;

/**
 * This represents an item with an associated depth.
 * 
 * @author michael
 */
public class DepthItem<T> {
	private final T item;
	private final int depth;

	/**
	 * Creates a new depth item
	 * 
	 * @param item
	 *            The item this relates to
	 * @param depth
	 *            The depth the item has.
	 */
	public DepthItem(final T item, final int depth) {
		this.item = item;
		this.depth = depth;
	}

	/**
	 * Gets the depth of the item
	 * 
	 * @return The depth, 0 indicating top level.
	 */
	public int getDepth() {
		return this.depth;
	}

	/**
	 * Gets the item contained in this package.
	 * 
	 * @return The item.
	 */
	public T getItem() {
		return this.item;
	}
}
