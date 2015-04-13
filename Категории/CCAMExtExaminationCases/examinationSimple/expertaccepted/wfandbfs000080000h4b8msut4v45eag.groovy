if (subject.cost != null && subject.cost.compareTo(0) != 0)
{
 agr = subject.agreement;
 if (agr == null) {
   new ru.naumen.fcntp.bobject.agreement.AddExpertAgreementBA(subject).execute(session);
 } else {
   new ru.naumen.ccamext.docgen.GenerateDocumentFileBA(agr).execute(session);
 }
}
