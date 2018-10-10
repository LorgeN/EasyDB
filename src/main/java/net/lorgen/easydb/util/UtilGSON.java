package net.lorgen.easydb.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

public class UtilGSON {

    public static final Gson INSTANCE = new GsonBuilder()
      .setPrettyPrinting()
      .create();

    public static final JsonParser PARSER = new JsonParser();
}
