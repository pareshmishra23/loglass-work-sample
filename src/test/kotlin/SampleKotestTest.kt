import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

/**
 * Reference: https://kotest.io/
 */
class SampleKotestTest : StringSpec({
    "should be something when ~" {
        1 shouldBe 1
    }

    "~のとき、〇〇になること" {
        1 shouldBe 1
    }
})
