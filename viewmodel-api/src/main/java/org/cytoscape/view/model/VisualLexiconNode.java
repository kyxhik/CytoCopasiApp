package org.cytoscape.view.model;

import java.util.Collection;
import java.util.HashSet;

/**
 * A node in the visual property tree (lexicon).
 * <p>
 * Wrapping a {@linkplain VisualProperty} and holding parent-child relationships.
 * <p>
 * All data fields are immutable. 
 * 
 * @CyAPI.Final.Class
 */
public final class VisualLexiconNode {
	
	private final VisualProperty<?> vp;
	
	private final VisualLexiconNode parent;
	private final Collection<VisualLexiconNode> children;
	
	private boolean isDepend;
	
	/**
	 * Constructs this VisualLexiconNode.
	 * @param vp the {@link VisualProperty} to wrap.
	 * @param parent this VisualLexiconNodes parent VisualLexiconNode.
	 */
	public VisualLexiconNode(final VisualProperty<?> vp, final VisualLexiconNode parent) {
		if(vp == null)
			throw new NullPointerException("Visual Property cannot be null.");
		
		this.vp = vp;
		this.parent = parent;
		this.children = new HashSet<VisualLexiconNode>();
		this.isDepend = false;
		
		if(parent != null)
			parent.getChildren().add(this);
	}
	
	
	/**
	 * Sets the dependency of this VisualLexiconNode.
	 * @param depend True to make this VisualLexiconNode dependent on its parent, false otherwise.
	 */
	public void setDependency(boolean depend) {
		this.isDepend = depend;
	}
	
	/**
	 * Returns true if this VisualLexiconNode is dependent on its parent.
	 * @return true if this VisualLexiconNode is dependent on its parent.
	 */
	public boolean isDepend() {
		return isDepend;
	}
	
	/**
	 * Returns wrapped {@linkplain VisualProperty} object.
	 * 
	 * Since VisualProperty itself does not have any hierarchical structure, 
	 * such relationship is implemented by this wrapper.
	 * 
	 * @return wrapped {@linkplain VisualProperty} object.
	 */
	public VisualProperty<?> getVisualProperty() {
		return vp;
	}

	
	/**
	 * Get the parent of this VP node. The relationship is immutable, i.e.,
	 * cannot change parent/child relationship.
	 * 
	 * @return parent VisualProperty object.
	 */
	public VisualLexiconNode getParent() {
		return parent;
	}

	/**
	 * Returns collection of all children of this node.
	 * 
	 * @return collection of all children of this node.
	 */
	public Collection<VisualLexiconNode> getChildren() {
		return children;
	}

}
