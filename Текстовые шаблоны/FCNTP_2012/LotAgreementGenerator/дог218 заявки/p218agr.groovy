task = helper.get('corebofs000080000jun3pcpdot9118k')

report.vars.report=report
report.vars.expert=task.expert
report.vars.task=task
report.vars.performer=task?.contract?.performer?.titleInInstrumentalCase
report.vars.contract_id=task?.contract?.identifier
report.vars.contract_sign_date=task?.contract?.appendSignatureDate