package com.hrms.company_management.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hrms.company_management.entity.Notice;
import com.hrms.company_management.entity.NoticeStatus;

@Repository
public interface NoticeRepo extends JpaRepository<Notice, Long> {

    /** Visibility rule shared by the two employee queries. Null status / publishAt are legacy rows that count as published. */
    String VISIBLE = "(n.status in :liveStatuses or n.status is null)"
            + " and (n.publishAt is null or n.publishAt <= :now)"
            + " and (n.expiresAt is null or n.expiresAt > :now)";

    List<Notice> findAllByOrderByIdDesc();

    @Query("select n from Notice n where " + VISIBLE + " order by n.publishAt desc nulls last, n.id desc")
    List<Notice> findVisible(@Param("liveStatuses") Collection<NoticeStatus> liveStatuses, @Param("now") Instant now);

    @Query("select n from Notice n where n.id = :id and " + VISIBLE)
    Optional<Notice> findVisibleById(@Param("id") Long id,
                                     @Param("liveStatuses") Collection<NoticeStatus> liveStatuses,
                                     @Param("now") Instant now);

    /** SCHEDULED -> PUBLISHED once publishAt has passed. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notice n set n.status = :published, n.updatedAt = :today"
            + " where n.status = :scheduled and n.publishAt <= :now")
    int publishDue(@Param("scheduled") NoticeStatus scheduled, @Param("published") NoticeStatus published,
                   @Param("now") Instant now, @Param("today") LocalDate today);

    /** PUBLISHED or SCHEDULED -> EXPIRED once expiresAt has passed. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Notice n set n.status = :expired, n.updatedAt = :today"
            + " where n.status in :liveStatuses and n.expiresAt is not null and n.expiresAt <= :now")
    int expireDue(@Param("liveStatuses") Collection<NoticeStatus> liveStatuses, @Param("expired") NoticeStatus expired,
                  @Param("now") Instant now, @Param("today") LocalDate today);
}
