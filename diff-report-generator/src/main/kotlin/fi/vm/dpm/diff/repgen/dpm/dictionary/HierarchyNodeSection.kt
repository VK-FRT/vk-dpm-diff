package fi.vm.dpm.diff.repgen.dpm.dictionary

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

object HierarchyNodeSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val hierarchyId = FallbackField(
            fieldName = "HierarchyId"
        )

        val hierarchyInherentLabel = FallbackField(
            fieldName = "HierarchyLabel"
        )

        val memberId = FallbackField(
            fieldName = "MemberId"
        )

        val memberInherentLabel = FallbackField(
            fieldName = "MemberLabel"
        )

        val hierarchyNodeInherentLabel = FallbackField(
            fieldName = "HierarchyNodeLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(hierarchyId, memberId, hierarchyNodeInherentLabel)
        )

        val hierarchyCode = KeyField(
            fieldName = "HierarchyCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = hierarchyInherentLabel
        )

        val memberCode = KeyField(
            fieldName = "MemberCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = memberInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "HierarchyNodeLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val isAbstract = AtomField(
            fieldName = "IsAbstract"
        )

        val comparisonOperator = AtomField(
            fieldName = "ComparisonOperator"
        )

        val unaryOperator = AtomField(
            fieldName = "UnaryOperator"
        )

        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "HierNode",
            sectionTitle = "HierarchyNodes",
            sectionDescription = "Added and deleted HierarchyNodes, changes in ComparisonOperator, UnaryOperator and IsAbstract details",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                hierarchyId,
                hierarchyInherentLabel,
                memberId,
                memberInherentLabel,
                hierarchyNodeInherentLabel,
                recordIdentityFallback,
                hierarchyCode,
                memberCode,
                *identificationLabels.labelFields(),
                changeKind,
                isAbstract,
                comparisonOperator,
                unaryOperator,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(hierarchyCode),
                NumberAwareSortBy(memberCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "HierarchyId" to hierarchyId,
            "HierarchyInherentLabel" to hierarchyInherentLabel,
            "MemberId" to memberId,
            "MemberInherentLabel" to memberInherentLabel,
            "HierarchyNodeInherentLabel" to hierarchyNodeInherentLabel,
            "HierarchyCode" to hierarchyCode,
            "MemberCode" to memberCode,
            *identificationLabels.labelColumnMapping(),
            "IsAbstract" to isAbstract,
            "ComparisonOperator" to comparisonOperator,
            "UnaryOperator" to unaryOperator
        )

        val query = """
            SELECT
            mHierarchyNode.HierarchyID AS 'HierarchyId'
            ,mHierarchy.HierarchyLabel AS 'HierarchyInherentLabel'
            ,mHierarchyNode.MemberID AS 'MemberId'
            ,mMember.MemberLabel AS 'MemberInherentLabel'
            ,mHierarchyNode.HierarchyNodeLabel AS 'HierarchyNodeInherentLabel'
            ,mHierarchy.HierarchyCode AS 'HierarchyCode'
            ,mMember.MemberCode AS 'MemberCode'
             ${identificationLabels.labelAggregateFragment()}
            ,mHierarchyNode.IsAbstract AS 'IsAbstract'
            ,mHierarchyNode.ComparisonOperator AS 'ComparisonOperator'
            ,mHierarchyNode.UnaryOperator AS 'UnaryOperator'

            FROM mHierarchyNode
            LEFT JOIN mHierarchy ON mHierarchy.HierarchyID = mHierarchyNode.HierarchyID
            LEFT JOIN mMember ON mMember.MemberID = mHierarchyNode.MemberID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mHierarchyNode.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)

            GROUP BY mHierarchyNode.HierarchyID, mHierarchyNode.MemberID

            ORDER BY mHierarchy.HierarchyCode ASC, mMember.MemberCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            "mHierarchyNode"
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
