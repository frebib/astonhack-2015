package com.dungeoncrawler.generator.search;

import java.util.Collection;
import java.util.Iterator;

// TODO: NXT seems to only have java.util.Vector, not Collection
public abstract class GenericCollection<A extends Collection<B>, B> implements Iterable<B>
{
	protected A collection;

	public GenericCollection(A collection)
	{
		this.collection = collection;
	}

	public abstract B pop();

	public abstract void push(B item);

	public String toString()
	{
		return collection.toString();
	}

	public boolean contains(B item)
	{
		return collection.contains(item);
	}

	public boolean isEmpty()
	{
		return collection.isEmpty();
	}

	public int size()
	{
		return collection.size();
	}

	public void clear()
	{
		collection.clear();
	}

	public boolean remove(B item)
	{
		return collection.remove(item);
	}

	@Override
	public Iterator<B> iterator()
	{
		return collection.iterator();
	}
}

