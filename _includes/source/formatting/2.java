FileReader reader = new FileReader("entry.ldif");
LdifReader ldifReader = new LdifReader(reader);
SearchResponse response = ldifReader.read();
