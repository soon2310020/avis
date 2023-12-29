package com.emoldino.framework.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.emoldino.framework.util.Closure1Param;
import com.emoldino.framework.util.ValueUtils;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
public class LargeListOut<T> {
	private List<T> content;

	public LargeListOut(Closure1Param<Pageable, Page<T>> get, int pageSize, Sort sort) {
		this.content = new LargeList<T>(get, pageSize, sort);
	}

	@Slf4j
	public static class LargeList<T> implements List<T> {
		private Closure1Param<Pageable, Page<T>> get;
		private int pageSize;
		private Sort sort;

		private Page<T> firstPage;

		public LargeList(Closure1Param<Pageable, Page<T>> get, int pageSize, Sort sort) {
			this.get = get;
			this.pageSize = pageSize;
			this.sort = sort;
		}

		private Page<T> getFirstPage() {
			if (firstPage == null) {
				firstPage = get.execute(PageRequest.of(0, pageSize, sort == null ? Sort.unsorted() : sort));
			}
			return firstPage;
		}

		@Override
		public int size() {
			log.info("LargeList.size()");
			return ValueUtils.toInteger(getFirstPage().getTotalElements(), 0);
		}

		@Override
		public boolean isEmpty() {
			log.info("LargeList.isEmpty()");
			return getFirstPage().isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			log.info("LargeList.contains(Object o)");
			return getFirstPage().getContent().contains(o);
		}

		@Override
		public Iterator<T> iterator() {
			log.info("LargeList.iterator()");
			return new LargeIterator<>(get, pageSize, sort);
		}

		@Override
		public Object[] toArray() {
			log.info("LargeList.toArray()");
			return getFirstPage().getContent().toArray();
		}

		@SuppressWarnings("hiding")
		@Override
		public <T> T[] toArray(T[] a) {
			log.info("LargeList.toArray(T[] a)");
			return getFirstPage().getContent().toArray(a);
		}

		@Override
		public boolean add(T e) {
			log.info("LargeList.add(T e)");
			return false;
		}

		@Override
		public boolean remove(Object o) {
			log.info("LargeList.remove(Object o)");
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			log.info("LargeList.containsAll(Collection<?> c)");
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			log.info("LargeList.addAll(Collection<? extends T> c)");
			return false;
		}

		@Override
		public boolean addAll(int index, Collection<? extends T> c) {
			log.info("LargeList.addAll(int index, Collection<? extends T> c)");
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			log.info("LargeList.removeAll(Collection<?> c)");
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			log.info("LargeList.retainAll(Collection<?> c)");
			return false;
		}

		@Override
		public void clear() {
			log.info("LargeList.clear()");
		}

		@Override
		public T get(int index) {
			log.info("LargeList.get(int index)");
			return null;
		}

		@Override
		public T set(int index, T element) {
			log.info("LargeList.set(int index, T element)");
			return null;
		}

		@Override
		public void add(int index, T element) {
			log.info("LargeList.add(int index, T element)");
		}

		@Override
		public T remove(int index) {
			log.info("LargeList.remove(int index)");
			return null;
		}

		@Override
		public int indexOf(Object o) {
			log.info("LargeList.indexOf(Object o)");
			return -1;
		}

		@Override
		public int lastIndexOf(Object o) {
			log.info("LargeList.lastIndexOf(o)");
			return -1;
		}

		@Override
		public ListIterator<T> listIterator() {
			log.info("LargeList.listIterator");
			return Collections.emptyListIterator();
		}

		@Override
		public ListIterator<T> listIterator(int index) {
			log.info("LargeList.listIterator(int index)");
			return Collections.emptyListIterator();
		}

		@Override
		public List<T> subList(int fromIndex, int toIndex) {
			log.info("LargeList.subList(int fromIndex, int toIndex)");
			return Collections.emptyList();
		}
	}

	public static class LargeIterator<T> implements Iterator<T> {
		private Closure1Param<Pageable, Page<T>> get;
		private int pageNo = -1;
		private int pageSize;
		private Sort sort;

		private Page<T> page;
		private List<T> content;
		private int itemIndex;

		public LargeIterator(Closure1Param<Pageable, Page<T>> closure, int pageSize, Sort sort) {
			this.get = closure;
			this.pageSize = pageSize;
			this.sort = sort;
		}

		@Override
		public boolean hasNext() {
			doPage();
			return page.getTotalElements() > itemIndex;
		}

		@Override
		public T next() {
			doPage();
			return content.get(itemIndex++ - pageNo * pageSize);
		}

		private void doPage() {
			if (page != null && (pageNo + 1) * pageSize > itemIndex) {
				return;
			}
			page = get.execute(PageRequest.of(++pageNo, pageSize, sort == null ? Sort.unsorted() : sort));
			content = page.getContent();
		}

	}

}
