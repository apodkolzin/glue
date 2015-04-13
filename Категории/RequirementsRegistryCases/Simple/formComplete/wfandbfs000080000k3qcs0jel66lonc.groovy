package resources.groovy

import org.hibernate.Session
import ru.naumen.ccamcore.bobject.bactions.SetCCAMStageBusinessAction
import ru.naumen.core.bobjects.person.CorePerson
import ru.naumen.core.hibernate.bactions.BusinessActionBase
import ru.naumen.core.workflow.IWorkflowDefinition
import ru.naumen.core.workflow.stages.Stage
import ru.naumen.fcntp.bobject.requirement.RequirementFcntp
import ru.naumen.fcntp.bobject.requirement.RequirementsRegistry
import ru.naumen.fcntp.bobject.requirement.RequirementsRegistryHibernateHandler

/**
 * User: dmartyanov
 * Date: 10/23/13
 * Time: 4:52 PM
 */

oldStageId = 'checkСomplete';
newStageId = 'underConsiderationRG';
def registry = BusinessActionBase.unproxy(subject)

List<RequirementFcntp> reqs = RequirementsRegistryHibernateHandler.listRequirements((RequirementsRegistry)registry, session);
for(RequirementFcntp req : reqs)
    process(req)

def process(RequirementFcntp req)
{
    stageID = req.getCurrentStage().getIdentificator();
    if(!stageID.equals(oldStageId))
        return;
    Stage stage = getNewStage(req);
    if(stage != null)
        changeReqStage(req, stage );
}

def changeReqStage(RequirementFcntp req, Stage stage)
{
    CorePerson pers = ru.naumen.core.ui.BKUIUtils.getCurrentPerson();
    new SetCCAMStageBusinessAction(req, stage, null, pers).execute();
}

def getNewStage(RequirementFcntp req)
{
    IWorkflowDefinition workflowDefinition = req.getBOCase().getWorkflowDefinition();
    Stage state = (Stage) workflowDefinition.getStageByIdentificator(newStageId);
    if(state!= null)
        return state;
    return null;
}

