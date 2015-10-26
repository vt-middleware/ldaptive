FileReader reader = new FileReader("entry.dsml");
Dsmlv1Reader dsmlReader = new Dsmlv1Reader(reader);
SearchResult result = dsmlReader.read();
