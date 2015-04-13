caseCodes = ["'Demand220'"]
winnerStageIdentificator = 'contract'

query = "select d.leadScientistForm from DemandFcntp d where d.ccamBOCase.code in (" + ru.naumen.common.utils.StringUtilities.join(caseCodes.toArray(), ", ") + ") and d.currentStage.identificator='"+winnerStageIdentificator+"'"

list = helper.select(query) 
scients = []
for(scient in list){
      scient.leadScientistWorkCountry = ru.naumen.core.hibernate.HibernateUtil.unproxy(scient.leadScientistWorkCountry)
      scient.leadScientistResidenceCountry = ru.naumen.core.hibernate.HibernateUtil.unproxy(scient.leadScientistResidenceCountry)
      
      scients.add(scient)
}
report.vars.scients = scients