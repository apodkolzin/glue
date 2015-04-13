import ru.naumen.guic.components.forms.UIForm.UIFormUserException; 
import ru.naumen.ccamcore.logging.CCAMLogUtil;
import ru.naumen.core.ui.BKUIUtils;

lotStageUuid='wstagefs000080000hshcbolp62o0gmk';
if(lotStageUuid=='')
  throw new UIFormUserException('Невозможно выполнить скрипт перевода состояний для связанных лотов. Не указано новое состоние для лотов.');
lotStage = helper.getObject(lotStageUuid);

author=BKUIUtils.getCurrentPerson();
helper.execute()
{
  s->
    query = s.createQuery('from LotFcntp lot where lot.notice=:notice');
    query.setParameter('notice',subject);
    lots = query.list();
    lots.each()
    {
      lot->
        oldState = lot.currentStage;
        lot.currentStage = lotStage;
        lot.doUpdate(s);
        CCAMLogUtil.logStateChange(lot, author, oldState, lotStage, s);
    }
}

