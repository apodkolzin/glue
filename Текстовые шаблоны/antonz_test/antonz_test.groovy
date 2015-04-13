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

//def notice = helper.get('noticefs000080000k8fjumcb6ijohms')
//def notice = helper.get('noticefs000080000idf2061fg3vffe0') // извещение с не пустым полем scienceAndEconomicOrAvgMarkSum в заявках
//def notice = helper.get('noticeo2k02ho0000i6io0s91fkqasig')
def notice = helper.get('noticefs000080000jkrhtc2on2vho64') //извещение сервер 19080 FCNTP-444 с несколькими заполненными полями ранга заявки
//def notice = object
def htmlString = "<SPAN STYLE='BACKGROUND-COLOR: yellow'>не заполнено</SPAN>"

report.vars.noticeIdentifier = notice.identifier
report.vars.protocolNumber = notice.protocolNumber
report.vars.protocolNumberSummation = notice.protocolNumberSummation
report.vars.competitiveCommission = notice.competitiveCommission?.fullNumber
report.vars.displayableIdentifier = notice.programAction.displayableIdentifier
report.vars.queueNumber = notice.queueNumber
report.vars.titleEdit = notice.titleEdit
report.vars.openEnvelopesDate = notice.openEnvelopesDate
report.vars.publicationDate = notice.publicationDate
report.vars.openEnvelopesPlace = notice.openEnvelopesPlace

report.vars.commissionMembersP1Set = StringUtilities.join(notice.commissionMembersP1Set.title, "\n")
report.vars.commissionMembersP2Set = StringUtilities.join(notice.commissionMembersP2Set.title, "\n")
report.vars.commissionMembersP3Set = StringUtilities.join(notice.commissionMembersP3Set.title, "\n")
report.vars.numberCommissionMembers = notice.commissionMembersP1Set.size()
report.vars.commissionMemberPresentPercent = notice.getCommissionMemberPresentPercent(notice.commissionMembersP1)
report.vars.considerationDate = notice.considerationDate
report.vars.protocolViewDemandOpenDate = notice.protocolViewDemandOpenDate
report.vars.considerationPlace = notice.considerationPlace
report.vars.protocolViewDemandNumber= notice.protocolViewDemandNumber

def openingProtocolLotList = []

def demandsToXML = []

def lots = notice.getLots()

report.vars.lotFullNumber = lots[0]?.fullNumber
report.vars.leadingSubPriorityLine = lots[0]?.leadingSubPriorityLine
report.vars.lotTheme = lots[0]?.theme

lots.each(){
    lot->
        openingProtocolLotList.add(getOpeningProtocolLot(lot))

        def demands = []

        demandsAccepted = DemandFcntpHibernateHandler.listAcceptedDemands(lot)

        demandsRejected = DemandFcntpHibernateHandler.listRejectedDemands(lot)
        demands.addAll(demandsAccepted)
        demands.addAll(demandsRejected)

        demands.each(){
            demand->

                StringBuilder lotString = new StringBuilder()
                lotString
                        .append("Лот ")
                        .append(lot.inOfferNumber == null ? htmlString : lot.inOfferNumber)
                        .append(". № ")
                        .append(lot.fullNumber == null ? htmlString : lot.fullNumber)
                        .append("«")
                        .append(lot.theme == null ? htmlString : lot.theme)
                        .append("». Плановый срок выполнения работ ")
                        .append(lot.lotPeriod == null ? htmlString : lot.lotPeriod)
                        .append(" дней. Максимальная стоимость контракта ")
                        .append(lot.getFinance(null,'budgetRF',null) == null ? htmlString : lot.getFinance(null,'budgetRF',null))

                OpeningProtocolDemand openingProtocolDemand = new OpeningProtocolDemand()

                openingProtocolDemand.lotString = lotString

                openingProtocolDemand.state = demand.currentStage?.number
                openingProtocolDemand.viewNumber = demand.viewNumber
                openingProtocolDemand.topic = demand.topic
                openingProtocolDemand.executor = demand.executor?.title
                openingProtocolDemand.postAddress = getPostAddressWithIndexOnTheFirstPlace(demand.executor?.postAddress)
                openingProtocolDemand.financeTotal = demand.getFinance(null,'budgetRF',null)/1000000
                openingProtocolDemand.finance2014 = demand.getFinance(null,'budgetRF',2014)/1000000
                openingProtocolDemand.finance2015 = demand.getFinance(null,'budgetRF',2015)/1000000
                openingProtocolDemand.finance2016 = demand.getFinance(null,'budgetRF',2016)/1000000
                openingProtocolDemand.accessedDecisionSubstantiation = demand.accessedDecisionSubstantiation
                def demandIndex = LotFcntpHibernateHandler.listDemandCommissionMemberMarks(lot, session).collect {[it.demand]}.indexOf([demand])
                if(demandIndex!=-1) {
                    openingProtocolDemand.demandTotalRating = LotFcntpHibernateHandler.listDemandCommissionMemberMarks(lot, session)[demandIndex]?.getTotal();
                } else {
                    openingProtocolDemand.demandTotalRating = 0.0;
                }
                def portalNumber = demand.portalNumber
                if(portalNumber!=null){
                    openingProtocolDemand.portalNumber = portalNumber?.substring(portalNumber?.length() - 4, portalNumber?.length())
                }
                demandsToXML.add(openingProtocolDemand)
        }

}

report.vars.lots = openingProtocolLotList
report.vars.demands = demandsToXML

def getPostAddressWithIndexOnTheFirstPlace(def address)
{
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

    for (Iterator<String> iter = resAddress.iterator(); iter.hasNext();)
    {
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
    openingProtocolLot
}

class OpeningProtocolLot {
    def lotFullNumber
    def leadingSubPriorityLine
    def lotTheme
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
    def lotString
    def accessedDecisionSubstantiation
    def portalNumber
    def demandTotalRating
}