package dev.kinau.resourcepackvalidator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.kinau.resourcepackvalidator.cache.AssetDictionary;
import dev.kinau.resourcepackvalidator.config.Config;
import dev.kinau.resourcepackvalidator.report.ReportGenerator;
import dev.kinau.resourcepackvalidator.report.TestCase;
import dev.kinau.resourcepackvalidator.report.TestSuite;
import dev.kinau.resourcepackvalidator.utils.FileUtils;
import dev.kinau.resourcepackvalidator.utils.ZipUtils;
import dev.kinau.resourcepackvalidator.validator.ValidatorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.LogManager;

@Slf4j
public class ResourcePackValidator {

    private final TestSuite testSuite;
    private CommandLine commandLine;
    private File cleanupOnShutdown;

    public static void main(String[] args) {
        new ResourcePackValidator(args);
    }

    public ResourcePackValidator(String[] args) {
        initLogging();
        this.testSuite = new TestSuite();
        TestCase cliArgumentsCorrect = testSuite.getCase("CLI Arguments valid").start();
        try {
            this.commandLine = initCLI(args);
            if (commandLine == null) return;
        } catch (ParseException ex) {
            cliArgumentsCorrect.addError("Could not parse start arguments", ex);
        } finally {
            cliArgumentsCorrect.stop();
        }
        if (commandLine.hasOption("verbose"))
            adjustLogLevel();

        shouldCreateAssetCache();

        Config config = initConfig();

        registerShutdownHook();
        File rootDir = getRootDirectory();
        if (rootDir == null) return;

        ValidatorRegistry registry = new ValidatorRegistry(config, testSuite);
        boolean finishedWithoutError = validate(rootDir, registry) && testSuite.hasNoFailure();

        if (commandLine.hasOption("report")) {
            new ReportGenerator(testSuite.testCases(), new File(commandLine.getOptionValue("report")));
        }

        System.exit(finishedWithoutError ? 0 : 1);
    }

    private void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(ResourcePackValidator.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void adjustLogLevel() {
        TestCase adjustLogLevel = testSuite.getCase("Adjust log level").start();
        try {
            LogManager.getLogManager().updateConfiguration((key) -> (oldVal, newVal) -> {
                if (key.equals(".level"))
                    return "FINEST";
                if (key.equals("java.util.logging.ConsoleHandler.level"))
                    return "FINEST";
                return oldVal;
            });
        } catch (IOException ex) {
            adjustLogLevel.addError("Could not adjust the log level", ex);
        } finally {
            adjustLogLevel.stop();
        }
    }

    private void shouldCreateAssetCache() {
        if (commandLine.hasOption("createAssetCache")) {
            JsonObject assets = new AssetDictionary().createAssets(new File(commandLine.getOptionValue("createAssetCache")));
            File assetsFile = new File("vanillaassets.json");
            try {
                Files.writeString(assetsFile.toPath(), assets.toString());
            } catch (IOException ex) {
                log.error("Could not saved " + assetsFile.getPath(), ex);
            }
            System.exit(0);
        }
    }

    private CommandLine initCLI(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("help", false, "prints the help");
        options.addOption("rp", "resourcepack", true, "specifies the path to the resourcepack to be validated");
        options.addOption("v", "verbose", false, "sets the log level to DEBUG");
        options.addOption("config", true, "specifies the path of the configuration file");
        options.addOption("report", true, "specifies the path for the generated report file");
        options.addOption("createAssetCache", true, "creates a new vanilla asset cache from the given directory");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption("help")) {
            new HelpFormatter().printHelp("Resource Pack Validator", options);
            return null;
        }
        return commandLine;
    }

    private Config initConfig() {
        File file = new File("config.json");
        if (commandLine.hasOption("config"))
            file = new File(commandLine.getOptionValue("config"));
        if (!file.exists()) {
            log.warn("Could not load configuration at {}. Using default configuration.", file.getAbsolutePath());
            return new Config();
        }
        try {
            return new Gson().fromJson(new FileReader(file), Config.class);
        } catch (Exception ex) {
            log.warn("Could not load configuration at " + file.getAbsolutePath(), ex);
        }
        return new Config();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (cleanupOnShutdown != null)
                    FileUtils.deleteDirectory(cleanupOnShutdown);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }));
    }

    private File getRootDirectory() {
        TestCase rootDirectoryValid = testSuite.getCase("Root directory valid").start();
        File rootDir = new File("resourcepack");
        if (commandLine.hasOption("rp")) {
            String rpOption = commandLine.getOptionValue("rp");
            rootDir = new File(rpOption);
        } else if (!rootDir.exists()) {
            rootDir = new File("resourcepack.zip");
        }

        if (!rootDir.exists()) {
            rootDirectoryValid.addError("Could not find directory " + rootDir.getAbsolutePath());
            rootDirectoryValid.stop();
            return null;
        }

        if (rootDir.getName().endsWith(".zip")) {
            try {
                File tmpDirectory = new File(rootDir.getName().substring(0, rootDir.getName().length() - 4));
                log.debug("Using zip resourcepack: extracting files to {}", tmpDirectory.getPath());
                FileUtils.deleteDirectory(tmpDirectory);
                if (tmpDirectory.mkdir()) {
                    ZipUtils.extractFiles(rootDir, tmpDirectory);
                    rootDir = tmpDirectory;
                    this.cleanupOnShutdown = tmpDirectory;
                }
            } catch (IOException ex) {
                log.error("Could not create tmp directory to extract resourcepack", ex);
            }
        }

        if (!rootDir.isDirectory()) {
            rootDirectoryValid.addError("Path " + rootDir.getAbsolutePath() + " points to a file, but needs to be a directory");
            rootDirectoryValid.stop();
            return null;
        }
        rootDirectoryValid.stop();

        return rootDir;
    }

    private boolean validate(File rootDir, ValidatorRegistry registry) {
        log.info("Starting validation of {}", rootDir.getPath());
        ValidationJob validation = new ValidationJob(rootDir, registry);
        return validation.validate();
    }

}
