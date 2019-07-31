SearchFilter filter = new SearchFilter("(|(uid={0})(mail={1}))");
filter.setParameters(new Object[] {"1234", "dfisher*@ldaptive.org"});
