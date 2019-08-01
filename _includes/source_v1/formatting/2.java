FileReader reader = new FileReader("entry.ldif");
LdifReader ldifReader = new LdifReader(reader);
SearchResult result = ldifReader.read();
