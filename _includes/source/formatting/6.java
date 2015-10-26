FileReader reader = new FileReader("entry.json");
JsonReader jsonReader = new JsonReader(reader);
SearchResult result = jsonReader.read();
