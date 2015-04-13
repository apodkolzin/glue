package resources.groovy

import ru.naumen.ccamcore.util.CcamExceptionUtil
import ru.naumen.fcntp.bobject.report.bo.FormularReport
import ru.naumen.fx.exceptions.ReadableException
import ru.naumen.guic.components.forms.UIForm
import ru.naumen.integration.formular.bobject.handlers.FormularReportDAOUtils
import ru.naumen.integration.formular.bobject.relations.FormularReportToFormRelation
import ru.naumen.wcf.exceptions.UIException

/**
 *
 * <p>Created 06.11.14</p>
 * @author kblokhin
 */
def errors = []
def incorrectForms = []
boolean fileMissing = false

FormularReport report = subject as FormularReport
List<FormularReportToFormRelation> reportForms =
        FormularReportDAOUtils.formularFormRelationDAO(session).findIncorrect(report)
reportForms.each {
    if(it.file == null)
        fileMissing = true
    else
        incorrectForms.add(it.getRightBO().title)
}

if(fileMissing)
    errors.add('Необходимо ввести данные и нажать кнопку "Сохранить" во всех отчетных формах.')

if(!incorrectForms.isEmpty())
    errors.add('Необходимо исправить некорректные данные (подсвеченные красным цветом) \
                    в следующих отчетных формах: ' + incorrectForms.join(', '))

if(errors.size() > 0)
    throw CcamExceptionUtil.parseTo( UIException.class, UIForm.UIFormUserException.class,
            new ReadableException('Ошибка', errors.join('\n')));