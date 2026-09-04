package llm

import com.patrykdolata.lifeagent.llm.StubLlmClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StubLlmClientTest {

    private val client = StubLlmClient()

    @Test
    fun `should generate stub response`() {
        // when
        val response = client.generate("hello")

        assertTrue(response.contains("hello"))
    }
}
