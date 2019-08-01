SearchFilter filter = new SearchFilter("(|(uid={uid})(mail={mail}))");
filter.setParameter("uid", "1234");
filter.setParameter("mail", "dfisher*@ldaptive.org");
