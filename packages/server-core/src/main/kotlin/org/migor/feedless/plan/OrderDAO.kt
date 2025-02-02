package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface OrderDAO : JpaRepository<OrderEntity, UUID> {
  fun findAllByUserId(userId: UUID, pageable: Pageable): List<OrderEntity>

  @Query(
    """SELECT DISTINCT m FROM OrderEntity m
    LEFT JOIN FETCH m.product
    WHERE m.id = :id"""
  )
  fun findByIdWithProduct(@Param("id") orderId: UUID): OrderEntity?
  fun findByStripeSubscriptionId(subscription: String?): OrderEntity?
}
