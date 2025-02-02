package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.util.*

@Entity
@Table(
  name = "t_invoice",
)
open class InvoiceEntity : EntityWithUUID() {

  @Column(name = "amount_paid", nullable = false)
  open var amountPaid: Long? = null

  @Column(name = "amount_remaining", nullable = false)
  open var amountRemaining: Long? = null

  @Column(name = "pdf_url", nullable = false)
  open lateinit var pdfUrl: String

  @Column(name = "invoice_id", nullable = false)
  open lateinit var invoiceId: String

  @Column(name = "customer_email")
  open var customerEmail: String? = null

  @Column(name = StandardJpaFields.order_id, nullable = false)
  open var orderId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.order_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_invoice__to__order")
  )
  open var order: OrderEntity? = null
}
