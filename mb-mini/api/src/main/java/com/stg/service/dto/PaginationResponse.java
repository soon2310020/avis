package com.stg.service.dto;

import com.stg.errors.PaginationException;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

public class PaginationResponse<T> {
    private List<T> data;
    private long totalItem;
    private int page;
    private int pageSize;
    private int totalPage;

    public PaginationResponse() {
    }

    public PaginationResponse(Pageable pageable) {
        this(pageable.getPageNumber(), pageable.getPageSize());
    }

    public PaginationResponse(Pageable pageable, boolean createDefaultList) {
        this(pageable.getPageNumber(), pageable.getPageSize(), createDefaultList);
    }

    public PaginationResponse(int page, int pageSize) {
        this(page, pageSize, false);
    }

    public PaginationResponse(int page, int pageSize, boolean createDefaultList) {
        page++;

        if (page < 1) {
            throw new PaginationException("Page number is not positive!");
        }
        if (pageSize < 1) {
            throw new PaginationException("Page size is not positive!");
        }

        this.page = page;
        this.pageSize = pageSize;
        this.data = createDefaultList ? new ArrayList<>() : this.data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public long getTotalItem() {
        return totalItem;
    }

    public void setTotalItem(long totalItem) {
        this.totalItem = totalItem;
        if (pageSize > 0 && totalItem > 0) {
            totalPage = (int) Math.ceil((float) totalItem / pageSize);
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        if (pageSize > 0 && totalItem > 0) {
            totalPage = (int) Math.ceil((float) totalItem / pageSize);
        }
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

}
