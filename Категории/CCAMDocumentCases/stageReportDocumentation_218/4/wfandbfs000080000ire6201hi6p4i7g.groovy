/*
 * $Id$
 */
package resources.groovy

import ru.naumen.core.workflow.stages.Stage
import ru.naumen.ccamcore.bobject.bactions.SetCCAMStageBusinessAction
import ru.naumen.core.ui.BKUIUtils

def stageBA = {bo, idStage, user ->
    Stage stage =  bo.ccamBOCase.workflowDefinition.getStageByIdentificator(idStage)
    return new SetCCAMStageBusinessAction(bo, stage, null, user);
}

def changeState = {stage, oldStateId, newStateId, user, session ->
    if (oldStateId == null ||  oldStateId.equals(stage.currentStage.identificator))
    {
        stageBA(stage, newStateId, user).execute(session);
    }
}

user = BKUIUtils.currentPerson;
stage = subject.parent
//stage = helper.getObject("")
//changeState(stage, "1", "beginAudit", user, session)
changeState(stage, null, "editComplited", user, session)
