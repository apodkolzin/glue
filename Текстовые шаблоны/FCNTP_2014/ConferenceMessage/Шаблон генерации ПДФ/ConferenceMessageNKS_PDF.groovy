import ru.naumen.core.hibernate.HibernateUtil
import ru.naumen.wcf.engine.urls.URLCreator

/**
 * ayakovlev
 * L-17877 Создать БИРТ шаблон для ПДФ формы
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/17877    
 * */

//conference = helper.get("cnfrncfs000080000k4mna2ja5247oh0")
conference = object

String query = "select rel.lot from ConferenceLotRelation rel where rel.conference.id=:confid"
def lots = HibernateUtil.
    currentSession().
    createQuery(query).
    setParameter("confid", conference.UUID).
    list()
 
 def result = []
 lots.each()
 {
     lot->
         LotEntry lotEntry = new LotEntry()
         
         lotEntry.fullNumber = lot.fullNumber
         lotEntry.theme = lot.theme
         lotEntry.programAction = lot.programAction?.persistedDisplayableTitle
         lotEntry.leadingSubPriorityLine = lot.leadingSubPriorityLine?.title
         lotEntry.budgetRF2014 = lot.getFinance(null,'budgetRF',2014) == null ? "" : lot.getFinance(null,'budgetRF',2014)/1000000 
         lotEntry.budgetRF2015 = lot.getFinance(null,'budgetRF',2015) == null ? "" : lot.getFinance(null,'budgetRF',2015)/1000000
         lotEntry.budgetRF2016 = lot.getFinance(null,'budgetRF',2016) == null ? "" : lot.getFinance(null,'budgetRF',2016)/1000000
         lotEntry.budgetRFTotal = lot.getFinance(null,'budgetRF',null) == null ? "" : lot.getFinance(null,'budgetRF',null)/1000000
         lotEntry.umbrellaType = lot.umbrellaType
         lotEntry.lotURL = URLCreator.createFullLinkToPublishedObject(lot)+"&activeComponent=filesMain.files"
         if (lotEntry.umbrellaType)
         {
             lotEntry.contractNumberInLot = lot.contractNumberInLot == null ? "-" : lot.contractNumberInLot
             lotEntry.marginalProjectsCost = lot.marginalProjectsCost == null ? "-" : lot.marginalProjectsCost/1000000
         }
         else
         {
             lotEntry.contractNumberInLot = "-"
             lotEntry.marginalProjectsCost = "-"
         }
         
         result.add(lotEntry)
 }

 report.vars.confIdentifier = conference.identifier
 report.vars.confDate = conference.conferenceDate
 report.vars.confComment = conference.comment
 report.vars.confURL = URLCreator.createFullLinkToPublishedObject(conference)
 
 
 report.vars.lots = result
 
class LotEntry
{
    def fullNumber
    def theme
    def programAction
    def leadingSubPriorityLine
    def budgetRF2014
    def budgetRF2015
    def budgetRF2016
    def budgetRFTotal
    def umbrellaType
    def lotURL
    def contractNumberInLot
    def marginalProjectsCost
}