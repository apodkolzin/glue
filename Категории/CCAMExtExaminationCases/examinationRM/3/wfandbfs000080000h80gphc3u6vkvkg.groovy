if (subject.cost != null && subject.cost.compareTo(0) != 0)
{
new ru.naumen.fcntp.bobject.agreement.AddExpertAgreementBA(subject).execute(session);
}