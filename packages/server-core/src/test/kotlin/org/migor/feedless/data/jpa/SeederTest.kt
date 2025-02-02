package org.migor.feedless.data.jpa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentDAO
import org.migor.feedless.feature.FeatureGroupDAO
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.feed.StandaloneFeedService
import org.migor.feedless.group.GroupDAO
import org.migor.feedless.group.GroupEntity
import org.migor.feedless.group.UserGroupAssignmentDAO
import org.migor.feedless.plan.PricedProductDAO
import org.migor.feedless.plan.ProductDAO
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.any2
import org.migor.feedless.repository.eq
import org.migor.feedless.secrets.UserSecretDAO
import org.migor.feedless.user.UserDAO
import org.migor.feedless.user.UserEntity
import org.mockito.Mockito
import org.mockito.Mockito.argThat
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.core.env.Environment
import java.util.*

class SeederTest {

  private lateinit var featureGroupDAO: FeatureGroupDAO
  private lateinit var featureService: FeatureService
  private lateinit var documentDAO: DocumentDAO
  private lateinit var environment: Environment
  private lateinit var propertyService: PropertyService
  private lateinit var productDAO: ProductDAO
  private lateinit var pricedProductDAO: PricedProductDAO
  private lateinit var userSecretDAO: UserSecretDAO
  private lateinit var repositoryDAO: RepositoryDAO
  private lateinit var standaloneFeedService: StandaloneFeedService
  private lateinit var userDAO: UserDAO
  private lateinit var groupDAO: GroupDAO
  private lateinit var userGroupAssignmentDAO: UserGroupAssignmentDAO
  private lateinit var seeder: Seeder

  @BeforeEach
  fun setUp() {
    featureGroupDAO = mock(FeatureGroupDAO::class.java)
    featureService = mock(FeatureService::class.java)
    documentDAO = mock(DocumentDAO::class.java)
    environment = mock(Environment::class.java)
    propertyService = mock(PropertyService::class.java)
    productDAO = mock(ProductDAO::class.java)
    pricedProductDAO = mock(PricedProductDAO::class.java)
    userSecretDAO = mock(UserSecretDAO::class.java)
    repositoryDAO = mock(RepositoryDAO::class.java)
    standaloneFeedService = mock(StandaloneFeedService::class.java)
    groupDAO = mock(GroupDAO::class.java)
    userGroupAssignmentDAO = mock(UserGroupAssignmentDAO::class.java)
    userDAO = mock(UserDAO::class.java)

    seeder = Seeder(
      featureGroupDAO,
      featureService,
      documentDAO,
      environment,
      propertyService,
      productDAO,
      pricedProductDAO,
      userSecretDAO,
      repositoryDAO,
      standaloneFeedService,
      userDAO,
      groupDAO,
      userGroupAssignmentDAO
    )

    `when`(propertyService.rootEmail).thenReturn("admin@foo")
    `when`(propertyService.anonymousEmail).thenReturn("anon@foo")
    `when`(propertyService.rootSecretKey).thenReturn("aSecretSecret")
    `when`(userDAO.saveAndFlush(any2())).thenAnswer{ it.arguments[0] }
    `when`(repositoryDAO.save(any2())).thenAnswer{ it.arguments[0] }
    `when`(featureGroupDAO.save(any2())).thenAnswer{ it.arguments[0] }
    `when`(standaloneFeedService.getRepoTitleForStandaloneFeedNotifications()).thenReturn("opsops")


    `when`(userDAO.save(any2())).thenAnswer { it.arguments[0] }
    `when`(groupDAO.save(any2())).thenAnswer { it.arguments[0] }
    `when`(userSecretDAO.save(any2())).thenAnswer { it.arguments[0] }
  }

  @Test
  fun `given root user does not exist, will seed one with key`() {
    `when`(userSecretDAO.existsByValueAndOwnerId(any2(), any2())).thenReturn(false)

    seeder.onInit()

    verify(userDAO, times(1)).save(argThat { it.admin })
  }

  @Test
  fun `base feature group is named feedless`() {
    assertThat(seeder.BASE_FEATURE_GROUP_ID).isEqualTo("feedless")
  }

  @Test
  fun `given root user exists, won't do anything`() {
    val root = mock(UserEntity::class.java)
    `when`(root.id).thenReturn(UUID.randomUUID())
    `when`(root.email).thenReturn("admin@foo")
    `when`(userDAO.findFirstByAdminIsTrue()).thenReturn(root)
    `when`(userDAO.findByEmail(eq("anon@foo"))).thenReturn(mock(UserEntity::class.java))
    `when`(userSecretDAO.existsByValueAndOwnerId(any2(), any2())).thenReturn(true)

    seeder.onInit()

    verify(userDAO, times(0)).save(argThat { !it.admin })
  }

  @Test
  fun `given admin group does not exist, will seed one`() {

    seeder.onInit()

    verify(groupDAO, times(1)).save(any2())
//    Mockito.verify(userGroupAssignmentDAO, times(1)).save(any2())
  }

  @Test
  fun `given admin group exists, won't do anything`() {
    val adminGroup = mock(GroupEntity::class.java)
    `when`(adminGroup.id).thenReturn(UUID.randomUUID())
    `when`(groupDAO.findByName(any2())).thenReturn(adminGroup)

    seeder.onInit()

    verify(groupDAO, times(0)).save(any2())
//    Mockito.verify(userGroupAssignmentDAO, times(0)).save(any2())
  }
}
