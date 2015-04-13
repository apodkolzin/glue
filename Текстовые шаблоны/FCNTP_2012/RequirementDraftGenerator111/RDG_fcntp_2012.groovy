package resources.groovy.birt
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.core.catalogsengine.SimpleCatalogItem
import ru.naumen.stech.bobject.program.priorityline.ProgramPriorityLine
import ru.naumen.stech.catalogs.CriticalTechCatalogItem
import ru.naumen.stech.catalogs.GRNTICatalogItem


/**
 * @author ayakovlev
 * 04.10.2013
 * Скрипт для генерации печатной версии зфт
 */

def finances = []
def zft = object
helper.execute { s ->

report.vars.zft = zft
report.vars.program = zft.parent.title
report.vars.programAction = zft.programAction.displayableIdentifier + " " + zft.programAction.title

def priorityLines = []
def extraResearchArea = []
def criticalTech = []
def blocks = []
def scienceProgramForm = []

for (ProgramPriorityLine item in zft.priorityLinesSet)
    priorityLines.add(item.priorityLine.title)

for (GRNTICatalogItem item in zft.extraResearchAreaSet)
    extraResearchArea.add(item.code + " " + item.title)

for (CriticalTechCatalogItem item in zft.criticalTechSet)
    criticalTech.add(item.title)

for (SimpleCatalogItem item in zft.requirementProperties.scienceProgramFormSet)
	scienceProgramForm.add(item.title)
	
report.vars.priorityLines = priorityLines
report.vars.extraResearchArea = extraResearchArea
report.vars.criticalTech = criticalTech
//форма мероприятия
report.vars.scienceProgramForm = scienceProgramForm
def box
//финансовый бокс
if (BoxHibernateHandler.getBoxWithCode(zft, "requirementDraftWorkPrice", s) != null)
{     
    box = BoxHibernateHandler.getBoxWithCode(zft, "requirementDraftWorkPrice", s).cells.toArray()
    for (item in box)
      finances.add(new res(ordinalYear: item.getCellCoordinate().get("OrdinalYear"), workPriceRequirementDraft: item.getCellCoordinate().get("workPriceRequirementDraft"), value : item.value))
    report.vars.financeBox = finances
}

//перечень блоков для ПМ
StringBuilder blocksStr = new StringBuilder() 

for(def block in getCatalogItemByAction(zft.programAction.UUID).getBlockSet()){	
	blockItem = helper.get(block)
	blocks.add(blockItem.code)
	blocksStr.append(blockItem.code).append(",")
}
report.vars.blocks = blocksStr

//Поддержка проекта
report.vars.rfSubjectDepartment = zft.rfSubjectDepartment
report.vars.researchBranchOrganization = zft.researchBranchOrganization
report.vars.scientificCommunity = zft.scientificCommunity
report.vars.businessCommunity = zft.businessCommunity
report.vars.consortium = zft.consortium
report.vars.projectNotSupported = zft.isProjectNotSupported()

}//helper.execute { s ->

def getCatalogItemByAction(paId)  {
	for(item in helper.select("from ProgramActionBlockCatalogItem")){
	  if(item.programAction.UUID.equals(paId)){
		  return item
	  }
	}
}

class res
{
    String ordinalYear
    String workPriceRequirementDraft
    BigDecimal value
}