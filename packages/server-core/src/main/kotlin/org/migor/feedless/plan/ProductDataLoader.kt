package org.migor.feedless.plan

import com.netflix.graphql.dgs.DgsDataLoader
import org.dataloader.BatchLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = "products")
class ProductDataLoader : BatchLoader<String, ProductEntity> {

//  @Autowired
//  lateinit var productDAO: ProductDAO

//  @Transactional(readOnly = true)
  override fun load(ids: MutableList<String>): CompletionStage<MutableList<ProductEntity>> {
    return CompletableFuture.supplyAsync {
//      productDAO.findAllById(ids.distinct().map { UUID.fromString(it) })
      mutableListOf()
    }
  }

}
