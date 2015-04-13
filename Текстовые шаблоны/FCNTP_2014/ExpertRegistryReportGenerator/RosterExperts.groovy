import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

/**
 * Created by azimka on 24.10.14.
 */
//def expertRegistry = object
def expertRegistry = helper.get('expregfs002080000kigheipiold64io')
def lot = expertRegistry.parent
report.vars.programAction = lot.programAction?.displayableTitle
report.vars.queueNumber = lot.queueNumber
report.vars.inOfferNumber = lot.inOfferNumber
report.vars.theme = lot.theme
report.vars.fullNumber = lot.fullNumber
report.vars.leadingSubPriorityLine = lot.leadingSubPriorityLine?.title
report.vars.registryCategory = expertRegistry.getCategoryTitle()
report.vars.curatorFIO = expertRegistry.curator?.getIOFString()
report.vars.creationDays = new SimpleDateFormat("dd.MM.yyyy").format(new Date())

def persons = []
def relations = helper.select("from ExpertRegistryToEmployeeRelation f where f.registry.UUID = '$expertRegistry.UUID'")
for (def item in relations)
{
    Person person = new Person();
    person.fio = item.rightBO.title()
    person.academicDegrees = StringUtils.join(item.academicDegrees.collect({it.academicDegree}).collect({it.title}),",\n")
    person.academicStatuses = StringUtils.join(item.academicStatuses.collect({it.academicStatus}).collect({it.title}),",\n")
    person.academicDegrees = StringUtils.join(item.organizations.collect({it.title}),",\n")
    person.note = item.note
    persons.add(person)
}
report.vars.persons = persons


class Person
{
    def fio
    def academicDegrees
    def academicStatuses
    def organizations
    def note
}