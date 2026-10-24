package com.allst.boot.common;

import java.util.List;

/**
 * 各领域的分页结果共用这一结构。GraphQL 没有泛型，schema 里仍然按 ProductPage、OrderPage 分开声明。
 */
public class Slice<T> {

    private final List<T> content;
    private final int total;
    private final int page;
    private final int size;
    private final int totalPages;

    public Slice(List<T> content, int total, int page, int size, int totalPages) {
        this.content = content;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = totalPages;
    }

    public static <T> Slice<T> from(org.springframework.data.domain.Page<?> source, List<T> content) {
        return new Slice<T>(content, (int) source.getTotalElements(), source.getNumber(), source.getSize(), source.getTotalPages());
    }

    public List<T> getContent() {
        return content;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
