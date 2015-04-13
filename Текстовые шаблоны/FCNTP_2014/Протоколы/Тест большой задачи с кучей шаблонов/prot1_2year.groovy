import org.apache.commons.lang3.StringUtils
import ru.naumen.fcntp.bobject.demand.DemandFcntpHibernateHandler
import ru.naumen.common.utils.StringUtilities
import ru.naumen.fcntp.bobject.lot.LotFcntpHibernateHandler

/**
 * ayakovlev
 * 27.03.2014
 * L-18274 IR. Создание БИРТ шаблона протокол 1 (субсидия)
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/18274
 */

//def notice = helper.get('noticefs000080000i95mp0u2mseg8kg') //извещение сервер 19080 FCNTP-444 с несколькими заполненными полями ранга заявки
def notice = object
//def notice = helper.get('noticefs000080000kc6cmpfr5hugbek')
def htmlString = "<SPAN STYLE='BACKGROUND-COLOR: yellow'>не заполнено</SPAN>"

report.vars.noticeIdentifier = notice.identifier
report.vars.protocolNumber = notice.protocolNumber
report.vars.protocolNumberSummation = notice.protocolNumberSummation
report.vars.competitiveCommission = notice.competitiveCommission?.fullNumber
report.vars.displayableIdentifier = notice.programAction?.displayableIdentifier
report.vars.queueNumber = notice.queueNumber
report.vars.titleEdit = notice.titleEdit
report.vars.openEnvelopesDate = notice.openEnvelopesDate
report.vars.publicationDate = notice.publicationDate
report.vars.openEnvelopesPlace = notice.openEnvelopesPlace
report.vars.resultPlace = notice.resultPlace
report.vars.resultDate = notice.resultDate

report.vars.commissionMembersP1Set = StringUtilities.join(notice.commissionMembersP1Set.title, "\n")
report.vars.commissionMembersP2Set = StringUtilities.join(notice.commissionMembersP2Set.title, "\n")
report.vars.commissionMembersP3Set = StringUtilities.join(notice.commissionMembersP3Set.title, "\n")
report.vars.numberCommissionMembersP1Set = notice.commissionMembersP1Set.size()
report.vars.numberCommissionMembersP2Set = notice.commissionMembersP2Set.size()
report.vars.numberCommissionMembersP3Set = notice.commissionMembersP3Set.size()
report.vars.commissionMemberPresentPercentP1Set = notice.getCommissionMemberPresentPercent(notice.commissionMembersP1)
report.vars.commissionMemberPresentPercentP2Set = notice.getCommissionMemberPresentPercent(notice.commissionMembersP2)
report.vars.commissionMemberPresentPercentP3Set = notice.getCommissionMemberPresentPercent(notice.commissionMembersP3)
report.vars.considerationDate = notice.considerationDate
report.vars.protocolViewDemandOpenDate = notice.protocolViewDemandOpenDate
report.vars.considerationPlace = notice.considerationPlace
report.vars.protocolViewDemandNumber = notice.protocolViewDemandNumber

def openingProtocolLotList = []

def demandsToXML = []

def lots = notice.getLots()

report.vars.lotFullNumber = lots[0]?.fullNumber
report.vars.leadingSubPriorityLine = lots[0]?.leadingSubPriorityLine
report.vars.lotTheme = lots[0]?.theme
report.vars.lotUmbrellaType = lots[0]?.umbrellaType
report.vars.planingLotsCount = lots[0]?.contractNumberInLot

def years = ""
if(lots.size()>0){
    for(int i = getYear(lots[0].datePlannedWorkStart); i<getYear(lots[0].datePlannedWorkEnd)+1; i++){
        years <<= i.toString() << ";"
    }
}
report.vars.lotYears = years


lots.each() {
    lot ->
        openingProtocolLotList.add(getOpeningProtocolLot(lot))

        demands = DemandFcntpHibernateHandler.listAcceptedAndWinnerDemands(lot)
        demands.addAll(DemandFcntpHibernateHandler.listRejectedDemands(lot))

        demands.each() {
            demand ->

                StringBuilder lotString = new StringBuilder()
                lotString
                        .append("Лот ")
                        .append(lot.inOfferNumber == null ? htmlString : lot.inOfferNumber)
                        .append(". № ")
                        .append(lot.fullNumber == null ? htmlString : lot.fullNumber)
                        .append(lot.theme == null ? htmlString : lot.theme)
                        .append(". Плановый срок выполнения работ ")
                        .append(lot.lotPeriod == null ? htmlString : lot.lotPeriod)
                        .append(" дней. Максимальная стоимость контракта ")
                        .append(lot.getFinance(null, 'budgetRF', null) == null ? htmlString : lot.getFinance(null, 'budgetRF', null))

                OpeningProtocolDemand openingProtocolDemand = new OpeningProtocolDemand()

                openingProtocolDemand.lotString = lotString
                openingProtocolDemand.state = demand.currentStage?.number
                openingProtocolDemand.viewNumber = demand.viewNumber
                openingProtocolDemand.topic = demand.topic
                openingProtocolDemand.executor = demand.executor?.title
                openingProtocolDemand.postAddress = getPostAddressWithIndexOnTheFirstPlace(demand.executor?.postAddress)
                openingProtocolDemand.financeTotal = demand.getFinance(null, 'budgetRF', null) / 1000000
                openingProtocolDemand.finance2014 = demand.getFinance(null, 'budgetRF', 2014) / 1000000
                openingProtocolDemand.finance2015 = demand.getFinance(null, 'budgetRF', 2015) / 1000000
                openingProtocolDemand.finance2016 = demand.getFinance(null, 'budgetRF', 2016) / 1000000
                openingProtocolDemand.finance2017 = demand.getFinance(null,'budgetRF', 2017) / 1000000
                openingProtocolDemand.finance2018 = demand.getFinance(null,'budgetRF', 2018) / 1000000
                openingProtocolDemand.finance2019 = demand.getFinance(null,'budgetRF', 2019) / 1000000
                openingProtocolDemand.finance2020 = demand.getFinance(null,'budgetRF', 2020) / 1000000
                openingProtocolDemand.accessedDecisionSubstantiation = demand.accessedDecisionSubstantiation
                def reqlist = LotFcntpHibernateHandler.listDemandCMMarksByDemand(lot, demand)
                def totalMark = 0
                reqlist.each() {
                    req ->
                        totalMark += req.getTotal()
                }
                if (reqlist.size() != 0) {
                    openingProtocolDemand.demandTotalRating = totalMark / reqlist.size()
                }
                def displayDemandsCount = 1
                if (lot.umbrellaType) {
                    if (lot.marginalProjectsCost != null) {
                        displayDemandsCount = 30;
                    } else {
                        displayDemandsCount = lot.contractNumberInLot
                    }
                }
                openingProtocolDemand.displayCount = displayDemandsCount;
                def portalNumber = demand.portalNumber
                if (portalNumber != null) {
                    openingProtocolDemand.portalNumber = portalNumber?.substring(portalNumber?.length() - 4, portalNumber?.length())
                }
                openingProtocolDemand.inOfferNumber = lot.inOfferNumber
                openingProtocolDemand.currentStage=demand.currentStage.number
                demandsToXML.add(openingProtocolDemand)
        }
}

report.vars.lots = openingProtocolLotList
report.vars.demands = demandsToXML

def getPostAddressWithIndexOnTheFirstPlace(def address) {
    if (address == null)
        return ""

    StringBuffer fullTitle = new StringBuffer()

    def resAddress = []

    if (!StringUtils.isEmpty(address.postalCode))
        resAddress.add(address.postalCode)

    if (address.federalRegion != null)
        resAddress.add(address.federalRegion.displayableTitle)

    if (address.region != null)
        resAddress.add(address.region.displayableTitle)

    if (address.city != null)
        resAddress.add(address.city.displayableTitle)

    if (address.street != null)
        resAddress.add(address.street.displayableTitle)

    if (!StringUtils.isEmpty(address.houseNumber))
        resAddress.add("дом ".concat(address.houseNumber))

    if (!StringUtils.isEmpty(address.houseUnitNumber))
        resAddress.add("корпус ".concat(address.houseUnitNumber))

    if (!StringUtils.isEmpty(address.flatNumber))
        resAddress.add("офис ".concat(address.flatNumber))

    if (!StringUtils.isEmpty(address.additionalInformation))
        resAddress.add(address.additionalInformation);

    for (Iterator<String> iter = resAddress.iterator(); iter.hasNext();) {
        fullTitle.append(iter.next());
        fullTitle.append(iter.hasNext() ? ",  " : StringUtils.EMPTY);
    }

    fullTitle.toString();
}

def getOpeningProtocolLot(def lot) {
    def openingProtocolLot = new OpeningProtocolLot()
    openingProtocolLot.lotFullNumber = lot.fullNumber
    openingProtocolLot.leadingSubPriorityLine = lot.leadingSubPriorityLine
    openingProtocolLot.lotTheme = lot.theme
    openingProtocolLot.lotInOfferNumber = lot.inOfferNumber
    openingProtocolLot
}

def getYear(def date){
    Calendar calendar = Calendar.getInstance()
    calendar.setTime(new Date(date.getTime()))
    return calendar.get(Calendar.YEAR)
}

class OpeningProtocolLot {
    def lotFullNumber
    def leadingSubPriorityLine
    def lotTheme
    def lotInOfferNumber
}

class OpeningProtocolDemand {
    def state
    def viewNumber
    def topic
    def executor
    def postAddress
    def financeTotal
    def finance2014
    def finance2015
    def finance2016
    def finance2017
    def finance2018
    def finance2019
    def finance2020
    def lotString
    def accessedDecisionSubstantiation
    def portalNumber
    def demandTotalRating
    def displayCount
    def inOfferNumber
    def currentStage
}