package fi.vm.dpm.diff.repgen.dpm.section

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.AtomOption
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.Field
import fi.vm.dpm.diff.model.FixedChangeKindSort
import fi.vm.dpm.diff.model.FixedElementTypeSort
import fi.vm.dpm.diff.model.FixedTranslationRoleSort
import fi.vm.dpm.diff.model.KeySegmentField
import fi.vm.dpm.diff.model.KeySegmentKind
import fi.vm.dpm.diff.model.NumberAwareSort
import fi.vm.dpm.diff.model.SectionDescriptor
import fi.vm.dpm.diff.repgen.dpm.DpmGenerationContext
import fi.vm.dpm.diff.repgen.dpm.section.ElementTranslationHelpers.translationLanguageWhereStatement

open class ElementTranslationSectionBase(
    generationContext: DpmGenerationContext
) : ElementOverviewSectionBase(
    generationContext
) {
    private val translationRole = KeySegmentField(
        fieldName = "TranslationRole",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    private val translationLanguage = KeySegmentField(
        fieldName = "Language",
        segmentKind = KeySegmentKind.SUB_SEGMENT,
        segmentFallback = null
    )

    private val translation = AtomField(
        fieldName = "Translation",
        displayHint = DisplayHint.FIXED_EXTRA_WIDE,
        atomOptions = AtomOption.OUTPUT_TO_ADDED_CHANGE
    )

    protected fun elementTranslationSectionDescriptor(
        sectionShortTitle: String,
        sectionTitle: String,
        sectionDescription: String
    ): SectionDescriptor {
        return SectionDescriptor(
            sectionShortTitle = sectionShortTitle,
            sectionTitle = sectionTitle,
            sectionDescription = sectionDescription,
            sectionFields = listOf(
                elementId,
                elementInherentLabel,
                recordIdentityFallback,
                parentElementType,
                parentElementCode,
                elementType,
                elementCode,
                *identificationLabels,
                translationRole,
                translationLanguage,
                changeKind,
                translation,
                note
            ),
            sectionSortOrder = listOf(
                FixedElementTypeSort(parentElementType),
                NumberAwareSort(parentElementCode),
                FixedElementTypeSort(elementType),
                NumberAwareSort(elementCode),
                FixedTranslationRoleSort(translationRole),
                NumberAwareSort(translationLanguage),
                FixedChangeKindSort(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )
    }

    protected fun elementTranslationQueryColumnMappings(): Map<String, Field> {
        return mapOf(
            "ElementId" to elementId,
            "ElementInherentLabel" to elementInherentLabel,
            "ElementType" to elementType,
            "ElementCode" to elementCode,
            "ParentElementType" to parentElementType,
            "ParentElementCode" to parentElementCode,
            *idLabelColumnMapping(),
            "TranslationRole" to translationRole,
            "TranslationLanguage" to translationLanguage,
            "Translation" to translation
        )
    }

    protected fun elementTranslationQuery(
        elementQueryDescriptors: List<ElementQueryDescriptor>,
        translationLangCodes: List<String>?
    ): String {
        val query =
            """
            -- Shared sub-queries
            WITH ${elementQueryDescriptors
                .map {
                    """
                        ${elementOverviewQueryExpression(it)},
                        ${"\n" + elementTranslationQueryExpression(it)}
                    """.trimLineStartsAndConsequentBlankLines()
                }
                .joinToString(",\n\n")
            }

            -- Main query
            SELECT
            ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementType AS ElementType
            ,ElementCode AS ElementCode
            ,ParentElementType AS ParentElementType
            ,ParentElementCode AS ParentElementCode
            ${idLabelColumnNamesFragment()}
            ,TranslationRole AS TranslationRole
            ,TranslationLanguage AS TranslationLanguage
            ,Translation AS Translation

            FROM (
                ${elementQueryDescriptors
                .map { "SELECT * FROM ${elementTranslationQueryName(it)}" }
                .joinToString("\nUNION ALL\n")}
            )

            ${translationLanguageWhereStatement(translationLangCodes)}

            ORDER BY ElementType, ParentElementCode, ElementCode
            """

        return query.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationQueryExpression(
        elementQueryDescription: ElementQueryDescriptor
    ): String {
        return """
            ${elementTranslationQueryName(elementQueryDescription)} AS (
            SELECT
            ElementType AS ElementType
            ,ElementId AS ElementId
            ,ElementInherentLabel AS ElementInherentLabel
            ,ElementCode AS ElementCode
            ,ParentElementType AS ParentElementType
            ,ParentElementCode AS ParentElementCode
            ${idLabelColumnNamesFragment()}
            ,mConceptTranslation.Role AS TranslationRole
            ,mLanguage.IsoCode AS TranslationLanguage
            ,mConceptTranslation.Text AS Translation

            FROM
            ${elementOverviewQueryName(elementQueryDescription)}

            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = ElementConceptId
            LEFT JOIN mLanguage ON mLanguage.LanguageID = mConceptTranslation.LanguageID
            )
            """.trimLineStartsAndConsequentBlankLines()
    }

    private fun elementTranslationQueryName(
        elementQueryDescription: ElementQueryDescriptor
    ): String {
        return "${elementQueryDescription.elementType}Translation"
    }
}