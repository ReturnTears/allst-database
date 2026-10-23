package com.allst.boot.guide.repository;

import com.allst.boot.guide.entity.Book;
import com.allst.boot.guide.entity.Product;
import com.allst.boot.guide.repository.support.GuideBaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends GuideBaseRepository<Product, Long> {

    List<Product> findByNameContaining(String name);

    @Query("select b from Book b where b.isbn = :isbn")
    Optional<Book> findBookByIsbn(@Param("isbn") String isbn);
}
