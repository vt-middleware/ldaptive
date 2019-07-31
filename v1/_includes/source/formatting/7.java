FileReader reader = new FileReader("entry.ldif");
LdifReader ldifReader = new LdifReader(reader, SortBehavior.SORTED);
SearchResult result = ldifReader.read(); // data will be sorted accordingly
