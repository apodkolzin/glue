contractCategory = "contractSimple_Creative"
contractProjectCategory = "contractProject_Creative"
  
report.vars.states = helper.run("AgreementProjStatesReport", ["contractCategory":contractCategory, "contractProjectCategory":contractProjectCategory, "reportType": 2])