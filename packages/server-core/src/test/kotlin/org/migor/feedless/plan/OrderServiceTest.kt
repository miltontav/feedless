package org.migor.feedless.plan

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.repository.any2
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserDAO
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.data.domain.Pageable
import java.net.MalformedURLException

class OrderServiceTest {

  private lateinit var orderService: OrderService
  private lateinit var  orderDAO: OrderDAO
  private lateinit var  userDAO: UserDAO
  private lateinit var  sessionService: SessionService
  private lateinit var  productService: ProductService

  @BeforeEach
  fun setUp() {
    orderDAO = mock(OrderDAO::class.java)
    userDAO = mock(UserDAO::class.java)
    productService = mock(ProductService::class.java)
    sessionService = mock(SessionService::class.java)

    orderService = OrderService(
      orderDAO,
      userDAO,
      sessionService,
      productService
    )
  }

  @Test
  @Disabled
  fun `orders can be created`() {
    verify(orderDAO).save(any2())
  }

  @Test
  @Disabled
  fun `orders can be listed`() {
    verify(orderDAO).findAll(any(Pageable::class.java))
  }

  @Test
  @Disabled
  fun `orders can be updated by admins`() {
    verify(orderDAO).save(any2())
  }

  @Test
  @Disabled
  fun `orders cannot be updated by non-admins`() {
    assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy {
      runTest {
//        httpService.httpGet("gemma", 200)
      }
    }
  }

  @Test
  @Disabled
  fun `orders can be cancelled by owners`() {

  }

}
