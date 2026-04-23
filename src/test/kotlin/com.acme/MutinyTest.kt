package com.acme

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

class MutinyTest {

    val PAGE_SIZE = 50
    val pages = listOf(42, 0, 0, 0)

    @Test
    fun `test mutiny generator with whilst`() {
        val l1 = Multi.createBy().repeating().uni(
            { AtomicInteger(0) },
            { counter ->
                val n = pages[counter.incrementAndGet() - 1]
                Uni.createFrom().item(n).invoke { _ -> println("uni called with number of items: $n") }
            })
            .whilst { n ->
                (n >= PAGE_SIZE).also { println("proceed condition is: $it ($n >= $PAGE_SIZE)"); it }
            }
            .collect().asList() // fix for the bug > .memoize().indefinitely()

        val l1_dep = l1.map { it.sum() }
        val l1_result = Uni.combine().all().unis(l1, l1_dep).with { a, b -> a.sum() - b }.await().indefinitely()

        println(l1_result)
    }

    @Test
    fun `test mutiny generator with until`() {
        val l2 = Multi.createBy().repeating().uni(
            { AtomicInteger(0) },
            { counter ->
                val n = pages[counter.incrementAndGet() - 1]
                Uni.createFrom().item(n).invoke { _ -> println("uni called with number of items: $n") }
            })
            .until { n ->
                (n == 0).also { println("stop condition is: $it ($n == 0)") }
            }
            .collect().asList()

        val l2_dep = l2.map { it.sum() }
        val l2_result = Uni.combine().all().unis(l2, l2_dep).with { a, b -> a.sum() - b }.await().indefinitely()

        println(l2_result)
    }
}
