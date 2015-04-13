package resources.groovy.birt
import ru.naumen.stech.bobject.program.priorityline.ProgramPriorityLine
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.stech.catalogs.CriticalTechCatalogItem
import ru.naumen.stech.catalogs.GRNTICatalogItem
import ru.naumen.fcntp.measurement.celltype.RequirementDraftWorkPriceBox.RDraftFinancingCell


zft_uuid = 'reqdrafs000080000k0tir53gmg9gvps'
def box
def finances = []
helper.execute { s ->

zft = helper.getObject(zft_uuid, s)
report.vars.zft = zft
report.vars.program = zft.parent.title
report.vars.programAction = zft.programAction.title

priorityLines = []
extraResearchArea = []
criticalTech = []

for (ProgramPriorityLine item in zft.priorityLinesSet)
    priorityLines.add(item.priorityLine.title)

for (GRNTICatalogItem item in zft.extraResearchAreaSet)
    extraResearchArea.add(item.title)

for (CriticalTechCatalogItem item in zft.criticalTechSet)
    criticalTech.add(item.title)
    
box = ru.naumen.ccamext.measurement.impl.BoxHibernateHandler.getBoxWithCode(zft, "requirementDraftWorkPrice", s).cells.toArray()
for (item in box)
      finances.add(new fin(ordinalYear: item.getCellCoordinate().get("OrdinalYear"), workPriceRequirementDraft: item.getCellCoordinate().get("workPriceRequirementDraft"), value : item.value))
    
report.vars.priorityLines = priorityLines
report.vars.extraResearchArea = extraResearchArea
report.vars.criticalTech = criticalTech
report.vars.financeBox = finances

}
class fin
{
    String ordinalYear
    String workPriceRequirementDraft
    BigDecimal value
}