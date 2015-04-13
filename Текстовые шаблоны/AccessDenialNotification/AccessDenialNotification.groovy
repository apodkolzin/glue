/**
 * ayakovlev
 * L-17990 IR. уведомление о допуске\отказе
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/17990    
 * 
 */
import ru.naumen.fcntp.bobject.demand.DemandFcntpHibernateHandler

def lot = helper.get("corebofs000080000k57cf6k0d6ei00g")
//def lot = object


def acceptedDemands = DemandFcntpHibernateHandler.listAcceptedDemands(lot)
def rejectedDemands = DemandFcntpHibernateHandler.listRejectedDemands(lot)

def demands = []
demands.addAll(acceptedDemands)
demands.addAll(rejectedDemands)

report.vars.commission = lot.commission?.fullNumber
report.vars.inOfferNumber = lot.inOfferNumber
report.vars.fullNumber = lot.fullNumber
report.vars.theme = lot.theme
report.vars.protocolNumberViewDemand = lot.protocolNumberViewDemand
report.vars.dateViewDemand = lot.dateViewDemand

def demandsToXML = []
demands.each(){
    demand->
        AccessDenialNotificationDemand accessDenialNotificationDemand = new AccessDenialNotificationDemand()
        accessDenialNotificationDemand.executor = demand.executor?.title
        accessDenialNotificationDemand.workManager = demand.workManager?.title
        accessDenialNotificationDemand.postAddress = demand.executor?.postAddress?.fullTitle
        accessDenialNotificationDemand.viewNumber = demand.viewNumber
        accessDenialNotificationDemand.currentStage = demand.currentStage.identificator
        demandsToXML.add(accessDenialNotificationDemand)
}
report.vars.demands = demandsToXML
class AccessDenialNotificationDemand
{
    def executor
    def workManager
    def postAddress
    def viewNumber
    def currentStage
}