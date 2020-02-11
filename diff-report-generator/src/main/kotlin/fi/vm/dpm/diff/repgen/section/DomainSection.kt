package fi.vm.dpm.diff.repgen.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.DifferenceKind
import fi.vm.dpm.diff.model.FieldDescriptor
import fi.vm.dpm.diff.model.FieldKind
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.SectionBase

class DomainSection(
    generationContext: GenerationContext
) : SectionBase(
    generationContext
) {
    override val includedDifferenceKinds: Array<DifferenceKind> = DifferenceKind.values()

    private val domainId = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "DomainId"
    )

    private val domainInherentLabel = FieldDescriptor(
        fieldKind = FieldKind.FALLBACK_VALUE,
        fieldName = "DomainLabel"
    )

    private val domainCode = FieldDescriptor(
        fieldKind = FieldKind.CORRELATION_KEY,
        fieldName = "DomainCode",
        correlationKeyFallback = domainInherentLabel,
        noteFallback = listOf(domainId, domainInherentLabel)
    )

    override val identificationLabels = composeIdentificationLabelFields(
        noteFallback = domainInherentLabel
    ) {
        "DomainLabel$it"
    }

    private val dataType = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "DataType"
    )

    private val isTypedDomain = FieldDescriptor(
        fieldKind = FieldKind.ATOM,
        fieldName = "IsTypedDomain"
    )

    override val sectionDescriptor = SectionDescriptor(
        sectionShortTitle = "Domain",
        sectionTitle = "Domains",
        sectionDescription = "Domains: data type changes (in TypedDomains) and domain kind changes (TypedDomain/ExplicitDomain)",
        sectionFields = listOf(
            domainId,
            domainInherentLabel,
            domainCode,
            *identificationLabels,
            differenceKind,
            dataType,
            isTypedDomain,
            note
        )
    )

    override val queryColumnMapping = mapOf(
        "DomainId" to domainId,
        "DomainInherentLabel" to domainInherentLabel,
        "DomainCode" to domainCode,
        *composeIdentificationLabelColumnNames(),
        "DataType" to dataType,
        "IsTypedDomain" to isTypedDomain
    )

    override val query = """
        SELECT
        mDomain.DomainID AS 'DomainId'
        ,mDomain.DomainLabel AS 'DomainInherentLabel'
        ,mDomain.DomainCode AS 'DomainCode'
        ${composeIdentificationLabelQueryFragment("mLanguage.IsoCode", "mConceptTranslation.Text")}
        ,mDomain.DataType AS 'DataType'
        ,mDomain.IsTypedDomain AS 'IsTypedDomain'

        FROM mDomain
        LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mDomain.ConceptID
        LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID

        WHERE
        mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL

        GROUP BY mDomain.DomainID

        ORDER BY mDomain.DomainCode ASC
    """.trimLineStartsAndConsequentBlankLines()

    override val primaryTables = listOf(
        "mDomain"
    )

    init {
        sanityCheckSectionConfig()
    }
}
