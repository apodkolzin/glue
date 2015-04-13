import org.apache.commons.lang3.StringUtils
import ru.naumen.ccamext.measurement.impl.BoxHibernateHandler
import ru.naumen.core.catalogsengine.SimpleCatalogItem
import ru.naumen.fcntp.ui.tlc.RequirementConferenceExperienceTLC

/**
 * Created by azimka on 30.06.14.
 */

def zft = object
//def zft = helper.get('reqdrafs000080000kcjc9camartmet4') //19090
//def zft = helper.get('reqdrafs000080000ked439lmfmj9hko') //19090
//def zft = helper.get('reqdrafs000080000kc7l111n7b9550g') //19090

helper.execute { session ->

    def blocks = []
    def scienceProgramForm = []

//верный порядок форм мероприятия
    def scienceProgramFormSet = zft.requirementProperties.scienceProgramFormSet.sort()
    Collections.reverse(scienceProgramFormSet)
    def scienceProgramFormLength = 0//сколько всего форм исключая "другое"
    for (SimpleCatalogItem item in scienceProgramFormSet)
    {
        scienceProgramForm.add(item.title)
        if (!item.title.equals("другое"))
        {
            scienceProgramFormLength++
        }
    }

    //форма мероприятия
    report.vars.scienceProgramForm = scienceProgramForm
    report.vars.scienceProgramFormLength = scienceProgramFormLength
    def scienceProgramFormOtherChecked = false
    if (scienceProgramForm.contains("другое"))
    {
        scienceProgramFormOtherChecked = true
    }
    report.vars.scienceProgramFormOtherChecked = scienceProgramFormOtherChecked

    //Блоки уровень профессионального образования; Характер (тип программы)
    def profEducationBuilder = ""
    report.vars.profEducationLevel = getStringFromList(zft.educationLevelSet)
    report.vars.programType = getStringFromList(zft.farmaProgramTypeSet)

    //перечень блоков для ПМ
    def blocksStr = ""
    for (
            def block in getCatalogItemByAction(zft.programAction?.UUID).getBlockSet())
    {
        blockItem = helper.get(block)
        blocks.add(blockItem.code)
        blocksStr <<= (blockItem.code) << (",")
    }
    report.vars.blocks = blocksStr

    //Поддержка проекта
    report.vars.rfSubjectDepartment = zft.rfSubjectDepartment
    report.vars.researchBranchOrganization = zft.researchBranchOrganization
    report.vars.scientificCommunity = zft.scientificCommunity
    report.vars.businessCommunity = zft.businessCommunity
    report.vars.consortium = zft.consortium
    report.vars.projectSupported = zft.projectSupportedBy.size() == 0 ? false : true


    ZftParams zftParams = new ZftParams()

//Вкладка "Основные данные"
    //Блок "Мероприятие федеральной целевой программы"
    GeneralInfo generalInfo = new GeneralInfo()
    generalInfo.demandIdentifier = zft.identifier
    generalInfo.demandTitle = zft?.title

    generalInfo.program = zft.parent?.title
    generalInfo.programAction = zft.programAction.displayableIdentifier + " " + zft.programAction?.title
    generalInfo.programActionIdentifier = zft.programAction.displayableIdentifier

    //Блок "Область знаний, к которым относятся исследования и разработки по проекту"
    generalInfo.extraResearchAreaSet = getStringFromList(zft.extraResearchAreaSet)

    //Блок "Технологическая платформа"
    generalInfo.technologicalPlatform = zft.technologicalPlatform?.title

    //Блок Виды работ
    WorkKinds workKinds = new WorkKinds()
    workKinds.researchEffort = getStringFromList(zft.requirementProperties.researchEffort)
    workKinds.experimentalDevelopment = zft.requirementProperties.experimentalDevelopment?.title
    workKinds.technologicalDevelopment = zft.requirementProperties.technologicalDevelopment?.title
    workKinds.conferenceOrSchool = zft.requirementProperties.conferenceOrSchool?.title
    workKinds.productionHiTech = zft.requirementProperties.developmentManagement?.title
    workKinds.production = zft.requirementProperties.production?.title
    workKinds.conferences = getStringFromList(zft.requirementProperties.conferenceSet) //: 'Конференция/школа-семинар/выставка',
    workKinds.works = zft.requirementProperties.works?.title
    generalInfo.workKinds = workKinds

    //Блок цель проекта
    ProjectAim aim = new ProjectAim()
    aim.taskContent = zft.taskContent
    aim.description = zft.description
    generalInfo.projectAim = aim

    //Приоритетные направления модернизации и технологического развития экономики России
    generalInfo.modernLinesSet = getStringFromList(zft.modernLinesSet)
    generalInfo.modernLine = zft.modernLine?.title

    //Критические технологии
    generalInfo.criticalTechSet = getStringFromList(zft.criticalTechSet)
    generalInfo.criticalTech = zft.leadingCriticalTech?.title

    //объемы финансирования проекта
    report.vars.financeBox = getDraftFinanceBoxCells(zft, "requirementDraftWorkPrice")

    //Для 5.22
    RequirementProperties requirementProperties = new RequirementProperties();
    requirementProperties.projectJustification = zft.requirementProperties?.projectJustification
    requirementProperties.pharmaDescriptionTypeTitle = zft.requirementProperties?.farmaDescriptionType?.title
    requirementProperties.entranceRequirements = zft.requirementProperties?.entranceRequirements
    requirementProperties.mainEducationPrograms = zft.requirementProperties?.mainEducationPrograms
    requirementProperties.moduleExpidiency = zft.requirementProperties?.moduleExpidiency
    requirementProperties.moduleVolume = zft.requirementProperties?.moduleVolume
    requirementProperties.practiceAreaMastered = zft.requirementProperties?.practiceAreaMastered
    requirementProperties.plannedCompetenceProgram = zft.requirementProperties?.plannedCompetenceModule
    requirementProperties.potentialEmployersMastered = zft.requirementProperties?.potentialEmployersMastered
    requirementProperties.educationProgram = zft.requirementProperties?.educationProgram
    requirementProperties.trainingDirection = zft.requirementProperties?.trainingDirection
    requirementProperties.educationStandard = zft.requirementProperties?.educationStandard
    requirementProperties.programVolume = zft.requirementProperties?.programVolume
    requirementProperties.practiceArea = zft.requirementProperties?.practiceArea
    requirementProperties.plannedCompetenceModule = zft.requirementProperties?.plannedCompetenceProgram
    requirementProperties.implModularFormat = zft.requirementProperties?.implModularFormat
    requirementProperties.implNetworkFormat = zft.requirementProperties?.implNetworkFormat
    requirementProperties.potentialEmployers = zft.requirementProperties?.potentialEmployers
    requirementProperties.projectExpectedResult = zft.requirementProperties?.projectExpectedResult
    zftParams.requirementProperties = requirementProperties

    //Блок заявитель
    if (zft.authorPerson == null)
    {
        zftParams.authorType = "Юридическое лицо"
        AuthorOrg authorOrganization = new AuthorOrg()
        authorOrganization.title = zft.authorOrg?.title
        authorOrganization.shortTitle = zft.authorOrg?.shortTitle
        authorOrganization.inn = zft.authorOrg?.inn
        authorOrganization.isBranch = zft.authorOrg?.isBranch ? "Да" : "Нет"
        authorOrganization.orgType = zft.authorOrg?.orgType
        authorOrganization.orgRole = zft.authorOrg?.orgRole
        authorOrganization.orgRoles = getStringFromList(zft.authorOrg?.orgRoles)
        authorOrganization.orgRoleOther = zft.authorOrg?.orgRoleOther
        authorOrganization.declarantContributions = getStringFromList(zft.authorOrg?.declarantContributions)
        authorOrganization.declarantContributionOther = zft.authorOrg?.declarantContributionOther
        //authorOrganization.juridicalAddress = zft.authorOrg?.juridicalAddress
        authorOrganization.juridicalAddressDraft = zft.authorOrg?.juridicalAddressDraft
        authorOrganization.authorEquals = zft.authorOrg?.authorEquals
        zftParams.authorOrg = authorOrganization

        Person manager = new Person()
        manager.firstName = zft.manager?.firstName     //Имя
        manager.middleName = zft.manager?.middleName     //Отчество
        manager.lastName = zft.manager?.lastName     //Фамилия
        manager.sex = zft.manager?.sex?.title             //Пол
        manager.cityPhoneNumber = zft.manager?.cityPhoneNumber
        //Рабочий телефон
        manager.email = zft.manager?.email     //Адрес электронной почты
        manager.otherAdditionalEmail = zft?.manager?.otherAdditionalEmail
        manager.additionalEmail = zft.manager?.additionalEmail
        //Дополнительный адрес электронной почты
        manager.post = zft.manager?.post    //Должность
        manager.passportData = zft.manager?.passportData     //Паспортные данные
        manager.address = zft.manager?.address     //Адрес
        manager.personInn = zft.manager?.personInn     //ИНН
        manager.employee = zft.manager?.employee     //Выбранная персона
        manager.displayableTitle = zft.manager?.displayableTitle     //ФИО
        zftParams.manager = manager

    } else
    {
        zftParams.authorType = "Физическое лицо"
        Person person = new Person()
        person.firstName = zft.authorPerson?.firstName     //Имя
        person.middleName = zft.authorPerson?.middleName     //Отчество
        person.lastName = zft.authorPerson?.lastName     //Фамилия
        person.sex = zft.authorPerson?.sex?.title             //Пол
        person.cityPhoneNumber = zft.authorPerson?.cityPhoneNumber
        //Рабочий телефон
        person.email = zft.authorPerson?.email     //Адрес электронной почты
        person.additionalEmail = zft.authorPerson?.additionalEmail
        //Дополнительный адрес электронной почты
        person.post = zft.authorPerson?.post    //Должность
        person.passportData = zft.authorPerson?.passportData
        //Паспортные данные
        person.address = zft.authorPerson?.address     //Адрес
        person.personInn = zft.authorPerson?.personInn     //ИНН
        person.employee = zft.authorPerson?.employee     //Выбранная персона
        person.displayableTitle = zft.authorPerson?.displayableTitle     //ФИО
        zftParams.authorPerson = person
    }

    zftParams.generalInfo = generalInfo

//Вкладка "Сведения о заявителе и исполнителях"
    //Блок "Финансовый оборот организации заявителя"
    AuthorOrgInfo authorOrgInfo = new AuthorOrgInfo()
    authorOrgInfo.financialTurnoverAuthorOrgYear1 = zft.requirementProperties.financialTurnoverAuthorOrgYear3
    authorOrgInfo.financialTurnoverAuthorOrgYear2 = zft.requirementProperties.financialTurnoverAuthorOrgYear2
    authorOrgInfo.financialTurnoverAuthorOrgYear3 = zft.requirementProperties.financialTurnoverAuthorOrgYear1
    authorOrgInfo.financialTurnoverAuthorOrgYearTotal = zft.requirementProperties.financialTurnoverAuthorOrgYear1 +
            zft.requirementProperties.financialTurnoverAuthorOrgYear2 + zft.requirementProperties.financialTurnoverAuthorOrgYear3
    //Блок "Среднесписочная численность персонала организации заявителя"
    authorOrgInfo.employeeQuantityYear1 = zft.requirementProperties.employeeQuantityYear1
    authorOrgInfo.employeeQuantityYear2 = zft.requirementProperties.employeeQuantityYear2
    authorOrgInfo.employeeQuantityYear3 = zft.requirementProperties.employeeQuantityYear3
    authorOrgInfo.employeeQuantityYear4 = zft.requirementProperties.employeeQuantityYear4
    authorOrgInfo.employeeQuantityAll = zft.requirementProperties.employeeQuantityYear1 +
            zft.requirementProperties.employeeQuantityYear2 +
            zft.requirementProperties.employeeQuantityYear3 +
            zft.requirementProperties.employeeQuantityYear4

    //Блок "Потенциальные организации-участники"
    def executors = []
    for (def item in zft.executors.toArray())
    {
        AuthorOrg executor = new AuthorOrg()
        executor.title = item?.title
        executor.shortTitle = item?.shortTitle
        executor.inn = item?.inn
        executor.isBranch = item?.isBranch ? "Да" : "Нет"
        executor.orgType = item?.orgType
        executor.orgRole = item?.orgRole
        executor.orgRoles = getStringFromList(item?.orgRoles)
        executor.orgRoleOther = item?.orgRoleOther
        executor.organization = item?.organization?.title
        //authorOrganization.declarantContributionSet = zft.authorOrg?.declarantContributionSet
        executor.declarantContributionOther = item?.declarantContributionOther
        //authorOrganization.juridicalAddress = zft.authorOrg?.juridicalAddress
        executor.juridicalAddressDraft = item?.juridicalAddressDraft
        executor.authorEquals = item?.authorEquals
        executors.add(executor)
    }
    authorOrgInfo.executors = executors

    // Блок "Опыт проведения научных конференций (школ)"
    def conferenceExperienesList = []
    if (zft.getDraftDocument() != null)
    {
        conferenceExperienesList = new RequirementConferenceExperienceTLC().
                listConferenceExperience(zft.getDraftDocument()?.getDraft(), session)
    }
    def conferencies = []
    for (def item in conferenceExperienesList)
    {
        RequirementConferenceExperience conference = new RequirementConferenceExperience()
        conference.conferenceTitle = item.conferenceTitle
        conference.conferenceLocation = item.conferenceLocation
        conference.conferenceDates = item.conferenceDates
        conference.organizerOfConference = item.organizerOfConference
        conference.numberOfParticipants = item.numberOfParticipants
        conference.conferenceFrequency = item.conferenceFrequency ? "Да" : "Нет"
        conferencies.add(conference)
    }
    authorOrgInfo.conferencies = conferencies
    authorOrgInfo.conferenciesTotal = conferencies.size()

    //Блок "В целях выполнения работ по проекту может быть образован консорциум"
    authorOrgInfo.consortiumType = zft.requirementProperties.consortiumType?.title
    zftParams.authorOrgInfo = authorOrgInfo

//Вкладка "Ожидаемые результаты"
    ExpectedResults expectedResults = new ExpectedResults()
    //Блок Наименование планируемого (ожидаемого) результата НИОКР, работы (услуги)
    def requirementWorkResultPropertiesList = []
    if(zft.getDraftDocument()!=null)
    {
        for (def item in zft.getDraftDocument()?.getDraft().listWorkResultProperties(session))
        {
            RequirementWorkResultProperties resultPropertiesEntity = new RequirementWorkResultProperties()
            resultPropertiesEntity.workResultName = item.workResultName?.title
            resultPropertiesEntity.workResultDescription = item.workResultDescription
            resultPropertiesEntity.noveltyOfFundamentallyNewResult = item.noveltyOfFundamentallyNewResult?.title
            resultPropertiesEntity.improvementOfWellKnownResults = item.improvementOfWellKnownResults?.title
            resultPropertiesEntity.improvementOfWellKnownResultsOther = item.improvementOfWellKnownResultsOther
            resultPropertiesEntity.analogueResult = item.analogueResult
            resultPropertiesEntity.mainScientificCharacteristics = item.mainScientificCharacteristics
            resultPropertiesEntity.analogueDescription = item.analogueDescription
            resultPropertiesEntity.competitiveness = item.competitiveness?.title
            resultPropertiesEntity.commercializationPossibility = item.commercializationPossibility?.title
            resultPropertiesEntity.commercializationDate = item.commercializationDate?.title
            resultPropertiesEntity.paybackDate = item.paybackDate?.title
            resultPropertiesEntity.competitivenessSavePeriod = item.competitivenessSavePeriod?.title
            requirementWorkResultPropertiesList.add(resultPropertiesEntity)
        }
    }
    expectedResults.requirementWorkResultPropertiesList = requirementWorkResultPropertiesList

    //Блок 'Новизна планируемого (ожидаемого) результата'
    expectedResults.uniqueResult = zft.requirementProperties?.uniqueResult?.title

    //Блок Научно-технический уровень ожидаемого результата
    ScientificLevelResult scientificLevelResult = new ScientificLevelResult()
    scientificLevelResult.scientificAndTechnicalLevelOfComingResult = zft.requirementProperties.scientificAndTechnicalLevelOfComingResult?.title
    scientificLevelResult.worldLevelPublication = zft.requirementProperties.worldLevelPublication
    scientificLevelResult.executorPublication = zft.requirementProperties.executorPublication
    scientificLevelResult.projectKeyword = zft.requirementProperties.projectKeyword
    scientificLevelResult.worldLevelLicence = zft.requirementProperties.worldLevelLicence
    scientificLevelResult.authorLicence = zft.requirementProperties.authorLicence
    scientificLevelResult.protectedResult = zft.requirementProperties.protectedResult
    expectedResults.scientificLevelResult = scientificLevelResult

    //Блок "Формулировка конечного продукта, в котором предполагается использование планируемого (ожидаемого) результата"
    expectedResults.definitionEndResult = zft.requirementProperties.definitionEndResult

    //Блок "Наименование отрасли экономики, в которой может быть применен планируемый (ожидаемый) результат"
    expectedResults.economicFieldNameSet = getStringFromList(zft.requirementProperties.economicFieldNameSet)
    //Ожидаемый социально-экономический эффект использования планируемого (ожидаемого) результата
    expectedResults.forthcomingSocialAndEconomicalEffectSet = getStringFromList(zft.requirementProperties.forthcomingSocialAndEconomicalEffectSet)
    expectedResults.forthcomingSocialAndEconomicalEffectOther = zft.requirementProperties.forthcomingSocialAndEconomicalEffectOther
    // Основные направления дальнейшего использования планируемого (ожидаемого) результата
    expectedResults.resultMainLineSet = getStringFromList(zft.requirementProperties.resultMainLineSet)
    expectedResults.resultMainLineOther = zft.requirementProperties.resultMainLineOther

    zftParams.expectedResults = expectedResults

//Вкладка "Обоснование проекта"
    //Блок Степень проработки и обеспеченности проекта
    EditPreparationDegree editPreparationDegree = new EditPreparationDegree()
    editPreparationDegree.scientificAndTechnicalStartsSet = getStringFromList(zft.requirementProperties.scientificAndTechnicalStartsSet)
    editPreparationDegree.scientificAndTechnicalStartsOther = zft.requirementProperties.scientificAndTechnicalStartsOther
    editPreparationDegree.trainedPotentialSet = getStringFromList(zft.requirementProperties.trainedPotentialSet)
    editPreparationDegree.trainedPotentialOther = zft.requirementProperties.trainedPotentialOther
    editPreparationDegree.supplyMachineryAndEquipmentSet = getStringFromList(zft.requirementProperties.supplyMachineryAndEquipmentSet)
    editPreparationDegree.experienceOfImplementingSimilarProjectsSet = getStringFromList(zft.requirementProperties.experienceOfImplementingSimilarProjectsSet)
    editPreparationDegree.neededInUsingUniqueTestBenches = zft.requirementProperties.neededInUsingUniqueTestBenches
    editPreparationDegree.neededInUsingScientificEquipment = zft.requirementProperties.neededInUsingScientificEquipment
    zftParams.editPreparationDegree = editPreparationDegree

    // Блок "Виды основных работ по проекту. НИОКР, Работы(услуги)"
    costs = zft.requirementProperties.costBasisFarma

    def costBasisEntityList = []
    def stageName
    def currentStageUUID = ""
    def totalCost = 0
    for (def entity in costs)
    {
        CostBasisEntity costBasisEntity = new CostBasisEntity()
        if(!currentStageUUID.equals(entity.stageRow)){ //Признак нового этапа
            currentStageUUID = entity.stageRow
            stageName = entity.title?:(entity.stage.title)
        }
        else{
            costBasisEntity.cost = entity.cost
            costBasisEntity.subStageName = entity.costBasisCatalogItem?.title?:(entity.title?:(entity.stage.title))
            costBasisEntity.manday = entity.manday
            totalCost+=entity.cost
        }
        costBasisEntity.stageName = stageName
        costBasisEntityList.add(costBasisEntity)
    }
    report.vars.costBasisList = costBasisEntityList
    report.vars.totalCost = totalCost

    // Блок Состав исполнителей (команда), непосредственно занятых в проекте
    report.vars.executorsTeam = getDecimalBoxCells(zft, "projectLineup", "ProjectLineup")
    //Блок Объемы бюджетного финансирования проекта по статьям затрат, руб.
    report.vars.budgetFinancing = getDecimalBoxCells(zft, "projectBudgetFin", "BudgetFin")
    //Объем новой и усовершенствованной продукции, произведенной за счет коммерциализации передовых технологий (после окончания проекта)
    report.vars.commercializatioinTechnologies = getDecimalBoxCells(zft, "projectNewestProduction", "ProjectNewestProduction")

    //Вкладка "Обоснование проекта" Остальные блоки
    ProjectJustificationBlocks justificationBlocks = new ProjectJustificationBlocks()
    justificationBlocks.fiveTimesIncreasing = zft.requirementProperties.fiveTimesIncreasing?.title
    justificationBlocks.technologicalPossibilitiesInConsumptionArea = zft.requirementProperties.technologicalPossibilitiesInConsumptionArea?.title
    justificationBlocks.technologicalPossibilitiesInProductionArea = zft.requirementProperties.technologicalPossibilitiesInProductionArea?.title
    justificationBlocks.inConsumptionArea = zft.requirementProperties.inConsumptionArea?.title
    justificationBlocks.inProductionArea = zft.requirementProperties.inProductionArea?.title
    ProductDirectives directives = new ProductDirectives()
    directives.productMarketSet = getStringFromList(zft.requirementProperties.productMarketSet)
    directives.demandConditionalitySet = getStringFromList(zft.requirementProperties.demandConditionalitySet)
    directives.demandTendency = zft.requirementProperties.demandTendency?.title
    directives.demandTendencyOther = zft.requirementProperties.demandTendencyOther
    directives.possibilityOfImportReplacement = zft.requirementProperties.possibilityOfImportReplacement?.title
    directives.exportPotentialSet = getStringFromList(zft.requirementProperties.exportPotentialSet)
    justificationBlocks.productDirectives = directives
    justificationBlocks.additionalConditionsSet = getStringFromList(zft.requirementProperties.additionalConditionsSet)
    justificationBlocks.additionalConditionsOther = zft.requirementProperties.additionalConditionsOther
    justificationBlocks.productionPrerequisites = zft.requirementProperties.productionPrerequisites?.title
    justificationBlocks.productionPrerequisitesOther = zft.requirementProperties.productionPrerequisitesOther
    zftParams.projectJustificationBlocks = justificationBlocks

//Вкладка "Другие данные"
    //Блок Целесообразность выполнения проекта отечественными исполнителями
    DomesticExecutor domesticExecutor = new DomesticExecutor()
    domesticExecutor.domesticExecutorsFactors = getStringFromList(zft.requirementProperties.expediencyOfImplementingProjectByNativeExecutorsSet)
    domesticExecutor.domesticExecutorsFactorsOther = zft.requirementProperties.otherFactors
    zftParams.domesticExecutor = domesticExecutor

    //Блок Оценка перспектив международного сотрудничества при выполнении проекта
    InternationalCooperationAcpectsEstimate acpect = new InternationalCooperationAcpectsEstimate()
    acpect.expediencyOfInternationalCollaboration = zft.requirementProperties.expediencyOfInternationalCollaboration?.title
    acpect.internationalCollaborationAimsSet = getStringFromList(zft.requirementProperties.internationalCollaborationAimsSet)
    acpect.internationalCollaborationAimsOther = zft.requirementProperties.internationalCollaborationAimsOther
    acpect.russianParticipantMotivationSet = getStringFromList(zft.requirementProperties.russianParticipantMotivationSet)

    acpect.russianParticipantMotivationOther = zft.requirementProperties.russianParticipantMotivationOther
    acpect.internationalCollaborationMarkSet = getStringFromList(zft.requirementProperties.internationalCollaborationMarkSet)
    acpect.internationalCollaborationMarkOther = zft.requirementProperties.internationalCollaborationMarkOther
    acpect.foreignPartnerCharacteristicsSet = getStringFromList(zft.requirementProperties.foreignPartnerCharacteristicsSet)

    acpect.foreignPartnerCharacteristicsOther = zft.requirementProperties.foreignPartnerCharacteristicsOther
    acpect.ensuringParity = zft.requirementProperties.ensuringParity?.title
    acpect.ensuringParityOther = zft.requirementProperties.ensuringParityOther
    acpect.reasonsForInternationalProject = zft.requirementProperties.reasonsForInternationalProject?.title

    acpect.reasonsForInternationalProjectOther = zft.requirementProperties.reasonsForInternationalProjectOther
    zftParams.internationalCooperationAcpectsEstimate = acpect

    report.vars.zftParams = zftParams
}

def getCatalogItemByAction(paId)
{
    for (item in helper.select("from ProgramActionBlockCatalogItem"))
    {
        if (item.programAction?.UUID.equals(paId))
        {
            return item
        }
    }
}

def getStringFromList(properiesList)
{
    def propertiesAsString = ""
    for (def item in properiesList)
    {
        propertiesAsString <<= "- " << item.title << "\n"
    }
    return propertiesAsString.toString()
}

def getDraftFinanceBoxCells(def zft, def boxesCode)
{
    def cellsList = []
    if (BoxHibernateHandler.getBoxWithCode(zft, boxesCode, session) != null)
    {
        def boxCells = BoxHibernateHandler.getBoxWithCode(zft, boxesCode, session).cells.toArray()
        for (item in boxCells)
        {
            cellsList.add(new FinanceCell(
                    ordinalYear: item.getCellCoordinate().get("OrdinalYear"),
                    workPriceRequirementDraft: item.getCellCoordinate().get("workPriceRequirementDraft"),
                    value: item.value))
        }
    }
    return cellsList
}

def getDecimalBoxCells(def zft, def boxName, def className)
{
    def decCells = []
    if (BoxHibernateHandler.getBoxWithCode(zft, boxName, session) != null)
    {
        def cells = BoxHibernateHandler.getBoxWithCode(zft, boxName, session).cells.toArray()
        for (def cell in cells)
        {
            def decimalCell = this.class.classLoader.loadClass(className, true, false)?.newInstance()
            coordinate = cell.coordinates.toString()
            def properties = decimalCell.getPropertiesNames()
            for (def item in StringUtils.split(coordinate, ";"))
            {
                if (StringUtils.split(item, ":")[0].equals(properties[0]))
                {
                    def property = properties[0]
                    decimalCell.@"$property" = StringUtils.split(item, ":")[1]
                    continue
                }
                if (StringUtils.split(item, ":")[0].equals(properties[1]))
                {
                    def property = properties[1]
                    decimalCell.@"$property" = StringUtils.split(item, ":")[1]
                    continue
                }
            }
            decimalCell.value = cell.value
            decCells.add(decimalCell)
        }
    }
    return decCells
}

class CostBasisEntity
{
    def stageName
    def subStageName
    def manday
    def cost
}

class FinanceCell
{
    String ordinalYear
    String workPriceRequirementDraft
    BigDecimal value
}

class ProjectLineup
{
    def projectLineupX
    def projectLineupY
    def value

    def getPropertiesNames()
    {
        return ["projectLineupX", "projectLineupY"]
    }
}

class BudgetFin
{
    def value
    def projectBudgetFinX
    def projectBudgetFinY

    def getPropertiesNames()
    {
        return ["projectBudgetFinX", "projectBudgetFinY"]
    }
}

class ProjectNewestProduction
{
    def value
    def projectNewestProductionX
    def projectNewestProductionY

    def getPropertiesNames()
    {
        return ["projectNewestProductionX", "projectNewestProductionY"]
    }
}

class ZftParams
{
    def generalInfo
    def requirementProperties
    def authorType
    def authorOrg
    def authorPerson
    def manager
    def authorOrgInfo
    def editPreparationDegree
    def expectedResults
    def projectJustificationBlocks
    def domesticExecutor
    def internationalCooperationAcpectsEstimate
}

class GeneralInfo
{
    def program
    def programAction
    def programActionIdentifier
    def demandIdentifier
    def demandTitle
    def technologicalPlatform
    def workKinds
    def projectAim
    def extraResearchAreaSet
    def modernLinesSet
    def modernLine
    def criticalTechSet
    def criticalTech
}

class RequirementProperties
{
    def projectJustification
    def pharmaDescriptionTypeTitle
    def entranceRequirements
    def mainEducationPrograms
    def moduleExpidiency
    def moduleVolume
    def practiceAreaMastered
    def plannedCompetenceProgram
    def potentialEmployersMastered
    def educationProgram
    def trainingDirection
    def educationStandard
    def programVolume
    def practiceArea
    def plannedCompetenceModule
    def implModularFormat
    def implNetworkFormat
    def potentialEmployers
    def projectExpectedResult
}

class RequirementConferenceExperience
{
    def conferenceTitle
    def conferenceLocation
    def conferenceDates
    def organizerOfConference
    def numberOfParticipants
    def conferenceFrequency
}

class RequirementWorkResultProperties
{
    def workResultName     //Наименование результата
    def workResultDescription     //Формулировка результата
    def noveltyOfFundamentallyNewResult
    //Может быть получен принципиально новый результат
    def improvementOfWellKnownResults
    //Планируемый результат возможно будет являться усовершенствованием существующих (известных) результатов
    def improvementOfWellKnownResultsOther     //Другое
    def analogueResult     //Сведения об аналогах планируемого результата
    def mainScientificCharacteristics
    //Значения характеристик, которые могут быть достигнуты в результате выполнения проекта
    def analogueDescription     //Значения характеристик аналогов
    def competitiveness
    //Конкурентоспособность планируемого (ожидаемого) результата
    def commercializationPossibility     //Возможность коммерциализации
    def commercializationDate     //Срок коммерциализации
    def paybackDate     //Срок окупаемости бюджетных средств
    def competitivenessSavePeriod     //Период сохранения конкурентоспособности
}

class AuthorOrg
{
    def title
    def shortTitle
    def inn
    def isBranch
    def orgType
    def orgRole
    def orgRoles
    def orgRoleOther
    def declarantContributions
    def declarantContributionSet
    def declarantContributionOther
    def juridicalAddress
    def juridicalAddressDraft
    def authorEquals
    def organization //Выбранная организация
}

class Person
{
    def firstName     //Имя
    def middleName     //Отчество
    def lastName     //Фамилия
    def sex             //Пол
    def cityPhoneNumber     //Рабочий телефон
    def email     //Адрес электронной почты
    def additionalEmail     //Дополнительный адрес электронной почты
    def otherAdditionalEmail //
    def post     //Должность
    def passportData     //Паспортные данные
    def address     //Адрес
    def personInn     //ИНН
    def employee     //Выбранная персона
    def displayableTitle     //ФИО
}

//Вкладка сведения о заявителе
class AuthorOrgInfo
{
    //Блок 'Финансовый оборот организации заявителя'
    def financialTurnoverAuthorOrgYear1
    def financialTurnoverAuthorOrgYear2
    def financialTurnoverAuthorOrgYear3
    def financialTurnoverAuthorOrgYearTotal
    def employeeQuantityYear1
    def employeeQuantityYear2
    def employeeQuantityYear3
    def employeeQuantityYear4
    def employeeQuantityAll
    def executors = []
    def conferencies = []
    def consortiumType
    def conferenciesTotal
}

class EditPreparationDegree
{
    def scientificAndTechnicalStartsSet
    def scientificAndTechnicalStartsOther
    def trainedPotentialSet
    def trainedPotentialOther
    def supplyMachineryAndEquipmentSet
    def experienceOfImplementingSimilarProjectsSet
    def neededInUsingUniqueTestBenches
    def neededInUsingScientificEquipment
}

class WorkKinds
{
    def researchEffort    //'Научно-исследовательская работа',
    def experimentalDevelopment
    //'requirementProperties.experimentalDevelopment' : 'Опытно-конструкторские работы',
    def technologicalDevelopment
    //'requirementProperties.experimentalDevelopment' : 'Опытно-технологическая работа',
    def conferenceOrSchool
    def productionHiTech
    //'requirementProperties.developmentManagement'   : 'Организация производства высокотехнологичной продукции',
    def production
    //'requirementProperties.production'              : 'Производство',
    def conferences
    //'requirementProperties.conferenceSet'           : 'Конференция/школа-семинар/выставка',
    def works
    //'description'                                   : 'Формулировка цели предлагаемого к реализации проекта',
}

class ProjectAim
{
    def taskContent
    def description
}

class InternationalCooperationAcpectsEstimate
{
    def expediencyOfInternationalCollaboration
    def internationalCollaborationAimsSet
    def internationalCollaborationAimsOther
    def russianParticipantMotivationSet
    def russianParticipantMotivationOther
    def internationalCollaborationMarkSet
    def internationalCollaborationMarkOther
    def foreignPartnerCharacteristicsSet
    def foreignPartnerCharacteristicsOther
    def ensuringParity
    def ensuringParityOther
    def reasonsForInternationalProject
    def reasonsForInternationalProjectOther
}

class DomesticExecutor
{
    def domesticExecutorsFactors
    def domesticExecutorsFactorsOther
}

class ProjectJustificationBlocks
{
    def fiveTimesIncreasing
    def technologicalPossibilitiesInConsumptionArea
    def technologicalPossibilitiesInProductionArea
    def inConsumptionArea                           //Экологические аспекты ...
    def inProductionArea
    def productDirectives
    def additionalConditionsSet
    def additionalConditionsOther
    def productionPrerequisites
    def productionPrerequisitesOther
}

class ExpectedResults //вкладка ожидаемые результаты
{
    //Наименование планируемого (ожидаемого) результата НИОКР, работы (услуги)
    def requirementWorkResultPropertiesList = []
    //Блок "Новизна планируемого (ожидаемого) результата"
    def uniqueResult
    def scientificLevelResult
    //Блок "Научно-технический уровень ожидаемого результата"
    // Блок Формулировка конечного продукта, в котором предполагается использование планируемого (ожидаемого) результата
    def definitionEndResult
    //Наименование отрасли экономики, в которой может быть применен планируемый (ожидаемый) результат
    def economicFieldNameSet
    //Ожидаемый социально-экономический эффект использования планируемого (ожидаемого) результата
    def forthcomingSocialAndEconomicalEffectSet
    def forthcomingSocialAndEconomicalEffectOther
    //Основные направления дальнейшего использования планируемого (ожидаемого) результата
    def resultMainLineSet
    def resultMainLineOther
}

class ScientificLevelResult
{
    def scientificAndTechnicalLevelOfComingResult
    def worldLevelPublication
    def executorPublication
    def projectKeyword
    def worldLevelLicence
    def authorLicence
    def protectedResult
}

class ProductDirectives   //Вкладка "Обоснование проекта" блок "Оцена рыночного потенциала"
{
    def productMarketSet  //Рынок, на котором планируется реализация продукции
    def demandConditionalitySet
    //Обусловленность спроса на разрабатываемую продукцию
    def demandTendency //Тенденция изменения спроса на разрабатываемую продукцию
    def demandTendencyOther
    //Тенденция изменения спроса на разрабатываемую продукцию (Возможны другие варианты)
    def possibilityOfImportReplacement
    //Возможность импортозамещения ранее используемой продукции аналогичного назначения
    def exportPotentialSet //Экспортный потенциал разрабатываемой продукции
}