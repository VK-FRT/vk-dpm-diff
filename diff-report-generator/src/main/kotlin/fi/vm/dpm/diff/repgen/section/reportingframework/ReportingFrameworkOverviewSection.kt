package fi.vm.dpm.diff.repgen.section.reportingframework

import ext.kotlin.trimLineStartsAndConsequentBlankLines
import fi.vm.dpm.diff.repgen.GenerationContext
import fi.vm.dpm.diff.repgen.section.ElementOverviewSectionBase
import fi.vm.dpm.diff.repgen.section.ElementQueryDescriptor

class ReportingFrameworkOverviewSection(
    generationContext: GenerationContext
) : ElementOverviewSectionBase(
    generationContext
) {
    companion object {
        val elementQueryDescriptors = listOf(
            ElementQueryDescriptor(
                elementType = "ReportingFramework",
                elementTableName = "mReportingFramework",
                elementIdColumn = "FrameworkID",
                elementCodeColumn = "FrameworkCode",
                elementInherentLabelColumn = "FrameworkLabel",
                parentType = "",
                parentCodeStatement = "NULL",
                parentTableJoin = "",
                elementTableSliceCriteria = ""
            ),

            ElementQueryDescriptor(
                elementType = "Taxonomy",
                elementTableName = "mTaxonomy",
                elementIdColumn = "TaxonomyID",
                elementCodeColumn = "TaxonomyCode",
                elementInherentLabelColumn = "TaxonomyLabel",
                parentType = "ReportingFramework",
                parentCodeStatement = "mReportingFramework.FrameworkCode",
                parentTableJoin = "LEFT JOIN mReportingFramework ON mReportingFramework.FrameworkID = mTaxonomy.FrameworkID",
                elementTableSliceCriteria = ""
            ),

            ElementQueryDescriptor(
                elementType = "Module",
                elementTableName = "mModule",
                elementIdColumn = "ModuleID",
                elementCodeColumn = "ModuleCode",
                elementInherentLabelColumn = "ModuleLabel",
                parentType = "Taxonomy",
                parentCodeStatement = "mTaxonomy.TaxonomyCode",
                parentTableJoin = "LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mModule.TaxonomyID",
                elementTableSliceCriteria = ""
            ),

            ElementQueryDescriptor(
                elementType = "Table",
                elementTableName = "mTable",
                elementIdColumn = "TableID",
                elementCodeColumn = "TableCode",
                elementInherentLabelColumn = "TableLabel",
                parentType = "Taxonomy",
                parentCodeStatement = "mTaxonomy.TaxonomyCode",
                parentTableJoin =
                """
                    LEFT JOIN mTaxonomyTable ON mTaxonomyTable.TableID = mTable.TableID
                    LEFT JOIN mTaxonomy ON mTaxonomy.TaxonomyID = mTaxonomyTable.TaxonomyID
                """.trimLineStartsAndConsequentBlankLines(),
                elementTableSliceCriteria = ""
            )
        )
    }

    override val sectionDescriptor = elementOverviewSectionDescriptor(
        sectionShortTitle = "RepFwOverview",
        sectionTitle = "ReportingFramework overview",
        sectionDescription = "ReportingFramework: added and deleted Reporting Frameworks, Taxonomies, Modules and Tables"
    )

    override val queryColumnMapping = elementOverviewQueryColumnMappings()

    override val query = elementOverviewQuery(ReportingFrameworkOverviewSection.elementQueryDescriptors)

    override val sourceTableDescriptors = listOf(
        "mReportingFramework",
        "mTaxonomy",
        "mModule",
        "mTable"
    )

    init {
        sanityCheckSectionConfig()
    }
}