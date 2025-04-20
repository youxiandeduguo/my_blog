package com.it.service;

import com.it.pojo.Article;
import com.it.pojo.PageBean;
import jakarta.validation.constraints.NotNull;

public interface ArticleService {
    void add(Article article);

    PageBean<Article> list(Integer pageNum, Integer pageSize, Integer categoryId, String state);

    void update(Article article);

    Article findById(Integer id);

    void delete(@NotNull Integer id);
}
