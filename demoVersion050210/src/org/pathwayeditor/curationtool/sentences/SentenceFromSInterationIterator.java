package org.pathwayeditor.curationtool.sentences;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.pathwayeditor.notations.annotator.ndom.ISInteractionArc;
import org.pathwayeditor.notations.annotator.ndom.ISentence;

public class SentenceFromSInterationIterator implements Iterator<ISentence> {
	private final Deque<ISentence> stack;
	private final Iterator<ISInteractionArc> iter;
	
	public SentenceFromSInterationIterator(Iterator<ISInteractionArc> iter){
		this.stack = new LinkedList<ISentence>();
		this.iter = iter;
		if(iter.hasNext()){
			loadNextSentences();
		}
	}
	
	private void loadNextSentences() {
		ISInteractionArc sarc = iter.next();
		Iterator<ISentence> sIter = sarc.getSentenceIterator();
		while(sIter.hasNext()){
			this.stack.push(sIter.next());
		}
	}

	@Override
	public boolean hasNext() {
		if(this.stack.isEmpty() && this.iter.hasNext()){
			loadNextSentences();
		}
		return !this.stack.isEmpty();
	}

	@Override
	public ISentence next() {
		if(this.stack.isEmpty() && this.iter.hasNext()){
			loadNextSentences();
		}
		if(this.stack.isEmpty()){
			throw new NoSuchElementException();
		}
		return this.stack.pop();
	}

	@Override
	public void remove() {
		throw new IllegalArgumentException("Removal not supported by this iterator");
	}

}
