package com.zeroturnaround.alvor.sqlparser.framework.priorityqueue;


public class BinaryHeap<E extends IPriorityQueueItem<E>> implements
		IPriorityQueue<E> {

	private int version = 0;
	@SuppressWarnings("unchecked")
	private E[] data = (E[]) new IPriorityQueueItem[1];
	private int size = 0;

	public int size() {
		return size;
	}

	private void siftUp(int x) {
		int p = parent(x);
		while (exists(p)) {
			if (data[p].compareTo(data[x]) <= 0) {
				break;
			}
			swap(p, x);
			x = p;
			p = parent(x);
		}
	}

	private void setData(int index, E item) {
		data[index] = item;
		item.setIndex(index);
	}
	
	private void swap(int p, int x) {
		E t = data[p];
		setData(p, data[x]);
		setData(x, t);
	}

	private void expandData() {
		@SuppressWarnings("unchecked")
		E[] newData = (E[]) new IPriorityQueueItem[data.length * 2];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	public void enqueue(E p) {
		if (size >= data.length) {
			expandData();
		}
		setData(size, p);
		size++;
		siftUp(size - 1);
		version++;
	}

	public E peek() {
		if (isEmpty()) {
			throw new IllegalArgumentException();
		}

		return data[0];
	}

	public E dequeue() {
		if (isEmpty()) {
			throw new IllegalArgumentException();
		}
		E result = data[0];
		setData(0, data[size - 1]);
		size--;
		data[size] = null;
		siftDown(0);
		version++;
		return result;
	}

	private void siftDown(int x) {
		E v = data[x];
		while (true) {
			int l = left(x);
			if (!exists(l)) {
				break;
			}
			int target = x;
			E targV = v;
			E dl = data[l];
			if (targV.compareTo(dl) > 0) {
				target = l;
				targV = dl;
			}
			int r = right(x);
			if (exists(r)) {
				if (targV.compareTo(data[r]) > 0) {
					target = r;
				}
			}
			if (x == target) {
				break;
			}
			swap(x, target);
			x = target;
		}
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public boolean remove(E item) {
		int index = item.getIndex();
		setData(index, data[size - 1]);
		size--;
		data[size] = null;
		siftDown(index);

		version++;
		return true;
	}

	@Override
	public void handleDecreasedKey(E item) {
		siftDown(item.getIndex());
	}

	private static int parent(int x) {
		return x == 0 ? -1 : (x - 1) / 2;
	}

	private static int left(int x) {
		return x * 2 + 1;
	}

	private static int right(int x) {
		return x * 2 + 2;
	}

	private boolean exists(int x) {
		return (x >= 0) && (x < size);
	}
	
}
