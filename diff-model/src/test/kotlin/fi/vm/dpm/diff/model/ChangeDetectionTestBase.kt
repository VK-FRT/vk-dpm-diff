package fi.vm.dpm.diff.model

import org.assertj.core.api.Assertions.assertThat

internal open class ChangeDetectionTestBase {

    protected val scopeKeySegment = KeySegmentField(
        fieldName = "scopeKeySegment",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = null
    )

    protected val scopeKeySegment2 = KeySegmentField(
        fieldName = "scopeKeySegment2",
        segmentKind = KeySegmentKind.SCOPE_SEGMENT,
        segmentFallback = null
    )

    protected val primeKeySegment = KeySegmentField(
        fieldName = "primeKeySegment",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = null
    )

    protected val primeKeySegment2 = KeySegmentField(
        fieldName = "primeKeySegment2",
        segmentKind = KeySegmentKind.PRIME_SEGMENT,
        segmentFallback = null
    )

    protected val subKeySegment = KeySegmentField(
        fieldName = "subKeySegment",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    protected val subKeySegment2 = KeySegmentField(
        fieldName = "subKeySegment2",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    protected val atom = AtomField(
        fieldName = "Atom"
    )

    protected val changeKind = ChangeKindField()

    protected val note = NoteField()

    protected fun executeChangeDetectionTest(
        baselineRecordsValues: String?,
        currentRecordsValues: String?,
        expectedResultsValues: String?,
        sectionDescriptor: SectionDescriptor,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>),
        changeResultsMapper: (List<ChangeRecord>) -> (List<String>)
    ) {
        val changes = executeResolveChanges(
            sectionDescriptor = sectionDescriptor,
            baselineRecordsFieldValues = buildRecordsFieldValues(baselineRecordsValues, recordValueMapper),
            currentRecordsFieldValues = buildRecordsFieldValues(currentRecordsValues, recordValueMapper)
        )

        val changesResults = changeResultsMapper(changes)

        if (expectedResultsValues == null) {
            assertThat(changesResults).isEmpty()
        } else {
            val expectedResults = expectedResultsValues.splitExpectedResultsToList()
            assertThat(changesResults).containsExactly(*expectedResults.toTypedArray())
        }
    }

    protected fun List<ChangeRecord>.toKeyAndChangeKindList(): List<String> {
        return map { changeRecord ->
            "${CorrelationKey.fullKey(changeRecord.fields).keyValue()} ${changeRecord.fields[changeKind]}"
        }
    }

    private fun buildRecordsFieldValues(
        recordsValues: String?,
        recordValueMapper: (List<String?>) -> (Map<Field, String?>)
    ): List<Map<Field, String?>> {
        if (recordsValues == null) return emptyList()

        return recordsValues
            .splitRecordsValuesToNestedLists()
            .map { recordValues -> recordValueMapper(recordValues) }
    }

    private fun executeResolveChanges(
        sectionDescriptor: SectionDescriptor,
        baselineRecordsFieldValues: List<Map<Field, String?>>,
        currentRecordsFieldValues: List<Map<Field, String?>>
    ): List<ChangeRecord> {

        val baselineSourceRecords = createSourceRecords(
            sectionDescriptor,
            SourceKind.BASELINE,
            baselineRecordsFieldValues
        )

        val currentSourceRecords = createSourceRecords(
            sectionDescriptor,
            SourceKind.CURRENT,
            currentRecordsFieldValues
        )

        val changes = ChangeRecord.resolveChanges(
            sectionDescriptor = sectionDescriptor,
            baselineSourceRecords = baselineSourceRecords,
            currentSourceRecords = currentSourceRecords
        )

        return changes
    }

    private fun createSourceRecords(
        sectionDescriptor: SectionDescriptor,
        sourceKind: SourceKind,
        recordsFieldValues: List<Map<Field, String?>>
    ): List<SourceRecord> {
        val sourceRecords = recordsFieldValues.map { recordFieldValues ->

            val totalFieldValues = sectionDescriptor.sectionFields.map {
                it to null
            }.toMap() + recordFieldValues

            SourceRecord(
                sectionDescriptor,
                sourceKind,
                totalFieldValues
            )
        }

        return sourceRecords
    }

    private fun String.splitRecordsValuesToNestedLists(): List<List<String>> {
        val recordsList = split("|").map { it.trim() }
        return recordsList.map { record -> record.split(" ").map { it.trim() } }
    }

    private fun String.splitExpectedResultsToList(): List<String> {
        return split("|").map { it.trim() }
    }
}