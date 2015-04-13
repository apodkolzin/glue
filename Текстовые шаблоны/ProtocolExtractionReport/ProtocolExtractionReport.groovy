package resources.groovy.birt

import ru.naumen.fcntp.bobject.contract.ContractFcntp
import ru.naumen.fcntp.bobject.document.FcntpDocumentStage

/**
 * @author ayakovlev
 * Date: 6.03.2013
 * Скрипт для генерации документа "Выписка из протокола"
 * http://ssh-gate.naumen.ru:10305/lab_labour/show/14599
 */


FcntpDocumentStage document = object
ContractFcntp contract = document.parent.contract

report.vars.number = contract.identifier
report.vars.sign = contract.appendSignatureDate 
report.vars.executer = contract.managerFromExecuter.title
report.vars.theme = contract.fullTitle