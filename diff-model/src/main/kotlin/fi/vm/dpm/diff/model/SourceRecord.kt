package fi.vm.dpm.diff.model

import ext.kotlin.filterFieldType
import ext.kotlin.firstFieldOfType
import ext.kotlin.firstFieldOfTypeOrNull
import ext.kotlin.splitCamelCaseWords
import kotlin.reflect.KClass

data class SourceRecord(
    val fields: Map<Field, String?>,
    private val sectionOutline: SectionOutline,
    private val sourceKind: SourceKind
) {
    val fullKeyFieldKey: CorrelationKey by lazy {
        CorrelationKey.createCorrelationKey(CorrelationKeyKind.FULL_KEY_FIELD_CORRELATION_KEY, this)
    }

    val parentKeyFieldKey: CorrelationKey by lazy {
        CorrelationKey.createCorrelationKey(CorrelationKeyKind.PARENT_KEY_FIELD_CORRELATION_KEY, this)
    }

    val atomFieldKey: CorrelationKey by lazy {
        CorrelationKey.createCorrelationKey(CorrelationKeyKind.ATOM_FIELD_CORRELATION_KEY, this)
    }

    fun toAddedChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToAddedChange()
        changeFields.setChangeKind(ChangeKind.ADDED)
        changeFields.setNoteWithDetails()

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDeletedChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToDeletedChange()
        changeFields.setChangeKind(ChangeKind.DELETED)
        changeFields.setNoteWithDetails()

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toDuplicateKeyChange(): ChangeRecord {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.setChangeKind(ChangeKind.DUPLICATE_KEY_ALERT)
        changeFields.setNoteWithDetails()

        changeFields.discardFields(
            listOf(
                FallbackField::class,
                AtomField::class
            )
        )

        return ChangeRecord(
            fields = changeFields
        )
    }

    fun toModifiedChangeOrNullFromBaseline(baselineRecord: SourceRecord): ChangeRecord? {
        val changeFields: MutableMap<Field, Any?> = fields.toMutableMap()

        changeFields.transformAtomsToModifiedChange(baselineRecord.fields)
        changeFields.setChangeKind(ChangeKind.MODIFIED)
        changeFields.setNoteWithDetails(
            includeAtomValueModifiedDetail = true
        )

        changeFields.discardFields(
            listOf(
                FallbackField::class
            )
        )

        val atoms = changeFields.filterFieldType<AtomField, Any?>()

        return if (atoms.isNotEmpty()) {
            ChangeRecord(fields = changeFields)
        } else {
            null
        }
    }

    private fun MutableMap<Field, Any?>.transformAtomsToAddedChange() {
        doTransformAtoms { field, value ->

            if (AtomOption.OUTPUT_TO_ADDED_CHANGE in field.atomOptions) {
                ChangeAtomValueAdded(
                    value = value
                )
            } else {
                null
            }
        }
    }

    private fun MutableMap<Field, Any?>.transformAtomsToDeletedChange() {
        doTransformAtoms { field, value ->

            if (AtomOption.OUTPUT_TO_DELETED_CHANGE in field.atomOptions) {
                ChangeAtomValueDeleted(
                    value = value
                )
            } else {
                null
            }
        }
    }

    private fun MutableMap<Field, Any?>.transformAtomsToModifiedChange(
        baselineFields: Map<Field, String?>
    ) {
        doTransformAtoms { field, value ->
            val baselineValue = baselineFields[field]

            if (value != baselineValue) {
                ChangeAtomValueModified(
                    currentValue = value,
                    baselineValue = baselineValue
                )
            } else {
                null
            }
        }
    }

    private fun <T> MutableMap<Field, Any?>.doTransformAtoms(transform: (AtomField, String?) -> T?) {
        val atoms = this
            .filterFieldType<AtomField, Any?>()
            .map { (field, value) ->
                value as String?

                val modifiedValue = transform(field, value)

                field to modifiedValue
            }

        val (discard, update) = atoms.partition { (_, value) -> value == null }
        discard.forEach { (field, _) -> remove(field) }
        update.forEach { (field, value) -> put(field, value) }
    }

    private fun MutableMap<Field, Any?>.setChangeKind(
        changeKind: ChangeKind
    ) {
        val changeKindField = sectionOutline
            .sectionFields
            .firstFieldOfType<ChangeKindField>()

        this[changeKindField] = changeKind
    }

    private fun MutableMap<Field, Any?>.setNoteWithDetails(
        includeAtomValueModifiedDetail: Boolean = false
    ) {
        val details = listOf(
            {
                recordIdentificationDetail(this)
            },
            {
                if (includeAtomValueModifiedDetail) {
                    atomValueModifiedDetail(this)
                } else {
                    null
                }
            }
        ).mapNotNull { it() }

        if (details.isEmpty()) return

        val noteField = sectionOutline
            .sectionFields
            .firstFieldOfType<NoteField>()

        val noteValue = details.joinToString(separator = "\n\n")

        this[noteField] = noteValue
    }

    private fun MutableMap<Field, Any?>.discardFields(discarded: List<KClass<*>>) {
        val discard = filter { it::class in discarded }.keys
        discard.forEach { remove(it) }
    }

    private fun recordIdentificationDetail(
        fields: Map<Field, Any?>
    ): String? {
        fun shouldOutputRecordIdentityFallbackForCorrelationKeys() = fields
            .filterFieldType<KeyField, Any?>()
            .filter { (field, value) -> field.shouldOutputRecordIdentityFallback(value) }
            .any()

        fun shouldOutputRecordIdentityFallbackForIdentificationLabels() = fields
            .filterFieldType<IdentificationLabelField, Any?>()
            .all { (field, value) -> field.shouldOutputRecordIdentityFallback(value) }

        val identityFallbackField = sectionOutline
            .sectionFields
            .firstFieldOfTypeOrNull<RecordIdentityFallbackField>() ?: return null

        return if (
            shouldOutputRecordIdentityFallbackForCorrelationKeys() ||
            shouldOutputRecordIdentityFallbackForIdentificationLabels()
        ) {
            val identityFallbackItems = identityFallbackField
                .identityFallbacks
                .map { fallbackField -> "${fallbackField.fieldName.splitCamelCaseWords()}: ${fields[fallbackField]}" }

            layoutNoteDetail(
                detailTitle = "$sourceKind ${identityFallbackField.fieldName.splitCamelCaseWords().toUpperCase()}",
                detailItems = identityFallbackItems
            )
        } else {
            null
        }
    }

    private fun atomValueModifiedDetail(
        fields: Map<Field, Any?>
    ): String? {

        val modifiedFieldItems = fields
            .filterFieldType<AtomField, Any?>()
            .filter { (_, value) -> value is ChangeAtomValueModified }
            .map { (field, _) -> field.fieldName.splitCamelCaseWords() }

        return layoutNoteDetail(
            detailTitle = "MODIFIED",
            detailItems = modifiedFieldItems
        )
    }

    private fun layoutNoteDetail(
        detailTitle: String,
        detailItems: List<String>
    ): String? {
        if (detailItems.isEmpty()) return null
        val itemPrefix = "\n- "
        return "$detailTitle:${detailItems.joinToString(prefix = itemPrefix, separator = itemPrefix)}"
    }
}
