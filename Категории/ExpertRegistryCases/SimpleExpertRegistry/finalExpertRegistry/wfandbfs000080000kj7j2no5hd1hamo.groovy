package resources.groovy

import ru.naumen.core.ui.BKUIUtils
import ru.naumen.fcntp.bobject.expertregistry.ExpertRegistryHibernateHandler
import ru.naumen.fcntp.bobject.registry.AddExpertToRegistryBA
import ru.naumen.guic.components.forms.UIForm

/**
* @author azimka
* 11.11.2014
*
*/

def expertRegistry = subject
//def expertRegistry = helper.get('expregfs000080000kj7fke3plc1re70')

validate(expertRegistry)

def experts = helper.select("from ExaminationTaskDemand e where e.parent.UUID = '$expertRegistry.parent.UUID'" +
" and e.currentStage.identificator='accepted'").collect({it.expert}).unique()
for (def empStech in experts)
{
if (ExpertRegistryHibernateHandler.getExpertRegistryToEmployeeRelation(expertRegistry, empStech, session) == null)
{
def author = BKUIUtils.getCurrentPerson()
def organizations = empStech.getOrganizations() as Set
def academicDegrees = empStech.getAcademicDegree() as Set
def academicStatuses = empStech.getAcademicStatus() as Set
def stages = [] as Set
stages.add(expertRegistry.currentStage)
AddExpertToRegistryBA addExpertToRegistryBA = new AddExpertToRegistryBA(expertRegistry,
empStech, organizations, academicDegrees,
academicStatuses, stages, author).execute();
}
}
def validate(expertRegistry)
{
def lot = expertRegistry.parent
def query = session.createQuery("select e from ExaminationTask e where e.parent.id=:lotUUID and e.currentStage.identificator='accepted'")

if (query.setParameter("lotUUID", lot.UUID).list().size() == 0)
throw new UIForm.UIFormUserException('Формирование Итогового реестра экспертов невозможно. Необходимо наличие выполненных заданий на экспертизу по Лоту.')
} 