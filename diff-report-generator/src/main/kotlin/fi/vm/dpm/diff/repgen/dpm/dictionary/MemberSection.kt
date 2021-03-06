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
import fi.vm.dpm.diff.repgen.dpm.utils.SourceTableDescriptor

object MemberSection {

    fun sectionPlan(dpmSectionOptions: DpmSectionOptions): SectionPlanSql {

        val domainInherentLabel = FallbackField(
            fieldName = "DomainLabel"
        )

        val domainCode = KeyField(
            fieldName = "DomainCode",
            keyFieldKind = KeyFieldKind.CONTEXT_PARENT_KEY,
            keyFieldFallback = domainInherentLabel
        )

        val memberId = FallbackField(
            fieldName = "MemberId"
        )

        val memberInherentLabel = FallbackField(
            fieldName = "MemberLabel"
        )

        val recordIdentityFallback = RecordIdentityFallbackField(
            identityFallbacks = listOf(memberId, memberInherentLabel)
        )

        val memberCode = KeyField(
            fieldName = "MemberCode",
            keyFieldKind = KeyFieldKind.PRIME_KEY,
            keyFieldFallback = memberInherentLabel
        )

        val identificationLabels = DpmSectionIdentificationLabels(
            fieldNameBase = "MemberLabel",
            dpmSectionOptions = dpmSectionOptions
        )

        val changeKind = ChangeKindField()

        val isDefaultMember = AtomField(
            fieldName = "IsDefaultMember"
        )
        val note = NoteField()

        val sectionOutline = SectionOutline(
            sectionShortTitle = "Member",
            sectionTitle = "Members",
            sectionDescription = "Added and deleted Members, changes in IsDefaultMember",
            sectionChangeDetectionMode = ChangeDetectionMode.CORRELATE_BY_KEY_FIELDS,
            sectionFields = listOf(
                domainInherentLabel,
                domainCode,
                memberId,
                memberInherentLabel,
                recordIdentityFallback,
                memberCode,
                *identificationLabels.labelFields(),
                changeKind,
                isDefaultMember,
                note
            ),
            sectionSortOrder = listOf(
                NumberAwareSortBy(domainCode),
                NumberAwareSortBy(memberCode),
                FixedChangeKindSortBy(changeKind)
            ),
            includedChanges = ChangeKind.allChanges()
        )

        val queryColumnMapping = mapOf(
            "DomainInherentLabel" to domainInherentLabel,
            "DomainCode" to domainCode,
            "MemberId" to memberId,
            "MemberInherentLabel" to memberInherentLabel,
            "MemberCode" to memberCode,
            *identificationLabels.labelColumnMapping(),
            "IsDefaultMember" to isDefaultMember
        )

        val query = """
            SELECT
            mDomain.DomainLabel AS 'DomainInherentLabel'
            ,mDomain.DomainCode AS 'DomainCode'
            ,mMember.MemberID AS 'MemberId'
            ,mMember.MemberLabel AS 'MemberInherentLabel'
            ,mMember.MemberCode AS 'MemberCode'
             ${identificationLabels.labelAggregateFragment()}
            ,mMember.IsDefaultMember AS 'IsDefaultMember'

            FROM mMember
            LEFT JOIN mDomain ON mDomain.DomainID = mMember.DomainID
            LEFT JOIN mConceptTranslation ON mConceptTranslation.ConceptID = mMember.ConceptID
            LEFT JOIN mLanguage ON mConceptTranslation.LanguageID = mLanguage.LanguageID

            WHERE
            (mConceptTranslation.Role = "label" OR mConceptTranslation.Role IS NULL)
            AND mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)

            GROUP BY mMember.MemberID

            ORDER BY mDomain.DomainCode ASC, mMember.MemberCode ASC
            """.trimLineStartsAndConsequentBlankLines()

        val sourceTableDescriptors = listOf(
            SourceTableDescriptor(
                table = "mMember",
                where = "mMember.MemberID NOT IN (SELECT CorrespondingMemberID FROM mMetric)"
            )
        )

        return SectionPlanSql.withSingleQuery(
            sectionOutline = sectionOutline,
            queryColumnMapping = queryColumnMapping,
            query = query,
            sourceTableDescriptors = sourceTableDescriptors
        )
    }
}
