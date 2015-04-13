 	
if ("corebofs000080000gte9v29hjmmodmk".equals(subject.parent.parent.UUID) || "corebofs000080000hgn46g4u83027lk".equals(subject.parent.parent.UUID) || "corebofs000080000hfqib0fho5uol08".equals(subject.parent.parent.UUID))
{
if (subject.getCost() != null && subject.getCost().compareTo(0) != 0)
{
new ru.naumen.fcntp.bobject.agreement.AddExpertAgreementBA(subject).execute(session);
}
} 