/**
 * Скрипт для генерации данных для договора на экспертизу отчетных материалов
 * Предполагает входной параметр task, являющийся экземпляром класса ExaminationTaskRM
 *
 */

report.vars.report=report
report.vars.expert=task.expert
report.vars.task=task
report.vars.contract_id=task.contract.identifier
report.vars.contract_sign_date=task.contract.appendSignatureDate
report.vars.object_id=task.object.number
report.vars.object_finalmid=task.object.finalmid
report.vars.numberLetter=task.numdogovorPAOM

report.vars.gosContract = helper.getCatalogItem("СomplianceLettersContracts", report.vars.numberLetter).title