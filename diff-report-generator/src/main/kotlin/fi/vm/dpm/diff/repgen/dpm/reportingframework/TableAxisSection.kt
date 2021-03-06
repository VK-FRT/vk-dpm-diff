package fi.vm.dpm.diff.repgen.dpm.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSortBy
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSortBy
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql
import fi.vm.dpm.diff.repgen.dpm.DpmSectionOptions
import fi.vm.dpm.diff.repgen.dpm.utils.DpmSectionIdentificationLabels

object TableAxisSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val taxonomyInherentLabel = FallbackField(
            fieldName = "TaxonomyLabel"
        )

        val taxonomyCode = KeyField(
            fieldName = "TaxonomyCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = taxonomyInherentLabel
        )

        val tableInherentLabel = FallbackField(
            fieldName = "TableLabel"
        )

        val tableCode = KeyField(
            fieldName = "TableCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = tableInherentLabel
        )

        val axisId = FallbackField(
            fieldName = "AxisId"
        )

        val axisInherentLabel = FallbackField(
            fieldName = "AxisLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(axisId, axisInherentLabel)
        )

        val axisOrientation = KeyField(
            fieldName = "AxisOrientation",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = axisInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "AxisLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val order = AtomField(
            fieldName = "Order"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "TableAxis",
            sectionTitle = "TableAxis",
            sectionDescription = "Added and deleted Table Axis, changes in Order",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                taxonomyInherentLabel,
                taxonomyCode,
                tableInherentLabel,
                tableCode,
                axisId,
                axisInherentLabel,
                recordIdentityFallback,
                axisOrientation,
                *identificationLabels.labelFields(),
                changeKind,
                order,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(taxonomyCode),
                NumberAwareSortBy(tableCode),
                NumberAwareSortBy(axisOrientation),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "TaxonomyInherentLabel" to taxonomyInherentLabel,
            "TaxonomyCode" to taxonomyCode,
            "TableInherentLabel" to tableInherentLabel,
            "TableCode" to tableCode,
            "AxisId" to axisId,
            "AxisInherentLabel" to axisInherentLabel,
            "AxisOrientation" to axisOrientation,
            *identificationLabels.labelColumnMapping(),
            "Order" to order
        )

        val query = """
            SELECT
            mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
            ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
            ,mTable.TableLabel AS 'TableInherentLabel'
            ,mTable.TableCode AS 'TableCode'
            ,mAxis.AxisID AS 'AxisId'
            ,mAxis.AxisLabel AS 'AxisInherentLabel'
            ,mAxis.AxisOrientation AS 'AxisOrientation'
             ${identificationLabels.labelAggregateFragment()}
            ,mTableAxis.'Order' AS 'Order'

            FROM mTableAxis
            LEFT JOIN mAxis ON mAxis.AxisID = mTableAxis.AxisID
            LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
            LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
            LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mAxis.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

            GROUP BY mAxis.AxisID

            ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mTableAxis"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
