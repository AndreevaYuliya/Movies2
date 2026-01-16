package com.movies2.movie;

import com.movies2.movie.dto.MovieListRequestDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MovieRepositoryImpl implements MovieRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<MovieEntity> findByFilter(MovieListRequestDto f, Pageable pageable) {
        var cb = em.getCriteriaBuilder();

        CriteriaQuery<MovieEntity> cq = cb.createQuery(MovieEntity.class);
        Root<MovieEntity> root = cq.from(MovieEntity.class);

        // ❗ правильне імʼя поля
        root.fetch("director", JoinType.LEFT);

        List<Predicate> p = new ArrayList<>();

        if (f.directorId() != null)
            p.add(cb.equal(root.get("director").get("id"), f.directorId()));

        if (f.yearFrom() != null)
            p.add(cb.greaterThanOrEqualTo(root.get("yearReleased"), f.yearFrom()));

        if (f.yearTo() != null)
            p.add(cb.lessThanOrEqualTo(root.get("yearReleased"), f.yearTo()));

        if (f.genreContains() != null && !f.genreContains().isBlank())
            p.add(cb.like(cb.lower(root.get("genres")), "%" + f.genreContains().toLowerCase() + "%"));

        if (f.title() != null && !f.title().isBlank())
            p.add(cb.like(cb.lower(root.get("title")), "%" + f.title().toLowerCase() + "%"));

        cq.where(p.toArray(Predicate[]::new));
        cq.orderBy(cb.asc(root.get("id")));

        var query = em.createQuery(cq);
        if (pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        List<MovieEntity> items = query.getResultList();

        // COUNT
        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<MovieEntity> countRoot = countQ.from(MovieEntity.class);
        List<Predicate> countPredicates = new ArrayList<>();

        if (f.directorId() != null)
            countPredicates.add(cb.equal(countRoot.get("director").get("id"), f.directorId()));

        if (f.yearFrom() != null)
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("yearReleased"), f.yearFrom()));

        if (f.yearTo() != null)
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("yearReleased"), f.yearTo()));

        if (f.genreContains() != null && !f.genreContains().isBlank())
            countPredicates.add(cb.like(cb.lower(countRoot.get("genres")), "%" + f.genreContains().toLowerCase() + "%"));

        if (f.title() != null && !f.title().isBlank())
            countPredicates.add(cb.like(cb.lower(countRoot.get("title")), "%" + f.title().toLowerCase() + "%"));

        countQ.select(cb.count(countRoot));
        countQ.where(countPredicates.toArray(Predicate[]::new));
        Long total = em.createQuery(countQ).getSingleResult();

        return new PageImpl<>(items, pageable, total);
    }
}
