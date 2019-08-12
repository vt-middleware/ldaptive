FileReader reader = new FileReader("entry.json");
JsonReader jsonReader = new JsonReader(reader);
SearchResponse response = jsonReader.read();
