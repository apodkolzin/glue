class ReportComment
{
  public String author;
  public String objTitle;
  public String privateComment;
  public String publicComment;

  public ReportComment(auth,obj,pubC,priC)
  {
    author=auth;
    objTitle=obj;
    privateComment=priC;
    publicComment=pubC;
  }
}

reportData = [];
comments.each()
{
comment->
  reportData.add(new ReportComment(comment.author.getDisplayableTitle(false),comment.parent.requirement.getDisplayableTitle(),comment.privateComment,comment.publicComment));
}

reportData;