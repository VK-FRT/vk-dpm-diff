package fi.vm.dpm.diff.cli

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach

open class DpmDiffCli_TestBase {
    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: DiffCli

    @BeforeEach
    fun testBaseInit() {
        charset = StandardCharsets.UTF_8
        outCollector = PrintStreamCollector(charset)
        errCollector = PrintStreamCollector(charset)

        cli = DiffCli(
            outStream = outCollector.printStream(),
            errStream = errCollector.printStream(),
            charset = charset
        )
    }

    fun testBaseTeardown() {
    }

    protected fun executeCliAndExpectSuccess(args: Array<String>, verifyAction: (String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isBlank()

        verifyAction(result.outText)

        assertThat(result.status).isEqualTo(DPM_DIFF_CLI_SUCCESS)
    }

    private fun executeCli(args: Array<String>): ExecuteResult {
        outCollector.printStream().write("CLI args:\n ${args.joinToString(separator = "\n")}\n\n\n".toByteArray())

        val status = cli.execute(args)

        val result = ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )

        return result
    }

    private class PrintStreamCollector(val charset: Charset) {
        private val baos = ByteArrayOutputStream()
        private val ps = PrintStream(baos, true, charset.name())

        fun printStream(): PrintStream = ps

        fun grabText(): String {
            ps.close()
            return String(baos.toByteArray(), charset)
        }
    }

    private data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )
}
