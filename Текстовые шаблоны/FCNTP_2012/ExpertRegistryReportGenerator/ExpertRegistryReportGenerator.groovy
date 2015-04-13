import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

/**
 * Created by azimka on 24.10.14.
 */
def expertRegistry = object
//def expertRegistry = helper.get('expregfs000080000kj7fke3plc1re70')
def lot = expertRegistry.parent
report.vars.programAction = lot.programAction?.displayableTitle
report.vars.queueNumber = lot.queueNumber
report.vars.inOfferNumber = lot.inOfferNumber
report.vars.theme = lot.theme
report.vars.fullNumber = lot.fullNumber
report.vars.leadingSubPriorityLine = lot.leadingSubPriorityLine?.title
report.vars.registryStage = expertRegistry.currentStage?.identificator
report.vars.curatorFIO = expertRegistry.curator?.title
report.vars.creationDays = new SimpleDateFormat("dd.MM.yyyy").format(new Date())

def relationsAll = helper.select("from ExpertRegistryToEmployeeRelation f where f.registry.UUID = '$expertRegistry.UUID'")
relations = []
for(def rel in relationsAll){
    if(rel.stages.contains(expertRegistry.currentStage)){
        relations.add(rel)
    }
}

def persons = []
for (def item in relations)
{
    Person person = new Person();
    person.fio = item.rightBO.title
    person.academicDegrees = StringUtils.join(item.academicDegrees.collect({it.academicDegree.title+" "+it.speciality.title}),",\n")
    person.academicStatuses = StringUtils.join(item.academicStatuses.collect({it.academicStatus.title+" "+it.chair}),",\n")
    person.organizations = StringUtils.join(item.organizations.collect({it.title}),",\n")
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