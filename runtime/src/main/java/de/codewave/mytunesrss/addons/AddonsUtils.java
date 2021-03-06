/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.addons;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.addons.AddonsUtils
 */
public class AddonsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddonsUtils.class);

    public static Collection<ThemeDefinition> getThemes(boolean builtinThemes) {
        Set<ThemeDefinition> themeSet = new HashSet<>();
        try {
            File themesDir = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/themes");
            if (!themesDir.exists() && builtinThemes) {
                if (!themesDir.mkdirs()) {
                    LOGGER.warn("Could not create folder for themes.");
                }
            }
            if (builtinThemes) {
                themeSet.addAll(getThemesFromDir(themesDir));
            }
            themesDir = getUserThemesDir();
            themeSet.addAll(getThemesFromDir(themesDir));
        } catch (IOException e) {
            LOGGER.error("Problem reading existing themes.", e);
        }
        List<ThemeDefinition> themes = new ArrayList<>(themeSet);
        Collections.sort(themes, new Comparator<ThemeDefinition>() {
            @Override
            public int compare(ThemeDefinition o1, ThemeDefinition o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return themes;
    }

    private static File getUserThemesDir() {
        File themesDir = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/themes");
        if (!themesDir.exists()) {
            if (!themesDir.mkdirs()) {
                LOGGER.warn("Could not create folder for themes.");
            }
        }
        return themesDir;
    }

    private static Collection<ThemeDefinition> getThemesFromDir(File themesDir) throws IOException {
        Collection<ThemeDefinition> themes = new HashSet<>();
        if (themesDir.isDirectory()) {
            String[] themeFileNames = themesDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return new File(dir, name + "/images").isDirectory() && new File(dir, name + "/styles").isDirectory();
                }
            });
            if (themeFileNames != null) {
                for (String theme : themeFileNames) {
                    File readme = new File(themesDir + "/" + theme, "readme.txt");
                    if (readme.isFile()) {
                        themes.add(new ThemeDefinition(theme, FileUtils.readFileToString(readme, "UTF-8")));
                    } else {
                        themes.add(new ThemeDefinition(theme, null));
                    }
                }
            }
        }
        return themes;
    }

    public static Collection<LanguageDefinition> getLanguages(boolean builtinLanguages) {
        Set<LanguageDefinition> languageSet = new HashSet<>();
        File languagesDir = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/languages");
        if (!languagesDir.exists() && builtinLanguages) {
            if (!languagesDir.mkdirs()) {
                LOGGER.warn("Could not create folder for languages.");
            }
        }
        if (builtinLanguages) {
            languageSet.add(new LanguageDefinition().setCode("de"));
            languageSet.add(new LanguageDefinition().setCode("en"));
            languageSet.addAll(getLanguagesFromDir(languagesDir));
        }
        languagesDir = getUserLanguagesDir();
        languageSet.addAll(getLanguagesFromDir(languagesDir));
        List<LanguageDefinition> languages = new ArrayList<>(languageSet);
        Collections.sort(languages, new Comparator<LanguageDefinition>() {
            @Override
            public int compare(LanguageDefinition o1, LanguageDefinition o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });
        return languages;
    }

    private static File getUserLanguagesDir() {
        File languagesDir = new File(MyTunesRss.PREFERENCES_DATA_PATH + "/languages");
        if (!languagesDir.exists()) {
            if (!languagesDir.mkdirs()) {
                LOGGER.warn("Could not create folder for languages.");
            }
        }
        return languagesDir;
    }

    private static Collection<LanguageDefinition> getLanguagesFromDir(File languagesDir) {
        Collection<LanguageDefinition> languages = new HashSet<>();
        if (languagesDir.isDirectory()) {
            String[] languageFileNames = languagesDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("MyTunesRssWeb_") && name.endsWith(".properties");
                }
            });
            if (languageFileNames != null) {
                for (String language : languageFileNames) {
                    String languageCode = getLanguageCode(language);
                    if (languageCode != null) {
                        try {
                            languages.add(getLocalLanguageDefinition(languagesDir, languageCode));
                        } catch (IOException | NumberFormatException e) {
                            LOGGER.warn("Could not use language defintion.", e);
                        }
                    }
                }
            }
        }
        return languages;
    }

    private static LanguageDefinition getLocalLanguageDefinition(File languagesDir, String code) throws IOException {
        File metaFile = new File(languagesDir, "MyTunesRssWeb_" + code + ".json");
        if (metaFile.isFile()) {
            try (InputStream is = new FileInputStream(metaFile)) {
                return LanguageDefinition.deserialize(is);
            }
        }
        return new LanguageDefinition().setCode(code);
    }

    private static String getLanguageCode(String propertiesFilesName) {
        int underscoreIndex = propertiesFilesName.indexOf('_');
        if (underscoreIndex > -1 && underscoreIndex + 1 < propertiesFilesName.length()) {
            return propertiesFilesName.substring("MyTunesRssWeb_".length(), propertiesFilesName.length() - ".properties".length());
        }
        return null;
    }

    public static AddFileResult addTheme(String themeName, File theme) {
        if (isThemeArchive(theme)) {
            File themeDir = null;
            ZipArchiveInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipArchiveInputStream(new FileInputStream(theme));
                for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                    themeDir = new File(getUserThemesDir() ,themeName);
                    saveFile(themeDir, entry.getName(), zipInputStream);
                }
            } catch (IOException e) {
                if (themeDir != null && themeDir.exists()) {
                    try {
                        FileUtils.deleteDirectory(themeDir);
                    } catch (IOException ignored) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not delete directory.", e);
                        }
                    }
                }
                return AddFileResult.ExtractFailed;
            } finally {
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not close zip input stream.", e);
                        }
                    }
                }
            }
        } else {
            return AddFileResult.InvalidFile;
        }
        return AddFileResult.Ok;
    }

    private static boolean isThemeArchive(File theme) {
        ZipArchiveInputStream zipInputStream = null;
        boolean stylesFound = false;
        boolean imagesFound = false;
        try {
            zipInputStream = new ZipArchiveInputStream(new FileInputStream(theme));
            for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                String entryName = entry.getName();
                if ("styles".equals(entryName) || entryName.startsWith("styles/") || entryName.startsWith("styles\\")) {
                    stylesFound = true;
                } else if ("images".equals(entryName) || entryName.startsWith("images/") || entryName.startsWith("images\\")) {
                    imagesFound = true;
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not verify language archive.", e);
            }
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not close zip input stream.", e);
                    }
                }
            }
        }
        return imagesFound && stylesFound;
    }

    private static void saveFile(File baseDir, String fileName, InputStream inputStream) throws IOException {
        if (isAccepted(fileName)) {
            String dirName = "";
            if (fileName.contains("/")) {
                dirName = fileName.substring(0, fileName.lastIndexOf("/"));
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            } else if (fileName.contains("\\")) {
                dirName = fileName.substring(0, fileName.lastIndexOf("\\"));
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            }
            File uploadDir = new File(baseDir, dirName);
            if (!uploadDir.exists()) {
                LOGGER.debug("Creating upload directory \"" + uploadDir + "\".");
                if (!uploadDir.mkdirs()) {
                    LOGGER.warn("Could not create folder for uploads.");
                }
            }
            if (uploadDir.isDirectory()) {
                FileOutputStream targetStream = new FileOutputStream(new File(uploadDir, fileName));
                IOUtils.copy(inputStream, targetStream);
                targetStream.close();
            }
        }
    }

    private static boolean isAccepted(String fileName) {
        return !(StringUtils.isEmpty(fileName) || fileName.endsWith("/") || fileName.endsWith("\\")) && !fileName.contains("__MACOSX/");
    }

    public static AddFileResult addLanguage(File language, String originalFilename) {
        if (language.isFile() && isLanguageArchive(language)) {
            ZipArchiveInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipArchiveInputStream(new FileInputStream(language));
                for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                    if (isLanguageFilename(entry.getName())) {
                        storeLanguage(getUserLanguagesDir(), new LanguageDefinition().setCode(getLanguageCode(entry.getName())), zipInputStream);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Could save language file.", e);
                return AddFileResult.ExtractFailed;
            } finally {
                if (zipInputStream != null) {
                    try {
                        zipInputStream.close();
                    } catch (IOException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Could not close zip input stream.", e);
                        }
                    }
                }
            }
        } else if (language.isFile() && isLanguageFilename(originalFilename)) {
            try {
                try (FileInputStream languageInputStream = new FileInputStream(language)) {
                    storeLanguage(getUserLanguagesDir(), new LanguageDefinition().setCode(getLanguageCode(originalFilename)), languageInputStream);
                }
            } catch (IOException ignored) {
                return AddFileResult.SaveFailed;
            }
        } else {
            return AddFileResult.InvalidFile;
        }
        return AddFileResult.Ok;
    }

    private static boolean isLanguageArchive(File language) {
        ZipArchiveInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipArchiveInputStream(new FileInputStream(language));
            for (ZipArchiveEntry entry = zipInputStream.getNextZipEntry(); entry != null; entry = zipInputStream.getNextZipEntry()) {
                if (isLanguageFilename(entry.getName())) {
                    return true;
                }
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not verify language archive.", e);
            }
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Could not close zip input stream.", e);
                    }
                }
            }
        }
        return false;
    }

    private static boolean isLanguageFilename(String filename) {
        return StringUtils.startsWith(FilenameUtils.getBaseName(filename), "MyTunesRssWeb_") && "properties".equalsIgnoreCase(FilenameUtils.getExtension(filename));
    }

    public static String deleteTheme(String themeName) {
        if (StringUtils.equals(MyTunesRss.CONFIG.getDefaultUserInterfaceTheme(), themeName)) {
            MyTunesRss.CONFIG.setDefaultUserInterfaceTheme(null);
        }
        try {
            File themeDir = new File(getUserThemesDir(), themeName);
            if (themeDir.isDirectory()) {
                FileUtils.deleteDirectory(themeDir);
            } else {
                return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.deleteThemeNoDir");
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not delete theme \"" + themeName + "\".", e);
            }
            return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.couldNotRemoveTheme");
        }
        return null;
    }

    public static String deleteLanguage(String languageCode) {
        File languageFile = new File(getUserLanguagesDir(), "MyTunesRssWeb_" + languageCode + ".properties");
        if (languageFile.isFile()) {
            if (!languageFile.delete()) {
                LOGGER.debug("Could not delete file \"" + languageFile.getAbsolutePath() + "\".");
            }
        } else {
            return MyTunesRssUtils.getBundleString(Locale.getDefault(), "error.deleteLanguageNoFile");
        }
        File metaFile = new File(getUserLanguagesDir(), "MyTunesRssWeb_" + languageCode + ".json");
        if (metaFile.isFile()) {
            if (!metaFile.delete()) {
                LOGGER.debug("Could not delete file \"" + metaFile + "\".");
            }
        }
        return null;
    }

    public static File getBestLanguageFile(Locale locale) {
        return getLanguageFile(locale, true, true, false);
    }

    public static File getUserLanguageFile(Locale locale) {
        return getUserLanguageFile(locale, false);
    }

    public static File getUserLanguageFile(Locale locale, boolean returnMissing) {
        return getLanguageFile(locale, true, false, returnMissing);
    }

    public static File getBuiltinLanguageFile(Locale locale) {
        return getLanguageFile(locale, false, true, false);
    }

    private static File getLanguageFile(Locale locale, boolean user, boolean builtin, boolean returnMissing) {
        List<String> fileNames = getLanguageFileNames(locale);
        for (String fileName : fileNames) {
            File languageFile = new File(getUserLanguagesDir(), fileName);
            if (user && (returnMissing || languageFile.isFile())) {
                return languageFile;
            }
            languageFile = new File(MyTunesRssUtils.getBuiltinAddonsPath() + "/languages/" + fileName);
            if (builtin && (returnMissing || languageFile.isFile())) {
                return languageFile;
            }
        }
        return null;
    }

    private static List<String> getLanguageFileNames(Locale locale) {
        String[] codes = locale.toString().split("_");
        List<String> fileNames = new ArrayList<>();
        if (codes.length == 3) {
            fileNames.add("MyTunesRssWeb_" + codes[0] + "_" + codes[1] + "_" + codes[2] + ".properties");
        }
        if (codes.length >= 2) {
            fileNames.add("MyTunesRssWeb_" + codes[0] + "_" + codes[1] + ".properties");
        }
        fileNames.add("MyTunesRssWeb_" + codes[0] + ".properties");
        fileNames.add("MyTunesRssWeb_en.properties"); // minimum available default
        return fileNames;
    }

    public enum AddFileResult {
        ExtractFailed(), InvalidFile(), Ok(), SaveFailed()
    }

    public static void storeLanguage(LanguageDefinition definition, Properties language) throws IOException {
        storeLanguage(getUserLanguagesDir(), definition, language);
    }

    private static void storeLanguage(File languagesDir, LanguageDefinition definition, InputStream languageStream) throws IOException {
        Properties props = new Properties();
        props.load(languageStream);
        storeLanguage(languagesDir, definition, props);
    }

    private static void storeLanguage(File languagesDir, LanguageDefinition definition, Properties language) throws IOException {
        File metaFile = new File(languagesDir, "MyTunesRssWeb_" + definition.getCode() + ".json");
        try (OutputStream osMeta = new FileOutputStream(metaFile)) {
            LanguageDefinition.serialize(definition, osMeta);
            File langFile = new File(languagesDir, "MyTunesRssWeb_" + definition.getCode() + ".properties");
            try (OutputStream osLang = new FileOutputStream(langFile)) {
                language.store(osLang, null);
            }
        }
    }

    public static boolean isComplete(LanguageDefinition languageDefinition) throws IOException {
        Properties refProps = new Properties();
        try (InputStream refInputStream = new FileInputStream(AddonsUtils.getBuiltinLanguageFile(Locale.ENGLISH))) {
            refProps.load(refInputStream);
        }
        Properties props = new Properties();
        try (FileInputStream is = new FileInputStream(AddonsUtils.getUserLanguageFile(languageDefinition.getLocale()))) {
            props.load(is);
        }
        for (Object key : refProps.keySet()) {
            if (!props.containsKey(key) || StringUtils.isBlank((CharSequence) props.get(key))) {
                return false;
            }
        }
        return true;
    }
}
