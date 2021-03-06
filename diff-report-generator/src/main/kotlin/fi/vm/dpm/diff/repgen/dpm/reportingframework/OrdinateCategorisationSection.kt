package fi.vm.dpm.diff.repgen.dpm.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.model.AtomField
import fi.vm.dpm.diff.model.ChangeDetectionMode
import fi.vm.dpm.diff.model.ChangeKind
import fi.vm.dpm.diff.model.ChangeKindField
import fi.vm.dpm.diff.model.DisplayHint
import fi.vm.dpm.diff.model.FallbackField
import fi.vm.dpm.diff.model.FixedChangeKindSortBy
import fi.vm.dpm.diff.model.KeyField
import fi.vm.dpm.diff.model.KeyFieldKind
import fi.vm.dpm.diff.model.NoteField
import fi.vm.dpm.diff.model.NumberAwareSortBy
import fi.vm.dpm.diff.model.RecordIdentityFallbackField
import fi.vm.dpm.diff.model.SectionOutline
import fi.vm.dpm.diff.repgen.SectionPlanSql

object OrdinateCategorisationSection {

    fun sectionPlan(): SectionPlanSql {

        // Scope
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

        val axisInherentLabel = FallbackField(
            fieldName = "AxisLabel"
        )

        val axisOrientation = KeyField(
            fieldName = "AxisOrientation",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = axisInherentLabel
        )

        // OrdinateCategorisation
        val ordinateId = FallbackField(
            fieldName = "OrdinateId"
        )

        val ordinateInherentLabel = FallbackField(
            fieldName = "OrdinateLabel"
        )

        val ordinateCode = KeyField(
            fieldName = "OrdinateCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = ordinateInherentLabel
        )

        val dimensionId = FallbackField(
            fieldName = "DimensionId"
        )

        val dimensionInherentLabel = FallbackField(
            fieldName = "DimensionLabel"
        )

        val dimensionCode = KeyField(
            fieldName = "DimensionCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = dimensionInherentLabel
        )

        val memberId = FallbackField(
            fieldName = "MemberId"
        )

        val memberInherentLabel = FallbackField(
            fieldName = "MemberLabel"
        )

        val memberCode = KeyField(
            fieldName = "MemberCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = memberInherentLabel
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(ordinateId, dimensionId, memberId)
        )

        val changeKind = ChangeKindField()

        // Atoms
        val source = AtomField(
            fieldName = "Source"
        )

        val dps = AtomField(
            fieldName = "Dps",
            displayHint = DisplayHint.FIXED_EXTRA_WIDE
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "OrdCat",
            sectionTitle = "OrdinateCategorisations",
            sectionDescription = "Added and deleted OrdinateCategorisations",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                taxonomyInherentLabel,
                taxonomyCode,
                tableInherentLabel,
                tableCode,
                axisInherentLabel,
                axisOrientation,
                ordinateId,
                ordinateInherentLabel,
                ordinateCode,
                dimensionId,
                dimensionInherentLabel,
                dimensionCode,
                memberId,
                memberInherentLabel,
                memberCode,
                recordIdentityFallback,
                changeKind,
                source,
                dps,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(taxonomyCode),
                NumberAwareSortBy(tableCode),
                NumberAwareSortBy(axisOrientation),
                NumberAwareSortBy(ordinateCode),
                NumberAwareSortBy(dimensionCode),
                NumberAwareSortBy(memberCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "TaxonomyInherentLabel" to taxonomyInherentLabel,
            "TaxonomyCode" to taxonomyCode,
            "TableInherentLabel" to tableInherentLabel,
            "TableCode" to tableCode,
            "AxisInherentLabel" to axisInherentLabel,
            "AxisOrientation" to axisOrientation,

            "OrdinateId" to ordinateId,
            "OrdinateInherentLabel" to ordinateInherentLabel,
            "OrdinateCode" to ordinateCode,

            "DimensionId" to dimensionId,
            "DimensionInherentLabel" to dimensionInherentLabel,
            "DimensionCode" to dimensionCode,

            "MemberId" to memberId,
            "MemberInherentLabel" to memberInherentLabel,
            "MemberCode" to memberCode,

            "Source" to source,
            "DPS" to dps
        )

        val query = """
            SELECT
            mTaxonomy.TaxonomyLabel AS 'TaxonomyInherentLabel'
            ,mTaxonomy.TaxonomyCode AS 'TaxonomyCode'
            ,mTable.TableLabel AS 'TableInherentLabel'
            ,mTable.TableCode AS 'TableCode'
            ,mAxis.AxisLabel AS 'AxisInherentLabel'
            ,mAxis.AxisOrientation AS 'AxisOrientation'
            ,mAxisOrdinate.OrdinateID AS 'OrdinateId'
            ,mAxisOrdinate.OrdinateLabel AS 'OrdinateInherentLabel'
            ,mAxisOrdinate.OrdinateCode AS 'OrdinateCode'
            ,mDimension.DimensionID AS 'DimensionId'
            ,mDimension.DimensionLabel AS 'DimensionInherentLabel'
            ,mDimension.DimensionCode AS 'DimensionCode'
            ,mMember.MemberID AS 'MemberId'
            ,mMember.MemberLabel AS 'MemberInherentLabel'
            ,mMember.MemberCode AS 'MemberCode'
            ,mOrdinateCategorisation.Source AS 'Source'
            ,mOrdinateCategorisation.DPS AS 'DPS'

            FROM mOrdinateCategorisation
            LEFT JOIN mAxisOrdinate ON mAxisOrdinate.OrdinateID = mOrdinateCategorisation.OrdinateID
            LEFT JOIN mAxis ON mAxis.AxisID = mAxisOrdinate.AxisID
            LEFT JOIN mTableAxis ON mTableAxis.AxisID = mAxisOrdinate.AxisID
            LEFT JOIN mTable ON mTable.TableID = mTableAxis.TableID
            LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
            LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
            LEFT JOIN mDimension ON mDimension.DimensionID = mOrdinateCategorisation.DimensionID
            LEFT JOIN mMember ON mMember.MemberID = mOrdinateCategorisation.MemberID

            ORDER BY mTaxonomy.TaxonomyCode ASC, mTable.TableCode ASC, mAxis.AxisOrientation ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mOrdinateCategorisation"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
