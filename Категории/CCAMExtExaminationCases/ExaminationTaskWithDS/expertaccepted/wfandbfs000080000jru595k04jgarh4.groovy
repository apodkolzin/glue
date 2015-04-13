 import ru.naumen.guic.components.forms.UIForm.UIFormUserException
 import ru.naumen.fcntp.bobject.examination.ExaminationTaskFcntpBase
 
 agreement = ((ExaminationTaskFcntpBase) subject).agreement
 
 if(agreement == null || !agreement.signed){
      throw new UIFormUserException("Необходимо подписать договор электронной подписью")
 } 