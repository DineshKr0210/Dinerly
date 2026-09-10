package com.restaurant.waitlist.backend.repository;

import com.restaurant.waitlist.backend.entity.Redemption;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RedemptionRepositoryImpl implements RedemptionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Redemption> findFiltered(Long restaurantId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Redemption> cq = cb.createQuery(Redemption.class);
        Root<Redemption> root = cq.from(Redemption.class);

        List<Predicate> preds = new ArrayList<>();
        if (restaurantId != null) {
            preds.add(cb.equal(root.get("restaurantId"), restaurantId));
        }
        if (from != null) {
            preds.add(cb.greaterThanOrEqualTo(root.get("redeemedAt"), from));
        }
        if (to != null) {
            preds.add(cb.lessThanOrEqualTo(root.get("redeemedAt"), to));
        }

        cq.where(preds.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("redeemedAt")));

        TypedQuery<Redemption> q = em.createQuery(cq);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());
        List<Redemption> results = q.getResultList();

        // count
        CriteriaQuery<Long> countQ = cb.createQuery(Long.class);
        Root<Redemption> countRoot = countQ.from(Redemption.class);
        countQ.select(cb.count(countRoot));
        List<Predicate> countPreds = new ArrayList<>();
        if (restaurantId != null) {
            countPreds.add(cb.equal(countRoot.get("restaurantId"), restaurantId));
        }
        if (from != null) {
            countPreds.add(cb.greaterThanOrEqualTo(countRoot.get("redeemedAt"), from));
        }
        if (to != null) {
            countPreds.add(cb.lessThanOrEqualTo(countRoot.get("redeemedAt"), to));
        }
        countQ.where(countPreds.toArray(new Predicate[0]));
        Long total = em.createQuery(countQ).getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
