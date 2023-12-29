package saleson.mms.data;

import org.springframework.data.domain.Page;

import lombok.Getter;

@Getter
public class DataPagination {

	private int current;
	private int startPage;
	private int endPage;

	private int totalPages;

	public DataPagination(Page<?> page) {

		double size = 10;
		double halfFloor = Math.floor(size / 2);
		double halfSizeFloor = halfFloor - (1 - (halfFloor % 1)) % 1;

		totalPages = page.getTotalPages();

		current = page.getNumber() + 1;

		double start = current < halfSizeFloor + 1 ? 1 : current - halfSizeFloor;
		start = current > totalPages - halfSizeFloor ? totalPages - size + 1 : start;

		double end = start + size - 1;
		end = end > totalPages ? totalPages : end;

		start = totalPages < size ? 1 : start;
		end = totalPages < size ? totalPages : end;

		this.startPage = (int) start;
		this.endPage = (int) end;

	}
}
