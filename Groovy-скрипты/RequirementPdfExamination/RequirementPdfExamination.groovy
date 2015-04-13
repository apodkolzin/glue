def groovyScriptCodesByProgramUUID = [
'corebofs000080000j0jr0p01913b32k': 'RequirementPdfExamination_FM',
'corebofs000080000hfqib0fho5uol08': 'RequirementPdfExamination_NANO',
'default' : 'RequirementPdfExamination_DEF'
];

def programUUID = req.getParent().getUUID();

helper.run(
groovyScriptCodesByProgramUUID.containsKey(programUUID) ?
groovyScriptCodesByProgramUUID[programUUID] : groovyScriptCodesByProgramUUID['default'],
[
report: report,
req: req,
document: document,
helper: helper,
session: session,
user: user,
log: log
],
session
)