package com.example.passwordmanagerclient.util.appdata;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FilepathConstants {

    public static final Path APPDATA_PATH = Paths.get(System.getenv("APPDATA"), "SeguraPass");

}
