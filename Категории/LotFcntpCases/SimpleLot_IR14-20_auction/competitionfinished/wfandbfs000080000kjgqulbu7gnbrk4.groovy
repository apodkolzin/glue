import ru.naumen.core.util.hquery.HCriteria
import ru.naumen.core.util.hquery.HRestrictions
import ru.naumen.core.workflow.stages.StagesHibernateHandler
import ru.naumen.fcntp.bobject.demand.DemandFcntp

/**
 * Created by azimka on 18.11.14.
 */

def lot = subject
def demandStageIdentificator = "1"
def lotDemandCategories = ["SimpleLot_IR14-20_auction":"Demand_IR14-20_auction",
                            "SimpleLot_IR14-20_grant":"DemandIR14-20_grant",
                            "SimpleLot_IR14-20FZ":"Demand_IR14-20FZ"]
def newStageID = "complete"

helper.execute{ s->
    def demands = getDemandsByLotAndStages(lot,demandStageIdentificator,s)
    for(def demand in demands)
    {
        if (lotDemandCategories.get(lot.ccamBOCase.code).equals(demand.ccamBOCase.code)){
            def newStage = StagesHibernateHandler.getStageByIdentificator(demand.ccamBOCase.workflowDefinition, newStageID, s)
            if(newStage!=null){
                demand.currentStage = newStage
                demand.doUpdate(s)
            }
        }
    }
}

def getDemandsByLotAndStages(lot,stage,s)
{
    HCriteria criteria = new HCriteria();
    criteria.addSource(DemandFcntp.class, "demand");
    criteria.add(HRestrictions.eq("demand.parent", lot));
    criteria.add(HRestrictions.eq("demand.currentStage.identificator", stage));
    return criteria.createQuery(s).list();
}