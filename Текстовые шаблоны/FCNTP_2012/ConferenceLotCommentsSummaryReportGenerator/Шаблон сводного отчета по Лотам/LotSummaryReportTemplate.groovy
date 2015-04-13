class ReportComment
{
  public String author;
  public String lotTitle;
  public String privateComment;
  public String publicComment;

  public ReportComment(auth,lot,pubC,priC)
  {
    author=auth;
    lotTitle=lot;
    privateComment=priC;
    publicComment=pubC;
  }
}

reportData = [];
comments.each()
{
comment->
  reportData.add(new ReportComment(comment.author.getDisplayableTitle(false),comment.parent.lot.getDisplayableTitle(),comment.privateComment,comment.publicComment));
}

reportData;