SearchFilter filter = SearchFilter.builder()
  .filter("(|(uid={uid})(mail={mail}))")
  .parameter("uid", "1234")
  .parameter("mail", "dfisher*@ldaptive.org")
  .build();
