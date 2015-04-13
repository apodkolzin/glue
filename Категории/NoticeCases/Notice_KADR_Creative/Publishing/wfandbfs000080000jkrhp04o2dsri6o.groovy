import ru.naumen.guic.components.forms.UIForm.UIFormUserException; 

lotStageUuid='wstagefs000080000jcaesjuc9h6e6dk';
if(lotStageUuid=='')
  throw new UIFormUserException('Невозможно выполнить скрипт перевода состояний для связанных лотов. Не указано новое состоние для лотов.');
lotStage = helper.getObject(lotStageUuid);
helper.execute()
{
  s->
    query = s.createQuery('from LotFcntp lot where lot.notice=:notice');
    query.setParameter('notice',subject);
    lots = query.list();
    lots.each()
    {
      lot->
        lot.currentStage = lotStage;
        lot.doUpdate(s);
    }
}

