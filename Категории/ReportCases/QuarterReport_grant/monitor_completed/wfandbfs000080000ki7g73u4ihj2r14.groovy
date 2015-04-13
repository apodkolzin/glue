import ru.naumen.ccamcore.util.CcamExceptionUtil
import ru.naumen.wcf.exceptions.UIException
import ru.naumen.guic.components.forms.UIForm.UIFormUserException
import ru.naumen.fx.exceptions.ReadableException

if("re-approval".equals(newState?.getIdentificator()) && 
  subject.docVersion != null && subject.docVersion != 1)
{
    throw CcamExceptionUtil.parseTo( UIException.class, UIFormUserException.class, 
    new ReadableException("Ошибка", "Нельзя создавать больше 2х версий отчета."));
}