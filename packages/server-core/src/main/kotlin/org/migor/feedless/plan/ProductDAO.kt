package org.migor.feedless.plan

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.Vertical
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.plan} & ${AppLayer.repository}")
interface ProductDAO : JpaRepository<ProductEntity, UUID> {
  fun findByNameEqualsIgnoreCase(name: String): ProductEntity?
  fun findByPartOfAndBaseProductIsTrue(name: Vertical): ProductEntity?
  fun findAllByPartOfOrPartOfIsNullAndAvailableTrue(category: Vertical): List<ProductEntity>
  fun findAllByIdIn(ids: List<UUID>): List<ProductEntity>

  @Query(
    """SELECT DISTINCT p FROM ProductEntity p
    LEFT JOIN FETCH p.prices
    WHERE p.id = :id"""
  )
  fun findByIdWithPrices(@Param("id") productId: UUID): ProductEntity?
}
