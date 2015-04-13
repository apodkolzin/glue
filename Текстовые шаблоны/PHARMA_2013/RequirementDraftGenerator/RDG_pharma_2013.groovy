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
//def zft = object
//zft = helper.get('reqdrafs000080000k3fupntfcmblhk4')
zft = helper.get('reqdrafs000080000k1sn8j9hr7ne62g')
helper.execute { s ->

    report.vars.zft = zft
    report.vars.program = zft.parent.title
    report.vars.programAction = zft.programAction.displayableIdentifier + " " + zft.programAction.title

    def priorityLines = []
    def extraResearchArea = []
    def criticalTech = []
    def blocks = []
    def scienceProgramForm = []

    for (ProgramPriorityLine item in zft.priorityLinesSet) {
        priorityLines.add(item.priorityLine.title)
    }

    for (GRNTICatalogItem item in zft.extraResearchAreaSet) {
        extraResearchArea.add(item.code + " " + item.title)
    }

    for (CriticalTechCatalogItem item in zft.criticalTechSet) {
        criticalTech.add(item.title)
    }

//верный порядок форм мероприятия
    def scienceProgramFormSet = zft.requirementProperties.scienceProgramFormSet.sort()
    Collections.reverse(scienceProgramFormSet)
    def scienceProgramFormLength = 0//сколько всего форм исключая "другое"
    for (SimpleCatalogItem item in scienceProgramFormSet) {
        scienceProgramForm.add(item.title)
        if (!item.title.equals("другое")){
            scienceProgramFormLength++
        }
    }

    report.vars.priorityLines = priorityLines
    report.vars.extraResearchArea = extraResearchArea
    report.vars.criticalTech = criticalTech
//форма мероприятия
    report.vars.scienceProgramForm = scienceProgramForm
    report.vars.scienceProgramFormLength = scienceProgramFormLength
    def scienceProgramFormOtherChecked = false
    if (scienceProgramForm.contains("другое")) {
        scienceProgramFormOtherChecked = true
    }
    report.vars.scienceProgramFormOtherChecked = scienceProgramFormOtherChecked

    //финансовый бокс
    if (BoxHibernateHandler.getBoxWithCode(zft, "requirementDraftWorkPrice", s) != null) {
        def boxCells = BoxHibernateHandler.getBoxWithCode(zft, "requirementDraftWorkPrice", s).cells.toArray()
        for (item in boxCells) {
            finances.add(new res(
                    ordinalYear: item.getCellCoordinate().get("OrdinalYear"),
                    workPriceRequirementDraft: item.getCellCoordinate().get("workPriceRequirementDraft"),
                    value: item.value))
        }
        report.vars.financeBox = finances
    }

//Блоки уровень профессионального образования; Характер (тип программы)
    def profEducationBuilder = ""
    for (def item in zft.educationLevelSet) {
        profEducationBuilder <<= item.title << ";\n"
    }
    report.vars.profEducationLevel = profEducationBuilder.toString()

    def programTypeBuilder = ""
    for (def item in zft.farmaProgramTypeSet) {
        programTypeBuilder <<= item.title << ";\n"
    }
    report.vars.programType = programTypeBuilder

//перечень блоков для ПМ
    def blocksStr = ""
    for (def block in getCatalogItemByAction(zft.programAction?.UUID).getBlockSet()) {
        blockItem = helper.get(block)
        blocks.add(blockItem.code)
        blocksStr<<=(blockItem.code)<<(",")
    }
    report.vars.blocks = blocksStr

//Поддержка проекта
    report.vars.rfSubjectDepartment = zft.rfSubjectDepartment
    report.vars.researchBranchOrganization = zft.researchBranchOrganization
    report.vars.scientificCommunity = zft.scientificCommunity
    report.vars.businessCommunity = zft.businessCommunity
    report.vars.consortium = zft.consortium
    report.vars.projectSupported = zft.projectSupportedBy.size() == 0 ? false : true

}//helper.execute { s ->

def getCatalogItemByAction(paId) {
    for (item in helper.select("from ProgramActionBlockCatalogItem")) {
        if (item.programAction?.UUID.equals(paId)) {
            return item
        }
    }
}

class res {
    String ordinalYear
    String workPriceRequirementDraft
    BigDecimal value
}