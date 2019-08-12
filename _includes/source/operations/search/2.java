SearchFilter filter = SearchFilter.builder()
  .filter("(|(uid={0})(mail={1}))")
  .parameters("1234", "dfisher*@ldaptive.org")
  .build();
