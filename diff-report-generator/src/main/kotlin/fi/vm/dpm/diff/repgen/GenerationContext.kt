package fi.vm.dpm.diff.repgen

import fi.vm.dpm.diff.model.diagnostic.Diagnostic

data class GenerationContext(
    val baselineConnection: DbConnection,
    val actualConnection: DbConnection,
    val discriminationLangCodes: List<String>,
    val diagnostic: Diagnostic
)
